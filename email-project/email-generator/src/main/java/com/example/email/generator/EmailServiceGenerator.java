package com.example.email.generator;

import com.example.email.core.generator.EmailDefinition;
import com.example.email.core.model.Email;
import com.example.email.core.service.EmailTemplateService;
import com.example.email.core.template.TemplateEngine;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmailServiceGenerator {
    private final File outputDirectory;
    private final String packageName;
    private final String serviceClassName;
    private final boolean includeRenderMethod;
    private final GeneratorLogger logger;
    private final ParameterClassGenerator parameterClassGenerator;

    public EmailServiceGenerator(
            File outputDirectory,
            String packageName,
            String serviceClassName,
            boolean includeRenderMethod,
            GeneratorLogger logger,
            ParameterClassGenerator parameterClassGenerator) {
        this.outputDirectory = outputDirectory;
        this.packageName = packageName;
        this.serviceClassName = serviceClassName;
        this.includeRenderMethod = includeRenderMethod;
        this.logger = logger;
        this.parameterClassGenerator = parameterClassGenerator;
    }

    public void generateEmailService(List<EmailDefinition> definitions) throws IOException {
        logger.info("Generating email service class: " + serviceClassName);

        TypeSpec.Builder serviceBuilder = TypeSpec.classBuilder(serviceClassName)
                .superclass(EmailTemplateService.class)
                .addModifiers(Modifier.PUBLIC);

        // Add constructor
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(TemplateEngine.class), "templateEngine")
                .addParameter(ClassName.get(EmailTemplateService.EmailConfig.class), "emailConfig")
                .addStatement("super(templateEngine, emailConfig)")
                .build();

        serviceBuilder.addMethod(constructor);

        // Generate parameter classes and methods for each email type
        for (EmailDefinition email : definitions) {
            // Generate parameter class for this email type
            ClassName paramClassName = parameterClassGenerator.generateParameterClass(email);

            // Generate service method with parameter class
            generateServiceMethod(serviceBuilder, email, paramClassName);

            // Generate render method if needed
            if (includeRenderMethod) {
                generateRenderMethod(serviceBuilder, email, paramClassName);
            }
        }

        // Create the JavaFile and write to output directory
        JavaFile javaFile = JavaFile.builder(packageName, serviceBuilder.build()).build();
        javaFile.writeTo(outputDirectory);

        logger.info("Generated email service class: " + serviceClassName);
    }

    private void generateServiceMethod(
            TypeSpec.Builder serviceBuilder,
            EmailDefinition email,
            ClassName paramClassName) {

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(email.getMethodName())
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(Email.Builder.class))
                .addJavadoc("Create an email builder for the $L template\n", email.getIdentifier())
                .addJavadoc("<p>Default subject: $L</p>\n", email.getSubject())
                .addParameter(ParameterSpec.builder(paramClassName, "params").build())
                .addJavadoc("@param params The parameters for this email template\n")
                .addJavadoc("@return An email builder configured with the template content\n");

        // Create the method body
        methodBuilder.addStatement("$T<$T, $T> model = new $T<>()",
                Map.class, String.class, Object.class, HashMap.class);

        // Add variables to model from params
        for (int i = 0; i < email.getVariables().size(); i++) {
            String varName = email.getVariables().get(i).getName();
            methodBuilder.addStatement("model.put($S, params.get$L())",
                    varName, capitalizeFirst(varName));
        }

        // Set subject
        methodBuilder.addStatement("$T actualSubject = params.getSubject() != null ? params.getSubject() : $S",
                ClassName.get(String.class), email.getSubject());
        methodBuilder.addStatement("model.put($S, actualSubject)", "subject");

        // Process template and return Email builder
        methodBuilder.addStatement("$T content = processTemplate($S, model)",
                ClassName.get(String.class),
                email.getTemplatePath());

        methodBuilder.addStatement("return createEmailBuilder()" +
                "\n                .subject(actualSubject)" +
                "\n                .content(content)" +
                "\n                .html(true)");

        serviceBuilder.addMethod(methodBuilder.build());
    }

    private void generateRenderMethod(
            TypeSpec.Builder serviceBuilder,
            EmailDefinition email,
            ClassName paramClassName) {

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(email.getRenderMethodName())
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(String.class))
                .addJavadoc("Render the $L template without creating an email\n", email.getIdentifier())
                .addParameter(ParameterSpec.builder(paramClassName, "params").build())
                .addJavadoc("@param params The parameters for this email template\n")
                .addJavadoc("@return The rendered HTML content\n");

        // Create the method body
        methodBuilder.addStatement("$T<$T, $T> model = new $T<>()",
                Map.class, String.class, Object.class, HashMap.class);

        // Add variables to model from params
        for (int i = 0; i < email.getVariables().size(); i++) {
            String varName = email.getVariables().get(i).getName();
            methodBuilder.addStatement("model.put($S, params.get$L())",
                    varName, capitalizeFirst(varName));
        }

        // Set subject if available
        methodBuilder.addStatement("if (params.getSubject() != null) {");
        methodBuilder.addStatement("    model.put($S, params.getSubject())", "subject");
        methodBuilder.addStatement("} else {");
        methodBuilder.addStatement("    model.put($S, $S)", "subject", email.getSubject());
        methodBuilder.addStatement("}");

        // Process template and return content
        methodBuilder.addStatement("return processTemplate($S, model)",
                email.getTemplatePath());

        serviceBuilder.addMethod(methodBuilder.build());
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}

package com.example.email.generator;

import com.example.email.core.generator.EmailDefinition;
import com.example.email.core.generator.SectionDefinition;
import com.example.email.core.generator.VariableDefinition;
import com.example.email.core.model.Email;
import com.example.email.core.service.EmailConfig;
import com.example.email.core.service.EmailTemplateService;
import com.example.email.core.template.TemplateEngine;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
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

/**
 * Updated EmailServiceGenerator with simplified model structure
 */
public class EmailServiceGenerator {
    // Existing fields
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
                .addParameter(ClassName.get(EmailConfig.class), "emailConfig")
                .addStatement("super(templateEngine, emailConfig)")
                .build();

        serviceBuilder.addMethod(constructor);

        // Generate parameter classes and methods for each email type
        for (EmailDefinition email : definitions) {
            // Generate parameter class for this email type
            ClassName paramClassName = parameterClassGenerator.generateParameterClass(email);

            // Generate section parameter classes for each section with variables
            Map<String, ClassName> sectionParamClasses = new HashMap<>();
            for (SectionDefinition section : email.getSectionDefinitions()) {
                if (section.getVariables() != null && !section.getVariables().isEmpty()) {
                    String sectionClassName = section.getParameterClassName(email.getIdentifier());
                    sectionParamClasses.put(section.getName(),
                            ClassName.get(packageName, sectionClassName));
                }
            }

            // Generate service method with parameter class
            generateServiceMethod(serviceBuilder, email, paramClassName, sectionParamClasses);

            // Generate render method if needed
            if (includeRenderMethod) {
                generateRenderMethod(serviceBuilder, email, paramClassName, sectionParamClasses);
            }
        }

        // Create the JavaFile and write to output directory
        JavaFile javaFile = JavaFile.builder(packageName, serviceBuilder.build()).build();
        javaFile.writeTo(outputDirectory);

        logger.info("Generated email service class: " + serviceClassName);
    }

    /**
     * Generate service method with simplified model structure
     */
    private void generateServiceMethod(
            TypeSpec.Builder serviceBuilder,
            EmailDefinition email,
            ClassName paramClassName,
            Map<String, ClassName> sectionParamClasses) {

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(email.getMethodName())
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(Email.Builder.class))
                .addJavadoc("Create an email builder for the $L template\n", email.getIdentifier())
                .addJavadoc("<p>Default subject: $L</p>\n", email.getSubject())
                .addParameter(ParameterSpec.builder(paramClassName, "params").build())
                .addJavadoc("@param params The parameters for this email template\n")
                .addJavadoc("@return An email builder configured with the template content\n");

        // Create method body with simplified structure
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder()
                .addStatement("$T<$T, $T> model = new $T<>()",
                        Map.class, String.class, Object.class, HashMap.class);

        // Add main variables to model - these go at the top level
        for (VariableDefinition var : email.getVariables()) {
            codeBlockBuilder.addStatement("model.put($S, params.get$L())",
                    var.getName(), capitalizeFirst(var.getName()));
        }

        // Add section parameters with special handling for 'content' section
        for (Map.Entry<String, ClassName> entry : sectionParamClasses.entrySet()) {
            String sectionName = entry.getKey();
            SectionDefinition section = email.getSectionDefinition(sectionName).orElse(null);

            if (section != null && section.getVariables() != null && !section.getVariables().isEmpty()) {
                codeBlockBuilder.beginControlFlow("if (params.get$LParams() != null)", capitalizeFirst(sectionName));

                // For 'content' section, put variables directly in top level of model
                if ("content".equals(sectionName)) {
                    // Add content section variables directly to model top level
                    for (VariableDefinition var : section.getVariables()) {
                        codeBlockBuilder.addStatement("model.put($S, params.get$LParams().get$L())",
                                var.getName(), // no section prefix for content variables
                                capitalizeFirst(sectionName), capitalizeFirst(var.getName()));
                    }
                } else {
                    // For other sections, create a single section map
                    codeBlockBuilder.addStatement("$T<$T, $T> " + sectionName + "Map = new $T<>()",
                            Map.class, String.class, Object.class, HashMap.class);

                    // Add section variables directly to section map
                    for (VariableDefinition var : section.getVariables()) {
                        codeBlockBuilder.addStatement(sectionName + "Map.put($S, params.get$LParams().get$L())",
                                var.getName(),
                                capitalizeFirst(sectionName), capitalizeFirst(var.getName()));
                    }

                    // Add the section map to the model
                    codeBlockBuilder.addStatement("model.put($S, " + sectionName + "Map)", sectionName);
                }

                codeBlockBuilder.endControlFlow();
            }
        }

        // Set subject (top level variable)
        codeBlockBuilder.addStatement("String actualSubject = params.getSubject() != null ? params.getSubject() : $S",
                        email.getSubject())
                .addStatement("model.put($S, actualSubject)", "subject");

        // Process template and create email
        codeBlockBuilder.addStatement("String content = processTemplate($S, model)",
                        email.getTemplatePath())
                .addStatement("return createEmailBuilder()" +
                        "\n        .subject(actualSubject)" +
                        "\n        .content(content)" +
                        "\n        .html(true)");

        methodBuilder.addCode(codeBlockBuilder.build());
        serviceBuilder.addMethod(methodBuilder.build());
    }

    /**
     * Generate render method with simplified model structure
     */
    private void generateRenderMethod(
            TypeSpec.Builder serviceBuilder,
            EmailDefinition email,
            ClassName paramClassName,
            Map<String, ClassName> sectionParamClasses) {

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(email.getRenderMethodName())
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(String.class))
                .addJavadoc("Render the $L template without creating an email\n", email.getIdentifier())
                .addParameter(ParameterSpec.builder(paramClassName, "params").build())
                .addJavadoc("@param params The parameters for this email template\n")
                .addJavadoc("@return The rendered HTML content\n");

        // Create method body with same structure as service method
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder()
                .addStatement("$T<$T, $T> model = new $T<>()",
                        Map.class, String.class, Object.class, HashMap.class);

        // Add main variables to model
        for (VariableDefinition var : email.getVariables()) {
            codeBlockBuilder.addStatement("model.put($S, params.get$L())",
                    var.getName(), capitalizeFirst(var.getName()));
        }

        // Add section parameters with special handling for 'content' section
        for (Map.Entry<String, ClassName> entry : sectionParamClasses.entrySet()) {
            String sectionName = entry.getKey();
            SectionDefinition section = email.getSectionDefinition(sectionName).orElse(null);

            if (section != null && section.getVariables() != null && !section.getVariables().isEmpty()) {
                codeBlockBuilder.beginControlFlow("if (params.get$LParams() != null)", capitalizeFirst(sectionName));

                // For 'content' section, put variables directly in top level of model
                if ("content".equals(sectionName)) {
                    // Add content section variables directly to model top level
                    for (VariableDefinition var : section.getVariables()) {
                        codeBlockBuilder.addStatement("model.put($S, params.get$LParams().get$L())",
                                var.getName(), // no section prefix for content variables
                                capitalizeFirst(sectionName), capitalizeFirst(var.getName()));
                    }
                } else {
                    // For other sections, create a single section map
                    codeBlockBuilder.addStatement("$T<$T, $T> " + sectionName + "Map = new $T<>()",
                            Map.class, String.class, Object.class, HashMap.class);

                    // Add section variables directly to section map
                    for (VariableDefinition var : section.getVariables()) {
                        codeBlockBuilder.addStatement(sectionName + "Map.put($S, params.get$LParams().get$L())",
                                var.getName(),
                                capitalizeFirst(sectionName), capitalizeFirst(var.getName()));
                    }

                    // Add the section map to the model
                    codeBlockBuilder.addStatement("model.put($S, " + sectionName + "Map)", sectionName);
                }

                codeBlockBuilder.endControlFlow();
            }
        }

        // Set subject
        codeBlockBuilder.beginControlFlow("if (params.getSubject() != null)")
                .addStatement("model.put($S, params.getSubject())", "subject")
                .nextControlFlow("else")
                .addStatement("model.put($S, $S)", "subject", email.getSubject())
                .endControlFlow();

        // Process template and return content
        codeBlockBuilder.addStatement("return processTemplate($S, model)",
                email.getTemplatePath());

        methodBuilder.addCode(codeBlockBuilder.build());
        serviceBuilder.addMethod(methodBuilder.build());
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}

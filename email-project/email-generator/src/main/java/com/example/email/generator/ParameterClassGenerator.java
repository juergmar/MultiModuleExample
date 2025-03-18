package com.example.email.generator;

import com.example.email.core.generator.EmailDefinition;
import com.example.email.core.generator.VariableDefinition;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class ParameterClassGenerator {
    private final File outputDirectory;
    private final String packageName;
    private final String nullableAnnotation;
    private final boolean useLombok;
    private final GeneratorLogger logger;

    public ParameterClassGenerator(File outputDirectory, String packageName,
                                   String nullableAnnotation, boolean useLombok,
                                   GeneratorLogger logger) {
        this.outputDirectory = outputDirectory;
        this.packageName = packageName;
        this.nullableAnnotation = nullableAnnotation;
        this.useLombok = useLombok;
        this.logger = logger;
    }

    public ClassName generateParameterClass(EmailDefinition emailDefinition) throws IOException {
        String className = capitalizeFirst(emailDefinition.getIdentifier()) + "Params";

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC);

        // Add Lombok annotations if enabled
        if (useLombok) {
            classBuilder.addAnnotation(ClassName.get("lombok", "Data"))
                    .addAnnotation(ClassName.get("lombok", "Builder"))
                    .addAnnotation(ClassName.get("lombok", "NoArgsConstructor"))
                    .addAnnotation(ClassName.get("lombok", "AllArgsConstructor"));
        }

        // Add class JavaDoc
        classBuilder.addJavadoc("Parameters for the $L email template.\n", emailDefinition.getIdentifier());

        // Set to collect unique imports
        Set<String> imports = emailDefinition.getVariables().stream()
                .flatMap(v -> v.getRequiredImports().stream())
                .collect(Collectors.toSet());

        // Process each variable
        for (VariableDefinition variable : emailDefinition.getVariables()) {
            addField(classBuilder, variable);
        }

        // Add subject field
        FieldSpec.Builder subjectFieldBuilder = FieldSpec.builder(
                ClassName.get(String.class), "subject", Modifier.PRIVATE);
        subjectFieldBuilder.addJavadoc("Custom subject line. If null, the default will be used.");

        if (!useLombok) {
            subjectFieldBuilder.addAnnotation(
                    ClassName.bestGuess(nullableAnnotation));
        }

        classBuilder.addField(subjectFieldBuilder.build());

        // Generate non-Lombok constructors, getters, setters if needed
        if (!useLombok) {
            addConstructors(classBuilder, emailDefinition);
            addGettersAndSetters(classBuilder, emailDefinition);
            addBuilderClass(classBuilder, emailDefinition);
        }

        // Write the class to the output directory
        JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
        javaFile.writeTo(outputDirectory);

        logger.info("Generated parameter class: " + className);

        return ClassName.get(packageName, className);
    }

    private void addField(TypeSpec.Builder classBuilder, VariableDefinition variable) {
        FieldSpec.Builder fieldBuilder = FieldSpec.builder(
                variable.getTypeName(), variable.getName(), Modifier.PRIVATE);

        fieldBuilder.addJavadoc("$L", variable.getDescription());

        if (variable.isRequired() && useLombok) {
            fieldBuilder.addAnnotation(ClassName.get("lombok", "NonNull"));
        } else if (!variable.isRequired()) {
            fieldBuilder.addAnnotation(ClassName.bestGuess(nullableAnnotation));
        }

        classBuilder.addField(fieldBuilder.build());
    }

    private void addConstructors(TypeSpec.Builder classBuilder, EmailDefinition emailDefinition) {
        // Add no-args constructor
        MethodSpec.Builder noArgsConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        classBuilder.addMethod(noArgsConstructor.build());

        // Add all-args constructor
        MethodSpec.Builder allArgsConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (VariableDefinition variable : emailDefinition.getVariables()) {
            ParameterSpec.Builder param = ParameterSpec.builder(
                    variable.getTypeName(), variable.getName());

            if (variable.isRequired()) {
                // Could add validation here
            }

            allArgsConstructor.addParameter(param.build());
            allArgsConstructor.addStatement("this.$N = $N", variable.getName(), variable.getName());
        }

        // Add subject parameter
        allArgsConstructor.addParameter(
                ParameterSpec.builder(ClassName.get(String.class), "subject").build());
        allArgsConstructor.addStatement("this.subject = subject");

        classBuilder.addMethod(allArgsConstructor.build());
    }

    private void addGettersAndSetters(TypeSpec.Builder classBuilder, EmailDefinition emailDefinition) {
        // Add getters and setters for each field including subject
        for (VariableDefinition variable : emailDefinition.getVariables()) {
            String name = variable.getName();
            TypeName type = variable.getTypeName();

            // Getter
            MethodSpec getter = MethodSpec.methodBuilder("get" + capitalizeFirst(name))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(type)
                    .addStatement("return this.$N", name)
                    .build();

            // Setter
            MethodSpec setter = MethodSpec.methodBuilder("set" + capitalizeFirst(name))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ParameterSpec.builder(type, name).build())
                    .addStatement("this.$N = $N", name, name)
                    .build();

            classBuilder.addMethod(getter);
            classBuilder.addMethod(setter);
        }

        // Subject getter and setter
        classBuilder.addMethod(MethodSpec.methodBuilder("getSubject")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(String.class))
                .addStatement("return this.subject")
                .build());

        classBuilder.addMethod(MethodSpec.methodBuilder("setSubject")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(ClassName.get(String.class), "subject").build())
                .addStatement("this.subject = subject")
                .build());
    }

    private void addBuilderClass(TypeSpec.Builder classBuilder, EmailDefinition emailDefinition) {
        String className = capitalizeFirst(emailDefinition.getIdentifier()) + "Params";

        TypeSpec.Builder builderClass = TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        // Add fields
        for (VariableDefinition variable : emailDefinition.getVariables()) {
            builderClass.addField(variable.getTypeName(), variable.getName(), Modifier.PRIVATE);
        }

        builderClass.addField(ClassName.get(String.class), "subject", Modifier.PRIVATE);

        // Add builder methods
        for (VariableDefinition variable : emailDefinition.getVariables()) {
            String name = variable.getName();
            TypeName type = variable.getTypeName();

            MethodSpec method = MethodSpec.methodBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.get("", "Builder"))
                    .addParameter(ParameterSpec.builder(type, name).build())
                    .addStatement("this.$N = $N", name, name)
                    .addStatement("return this")
                    .build();

            builderClass.addMethod(method);
        }

        // Add subject builder method
        MethodSpec subjectMethod = MethodSpec.methodBuilder("subject")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get("", "Builder"))
                .addParameter(ParameterSpec.builder(ClassName.get(String.class), "subject").build())
                .addStatement("this.subject = subject")
                .addStatement("return this")
                .build();

        builderClass.addMethod(subjectMethod);

        // Add build method
        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get("", className));

        // Validate required fields
        for (VariableDefinition variable : emailDefinition.getVariables()) {
            if (variable.isRequired()) {
                buildMethod.beginControlFlow("if ($N == null)", variable.getName())
                        .addStatement("throw new $T($S)",
                                IllegalArgumentException.class,
                                variable.getName() + " is required")
                        .endControlFlow();
            }
        }

        // Create instance
        buildMethod.addStatement("$L result = new $L()", className, className);

        for (VariableDefinition variable : emailDefinition.getVariables()) {
            buildMethod.addStatement("result.$N = this.$N", variable.getName(), variable.getName());
        }

        buildMethod.addStatement("result.subject = this.subject");
        buildMethod.addStatement("return result");

        builderClass.addMethod(buildMethod.build());

        // Add static builder method to parent class
        MethodSpec staticBuilder = MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get("", "Builder"))
                .addStatement("return new Builder()")
                .build();

        classBuilder.addMethod(staticBuilder);

        // Add the builder class
        classBuilder.addType(builderClass.build());
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}

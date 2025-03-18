package com.example.email.generator;

import com.example.email.core.generator.EmailDefinition;
import com.example.email.core.generator.SectionDefinition;
import com.example.email.core.generator.VariableDefinition;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ParameterClassGenerator {
    private final File outputDirectory;
    private final String packageName;
    private final String nullableAnnotation;
    private final boolean useLombok;
    private final GeneratorLogger logger;
    private final Map<String, ClassName> generatedSectionClasses = new HashMap<>();

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

        // Process main parameters
        for (VariableDefinition variable : emailDefinition.getVariables()) {
            addField(classBuilder, variable);
        }

        // Generate and add section parameter classes
        Map<String, ClassName> sectionParamClasses = new HashMap<>();
        for (SectionDefinition section : emailDefinition.getSectionDefinitions()) {
            if (!section.getVariables().isEmpty()) {
                ClassName sectionParamClass = generateSectionParameterClass(emailDefinition, section);
                sectionParamClasses.put(section.getName(), sectionParamClass);

                // Add field for section parameters
                FieldSpec.Builder fieldBuilder = FieldSpec.builder(
                        sectionParamClass, section.getName() + "Params", Modifier.PRIVATE);

                fieldBuilder.addJavadoc("Parameters for the $L section.\n", section.getName());

                if (!useLombok) {
                    fieldBuilder.addAnnotation(ClassName.bestGuess(nullableAnnotation));
                }

                classBuilder.addField(fieldBuilder.build());
            }
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
            addConstructors(classBuilder, emailDefinition, sectionParamClasses);
            addGettersAndSetters(classBuilder, emailDefinition, sectionParamClasses);
            addBuilderClass(classBuilder, emailDefinition, sectionParamClasses);
        }

        // Write the class to the output directory
        JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
        javaFile.writeTo(outputDirectory);

        logger.info("Generated parameter class: " + className);

        return ClassName.get(packageName, className);
    }

    /**
     * Generate a parameter class for a section.
     *
     * @param emailDefinition The email definition
     * @param section The section definition
     * @return The class name of the generated parameter class
     * @throws IOException If there is an error writing the file
     */
    private ClassName generateSectionParameterClass(EmailDefinition emailDefinition, SectionDefinition section) throws IOException {
        String className = section.getParameterClassName(emailDefinition.getIdentifier());

        // Check if already generated
        String key = packageName + "." + className;
        if (generatedSectionClasses.containsKey(key)) {
            return generatedSectionClasses.get(key);
        }

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
        classBuilder.addJavadoc("Parameters for the $L section of the $L email template.\n",
                section.getName(), emailDefinition.getIdentifier());

        // Add section-specific variables
        for (VariableDefinition variable : section.getVariables()) {
            addField(classBuilder, variable);
        }

        // Generate non-Lombok constructors, getters, setters if needed
        if (!useLombok) {
            addSectionConstructors(classBuilder, section);
            addSectionGettersAndSetters(classBuilder, section);
            addSectionBuilderClass(classBuilder, section, className);
        }

        // Write the class to the output directory
        JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
        javaFile.writeTo(outputDirectory);

        logger.info("Generated section parameter class: " + className);

        ClassName sectionClassName = ClassName.get(packageName, className);
        generatedSectionClasses.put(key, sectionClassName);
        return sectionClassName;
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

    private void addConstructors(TypeSpec.Builder classBuilder, EmailDefinition emailDefinition,
                                 Map<String, ClassName> sectionParamClasses) {
        // Add no-args constructor
        MethodSpec.Builder noArgsConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        classBuilder.addMethod(noArgsConstructor.build());

        // Add all-args constructor
        MethodSpec.Builder allArgsConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        // Add main variables
        for (VariableDefinition variable : emailDefinition.getVariables()) {
            ParameterSpec.Builder param = ParameterSpec.builder(
                    variable.getTypeName(), variable.getName());

            allArgsConstructor.addParameter(param.build());
            allArgsConstructor.addStatement("this.$N = $N", variable.getName(), variable.getName());
        }

        // Add section parameters
        for (Map.Entry<String, ClassName> entry : sectionParamClasses.entrySet()) {
            String paramName = entry.getKey() + "Params";
            ParameterSpec.Builder param = ParameterSpec.builder(entry.getValue(), paramName);

            allArgsConstructor.addParameter(param.build());
            allArgsConstructor.addStatement("this.$N = $N", paramName, paramName);
        }

        // Add subject parameter
        allArgsConstructor.addParameter(
                ParameterSpec.builder(ClassName.get(String.class), "subject").build());
        allArgsConstructor.addStatement("this.subject = subject");

        classBuilder.addMethod(allArgsConstructor.build());
    }

    private void addGettersAndSetters(TypeSpec.Builder classBuilder, EmailDefinition emailDefinition,
                                      Map<String, ClassName> sectionParamClasses) {
        // Add getters and setters for main variables
        for (VariableDefinition variable : emailDefinition.getVariables()) {
            addGetterAndSetter(classBuilder, variable.getName(), variable.getTypeName());
        }

        // Add getters and setters for section parameters
        for (Map.Entry<String, ClassName> entry : sectionParamClasses.entrySet()) {
            String fieldName = entry.getKey() + "Params";
            addGetterAndSetter(classBuilder, fieldName, entry.getValue());
        }

        // Subject getter and setter
        addGetterAndSetter(classBuilder, "subject", ClassName.get(String.class));
    }

    private void addGetterAndSetter(TypeSpec.Builder classBuilder, String name, TypeName type) {
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

    private void addBuilderClass(TypeSpec.Builder classBuilder, EmailDefinition emailDefinition,
                                 Map<String, ClassName> sectionParamClasses) {
        String className = capitalizeFirst(emailDefinition.getIdentifier()) + "Params";

        TypeSpec.Builder builderClass = TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        // Add fields for main variables
        for (VariableDefinition variable : emailDefinition.getVariables()) {
            builderClass.addField(variable.getTypeName(), variable.getName(), Modifier.PRIVATE);
        }

        // Add fields for section parameters
        for (Map.Entry<String, ClassName> entry : sectionParamClasses.entrySet()) {
            String fieldName = entry.getKey() + "Params";
            builderClass.addField(entry.getValue(), fieldName, Modifier.PRIVATE);
        }

        builderClass.addField(ClassName.get(String.class), "subject", Modifier.PRIVATE);

        // Add builder methods for main variables
        for (VariableDefinition variable : emailDefinition.getVariables()) {
            addBuilderMethod(builderClass, variable.getName(), variable.getTypeName());
        }

        // Add builder methods for section parameters
        for (Map.Entry<String, ClassName> entry : sectionParamClasses.entrySet()) {
            String fieldName = entry.getKey() + "Params";
            addBuilderMethod(builderClass, fieldName, entry.getValue());
        }

        // Add subject builder method
        addBuilderMethod(builderClass, "subject", ClassName.get(String.class));

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

        // Set main variables
        for (VariableDefinition variable : emailDefinition.getVariables()) {
            buildMethod.addStatement("result.$N = this.$N", variable.getName(), variable.getName());
        }

        // Set section parameters
        for (Map.Entry<String, ClassName> entry : sectionParamClasses.entrySet()) {
            String fieldName = entry.getKey() + "Params";
            buildMethod.addStatement("result.$N = this.$N", fieldName, fieldName);
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

    private void addBuilderMethod(TypeSpec.Builder builderClass, String name, TypeName type) {
        MethodSpec method = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get("", "Builder"))
                .addParameter(ParameterSpec.builder(type, name).build())
                .addStatement("this.$N = $N", name, name)
                .addStatement("return this")
                .build();

        builderClass.addMethod(method);
    }

    // Methods for section parameter classes
    private void addSectionConstructors(TypeSpec.Builder classBuilder, SectionDefinition section) {
        // No-args constructor
        MethodSpec.Builder noArgsConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        classBuilder.addMethod(noArgsConstructor.build());

        // All-args constructor
        MethodSpec.Builder allArgsConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (VariableDefinition variable : section.getVariables()) {
            ParameterSpec.Builder param = ParameterSpec.builder(
                    variable.getTypeName(), variable.getName());

            allArgsConstructor.addParameter(param.build());
            allArgsConstructor.addStatement("this.$N = $N", variable.getName(), variable.getName());
        }

        classBuilder.addMethod(allArgsConstructor.build());
    }

    private void addSectionGettersAndSetters(TypeSpec.Builder classBuilder, SectionDefinition section) {
        for (VariableDefinition variable : section.getVariables()) {
            addGetterAndSetter(classBuilder, variable.getName(), variable.getTypeName());
        }
    }

    private void addSectionBuilderClass(TypeSpec.Builder classBuilder, SectionDefinition section, String fullClassName) {
        TypeSpec.Builder builderClass = TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        // Add fields
        for (VariableDefinition variable : section.getVariables()) {
            builderClass.addField(variable.getTypeName(), variable.getName(), Modifier.PRIVATE);
        }

        // Add builder methods
        for (VariableDefinition variable : section.getVariables()) {
            addBuilderMethod(builderClass, variable.getName(), variable.getTypeName());
        }

        // Add build method
        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get("", fullClassName));

        // Validate required fields
        for (VariableDefinition variable : section.getVariables()) {
            if (variable.isRequired()) {
                buildMethod.beginControlFlow("if ($N == null)", variable.getName())
                        .addStatement("throw new $T($S)",
                                IllegalArgumentException.class,
                                variable.getName() + " is required")
                        .endControlFlow();
            }
        }

        // Create instance
        buildMethod.addStatement("$L result = new $L()",
                fullClassName,
                fullClassName);

        for (VariableDefinition variable : section.getVariables()) {
            buildMethod.addStatement("result.$N = this.$N", variable.getName(), variable.getName());
        }

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

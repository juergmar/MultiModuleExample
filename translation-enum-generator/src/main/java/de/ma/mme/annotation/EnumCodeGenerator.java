package de.ma.mme.annotation;

import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class EnumCodeGenerator {
    private final Filer filer;
    private final Messager messager;
    private final TranslationConfig config;
    private final CodeGenerationStrategy codeGenerationStrategy;

    public EnumCodeGenerator(Filer filer, Messager messager, TranslationConfig config) {
        this.filer = filer;
        this.messager = messager;
        this.config = config;
        this.codeGenerationStrategy = CodeGenerationStrategyFactory.createStrategy(config.useLombok());
    }

    public void generateEnum(Map<String, String> translations) throws IOException {
        String timestamp = generateTimestamp();

        // Create the base enum type builder
        TypeSpec.Builder enumBuilder = TypeSpec.enumBuilder(config.enumName())
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                .addJavadoc("Auto-generated Translations enum from $L\n", config.jsonPath())
                .addJavadoc("Generated on: $L\n", timestamp);

        // Apply the strategy to enhance the builder
        enumBuilder = codeGenerationStrategy.enhanceEnumBuilder(enumBuilder, timestamp, config.jsonPath());

        // Add enum constants
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            String enumName = entry.getKey();
            String translationValue = entry.getValue().replace("\"", "\\\"");

            enumBuilder.addEnumConstant(enumName,
                    TypeSpec.anonymousClassBuilder("$S", translationValue).build());
        }

        // Create the Java file
        JavaFile javaFile = JavaFile.builder(config.packageName(), enumBuilder.build())
                .build();

        // Write the file
        javaFile.writeTo(filer);

        logSuccess();
    }

    private void logSuccess() {
        messager.printMessage(Diagnostic.Kind.NOTE,
                "Generated " + config.fullyQualifiedEnumName() +
                        (config.useLombok() ? " with Lombok annotations" : ""));
    }

    private String generateTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}

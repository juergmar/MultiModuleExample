package de.ma.mme.annotation;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;

public class LombokCodeGenerationStrategy implements CodeGenerationStrategy {
    private static final ClassName GENERATED = ClassName.get("javax.annotation.processing", "Generated");
    private static final ClassName GETTER = ClassName.get("lombok", "Getter");
    private static final ClassName REQUIRED_ARGS_CONSTRUCTOR = ClassName.get("lombok", "RequiredArgsConstructor");
    private static final ClassName FIELD_DEFAULTS = ClassName.get("lombok.experimental", "FieldDefaults");
    private static final ClassName ACCESS_LEVEL = ClassName.get("lombok", "AccessLevel");

    @Override
    public TypeSpec.Builder enhanceEnumBuilder(TypeSpec.Builder enumBuilder, String timestamp, String jsonPath) {
        // Add Lombok-specific documentation
        enumBuilder.addJavadoc("Using Lombok for code generation\n");

        // Add Lombok annotations
        enumBuilder.addAnnotation(AnnotationSpec.builder(GENERATED)
                .addMember("value", "$S", EnumCodeGenerator.class.getCanonicalName())
                .addMember("date", "$S", timestamp)
                .build());

        enumBuilder.addAnnotation(GETTER);
        enumBuilder.addAnnotation(REQUIRED_ARGS_CONSTRUCTOR);

        enumBuilder.addAnnotation(AnnotationSpec.builder(FIELD_DEFAULTS)
                .addMember("makeFinal", "$L", true)
                .addMember("level", "$T.$L", ACCESS_LEVEL, "PRIVATE")
                .build());

        // Add the translation field (Lombok will generate constructor and getter)
        enumBuilder.addField(FieldSpec.builder(String.class, "translation")
                .build());

        return enumBuilder;
    }
}

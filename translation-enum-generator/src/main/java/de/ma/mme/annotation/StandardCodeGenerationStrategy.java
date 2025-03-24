package de.ma.mme.annotation;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

public class StandardCodeGenerationStrategy implements CodeGenerationStrategy {

    @Override
    public TypeSpec.Builder enhanceEnumBuilder(TypeSpec.Builder enumBuilder, String timestamp, String jsonPath) {
        // Add the translation field
        enumBuilder.addField(String.class, "translation", Modifier.PRIVATE, Modifier.FINAL);

        // Add constructor
        enumBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(String.class, "translation")
                .addStatement("this.translation = translation")
                .build());

        // Add getter method
        enumBuilder.addMethod(MethodSpec.methodBuilder("getTranslation")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return translation")
                .build());

        return enumBuilder;
    }
}

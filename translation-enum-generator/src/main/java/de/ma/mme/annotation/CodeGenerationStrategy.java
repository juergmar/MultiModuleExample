package de.ma.mme.annotation;

import com.squareup.javapoet.TypeSpec;

public interface CodeGenerationStrategy {
    TypeSpec.Builder enhanceEnumBuilder(TypeSpec.Builder enumBuilder, String timestamp, String jsonPath);
}

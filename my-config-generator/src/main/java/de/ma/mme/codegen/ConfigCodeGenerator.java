package de.ma.mme.codegen;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.javapoet.*;

import javax.annotation.Generated;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigCodeGenerator {
    private final JsonProcessor jsonProcessor;

    public ConfigCodeGenerator(JsonProcessor jsonProcessor) {
        this.jsonProcessor = jsonProcessor;
    }

    public void generateCode(ConfigGeneratorOptions options) throws IOException {
        JsonNode rootNode = jsonProcessor.parseJsonFile(options);
        Map<String, JsonNodeInfo> rootFields = jsonProcessor.extractRootFields(rootNode);

        TypeSpec typeSpec = generateTypeSpec(rootNode, rootFields);
        JavaFile javaFile = JavaFile.builder(options.packageName(), typeSpec).build();

        javaFile.writeTo(options.outputDirectory());
    }

    private TypeSpec generateTypeSpec(JsonNode rootNode, Map<String, JsonNodeInfo> rootFields) {
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(rootFields.values().iterator().next().getMethodName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(createGeneratedAnnotation());

        addConfigField(typeSpecBuilder);
        addStaticInitializer(typeSpecBuilder, rootNode);
        addGenericGetterMethods(typeSpecBuilder);
        addTypedGetterMethods(typeSpecBuilder, rootFields);

        return typeSpecBuilder.build();
    }

    private void addConfigField(TypeSpec.Builder typeSpecBuilder) {
        ParameterizedTypeName mapType = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(Object.class)
        );

        FieldSpec configField = FieldSpec.builder(mapType, "CONFIG")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T<>()", HashMap.class)
                .build();

        typeSpecBuilder.addField(configField);
    }

    private void addStaticInitializer(TypeSpec.Builder typeSpecBuilder, JsonNode rootNode) {
        CodeBlock.Builder initializerBuilder = CodeBlock.builder();
        jsonProcessor.processJsonNode(initializerBuilder, rootNode, "CONFIG");
        typeSpecBuilder.addStaticBlock(initializerBuilder.build());
    }

    private void addGenericGetterMethods(TypeSpec.Builder typeSpecBuilder) {
        // get(String key)
        typeSpecBuilder.addMethod(MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(Object.class)
                .addParameter(String.class, "key")
                .addStatement("return CONFIG.get(key)")
                .build());

        // getString(String key)
        typeSpecBuilder.addMethod(MethodSpec.methodBuilder("getString")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(String.class)
                .addParameter(String.class, "key")
                .addStatement("return (String) CONFIG.get(key)")
                .build());

        // getInteger(String key)
        typeSpecBuilder.addMethod(MethodSpec.methodBuilder("getInteger")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(Integer.class)
                .addParameter(String.class, "key")
                .addStatement("Object value = CONFIG.get(key)")
                .beginControlFlow("if (value instanceof Number)")
                .addStatement("return ((Number) value).intValue()")
                .endControlFlow()
                .addStatement("return null")
                .build());

        // getBoolean(String key)
        typeSpecBuilder.addMethod(MethodSpec.methodBuilder("getBoolean")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(Boolean.class)
                .addParameter(String.class, "key")
                .addStatement("return (Boolean) CONFIG.get(key)")
                .build());

        // getMap(String key)
        ParameterizedTypeName mapType = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(Object.class)
        );

        typeSpecBuilder.addMethod(MethodSpec.methodBuilder("getMap")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(mapType)
                .addParameter(String.class, "key")
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "$S", "unchecked")
                        .build())
                .addStatement("Object value = CONFIG.get(key)")
                .beginControlFlow("if (value instanceof Map)")
                .addStatement("return (Map<String, Object>) value")
                .endControlFlow()
                .addStatement("return $T.emptyMap()", Collections.class)
                .build());
    }

    private void addTypedGetterMethods(TypeSpec.Builder typeSpecBuilder, Map<String, JsonNodeInfo> nodeInfoMap) {
        nodeInfoMap.values().forEach(nodeInfo -> {
            typeSpecBuilder.addMethod(MethodSpec.methodBuilder(nodeInfo.getMethodName())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(nodeInfo.getTypeName())
                    .addStatement(nodeInfo.getGetterStatement())
                    .build());
        });
    }

    private AnnotationSpec createGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", ConfigCodeGenerator.class.getName())
                .addMember("date", "$S", java.time.ZonedDateTime.now().toString())
                .addMember("comments", "$S", "https://github.com/example/config-generator")
                .build();
    }
}

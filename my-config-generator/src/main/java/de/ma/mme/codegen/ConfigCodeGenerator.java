package de.ma.mme.codegen;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Generated;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigCodeGenerator {
    private final JsonProcessor jsonProcessor;
    private final Map<String, TypeName> typeNameCache = new HashMap<>();

    public ConfigCodeGenerator(JsonProcessor jsonProcessor) {
        this.jsonProcessor = jsonProcessor;
    }

    public void generateCode(ConfigGeneratorOptions options) throws IOException {
        JsonNode rootNode = jsonProcessor.parseJsonFile(options);
        JsonObjectInfo rootObject = jsonProcessor.processRootObject(rootNode, options.className());

        TypeSpec typeSpec = generateTypeSpec(rootObject);
        JavaFile javaFile = JavaFile.builder(options.packageName(), typeSpec).build();

        javaFile.writeTo(options.outputDirectory());
    }

    private TypeSpec generateTypeSpec(JsonObjectInfo objectInfo) {
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(objectInfo.className())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(createGeneratedAnnotation());

        // Add fields with initializers
        for (JsonFieldInfo field : objectInfo.fields()) {
            addField(typeSpecBuilder, field);
        }

        // Add constructor
        addConstructor(typeSpecBuilder, objectInfo);

        // Add getters for each field
        for (JsonFieldInfo field : objectInfo.fields()) {
            addGetter(typeSpecBuilder, field);
        }

        // Add nested classes if any
        for (JsonObjectInfo nestedObject : objectInfo.nestedObjects()) {
            TypeSpec nestedTypeSpec = generateTypeSpec(nestedObject);
            typeSpecBuilder.addType(nestedTypeSpec);
        }

        // Add toString method
        addToString(typeSpecBuilder, objectInfo);

        return typeSpecBuilder.build();
    }

    private void addField(TypeSpec.Builder typeSpec, JsonFieldInfo field) {
        TypeName typeName = resolveTypeName(field.type());

        FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(
                typeName,
                field.name(),
                Modifier.PRIVATE,
                Modifier.FINAL);

        typeSpec.addField(fieldSpecBuilder.build());
    }

    private void addConstructor(TypeSpec.Builder typeSpec, JsonObjectInfo objectInfo) {
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (JsonFieldInfo field : objectInfo.fields()) {
            String initializer = field.defaultValue();
            constructorBuilder.addStatement("this.$N = $L", field.name(), initializer);
        }

        typeSpec.addMethod(constructorBuilder.build());
    }

    private void addGetter(TypeSpec.Builder typeSpec, JsonFieldInfo field) {
        TypeName typeName = resolveTypeName(field.type());

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(field.getGetterName())
                .addModifiers(Modifier.PUBLIC)
                .returns(typeName)
                .addStatement("return $N", field.name());

        typeSpec.addMethod(methodBuilder.build());
    }

    private void addToString(TypeSpec.Builder typeSpec, JsonObjectInfo objectInfo) {
        MethodSpec.Builder toStringBuilder = MethodSpec.methodBuilder("toString")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class);

        StringBuilder sb = new StringBuilder();
        sb.append("return \"").append(objectInfo.className()).append("{\" + \n");

        for (int i = 0; i < objectInfo.fields().size(); i++) {
            JsonFieldInfo field = objectInfo.fields().get(i);
            sb.append("    \"").append(field.name()).append("=\" + ").append(field.name());

            if (i < objectInfo.fields().size() - 1) {
                sb.append(" + \", \" + \n");
            } else {
                sb.append(" + \"}\"");
            }
        }

        toStringBuilder.addStatement(sb.toString());
        typeSpec.addMethod(toStringBuilder.build());
    }

    private TypeName resolveTypeName(String typeStr) {
        if (typeNameCache.containsKey(typeStr)) {
            return typeNameCache.get(typeStr);
        }

        TypeName result;

        if (typeStr.startsWith("java.util.List<")) {
            // Extract the generic type
            String genericType = typeStr.substring(15, typeStr.length() - 1);

            // Resolve the component type
            TypeName componentType;
            if (genericType.contains(".")) {
                componentType = ClassName.bestGuess(genericType);
            } else {
                switch (genericType) {
                    case "String":
                        componentType = ClassName.get(String.class);
                        break;
                    case "Integer":
                        componentType = ClassName.get(Integer.class);
                        break;
                    case "Long":
                        componentType = ClassName.get(Long.class);
                        break;
                    case "Double":
                        componentType = ClassName.get(Double.class);
                        break;
                    case "Boolean":
                        componentType = ClassName.get(Boolean.class);
                        break;
                    default:
                        componentType = ClassName.get(Object.class);
                }
            }

            result = ParameterizedTypeName.get(
                    ClassName.get(List.class),
                    componentType
            );
        } else if (typeStr.contains(".")) {
            result = ClassName.bestGuess(typeStr);
        } else {
            switch (typeStr) {
                case "String":
                    result = ClassName.get(String.class);
                    break;
                case "Integer":
                    result = ClassName.get(Integer.class);
                    break;
                case "Long":
                    result = ClassName.get(Long.class);
                    break;
                case "Double":
                    result = ClassName.get(Double.class);
                    break;
                case "Boolean":
                    result = ClassName.get(Boolean.class);
                    break;
                default:
                    // Assume it's a nested class
                    result = ClassName.bestGuess(typeStr);
            }
        }

        typeNameCache.put(typeStr, result);
        return result;
    }

    private AnnotationSpec createGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", ConfigCodeGenerator.class.getName())
                .addMember("date", "$S", ZonedDateTime.now().toString())
                .build();
    }
}

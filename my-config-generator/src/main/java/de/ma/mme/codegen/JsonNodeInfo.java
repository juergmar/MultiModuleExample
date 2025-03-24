package de.ma.mme.codegen;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Map;

public record JsonNodeInfo(
        String key,
        JsonNode node
) {
    public String getMethodName() {
        return "get" + capitalize(key);
    }

    public TypeName getTypeName() {
        if (node.isNull()) {
            return ClassName.get(Object.class);
        } else if (node.isTextual()) {
            return ClassName.get(String.class);
        } else if (node.isInt()) {
            return ClassName.get(Integer.class);
        } else if (node.isBoolean()) {
            return ClassName.get(Boolean.class);
        } else if (node.isDouble() || node.isFloat()) {
            return ClassName.get(Double.class);
        } else if (node.isObject()) {
            return ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(Object.class)
            );
        } else {
            return ClassName.get(Object.class);
        }
    }

    public String getGetterStatement() {
        if (node.isObject()) {
            return String.format("return getMap(\"%s\")", key);
        } else {
            return String.format("return (%s) CONFIG.get(\"%s\")", getTypeName(), key);
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}

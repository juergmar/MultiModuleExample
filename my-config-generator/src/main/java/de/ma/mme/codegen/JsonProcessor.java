package de.ma.mme.codegen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonProcessor {
    private final ObjectMapper objectMapper;

    public JsonProcessor() {
        this.objectMapper = new ObjectMapper();
    }

    public JsonNode parseJsonFile(ConfigGeneratorOptions options) throws IOException {
        return objectMapper.readTree(options.jsonFile());
    }

    public JsonObjectInfo processRootObject(JsonNode rootNode, String className) {
        return processJsonObject(rootNode, className, null);
    }

    public JsonObjectInfo processJsonObject(JsonNode node, String className, String packageName) {
        List<JsonFieldInfo> fields = new ArrayList<>();
        List<JsonObjectInfo> nestedObjects = new ArrayList<>();

        Iterator<Map.Entry<String, JsonNode>> fieldIterator = node.fields();
        while (fieldIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = fieldIterator.next();
            String fieldName = entry.getKey();
            JsonNode fieldValue = entry.getValue();

            if (fieldValue.isObject()) {
                // Process nested object
                String nestedClassName = capitalize(fieldName);
                JsonObjectInfo nestedObject = processJsonObject(fieldValue, nestedClassName, packageName);
                nestedObjects.add(nestedObject);

                // Add field reference to the parent
                fields.add(createNestedObjectField(fieldName, nestedClassName));
            } else if (fieldValue.isArray()) {
                // Handle array types
                fields.add(processArrayField(fieldName, (ArrayNode)fieldValue));
            } else {
                // Handle primitive types
                fields.add(createPrimitiveField(fieldName, fieldValue));
            }
        }

        return new JsonObjectInfo(className, packageName, fields, nestedObjects);
    }

    private JsonFieldInfo createPrimitiveField(String fieldName, JsonNode value) {
        Class<?> fieldType = determineType(value);
        String defaultValue = determineDefaultValue(value);
        return new JsonFieldInfo(fieldName, fieldType.getCanonicalName(), defaultValue, false, null);
    }

    private JsonFieldInfo createNestedObjectField(String fieldName, String nestedClassName) {
        return new JsonFieldInfo(fieldName, nestedClassName, "new " + nestedClassName + "()", true, null);
    }

    private JsonFieldInfo processArrayField(String fieldName, ArrayNode arrayNode) {
        if (arrayNode.size() == 0) {
            return new JsonFieldInfo(fieldName, "java.util.List<Object>", "new java.util.ArrayList<>()", false, "List");
        }

        // Determine component type based on first element
        JsonNode firstElement = arrayNode.get(0);
        String componentType;

        if (firstElement.isObject()) {
            // We'd need more complex processing for object arrays
            componentType = "Object";
        } else {
            Class<?> elementType = determineType(firstElement);
            // Use proper wrapper types for primitives in generics
            componentType = elementType.getSimpleName();
        }

        return new JsonFieldInfo(
                fieldName,
                "java.util.List<" + componentType + ">",
                "new java.util.ArrayList<>()",
                false,
                "List"
        );
    }

    private Class<?> determineType(JsonNode node) {
        if (node.isTextual()) {
            return String.class;
        } else if (node.isInt()) {
            return Integer.class;
        } else if (node.isLong()) {
            return Long.class;
        } else if (node.isDouble() || node.isFloat()) {
            return Double.class;
        } else if (node.isBoolean()) {
            return Boolean.class;
        } else {
            return Object.class;
        }
    }

    private String determineDefaultValue(JsonNode node) {
        if (node.isNull()) {
            return "null";
        } else if (node.isTextual()) {
            return "\"" + node.asText().replace("\"", "\\\"") + "\"";
        } else if (node.isBoolean()) {
            return node.asBoolean() ? "true" : "false";
        } else if (node.isNumber()) {
            return node.asText();
        } else {
            return "null";
        }
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}

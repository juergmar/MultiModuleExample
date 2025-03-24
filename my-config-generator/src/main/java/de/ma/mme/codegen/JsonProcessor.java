package de.ma.mme.codegen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.CodeBlock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JsonProcessor {
    private final ObjectMapper objectMapper;

    public JsonProcessor() {
        this.objectMapper = new ObjectMapper();
    }

    public JsonNode parseJsonFile(ConfigGeneratorOptions options) throws IOException {
        return objectMapper.readTree(options.jsonFile());
    }

    public Map<String, JsonNodeInfo> extractRootFields(JsonNode rootNode) {
        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();
        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                                fieldsIterator,
                                Spliterator.ORDERED
                        ),
                        false
                )
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new JsonNodeInfo(entry.getKey(), entry.getValue())
                ));
    }

    public void processJsonNode(CodeBlock.Builder builder, JsonNode node, String mapVar) {
        if (node.isObject()) {
            processObjectNode(builder, node, mapVar);
        }
    }

    private void processObjectNode(CodeBlock.Builder builder, JsonNode node, String mapVar) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();

            if (value.isNull()) {
                builder.addStatement("$L.put($S, null)", mapVar, key);
            } else if (value.isValueNode()) {
                processValueNode(builder, mapVar, key, value);
            } else if (value.isObject()) {
                processNestedObject(builder, mapVar, key, value);
            }
        }
    }

    private void processValueNode(CodeBlock.Builder builder, String mapVar, String key, JsonNode value) {
        if (value.isTextual()) {
            builder.addStatement("$L.put($S, $S)", mapVar, key, value.asText());
        } else if (value.isBoolean()) {
            builder.addStatement("$L.put($S, $L)", mapVar, key, value.asBoolean());
        } else if (value.isInt()) {
            builder.addStatement("$L.put($S, $L)", mapVar, key, value.asInt());
        } else if (value.isDouble() || value.isFloat()) {
            builder.addStatement("$L.put($S, $L)", mapVar, key, value.asDouble());
        }
    }

    private void processNestedObject(CodeBlock.Builder builder, String mapVar, String key, JsonNode value) {
        builder.addStatement("$T<String, Object> $LMap = new $T<>()", Map.class, key, HashMap.class);
        processJsonNode(builder, value, key + "Map");
        builder.addStatement("$L.put($S, $LMap)", mapVar, key, key);
    }
}

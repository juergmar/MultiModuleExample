package de.ma.mme.codegen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;

@Mojo(name = "generate-config", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ConfigGeneratorMojo extends AbstractMojo {

    @Parameter(required = true)
    private File jsonFile;

    @Parameter(required = true)
    private String packageName;

    @Parameter(defaultValue = "AppConfig")
    private String className;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        File outputDirectory = new File(project.getBuild().getDirectory(), "generated-sources/config");
        outputDirectory.mkdirs();

        project.addCompileSourceRoot(outputDirectory.getPath());

        String packagePath = packageName.replace('.', '/');
        File packageDir = new File(outputDirectory, packagePath);
        packageDir.mkdirs();

        File outputFile = new File(packageDir, className + ".java");

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonFile);

            generateJavaFile(rootNode, outputFile);

            getLog().info("Generated configuration class: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            throw new MojoExecutionException("Error generating configuration class", e);
        }
    }

    private void generateJavaFile(JsonNode rootNode, File outputFile) throws IOException {
        try (Writer writer = new FileWriter(outputFile)) {
            writer.write("package " + packageName + ";\n\n");

            writer.write("import java.util.Collections;\n");
            writer.write("import java.util.HashMap;\n");
            writer.write("import java.util.Map;\n");
            writer.write("import java.util.Optional;\n\n");

            writer.write("public class " + className + " {\n\n");

            writer.write("    private static final Map<String, Object> CONFIG = new HashMap<>();\n\n");

            writer.write("    static {\n");
            processJsonNode(writer, rootNode, "CONFIG");
            writer.write("    }\n\n");

            generateGetterMethods(writer);
            generateTypedGetterMethods(writer, rootNode);

            writer.write("}\n");
        }
    }

    private void generateGetterMethods(Writer writer) throws IOException {
        writer.write("    public static Object get(String key) {\n");
        writer.write("        return CONFIG.get(key);\n");
        writer.write("    }\n\n");

        writer.write("    public static String getString(String key) {\n");
        writer.write("        return (String) CONFIG.get(key);\n");
        writer.write("    }\n\n");

        writer.write("    public static Integer getInteger(String key) {\n");
        writer.write("        Object value = CONFIG.get(key);\n");
        writer.write("        if (value instanceof Number) {\n");
        writer.write("            return ((Number) value).intValue();\n");
        writer.write("        }\n");
        writer.write("        return null;\n");
        writer.write("    }\n\n");

        writer.write("    public static Boolean getBoolean(String key) {\n");
        writer.write("        return (Boolean) CONFIG.get(key);\n");
        writer.write("    }\n\n");

        writer.write("    @SuppressWarnings(\"unchecked\")\n");
        writer.write("    public static Map<String, Object> getMap(String key) {\n");
        writer.write("        Object value = CONFIG.get(key);\n");
        writer.write("        if (value instanceof Map) {\n");
        writer.write("            return (Map<String, Object>) value;\n");
        writer.write("        }\n");
        writer.write("        return Collections.emptyMap();\n");
        writer.write("    }\n\n");
    }

    private void generateTypedGetterMethods(Writer writer, JsonNode rootNode) throws IOException {
        Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();

            String methodName = "get" + capitalize(key);
            String returnType = getNodeType(value);

            writer.write("    public static " + returnType + " " + methodName + "() {\n");
            if (value.isObject()) {
                writer.write("        return getMap(\"" + key + "\");\n");
            } else {
                writer.write("        return (" + returnType + ") CONFIG.get(\"" + key + "\");\n");
            }
            writer.write("    }\n\n");
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private String getNodeType(JsonNode node) {
        if (node.isTextual()) {
            return "String";
        } else if (node.isInt()) {
            return "Integer";
        } else if (node.isBoolean()) {
            return "Boolean";
        } else if (node.isDouble() || node.isFloat()) {
            return "Double";
        } else if (node.isObject()) {
            return "Map<String, Object>";
        } else {
            return "Object";
        }
    }

    private void processJsonNode(Writer writer, JsonNode node, String mapVar) throws IOException {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();

                if (value.isValueNode()) {
                    writer.write("        " + mapVar + ".put(\"" + key + "\", ");
                    if (value.isTextual()) {
                        writer.write("\"" + value.asText().replace("\"", "\\\"") + "\"");
                    } else if (value.isBoolean()) {
                        writer.write(value.asBoolean() ? "Boolean.TRUE" : "Boolean.FALSE");
                    } else if (value.isNumber()) {
                        writer.write(value.asText());
                    } else {
                        writer.write("null");
                    }
                    writer.write(");\n");
                } else if (value.isObject()) {
                    writer.write("        {\n");
                    writer.write("            Map<String, Object> nestedMap = new HashMap<>();\n");
                    processJsonNode(writer, value, "nestedMap");
                    writer.write("            " + mapVar + ".put(\"" + key + "\", nestedMap);\n");
                    writer.write("        }\n");
                }
            }
        }
    }
}

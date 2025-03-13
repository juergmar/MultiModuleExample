package de.ma.mme.annotation;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

public class EnumCodeGenerator {
    private final Filer filer;
    private final Messager messager;
    private final TranslationConfig config;

    public EnumCodeGenerator(Filer filer, Messager messager, TranslationConfig config) {
        this.filer = filer;
        this.messager = messager;
        this.config = config;
    }

    public void generateEnum(Map<String, String> translations) throws IOException {
        String qualifiedClassName = config.fullyQualifiedEnumName();
        FileObject sourceFile = filer.createSourceFile(qualifiedClassName);
        String timestamp = generateTimestamp();

        try (Writer writer = sourceFile.openWriter()) {
            writePackageDeclaration(writer);
            writeLombokImports(writer);
            writeJavaDoc(writer, timestamp);
            writeLombokAnnotations(writer, timestamp);
            writeEnumDeclaration(writer, translations);
        }

        logSuccess();
    }

    private void writePackageDeclaration(Writer writer) throws IOException {
        writer.write("package " + config.packageName() + ";\n\n");
    }

    private void writeLombokImports(Writer writer) throws IOException {
        if (config.useLombok()) {
            writer.write("import lombok.Getter;\n");
            writer.write("import lombok.RequiredArgsConstructor;\n");
            writer.write("import lombok.experimental.FieldDefaults;\n");
            writer.write("import javax.annotation.processing.Generated;\n\n");
        }
    }

    private void writeJavaDoc(Writer writer, String timestamp) throws IOException {
        writer.write("/**\n");
        writer.write(" * Auto-generated Translations enum from " + config.jsonPath() + "\n");
        writer.write(" * Generated on: " + timestamp + "\n");
        if (config.useLombok()) {
            writer.write(" * Using Lombok for code generation\n");
        }
        writer.write(" */\n");
    }

    private void writeLombokAnnotations(Writer writer, String timestamp) throws IOException {
        if (config.useLombok()) {
            writer.write("@Generated(\n");
            writer.write("    value = \"" + getClass().getCanonicalName() + "\",\n");
            writer.write("    date = \"" + timestamp + "\"\n");
            writer.write(")\n");
            writer.write("@Getter\n");
            writer.write("@RequiredArgsConstructor\n");
            writer.write("@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)\n");
        }
    }

    private void writeEnumDeclaration(Writer writer, Map<String, String> translations) throws IOException {
        writer.write("public enum " + config.enumName() + " {\n");

        String enumConstants = formatEnumConstants(translations);
        writer.write(enumConstants);

        if (config.useLombok()) {
            writer.write(EnumTemplate.LOMBOK_TEMPLATE);
        } else {
            String enumBody = EnumTemplate.STANDARD_TEMPLATE
                    .replace(EnumTemplate.ENUM_NAME_PLACEHOLDER, config.enumName());
            writer.write(enumBody);
        }
    }

    private String formatEnumConstants(Map<String, String> translations) {
        return translations.entrySet().stream()
                .map(this::formatEnumConstant)
                .collect(Collectors.joining(",\n", "", ";\n\n"));
    }

    private String formatEnumConstant(Map.Entry<String, String> entry) {
        String enumValue = entry.getKey();
        String translation = entry.getValue().replace("\"", "\\\"");
        return "    " + enumValue + "(\"" + translation + "\")";
    }

    private void logSuccess() {
        messager.printMessage(Diagnostic.Kind.NOTE,
                "Generated " + config.fullyQualifiedEnumName() +
                        (config.useLombok() ? " with Lombok annotations" : ""));
    }

    private String generateTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private static class EnumTemplate {
        static final String ENUM_NAME_PLACEHOLDER = "$ENUM_NAME$";

        static final String STANDARD_TEMPLATE =
                "    private final String translation;\n\n" +
                        "    " + ENUM_NAME_PLACEHOLDER + "(String translation) {\n" +
                        "        this.translation = translation;\n" +
                        "    }\n\n" +
                        "    public String getTranslation() {\n" +
                        "        return translation;\n" +
                        "    }\n" +
                        "}\n";

        static final String LOMBOK_TEMPLATE =
                "    String translation;\n" +
                        "}\n";
    }
}

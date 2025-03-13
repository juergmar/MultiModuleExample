package de.ma.mme.annotation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("de.ma.mme.annotation.GenerateTranslations")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class TranslationProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private ObjectMapper objectMapper;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateTranslations.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "@GenerateTranslations can only be applied to classes", element);
                continue;
            }

            TypeElement typeElement = (TypeElement) element;
            GenerateTranslations annotation = typeElement.getAnnotation(GenerateTranslations.class);
            String jsonPath = annotation.value();
            String enumName = annotation.enumName();

            // Determine target package
            String packageName = annotation.packageName();
            if (packageName.isEmpty()) {
                PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(typeElement);
                packageName = packageElement.getQualifiedName().toString();
            }

            try {
                // Read the JSON file from resources
                Map<String, String> translations = readTranslationsFromFile(jsonPath);
                if (translations.isEmpty()) {
                    messager.printMessage(Diagnostic.Kind.WARNING,
                            "No translations found in " + jsonPath, element);
                    continue;
                }

                // Generate the enum class
                generateEnumClass(packageName, enumName, translations);

                messager.printMessage(Diagnostic.Kind.NOTE,
                        "Generated " + packageName + "." + enumName + " from " + jsonPath);

            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "Failed to process translations: " + e.getMessage(), element);
            }
        }

        return true;
    }

    private Map<String, String> readTranslationsFromFile(String jsonPath) throws IOException {
        try {
            FileObject resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "", jsonPath);
            try (InputStream is = resource.openInputStream()) {
                return objectMapper.readValue(is, new TypeReference<HashMap<String, String>>() {});
            }
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.NOTE,
                    "Couldn't find " + jsonPath + " in output directory, trying classpath");

            // Try to find the file in the classpath
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(jsonPath)) {
                if (is == null) {
                    throw new IOException("Resource not found: " + jsonPath);
                }
                return objectMapper.readValue(is, new TypeReference<HashMap<String, String>>() {});
            }
        }
    }

    private void generateEnumClass(String packageName, String enumName, Map<String, String> translations)
            throws IOException {

        String className = packageName + "." + enumName;
        FileObject sourceFile = filer.createSourceFile(className);

        try (Writer writer = sourceFile.openWriter()) {
            writer.write("package " + packageName + ";\n\n");
            writer.write("/**\n");
            writer.write(" * Auto-generated Translations enum.\n");
            writer.write(" */\n");
            writer.write("public enum " + enumName + " {\n");

            int count = 0;
            for (Map.Entry<String, String> entry : translations.entrySet()) {
                String enumValue = entry.getKey();
                String translation = entry.getValue().replace("\"", "\\\"");

                writer.write("    " + enumValue + "(\"" + translation + "\")");
                if (count < translations.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
                count++;
            }

            writer.write("    ;\n\n");
            writer.write("    private final String translation;\n\n");
            writer.write("    " + enumName + "(String translation) {\n");
            writer.write("        this.translation = translation;\n");
            writer.write("    }\n\n");
            writer.write("    public String getTranslation() {\n");
            writer.write("        return translation;\n");
            writer.write("    }\n");
            writer.write("}\n");
        }
    }
}

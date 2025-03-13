package de.ma.mme.annotation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TranslationReader {
    private final ObjectMapper objectMapper;
    private final Filer filer;
    private final Messager messager;

    public TranslationReader(Filer filer, Messager messager) {
        this.objectMapper = new ObjectMapper();
        this.filer = filer;
        this.messager = messager;
    }

    public Map<String, String> loadTranslations(String jsonPath, Element element) throws IOException {
        try {
            return loadFromOutputDirectory(jsonPath);
        } catch (IOException firstAttemptException) {
            messager.printMessage(Diagnostic.Kind.NOTE,
                    "Attempting to load from classpath: " + jsonPath, element);
            return loadFromClasspath(jsonPath);
        }
    }

    private Map<String, String> loadFromOutputDirectory(String jsonPath) throws IOException {
        FileObject resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "", jsonPath);
        try (InputStream is = resource.openInputStream()) {
            return parseJsonToMap(is);
        }
    }

    private Map<String, String> loadFromClasspath(String jsonPath) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(jsonPath)) {
            if (is == null) {
                throw new ResourceNotFoundException(jsonPath);
            }
            return parseJsonToMap(is);
        }
    }

    private Map<String, String> parseJsonToMap(InputStream inputStream) throws IOException {
        return objectMapper.readValue(inputStream, new TypeReference<HashMap<String, String>>() {});
    }

    public static class ResourceNotFoundException extends IOException {
        private final String resourcePath;

        public ResourceNotFoundException(String resourcePath) {
            super("Resource not found: " + resourcePath);
            this.resourcePath = resourcePath;
        }

        public String getResourcePath() {
            return resourcePath;
        }
    }
}

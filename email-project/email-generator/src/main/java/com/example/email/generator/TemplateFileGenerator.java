package com.example.email.generator;

import com.example.email.core.generator.EmailDefinition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TemplateFileGenerator {
    private final File resourcesDirectory;
    private final GeneratorLogger logger;

    public TemplateFileGenerator(File resourcesDirectory, GeneratorLogger logger) {
        this.resourcesDirectory = resourcesDirectory;
        this.logger = logger;
    }

    public void generateTemplateFiles(List<EmailDefinition> definitions) throws IOException {
        if (definitions.isEmpty()) {
            logger.warn("No email definitions found. Skipping template generation.");
            return;
        }

        logger.info("Generating email templates in: " + resourcesDirectory.getAbsolutePath());

        for (EmailDefinition email : definitions) {
            generateTemplateFile(email);
        }
    }

    private void generateTemplateFile(EmailDefinition email) throws IOException {
        String templateFileName = email.getIdentifier() + ".html";
        File templateFile = new File(resourcesDirectory, templateFileName);

        logger.info("Generating template: " + templateFile.getName());

        try (FileWriter writer = new FileWriter(templateFile)) {
            writer.write(email.getTemplateText());
        } catch (IOException e) {
            logger.error("Failed to write template: " + templateFileName, e);
            throw e;
        }
    }
}

package com.example.email.generator;

import com.example.email.core.generator.EmailDefinition;
import com.example.email.core.generator.EmailDefinitions;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EmailDefinitionReader {
    private final File definitionsFile;
    private final GeneratorLogger logger;

    public EmailDefinitionReader(File definitionsFile, GeneratorLogger logger) {
        this.definitionsFile = definitionsFile;
        this.logger = logger;
    }

    public List<EmailDefinition> readDefinitions() throws IOException {
        try {
            logger.info("Reading email definitions from: " + definitionsFile.getAbsolutePath());
            ObjectMapper mapper = new ObjectMapper();
            EmailDefinitions definitions = mapper.readValue(definitionsFile, EmailDefinitions.class);

            List<EmailDefinition> emailDefinitions = Optional.ofNullable(definitions.getEmails())
                    .orElse(Collections.emptyList());

            logger.info("Found " + emailDefinitions.size() + " email definitions");
            validateDefinitions(emailDefinitions);

            return emailDefinitions;
        } catch (IOException e) {
            logger.error("Failed to read email definitions", e);
            throw e;
        }
    }

    private void validateDefinitions(List<EmailDefinition> definitions) {
        for (int i = 0; i < definitions.size(); i++) {
            EmailDefinition definition = definitions.get(i);

            if (definition.getIdentifier() == null || definition.getIdentifier().isEmpty()) {
                logger.warn("Email definition at index " + i + " has no identifier");
            }

            if (definition.getSubject() == null || definition.getSubject().isEmpty()) {
                logger.warn("Email definition '" + definition.getIdentifier() + "' has no subject");
            }

            if (definition.getTemplateText() == null || definition.getTemplateText().isEmpty()) {
                logger.warn("Email definition '" + definition.getIdentifier() + "' has no template text");
            }

            if (definition.getVariables() == null || definition.getVariables().isEmpty()) {
                logger.warn("Email definition '" + definition.getIdentifier() + "' has no variables");
            }
        }
    }
}

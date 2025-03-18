package com.example.email.generator;

import com.example.email.core.generator.EmailDefinition;
import com.example.email.core.generator.SectionDefinition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simplified TemplateFileGenerator that preserves Thymeleaf syntax from email definitions.
 * This generator assumes that email definitions already contain proper Thymeleaf syntax
 * and simply processes section references based on the model structure.
 */
public class TemplateFileGenerator {
    private final File resourcesDirectory;
    private final GeneratorLogger logger;
    private final File baseLayoutFile;
    private final String defaultSectionName;
    private TemplateProcessor templateProcessor;

    // Flag to control variable notation format
    private boolean useDotNotation = true;

    /**
     * Create a template generator without base layout.
     *
     * @param resourcesDirectory Output directory for template files
     * @param logger Logger instance
     */
    public TemplateFileGenerator(File resourcesDirectory, GeneratorLogger logger) {
        this.resourcesDirectory = resourcesDirectory;
        this.logger = logger;
        this.baseLayoutFile = null;
        this.defaultSectionName = "content";
    }

    /**
     * Create a template generator with base layout.
     *
     * @param resourcesDirectory Output directory for template files
     * @param baseLayoutFile Base layout template file
     * @param defaultSectionName Default section name for content without explicit sections
     * @param logger Logger instance
     */
    public TemplateFileGenerator(File resourcesDirectory, File baseLayoutFile,
                                 String defaultSectionName, GeneratorLogger logger) {
        this.resourcesDirectory = resourcesDirectory;
        this.baseLayoutFile = baseLayoutFile;
        this.defaultSectionName = defaultSectionName;
        this.logger = logger;
    }

    /**
     * Set whether to use dot notation for variables.
     * If true, section variables will be referenced as ${section.variable}
     * If false, they will be referenced as ${variable} within section context
     *
     * @param useDotNotation true to use dot notation, false otherwise
     */
    public void setUseDotNotation(boolean useDotNotation) {
        this.useDotNotation = useDotNotation;
    }

    /**
     * Generate template files for email definitions
     */
    public void generateTemplateFiles(List<EmailDefinition> definitions) throws IOException {
        if (definitions.isEmpty()) {
            logger.warn("No email definitions found. Skipping template generation.");
            return;
        }

        logger.info("Generating email templates in: " + resourcesDirectory.getAbsolutePath());

        // Initialize template processor if base layout is specified
        if (baseLayoutFile != null && baseLayoutFile.exists()) {
            try {
                templateProcessor = new TemplateProcessor(baseLayoutFile, logger);
                logger.info("Using base layout template: " + baseLayoutFile.getAbsolutePath());
                logger.info("Available sections: " + String.join(", ", templateProcessor.getSectionNames()));
            } catch (IOException e) {
                logger.error("Failed to read base layout template: " + e.getMessage());
                logger.info("Falling back to direct template generation without base layout");
                templateProcessor = null;
            }
        }

        for (EmailDefinition email : definitions) {
            generateTemplateFile(email);
        }
    }

    /**
     * Generate a single template file for an email definition
     */
    private void generateTemplateFile(EmailDefinition email) throws IOException {
        String templateFileName = email.getIdentifier() + ".html";
        File templateFile = new File(resourcesDirectory, templateFileName);

        logger.info("Generating template: " + templateFile.getName());

        try (FileWriter writer = new FileWriter(templateFile)) {
            String content;
            if (templateProcessor != null) {
                // Process using base layout and sections
                content = processWithBaseLayout(email);
            } else {
                // Fallback to direct template
                content = email.getTemplateText();
                if (content == null || content.isEmpty()) {
                    logger.warn("Email definition '" + email.getIdentifier() + "' has no template text");
                    content = "<div>No content defined for this email template.</div>";
                }

                // Only adjust variable paths based on section context
                content = processSectionVariables(content, null);
            }

            // Apply fix for double nested references
            content = fixDoubleNestedReferences(content);

            // Write the template file
            writer.write(content);
        } catch (IOException e) {
            logger.error("Failed to write template: " + templateFileName, e);
            throw e;
        }
    }

    /**
     * Fix double nested references in the template content
     * This method transforms ${section.section.variable} to ${section.variable}
     *
     * @param content The original template content
     * @return The fixed template content
     */
    private String fixDoubleNestedReferences(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // Replace patterns like ${section.section.variable} with ${section.variable}
        // First, identify common sections that might have this issue
        String[] sectionNames = {"orderSummary", "shipping", "tracking"};

        for (String section : sectionNames) {
            // Fix section.section.variable pattern
            String doubleNestPattern = "\\$\\{" + section + "\\." + section + "\\.([^}]+)\\}";
            String singleNestReplacement = "\\${" + section + ".$1}";
            content = content.replaceAll(doubleNestPattern, singleNestReplacement);

            // Fix specific item access pattern in loops
            // From: th:each="item : ${section.section.items}" with ${section.item.property}
            // To: th:each="item : ${section.items}" with ${item.property}
            String itemAccessPattern = "\\$\\{" + section + "\\.item\\.([^}]+)\\}";
            String itemAccessReplacement = "\\${item.$1}";
            content = content.replaceAll(itemAccessPattern, itemAccessReplacement);
        }

        return content;
    }


    /**
     * Process an email definition with the base layout
     */
    private String processWithBaseLayout(EmailDefinition email) {
        Map<String, String> sectionContents = new HashMap<>();

        // Process sections from section definitions
        List<SectionDefinition> sectionDefs = email.getSectionDefinitions();
        if (sectionDefs != null && !sectionDefs.isEmpty()) {
            for (SectionDefinition sectionDef : sectionDefs) {
                if (sectionDef.getContent() != null && !sectionDef.getContent().isEmpty()) {
                    String sectionContent = processSectionVariables(sectionDef.getContent(), sectionDef.getName());
                    sectionContents.put(sectionDef.getName(), sectionContent);
                }
            }
        }

        // Process sections from sections map
        Map<String, String> sections = email.getSections();
        if (sections != null && !sections.isEmpty()) {
            for (Map.Entry<String, String> entry : sections.entrySet()) {
                // Only add if not already added from section definitions
                if (!sectionContents.containsKey(entry.getKey())) {
                    String sectionContent = processSectionVariables(entry.getValue(), entry.getKey());
                    sectionContents.put(entry.getKey(), sectionContent);
                }
            }
        }

        // If there's a template text but no content section, add it to default section
        if ((sectionContents.isEmpty() || !sectionContents.containsKey(defaultSectionName))
                && email.getTemplateText() != null && !email.getTemplateText().isEmpty()) {
            String content = processSectionVariables(email.getTemplateText(), defaultSectionName);
            sectionContents.put(defaultSectionName, content);
        }

        return templateProcessor.processTemplate(sectionContents);
    }

    /**
     * Process section variables by adapting variable paths based on section context.
     * This is a simplified version that only adjusts variable paths without trying to convert syntax.
     */
    private String processSectionVariables(String content, String sectionName) {
        if (content == null || content.isEmpty() || !useDotNotation || sectionName == null) {
            return content;
        }

        // Find all ${variable} expressions and adjust paths based on section context
        Pattern varPattern = Pattern.compile("\\$\\{([a-zA-Z0-9_]+)((?:\\.[a-zA-Z0-9_]+)*)\\}");
        Matcher matcher = varPattern.matcher(content);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String rootVar = matcher.group(1);
            String remainingPath = matcher.group(2);

            // Skip if variable already has a prefix or is a special Thymeleaf variable
            if (rootVar.contains(".") || isThymeleafSpecialVariable(rootVar)) {
                continue;
            }

            // Content section variables are at top level
            if ("content".equals(sectionName)) {
                // Leave content variables as-is, they're already at top level
                continue;
            }

            // Add section prefix for other sections
            String replacement = "${" + sectionName + "." + rootVar + remainingPath + "}";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Check if a variable is a special Thymeleaf variable that should not be prefixed.
     */
    private boolean isThymeleafSpecialVariable(String varName) {
        // These are special Thymeleaf variables that should not be prefixed
        return varName.equals("subject") ||
                varName.equals("companyName") ||
                varName.startsWith("#") || // Thymeleaf utility objects like #dates
                varName.equals("this") ||
                varName.equals("root");
    }
}

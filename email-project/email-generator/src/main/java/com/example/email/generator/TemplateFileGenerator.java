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
 * Improved TemplateFileGenerator with proper handling of inline variables in content sections
 * and preservation of Thymeleaf attribute values.
 */
public class TemplateFileGenerator {
    private final File resourcesDirectory;
    private final GeneratorLogger logger;
    private final File baseLayoutFile;
    private final String defaultSectionName;
    private TemplateProcessor templateProcessor;

    // Flag to control variable notation format
    private boolean useDotNotation = true;

    // Marker used to temporarily replace ${} expressions during processing
    private static final String DOLLAR_MARKER = "###DOLLAR###";

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
                content = processTemplateContent(content, null);
            }

            // Clean Thymeleaf syntax
            content = cleanThymeleafSyntax(content);

            // Write the template file
            writer.write(content);
        } catch (IOException e) {
            logger.error("Failed to write template: " + templateFileName, e);
            throw e;
        }
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
                    String sectionContent = processTemplateContent(sectionDef.getContent(), sectionDef.getName());
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
                    String sectionContent = processTemplateContent(entry.getValue(), entry.getKey());
                    sectionContents.put(entry.getKey(), sectionContent);
                }
            }
        }

        // If there's a template text but no content section, add it to default section
        if ((sectionContents.isEmpty() || !sectionContents.containsKey(defaultSectionName))
                && email.getTemplateText() != null && !email.getTemplateText().isEmpty()) {
            String content = processTemplateContent(email.getTemplateText(), defaultSectionName);
            sectionContents.put(defaultSectionName, content);
        }

        return templateProcessor.processTemplate(sectionContents);
    }

    /**
     * Process template content with variable formatting
     */
    private String processTemplateContent(String content, String sectionName) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        // First, protect Thymeleaf attributes from being modified
        content = protectThymeleafAttributes(content);

        // Process variables based on section
        content = processVariablesWithMarker(content, sectionName);

        // Convert inline ${...} references to proper Thymeleaf syntax
        content = convertInlineVariablesToThymeleafSyntax(content);

        // Restore protected Thymeleaf attributes
        content = restoreProtectedAttributes(content);

        return content;
    }

    // Map to store protected attributes
    private final Map<String, String> protectedAttributes = new HashMap<>();
    private int protectionCounter = 0;
    private static final String PROTECT_MARKER = "###PROTECT###";

    /**
     * Protect Thymeleaf attributes from being modified by subsequent processing
     */
    private String protectThymeleafAttributes(String content) {
        protectedAttributes.clear();
        protectionCounter = 0;

        // Pattern to match any Thymeleaf attributes (th:*="...")
        Pattern thAttrPattern = Pattern.compile("(th:[^=]+=\"[^\"]*\\$\\{[^\"]*\\}[^\"]*\")");
        Matcher matcher = thAttrPattern.matcher(content);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String attrValue = matcher.group(1);
            String marker = PROTECT_MARKER + (protectionCounter++) + "#";
            protectedAttributes.put(marker, attrValue);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(marker));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Restore protected Thymeleaf attributes
     */
    private String restoreProtectedAttributes(String content) {
        String result = content;
        for (Map.Entry<String, String> entry : protectedAttributes.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Convert inline ${...} variable references to proper Thymeleaf syntax
     * This only processes references that are not inside Thymeleaf attributes
     */
    private String convertInlineVariablesToThymeleafSyntax(String content) {
        // No processing needed for empty content
        if (content == null || content.isEmpty()) {
            return content;
        }

        // Find text nodes with ${...} expressions
        Pattern inlineVarPattern = Pattern.compile("\\$\\{([a-zA-Z0-9_\\.]+)\\}");
        Matcher matcher = inlineVarPattern.matcher(content);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            // Check if this is protected (already in a Thymeleaf attribute)
            String matchedText = matcher.group(0);
            String varName = matcher.group(1);

            // Skip if it's in a marker (protected attribute)
            if (isInProtectedArea(content, matcher.start())) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matchedText));
                continue;
            }

            // Replace with Thymeleaf span
            String replacement = "<span th:text=\"${" + varName + "}\">Placeholder</span>";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Check if the position is inside a protected area
     */
    private boolean isInProtectedArea(String content, int position) {
        // Check if the position is within a protected marker
        for (String marker : protectedAttributes.keySet()) {
            int markerPos = content.indexOf(marker);
            if (markerPos >= 0 && position >= markerPos && position < markerPos + marker.length()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Process variables in template content using a marker approach to avoid regex issues
     */
    private String processVariablesWithMarker(String content, String sectionName) {
        // First, extract all ${...} expressions and replace with markers
        Pattern varPattern = Pattern.compile("\\$\\{([a-zA-Z0-9_\\.]+)\\}");
        Matcher matcher = varPattern.matcher(content);

        StringBuffer tempResult = new StringBuffer();
        int counter = 0;
        Map<String, String> markers = new HashMap<>();

        while (matcher.find()) {
            String varName = matcher.group(1);
            String finalVarName = varName;

            // Skip if in protected area
            if (isInProtectedArea(content, matcher.start())) {
                matcher.appendReplacement(tempResult, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }

            // Handle variables in content section
            if (useDotNotation && sectionName != null && !varName.contains(".")) {
                // Special handling for content section
                if ("content".equals(sectionName)) {
                    // For content section, don't add any section prefix - these are at top level
                    finalVarName = varName;
                } else {
                    // Normal case for other sections
                    finalVarName = sectionName + "." + varName;
                }
            }

            // Create a unique marker
            String marker = DOLLAR_MARKER + counter++ + "#";
            markers.put(marker, "${" + finalVarName + "}");

            // Replace with marker
            matcher.appendReplacement(tempResult, Matcher.quoteReplacement(marker));
        }
        matcher.appendTail(tempResult);

        // Now replace all markers with the actual expressions
        String result = tempResult.toString();
        for (Map.Entry<String, String> entry : markers.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * Clean Thymeleaf syntax to prevent common errors
     */
    private String cleanThymeleafSyntax(String content) {
        // Remove duplicate th: prefixes
        content = content.replace("th:th:", "th:");

        // Fix @{${...}} pattern which is invalid in Thymeleaf
        Pattern invalidPattern = Pattern.compile("th:href=\"@\\{\\$\\{([^}]+)\\}\\}\"");
        Matcher matcher = invalidPattern.matcher(content);

        StringBuffer tempResult = new StringBuffer();
        while (matcher.find()) {
            String varName = matcher.group(1);
            matcher.appendReplacement(tempResult, "th:href=\"${" + varName + "}\"");
        }
        matcher.appendTail(tempResult);

        // Fix variables that are already prefixed with content. in content section
        return fixContentPrefixedVariables(tempResult.toString());
    }

    /**
     * Fix variables that already have content. prefix in content section
     */
    private String fixContentPrefixedVariables(String content) {
        // Find all ${content.X} patterns in the content section
        Pattern contentVarPattern = Pattern.compile("\\$\\{content\\.([a-zA-Z0-9_]+)\\}");
        Matcher matcher = contentVarPattern.matcher(content);

        StringBuffer tempResult = new StringBuffer();

        // Determine if we're using a model structure where content vars are at top level
        boolean contentVarsAtTopLevel = true; // Set this based on your model structure

        if (contentVarsAtTopLevel) {
            while (matcher.find()) {
                String varName = matcher.group(1);
                matcher.appendReplacement(tempResult, "${" + varName + "}"); // Remove content. prefix
            }
            matcher.appendTail(tempResult);
            return tempResult.toString();
        }

        return content; // No changes needed if contentVarsAtTopLevel is false
    }
}

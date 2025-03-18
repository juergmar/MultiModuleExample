package com.example.email.generator;

import com.example.email.core.generator.EmailSection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to process templates with section replacements.
 */
public class TemplateProcessor {
    private static final Pattern SECTION_PATTERN = Pattern.compile(
            "<!-- SECTION: (\\w+) -->(.*?)<!-- END SECTION: \\1 -->",
            Pattern.DOTALL);

    private final String baseLayoutContent;
    private final Map<String, String> sections = new HashMap<>();
    private final GeneratorLogger logger;

    public TemplateProcessor(File baseLayoutFile, GeneratorLogger logger) throws IOException {
        this.logger = logger;
        this.baseLayoutContent = Files.readString(baseLayoutFile.toPath());
        extractSections();
    }

    public TemplateProcessor(String baseLayoutContent, GeneratorLogger logger) {
        this.baseLayoutContent = baseLayoutContent;
        this.logger = logger;
        extractSections();
    }

    private void extractSections() {
        Matcher matcher = SECTION_PATTERN.matcher(baseLayoutContent);
        while (matcher.find()) {
            String sectionName = matcher.group(1);
            String sectionContent = matcher.group(2);
            sections.put(sectionName, sectionContent);
            logger.debug("Extracted section: " + sectionName);
        }

        if (sections.isEmpty()) {
            logger.warn("No sections found in the base layout template");
        } else {
            logger.info("Found " + sections.size() + " sections in the base layout template: " +
                    String.join(", ", sections.keySet()));
        }
    }

    public List<String> getSectionNames() {
        return new ArrayList<>(sections.keySet());
    }

    public String getSectionContent(String sectionName) {
        return sections.get(sectionName);
    }

    public String processTemplate(Map<String, String> sectionReplacements) {
        // Start with the full base layout content
        String result = baseLayoutContent;

        // Process each section that needs to be replaced
        for (Map.Entry<String, String> entry : sectionReplacements.entrySet()) {
            String sectionName = entry.getKey();
            String replacement = entry.getValue();

            if (!sections.containsKey(sectionName)) {
                logger.warn("Section not found in base layout: " + sectionName);
                continue;
            }

            // Create the search pattern that matches the entire section including markers
            String sectionPattern = "<!-- SECTION: " + sectionName + " -->[\\s\\S]*?<!-- END SECTION: " + sectionName + " -->";

            // Create the replacement with the section markers preserved
            String sectionReplacement = "<!-- SECTION: " + sectionName + " -->" + replacement + "<!-- END SECTION: " + sectionName + " -->";

            // Use replaceAll with regex
            result = result.replaceAll(sectionPattern, Matcher.quoteReplacement(sectionReplacement));
        }

        return result;
    }

    public String processTemplate(List<EmailSection> sections) {
        Map<String, String> replacements = new HashMap<>();
        for (EmailSection section : sections) {
            replacements.put(section.getName(), section.getContent());
        }
        return processTemplate(replacements);
    }

    public static List<EmailSection> parseSections(String templateContent) {
        List<EmailSection> result = new ArrayList<>();

        if (templateContent == null || templateContent.isEmpty()) {
            return result;
        }

        Matcher matcher = SECTION_PATTERN.matcher(templateContent);

        while (matcher.find()) {
            String sectionName = matcher.group(1);
            String sectionContent = matcher.group(2);
            result.add(new EmailSection(sectionName, sectionContent));
        }

        return result;
    }
}

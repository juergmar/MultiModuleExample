package com.example.email.core.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a single email definition from the JSON schema.
 */
public class EmailDefinition {
    private String identifier;
    private String subject;
    private String templateText;
    private List<VariableDefinition> variables = new ArrayList<>();
    private Map<String, String> sections = new HashMap<>();
    private List<SectionDefinition> sectionDefinitions = new ArrayList<>();

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTemplateText() {
        return templateText;
    }

    public void setTemplateText(String templateText) {
        this.templateText = templateText;
    }

    public List<VariableDefinition> getVariables() {
        return variables;
    }

    public void setVariables(List<VariableDefinition> variables) {
        this.variables = variables != null ? variables : new ArrayList<>();
    }

    /**
     * Get the sections for this email template.
     * Sections allow for defining content that will replace sections in the base layout.
     *
     * @return Map of section name to section content
     */
    public Map<String, String> getSections() {
        return sections;
    }

    /**
     * Set the sections for this email template.
     *
     * @param sections Map of section name to section content
     */
    public void setSections(Map<String, String> sections) {
        this.sections = sections != null ? sections : new HashMap<>();
    }

    /**
     * Get the section definitions for this email template.
     * Each section definition can have its own variables and content.
     *
     * @return List of section definitions
     */
    public List<SectionDefinition> getSectionDefinitions() {
        return sectionDefinitions;
    }

    /**
     * Set the section definitions for this email template.
     *
     * @param sectionDefinitions List of section definitions
     */
    public void setSectionDefinitions(List<SectionDefinition> sectionDefinitions) {
        this.sectionDefinitions = sectionDefinitions != null ? sectionDefinitions : new ArrayList<>();
    }

    /**
     * Get a section definition by name.
     *
     * @param name The section name
     * @return Optional containing the section definition if found
     */
    public Optional<SectionDefinition> getSectionDefinition(String name) {
        return sectionDefinitions.stream()
                .filter(section -> section.getName().equals(name))
                .findFirst();
    }

    /**
     * Add a section definition.
     *
     * @param sectionDefinition The section definition to add
     */
    public void addSectionDefinition(SectionDefinition sectionDefinition) {
        if (sectionDefinition != null) {
            this.sectionDefinitions.add(sectionDefinition);

            // Also add the content to the sections map for backward compatibility
            if (sectionDefinition.getContent() != null) {
                this.sections.put(sectionDefinition.getName(), sectionDefinition.getContent());
            }
        }
    }

    /**
     * Get the method name for this email template.
     *
     * @return The method name
     */
    public String getMethodName() {
        return "create" + capitalize(identifier) + "Email";
    }

    /**
     * Get the full render method name for this email template.
     *
     * @return The render method name
     */
    public String getRenderMethodName() {
        return "render" + capitalize(identifier) + "Email";
    }

    /**
     * Get the template path for this email.
     *
     * @return The template path
     */
    public String getTemplatePath() {
        return "email/" + identifier;
    }

    /**
     * Get the sections as a list of EmailSection objects.
     *
     * @return List of email sections
     */
    public List<EmailSection> getEmailSections() {
        // First, get sections from the sections map
        List<EmailSection> result = new ArrayList<>();

        if (sections != null) {
            for (Map.Entry<String, String> entry : sections.entrySet()) {
                result.add(new EmailSection(entry.getKey(), entry.getValue()));
            }
        }

        // Then, add sections from section definitions if they have content
        if (sectionDefinitions != null) {
            for (SectionDefinition sectionDef : sectionDefinitions) {
                if (sectionDef.getContent() != null && !sectionDef.getContent().isEmpty()) {
                    // Only add if not already added from sections map
                    if (result.stream().noneMatch(s -> s.getName().equals(sectionDef.getName()))) {
                        result.add(new EmailSection(sectionDef.getName(), sectionDef.getContent()));
                    }
                }
            }
        }

        // If no content section is defined but templateText exists, create a content section
        if (result.stream().noneMatch(s -> s.getName().equals("content")) &&
                templateText != null && !templateText.isEmpty()) {
            result.add(new EmailSection("content", templateText));
        }

        return result;
    }

    /**
     * Get all variable definitions, including those from sections.
     *
     * @return List of all variable definitions
     */
    public List<VariableDefinition> getAllVariableDefinitions() {
        List<VariableDefinition> allVariables = new ArrayList<>(variables);

        // Add variables from section definitions
        if (sectionDefinitions != null) {
            for (SectionDefinition sectionDef : sectionDefinitions) {
                if (sectionDef.getVariables() != null) {
                    // Add section prefix to variable names to avoid conflicts
                    for (VariableDefinition var : sectionDef.getVariables()) {
                        allVariables.add(var);
                    }
                }
            }
        }

        return allVariables;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}

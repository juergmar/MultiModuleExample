package com.example.email.core.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a section definition in an email template.
 * Each section can have its own variables and content template.
 */
public class SectionDefinition {
    private String name;
    private String content;
    private List<VariableDefinition> variables = new ArrayList<>();
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<VariableDefinition> getVariables() {
        return variables;
    }

    public void setVariables(List<VariableDefinition> variables) {
        this.variables = variables != null ? variables : new ArrayList<>();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the parameter class name for this section.
     *
     * @param emailIdentifier The email identifier
     * @return The parameter class name
     */
    public String getParameterClassName(String emailIdentifier) {
        return capitalizeFirst(emailIdentifier) + capitalizeFirst(name) + "Params";
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}

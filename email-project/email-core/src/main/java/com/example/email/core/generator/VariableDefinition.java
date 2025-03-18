package com.example.email.core.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a variable definition in an email template from the JSON schema.
 */
public class VariableDefinition {
    private static final Pattern GENERIC_PATTERN = Pattern.compile("([^<]+)<(.+)>");

    private String name;
    private String type;
    private String description;
    private boolean required = true;
    private boolean array = false;
    private String complexType;

    // Standard getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        // If the type ends with [], it's an array
        if (type != null && type.endsWith("[]")) {
            this.array = true;
            this.type = type.substring(0, type.length() - 2);
        } else {
            this.type = type;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isArray() {
        return array;
    }

    public void setArray(boolean array) {
        this.array = array;
    }

    public String getComplexType() {
        return complexType;
    }

    public void setComplexType(String complexType) {
        this.complexType = complexType;
    }

    /**
     * Get the TypeName for JavaPoet code generation.
     * This provides a clean abstraction for the generator to use without duplicating type mapping logic.
     *
     * @return The TypeName for this variable
     */
    public TypeName getTypeName() {
        if (isArray()) {
            return ParameterizedTypeName.get(
                    ClassName.get(List.class),
                    getBaseTypeName()
            );
        }
        return getBaseTypeName();
    }

    /**
     * Get the base TypeName (without List wrapper for arrays).
     *
     * @return The base TypeName
     */
    private TypeName getBaseTypeName() {
        if ("complex".equalsIgnoreCase(type) && complexType != null) {
            // Handle generic types like List<OrderItem>
            Matcher matcher = GENERIC_PATTERN.matcher(complexType);
            if (matcher.matches()) {
                String rawType = matcher.group(1);
                String typeParameter = matcher.group(2);

                if (rawType.endsWith("List") || rawType.endsWith("java.util.List")) {
                    return ParameterizedTypeName.get(
                            ClassName.get(List.class),
                            getClassName(typeParameter)
                    );
                } else if (rawType.endsWith("Map") || rawType.endsWith("java.util.Map")) {
                    String[] typeParams = typeParameter.split(",");
                    if (typeParams.length == 2) {
                        return ParameterizedTypeName.get(
                                ClassName.get(Map.class),
                                getClassName(typeParams[0].trim()),
                                getClassName(typeParams[1].trim())
                        );
                    }
                } else {
                    // Other generic types
                    try {
                        ClassName rawClassName = getClassName(rawType);
                        return ParameterizedTypeName.get(
                                rawClassName,
                                getClassName(typeParameter)
                        );
                    } catch (Exception e) {
                        // Fallback to simple class
                        return ClassName.bestGuess(complexType);
                    }
                }
            }

            // Not a generic type
            return ClassName.bestGuess(complexType);
        }

        switch (type) {
            case "String":
                return ClassName.get(String.class);
            case "Integer":
                return ClassName.get(Integer.class);
            case "Boolean":
                return ClassName.get(Boolean.class);
            case "Double":
                return ClassName.get(Double.class);
            case "Long":
                return ClassName.get(Long.class);
            case "BigDecimal":
                return ClassName.get("java.math", "BigDecimal");
            case "LocalDate":
                return ClassName.get("java.time", "LocalDate");
            case "LocalDateTime":
                return ClassName.get("java.time", "LocalDateTime");
            case "URL":
                return ClassName.get("java.net", "URL");
            case "Map":
                return ParameterizedTypeName.get(
                        ClassName.get(Map.class),
                        ClassName.get(String.class),
                        ClassName.get(Object.class)
                );
            default:
                // For unrecognized types, assume it's a fully qualified class name
                return ClassName.bestGuess(type);
        }
    }

    /**
     * Helper method to get a ClassName from a string.
     * Handles both simple and fully qualified class names.
     *
     * @param className The class name as a string
     * @return The ClassName object
     */
    private ClassName getClassName(String className) {
        className = className.trim();

        // Check for primitives and their wrappers
        switch (className) {
            case "String":
                return ClassName.get(String.class);
            case "Integer":
            case "int":
                return ClassName.get(Integer.class);
            case "Boolean":
            case "boolean":
                return ClassName.get(Boolean.class);
            case "Double":
            case "double":
                return ClassName.get(Double.class);
            case "Long":
            case "long":
                return ClassName.get(Long.class);
            case "Float":
            case "float":
                return ClassName.get(Float.class);
            case "BigDecimal":
                return ClassName.get("java.math", "BigDecimal");
            case "Object":
                return ClassName.get(Object.class);
        }

        // Handle fully qualified names
        if (className.contains(".")) {
            String packageName = className.substring(0, className.lastIndexOf('.'));
            String simpleName = className.substring(className.lastIndexOf('.') + 1);
            return ClassName.get(packageName, simpleName);
        }

        // Fallback to best guess
        return ClassName.bestGuess(className);
    }

    /**
     * Get the set of imports needed for this variable type.
     *
     * @return Set of imports as fully qualified class names
     */
    public Set<String> getRequiredImports() {
        Set<String> imports = new HashSet<>();

        // Add List import for arrays
        if (isArray()) {
            imports.add("java.util.List");
        }

        // For complex types
        if ("complex".equalsIgnoreCase(type) && complexType != null) {
            // Handle generic types
            Matcher matcher = GENERIC_PATTERN.matcher(complexType);
            if (matcher.matches()) {
                String rawType = matcher.group(1);
                String typeParameter = matcher.group(2);

                // Add import for raw type
                if (rawType.contains(".")) {
                    imports.add(rawType);
                } else if (rawType.equals("List")) {
                    imports.add("java.util.List");
                } else if (rawType.equals("Map")) {
                    imports.add("java.util.Map");
                }

                // Add imports for type parameters
                for (String param : typeParameter.split(",")) {
                    param = param.trim();
                    if (param.contains(".")) {
                        imports.add(param);
                    }
                }
            } else if (complexType.contains(".")) {
                imports.add(complexType);
            }

            return imports;
        }

        // For built-in types that need imports
        switch (type) {
            case "BigDecimal":
                imports.add("java.math.BigDecimal");
                break;
            case "LocalDate":
                imports.add("java.time.LocalDate");
                break;
            case "LocalDateTime":
                imports.add("java.time.LocalDateTime");
                break;
            case "URL":
                imports.add("java.net.URL");
                break;
            case "Map":
                imports.add("java.util.Map");
                break;
            default:
                // For custom types that may contain a package
                if (type.contains(".")) {
                    imports.add(type);
                }
                break;
        }

        return imports;
    }
}

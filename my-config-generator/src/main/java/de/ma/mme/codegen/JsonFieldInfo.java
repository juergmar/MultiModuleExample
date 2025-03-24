package de.ma.mme.codegen;

public record JsonFieldInfo(
        String name,
        String type,
        String defaultValue,
        boolean isNestedObject,
        String collectionType
) {
    public String getGetterName() {
        return "get" + capitalize(name);
    }

    public boolean isCollection() {
        return collectionType != null;
    }

    private String capitalize(String str) {
        return JsonProcessor.capitalize(str);
    }
}

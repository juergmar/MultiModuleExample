package de.ma.mme.codegen;


import java.util.List;

public record JsonObjectInfo(
        String className,
        String packageName,
        List<JsonFieldInfo> fields,
        List<JsonObjectInfo> nestedObjects
) {
    public String getFullyQualifiedName() {
        return packageName != null ? packageName + "." + className : className;
    }

    public boolean hasNestedObjects() {
        return nestedObjects != null && !nestedObjects.isEmpty();
    }
}

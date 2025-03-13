package de.ma.mme.annotation;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public record TranslationConfig(
        String jsonPath,
        String packageName,
        String enumName,
        boolean useLombok
) {
    public String fullyQualifiedEnumName() {
        return packageName + "." + enumName;
    }

    public static TranslationConfig fromAnnotation(TypeElement element, Elements elementUtils) {
        GenerateTranslations annotation = element.getAnnotation(GenerateTranslations.class);

        String packageName = annotation.packageName();
        if (packageName.isEmpty()) {
            PackageElement packageElement = elementUtils.getPackageOf(element);
            packageName = packageElement.getQualifiedName().toString();
        }

        return new TranslationConfig(
                annotation.value(),
                packageName,
                annotation.enumName(),
                annotation.useLombok()
        );
    }
}

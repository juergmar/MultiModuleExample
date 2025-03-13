package de.ma.mme.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that triggers generation of a Translations enum from a JSON file.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateTranslations {
    /**
     * Path to the JSON file containing translations, relative to the resources directory.
     */
    String value() default "translations.json";

    /**
     * Package name for the generated enum.
     * If empty, the package of the annotated class will be used.
     */
    String packageName() default "";

    /**
     * Name of the generated enum class.
     */
    String enumName() default "Translations";
}

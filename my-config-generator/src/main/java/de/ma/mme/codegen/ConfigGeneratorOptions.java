package de.ma.mme.codegen;

import java.io.File;
import java.util.Optional;

public record ConfigGeneratorOptions(
        File jsonFile,
        String packageName,
        String className,
        File outputDirectory
) {
    public ConfigGeneratorOptions {
        if (jsonFile == null) {
            throw new IllegalArgumentException("jsonFile must not be null");
        }
        if (packageName == null || packageName.isBlank()) {
            throw new IllegalArgumentException("packageName must not be null or blank");
        }
        if (className == null || className.isBlank()) {
            className = "AppConfig";
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private File jsonFile;
        private String packageName;
        private String className = "AppConfig";
        private File outputDirectory;

        public Builder jsonFile(File jsonFile) {
            this.jsonFile = jsonFile;
            return this;
        }

        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder className(String className) {
            this.className = Optional.ofNullable(className)
                    .filter(s -> !s.isBlank())
                    .orElse("AppConfig");
            return this;
        }

        public Builder outputDirectory(File outputDirectory) {
            this.outputDirectory = outputDirectory;
            return this;
        }

        public ConfigGeneratorOptions build() {
            return new ConfigGeneratorOptions(jsonFile, packageName, className, outputDirectory);
        }
    }
}

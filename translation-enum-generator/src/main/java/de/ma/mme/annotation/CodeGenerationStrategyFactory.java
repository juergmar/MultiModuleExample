package de.ma.mme.annotation;

public class CodeGenerationStrategyFactory {

    public static CodeGenerationStrategy createStrategy(boolean useLombok) {
        return useLombok
                ? new LombokCodeGenerationStrategy()
                : new StandardCodeGenerationStrategy();
    }
}

package de.ma.mme.annotation;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes("de.ma.mme.annotation.GenerateTranslations")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class TranslationProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private TranslationReader translationReader;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.translationReader = new TranslationReader(filer, messager);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        roundEnv.getElementsAnnotatedWith(GenerateTranslations.class).stream()
                .filter(this::isClassElement)
                .forEach(this::processAnnotatedElement);

        return true;
    }

    private boolean isClassElement(Element element) {
        if (element.getKind() != ElementKind.CLASS) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "@GenerateTranslations can only be applied to classes", element);
            return false;
        }
        return true;
    }

    private void processAnnotatedElement(Element element) {
        TypeElement typeElement = (TypeElement) element;
        TranslationConfig config = TranslationConfig.fromAnnotation(
                typeElement, processingEnv.getElementUtils());

        try {
            Map<String, String> translations = translationReader.loadTranslations(
                    config.jsonPath(), element);

            if (!translations.isEmpty()) {
                EnumCodeGenerator codeGenerator = new EnumCodeGenerator(filer, messager, config);
                codeGenerator.generateEnum(translations);
            }
        } catch (TranslationReader.ResourceNotFoundException e) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Translation file not found: " + e.getResourcePath(), element);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Failed to process translations: " + e.getMessage(), element);
        }
    }
}

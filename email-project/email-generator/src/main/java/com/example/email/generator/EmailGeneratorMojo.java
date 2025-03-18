package com.example.email.generator;

import com.example.email.core.generator.EmailDefinition;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

/**
 * Maven plugin to generate email template files and service classes from email definitions.
 * This plugin expects email definitions to use proper Thymeleaf syntax.
 */
@Mojo(name = "generate-emails", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class EmailGeneratorMojo extends AbstractMojo {

    /**
     * JSON file containing email definitions.
     * Email templates in the definitions should use standard Thymeleaf syntax.
     */
    @Parameter(required = true)
    private File definitionsFile;

    /**
     * Package name for generated classes.
     */
    @Parameter(required = true)
    private String packageName;

    /**
     * Name of the generated service class.
     */
    @Parameter(defaultValue = "TypedEmailService")
    private String serviceClassName;

    /**
     * The Maven project reference.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Whether to include render methods in the generated service class.
     */
    @Parameter(defaultValue = "false")
    private boolean includeRenderMethod;

    /**
     * Annotation to use for nullable parameters.
     */
    @Parameter(defaultValue = "jakarta.annotation.Nullable")
    private String nullableAnnotation;

    /**
     * Whether to use Lombok for generated classes.
     */
    @Parameter(defaultValue = "false")
    private boolean useLombok;

    /**
     * Path to the base layout template file.
     * This template will be used as a blueprint for all generated email templates.
     * If not specified, the original templateText from the email definition will be used.
     */
    @Parameter
    private File baseLayoutFile;

    /**
     * Whether to use base layout sections.
     * If true, the generator will extract sections from the base layout and allow replacing them.
     * If false, the original templateText will be used directly.
     */
    @Parameter(defaultValue = "true")
    private boolean useBaseLayoutSections;

    /**
     * Default section name to use when templateText doesn't define sections.
     * When using base layout sections, this is the section name where templateText content
     * will be placed if no explicit sections are defined.
     */
    @Parameter(defaultValue = "content")
    private String defaultSectionName;

    @Override
    public void execute() throws MojoExecutionException {
        GeneratorLogger logger = new MavenGeneratorLogger(getLog());

        try {
            // Log information about how to write email templates
            logTemplateGuidelines(logger);

            // Prepare output directories
            File outputDirectory = new File(project.getBuild().getDirectory(), "generated-sources/email");
            File resourcesDirectory = new File(project.getBuild().getDirectory(), "generated-resources/templates/email");

            outputDirectory.mkdirs();
            resourcesDirectory.mkdirs();

            // Add generated sources to the project
            project.addCompileSourceRoot(outputDirectory.getPath());

            // Initialize components
            EmailDefinitionReader definitionReader = new EmailDefinitionReader(definitionsFile, logger);

            // Create template generator with baseLayoutFile if specified
            TemplateFileGenerator templateGenerator;
            if (baseLayoutFile != null && baseLayoutFile.exists() && useBaseLayoutSections) {
                templateGenerator = new TemplateFileGenerator(
                        resourcesDirectory,
                        baseLayoutFile,
                        defaultSectionName,
                        logger);

                // Set to use dot notation (no constructor change needed)
                templateGenerator.setUseDotNotation(true);
            } else {
                templateGenerator = new TemplateFileGenerator(resourcesDirectory, logger);
                templateGenerator.setUseDotNotation(true);
            }

            ParameterClassGenerator parameterClassGenerator = new ParameterClassGenerator(
                    outputDirectory, packageName, nullableAnnotation, useLombok, logger);
            EmailServiceGenerator serviceGenerator = new EmailServiceGenerator(
                    outputDirectory, packageName, serviceClassName,
                    includeRenderMethod, logger, parameterClassGenerator);

            // Read definitions
            List<EmailDefinition> definitions = definitionReader.readDefinitions();

            // Generate template files
            templateGenerator.generateTemplateFiles(definitions);

            // Generate service class with parameter classes
            serviceGenerator.generateEmailService(definitions);

        } catch (Exception e) {
            logger.error("Error generating email templates and service", e);
            throw new MojoExecutionException("Error generating email templates and service: " + e.getMessage(), e);
        }
    }

    /**
     * Log guidelines for writing email templates with proper Thymeleaf syntax.
     */
    private void logTemplateGuidelines(GeneratorLogger logger) {
        logger.info("==================== EMAIL TEMPLATE GUIDELINES ====================");
        logger.info("Email templates should use standard Thymeleaf syntax for variables:");
        logger.info("");
        logger.info("1. Use <span th:text=\"${variableName}\">Default text</span> for inline variables");
        logger.info("   Example: Hello, <span th:text=\"${userName}\">User</span>!");
        logger.info("");
        logger.info("2. Use th:href for links with variables");
        logger.info("   Example: <a th:href=\"${resetUrl}\">Reset Password</a>");
        logger.info("");
        logger.info("3. For iterating over collections:");
        logger.info("   Example: <tr th:each=\"item : ${orderSummary.items}\">...</tr>");
        logger.info("");
        logger.info("4. For conditional rendering:");
        logger.info("   Example: <div th:if=\"${showSection}\">...</div>");
        logger.info("");
        logger.info("5. Main section variables:");
        logger.info("   - Content section variables are accessed directly: ${variableName}");
        logger.info("   - Other section variables are accessed with section prefix: ${sectionName.variableName}");
        logger.info("");
        logger.info("6. Special variables available in all templates:");
        logger.info("   - ${subject} - Email subject");
        logger.info("   - ${companyName} - Company name");
        logger.info("   - ${#dates.format(#dates.createNow(), 'yyyy')} - Current year");
        logger.info("==================================================================");
    }
}

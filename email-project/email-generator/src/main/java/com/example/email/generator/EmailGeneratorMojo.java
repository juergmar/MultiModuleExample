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

@Mojo(name = "generate-emails", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class EmailGeneratorMojo extends AbstractMojo {

    @Parameter(required = true)
    private File definitionsFile;

    @Parameter(required = true)
    private String packageName;

    @Parameter(defaultValue = "TypedEmailService")
    private String serviceClassName;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "false")
    private boolean includeRenderMethod;

    @Parameter(defaultValue = "jakarta.annotation.Nullable")
    private String nullableAnnotation;

    @Parameter(defaultValue = "false")
    private boolean useLombok;

    @Override
    public void execute() throws MojoExecutionException {
        GeneratorLogger logger = new MavenGeneratorLogger(getLog());

        try {
            // Prepare output directories
            File outputDirectory = new File(project.getBuild().getDirectory(), "generated-sources/email");
            File resourcesDirectory = new File(project.getBuild().getDirectory(), "generated-resources/templates/email");

            outputDirectory.mkdirs();
            resourcesDirectory.mkdirs();

            // Add generated sources to the project
            project.addCompileSourceRoot(outputDirectory.getPath());

            // Initialize components
            EmailDefinitionReader definitionReader = new EmailDefinitionReader(definitionsFile, logger);
            TemplateFileGenerator templateGenerator = new TemplateFileGenerator(resourcesDirectory, logger);
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
}

package de.ma.mme.codegen;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Mojo(
        name = "generate-config",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true
)
public class ConfigGeneratorMojo extends AbstractMojo {

    @Parameter(required = true)
    private File jsonFile;

    @Parameter(required = true)
    private String packageName;

    @Parameter(defaultValue = "AppConfig")
    private String className;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            File outputDirectory = prepareOutputDirectory();

            ConfigGeneratorOptions options = createOptions(outputDirectory);
            JsonProcessor jsonProcessor = new JsonProcessor();
            ConfigCodeGenerator codeGenerator = new ConfigCodeGenerator(jsonProcessor);

            codeGenerator.generateCode(options);

            getLog().info("Generated configuration class: " +
                    Path.of(outputDirectory.getPath(),
                            packageName.replace('.', File.separatorChar),
                            className + ".java"));

        } catch (IOException e) {
            throw new MojoExecutionException("Error generating configuration class", e);
        }
    }

    private File prepareOutputDirectory() {
        File outputDirectory = new File(project.getBuild().getDirectory(), "generated-sources/config");
        outputDirectory.mkdirs();
        project.addCompileSourceRoot(outputDirectory.getPath());
        return outputDirectory;
    }

    private ConfigGeneratorOptions createOptions(File outputDirectory) {
        return ConfigGeneratorOptions.builder()
                .jsonFile(jsonFile)
                .packageName(packageName)
                .className(className)
                .outputDirectory(outputDirectory)
                .build();
    }
}

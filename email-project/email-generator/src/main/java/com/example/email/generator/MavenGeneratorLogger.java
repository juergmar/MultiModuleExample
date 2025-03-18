package com.example.email.generator;

import org.apache.maven.plugin.logging.Log;

public class MavenGeneratorLogger implements GeneratorLogger {
    private final Log mavenLog;

    public MavenGeneratorLogger(Log mavenLog) {
        this.mavenLog = mavenLog;
    }

    @Override
    public void info(String message) {
        mavenLog.info(message);
    }

    @Override
    public void warn(String message) {
        mavenLog.warn(message);
    }

    @Override
    public void error(String message) {
        mavenLog.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        mavenLog.error(message, throwable);
    }

    @Override
    public void debug(String message) {
        mavenLog.debug(message);
    }
}

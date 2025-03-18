package com.example.email.spring;

import com.example.email.core.template.TemplateEngine;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

public class ThymeleafTemplateEngine implements TemplateEngine, ApplicationContextAware {

    private final ITemplateEngine thymeleafEngine;

    public ThymeleafTemplateEngine(ITemplateEngine thymeleafEngine) {
        this.thymeleafEngine = thymeleafEngine;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
    }

    @Override
    public String process(String templateName, Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        context.setLocale(LocaleContextHolder.getLocale());
        return thymeleafEngine.process(templateName, context);
    }
}

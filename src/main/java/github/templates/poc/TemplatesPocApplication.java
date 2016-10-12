package github.templates.poc;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.ToolManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TemplatesPocApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemplatesPocApplication.class, args);
    }

    // Velocity template POC dependencies

    @Bean(initMethod = "init")
    public VelocityEngine velocityEngine() {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
        velocityEngine.setProperty("runtime.log.logsystem.log4j.logger", "github.templates.poc.velocity");
        velocityEngine.setProperty(Velocity.RESOURCE_LOADER, "string");
        velocityEngine.setProperty("string.resource.loader.description", "Velocity StringResource loader");
        velocityEngine.setProperty("string.resource.loader.class", "org.apache.velocity.runtime.resource.loader.StringResourceLoader");
        velocityEngine.setProperty("string.resource.loader.repository.class", "org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl");
        return velocityEngine;
    }

    @Bean
    public ToolManager toolManager() {
        return new ToolManager(true);
    }

    // Freemarker template POC dependencies

    @Bean
    public StringTemplateLoader templateLoader() {
        return new StringTemplateLoader();
    }

    @Bean
    public Configuration freeMarkerConfiguration(StringTemplateLoader templateLoader) {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_25);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateUpdateDelayMilliseconds(0L);
        configuration.setTemplateLoader(templateLoader);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
        return configuration;
    }

}

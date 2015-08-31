package github.templates.poc.controllers;

import github.templates.poc.model.TemplateTO;
import github.templates.poc.velocity.TemplateTool;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResource;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/velocity")
public class VelocityController {

    private static final String TEMPLATE_NAME = "template";

    private final VelocityEngine velocityEngine;
    private final TemplateTool templateTool;

    @Resource
    private Map<String, Object> commonTemplateContext;

    @Autowired
    public VelocityController(VelocityEngine velocityEngine, TemplateTool templateTool) {
        this.velocityEngine = velocityEngine;
        this.templateTool = templateTool;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public TemplateTO getTemplate() {
        StringResourceRepository templatesRepository = StringResourceLoader.getRepository();
        StringResource templateResource = templatesRepository.getStringResource(TEMPLATE_NAME);
        Template template = velocityEngine.getTemplate(TEMPLATE_NAME);
        Set<String> parameters = templateTool.referenceSet(template);
        return new TemplateTO(templateResource.getBody(), parameters, -1L);
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TemplateTO updateTemplate(@RequestBody Map<String, String> requestBody) {
        String templateText = requestBody.remove("__template__");
        if (templateText != null) {
            setTemplate(templateText);
        }
        Template template = velocityEngine.getTemplate(TEMPLATE_NAME);
        Set<String> parameters = templateTool.referenceSet(template);
        VelocityContext context = new VelocityContext(requestBody);
        for (Map.Entry<String, Object> commonContextItem : commonTemplateContext.entrySet()) {
            context.put(commonContextItem.getKey(), commonContextItem.getValue());
        }
        long conversionTime = System.nanoTime();
        StringWriter writer = new StringWriter();
        velocityEngine.mergeTemplate(TEMPLATE_NAME, "UTF-8", context, writer);
        return new TemplateTO(writer.toString(), parameters, System.nanoTime() - conversionTime);
    }

    private void setTemplate(String template) {
        StringResourceRepository templatesRepository = StringResourceLoader.getRepository();
        templatesRepository.putStringResource(TEMPLATE_NAME, template, "UTF-8");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public TemplateTO handleAbsentTemplate() {
        return new TemplateTO("", Collections.<String>emptySet(), -1L);
    }

    @ExceptionHandler(ParseErrorException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleTemplateParseFailure() { }

}
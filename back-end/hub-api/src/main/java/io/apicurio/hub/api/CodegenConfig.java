package io.apicurio.hub.api;


import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apicurio.hub.api.beans.CodegenSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author usman.shb013@gmail.com
 */
@ApplicationScoped
public class CodegenConfig {

    private static Logger logger = LoggerFactory.getLogger(CodegenConfig.class);

    private CodegenSettings settings;

    @PostConstruct
    public void load() {
        this.settings = new CodegenSettings();
        try (InputStream input = CodegenConfig.class.getResourceAsStream("codegen-config.json")) {
            if (input == null) {
                this.settings.setGeneratorName("spring");
                this.settings.setLibrary("spring-boot");
                this.settings.setOutputDir("code");
                this.settings.setArtifactId("my-api");
                this.settings.setApiPackage("com.fastcode.demo.api");
                this.settings.setModelPackage("com.fastcode.demo.model");
                this.settings.setInvokerPackage("com.fastcode.demo");
            } else {
                ObjectMapper objectMapper = new ObjectMapper();
                this.settings = objectMapper.readValue(input, CodegenSettings.class);
            }
        } catch (IOException e) {
            logger.error("Error loading config information.", e);
        }
    }

    /**
     * Gets a config settings object.
     */
    public CodegenSettings getSettings() {
        return this.settings;
    }

}

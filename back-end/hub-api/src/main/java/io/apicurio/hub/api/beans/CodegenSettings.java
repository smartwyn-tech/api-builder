package io.apicurio.hub.api.beans;


import java.util.HashMap;
import java.util.Map;

public class CodegenSettings {
    private String generatorName;
    private String library;
    private String artifactId;
    private String apiPackage;
    private String modelPackage;
    private String invokerPackage;
    private String outputDir;
    private Map<String, String> globalProperties = new HashMap();
    private Map<String, Object> additionalProperties = new HashMap();

    public CodegenSettings() {
    }

    public String getGeneratorName() {
        return generatorName;
    }

    public void setGeneratorName(String generatorName) {
        this.generatorName = generatorName;
    }

    public Map<String, String> getGlobalProperties() {
        return globalProperties;
    }

    public void setGlobalProperties(Map<String, String> globalProperties) {
        this.globalProperties = globalProperties;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getApiPackage() {
        return apiPackage;
    }

    public void setApiPackage(String apiPackage) {
        this.apiPackage = apiPackage;
    }

    public String getModelPackage() {
        return modelPackage;
    }

    public void setModelPackage(String modelPackage) {
        this.modelPackage = modelPackage;
    }

    public String getInvokerPackage() {
        return invokerPackage;
    }

    public void setInvokerPackage(String invokerPackage) {
        this.invokerPackage = invokerPackage;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public void setDefaults(CodegenSettings defaults) {
        if (generatorName == null || generatorName.isBlank()) {
            generatorName = defaults.getGeneratorName();
        }
        if (library == null || library.isBlank()) {
            library = defaults.getLibrary();
        }
        if (artifactId == null || artifactId.isBlank()) {
            artifactId = defaults.getArtifactId();
        }
        if (apiPackage == null || apiPackage.isBlank()) {
            apiPackage = defaults.getApiPackage();
        }
        if (modelPackage == null || modelPackage.isBlank()) {
            modelPackage = defaults.getModelPackage();
        }
        if (invokerPackage == null || invokerPackage.isBlank()) {
            invokerPackage = defaults.getInvokerPackage();
        }
        if (outputDir == null || outputDir.isBlank()) {
            outputDir = defaults.getOutputDir();
        }
        // Map fields
        for (Map.Entry<String, Object> entry : defaults.additionalProperties.entrySet()) {
            if (additionalProperties.get(entry.getKey()) == null || additionalProperties.get(entry.getKey()).toString().isEmpty()) {
                additionalProperties.put(entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry<String, String> entry : defaults.globalProperties.entrySet()) {
            if (globalProperties.get(entry.getKey()) == null || globalProperties.get(entry.getKey()).isEmpty()) {
                globalProperties.put(entry.getKey(), entry.getValue());
            }
        }
    }
}


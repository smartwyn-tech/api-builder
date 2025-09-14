package io.apicurio.hub.api.rest.impl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.apicurio.hub.api.CodegenConfig;
import io.apicurio.hub.api.beans.*;
import io.apicurio.hub.api.connectors.SourceConnectorFactory;
import io.apicurio.hub.api.content.ContentDereferencer;
import io.apicurio.hub.api.metrics.IApiMetrics;
import io.apicurio.hub.api.rest.ICodeResource;
import io.apicurio.hub.api.security.ISecurityContext;
import io.apicurio.hub.core.beans.*;
import io.apicurio.hub.core.cmd.OaiCommandException;
import io.apicurio.hub.core.cmd.OaiCommandExecutor;
import io.apicurio.hub.core.exceptions.*;
import io.apicurio.hub.core.storage.IStorage;
import io.apicurio.hub.core.storage.StorageException;
import io.apicurio.hub.core.util.FormatUtils;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.io.FileUtils;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author usman.shb013@gmail.com
 */
@ApplicationScoped
public class CodeResource implements ICodeResource {

    private static Logger logger = LoggerFactory.getLogger(CodeResource.class);
    private static ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(Include.NON_NULL);
    }

    @Inject
    private IStorage storage;
    @Inject
    private SourceConnectorFactory sourceConnectorFactory;
    @Inject
    private ISecurityContext security;
    @Inject
    private IApiMetrics metrics;
    @Inject
    private OaiCommandExecutor oaiCommandExecutor;
    @Inject
    private ContentDereferencer dereferencer;

    @Inject
    private CodegenConfig codegenConfig;
    @Inject
    private DesignsResource designsResource;

    /**
     * @see ICodeResource#getCode(String, Optional)
     */
    @Override
    public Response getCode(String designId, Optional<CodegenSettings> codegenSettings)
            throws ServerError, NotFoundException {
        logger.debug("Getting code for API design with ID: {}", designId);
        try {
            ApiDesign design = designsResource.getDesign(designId);
            String content = getApiContent(design, FormatType.JSON, false);
            CodegenSettings settings = codegenSettings.orElseGet(() -> codegenConfig.getSettings());
            settings.setDefaults(codegenConfig.getSettings());
            File tempFile = new File(settings.getOutputDir()+"/open-api-spec.json");
            FileUtils.writeStringToFile(tempFile, content, "UTF-8");
            CodegenConfigurator configurator = setConfigurator(design, settings, tempFile);
            ClientOptInput clientOptInput = configurator.toClientOptInput();
            DefaultGenerator generator = new DefaultGenerator();
            generator.opts(clientOptInput);
            generator.generate();
            tempFile.deleteOnExit();
            File apiDirectory = new File(settings.getOutputDir()+"/"+design.getName()+ "/src/main/java/"+ settings.getApiPackage().replace('.', '/'));
            generateDelegateStubs(apiDirectory);
            return getCodeAsZip(settings.getOutputDir(), design.getName());
        } catch (Exception e) {
            throw new ServerError(e.getMessage());
        }
    }

    private static CodegenConfigurator setConfigurator(ApiDesign design, CodegenSettings settings, File tempFile) {
        CodegenConfigurator configurator = new CodegenConfigurator();
        configurator.setInputSpec(tempFile.getAbsolutePath());
        configurator.setOutputDir(settings.getOutputDir()+"/" + design.getName());
        configurator.setGeneratorName(settings.getGeneratorName());
        configurator.setLibrary(settings.getLibrary());
        configurator.setArtifactId(settings.getArtifactId());
        configurator.setApiPackage(settings.getApiPackage());
        configurator.setModelPackage(settings.getModelPackage());
        configurator.setInvokerPackage(settings.getInvokerPackage());
        configurator.setGlobalProperties(settings.getGlobalProperties());
        configurator.setAdditionalProperties(settings.getAdditionalProperties());
        return configurator;
    }


    /**
     * Gets the current content of an API.
     * @param design
     * @param format
     * @throws ServerError
     * @throws NotFoundException
     */
    private String getApiContent(ApiDesign design, FormatType format, boolean dereference) throws ServerError, NotFoundException {
        try {
            String user = this.security.getCurrentUser().getLogin();

            ApiDesignContent designContent = this.storage.getLatestContentDocument(user, design.getId());
            String content = designContent.getDocument();

            if (design.getType() == ApiDesignType.GraphQL) {
                if (format != null && format != FormatType.SDL) {
                    throw new ServerError("Unsupported format: " + format);
                }
            } else {
                List<ApiDesignCommand> apiCommands = this.storage.listContentCommands(user, design.getId(), designContent.getContentVersion());
                if (!apiCommands.isEmpty()) {
                    List<String> commands = new ArrayList<>(apiCommands.size());
                    for (ApiDesignCommand apiCommand : apiCommands) {
                        commands.add(apiCommand.getCommand());
                    }
                    content = this.oaiCommandExecutor.executeCommands(designContent.getDocument(), commands);
                }

                // If we should dereference the content, do that now.
                if (dereference) {
                    content = dereferencer.dereference(content);
                }

                // Convert to yaml if necessary
                if (format == FormatType.YAML) {
                    content = FormatUtils.jsonToYaml(content);
                } else {
                    content = FormatUtils.formatJson(content);
                }
            }

            return content;
        } catch (StorageException | OaiCommandException | IOException | UnresolvableReferenceException e) {
            throw new ServerError(e);
        }
    }

    public Response getCodeAsZip(String destination, String designName) {
        logger.debug("Downloading a codegen project for API Design {}", designName);
        File zipDir = new File(destination+"/"+designName+"/src/main");
        String zipFile = zipDir + ".zip";
        ZipUtil.pack(zipDir, new File(zipFile));
        StreamingOutput stream = output -> {
            try (FileInputStream fis = new FileInputStream(zipFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                throw new WebApplicationException("Error streaming file", e);
            } finally {
                try {
                    File workingDir = new File(destination);
                    // deletion of the contents of the directory
                    FileUtils.cleanDirectory(workingDir);
                    // Delete the directory itself
                    Files.deleteIfExists(Paths.get(workingDir.getAbsolutePath()));
                    // Delete the zip file
                    Files.deleteIfExists(Paths.get(zipFile));
                } catch (IOException e) {
                    // Handle the exception, e.g., log it
                    e.printStackTrace();
                }
            }
        };
        ResponseBuilder builder = Response.ok().entity(stream)
                .header("Content-Disposition", "attachment; filename=\"" + designName + ".zip" + "\"")
                .header("Content-Type", "application/zip");
        return builder.build();

    }

    private void generateDelegateStubs(File apiDirectory) {
        List<File> delegateFiles = getFilesWithPrefix(apiDirectory, "Delegate");
        delegateFiles.forEach(file -> processDelegateFile(file, apiDirectory.getAbsolutePath()));
    }

    private void processDelegateFile(File file, String apiDirectory) {
        String className = file.getName().replace(".java", "");
        InterfaceInfo interfaceInfo = getInterfaceInfo(file);

        if (interfaceInfo != null) {
            for (MethodInfo methodInfo : interfaceInfo.getMethodInfoList()) {
                Map<String, Object> dataModel = createDataModel(className, interfaceInfo, methodInfo);
                generateFile(className, dataModel, apiDirectory);
            }
        }
    }

    private Map<String, Object> createDataModel(String className, InterfaceInfo interfaceInfo, MethodInfo methodInfo) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("className", className);
        dataModel.put("packageName", interfaceInfo.getPackageName());
        dataModel.put("imports", interfaceInfo.getImports());
        dataModel.put("returnType", methodInfo.getReturnType());
        dataModel.put("methodName", methodInfo.getMethodName());
        dataModel.put("parameters", getFormattedParameters(methodInfo.getParameters()));
        return dataModel;
    }

    private String getFormattedParameters(List<ParameterInfo> parameterInfoList) {
        return parameterInfoList.stream()
                .map(parameter -> parameter.getParamType() + " " + parameter.getParamName())
                .collect(Collectors.joining(", "));
    }

    private List<File> getFilesWithPrefix(File directory, String prefix) {
        return Arrays.stream(directory.listFiles())
                .filter(file -> file.isFile() && file.getName().contains(prefix) || file.isDirectory())
                .flatMap(file -> file.isDirectory() ? getFilesWithPrefix(file, prefix).stream() : Stream.of(file))
                .collect(Collectors.toList());
    }

    private InterfaceInfo getInterfaceInfo(File file) {
        try (FileInputStream in = new FileInputStream(file.getAbsolutePath())) {
            CompilationUnit compilationUnit = JavaParser.parse(in);

            // Extract package name
            String packageName = compilationUnit.getPackage() != null
                    ? compilationUnit.getPackage().getName().toString()
                    : "";

            // Extract import statements
            List<String> imports = new ArrayList<>();
            for (ImportDeclaration importDeclaration : compilationUnit.getImports()) {
                imports.add(importDeclaration.getName().toString());
            }

            // Traverse the AST to extract method information
            List<MethodInfo> methodInfoList = new ArrayList<>();
            compilationUnit.accept(new MethodVisitor(methodInfoList), null);

            // Print or process the collected information
            methodInfoList.forEach(System.out::println);
            return new InterfaceInfo(methodInfoList, packageName, imports);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static class MethodVisitor extends VoidVisitorAdapter<Void> {
        private final List<MethodInfo> methodInfoList;

        public MethodVisitor(List<MethodInfo> methodInfoList) {
            this.methodInfoList = methodInfoList;
        }

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            // Extract method information
            String methodName = n.getName();
            String returnType = n.getType().toString();

            // Extract parameter information if available
            List<ParameterInfo> parameters = new ArrayList<>();
            if (n.getParameters() != null && !n.getParameters().isEmpty()) {
                n.getParameters().forEach(parameter -> {
                    String paramName = parameter.getId().getName();
                    String paramType = parameter.getType().toString();

                    // Add the parameter information to the list
                    parameters.add(new ParameterInfo(paramName, paramType));
                });
            }

            // Add the information to the list
            methodInfoList.add(new MethodInfo(methodName, returnType, parameters));

            super.visit(n, arg);
        }
    }
    public void generateFile(String className, Map<String, Object> root, String destPath) {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setClassLoaderForTemplateLoading(CodeResource.class.getClassLoader(), "/api-templates/delegate-stubs");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        try {
            Template template = cfg.getTemplate("ServiceImplementationTemplate.ftl");
            File fileName = new File(destPath + "/" + className + "Impl.java");
            String dirPath = destPath;

            if (destPath.split("/").length > 1 && className.split("/").length > 1) {
                dirPath = dirPath + className.substring(0, className.lastIndexOf('/'));
            }

            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try (PrintWriter writer = new PrintWriter(fileName)) {
                template.process(root, writer);
            } catch (IOException e) {
                // Handle exceptions, log, etc.
                e.printStackTrace();
            }


        } catch (IOException e) {
            logger.error("Error occurred while generating file " + e.getMessage());
        } catch (TemplateException e) {
            logger.error("Error while updating template " + e.getMessage());
        }
    }


}

/*
 * Here comes the text of your license
 * Each line should be prefixed with  *
 */
package net.thevpc.nuts.lib.template;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NAsk;
import net.thevpc.nuts.util.NAskValidator;
import org.w3c.dom.Document;

/**
 * @author thevpc
 */
public class DefaultProjectTemplate implements ProjectTemplate {

    private final Map<String, ProjectProperty> config = new HashMap<>();
    private TemplateConsole console;
    private boolean askAll = false;
    private List<ProjectTemplateListener> configListeners = new ArrayList<>();
    private NSession session;
    private String targetRoot = "/";
    public Set<String> createPaths = new HashSet<>();
    private MessageNameFormatContext DEFAULT = new MessageNameFormatContext(true, true);
    private MessageNameFormatContext context = DEFAULT;
    private Function<String, String> dollar_converter = new Function<String, String>() {
        @Override
        public String apply(String str) {
            return evalExpression(str);
        }
    };

    public DefaultProjectTemplate(NSession session) {
        this.session = session;
        console = new TemplateConsole() {
            @Override
            public void println(String message, Object... params) {
                DefaultProjectTemplate.this.session.out().println(NMsg.ofC(message, params));
            }

            @Override
            public String ask(String propName, String propertyTitle, StringValidator validator, String defaultValue) {
                if (DefaultProjectTemplate.this.session.getConfirm().orDefault() == NConfirmationMode.YES) {
                    return defaultValue;
                }
                return NAsk.of(session)
                        .forString(
                                NMsg.ofNtf(
                                        NTexts.of(getSession()).ofBuilder()
                                                .append(propertyTitle, NTextStyle.primary4())
                                                .append(" (")
                                                .append(propName, NTextStyle.pale())
                                                .append(")\n ?")
                                                .toString()))
                        .setDefaultValue(defaultValue)
                        .setValidator(new NAskValidator<String>() {
                            @Override
                            public String validate(String value, NAsk<String> question) throws NValidationException {
                                return validator.validate(value);
                            }
                        }).getValue();
            }
        };
    }

    public boolean isAskAll() {
        return askAll;
    }

    public void setAskAll(boolean askAll) {
        this.askAll = askAll;
    }

    public List<ProjectTemplateListener> getConfigListeners() {
        return configListeners;
    }

    public final void setConfigProperty(String name, String defaultvalue, StringValidator g, String title, boolean promenent) {
        ProjectProperty v = getConfigProperty(name);
        v.setDefaultValue(defaultvalue);
        v.setValidator(g);
        v.setTitle(title);
        v.setAskMe(promenent);
    }

    //    public void setConsole(TemplateConsole console) {
//        this.console = console;
//    }
    public TemplateConsole getConsole() {
        return console;
    }

    public void setConfigValue(String propertyName, String value) {
        if (value == null || value.trim().isEmpty()) {
            value = null;
        }
        getConfigProperty(propertyName).setValue(value);
    }

    public void loadConfigProperties(File file) {
        Properties p = new Properties();
        try {
            p.load(new FileReader(file));
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
        for (Map.Entry<Object, Object> e : p.entrySet()) {
            setConfigValue((String) e.getKey(), (String) e.getValue());
        }
    }

    public Properties getConfigProperties() {
        Properties p = new Properties();
        for (Map.Entry<String, ProjectProperty> entry : config.entrySet()) {
            ProjectProperty value = entry.getValue();
            if (entry.getKey() != null && value != null) {
                if (value.getValue() != null) {
                    p.put(entry.getKey(), value.getValue());
                }
            } else {
                System.out.print("");
            }
        }
        return p;
    }

    public ProjectProperty getConfigProperty(String name) {
        ProjectProperty projectProperty = config.get(name);
        if (projectProperty == null) {
            projectProperty = new ProjectProperty(name, name, null, null, new ValidatorFactory(getSession()).STRING, this, false);
            config.put(name, projectProperty);
        }
        return projectProperty;
    }

    public File getProjectRootFolder() {
        return new File(getConfigProperty("ProjectRootFolder").get());
    }

    public void registerDefaultsFunctions() {
        context.register("path", new AbstractFunction() {
            @Override
            public Object evalArgs(Object[] args, MessageNameFormat format, Function<String, Object> provider) {
                return JavaUtils.path(String.valueOf(args[0]));
            }
        });
        context.register("now", new MessageNameFormat.Function() {
            @Override
            public Object eval(MessageNameFormat.ExprNode[] args, MessageNameFormat format, Function<String, Object> provider, MessageNameFormatContext messageNameFormatContext) {
                return new Date();
            }
        });
        context.register("packageName", new AbstractFunction() {
            @Override
            public Object evalArgs(Object[] args, MessageNameFormat format, Function<String, Object> provider) {
                return JavaUtils.packageName(String.valueOf(args[0]));
            }
        });
        context.register("className", new AbstractFunction() {
            @Override
            public Object evalArgs(Object[] args, MessageNameFormat format, Function<String, Object> provider) {
                return JavaUtils.className(String.valueOf(args[0]));
            }
        });
        context.register("varName", new AbstractFunction() {
            @Override
            public Object evalArgs(Object[] args, MessageNameFormat format, Function<String, Object> provider) {
                return JavaUtils.varName(String.valueOf(args[0]));
            }
        });
        context.register("pathToPackage", new AbstractFunction() {
            @Override
            public Object evalArgs(Object[] args, MessageNameFormat format, Function<String, Object> provider) {
                return JavaUtils.pathToPackage(String.valueOf(args[0]));
            }
        });
    }

    public final MessageNameFormat.Function getFunction(String name) {
        return context.getFunction(name);
    }

    public final void registerFunction(String name, MessageNameFormat.Function function) {
        if (!context.isEditable()) {
            context = context.toEditable();
        }
        context.register(name, function);
    }

    public final void unregisterFunction(String name) {
        if (!context.isEditable()) {
            context = context.toEditable();
        }
        context.register(name, null);
    }

    public MessageNameFormatContext getMessageNameFormatContext() {
        return context;
    }

    public String evalExpression(String expression) {
        MessageNameFormat f = new MessageNameFormat("${" + expression + "}");
        return f.format(new Function<String, Object>() {
            @Override
            public Object apply(String string) {
                return getConfigProperty(string).get();
            }
        }, getMessageNameFormatContext());
    }

    public void setNewlyCreated(String p) {
        createPaths.add(p);
    }

    public boolean isNewlyCreated(String p) {
        return createPaths.contains(p);
    }

    public void println(String message, Object... params) {
        getConsole().println(replacePlaceHoldersSimple(message), params);
    }

    public static String sourceConvertPath(String path) {
        StringBuilder sb = new StringBuilder();
        sb.append("/META-INF/templates");
        if (!path.startsWith("/")) {
            sb.append("/");
        }
        sb.append(path);
        return sb.toString();
    }

    public String replacePlaceHoldersSimple(String path) {
        return _StringUtils.replacePlaceHolders(path, "${", "}", dollar_converter);
    }

    public File convertToPath(String path) {
        return new File(getProjectRootFolder(), _StringUtils.replacePlaceHolders(targetRoot + "/" + path, "${", "}", dollar_converter));
    }

    public Document loadSourceXmlDocument(String from) throws UncheckedIOException {
        try {
            return XmlUtils.load(IOUtils.getTextResource(sourceConvertPath(from)));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public Document loadTargetXmlDocument(String from) throws UncheckedIOException {
        try {
            return XmlUtils.load(IOUtils.getText(convertToPath(from)));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void storeTargetXmlDocument(Document doc, String to) throws UncheckedIOException {
        try {
            IOUtils.writeString(XmlUtils.toString(doc), convertToPath(to), this);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void copyXml(String from, String toFolder) throws UncheckedIOException {
        try {
            String n = IOUtils.extractFileName(from);
            String text = IOUtils.getTextResource(sourceConvertPath(from));
            String converted = _StringUtils.replacePlaceHolders(text, "${{", "}}", dollar_converter);
            IOUtils.writeString(converted, convertToPath(toFolder + "/" + n), this);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void copyXml(String from, String toFolder, String newName) throws UncheckedIOException {
        try {
            String text = IOUtils.getTextResource(sourceConvertPath(from));
            String converted = _StringUtils.replacePlaceHolders(text, "${{", "}}", dollar_converter);
            IOUtils.writeString(converted, convertToPath(toFolder + "/" + newName), this);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void copyProperties(String from, String toFolder) throws UncheckedIOException {
        try {
            String n = IOUtils.extractFileName(from);
            String text = IOUtils.getTextResource(sourceConvertPath(from));
            String converted = _StringUtils.replacePlaceHolders(text, "${{", "}}", dollar_converter);
            IOUtils.writeString(converted, convertToPath(toFolder + "/" + n), this);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void targetAppendProperty(String toFile, String name, String value) throws UncheckedIOException {
        targetAppendProperties(toFile, new String[]{name, value});
    }

    public void targetAppendProperties(String toFile, String[] keyValues) throws UncheckedIOException {
        try {
            File okFile = convertToPath(toFile);
            Properties oldProperties = new Properties();
            oldProperties.load(new StringReader(IOUtils.getText(okFile)));

            Properties newProperties = new Properties();
            for (int i = 0; i < keyValues.length; i += 2) {
                String name = keyValues[i];
                String value = keyValues[i + 1];
                String name2 = _StringUtils.replacePlaceHolders(name, "${{", "}}", dollar_converter);
                String value2 = _StringUtils.replacePlaceHolders(value, "${{", "}}", dollar_converter);
                if (!value2.equals(oldProperties.getProperty(name2))) {
                    newProperties.setProperty(name2, value2);
                }
            }
            if (newProperties.size() > 0) {
                IOUtils.writeStringAppend(IOUtils.toString(newProperties, null), okFile);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void copyJava(String from, String toFolder) throws UncheckedIOException {
        try {
            String text = IOUtils.getTextResource(sourceConvertPath(from));
            String converted = _StringUtils.replacePlaceHolders(text, "${{", "}}", dollar_converter);
            ClassInfo c = JavaUtils.detectedJavaClassInfo(converted);
            IOUtils.writeString(converted, new File(convertToPath(toFolder).getPath() + "/" + c.getFullClassName().replace('.', '/') + ".java"), this);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void copyScala(String from, String toFolder) throws UncheckedIOException {
        try {
            String text = IOUtils.getTextResource(sourceConvertPath(from));
            String converted = _StringUtils.replacePlaceHolders(text, "${{", "}}", dollar_converter);
            ClassInfo c = JavaUtils.detectedScalaClassInfo(converted);
            IOUtils.writeString(converted, new File(convertToPath(toFolder).getPath() + "/" + c.getFullClassName().replace('.', '/') + ".scala"), this);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public String getTargetRoot() {
        return targetRoot;
    }

    public void setTargetRoot(String targetRoot) {
        this.targetRoot = targetRoot;
    }

    public void targetMkdirs(String path) {
        convertToPath(path).mkdirs();
    }

    public void targetAddPomParentModule(String pomxml, String modulePath) throws UncheckedIOException {
        try {
            Document doc = loadTargetXmlDocument(pomxml);
            if (XmlUtils.addMavenModule(doc, replacePlaceHoldersSimple(modulePath))) {
                storeTargetXmlDocument(doc, pomxml);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public NSession getSession() {
        return session;
    }

    public File resolveFirstPomFile(File folder) {
        while (folder != null) {
            File p = resolvePomFile(folder);
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    public File resolvePomFile(File folder) {
        try {
            File pomFile = new File(folder, "pom.xml");
            if (!pomFile.isFile()) {
                return null;
            }
            NDescriptorParser.of(session).setDescriptorStyle(NDescriptorStyle.MAVEN).parse(pomFile);
            return pomFile;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public NDescriptor askForPomAndUpdateConfig() {
        NDescriptor pomxml = askForPom();
        getConfigProperty("ProjectGroup").setValue(pomxml.getId().getGroupId());
        getConfigProperty("ProjectName").setValue(pomxml.getId().getArtifactId());
        getConfigProperty("ProjectVersion").setValue(pomxml.getId().getVersion().toString());
        return pomxml;
    }

    public NDescriptor askForPom() {
        File p = resolvePomFile(getProjectRootFolder());
        if (p == null) {
            p = resolveFirstPomFile(getProjectRootFolder());
            if (p != null) {
                if (!NAsk.of(getSession())
                        .forBoolean(NMsg.ofC("accept project location %s?",
                                NTexts.of(session).ofStyled(p.getPath(), NTextStyle.path())))
                        .setDefaultValue(false)
                        .getBooleanValue()) {
                    throw new NCancelException(getSession());
                }
            }
        }
        try {
            return NDescriptorParser.of(session).setDescriptorStyle(NDescriptorStyle.MAVEN)
                    .parse(new File(getProjectRootFolder(), "pom.xml")).get(session);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getProjectName() {
        return getConfigProperty("ProjectName").get();
    }

    public String getProjectVersion() {
        return getConfigProperty("ProjectVersion").get();
    }

    public String getProjectGroup() {
        return getConfigProperty("ProjectGroup").get();
    }

    public String getModuleName() {
        return getConfigProperty("ModuleName").get();
    }

    public String getModuleVersion() {
        return getConfigProperty("ModuleVersion").get();
    }
}

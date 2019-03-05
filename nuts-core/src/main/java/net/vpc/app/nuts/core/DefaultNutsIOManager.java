package net.vpc.app.nuts.core;

import com.google.gson.*;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.terminals.AbstractSystemTerminalAdapter;
import net.vpc.app.nuts.core.terminals.DefaultSystemTerminal;
import net.vpc.app.nuts.core.util.NullOutputStream;
import net.vpc.app.nuts.core.util.*;
import net.vpc.common.io.*;
import net.vpc.common.strings.StringConverter;
import net.vpc.common.strings.StringConverterMap;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.Convert;
import net.vpc.common.util.MapBuilder;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.vpc.app.nuts.core.terminals.DefaultNutsSessionTerminal;

public class DefaultNutsIOManager implements NutsIOManager {

    private static final Logger log = Logger.getLogger(DefaultNutsIOManager.class.getName());
    private NutsWorkspace workspace;
    private static Gson GSON;
    private static Gson GSON_PRETTY;
    private final StringConverter pathExpansionConverter = new StringConverter() {
        @Override
        public String convert(String from) {
            switch (from) {
                case "home.config":
                    return workspace.getConfigManager().getHome(NutsStoreFolder.CONFIG);
                case "home.programs":
                    return workspace.getConfigManager().getHome(NutsStoreFolder.PROGRAMS);
                case "home.lib":
                    return workspace.getConfigManager().getHome(NutsStoreFolder.LIB);
                case "home.temp":
                    return workspace.getConfigManager().getHome(NutsStoreFolder.TEMP);
                case "home.var":
                    return workspace.getConfigManager().getHome(NutsStoreFolder.VAR);
                case "home.cache":
                    return workspace.getConfigManager().getHome(NutsStoreFolder.CACHE);
                case "home.logs":
                    return workspace.getConfigManager().getHome(NutsStoreFolder.LOGS);
                case "workspace":
                    return workspace.getConfigManager().getRunningContext().getWorkspace();
                case "user.home":
                    return System.getProperty("user.home");
                case "config":
                    return workspace.getConfigManager().getRunningContext().getStoreLocation(NutsStoreFolder.CONFIG);
                case "lib":
                    return workspace.getConfigManager().getRunningContext().getStoreLocation(NutsStoreFolder.LIB);
                case "programs":
                    return workspace.getConfigManager().getRunningContext().getStoreLocation(NutsStoreFolder.PROGRAMS);
                case "cache":
                    return workspace.getConfigManager().getRunningContext().getStoreLocation(NutsStoreFolder.CACHE);
                case "temp":
                    return workspace.getConfigManager().getRunningContext().getStoreLocation(NutsStoreFolder.TEMP);
                case "logs":
                    return workspace.getConfigManager().getRunningContext().getStoreLocation(NutsStoreFolder.LOGS);
                case "var":
                    return workspace.getConfigManager().getRunningContext().getStoreLocation(NutsStoreFolder.VAR);
            }
            return "${" + from + "}";
        }
    };

    public DefaultNutsIOManager(NutsWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public InputStream monitorInputStream(String path, String name, NutsSession session) {
        InputStream stream = null;
        NutsURLHeader header = null;
        long size = -1;
        try {
            if (URLUtils.isURL(path)) {
                if (URLUtils.isFileURL(new URL(path))) {
                    path = URLUtils.toFile(new URL(path)).getPath();
                    size = new File(path).length();
                    stream = new FileInputStream(path);
                } else {
                    NutsHttpConnectionFacade f = CoreHttpUtils.getHttpClientFacade(workspace, path);
                    try {

                        header = f.getURLHeader();
                        size = header.getContentLength();
                    } catch (Exception ex) {
                        //ignore error
                    }
                    stream = f.open();
                }
            } else {
                //this is file!
                size = new File(path).length();
                stream = new FileInputStream(path);
            }
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
        return monitorInputStream(stream, size, (name == null ? path : name), session);
    }

    @Override
    public InputStream monitorInputStream(InputStream stream, long length, String name, NutsSession session) {
        if (length > 0) {
            return IOUtils.monitor(stream, null, (name == null ? "Stream" : name), length, new DefaultNutsInputStreamMonitor(workspace, session.getTerminal().getOut()));
        } else {
            return stream;
        }
    }

    @Override
    public InputStream monitorInputStream(String path, Object source, NutsSession session) {
        String sourceName = String.valueOf(path);
        boolean monitorable = true;
        Object o = session.getProperty("monitor-allowed");
        if (o != null) {
            o = Convert.toBoolean(o);
        }
        if (o instanceof Boolean) {
            monitorable = ((Boolean) o).booleanValue();
        } else {
            if (source instanceof NutsId) {
                NutsId d = (NutsId) source;
                if (NutsConstants.FACE_COMPONENT_HASH.equals(d.getFace())) {
                    monitorable = false;
                }
                if (NutsConstants.FACE_DESC_HASH.equals(d.getFace())) {
                    monitorable = false;
                }
            }
        }
        if (!CoreNutsUtils.getSystemBoolean("nuts.monitor.enabled", true)) {
            monitorable = false;
        }
        DefaultNutsInputStreamMonitor monitor = null;
        if (monitorable && log.isLoggable(Level.INFO)) {
            monitor = new DefaultNutsInputStreamMonitor(workspace, session.getTerminal().getOut());
        }
        boolean verboseMode =
                CoreNutsUtils.getSystemBoolean("nuts.monitor.start", false)
                        ||
                        workspace.getConfigManager().getOptions().getLogConfig() != null && workspace.getConfigManager().getOptions().getLogConfig().getLogLevel() == Level.FINEST;
        InputStream stream = null;
        NutsURLHeader header = null;
        long size = -1;
        try {
            if (verboseMode && monitor != null) {
                monitor.onStart(new InputStreamEvent(source, sourceName, 0, 0, 0, 0, size, null));
            }
            NutsHttpConnectionFacade f = CoreHttpUtils.getHttpClientFacade(workspace, path);
            try {

                header = f.getURLHeader();
                size = header.getContentLength();
            } catch (Exception ex) {
                //ignore error
            }
            stream = f.open();
        } catch (IOException e) {
            if (verboseMode && monitor != null) {
                monitor.onComplete(new InputStreamEvent(source, sourceName, 0, 0, 0, 0, size, e));
            }
            throw new NutsIOException(e);
        }
        if (stream != null) {
            if (path.toLowerCase().startsWith("file://")) {
                log.log(Level.FINE, "[START  ] Downloading file {0}", new Object[]{path});
            } else {
                log.log(Level.FINEST, "[START  ] Downloading url {0}", new Object[]{path});
            }
        } else {
            log.log(Level.FINEST, "[ERROR  ] Downloading url failed : {0}", new Object[]{path});
        }

        if (!monitorable) {
            return stream;
        }
        if (monitor != null) {
            DefaultNutsInputStreamMonitor finalMonitor = monitor;
            if (!verboseMode) {
                monitor.onStart(new InputStreamEvent(source, sourceName, 0, 0, 0, 0, size, null));
            }
            //adapt to disable onStart call (it is already invoked)
            return IOUtils.monitor(stream, source, sourceName, size, new InputStreamMonitor() {
                @Override
                public void onStart(InputStreamEvent event) {
                }

                @Override
                public void onComplete(InputStreamEvent event) {
                    finalMonitor.onComplete(event);
                    if (event.getException() != null) {
                        log.log(Level.FINEST, "[ERROR    ] Download Failed    : {0}", new Object[]{path});
                    } else {
                        log.log(Level.FINEST, "[SUCCESS  ] Download Succeeded : {0}", new Object[]{path});
                    }
                }

                @Override
                public boolean onProgress(InputStreamEvent event) {
                    return finalMonitor.onProgress(event);
                }
            });
        }
        return stream;

    }

    @Override
    public String toJsonString(Object obj, boolean pretty) {
        StringWriter w=new StringWriter();
        writeJson(obj,w,pretty);
        return w.toString();
    }

    @Override
    public void writeJson(Object obj, Writer out, boolean pretty) {
        getGson(pretty).toJson(obj, out);
        try {
            out.flush();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    @Override
    public <T> T readJson(Reader reader, Class<T> cls) {
        return getGson(true).fromJson(reader, cls);
    }

    @Override
    public <T> T readJson(File file, Class<T> cls) {
        try (FileReader r = new FileReader(file)) {
            return readJson(r, cls);
        } catch (IOException ex) {
            throw new NutsIOException(ex);
        }
    }

    @Override
    public <T> void writeJson(Object obj, File file, boolean pretty) {
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        try (FileWriter w = new FileWriter(file)) {
            writeJson(obj, w, pretty);
        } catch (IOException ex) {
            throw new NutsIOException(ex);
        }
    }

    @Override
    public <T> void writeJson(Object obj, PrintStream printStream, boolean pretty) {
        Writer w = new PrintWriter(printStream);
        writeJson(obj, w, pretty);
        try {
            w.flush();
        } catch (IOException ex) {
            throw new NutsIOException(ex);
        }
    }

    @Override
    public String expandPath(String path) {
        return expandPath(path, workspace.getConfigManager().getWorkspaceLocation());
    }

    public String expandPath(String path, String baseFolder) {
        if (path != null && path.length() > 0) {
            path = StringUtils.replaceDollarPlaceHolders(path, pathExpansionConverter);
            if (path.startsWith("file:") || path.startsWith("http://") || path.startsWith("https://")) {
                return path;
            }
            if (path.startsWith("~")) {
                if (path.equals("~~")) {
                    String nutsHome = workspace.getConfigManager().getHome(NutsStoreFolder.CONFIG);
                    return CoreIOUtils.getAbsolutePath(nutsHome);
                } else if (path.startsWith("~~") && path.length() > 2 && (path.charAt(2) == '/' || path.charAt(2) == '\\')) {
                    String nutsHome = workspace.getConfigManager().getHome(NutsStoreFolder.CONFIG);
                    return CoreIOUtils.getAbsolutePath(nutsHome + File.separator + path.substring(3));
                } else if (path.equals("~")) {
                    return (System.getProperty("user.home"));
                } else if (path.startsWith("~") && path.length() > 1 && (path.charAt(1) == '/' || path.charAt(1) == '\\')) {
                    return System.getProperty("user.home") + File.separator + path.substring(2);
                } else if (baseFolder != null) {
                    return CoreIOUtils.getAbsolutePath(baseFolder + File.separator + path);
                } else {
                    return CoreIOUtils.getAbsolutePath(path);
                }
            } else if (FileUtils.isAbsolutePath(path)) {
                return CoreIOUtils.getAbsolutePath(path);
            } else if (baseFolder != null) {
                return CoreIOUtils.getAbsolutePath(baseFolder + File.separator + path);
            } else {
                return CoreIOUtils.getAbsolutePath(path);
            }
        }
        return CoreIOUtils.getAbsolutePath(baseFolder);
    }

    @Override
    public String getResourceString(String resource, Class cls, String defaultValue) {
        String help = null;
        try {
            InputStream s = null;
            try {
                s = cls.getResourceAsStream(resource);
                if (s != null) {
                    help = IOUtils.loadString(s, true);
                }
            } finally {
                if (s != null) {
                    s.close();
                }
            }
        } catch (IOException e) {
            Logger.getLogger(Nuts.class.getName()).log(Level.SEVERE, "Unable to load text from " + resource, e);
        }
        if (help == null) {
            help = defaultValue;//"no help found";
        }
        HashMap<String, String> props = new HashMap<>((Map) System.getProperties());
        props.putAll(workspace.getConfigManager().getRuntimeProperties());
        help = StringUtils.replaceDollarPlaceHolders(help, new StringConverterMap(props));
        return help;
    }

    @Override
    public String computeHash(InputStream input) {
        return CoreSecurityUtils.evalSHA1(input, false);
    }

    @Override
    public InputStream createNullInputStream() {
        return NullInputStream.INSTANCE;
    }

    @Override
    public PrintStream createNullPrintStream() {
        return createPrintStream(NullOutputStream.INSTANCE, NutsTerminalMode.INHERITED);
    }

    @Override
    public PrintStream createPrintStream(File out) {
        if (out == null) {
            return null;
        }
        try {
            return new PrintStream(out);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public PrintStream createPrintStream(OutputStream out, NutsTerminalMode mode) {
        if (mode == null) {
            mode = NutsTerminalMode.INHERITED;
        }
        if (mode == NutsTerminalMode.FORMATTED) {
            if (workspace.getConfigManager().getOptions().getTerminalMode() == NutsTerminalMode.FILTERED) {
                //if nuts started with --no-color modifier, will disable FORMATTED terminal mode
                mode = NutsTerminalMode.FILTERED;
            }
        }
        switch (mode) {
            case FORMATTED: {
                if (out instanceof NutsFormattedPrintStream) {
                    return ((PrintStream) out);
                }
                if (out instanceof NutsFormatFilteredPrintStream) {
                    return createPrintStream(((NutsFormatFilteredPrintStream) out).getUnformattedInstance(), mode);
                }
                //return new NutsDefaultFormattedPrintStream(out);
                return (PrintStream) workspace.getExtensionManager().createSupported(NutsFormattedPrintStream.class,
                        MapBuilder.of("workspace", this, "out", out).build(),
                        new Class[]{OutputStream.class}, new Object[]{out});
            }
            case FILTERED: {
                if (out instanceof NutsFormatFilteredPrintStream) {
                    return ((PrintStream) out);
                }
                if (out instanceof NutsFormattedPrintStream) {
                    return createPrintStream(((NutsFormattedPrintStream) out).getUnformattedInstance(), mode);
                }
                //return new NutsDefaultFormattedPrintStream(out);
                return (PrintStream) workspace.getExtensionManager().createSupported(NutsFormatFilteredPrintStream.class,
                        MapBuilder.of("workspace", this, "out", out).build(),
                        new Class[]{OutputStream.class}, new Object[]{out});
            }
            case INHERITED: {
                if (out instanceof PrintStream) {
                    return (PrintStream) out;
                }
                return new PrintStream(out);
            }
        }
        throw new IllegalArgumentException("Unsupported NutsTerminalMode " + mode);
    }

    @Override
    public NutsSessionTerminal createTerminal() {
        return createTerminal(null);
    }

    @Override
    public NutsSessionTerminal createTerminal(NutsTerminalBase parent) {
        if (parent == null) {
            parent = new AbstractSystemTerminalAdapter(){
                @Override
                public NutsSystemTerminalBase getParent() {
                    return  workspace.getSystemTerminal();
                }
            };
        }
        NutsSessionTerminalBase termb = workspace.getExtensionManager().createSupported(NutsSessionTerminalBase.class, null);
        if (termb == null) {
            throw new NutsExtensionMissingException(NutsSessionTerminal.class, "Terminal");
        }
        try {
            NutsSessionTerminal term = null;
            if (termb instanceof NutsSessionTerminal) {
                term = (NutsSessionTerminal) termb;
                term.install(workspace);
                term.setParent(parent);
            } else {
                term = new DefaultNutsSessionTerminal();
                term.install(workspace);
                term.setParent(termb);
            }
            return term;
        } catch (Exception anyException) {
            final NutsSessionTerminal c = new DefaultNutsSessionTerminal();
            c.install(workspace);
            c.setParent(parent);
            return c;
        }
    }

    @Override
    public void downloadPath(String from, File to, Object source, NutsSession session) {
        CoreNutsUtils.copy(monitorInputStream(from, source, session), to, true, true);
    }

    @Override
    public File createTempFile(String name) {
        return createTempFile(name, null);
    }

    @Override
    public File createTempFolder(String name) {
        return createTempFolder(name, null);
    }

    @Override
    public File createTempFolder(String name, NutsRepository repository) {
        File folder = null;
        if (repository == null) {
            folder = new File(workspace.getConfigManager().getStoreLocation(NutsStoreFolder.TEMP));
        } else {
            folder = new File(repository.getConfigManager().getStoreLocation(NutsStoreFolder.TEMP));
        }
        final File temp;
        if (StringUtils.isEmpty(name)) {
            name = "temp-";
        } else if (name.length() < 3) {
            name += "-temp-";
        }
        try {
            temp = File.createTempFile(name, Long.toString(System.nanoTime()), folder);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        if (!(temp.delete())) {
            throw new RuntimeException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new RuntimeException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }

    @Override
    public File createTempFile(String name, NutsRepository repository) {
        File folder = null;
        if (repository == null) {
            folder = new File(workspace.getConfigManager().getStoreLocation(NutsStoreFolder.TEMP));
        } else {
            folder = new File(repository.getConfigManager().getStoreLocation(NutsStoreFolder.TEMP));
        }
        String prefix = "temp-";
        String ext = null;
        if (!StringUtils.isEmpty(name)) {
            ext = FileUtils.getFileExtension(name);
            prefix = name;
            if (prefix.length() < 3) {
                prefix = prefix + "-temp-";
            }
            if (!ext.isEmpty()) {
                ext = "." + ext;
                if (ext.length() < 3) {
                    ext = ".tmp" + ext;
                }
            } else {
                ext = "-nuts";
            }
        }
        try {
            return File.createTempFile(prefix, "-nuts" + (ext != null ? ("." + ext) : ""), folder);
        } catch (IOException e) {
            throw new NutsIOException(e);
        }

    }

    @Override
    public String getSHA1(NutsDescriptor descriptor) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        workspace.getFormatManager().createDescriptorFormat().setPretty(false).format(descriptor, o);
        return CoreSecurityUtils.evalSHA1(new ByteArrayInputStream(o.toByteArray()), true);
    }

    public static GsonBuilder prepareBuilder() {
        return new GsonBuilder()
                //                .registerTypeHierarchyAdapter(NutsDescriptor.class, new JsonDeserializer<NutsDescriptor>() {
                //                    @Override
                //                    public NutsDescriptor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                //                        return context.deserialize(json,DefaultNutsDescriptor.class);
                //                    }
                //                })
                .registerTypeHierarchyAdapter(NutsId.class, new NutsIdJsonAdapter())
                .registerTypeHierarchyAdapter(NutsVersion.class, new NutsVersionJsonAdapter())
                .registerTypeHierarchyAdapter(NutsDescriptor.class, new NutsDescriptorJsonAdapter())
                .registerTypeHierarchyAdapter(NutsDependency.class, new NutsNutsDependencyJsonAdapter());
    }

    private Gson getGson(boolean pretty) {
        if (pretty) {
            if (GSON_PRETTY == null) {
                GSON_PRETTY = prepareBuilder().setPrettyPrinting().create();
            }
            return GSON_PRETTY;
        } else {
            if (GSON == null) {
                GSON = prepareBuilder().create();
            }
            return GSON;
        }
    }

    private static class NutsIdJsonAdapter implements
            com.google.gson.JsonSerializer<NutsId>,
            com.google.gson.JsonDeserializer<NutsId> {

        @Override
        public NutsId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String s = context.deserialize(json, String.class);
            if (s == null) {
                return null;
            }
            return CoreNutsUtils.parseRequiredNutsId(s);
        }

        @Override
        public JsonElement serialize(NutsId src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src == null ? null : src.toString());
        }
    }

    private static class NutsVersionJsonAdapter implements
            com.google.gson.JsonSerializer<NutsVersion>,
            com.google.gson.JsonDeserializer<NutsVersion> {

        @Override
        public NutsVersion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String s = context.deserialize(json, String.class);
            if (s == null) {
                return null;
            }
            return DefaultNutsVersion.valueOf(s);
        }

        @Override
        public JsonElement serialize(NutsVersion src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src == null ? null : src.toString());
        }
    }

    private static class NutsDescriptorJsonAdapter implements
            com.google.gson.JsonSerializer<NutsDescriptor>,
            com.google.gson.JsonDeserializer<NutsDescriptor> {

        @Override
        public NutsDescriptor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            DefaultNutsDescriptorBuilder b = context.deserialize(json, DefaultNutsDescriptorBuilder.class);
            return b.build();
        }

        @Override
        public JsonElement serialize(NutsDescriptor src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                return context.serialize(new DefaultNutsDescriptorBuilder(src));
            }
            return context.serialize(src);
        }
    }

    private static class NutsNutsDependencyJsonAdapter implements
            com.google.gson.JsonSerializer<NutsDependency>,
            com.google.gson.JsonDeserializer<NutsDependency> {

        @Override
        public NutsDependency deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            DefaultNutsDependencyBuilder b = context.deserialize(json, DefaultNutsDependencyBuilder.class);
            return b.build();
        }

        @Override
        public JsonElement serialize(NutsDependency src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                return context.serialize(new DefaultNutsDependencyBuilder(src));
            }
            return context.serialize(src);
        }
    }

}

package net.vpc.app.nuts.core;

import com.google.gson.*;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.terminals.AbstractSystemTerminalAdapter;
import net.vpc.app.nuts.core.util.NullOutputStream;
import net.vpc.app.nuts.core.util.*;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.vpc.app.nuts.core.terminals.DefaultNutsSessionTerminal;
import net.vpc.app.nuts.core.util.bundledlibs.io.InputStreamEvent;
import net.vpc.app.nuts.core.util.bundledlibs.io.InputStreamMonitor;

public class DefaultNutsIOManager implements NutsIOManager {

    private static final Logger log = Logger.getLogger(DefaultNutsIOManager.class.getName());
    private NutsWorkspace ws;
    private static Gson GSON;
    private static Gson GSON_PRETTY;
    private final Function<String, String> pathExpansionConverter = new Function<String, String>() {
        @Override
        public String apply(String from) {
            switch (from) {
                case "home.config":
                    return ws.config().getHomeLocation(NutsStoreLocation.CONFIG).toString();
                case "home.programs":
                    return ws.config().getHomeLocation(NutsStoreLocation.PROGRAMS).toString();
                case "home.lib":
                    return ws.config().getHomeLocation(NutsStoreLocation.LIB).toString();
                case "home.temp":
                    return ws.config().getHomeLocation(NutsStoreLocation.TEMP).toString();
                case "home.var":
                    return ws.config().getHomeLocation(NutsStoreLocation.VAR).toString();
                case "home.cache":
                    return ws.config().getHomeLocation(NutsStoreLocation.CACHE).toString();
                case "home.logs":
                    return ws.config().getHomeLocation(NutsStoreLocation.LOGS).toString();
                case "workspace":
                    return ws.config().getRunningContext().getWorkspace();
                case "user.home":
                    return System.getProperty("user.home");
                case "config":
                    return ws.config().getRunningContext().getStoreLocation(NutsStoreLocation.CONFIG);
                case "lib":
                    return ws.config().getRunningContext().getStoreLocation(NutsStoreLocation.LIB);
                case "programs":
                    return ws.config().getRunningContext().getStoreLocation(NutsStoreLocation.PROGRAMS);
                case "cache":
                    return ws.config().getRunningContext().getStoreLocation(NutsStoreLocation.CACHE);
                case "temp":
                    return ws.config().getRunningContext().getStoreLocation(NutsStoreLocation.TEMP);
                case "logs":
                    return ws.config().getRunningContext().getStoreLocation(NutsStoreLocation.LOGS);
                case "var":
                    return ws.config().getRunningContext().getStoreLocation(NutsStoreLocation.VAR);
            }
            return "${" + from + "}";
        }
    };

    public DefaultNutsIOManager(NutsWorkspace workspace) {
        this.ws = workspace;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public InputStream monitorInputStream(String path, String name, NutsTerminalProvider session) {
        InputStream stream = null;
        NutsURLHeader header = null;
        long size = -1;
        try {
            if (CoreIOUtils.isURL(path)) {
                if (CoreIOUtils.isPathFile(path)) {
//                    path = URLUtils.toFile(new URL(path)).getPath();
                    Path p = CoreIOUtils.toPathFile(path);
                    size = Files.size(p);
                    stream = Files.newInputStream(p);
                } else {
                    NutsHttpConnectionFacade f = CoreIOUtils.getHttpClientFacade(ws, path);
                    try {

                        header = f.getURLHeader();
                        size = header.getContentLength();
                    } catch (Exception ex) {
                        //ignore error
                    }
                    stream = f.open();
                }
            } else {
                Path p = ws.io().path(path);
                //this is file!
                size = Files.size(p);
                stream = Files.newInputStream(p);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return monitorInputStream(stream, size, (name == null ? path : name), session);
    }

    @Override
    public InputStream monitorInputStream(InputStream stream, long length, String name, NutsTerminalProvider session) {
        if (length > 0) {
            if (session == null) {
                session = ws.createSession();
            }
            return CoreIOUtils.monitor(stream, null, (name == null ? "Stream" : name), length, new DefaultNutsInputStreamMonitor(ws, session.getTerminal().getOut()));
        } else {
            return stream;
        }
    }

    @Override
    public InputStream monitorInputStream(InputStream stream, NutsTerminalProvider session) {
        if (stream instanceof InputStreamMetadataAware) {
            if (session == null) {
                session = ws.createSession();
            }
            return CoreIOUtils.monitor(stream, null, new DefaultNutsInputStreamMonitor(ws, session.getTerminal().getOut()));
        } else {
            return stream;
        }
    }

    @Override
    public InputStream monitorInputStream(String path, Object source, NutsTerminalProvider session) {
        String sourceName = String.valueOf(path);
        boolean monitorable = true;
        Object o = session.getProperty("monitor-allowed");
        if (o != null) {
            o = CoreCommonUtils.convertToBoolean(String.valueOf(o), false);
        }
        if (o instanceof Boolean) {
            monitorable = ((Boolean) o).booleanValue();
        } else {
            if (source instanceof NutsId) {
                NutsId d = (NutsId) source;
                if (NutsConstants.QueryFaces.COMPONENT_HASH.equals(d.getFace())) {
                    monitorable = false;
                }
                if (NutsConstants.QueryFaces.DESC_HASH.equals(d.getFace())) {
                    monitorable = false;
                }
            }
        }
        if (!CoreCommonUtils.getSystemBoolean("nuts.monitor.enabled", true)) {
            monitorable = false;
        }
        DefaultNutsInputStreamMonitor monitor = null;
        if (monitorable && log.isLoggable(Level.INFO)) {
            monitor = new DefaultNutsInputStreamMonitor(ws, session.getTerminal().getOut());
        }
        boolean verboseMode
                = CoreCommonUtils.getSystemBoolean("nuts.monitor.start", false)
                || ws.config().getOptions().getLogConfig() != null && ws.config().getOptions().getLogConfig().getLogLevel() == Level.FINEST;
        InputStream stream = null;
        NutsURLHeader header = null;
        long size = -1;
        try {
            if (verboseMode && monitor != null) {
                monitor.onStart(new InputStreamEvent(source, sourceName, 0, 0, 0, 0, size, null));
            }
            NutsHttpConnectionFacade f = CoreIOUtils.getHttpClientFacade(ws, path);
            try {

                header = f.getURLHeader();
                size = header.getContentLength();
            } catch (Exception ex) {
                //ignore error
            }
            stream = f.open();
        } catch (UncheckedIOException e) {
            if (verboseMode && monitor != null) {
                monitor.onComplete(new InputStreamEvent(source, sourceName, 0, 0, 0, 0, size, e));
            }
            throw e;
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
            return CoreIOUtils.monitor(stream, source, sourceName, size, new InputStreamMonitor() {
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
        StringWriter w = new StringWriter();
        writeJson(obj, w, pretty);
        return w.toString();
    }

    @Override
    public void writeJson(Object obj, Writer out, boolean pretty) {
        getGson(pretty).toJson(obj, out);
        try {
            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public <T> T readJson(Reader reader, Class<T> cls) {
        return getGson(true).fromJson(reader, cls);
    }

    @Override
    public <T> T readJson(Path path, Class<T> cls) {
        File file = path.toFile();
        try (FileReader r = new FileReader(file)) {
            return readJson(r, cls);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public <T> T readJson(File file, Class<T> cls) {
        try (FileReader r = new FileReader(file)) {
            return readJson(r, cls);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
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
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public <T> void writeJson(Object obj, Path path, boolean pretty) {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        try (Writer w = Files.newBufferedWriter(path)) {
            writeJson(obj, w, pretty);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public <T> void writeJson(Object obj, PrintStream printStream, boolean pretty) {
        Writer w = new PrintWriter(printStream);
        writeJson(obj, w, pretty);
        try {
            w.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String expandPath(Path path) {
        return expandPath(path.toString());
    }

    @Override
    public String expandPath(String path) {
        return expandPath(path, ws.config().getWorkspaceLocation().toString());
    }

    @Override
    public String expandPath(String path, String baseFolder) {
        if (path != null && path.length() > 0) {
            path = CoreStringUtils.replaceDollarPlaceHolders(path, pathExpansionConverter);
            if (CoreIOUtils.isURL(path)) {
                return path;
            }
            Path ppath = path(path);
//            if (path.startsWith("file:") || path.startsWith("http://") || path.startsWith("https://")) {
//                return path;
//            }
            if (path.startsWith("~")) {
                if (path.equals("~~")) {
                    Path nutsHome = ws.config().getHomeLocation(NutsStoreLocation.CONFIG);
                    return nutsHome.normalize().toString();
                } else if (path.startsWith("~~") && path.length() > 2 && (path.charAt(2) == '/' || path.charAt(2) == '\\')) {
                    Path nutsHome = ws.config().getHomeLocation(NutsStoreLocation.CONFIG);
                    return nutsHome.resolve(path.substring(3)).normalize().toString();
                } else if (path.equals("~")) {
                    return (System.getProperty("user.home"));
                } else if (path.startsWith("~") && path.length() > 1 && (path.charAt(1) == '/' || path.charAt(1) == '\\')) {
                    return System.getProperty("user.home") + File.separator + path.substring(2);
                } else if (baseFolder != null) {
                    if (CoreIOUtils.isURL(baseFolder)) {
                        return baseFolder + "/" + path;
                    }
                    return path(baseFolder).resolve(path).toAbsolutePath().normalize().toString();
                } else {
                    if (CoreIOUtils.isURL(path)) {
                        return path;
                    }
                    return ppath.toAbsolutePath().normalize().toString();
                }
            } else if (ppath.isAbsolute()) {
                return ppath.normalize().toString();
            } else if (baseFolder != null) {
                if (CoreIOUtils.isURL(baseFolder)) {
                    return baseFolder + "/" + path;
                }
                return path(baseFolder).resolve(path).toAbsolutePath().normalize().toString();
            } else {
                return ppath.toAbsolutePath().normalize().toString();
            }
        }
        if (CoreIOUtils.isURL(baseFolder)) {
            return baseFolder;
        }
        return path(baseFolder).toAbsolutePath().normalize().toString();
    }

    @Override
    public String getResourceString(String resource, Class cls, String defaultValue) {
        String help = null;
        try {
            InputStream s = null;
            try {
                s = cls.getResourceAsStream(resource);
                if (s != null) {
                    help = CoreIOUtils.loadString(s, true);
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
        props.putAll(ws.config().getRuntimeProperties());
        help = CoreStringUtils.replaceDollarPlaceHolders(help, props);
        return help;
    }

    @Override
    public String computeHash(InputStream input) {
        return CoreIOUtils.evalSHA1(input, false);
    }

    @Override
    public InputStream nullInputStream() {
        return NullInputStream.INSTANCE;
    }

    @Override
    public PrintStream nullPrintStream() {
        return createPrintStream(NullOutputStream.INSTANCE, NutsTerminalMode.INHERITED);
    }

    @Override
    public PrintStream createPrintStream(Path out) {
        if (out == null) {
            return null;
        }
        try {
            return new PrintStream(Files.newOutputStream(out));
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
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
            if (ws.config().getOptions().getTerminalMode() == NutsTerminalMode.FILTERED) {
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
                return (PrintStream) ws.extensions().createSupported(NutsFormattedPrintStream.class,
                        Map.of("workspace", this, "out", out),
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
                return (PrintStream) ws.extensions().createSupported(NutsFormatFilteredPrintStream.class,
                        Map.of("workspace", this, "out", out),
                        new Class[]{OutputStream.class}, new Object[]{out});
            }
            case INHERITED: {
                if (out instanceof PrintStream) {
                    return (PrintStream) out;
                }
                return new PrintStream(out);
            }
        }
        throw new NutsUnsupportedArgumentException("Unsupported NutsTerminalMode " + mode);
    }

    @Override
    public NutsSessionTerminal createTerminal() {
        return createTerminal(null);
    }

    @Override
    public NutsSessionTerminal createTerminal(NutsTerminalBase parent) {
        if (parent == null) {
            parent = new AbstractSystemTerminalAdapter() {
                @Override
                public NutsSystemTerminalBase getParent() {
                    return ws.getSystemTerminal();
                }
            };
        }
        NutsSessionTerminalBase termb = ws.extensions().createSupported(NutsSessionTerminalBase.class, null);
        if (termb == null) {
            throw new NutsExtensionMissingException(NutsSessionTerminal.class, "Terminal");
        }
        try {
            NutsSessionTerminal term = null;
            if (termb instanceof NutsSessionTerminal) {
                term = (NutsSessionTerminal) termb;
                term.install(ws);
                term.setParent(parent);
            } else {
                term = new DefaultNutsSessionTerminal();
                term.install(ws);
                term.setParent(termb);
            }
            return term;
        } catch (Exception anyException) {
            final NutsSessionTerminal c = new DefaultNutsSessionTerminal();
            c.install(ws);
            c.setParent(parent);
            return c;
        }
    }

    @Override
    public Path createTempFile(String name) {
        return createTempFile(name, null);
    }

    @Override
    public Path createTempFolder(String name) {
        return createTempFolder(name, null);
    }

    @Override
    public Path createTempFolder(String name, NutsRepository repository) {
        File folder = null;
        if (repository == null) {
            folder = ws.config().getStoreLocation(NutsStoreLocation.TEMP).toFile();
        } else {
            folder = repository.config().getStoreLocation(NutsStoreLocation.TEMP).toFile();
        }
        final File temp;
        if (CoreStringUtils.isBlank(name)) {
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

        return (temp.toPath());
    }

    @Override
    public Path createTempFile(String name, NutsRepository repository) {
        File folder = null;
        if (repository == null) {
            folder = ws.config().getStoreLocation(NutsStoreLocation.TEMP).toFile();
        } else {
            folder = repository.config().getStoreLocation(NutsStoreLocation.TEMP).toFile();
        }
        folder.mkdirs();
        String prefix = "temp-";
        String ext = null;
        if (!CoreStringUtils.isBlank(name)) {
            ext = CoreIOUtils.getFileExtension(name);
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
            return File.createTempFile(prefix, "-nuts" + (ext != null ? ("." + ext) : ""), folder).toPath();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    @Override
    public String getSHA1(NutsDescriptor descriptor) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        ws.formatter().createDescriptorFormat().setPretty(false).print(descriptor, o);
        return CoreIOUtils.evalSHA1(new ByteArrayInputStream(o.toByteArray()), true);
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

    public NutsWorkspace getWorkspace() {
        return ws;
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

    @Override
    public NutsPathCopyAction copy() {
        return new DefaultNutsIOCopyAction(this);
    }

    @Override
    public Path path(String first, String... more) {
        if ((first == null || first.isEmpty()) && more.length == 0) {
            return null;
        }
        return Paths.get(first, more);
    }

}

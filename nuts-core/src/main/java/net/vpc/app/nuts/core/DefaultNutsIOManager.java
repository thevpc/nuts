package net.vpc.app.nuts.core;

import com.google.gson.*;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.NullOutputStream;
import net.vpc.app.nuts.core.util.*;
import net.vpc.common.io.*;
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
            return IOUtils.monitor(stream, null, (name == null ? "Stream" : name), length, new DefaultInputStreamMonitor(workspace, session.getTerminal().getOut()));
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
                if (CoreNutsUtils.FACE_PACKAGE_HASH.equals(d.getFace())) {
                    monitorable = false;
                }
                if (CoreNutsUtils.FACE_DESC_HASH.equals(d.getFace())) {
                    monitorable = false;
                }
            }
        }
        DefaultInputStreamMonitor monitor = null;
        if (monitorable && log.isLoggable(Level.INFO)) {
            monitor = new DefaultInputStreamMonitor(workspace, session.getTerminal().getOut());
        }
        InputStream stream = null;
        NutsURLHeader header = null;
        long size = -1;
        try {
            if (monitor != null) {
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
            if (monitor != null) {
                monitor.onComplete(new InputStreamEvent(source, sourceName, 0, 0, 0, 0, size, e));
            }
            throw new NutsIOException(e);
        }
        if (stream != null) {
            if (!path.toLowerCase().startsWith("file://")) {
                log.log(Level.FINE, "downloading file {0}", new Object[]{path});
            } else {
                log.log(Level.FINEST, "downloading url {0}", new Object[]{path});
            }
        } else {
            log.log(Level.FINEST, "downloading url failed : {0}", new Object[]{path});
        }

        if (!monitorable) {
            return stream;
        }
        if (monitor != null) {
            DefaultInputStreamMonitor finalMonitor = monitor;
            //adapt to disable onStart call (it is already invoked)
            return IOUtils.monitor(stream, source, sourceName, size, new InputStreamMonitor() {
                @Override
                public void onStart(InputStreamEvent event) {
                }

                @Override
                public void onComplete(InputStreamEvent event) {
                    finalMonitor.onComplete(event);
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
    public String resolvePath(String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }
        return FileUtils.getAbsolutePath(new File(workspace.getConfigManager().getCwd()), path);
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
            Logger.getLogger(Nuts.class.getName()).log(Level.SEVERE, "Unable to load main help", e);
        }
        if (help == null) {
            help = defaultValue;//"no help found";
        }
        HashMap<String, String> props = new HashMap<>((Map) System.getProperties());
        props.putAll(workspace.getConfigManager().getRuntimeProperties());
        help = CoreStringUtils.replaceVars(help, new MapStringMapper(props));
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
                //if nuts started with --no-colors modifier, will disable FORMATTED terminal mode
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
            parent = workspace.getSystemTerminal();
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

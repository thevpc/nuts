package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.terminals.DefaultNutsTerminal;
import net.vpc.app.nuts.extensions.terminals.NutsTerminalDelegate;
import net.vpc.app.nuts.extensions.util.*;
import net.vpc.common.io.FileUtils;
import net.vpc.common.io.IOUtils;
import net.vpc.common.io.URLUtils;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.MapBuilder;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultNutsIOManager implements NutsIOManager {
    private NutsWorkspace workspace;
    private JsonIO jsonIO;

    public DefaultNutsIOManager(NutsWorkspace workspace) {
        this.workspace = workspace;
        this.jsonIO = CoreJsonUtils.get();
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public InputStream monitorInputStream(String path, String name, NutsSession session) {
        InputStream stream = null;
        URLHeader header = null;
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
            return IOUtils.monitor(stream, null, (name == null ? "Stream" : name), length, new DefaultInputStreamMonitor(session.getTerminal().getOut()));
        } else {
            return stream;
        }
    }

    @Override
    public void writeJson(Object obj, Writer out, boolean pretty) {
        jsonIO.write(obj, out, pretty);
    }

    @Override
    public <T> T readJson(Reader reader, Class<T> cls) {
        return jsonIO.read(reader, cls);
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
            if(workspace.getConfigManager().getOptions().isNoColors()){
                //if nuts started with --no-colors modifier, will disable FORMATTED terminal mode
                mode=NutsTerminalMode.FILTERED;
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
                        MapBuilder.of("workspace", this, "out", out).build()
                        , new Class[]{OutputStream.class}, new Object[]{out});
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
                        MapBuilder.of("workspace", this, "out", out).build()
                        , new Class[]{OutputStream.class}, new Object[]{out});
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
    public NutsTerminal createDefaultTerminal(InputStream in, PrintStream out, PrintStream err) {
        DefaultNutsTerminal v = new DefaultNutsTerminal();
        v.install(workspace, in, out, err);
        return v;
    }

    @Override
    public NutsTerminal createTerminal() {
        return createTerminal(null, null, null);
    }

//    @Override
//    public NutsTerminal createTerminal(NutsTerminal delegated, InputStream in, PrintStream out, PrintStream err) {
//        NutsTerminalDelegate term = new NutsTerminalDelegate(delegated, in, out, err, false);
//        term.install(this, in, out, err);
//        return term;
//    }

    @Override
    public NutsTerminal createTerminal(InputStream in, PrintStream out, PrintStream err) {
        NutsTerminalBase termb = workspace.getExtensionManager().createSupported(NutsTerminalBase.class, null);
        if (termb == null) {
            throw new NutsExtensionMissingException(NutsTerminal.class, "Terminal");
        }
        try {
            NutsTerminal term = (termb instanceof NutsTerminal) ? (NutsTerminal) termb : new NutsTerminalDelegate(termb, true);
            term.install(workspace, in, out, err);
            return term;
        } catch (Exception anyException) {
            return createDefaultTerminal(in, out, err);
        }
    }

    @Override
    public void downloadPath(String from, File to, NutsSession session) {
        CoreIOUtils.downloadPath(from, to, null, workspace, session);
    }
}

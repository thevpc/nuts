/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.io;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author vpc
 */
public class DefaultNutsMonitorCommand implements NutsMonitorCommand {

    private static final Logger LOG = Logger.getLogger(DefaultNutsMonitorCommand.class.getName());
    private final NutsWorkspace ws;
    private String sourceType;
    private Object source;
    private Object sourceOrigin;
    private String sourceName;
    private long length = -1;
    private NutsSession session;

    public DefaultNutsMonitorCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsMonitorCommand session(NutsSession s) {
        return setSession(s);
    }

    @Override
    public NutsMonitorCommand setSession(NutsSession s) {
        this.session = s;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsMonitorCommand name(String s) {
        return setName(s);
    }

    @Override
    public NutsMonitorCommand setName(String s) {
        this.sourceName = s;
        return this;
    }

    @Override
    public String getName() {
        return sourceName;
    }

    @Override
    public NutsMonitorCommand origin(Object s) {
        return setOrigin(s);
    }

    @Override
    public NutsMonitorCommand setOrigin(Object s) {
        this.sourceOrigin = s;
        return this;
    }

    @Override
    public Object getOrigin() {
        return sourceOrigin;
    }

    @Override
    public NutsMonitorCommand length(long len) {
        return setLength(len);
    }

    @Override
    public NutsMonitorCommand setLength(long len) {
        this.length = len;
        return this;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public NutsMonitorCommand source(String path) {
        return setSource(path);
    }

    @Override
    public NutsMonitorCommand setSource(String path) {
        this.source = path;
        this.sourceType = "string";
        return this;
    }

    @Override
    public NutsMonitorCommand source(InputStream path) {
        return setSource(path);
    }

    @Override
    public NutsMonitorCommand setSource(InputStream path) {
        this.source = path;
        this.sourceType = "stream";
        return this;
    }

    @Override
    public InputStream create() {
        if (source == null || sourceType == null) {
            throw new NutsIllegalArgumentException(ws, "Missing Source");
        }
        switch (sourceType) {
            case "string": {
                return monitorInputStream((String) source, sourceOrigin, sourceName, session);
            }
            case "stream": {
                return monitorInputStream((InputStream) source, length, sourceName, session);
            }
            default:
                throw new NutsUnsupportedArgumentException(ws, sourceType);
        }
    }

    public InputStream monitorInputStream(String path, Object source, String sourceName, NutsSession session) {
        if (CoreStringUtils.isBlank(path)) {
            throw new UncheckedIOException(new IOException("Missing Path"));
        }
        if (CoreStringUtils.isBlank(sourceName)) {
            sourceName = String.valueOf(path);
        }
        boolean monitorable = true;
        if (session == null) {
            session = ws.createSession();
        }
        Object o = session.getProperty("monitor-allowed");
        if (o != null) {
            o = ws.commandLine().setArgs(new String[]{String.valueOf(o)}).next().getBoolean();
        }
        if (o instanceof Boolean) {
            monitorable = ((Boolean) o).booleanValue();
        }
        if (monitorable) {
            if (source instanceof NutsId) {
                NutsId d = (NutsId) source;
                if (NutsConstants.QueryFaces.COMPONENT_HASH.equals(d.getFace())) {
                    monitorable = false;
                }
                if (NutsConstants.QueryFaces.DESC_HASH.equals(d.getFace())) {
                    monitorable = false;
                }
            }
            if (monitorable) {
                if (path.endsWith("/.folders") || path.endsWith("/.files")
                        || path.endsWith(".pom") || path.endsWith(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION)
                        || path.endsWith(".xml") || path.endsWith(".json")) {
                    monitorable = false;
                }
            }
        }
        if (!CoreCommonUtils.getSysBoolNutsProperty("monitor.enabled", true)) {
            monitorable = false;
        }
        DefaultNutsInputStreamMonitor monitor = null;
        if (monitorable && LOG.isLoggable(Level.INFO)) {
            monitor = new DefaultNutsInputStreamMonitor(ws, session.getTerminal().out());
        }
        boolean verboseMode
                = CoreCommonUtils.getSysBoolNutsProperty("monitor.start", false)
                || ws.config().options().getLogConfig() != null && ws.config().options().getLogConfig().getLogLevel() == Level.FINEST;
        InputSource stream = null;
        long size = -1;
        try {
            if (verboseMode && monitor != null) {
                monitor.onStart(new InputStreamEvent(source, sourceName, 0, 0, 0, 0, size, null));
            }
            stream = CoreIOUtils.createInputSource(path);
            size = stream.length();
        } catch (UncheckedIOException e) {
            if (verboseMode && monitor != null) {
                monitor.onComplete(new InputStreamEvent(source, sourceName, 0, 0, 0, 0, size, e));
            }
            throw e;
        }
        if (stream != null) {
            if (path.toLowerCase().startsWith("file://")) {
                LOG.log(Level.FINE, "[START  ] Downloading file {0}", new Object[]{path});
            } else {
                LOG.log(Level.FINEST, "[START  ] Downloading url {0}", new Object[]{path});
            }
        } else {
            LOG.log(Level.FINEST, "[ERROR  ] Downloading url failed : {0}", new Object[]{path});
        }

        InputStream open = stream.open();
        if (!monitorable || monitor == null) {
            return open;
        }
        DefaultNutsInputStreamMonitor finalMonitor = monitor;
        if (!verboseMode) {
            monitor.onStart(new InputStreamEvent(source, sourceName, 0, 0, 0, 0, size, null));
        }
        return CoreIOUtils.monitor(open, source, sourceName, size, new InputStreamMonitor() {
            @Override
            public void onStart(InputStreamEvent event) {
            }

            @Override
            public void onComplete(InputStreamEvent event) {
                finalMonitor.onComplete(event);
                if (event.getException() != null) {
                    LOG.log(Level.FINEST, "[ERROR    ] Download Failed    : {0}", new Object[]{path});
                } else {
                    LOG.log(Level.FINEST, "[SUCCESS  ] Download Succeeded : {0}", new Object[]{path});
                }
            }

            @Override
            public boolean onProgress(InputStreamEvent event) {
                return finalMonitor.onProgress(event);
            }
        });

    }

    public InputStream monitorInputStream(InputStream stream, long length, String name, NutsSession session) {
        if (length > 0) {
            if (session == null) {
                session = ws.createSession();
            }
            return CoreIOUtils.monitor(stream, null, (name == null ? "Stream" : name), length, new DefaultNutsInputStreamMonitor(ws, session.getTerminal().out()));
        } else {
            if (stream instanceof InputStreamMetadataAware) {
                if (session == null) {
                    session = ws.createSession();
                }
                return CoreIOUtils.monitor(stream, null, new DefaultNutsInputStreamMonitor(ws, session.getTerminal().out()));
            } else {
                return stream;
            }
        }
    }

}

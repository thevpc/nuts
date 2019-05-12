/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.io;

import net.vpc.app.nuts.core.io.DefaultNutsInputStreamMonitor;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsDefaultArgument;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsHttpConnectionFacade;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsMonitorCommand;
import net.vpc.app.nuts.NutsTerminalProvider;
import net.vpc.app.nuts.NutsURLHeader;
import net.vpc.app.nuts.NutsUnsupportedArgumentException;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.io.InputStreamEvent;
import net.vpc.app.nuts.core.util.io.InputStreamMetadataAware;
import net.vpc.app.nuts.core.util.io.InputStreamMonitor;

/**
 *
 * @author vpc
 */
public class DefaultNutsMonitorCommand implements NutsMonitorCommand {

    private static final Logger LOG = Logger.getLogger(DefaultNutsMonitorCommand.class.getName());
    private String sourceType;
    private Object source;
    private Object sourceOrigin;
    private String sourceName;
    private long length = -1;
    private NutsTerminalProvider session;
    private final NutsWorkspace ws;

    public DefaultNutsMonitorCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsMonitorCommand setSession(NutsTerminalProvider s) {
        this.session=s;
        return this;
    }

    @Override
    public NutsTerminalProvider getSession() {
        return session;
    }

    @Override
    public NutsMonitorCommand setName(String s) {
        this.sourceName=s;
        return this;
    }

    @Override
    public String getName() {
        return sourceName;
    }

    @Override
    public NutsMonitorCommand setOrigin(Object s) {
        this.sourceOrigin=s;
        return this;
    }

    @Override
    public Object getOrigin() {
        return sourceOrigin;
    }

    @Override
    public NutsMonitorCommand setLength(long len) {
        this.length=len;
        return this;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public NutsMonitorCommand setSource(String path) {
        this.source=path;
        this.sourceType="string";
        return this;
    }

    @Override
    public NutsMonitorCommand setSource(InputStream path) {
        this.source=path;
        this.sourceType="stream";
        return this;
    }

    @Override
    public NutsMonitorCommand session(NutsTerminalProvider s) {
        return setSession(s);
    }

    @Override
    public NutsMonitorCommand name(String s) {
        return setName(s);
    }

    @Override
    public NutsMonitorCommand origin(Object s) {
        return setOrigin(s);
    }

    @Override
    public NutsMonitorCommand length(long len) {
        return setLength(len);
    }

    @Override
    public NutsMonitorCommand source(String path) {
        return setSource(path);
    }

    @Override
    public NutsMonitorCommand source(InputStream path) {
        return setSource(path);
    }
    
    

    @Override
    public InputStream create() {
        if (source == null || sourceType == null) {
            throw new NutsIllegalArgumentException("Missing Source");
        }
        switch (sourceType) {
            case "string": {
                return monitorInputStream((String) source, sourceOrigin, sourceName, session);
            }
            case "stream": {
                return monitorInputStream((InputStream) source, length, sourceName, session);
            }
            default:
                throw new NutsUnsupportedArgumentException(sourceType);
        }
    }

//    public InputStream monitorInputStream(String path, String name, NutsTerminalProvider session) {
//        InputStream stream = null;
//        NutsURLHeader header = null;
//        long size = -1;
//        try {
//            if (CoreIOUtils.isURL(path)) {
//                if (CoreIOUtils.isPathFile(path)) {
////                    path = URLUtils.toFile(new URL(path)).getPath();
//                    Path p = CoreIOUtils.toPathFile(path);
//                    size = Files.size(p);
//                    stream = Files.newInputStream(p);
//                } else {
//                    NutsHttpConnectionFacade f = CoreIOUtils.getHttpClientFacade(ws, path);
//                    try {
//
//                        header = f.getURLHeader();
//                        size = header.getContentLength();
//                    } catch (Exception ex) {
//                        //ignore error
//                    }
//                    stream = f.open();
//                }
//            } else {
//                Path p = ws.io().path(path);
//                //this is file!
//                size = Files.size(p);
//                stream = Files.newInputStream(p);
//            }
//        } catch (IOException e) {
//            throw new UncheckedIOException(e);
//        }
//        return monitorInputStream(stream, size, (name == null ? path : name), session);
//    }
    public InputStream monitorInputStream(String path, Object source, String sourceName, NutsTerminalProvider session) {
        if (CoreStringUtils.isBlank(sourceName)) {
            sourceName = String.valueOf(path);
        }
        boolean monitorable = true;
        Object o = session.getProperty("monitor-allowed");
        if (o != null) {
            o = new NutsDefaultArgument(String.valueOf(o)).getBoolean();
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
        if (monitorable && LOG.isLoggable(Level.INFO)) {
            monitor = new DefaultNutsInputStreamMonitor(ws, session.getTerminal().out());
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
                LOG.log(Level.FINE, "[START  ] Downloading file {0}", new Object[]{path});
            } else {
                LOG.log(Level.FINEST, "[START  ] Downloading url {0}", new Object[]{path});
            }
        } else {
            LOG.log(Level.FINEST, "[ERROR  ] Downloading url failed : {0}", new Object[]{path});
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
        return stream;

    }

    public InputStream monitorInputStream(InputStream stream, long length, String name, NutsTerminalProvider session) {
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

/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NBootOptions;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.log.NLogFactorySPI;
import net.thevpc.nuts.log.NLogSPI;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NMsg;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author thevpc
 */
public class DefaultNLogModel {
    private static Handler[] EMPTY = new Handler[0];
    private NWorkspace workspace;
    //    private NPrintStream out;
    private Handler consoleHandler;
    private Handler fileHandler;
    private NLogConfig logConfig = new NLogConfig();
    private List<Handler> extraHandlers = new ArrayList<>();
    private Path logFolder;
    private NLogFactorySPI defaultFactorySPI = new NLogFactorySPIJUL();
    private NLogFactorySPI factorySPI;
    private NLog nullLogger;
    final InheritableThreadLocal<NLog> scopedLoggerThreadLocal = new InheritableThreadLocal<>();

    public DefaultNLogModel(NWorkspace ws) {
        this.workspace = ws;
    }

    public void init(NBootOptions effOptions, NBootOptions userOptions) {
        this.logFolder = Paths.get(effOptions.getStoreType(NStoreType.LOG).get());
        NLogConfig lc = userOptions.getLogConfig().orNull();
        if (lc != null) {
            if (lc.getLogFileLevel() != null) {
                logConfig.setLogFileLevel(lc.getLogFileLevel());
            }
            if (lc.getLogTermLevel() != null) {
                logConfig.setLogTermLevel(lc.getLogTermLevel());
            }
            logConfig.setLogFileName(lc.getLogFileName());
            logConfig.setLogFileCount(lc.getLogFileCount());
            logConfig.setLogFileBase(lc.getLogFileBase());
            logConfig.setLogFileSize(lc.getLogFileSize());
        }
//        out = ((NWorkspaceExt) ws).getModel().bootModel.getSystemTerminal().err();
    }

    public NLogFactorySPI getFactorySPI() {
        return factorySPI == null ? defaultFactorySPI : factorySPI;
    }


    public DefaultNLogModel setFactorySPI(NLogFactorySPI factorySPI) {
        if (factorySPI == null) {
            factorySPI = defaultFactorySPI;
        }
        NLogFactorySPI oldFactorySPI = factorySPI;
        if (oldFactorySPI != this.factorySPI && !Objects.equals(this.factorySPI, oldFactorySPI)) {
            this.factorySPI = factorySPI;
            NLogFactorySPI f2 = getFactorySPI();
            for (NLog l : loaded().values()) {
                ((DefaultNLog) l).updateSPI(f2.getLogSPI(l.getName()));
            }
        }
        return this;
    }

    public List<Handler> getHandlers() {
        if (extraHandlers.isEmpty()) {
            return Collections.emptyList();
        }
        return extraHandlers;
    }


    public void removeHandler(Handler handler) {
        extraHandlers.remove(handler);
    }


    public void addHandler(Handler handler) {
        if (handler != null) {
            extraHandlers.add(handler);
        }
    }


    public Handler getTermHandler() {
        return consoleHandler;
    }


    public Handler getFileHandler() {
        return fileHandler;
    }

    private Map<String, NLog> loaded() {
        return NApp.of().getOrComputeProperty(NLog.class.getName() + "#Map", NScopeType.WORKSPACE, () -> new HashMap<String, NLog>());
    }

    public NLog getNullLogger() {
        if(nullLogger==null){
            nullLogger=new DefaultNLog("", new NLogSPI() {
                @Override
                public void log(NMsg message) {

                }
            },false,this, false);
        }
        return nullLogger;
    }

    public NLog getLogger(String name) {
        Map<String, NLog> loaded = loaded();
        NLog y = loaded.get(name);
        if (y == null) {
            y = new DefaultNLog(name, getFactorySPI().getLogSPI(name), false,this,false);
            loaded.put(name, y);
        }
        return y;
    }

    public NLog createCustomLogger(String name,NLogSPI spi) {
        return new DefaultNLog(name, spi, false,this,true);
    }

    public void runWith(NLog log, Runnable r) {
        NLog old = scopedLoggerThreadLocal.get();
        scopedLoggerThreadLocal.set(log);
        try {
            r.run();
        } finally {
            if (old == null) {
                scopedLoggerThreadLocal.remove();
            } else {
                scopedLoggerThreadLocal.set(old);
            }
        }
    }

    public <T> T callWith(NLog log, NCallable<T> r) {
        NLog old = scopedLoggerThreadLocal.get();
        scopedLoggerThreadLocal.set(log);
        try {
            return r.call();
        } finally {
            if (old == null) {
                scopedLoggerThreadLocal.remove();
            } else {
                scopedLoggerThreadLocal.set(old);
            }
        }
    }

    public Level getTermLevel() {
        return this.logConfig.getLogTermLevel();
    }


    public void setTermLevel(Level level) {
        if (level == null) {
            level = Level.INFO;
        }
        this.logConfig.setLogFileLevel(level);
        if (consoleHandler != null) {
            consoleHandler.setLevel(level);
        }
    }


    public Level getFileLevel() {
        return this.logConfig.getLogFileLevel();
    }


    public void setFileLevel(Level level) {
        if (level == null) {
            level = Level.INFO;
        }
        this.logConfig.setLogFileLevel(level);
//        session = CoreNutsUtils.validate(session, workspace);
        if (fileHandler != null) {
            fileHandler.setLevel(level);
        }

    }

    public void updateHandlers(LogRecord record) {
        updateTermHandler(record);
        updateFileHandler(record);
    }

    public void updateFileHandler(LogRecord record) {
        if (fileHandler == null) {
            if (logConfig.getLogFileLevel() != Level.OFF) {
                if (fileHandler == null) {
                    try {
                        fileHandler = NLogFileHandler.create(
                                logConfig, true, logFolder);
                        fileHandler.setLevel(logConfig.getLogFileLevel());
                    } catch (Exception ex) {
                        Logger.getLogger(DefaultNLogs.class.getName()).log(Level.FINE, "unable to create file handler", ex);
                    }
                }
            }
        }
    }

    public void updateTermHandler(LogRecord record) {
        NSession session = NLogUtils.resolveSession(record);
        NPrintStream out = session.err();
        if (consoleHandler != null) {
            if (consoleHandler instanceof NLogConsoleHandler) {
                NLogConsoleHandler ch = (NLogConsoleHandler) consoleHandler;
                if (out != ch.out()) {
                    ch.setOutputStream(out, false);
                }
            } else {
                consoleHandler.flush(); // do not close!!
            }
            consoleHandler.setLevel(logConfig.getLogTermLevel());
        } else {
            consoleHandler = new NLogConsoleHandler(out, false);
            consoleHandler.setLevel(logConfig.getLogTermLevel());
        }
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }

}

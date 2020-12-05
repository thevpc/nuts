package net.thevpc.nuts.runtime.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class DefaultNutsLogManager implements NutsLogManager {
    private NutsWorkspace ws;

    private PrintStream out=System.err;
    private Handler consoleHandler;
    private Handler fileHandler;
    private NutsLogConfig logConfig=new NutsLogConfig();
    private List<Handler> extraHandlers = new ArrayList<>();
    private static Handler[] EMPTY = new Handler[0];
    private Path logFolder;

    public DefaultNutsLogManager(NutsWorkspace ws, NutsWorkspaceInitInformation options) {
        this.ws = ws;
        logFolder= Paths.get(options.getStoreLocation(NutsStoreLocation.LOG));
        NutsLogConfig lc = options.getOptions().getLogConfig();
        if(lc!=null){
            if(lc.getLogFileLevel()!=null){
                logConfig.setLogFileLevel(lc.getLogFileLevel());
            }
            if(lc.getLogTermLevel()!=null){
                logConfig.setLogTermLevel(lc.getLogTermLevel());
            }
            logConfig.setLogFileName(lc.getLogFileName());
            logConfig.setLogFileCount(lc.getLogFileCount());
            logConfig.setLogFileBase(lc.getLogFileBase());
            logConfig.setLogFileSize(lc.getLogFileSize());
            logConfig.setLogInherited(lc.isLogInherited());
        }
    }

    @Override
    public Handler[] getHandlers() {
        if (extraHandlers.isEmpty()) {
            return EMPTY;
        }
        return extraHandlers.toArray(EMPTY);
    }

    @Override
    public void removeHandler(Handler handler) {
        extraHandlers.remove(handler);
    }

    @Override
    public void addHandler(Handler handler) {
        if (handler != null) {
            extraHandlers.add(handler);
        }
    }

    @Override
    public Handler getTermHandler() {
        return consoleHandler;
    }

    @Override
    public Handler getFileHandler() {
        return fileHandler;
    }

    @Override
    public NutsLogger of(String name) {
        return new DefaultNutsLogger(ws, name);
    }

    @Override
    public NutsLogger of(Class clazz) {
        return new DefaultNutsLogger(ws, clazz);
    }

    @Override
    public Level getTermLevel() {
        return this.logConfig.getLogTermLevel();
    }

    @Override
    public void setTermLevel(Level level, NutsUpdateOptions options) {
        if (level == null) {
            level = Level.INFO;
        }
        this.logConfig.setLogFileLevel(level);
        options = CoreNutsUtils.validate(options, ws);
        if (consoleHandler != null) {
            consoleHandler.setLevel(level);
        }
    }

    @Override
    public Level getFileLevel() {
        return this.logConfig.getLogFileLevel();
    }

    @Override
    public void setFileLevel(Level level, NutsUpdateOptions options) {
        if (level == null) {
            level = Level.INFO;
        }
        this.logConfig.setLogFileLevel(level);
        options = CoreNutsUtils.validate(options, ws);
        if (fileHandler != null) {
            fileHandler.setLevel(level);
        }

    }

    public void updateHandlers(LogRecord record) {
        updateTermHandler(record);
        updateFileHandler(record);
    }

    public void updateFileHandler(LogRecord record) {
        if(fileHandler==null){
            if(logConfig.getLogFileLevel()!=Level.OFF){
                if(fileHandler==null){
                    try {
                        fileHandler = NutsLogFileHandler.create(ws, logConfig, true,logFolder);
                        fileHandler.setLevel(logConfig.getLogFileLevel());
                    } catch (Exception ex) {
                        Logger.getLogger(DefaultNutsLogManager.class.getName()).log(Level.FINE, "Unable to create file handler", ex);
                    }
                }
            }
        }
    }

    public void updateTermHandler(LogRecord record) {
        PrintStream out=null;
        if (record instanceof NutsLogRecord) {
            NutsLogRecord rr = (NutsLogRecord) record;
            NutsSession session = rr.getSession();
            NutsWorkspace ws = rr.getWorkspace();
            if (session != null) {
                out = session.out();
            } else {
                NutsIOManager io = ws.io();
                if(io!=null){
                    NutsSessionTerminal term = io.term().getTerminal();
                    if(term!=null){
                        out = term.out();
                    }
                }
            }
        }
        if(out==null){
            out=System.err;
        }
        if(out!=this.out || consoleHandler==null){
            this.out=out;
            if(consoleHandler!=null){
                consoleHandler.close();
            }
            consoleHandler=new NutsLogConsoleHandler(out,true);
            consoleHandler.setLevel(logConfig.getLogTermLevel());
        }
    }
}

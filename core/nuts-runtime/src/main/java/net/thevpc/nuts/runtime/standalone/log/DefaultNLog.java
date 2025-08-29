package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.log.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NMsgBuilder;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.*;

public class DefaultNLog implements NLog {
    private NLogSPI logSPI;
    private static final int offValue = Level.OFF.intValue();
    private LinkedList<NMsg> suspendedTerminalRecords = new LinkedList<>();
    private int suspendedMax = 100;
    private boolean suspendTerminalMode = false;
    private String name;

    public DefaultNLog(String name, NLogSPI logSPI, boolean suspended) {
        this.name = name;
        this.logSPI = logSPI;
        if (suspended) {
            suspendTerminal();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public void updateSPI(NLogSPI logSPI) {
        this.logSPI = logSPI;
    }

    @Override
    public void log(NMsgBuilder msg) {
        if (msg != null) {
            log(msg.build());
        }
    }

    public Filter getFilter() {
        //may have a filter ?
        return null;
    }

    private boolean isLoggable(Level level, Level current) {
        int levelValue = current.intValue();
        if (!(level.intValue() < levelValue || levelValue == offValue)) {
            return true;
        }
        return false;
    }

    public boolean isLoggable(Level level) {
        DefaultNLogModel logModel = NWorkspaceExt.of().getModel().logModel;
        Level tlvl = logModel.getTermLevel();
        Level flvl = logModel.getFileLevel();
        List<Handler> handlers = logModel.getHandlers();
        if (isLoggable(level, tlvl)) {
            return true;
        }
        if (isLoggable(level, flvl)) {
            return true;
        }
        for (Handler handler : handlers) {
            if (isLoggable(level, handler.getLevel())) {
                return true;
            }
        }

        if (logSPI.isLoggable(level)) {
            return true;
        }
        return false;
    }

    public void log(NMsg msg) {
        if (msg == null) {
            return;
        }
        if (!isLoggable(msg.getLevel())) {
            return;
        }
        logSPI.log(msg);
    }

    @Override
    public void log(Level level, Supplier<NMsg> msgSupplier) {
        NAssert.requireNonNull(level, "level");
        if (!isLoggable(level)) {
            return;
        }
        NMsg msg = msgSupplier.get();
        if (msg == null) {
            msg = NMsg.ofC("").withLevel(level);
        } else {
            msg = msg.withLevel(level);
        }

        DefaultNLogModel logManager = NWorkspaceExt.of().getModel().logModel;
        //logManager.updateHandlers(record);
        if (suspendTerminalMode) {
            suspendedTerminalRecords.add(msg);
            if (suspendedTerminalRecords.size() > suspendedMax) {
                NMsg r = suspendedTerminalRecords.removeFirst();
                logSPI.log(r);
            }
        }
//        Handler ch = logManager.getTermHandler();
//        if (ch != null) {
//            if (ch.isLoggable(record)) {
//
//            }
//        }
//        Handler fh = logManager.getFileHandler();
//        if (fh != null) {
//            if (fh.isLoggable(record)) {
//                fh.publish(record);
//            }
//        }
//        for (Handler handler : logManager.getHandlers()) {
//            if (handler.isLoggable(record)) {
//                handler.publish(record);
//            }
//        }
//        if (record instanceof NLogRecord) {
//            NLogRecord ll = (NLogRecord) record;
//            NMsg ms = ll.getFormattedMessage();
//            this.logSPI.log(ms);
//        } else {
//            this.logSPI.log(NMsg.ofC("%s", record.getMessage()).withThrowable(record.getThrown()));
//        }
        logSPI.log(msg);
    }

    private boolean isLoggable(LogRecord record) {
        Filter theFilter = getFilter();
        if (theFilter != null && !theFilter.isLoggable(record)) {
            return false;
        }
        return true;
    }


    public void suspendTerminal() {
        suspendTerminalMode = true;
    }


    public void resumeTerminal() {
        suspendTerminalMode = false;
        for (Iterator<NMsg> iterator = suspendedTerminalRecords.iterator(); iterator.hasNext(); ) {
            NMsg r = iterator.next();
            iterator.remove();
            logSPI.log(r);
        }
    }
}

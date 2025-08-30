package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.log.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NCallable;
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
    private NLog scoped;
    private DefaultNLogModel model;
    private boolean custom;

    public DefaultNLog(String name, NLogSPI logSPI, boolean suspended, DefaultNLogModel model,boolean custom) {
        this.name = name;
        this.logSPI = logSPI;
        this.model = model;
        this.custom = custom;
        if (suspended) {
            suspendTerminal();
        }
    }

    public boolean isCustom() {
        return custom;
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
        logSPI.log(msg);
    }

    @Override
    public NLog scoped() {
        if (scoped == null) {
            scoped = new ScopedNLog(model, this);
        }
        return scoped;
    }

    @Override
    public void runWith(Runnable r) {
        model.runWith(this, r);
    }

    @Override
    public <T> T callWith(NCallable<T> r) {
        return model.callWith(this, r);
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

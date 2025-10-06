package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.log.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NMsgBuilder;

import java.util.function.Supplier;
import java.util.logging.*;

public class DefaultNLog implements NLog {
    private NLogSPI logSPI;
    private static final int offValue = Level.OFF.intValue();
    private String name;
    private NLog scoped;
    private DefaultNLogModel model;
    private boolean custom;

    public DefaultNLog(String name, NLogSPI logSPI, DefaultNLogModel model, boolean custom) {
        this.name = name;
        this.logSPI = logSPI;
        this.model = model;
        this.custom = custom;
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
        if (logModel.getTermHandler().isLoggable(level)) {
            return true;
        }
        if (logModel.getFileHandler() != null && logModel.getFileHandler().isLoggable(level)) {
            return true;
        }
        return logSPI.isLoggable(level);
    }

    public void log(NMsg msg) {
        if (msg == null) {
            return;
        }
        DefaultNLogModel logModel = NWorkspaceExt.of().getModel().logModel;

        if (logModel.getTermHandler().isLoggable(msg.getLevel())) {
            logModel.getTermHandler().log(msg);
        }
        if (logModel.getFileHandler() != null && logModel.getFileHandler().isLoggable(msg.getLevel())) {
            logModel.getFileHandler().log(msg);
        }
        if (logSPI.isLoggable(msg.getLevel())) {
            logSPI.log(prepareMsg(msg));
        }
    }

    @Override
    public void log(Level level, Supplier<NMsg> msgSupplier) {
        NAssert.requireNonNull(level, "level");
        DefaultNLogModel logModel = NWorkspaceExt.of().getModel().logModel;
        boolean term = logModel.getTermHandler().isLoggable(level);
        boolean file = logModel.getFileHandler() != null && logModel.getFileHandler().isLoggable(level);
        boolean other = logSPI.isLoggable(level);
        if (!term && !file && !other) {
            return;
        }

        NMsg msg = msgSupplier.get();
        if (msg == null) {
            msg = prepareMsg(NMsg.ofPlain("").withLevel(level));
        } else {
            msg = prepareMsg(msg.withLevel(level));
        }
        if (term) {
            logModel.getTermHandler().log(msg);
        }
        if (file) {
            logModel.getFileHandler().log(msg);
        }
        if (other) {
            logSPI.log(prepareMsg(msg));
        }
    }


    @Override
    public NLog scoped() {
        if (scoped == null) {
            scoped = new ScopedNLog(model, this);
        }
        return scoped;
    }

    private NMsg prepareMsg(NMsg other) {
        NLogContext c = NLogs.of().getContext();
        return other.withPrefix(c.getMessagePrefix()).withSuffix(c.getMessageSuffix()).withPlaceholders(c::getPlaceholder);
    }
}

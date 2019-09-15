package net.vpc.app.nuts.core.log;

import net.vpc.app.nuts.NutsLogOp;
import net.vpc.app.nuts.NutsLogger;

import java.util.function.Supplier;
import java.util.logging.Level;

public class DefaultNutsLogOp implements NutsLogOp {
    public static final Object[] OBJECTS0 = new Object[0];
    private DefaultNutsLogger logger;
    private Level level;
    private String verb;
    private String msg;
    private long time;
    private boolean formatted;
    private Supplier<String> msgSupplier;
    private Throwable error;
    private Object[] params= OBJECTS0;

    public DefaultNutsLogOp(DefaultNutsLogger logger,Level level) {
        this.logger = logger;
        this.level = level;
    }

    @Override
    public NutsLogOp formatted(boolean value) {
        this.formatted=value;
        return this;
    }

    @Override
    public NutsLogOp formatted() {
        this.formatted=true;
        return this;
    }

    @Override
    public NutsLogOp withVerb(String verb) {
        this.verb=verb;
        return this;
    }

    @Override
    public NutsLogOp withError(Throwable throwable) {
        this.error=throwable;
        return null;
    }

    @Override
    public void log(String msg, Object... params) {
        this.msg=msg;
        this.params=params;
        run();
    }

    @Override
    public void log(Supplier<String> msgSupplier) {
        this.msgSupplier=msgSupplier;
        run();
    }

    @Override
    public NutsLogOp withTime(long time) {
        this.time=time;
        return this;
    }

    private void run(){
        String m=msg;
        if(msgSupplier!=null){
            m=msgSupplier.get();
        }
        NutsLogRecord record = new NutsLogRecord(
                logger.getWorkspace(),
                logger.getSession(),
                level,
                verb,
                m,
                params,formatted,time
        );
        if(error!=null){
            record.setThrown(error);
        }
        logger.log(record);
    }
}

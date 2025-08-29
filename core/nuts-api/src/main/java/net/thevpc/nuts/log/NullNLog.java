package net.thevpc.nuts.log;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NMsgBuilder;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NullNLog implements NLog{
    public static final NLog NULL=new NullNLog();
    @Override
    public boolean isLoggable(Level level) {
        return false;
    }

    @Override
    public void log(NMsg msg) {

    }

    @Override
    public String getName() {
        return "null";
    }

    @Override
    public void log(NMsgBuilder msg) {

    }

    @Override
    public void log(Level level, Supplier<NMsg> msgSupplier) {

    }
}

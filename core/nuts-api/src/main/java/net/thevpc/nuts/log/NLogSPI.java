package net.thevpc.nuts.log;

import net.thevpc.nuts.util.NMsg;

import java.util.logging.Level;

public interface NLogSPI {
    String getName();

    boolean isLoggable(Level level);

    void log(NMsg message);
}

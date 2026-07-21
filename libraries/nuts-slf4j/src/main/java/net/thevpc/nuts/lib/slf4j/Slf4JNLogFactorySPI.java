package net.thevpc.nuts.lib.slf4j;

import net.thevpc.nuts.spi.NLogFactorySPI;
import net.thevpc.nuts.spi.NLogSPI;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

@NScore(fixed = NScorable.CUSTOM_SCORE)
public class Slf4JNLogFactorySPI implements NLogFactorySPI {
    @Override
    public NLogSPI getLogSPI(String name) {
        return new Slf4JNLogSPI(name);
    }

}

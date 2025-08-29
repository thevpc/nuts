package net.thevpc.nuts.lib.slf4j;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.log.NLogFactorySPI;
import net.thevpc.nuts.log.NLogSPI;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class Slf4JNLogFactorySPI implements NLogFactorySPI {
    @Override
    public NLogSPI getLogSPI(String name) {
        return new Slf4JNLogSPI(name);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.CUSTOM_SUPPORT;
    }
}

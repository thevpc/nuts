package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.spi.NScorable;
import net.thevpc.nuts.spi.NScorableContext;
import net.thevpc.nuts.spi.NUtilSPI;

public class NUtilSPIImpl implements NUtilSPI {
    @Override
    public <T extends NScorable> Query<T> ofScorableQuery() {
        return new NScorableQueryImpl<>(NScorableContext.of());
    }

    @Override
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }

}

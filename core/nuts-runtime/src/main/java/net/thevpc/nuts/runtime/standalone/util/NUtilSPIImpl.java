package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.NScorableQuery;

@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class NUtilSPIImpl implements NUtilSPI {
    @Override
    public <T extends NScorable> NScorableQuery<T> ofScorableQuery() {
        return new NScorableNScorableQueryImpl<>(NScorableContext.of());
    }

}

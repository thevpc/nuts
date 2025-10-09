package net.thevpc.nuts.spi;

import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableQuery;

public interface NUtilSPI extends NComponent{
    <T extends NScorable> NScorableQuery<T> ofScorableQuery();
}

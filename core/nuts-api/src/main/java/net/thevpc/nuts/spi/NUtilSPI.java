package net.thevpc.nuts.spi;

import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableQuery;

public interface NUtilSPI extends NComponent{
    <T extends NScorable> NScorableQuery<T> ofScorableQuery();
}

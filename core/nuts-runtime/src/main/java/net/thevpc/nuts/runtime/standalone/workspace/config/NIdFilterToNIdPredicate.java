package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.base.AbstractNPredicate;
import net.thevpc.nuts.runtime.standalone.id.filter.NSearchIdById;

class NIdFilterToNIdPredicate extends AbstractNPredicate<NId> {
    private final NIdFilter filter;
    private final NSession session;

    public NIdFilterToNIdPredicate(NIdFilter filter, NSession session) {
        this.filter = filter;
        this.session = session;
    }

    @Override
    public boolean test(NId t) {
        return filter.acceptSearchId(new NSearchIdById(t));
    }

    @Override
    public String toString() {
        return filter.toString();
    }}

package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.base.AbstractNPredicate;
import net.thevpc.nuts.runtime.standalone.id.filter.NSearchIdById;

class NIdFilterToNIdPredicate extends AbstractNPredicate<NId> {
    private final NIdFilter filter;

    public NIdFilterToNIdPredicate(NIdFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean test(NId t) {
        return filter.acceptSearchId(new NSearchIdById(t));
    }

    @Override
    public String toString() {
        return filter.toString();
    }}

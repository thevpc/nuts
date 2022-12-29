package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.filter.NSearchIdById;
import net.thevpc.nuts.util.NPredicates;

class NIdFilterToNIdPredicate extends NPredicates.BasePredicate<NId> {
    private final NIdFilter filter;
    private final NSession session;

    public NIdFilterToNIdPredicate(NIdFilter filter, NSession session) {
        this.filter = filter;
        this.session = session;
    }

    @Override
    public boolean test(NId t) {
        return filter.acceptSearchId(new NSearchIdById(t), session);
    }

    @Override
    public String toString() {
        return filter.toString();
    }}

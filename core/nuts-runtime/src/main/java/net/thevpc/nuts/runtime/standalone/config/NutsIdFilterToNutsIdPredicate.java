package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsIdFilter;
import net.thevpc.nuts.NutsPredicates;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.filters.NutsSearchIdById;

class NutsIdFilterToNutsIdPredicate extends NutsPredicates.BasePredicate<NutsId> {
    private final NutsIdFilter filter;
    private final NutsSession session;

    public NutsIdFilterToNutsIdPredicate(NutsIdFilter filter, NutsSession session) {
        this.filter = filter;
        this.session = session;
    }

    @Override
    public boolean test(NutsId t) {
        return filter.acceptSearchId(new NutsSearchIdById(t), session);
    }
}

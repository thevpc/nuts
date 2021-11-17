package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsSearchIdById;

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

    @Override
    public String toString() {
        return filter.toString();
    }}

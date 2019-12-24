package net.vpc.app.nuts.runtime.util.io;

import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsSearchId;
import net.vpc.app.nuts.NutsSession;

public class NutsIdFilterTopInstalled implements NutsIdFilter {
    private NutsIdFilter base;

    public NutsIdFilterTopInstalled(NutsIdFilter base) {
        this.base = base;
    }

    @Override
    public boolean accept(NutsId id, NutsSession session) {
        if(base==null){
            return true;
        }
        return base.accept(id,session);
    }

    @Override
    public boolean acceptSearchId(NutsSearchId sid, NutsSession session) {
        if(base==null){
            return true;
        }
        return base.acceptSearchId(sid,session);
    }
}

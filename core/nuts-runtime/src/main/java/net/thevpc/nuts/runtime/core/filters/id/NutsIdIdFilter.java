/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.filters.id;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.Simplifiable;
import net.thevpc.nuts.runtime.standalone.bridges.maven.MavenRepositoryFolderHelper;

import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

/**
 *
 * @author thevpc
 */
public class NutsIdIdFilter extends AbstractIdFilter implements NutsIdFilter, Simplifiable<NutsIdFilter> {

    private NutsLogger LOG;
    private final NutsId filter;
    private final boolean compat;

    public NutsIdIdFilter(NutsId filter, boolean compat,NutsSession session) {
        super(session, NutsFilterOp.CUSTOM);
        this.filter = filter;
        this.compat = compat;
    }

    @Override
    public boolean acceptSearchId(NutsSearchId sid, NutsSession session) {
        return filter == null ? true : acceptId(sid.getId(session),session);
    }

    @Override
    public boolean acceptId(NutsId id, NutsSession session) {
        if (filter == null) {
            return true;
        }
        if(LOG==null){
            LOG=session.log().of(NutsIdIdFilter.class);
        }
        if(id.getShortName().equals(filter.getShortName())){
            if(compat){
                if (!filter.getVersion().filterCompat().acceptVersion(id.getVersion(), session)) {
                    return false;
                }
            }else {
                if (!filter.getVersion().filter().acceptVersion(id.getVersion(), session)) {
                    return false;
                }
            }
            Map<String, String> e = filter.getProperties();
            Map<String, String> m = id.getProperties();
            for (Map.Entry<String, String> v : e.entrySet()) {
                if(!Objects.equals(
                        v.getValue(),m.get(v.getKey())
                )){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.filter);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NutsIdIdFilter other = (NutsIdIdFilter) obj;
        if (!Objects.equals(this.filter, other.filter)) {
            return false;
        }
        return true;
    }

    @Override
    public NutsIdFilter simplify() {
        if(filter==null){
            return null;
        }
        return this;
    }

    @Override
    public String toString() {
        return String.valueOf(filter);
    }

}

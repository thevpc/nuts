/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.base.AbstractIdFilter;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import java.util.Objects;

import net.thevpc.nuts.util.NSimplifiable;
import net.thevpc.nuts.util.NFilterOp;

/**
 *
 * @author thevpc
 */
public class NDefaultVersionIdFilter extends AbstractIdFilter implements NIdFilter, NSimplifiable<NIdFilter> {

    private final Boolean defaultVersion;

    public NDefaultVersionIdFilter(NSession session, Boolean defaultVersion) {
        super(session, NFilterOp.CUSTOM);
        this.defaultVersion = defaultVersion;
    }

    @Override
    public boolean acceptId(NId other, NSession session) {
        if (defaultVersion == null) {
            return true;
        }
        return NWorkspaceExt.of(session.getWorkspace()).getInstalledRepository().isDefaultVersion(other, session) == defaultVersion;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.defaultVersion);
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
        final NDefaultVersionIdFilter other = (NDefaultVersionIdFilter) obj;
        if (!Objects.equals(this.defaultVersion, other.defaultVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public NIdFilter simplify() {
        if (defaultVersion == null) {
            return null;
        }
        return this;
    }

    @Override
    public String toString() {
        return "defaultVersion(" + defaultVersion + ")";
    }

}

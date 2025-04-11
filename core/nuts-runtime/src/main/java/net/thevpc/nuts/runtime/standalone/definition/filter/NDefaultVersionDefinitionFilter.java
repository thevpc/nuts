/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.base.AbstractIdFilter;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NSimplifiable;

import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class NDefaultVersionDefinitionFilter extends AbstractDefinitionFilter implements NDefinitionFilter, NSimplifiable<NDefinitionFilter> {

    private final Boolean defaultVersion;

    public NDefaultVersionDefinitionFilter(Boolean defaultVersion) {
        super(NFilterOp.CUSTOM);
        this.defaultVersion = defaultVersion;
    }

    @Override
    public boolean acceptDefinition(NDefinition definition) {
        if (defaultVersion == null) {
            return true;
        }
        return NWorkspaceExt.of().getInstalledRepository().isDefaultVersion(definition.getId()) == defaultVersion;
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
        final NDefaultVersionDefinitionFilter other = (NDefaultVersionDefinitionFilter) obj;
        if (!Objects.equals(this.defaultVersion, other.defaultVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public NDefinitionFilter simplify() {
        if (defaultVersion == null) {
            return NDefinitionFilters.of().always();
        }
        return this;
    }

    @Override
    public String toString() {
        return "defaultVersion(" + defaultVersion + ")";
    }

}

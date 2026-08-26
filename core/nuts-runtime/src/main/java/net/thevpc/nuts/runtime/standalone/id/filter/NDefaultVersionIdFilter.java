/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdFilter;
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

    public NDefaultVersionIdFilter(Boolean defaultVersion) {
        super(NFilterOp.CUSTOM);
        this.defaultVersion = defaultVersion;
    }

    @Override
    public boolean acceptId(NId other) {
        if (defaultVersion == null) {
            return true;
        }
        return NWorkspaceExt.of().getInstalledRepository().isDefaultVersion(other) == defaultVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NDefaultVersionIdFilter that = (NDefaultVersionIdFilter) o;
        return Objects.equals(defaultVersion, that.defaultVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), defaultVersion);
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

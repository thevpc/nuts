/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.core.filters.descriptor;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.util.Objects;
import net.thevpc.nuts.runtime.core.filters.CoreFilterUtils;

/**
 * Created by vpc on 2/20/17.
 */
public class NutsDescriptorFilterArch extends AbstractDescriptorFilter implements JsNutsDescriptorFilter {

    private final String arch;

    public NutsDescriptorFilterArch(NutsSession ws, String packaging) {
        super(ws, NutsFilterOp.CUSTOM);
        this.arch = packaging;
    }

    public String getArch() {
        return arch;
    }

    @Override
    public boolean acceptDescriptor(NutsDescriptor descriptor, NutsSession session) {
        return CoreFilterUtils.matchesArch(arch, descriptor.getCondition(), session);
    }

    /**
     * @return null if nothing to check after
     */
    @Override
    public NutsDescriptorFilter simplify() {
        if (NutsUtilStrings.isBlank(arch)) {
            return null;
        }
        return this;
    }

    @Override
    public String toJsNutsDescriptorFilterExpr() {
        return "descriptor.matchesArch('" + CoreStringUtils.escapeQuoteStrings(arch) + "')";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.arch);
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
        final NutsDescriptorFilterArch other = (NutsDescriptorFilterArch) obj;
        if (!Objects.equals(this.arch, other.arch)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Arch{" + arch + '}';
    }

}

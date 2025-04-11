/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.NDefinition;
import net.thevpc.nuts.NDefinitionFilter;
import net.thevpc.nuts.NDefinitionFilters;
import net.thevpc.nuts.NVersionFilter;
import net.thevpc.nuts.runtime.standalone.version.filter.DefaultNVersionFilter;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NSimplifiable;

import java.util.Objects;

/**
 * @author thevpc
 */
public class NDefinitionFilterByVersion extends AbstractDefinitionFilter {

    private NVersionFilter versionFilter;

    public NDefinitionFilterByVersion(String versionFilter) {
        super(NFilterOp.CUSTOM);
        this.versionFilter = (NVersionFilter) DefaultNVersionFilter.parse(versionFilter).get().simplify();
    }

    @Override
    public boolean acceptDefinition(NDefinition definition) {
        if (versionFilter != null) {
            return versionFilter.acceptVersion(definition.getId().getVersion());
        }
        return true;
    }

    @Override
    public NDefinitionFilter simplify() {
        switch (versionFilter.getFilterOp()) {
            case TRUE:
                return NDefinitionFilters.of().always();
            case FALSE:
                return NDefinitionFilters.of().never();
        }
        return this;
    }

    @Override
    public String toString() {
        return "version=" + versionFilter;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.versionFilter);
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
        final NDefinitionFilterByVersion other = (NDefinitionFilterByVersion) obj;
        if (!Objects.equals(this.versionFilter, other.versionFilter)) {
            return false;
        }
        return true;
    }

}

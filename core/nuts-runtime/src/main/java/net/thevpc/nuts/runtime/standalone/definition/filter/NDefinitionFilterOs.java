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
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Objects;

/**
 * Created by vpc on 2/20/17.
 */
public class NDefinitionFilterOs extends AbstractDefinitionFilter {

    private final String os;

    public NDefinitionFilterOs(String os) {
        super(NFilterOp.CUSTOM);
        this.os = os;
    }

    public String getOs() {
        return os;
    }

    @Override
    public boolean acceptDefinition(NDefinition descriptor) {
        return CoreFilterUtils.matchesOs(os, descriptor.getDescriptor().getCondition().getOs());
    }

    /**
     * @return null if nothing to check after
     */
    @Override
    public NDefinitionFilter simplify() {
        if (NBlankable.isBlank(os)) {
            return null;
        }
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.os);
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
        final NDefinitionFilterOs other = (NDefinitionFilterOs) obj;
        return Objects.equals(this.os, other.os);
    }

    @Override
    public String toString() {
        return "Os{" + os + '}';
    }

}

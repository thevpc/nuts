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
public class NDefinitionFilterPackaging extends AbstractDefinitionFilter {

    private final String packaging;

    public NDefinitionFilterPackaging(String packaging) {
        super(NFilterOp.CUSTOM);
        this.packaging = packaging;
    }

    public String getPackaging() {
        return packaging;
    }

    @Override
    public boolean acceptDefinition(NDefinition descriptor) {
        return CoreFilterUtils.matchesPackaging(packaging, descriptor.getDescriptor());
    }

    /**
     * @return null if nothing to check after
     */
    @Override
    public NDefinitionFilter simplify() {
        if (NBlankable.isBlank(packaging)) {
            return null;
        }
        return this;
    }

    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.packaging);
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
        final NDefinitionFilterPackaging other = (NDefinitionFilterPackaging) obj;
        return Objects.equals(this.packaging, other.packaging);
    }

    @Override
    public String toString() {
        return "Packaging{" + packaging + '}';
    }

}

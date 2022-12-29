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
package net.thevpc.nuts.runtime.standalone.descriptor.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;

import java.util.Objects;

/**
 * Created by vpc on 2/20/17.
 */
public class NDescriptorFilterPackaging extends AbstractDescriptorFilter {

    private final String packaging;

    public NDescriptorFilterPackaging(NSession session, String packaging) {
        super(session, NFilterOp.CUSTOM);
        this.packaging = packaging;
    }

    public String getPackaging() {
        return packaging;
    }

    @Override
    public boolean acceptDescriptor(NDescriptor descriptor, NSession session) {
        return CoreFilterUtils.matchesPackaging(packaging, descriptor, session);
    }

    /**
     * @return null if nothing to check after
     */
    @Override
    public NDescriptorFilter simplify() {
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
        final NDescriptorFilterPackaging other = (NDescriptorFilterPackaging) obj;
        return Objects.equals(this.packaging, other.packaging);
    }

    @Override
    public String toString() {
        return "Packaging{" + packaging + '}';
    }

}

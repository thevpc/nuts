/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.descriptor.filter;

import net.thevpc.nuts.NutsDescriptorFilter;
import net.thevpc.nuts.NutsFilter;
import net.thevpc.nuts.NutsFilterOp;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.standalone.util.Simplifiable;

/**
 *
 * @author thevpc
 */
public abstract class AbstractDescriptorFilter extends AbstractNutsFilter implements NutsDescriptorFilter, Simplifiable<NutsDescriptorFilter> {

    public AbstractDescriptorFilter(NutsSession session, NutsFilterOp op) {
        super(session, op);
    }

    @Override
    public NutsDescriptorFilter or(NutsDescriptorFilter other) {
        return or((NutsFilter) other).to(NutsDescriptorFilter.class);
    }

    @Override
    public NutsDescriptorFilter and(NutsDescriptorFilter other) {
        return and((NutsFilter) other).to(NutsDescriptorFilter.class);
    }

    @Override
    public NutsDescriptorFilter neg() {
        return super.neg().to(NutsDescriptorFilter.class);
    }
}

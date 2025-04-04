/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.NDependencyFilter;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.spi.base.AbstractNFilter;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NSimplifiable;

/**
 *
 * @author thevpc
 */
public abstract class AbstractDependencyFilter extends AbstractNFilter implements NDependencyFilter, NSimplifiable<NDependencyFilter> {

    public AbstractDependencyFilter(NFilterOp op) {
        super(op);
    }

    @Override
    public NDependencyFilter or(NDependencyFilter other) {
        return other == null ? this : or((NFilter) other).to(NDependencyFilter.class);
    }

    @Override
    public NDependencyFilter and(NDependencyFilter other) {
        return other == null ? this : and((NFilter) other).to(NDependencyFilter.class);
    }

    @Override
    public NDependencyFilter neg() {
        return super.neg().to(NDependencyFilter.class);
    }

    @Override
    public NFilter withDesc(NEDesc description) {
        if (description == null) {
            return this;
        }
        return new DependencyFilterWithDescription(this, description);
    }
}

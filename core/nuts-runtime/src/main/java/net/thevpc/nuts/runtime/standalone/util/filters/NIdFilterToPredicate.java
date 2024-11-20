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
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.util.filters;

import java.util.Objects;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.NIdFilter;
import net.thevpc.nuts.spi.base.AbstractNPredicate;

/**
 *
 * @author thevpc
 */
public class NIdFilterToPredicate extends AbstractNPredicate<NId> {
    
    private final NIdFilter ff;

    public NIdFilterToPredicate(NIdFilter ff) {
        this.ff = ff;
    }

    @Override
    public boolean test(NId x) {
        return ff.acceptId(x);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NIdFilterToPredicate that = (NIdFilterToPredicate) o;
        return Objects.equals(ff, that.ff) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ff);
    }

    @Override
    public String toString() {
        return ff.toString();
    }
}

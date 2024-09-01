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
package net.thevpc.nuts.runtime.standalone.repository.filter;

import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.NRepository;
import net.thevpc.nuts.NRepositoryFilter;
import net.thevpc.nuts.NSession;

/**
 * Created by vpc on 2/20/17.
 */
public class NRepositoryFilterTrue extends AbstractRepositoryFilter{
    public NRepositoryFilterTrue(NSession session) {
        super(session, NFilterOp.TRUE);
    }

    @Override
    public boolean acceptRepository(NRepository value) {
        return true;
    }

    /**
     * @return null if nothing to check after
     */
    @Override
    public NRepositoryFilter simplify() {
        return this;
    }

    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
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
        final NRepositoryFilterTrue other = (NRepositoryFilterTrue) obj;
        return true;
    }

    @Override
    public String toString() {
        return "true";
    }

    @Override
    public NFilterOp getFilterOp() {
        return NFilterOp.TRUE;
    }
}

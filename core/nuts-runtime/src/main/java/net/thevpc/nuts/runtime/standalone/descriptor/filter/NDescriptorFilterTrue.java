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
package net.thevpc.nuts.runtime.standalone.descriptor.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NFilterOp;

/**
 * Created by vpc on 2/20/17.
 */
public class NDescriptorFilterTrue extends AbstractDescriptorFilter {

    public NDescriptorFilterTrue() {
        super(NFilterOp.TRUE);
    }

    @Override
    public boolean acceptDescriptor(NDescriptor descriptor) {
        return true;
    }

    /**
     * @return null if nothing to check after
     */
    @Override
    public NDescriptorFilter simplify() {
        return null;
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
        final NDescriptorFilterTrue other = (NDescriptorFilterTrue) obj;
        return true;
    }

    @Override
    public String toString() {
        return "true";
    }

}

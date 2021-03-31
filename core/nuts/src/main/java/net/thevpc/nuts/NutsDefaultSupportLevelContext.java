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
package net.thevpc.nuts;

import java.util.Objects;

/**
 * Default and dummy NutsSupportLevelContext implementation
 * @author thevpc
 * @param <T> support level type
 * @category SPI Base
 */
public class NutsDefaultSupportLevelContext<T> implements NutsSupportLevelContext<T> {

    private final NutsSession session;
    private final NutsWorkspace ws;
    private final T constraints;

    /**
     * default constructor
     * @param session session
     * @param constraints constraints
     */
    public NutsDefaultSupportLevelContext(NutsSession session,T constraints) {
        if(session==null){
            throw new NullPointerException();
        }
        this.session = session;
        this.ws = session.getWorkspace();
        this.constraints = constraints;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }
    
    @Override
    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public T getConstraints() {
        return constraints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsDefaultSupportLevelContext<?> that = (NutsDefaultSupportLevelContext<?>) o;
        return Objects.equals(ws, that.ws) &&
                Objects.equals(constraints, that.constraints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ws, constraints);
    }

    @Override
    public String toString() {
        return "NutsDefaultSupportLevelContext{" +
                "constraints=" + constraints +
                '}';
    }
}

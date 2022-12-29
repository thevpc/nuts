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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;

import java.util.Objects;

/**
 * Default and dummy NutsSupportLevelContext implementation
 *
 * @author thevpc
 * @app.category SPI Base
 */
public class NDefaultSupportLevelContext implements NSupportLevelContext {

    private final NSession session;
    private final NWorkspace ws;
    private final Object constraints;

    /**
     * default constructor
     *
     * @param session     session
     * @param constraints constraints
     */
    public NDefaultSupportLevelContext(NSession session, Object constraints) {
        if (session == null) {
            throw new NullPointerException();
        }
        this.session = session;
        this.ws = session.getWorkspace();
        this.constraints = constraints;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NWorkspace getWorkspace() {
        return ws;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getConstraints() {
        return (T) constraints;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getConstraints(Class<T> expected) {
        if (constraints == null) {
            return null;
        }
        if (expected.isInstance(constraints)) {
            return (T) constraints;
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ws, constraints);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NDefaultSupportLevelContext that = (NDefaultSupportLevelContext) o;
        return Objects.equals(ws, that.ws) &&
                Objects.equals(constraints, that.constraints);
    }

    @Override
    public String toString() {
        return "NutsDefaultSupportLevelContext{" +
                "constraints=" + constraints +
                '}';
    }
}

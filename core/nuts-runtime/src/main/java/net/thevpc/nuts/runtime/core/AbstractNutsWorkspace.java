/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
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
package net.thevpc.nuts.runtime.core;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootModel;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

/**
 * Created by vpc on 1/6/17.
 */
public abstract class AbstractNutsWorkspace implements NutsWorkspace {

    protected NutsSession initSession;
    protected DefaultNutsBootModel bootModel;

    public AbstractNutsWorkspace() {
    }

    public NutsSession defaultSession() {
        if (initSession != null) {
            return initSession;
        }
        return bootModel.bootSession();
    }

    @Override
    public NutsSession createSession() {
        NutsSession nutsSession = new DefaultNutsSession(this);
        nutsSession.setTerminal(nutsSession.term().createTerminal());
        nutsSession.setExpireTime(nutsSession.boot().getBootOptions().getExpireTime());
        return nutsSession;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsWorkspaceOptions> criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public String toString() {
        return "NutsWorkspace{"
                + getName()
                + '}';
    }

}

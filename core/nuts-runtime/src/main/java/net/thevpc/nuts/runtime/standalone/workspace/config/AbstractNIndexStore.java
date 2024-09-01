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
 *
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
package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.NIndexStore;
import net.thevpc.nuts.NRepository;

import java.util.*;

public abstract class AbstractNIndexStore implements NIndexStore {

    private NRepository repository;
    private boolean enabled = true;
    private Date inaccessibleDate = null;

    public AbstractNIndexStore(NRepository repository) {
        this.repository = repository;
    }

    protected void setInaccessible() {
        inaccessibleDate = new Date();
    }

    public boolean isInaccessible() {
        if (inaccessibleDate == null) {
            return false;
        }
        long elapsed = System.currentTimeMillis() - inaccessibleDate.getTime();
        if (elapsed > 1000 * 60 * 5) {
            inaccessibleDate = null;
            return false;
        }
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public NIndexStore setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    public NRepository getRepository() {
        return repository;
    }

    public void setRepository(NRepository repository) {
        this.repository = repository;
    }
}

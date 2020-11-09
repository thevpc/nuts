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
package net.thevpc.nuts.runtime.config;

import net.thevpc.nuts.NutsIndexStore;
import net.thevpc.nuts.NutsRepository;

import java.util.*;

public abstract class AbstractNutsIndexStore implements NutsIndexStore {

    private NutsRepository repository;
    private boolean enabled = true;
    private Date inaccessibleDate = null;

    public AbstractNutsIndexStore(NutsRepository repository) {
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
    public NutsIndexStore setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public NutsIndexStore enabled(boolean enabled) {
        return setEnabled(enabled);
    }

    @Override
    public NutsIndexStore enabled() {
        return enabled(true);
    }

    public NutsRepository getRepository() {
        return repository;
    }

    public void setRepository(NutsRepository repository) {
        this.repository = repository;
    }
}

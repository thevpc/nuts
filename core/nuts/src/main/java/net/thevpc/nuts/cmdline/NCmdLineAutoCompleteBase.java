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
 *
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
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.NWorkspace;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Base (Abstract) implementation of NutsCommandAutoComplete
 *
 * @author thevpc
 * @app.category Command Line
 * @since 0.5.5
 */
public abstract class NCmdLineAutoCompleteBase implements NCmdLineAutoComplete {

    /**
     * candidates map
     */
    private final LinkedHashMap<String, NArgCandidate> candidates = new LinkedHashMap<>();

    @Override
    public NWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

    @Override
    public <T> T get(Class<T> t) {
        return null;
    }

    /**
     * possible candidates
     *
     * @return possible candidates
     */
    @Override
    public List<NArgCandidate> getCandidates() {
        return new ArrayList<>(candidates.values());
    }

    /**
     * add candidate
     *
     * @param value candidate
     */
    @Override
    public void addCandidate(NArgCandidate value) {
        if (value != null && !value.getValue().trim().isEmpty()) {
            addCandidatesImpl(value);
        }
    }

    /**
     * simple add candidates implementation
     *
     * @param value candidate
     * @return {@code this} instance
     */
    protected NArgCandidate addCandidatesImpl(NArgCandidate value) {
        return candidates.put(value.getValue(), value);
    }

}

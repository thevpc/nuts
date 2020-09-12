/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Base (Abstract) implementation of NutsCommandAutoComplete
 * @author vpc
 * @since 0.5.5
 * @category Command Line
 */
public abstract class NutsCommandAutoCompleteBase implements NutsCommandAutoComplete {

    /**
     * candidates map
     */
    private final LinkedHashMap<String, NutsArgumentCandidate> candidates = new LinkedHashMap<>();

    /**
     * possible candidates
     * @return possible candidates
     */
    @Override
    public List<NutsArgumentCandidate> getCandidates() {
        return new ArrayList<>(candidates.values());
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

    @Override
    public <T> T get(Class<T> t) {
        return null;
    }

    /**
     * add candidate
     * @param value candidate
     */
    @Override
    public void addCandidate(NutsArgumentCandidate value) {
        if (value != null && !value.getValue().trim().isEmpty()) {
            addCandidatesImpl(value);
        }
    }

    /**
     * simple add candidates implementation
     * @param value candidate
     * @return {@code this} instance
     */
    protected NutsArgumentCandidate addCandidatesImpl(NutsArgumentCandidate value) {
        return candidates.put(value.getValue(), value);
    }

}

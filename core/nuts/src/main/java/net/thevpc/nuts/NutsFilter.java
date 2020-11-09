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
package net.thevpc.nuts;

/**
 * Top Level filter
 */
public interface NutsFilter {

    NutsFilterOp getFilterOp();

    default Class<? extends NutsFilter> getFilterType() {
        return getWorkspace().filters().detectType(this);
    }

    NutsWorkspace getWorkspace();

    NutsFilter simplify();

    default <T extends NutsFilter> NutsFilter simplify(Class<T> type) {
        return simplify().to(type);
    }

    default NutsFilter or(NutsFilter other) {
        return other == null ? this : getWorkspace().filters().any(this, other);
    }

    default NutsFilter and(NutsFilter other) {
        return other == null ? this : getWorkspace().filters().all(this, other);
    }

    default NutsFilter neg() {
        return getWorkspace().filters().not(this);
    }

    default <T extends NutsFilter> T to(Class<T> type) {
        return getWorkspace().filters().to(type, this);
    }

    NutsFilter[] getSubFilters();
}

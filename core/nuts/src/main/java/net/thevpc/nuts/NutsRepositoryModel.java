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

import java.nio.file.Path;
import java.util.Iterator;

/**
 * 
 * @author vpc
 * %category Extensions
 */
public interface NutsRepositoryModel {
    int MIRRORING = 1;
    int LIB_READ = 2;
    int LIB_WRITE = 4;
    int LIB_OVERRIDE = 8;
    int CACHE_READ = 16;
    int CACHE_WRITE = 32;

    int LIB = LIB_READ | LIB_WRITE | LIB_OVERRIDE;
    int CACHE = CACHE_READ | CACHE_WRITE;


    String getName();

    default NutsStoreLocationStrategy getStoreLocationStrategy() {
        return null;
    }

    default int getDeployOrder() {
        return 100;
    }

    default String getUuid() {
        return null;
    }

    default int getMode() {
        return MIRRORING | LIB | CACHE;
    }

    default int getSpeed() {
        return NutsRepository.SPEED_FAST;
    }

    default String getRepositoryType() {
        return "custom";
    }

    default NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) {
        Iterator<NutsId> allVersions = searchVersions(id, filter, fetchMode, repository, session);
        NutsId a = null;
        while (allVersions != null && allVersions.hasNext()) {
            NutsId next = allVersions.next();
            if (a == null || next.getVersion().compareTo(a.getVersion()) > 0) {
                a = next;
            }
        }
        return a;
    }

    default Iterator<NutsId> searchVersions(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) {
        return null;
    }

    default NutsDescriptor fetchDescriptor(NutsId id, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) {
        return null;
    }

    default NutsContent fetchContent(NutsId id, NutsDescriptor descriptor, Path localPath, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) {
        return null;
    }

    default Iterator<NutsId> search(NutsIdFilter filter, String[] roots, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) {
        return null;
    }

    default void updateStatistics(NutsRepository repository, NutsSession session) {

    }

    default boolean acceptFetch(NutsId id, NutsFetchMode mode, NutsRepository repository, NutsSession session) {
        return true;
    }

    default boolean acceptDeploy(NutsId id, NutsFetchMode mode, NutsRepository repository, NutsSession session) {
        return true;
    }
}

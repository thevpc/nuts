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
package net.thevpc.nuts.core;

import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NFetchMode;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.pipeline.NIterator;

import java.util.Iterator;

/**
 * @author thevpc
 * @app.category Extensions
 */
public interface NRepositoryModel extends NComponent {
    int MIRRORING = 1;
    int LIB_READ = 2;
    int LIB_WRITE = 4;
    int LIB_OVERRIDE = 8;
    int CACHE_READ = 16;
    int CACHE_WRITE = 32;

    int LIB = LIB_READ | LIB_WRITE | LIB_OVERRIDE;
    int CACHE = CACHE_READ | CACHE_WRITE;


    /**
     * Name.
     *
     * @return name result
     */
    @NGetter
    String name();

    /**
     * Store strategy.
     *
     * @return store strategy result
     */
    @NGetter
    default NStoreStrategy storeStrategy() {
        return null;
    }

    /**
     * Uuid.
     *
     * @return uuid result
     */
    @NGetter
    default String uuid() {
        return null;
    }

    /**
     * Mode.
     *
     * @return mode result
     */
    @NGetter
    default int mode() {
        return MIRRORING | LIB | CACHE;
    }

    /**
     * Speed.
     *
     * @return speed result
     */
    @NGetter
    default NSpeedQualifier speed() {
        return NSpeedQualifier.NORMAL;
    }

    /**
     * Repository type.
     *
     * @return repository type result
     */
    default String repositoryType() {
        return "custom";
    }

    /**
     * Finds the search latest version.
     *
     * @param id id
     * @param filter filter
     * @param fetchMode fetch mode
     * @param repository repository
     * @return search latest version result
     */
    default NId searchLatestVersion(NId id, NDefinitionFilter filter, NFetchMode fetchMode, NRepository repository) {
        Iterator<NId> allVersions = searchVersions(id, filter, fetchMode, repository);
        NId a = null;
        while (allVersions != null && allVersions.hasNext()) {
            NId next = allVersions.next();
            if (a == null || next.version().compareTo(a.version()) > 0) {
                a = next;
            }
        }
        return a;
    }

    /**
     * Finds the search versions.
     *
     * @param id id
     * @param idFilter id filter
     * @param fetchMode fetch mode
     * @param repository repository
     * @return search versions result
     */
    default NIterator<NId> searchVersions(NId id, NDefinitionFilter idFilter, NFetchMode fetchMode, NRepository repository) {
        return null;
    }

    /**
     * Fetch descriptor.
     *
     * @param id id
     * @param fetchMode fetch mode
     * @param repository repository
     * @return fetch descriptor result
     */
    default NDescriptor fetchDescriptor(NId id, NFetchMode fetchMode, NRepository repository) {
        return null;
    }

    /**
     * Fetch content.
     *
     * @param id id
     * @param descriptor descriptor
     * @param fetchMode fetch mode
     * @param repository repository
     * @return fetch content result
     */
    default NPath fetchContent(NId id, NDescriptor descriptor, NFetchMode fetchMode, NRepository repository) {
        return null;
    }

    /**
     * Finds the search.
     *
     * @param filter filter
     * @param basePaths base paths
     * @param fetchMode fetch mode
     * @param repository repository
     * @return search result
     */
    default NIterator<NId> search(NDefinitionFilter filter, NPath[] basePaths, NFetchMode fetchMode, NRepository repository) {
        return null;
    }

    /**
     * Update statistics.
     *
     * @param repository repository
     */
    default void updateStatistics(NRepository repository) {

    }

    /**
     * Checks if is accept fetch.
     *
     * @param id id
     * @param mode mode
     * @param repository repository
     * @return is accept fetch result
     */
    default boolean isAcceptFetch(NId id, NFetchMode mode, NRepository repository) {
        return true;
    }

    /**
     * Checks if is accept deploy.
     *
     * @param id id
     * @param mode mode
     * @param repository repository
     * @return is accept deploy result
     */
    default boolean isAcceptDeploy(NId id, NFetchMode mode, NRepository repository) {
        return true;
    }

    /**
     * Checks if is accept fetch mode.
     *
     * @param mode mode
     * @return is accept fetch mode result
     */
    default boolean isAcceptFetchMode(NFetchMode mode) {
        return true;
    }

    /**
     * Remote.
     *
     * @return remote result
     */
    @NGetter
    default boolean remote() {
        return true;
    }
}

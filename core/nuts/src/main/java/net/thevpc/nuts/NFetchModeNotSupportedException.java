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
package net.thevpc.nuts;

/**
 * Created by vpc on 1/15/17.
 *
 * @app.category Exceptions
 * @since 0.5.4
 */
public class NFetchModeNotSupportedException extends NException {

    private final String id;
    private final String repositoryName;
    private final String repositoryUuid;
    private final NFetchMode fetchMode;

    /**
     * Constructs a new NutsFetchModeNotSupportedException exception
     *
     * @param session   workspace
     * @param repo      repository
     * @param fetchMode fetch mode
     * @param id        artifact id
     * @param message   message
     * @param cause     cause
     */
    public NFetchModeNotSupportedException(NSession session, NRepository repo, NFetchMode fetchMode, String id, NMsg message, Exception cause) {
        super(session, message == null ? NMsg.ofC("unsupported fetch mode %s", fetchMode) : message, cause);
        this.id = id;
        this.repositoryName = repo == null ? null : repo.getName();
        this.repositoryUuid = repo == null ? null : repo.getUuid();
        this.fetchMode = fetchMode;
    }

    /**
     * Constructs a new NutsFetchModeNotSupportedException exception
     *
     * @param session   workspace
     * @param repo      repository
     * @param fetchMode fetch mode
     * @param id        artifact id
     * @param message   message
     */
    public NFetchModeNotSupportedException(NSession session, NRepository repo, NFetchMode fetchMode, String id, NMsg message) {
        super(session, message == null ? NMsg.ofC("unsupported fetch mode %s", fetchMode) : message);
        this.id = id;
        this.repositoryName = repo == null ? null : repo.getName();
        this.repositoryUuid = repo == null ? null : repo.getUuid();
        this.fetchMode = fetchMode;
    }

    /**
     * repository name
     *
     * @return repository name
     */
    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * repository uuid
     *
     * @return repository uuid
     */
    public String getRepositoryUuid() {
        return repositoryUuid;
    }

    /**
     * fetch mode
     *
     * @return fetch mode
     */
    public NFetchMode getFetchMode() {
        return fetchMode;
    }

    /**
     * artifact id
     *
     * @return artifact id
     */
    public String getId() {
        return id;
    }
}

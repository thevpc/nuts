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
package net.thevpc.nuts;

import net.thevpc.nuts.cmdline.NCommandLineConfigurable;

import java.nio.file.Path;
import java.util.Collection;

/**
 * @author thevpc
 * @app.category Config
 * @since 0.5.5
 */
public interface NUpdateStatisticsCommand extends NWorkspaceCommand {
    static NUpdateStatisticsCommand of(NSession session) {
        return NExtensions.of(session).createSupported(NUpdateStatisticsCommand.class);
    }

    NUpdateStatisticsCommand clearRepos();

    NUpdateStatisticsCommand addRepo(String s);

    NUpdateStatisticsCommand removeRepo(String s);

    NUpdateStatisticsCommand addRepos(String... all);

    NUpdateStatisticsCommand addRepos(Collection<String> all);

    NUpdateStatisticsCommand clearPaths();

    NUpdateStatisticsCommand addPath(Path s);

    NUpdateStatisticsCommand removePath(Path s);

    NUpdateStatisticsCommand addPaths(Path... all);

    NUpdateStatisticsCommand addPaths(Collection<Path> all);

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NUpdateStatisticsCommand setSession(NSession session);

    /**
     * copy session
     *
     * @return {@code this} instance
     */
    @Override
    NUpdateStatisticsCommand copySession();

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCommandLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NUpdateStatisticsCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NUpdateStatisticsCommand run();

    /**
     * add path if repoOrPath is a path (contains path separator or is '.' or '..')
     * if not add repo name or id
     *
     * @param repoOrPath repo uuid, name or path
     */
    void add(String repoOrPath);
}

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
package net.thevpc.nuts;

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;

import java.nio.file.Path;
import java.util.Collection;

/**
 * @author thevpc
 * @app.category Config
 * @since 0.5.5
 */
public interface NUpdateStatsCmd extends NWorkspaceCmd {
    static NUpdateStatsCmd of() {
        return NExtensions.of().createComponent(NUpdateStatsCmd.class).get();
    }

    NUpdateStatsCmd clearRepos();

    NUpdateStatsCmd addRepo(String s);

    NUpdateStatsCmd removeRepo(String s);

    NUpdateStatsCmd addRepos(String... all);

    NUpdateStatsCmd addRepos(Collection<String> all);

    NUpdateStatsCmd clearPaths();

    NUpdateStatsCmd addPath(Path s);

    NUpdateStatsCmd removePath(Path s);

    NUpdateStatsCmd addPaths(Path... all);

    NUpdateStatsCmd addPaths(Collection<Path> all);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NUpdateStatsCmd configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NUpdateStatsCmd run();

    /**
     * add path if repoOrPath is a path (contains path separator or is '.' or '..')
     * if not add repo name or id
     *
     * @param repoOrPath repo uuid, name or path
     */
    void add(String repoOrPath);
}
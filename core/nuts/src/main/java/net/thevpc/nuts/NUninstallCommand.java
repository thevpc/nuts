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

import java.util.Collection;
import java.util.List;

/**
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public interface NUninstallCommand extends NWorkspaceCommand {
    static NUninstallCommand of(NSession session) {
        return NExtensions.of(session).createSupported(NUninstallCommand.class);
    }

    NUninstallCommand addId(NId id);

    NUninstallCommand removeId(NId id);

    NUninstallCommand addId(String id);

    NUninstallCommand removeId(String id);

    NUninstallCommand addIds(NId... ids);

    NUninstallCommand addIds(String... ids);

    NUninstallCommand clearIds();

    List<NId> getIds();

    NUninstallCommand addArg(String arg);

    NUninstallCommand addArgs(Collection<String> args);

    NUninstallCommand addArgs(String... args);

    NUninstallCommand clearArgs();

    List<String> getArgs();

    boolean isErase();

    NUninstallCommand setErase(boolean erase);

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NUninstallCommand setSession(NSession session);

    /**
     * copy session
     *
     * @return {@code this} instance
     */
    @Override
    NUninstallCommand copySession();

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
    NUninstallCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NUninstallCommand run();

}
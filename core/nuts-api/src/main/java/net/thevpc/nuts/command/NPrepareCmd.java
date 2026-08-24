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
package net.thevpc.nuts.command;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NWorkspaceCmd;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.util.NSetter;

import java.util.List;

/**
 * @author thevpc
 * @app.category Config
 * @since 0.5.5
 */
public interface NPrepareCmd extends NWorkspaceCmd {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NPrepareCmd of() {
        return NExtensions.of(NPrepareCmd.class);
    }

    /**
     * Target server.
     *
     * @param remoteServer remote server
     * @return target server result
     */
    @NSetter
    NPrepareCmd targetServer(String remoteServer);

    /**
     * User name.
     *
     * @param remoteUser remote user
     * @return user name result
     */
    @NSetter
    NPrepareCmd userName(String remoteUser);

    /**
     * Version.
     *
     * @param version version
     * @return version result
     */
    @NSetter
    NPrepareCmd version(String version);

    /**
     * Ids.
     *
     * @param id id
     * @return ids result
     */
    @NSetter
    NPrepareCmd ids(List<NId> id);

    /**
     * Adds the specified ids.
     *
     * @param id id
     * @return add ids result
     */
    NPrepareCmd addIds(List<NId> id);
    
    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NPrepareCmd configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NPrepareCmd run();

}

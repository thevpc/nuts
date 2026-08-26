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
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.util.Collection;
import java.util.List;

/**
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public interface NUninstall extends NWorkspaceCmd {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NUninstall of() {
        return NExtensions.of(NUninstall.class);
    }

    /**
     * Adds the specified id.
     *
     * @param id id
     * @return add id result
     */
    NUninstall addId(NId id);

    /**
     * Removes the specified id.
     *
     * @param id id
     * @return remove id result
     */
    NUninstall removeId(NId id);

    /**
     * Adds the specified id.
     *
     * @param id id
     * @return add id result
     */
    NUninstall addId(String id);

    /**
     * Removes the specified id.
     *
     * @param id id
     * @return remove id result
     */
    NUninstall removeId(String id);

    /**
     * Adds the specified ids.
     *
     * @param ids ids
     * @return add ids result
     */
    NUninstall addIds(NId... ids);

    /**
     * Adds the specified ids.
     *
     * @param ids ids
     * @return add ids result
     */
    NUninstall addIds(String... ids);

    /**
     * Clear ids.
     *
     * @return clear ids result
     */
    NUninstall clearIds();

    /**
     * Ids.
     *
     * @return ids result
     */
    @NGetter
    List<NId> ids();

    /**
     * Adds the specified arg.
     *
     * @param arg arg
     * @return add arg result
     */
    NUninstall addArg(String arg);

    /**
     * Args.
     *
     * @param args args
     * @return args result
     */
    NUninstall args(List<String> args);

    /**
     * Adds the specified args.
     *
     * @param args args
     * @return add args result
     */
    NUninstall addArgs(Collection<String> args);

    /**
     * Adds the specified args.
     *
     * @param args args
     * @return add args result
     */
    NUninstall addArgs(String... args);

    /**
     * Clear args.
     *
     * @return clear args result
     */
    NUninstall clearArgs();

    /**
     * Args.
     *
     * @return args result
     */
    @NGetter
    List<String> args();

    /**
     * Checks if is erase.
     *
     * @return is erase result
     */
    boolean isErase();

    /**
     * Erase.
     *
     * @param erase erase
     * @return erase result
     */
    @NSetter
    NUninstall erase(boolean erase);


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
    NUninstall configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NUninstall run();

}

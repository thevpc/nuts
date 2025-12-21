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

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NWorkspaceCmd;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.util.NStream;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Command for installing artifacts
 *
 * @author thevpc
 * @app.category Base
 * @app.category Commands
 * @since 0.5.4
 */
public interface NInstall extends NWorkspaceCmd {
    static NInstall of() {
        return NExtensions.of(NInstall.class);
    }

    static NInstall of(String... ids) {
        return NExtensions.of(NInstall.class).addIds(ids);
    }

    static NInstall of(NId... ids) {
        return NExtensions.of(NInstall.class).addIds(ids);
    }

    /**
     * remove artifact id to install
     *
     * @param id id to install
     * @return {@code this} instance
     */
    NInstall removeId(NId id);

    /**
     * remove artifact id to install
     *
     * @param id id to install
     * @return {@code this} instance
     */
    NInstall removeId(String id);

    /**
     * add artifact id to install
     *
     * @param id id to install
     * @return {@code this} instance
     */
    NInstall addId(NId id);

    /**
     * add artifact id to install
     *
     * @param id id to install
     * @return {@code this} instance
     */
    NInstall addId(String id);

    /**
     * add artifact ids to install
     *
     * @param ids ids to install
     * @return {@code this} instance
     */
    NInstall addIds(NId... ids);

    /**
     * add artifact ids to install
     *
     * @param ids ids to install
     * @return {@code this} instance
     */
    NInstall addIds(String... ids);


    /**
     * add artifact id to install
     *
     * @param id id to install
     * @return {@code this} instance
     */
    NInstall setId(NId id);

    /**
     * add artifact id to install
     *
     * @param id id to install
     * @return {@code this} instance
     */
    NInstall setId(String id);

    /**
     * clear ids to install
     *
     * @return {@code this} instance
     */
    NInstall clearIds();

    /**
     * return all ids to install
     *
     * @return all ids to install
     */
    List<NId> getIds();

    /**
     * add artifact ids to install
     *
     * @param ids ids to install
     * @return {@code this} instance
     */
    NInstall setIds(NId... ids);

    /**
     * add artifact ids to install
     *
     * @param ids ids to install
     * @return {@code this} instance
     */
    NInstall setIds(String... ids);

    /**
     * add argument to pass to the install command
     *
     * @param arg argument
     * @return {@code this} instance
     */
    NInstall addArg(String arg);

    NInstall addConditionalArgs(Predicate<NDefinition> definition, String... args);

    /**
     * add arguments to pass to the install command
     *
     * @param args argument
     * @return {@code this} instance
     */
    NInstall addArgs(Collection<String> args);

    /**
     * add arguments to pass to the install command
     *
     * @param args argument
     * @return {@code this} instance
     */
    NInstall addArgs(String... args);

    /**
     * clear all arguments to pass to the install command
     *
     * @return {@code this} instance
     */
    NInstall clearArgs();

    /**
     * return all arguments to pass to the install command
     *
     * @return all arguments to pass to the install command
     */
    List<String> getArgs();

    /**
     * set default version flag. the installed version will be defined as default.
     *
     * @return {@code this} instance
     */
    NInstall defaultVersion();

    /**
     * set default version flag. when true, the installed version will be defined as default
     *
     * @param defaultVersion when true, the installed version will be defined as
     *                       default
     * @return {@code this} instance
     */
    NInstall defaultVersion(boolean defaultVersion);

    /**
     * return true if the installer will update the default version
     *
     * @return true if the installer will update the default version
     */
    boolean isDefaultVersion();

    /**
     * set default version flag. when true, the installed version will be defined as default
     *
     * @param defaultVersion when true, the installed version will be defined as
     *                       default
     * @return {@code this} instance
     */
    NInstall setDefaultVersion(boolean defaultVersion);

    /**
     * return true companions should be installed as well
     *
     * @return return true companions should be installed as well
     */
    boolean isCompanions();

    /**
     * update companions
     *
     * @return return {@code this} instance
     */
    NInstall companions();

    /**
     * if true update companions
     *
     * @param value flag
     * @return return {@code this} instance
     */
    NInstall companions(boolean value);

    /**
     * if true update companions
     *
     * @param value flag
     * @return return {@code this} instance
     */
    NInstall setCompanions(boolean value);

    /**
     * return true installed artifacts should be re-installed as well
     *
     * @return true installed artifacts should be re-installed as well
     */
    boolean isInstalled();

    /**
     * if true reinstall installed artifacts
     *
     * @param value flag
     * @return {@code this} instance
     */
    NInstall setInstalled(boolean value);

    /**
     * execute installation and return result.
     *
     * @return installation result
     */
    NStream<NDefinition> getResult();

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
    NInstall configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NInstall run();
}

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
import net.thevpc.nuts.util.NStream;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Command for installing artifacts
 *
 * @author thevpc
 * @app.category Base
 * @app.category Commands
 * @since 0.5.4
 */
public interface NInstallCmd extends NWorkspaceCmd {
    static NInstallCmd of() {
        return NExtensions.of(NInstallCmd.class);
    }

    /**
     * remove artifact id to install
     *
     * @param id id to install
     * @return {@code this} instance
     */
    NInstallCmd removeId(NId id);

    /**
     * remove artifact id to install
     *
     * @param id id to install
     * @return {@code this} instance
     */
    NInstallCmd removeId(String id);

    /**
     * add artifact id to install
     *
     * @param id id to install
     * @return {@code this} instance
     */
    NInstallCmd addId(NId id);

    /**
     * add artifact id to install
     *
     * @param id id to install
     * @return {@code this} instance
     */
    NInstallCmd addId(String id);

    /**
     * add artifact ids to install
     *
     * @param ids ids to install
     * @return {@code this} instance
     */
    NInstallCmd addIds(NId... ids);

    /**
     * add artifact ids to install
     *
     * @param ids ids to install
     * @return {@code this} instance
     */
    NInstallCmd addIds(String... ids);


    /**
     * add artifact id to install
     *
     * @param id id to install
     * @return {@code this} instance
     */
    NInstallCmd setId(NId id);

    /**
     * add artifact id to install
     *
     * @param id id to install
     * @return {@code this} instance
     */
    NInstallCmd setId(String id);

    /**
     * clear ids to install
     *
     * @return {@code this} instance
     */
    NInstallCmd clearIds();

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
    NInstallCmd setIds(NId... ids);

    /**
     * add artifact ids to install
     *
     * @param ids ids to install
     * @return {@code this} instance
     */
    NInstallCmd setIds(String... ids);

    /**
     * add argument to pass to the install command
     *
     * @param arg argument
     * @return {@code this} instance
     */
    NInstallCmd addArg(String arg);

    NInstallCmd addConditionalArgs(Predicate<NDefinition> definition, String... args);

    /**
     * add arguments to pass to the install command
     *
     * @param args argument
     * @return {@code this} instance
     */
    NInstallCmd addArgs(Collection<String> args);

    /**
     * add arguments to pass to the install command
     *
     * @param args argument
     * @return {@code this} instance
     */
    NInstallCmd addArgs(String... args);

    /**
     * clear all arguments to pass to the install command
     *
     * @return {@code this} instance
     */
    NInstallCmd clearArgs();

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
    NInstallCmd defaultVersion();

    /**
     * set default version flag. when true, the installed version will be defined as default
     *
     * @param defaultVersion when true, the installed version will be defined as
     *                       default
     * @return {@code this} instance
     */
    NInstallCmd defaultVersion(boolean defaultVersion);

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
    NInstallCmd setDefaultVersion(boolean defaultVersion);

    Map<NId, NInstallStrategy> getIdMap();

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
    NInstallCmd companions();

    /**
     * if true update companions
     *
     * @param value flag
     * @return return {@code this} instance
     */
    NInstallCmd companions(boolean value);

    NInstallStrategy getCompanions();

    /**
     * if true update companions
     *
     * @param value flag
     * @return return {@code this} instance
     */
    NInstallCmd setCompanions(boolean value);

    /**
     * return true installed artifacts should be re-installed as well
     *
     * @return true installed artifacts should be re-installed as well
     */
    boolean isInstalled();

    NInstallStrategy getInstalled();

    /**
     * if true reinstall installed artifacts
     *
     * @param value flag
     * @return {@code this} instance
     */
    NInstallCmd setInstalled(boolean value);

    NInstallStrategy getStrategy();

    NInstallCmd setStrategy(NInstallStrategy value);

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
    NInstallCmd configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NInstallCmd run();
}

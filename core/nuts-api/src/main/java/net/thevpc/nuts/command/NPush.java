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

import java.util.Collection;
import java.util.List;

/**
 * Push command
 *
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public interface NPush extends NWorkspaceCmd {

    static NPush of() {
        return NExtensions.of(NPush.class);
    }
    /**
     * remove id to push.
     *
     * @param id id to push
     * @return {@code this} instance
     */
    NPush removeId(NId id);

    /**
     * add id to push.
     *
     * @param id id to push
     * @return {@code this} instance
     */
    NPush addId(NId id);

    /**
     * remove id to push.
     *
     * @param id id to push
     * @return {@code this} instance
     */
    NPush removeId(String id);

    /**
     * add id to push.
     *
     * @param id id to push
     * @return {@code this} instance
     */
    NPush addId(String id);

    /**
     * add ids to push.
     *
     * @param ids id to push
     * @return {@code this} instance
     */
    NPush addIds(NId... ids);

    /**
     * add ids to push.
     *
     * @param ids id to push
     * @return {@code this} instance
     */
    NPush addIds(String... ids);

    /**
     * reset ids to push for
     *
     * @return {@code this} instance
     */
    NPush clearIds();

    /**
     * return ids to push for
     *
     * @return ids to push for
     */
    List<NId> getIds();

    /**
     * remove locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     *
     * @param id id to unlock
     * @return {@code this} instance
     */
    NPush removeLockedId(NId id);

    /**
     * add locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     *
     * @param id id to lock
     * @return {@code this} instance
     */
    NPush addLockedId(NId id);

    /**
     * remove locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     *
     * @param id id to unlock
     * @return {@code this} instance
     */
    NPush removeLockedId(String id);

    /**
     * add locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     *
     * @param id id to lock
     * @return {@code this} instance
     */
    NPush addLockedId(String id);

    /**
     * add locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     *
     * @param values id to lock
     * @return {@code this} instance
     */
    NPush addLockedIds(NId... values);

    /**
     * define locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     *
     * @param values ids
     * @return {@code this} instance
     */
    NPush addLockedIds(String... values);

    /**
     * reset locked ids
     *
     * @return {@code this} instance
     */
    NPush clearLockedIds();

    /**
     * return locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     *
     * @return locked ids
     */
    List<NId> getLockedIds();

    /**
     * add argument to pass to the push command
     *
     * @param arg argument
     * @return {@code this} instance
     */
    NPush addArg(String arg);

    /**
     * add arguments to pass to the push command
     *
     * @param args argument
     * @return {@code this} instance
     */
    NPush addArgs(String... args);

    /**
     * add arguments to pass to the push command
     *
     * @param args argument
     * @return {@code this} instance
     */
    NPush args(Collection<String> args);

    /**
     * add arguments to pass to the push command
     *
     * @param args argument
     * @return {@code this} instance
     */
    NPush addArgs(Collection<String> args);

    /**
     * clear all arguments to pass to the push command
     *
     * @return {@code this} instance
     */
    NPush clearArgs();

    /**
     * return all arguments to pass to the push command
     *
     * @return all arguments to pass to the push command
     */
    List<String> getArgs();

    /**
     * true when offline mode
     *
     * @return true when offline mode
     */
    boolean isOffline();

    /**
     * local only (installed or not)
     *
     * @param offline enable offline mode
     * @return {@code this} instance
     */
    NPush setOffline(boolean offline);

    /**
     * repository to push from
     *
     * @return repository to push from
     */
    String getRepository();

    /**
     * repository to push from
     *
     * @param repository repository to push from
     * @return {@code this} instance
     */
    NPush setRepository(String repository);

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
    NPush configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NPush run();

}

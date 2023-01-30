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

import net.thevpc.nuts.spi.NComponent;

import java.util.List;

/**
 * Manage (add/update/remove) custom nuts commands (aliases)
 *
 * @author thevpc
 * @app.category Config
 */
public interface NCustomCommandManager extends NComponent,NSessionProvider {
    static NCustomCommandManager of(NSession session) {
        return NExtensions.of(session).createSupported(NCustomCommandManager.class);
    }

    /**
     * return registered command factories
     *
     * @return registered command factories
     */
    List<NCommandFactoryConfig> getCommandFactories();

    /**
     * register a new commandFactory. If it already exists, a NutsIllegalArgumentException is thrown
     *
     * @param commandFactory commandFactory
     */
    void addCommandFactory(NCommandFactoryConfig commandFactory);

    /**
     * unregister an existing commandFactory. If it is not found, a NutsIllegalArgumentException is thrown
     *
     * @param commandFactoryId commandFactoryId
     */
    void removeCommandFactory(String commandFactoryId);

    /**
     * unregister an existing commandFactory if it exists.
     *
     * @param commandFactoryId commandFactoryId
     * @return true if removed
     */
    boolean removeCommandFactoryIfExists(String commandFactoryId);

    /**
     * return true if the command is registered or provided by a registered command factory
     *
     * @param command command name
     * @return true if the command is registered or provided by a registered command factory
     */
    boolean commandExists(String command);

    /**
     * return true if the command factory is registered
     *
     * @param command command name
     * @return true if the command factory is registered
     */
    boolean commandFactoryExists(String command);

    /**
     * add command. if the command is already registered (regardless off being defined by command factories) a confirmation is required to update it.
     *
     * @param command command
     * @return true if successfully added
     */
    boolean addCommand(NCommandConfig command);

    /**
     * update command. if the command does not exists (not registered, regardless off being defined by command factories) a NutsIllegalArgumentException is thrown.
     *
     * @param command command
     * @return true if successfully updated
     */
    boolean updateCommand(NCommandConfig command);

    /**
     * remove command. if the command does not exists a NutsIllegalArgumentException is thrown.
     *
     * @param command command name
     */
    void removeCommand(String command);

    /**
     * return true if exists and is removed
     *
     * @param name name
     * @return true if exists and is removed
     */
    boolean removeCommandIfExists(String name);

    /**
     * return the first command for a given name, id and owner.
     * Search is first performed in the registered commands then in each registered command factory.
     *
     * @param name     command name, not null
     * @param forId    if not null, the alias name should resolve to the given id
     * @param forOwner if not null, the alias name should resolve to the owner
     * @return alias definition or null
     */
    NWorkspaceCustomCommand findCommand(String name, NId forId, NId forOwner);

    /**
     * return the first command for a given name, id and owner.
     * Search is first performed in the registered commands then in each registered command factory.
     *
     * @param name command name, not null
     * @return alias definition or null
     */
    NWorkspaceCustomCommand findCommand(String name);

    /**
     * find all registered and factory defined commands
     *
     * @return find all registered and factory defined commands
     */
    List<NWorkspaceCustomCommand> findAllCommands();

    /**
     * find all registered and factory defined commands by owner
     *
     * @param id owner
     * @return all registered and factory defined commands by owner
     */
    List<NWorkspaceCustomCommand> findCommandsByOwner(NId id);

    /**
     * update current session
     *
     * @param session current session
     * @return {@code this} instance
     */
    NCustomCommandManager setSession(NSession session);

}

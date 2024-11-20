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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NSession;

import java.util.List;

/**
 * Push Command
 *
 * @author thevpc
 * @app.category SPI Base
 */
public interface NPushRepositoryCmd extends NRepositoryCmd {

    /**
     * return id to push.
     *
     * @return id to push
     */
    NId getId();

    /**
     * set id to push.
     *
     * @param id id to push
     * @return {@code this} instance
     */
    NPushRepositoryCmd setId(NId id);

    /**
     * run this command and return {@code this} instance
     *
     * @return {@code this} instance
     */
    @Override
    NPushRepositoryCmd run();

    /**
     * return all arguments to pass to the push command
     *
     * @return all arguments to pass to the push command
     */
    List<String> getArgs();

    /**
     * args args to push
     *
     * @param args args to push
     * @return {@code this} instance
     */
    NPushRepositoryCmd setArgs(String[] args);
    NPushRepositoryCmd setArgs(List<String> args);

    /**
     * true if offline mode is activated
     *
     * @return true if offline mode is activated
     */
    boolean isOffline();

    /**
     * local only (installed or not)
     *
     * @param offline enable offline mode
     * @return {@code this} instance
     */
    NPushRepositoryCmd setOffline(boolean offline);

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
    NPushRepositoryCmd setRepository(String repository);
}

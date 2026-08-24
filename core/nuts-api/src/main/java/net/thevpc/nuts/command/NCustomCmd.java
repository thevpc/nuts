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
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NGetter;

import java.util.List;

/**
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public interface NCustomCmd {

    /**
     * Factory id.
     *
     * @return factory id result
     */
    @NGetter
    String factoryId();

    /**
     * Owner.
     *
     * @return owner result
     */
    @NGetter
    NId owner();

    /**
     * Name.
     *
     * @return name result
     */
    @NGetter
    String name();

    /**
     * Exec.
     *
     * @param args args
     * @param options options
     * @return exec result
     * @throws NExecutionException if execution fails
     */
    int exec(String[] args, NCmdExecOptions options) throws NExecutionException;

    /**
     * Help text.
     *
     * @return help text result
     * @throws NExecutionException if execution fails
     */
    @NGetter
    NText helpText() throws NExecutionException;

    /**
     * Command.
     *
     * @return command result
     */
    @NGetter
    List<String> command();

    /**
     * Executor options.
     *
     * @return executor options result
     */
    @NGetter
    List<String> executorOptions();

    /**
     * Converts to command config.
     *
     * @return to command config result
     */
    NCommandConfig toCommandConfig();
}

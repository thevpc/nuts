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
package net.thevpc.nuts.text;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.ext.NExtensions;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Format used to format command line by {@link NExec}
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.4
 */
public interface NExecWriter extends NObjectWriter {
    static NExecWriter of() {
       return NExtensions.of(NExecWriter.class);
    }

    /**
     * true if input redirection is displayed
     *
     * @return true if input redirection is displayed
     */
    boolean isRedirectInput();

    /**
     * if true input redirection is displayed
     *
     * @param redirectInput new value
     * @return {@code this} instance
     */
    NExecWriter redirectInput(boolean redirectInput);

    /**
     * true if output redirection is displayed
     *
     * @return true if output redirection is displayed
     */
    boolean isRedirectOutput();

    /**
     * if true output redirection is displayed
     *
     * @param redirectOutput new value
     * @return {@code this} instance
     */
    NExecWriter redirectOutput(boolean redirectOutput);

    /**
     * true if error redirection is displayed
     *
     * @return true if error redirection is displayed
     */
    boolean isRedirectError();

    /**
     * if true error redirection is displayed
     *
     * @param redirectError new value
     * @return {@code this} instance
     */
    NExecWriter redirectError(boolean redirectError);

    /**
     * return argument filter
     *
     * @return argument filter
     */
    Predicate<ArgEntry> argumentFilter();

    /**
     * set arg filter
     *
     * @param filter arg filter
     * @return {@code this} instance
     */
    NExecWriter argumentFilter(Predicate<ArgEntry> filter);

    /**
     * return argument replacer
     *
     * @return argument replacer
     */
    Function<ArgEntry, String> argumentReplacer();

    /**
     * set arg replacer
     *
     * @param replacer arg replacer
     * @return {@code this} instance
     */
    NExecWriter argumentReplacer(Function<ArgEntry, String> replacer);

    /**
     * return env filter
     *
     * @return env filter
     */
    Predicate<EnvEntry> envFilter();

    /**
     * set env filter
     *
     * @param filter env filter
     * @return {@code this} instance
     */
    NExecWriter envFilter(Predicate<EnvEntry> filter);

    /**
     * return env replacer
     *
     * @return env replacer
     */
    Function<EnvEntry, String> envReplacer();

    /**
     * set env replacer
     *
     * @param replacer env replacer
     * @return {@code this} instance
     */
    NExecWriter envReplacer(Function<EnvEntry, String> replacer);


    NExecWriter ntf(boolean ntf);

    /**
     * env entry
     *
     * @app.category Format
     */
    interface EnvEntry {
        /**
         * env name
         *
         * @return env name
         */
        String name();

        /**
         * env value
         *
         * @return env value
         */
        String value();
    }

    /**
     * argument entry
     *
     * @app.category Format
     */
    interface ArgEntry {
        /**
         * argument index
         *
         * @return argument index
         */
        int index();

        /**
         * argument value
         *
         * @return argument value
         */
        String value();
    }
}

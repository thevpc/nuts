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
 *
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

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Format used to format command line by {@link NutsExecCommand}
 *
 * @author thevpc
 * @see NutsExecCommand#format()
 * @since 0.5.4
 * @app.category Format
 */
public interface NutsExecCommandFormat extends NutsFormat {
    /**
     * true if input redirection is displayed
     * @return true if input redirection is displayed
     */
    boolean isRedirectInput();

    /**
     * if true input redirection is displayed
     * @param redirectInput new value
     * @return {@code this} instance
     */
    NutsExecCommandFormat setRedirectInput(boolean redirectInput);

    /**
     * true if output redirection is displayed
     * @return true if output redirection is displayed
     */
    boolean isRedirectOutput();

    /**
     * if true output redirection is displayed
     * @param redirectOutput new value
     * @return {@code this} instance
     */
    NutsExecCommandFormat setRedirectOutput(boolean redirectOutput);

    /**
     * true if error redirection is displayed
     * @return true if error redirection is displayed
     */
    boolean isRedirectError();

    /**
     * if true error redirection is displayed
     * @param redirectError new value
     * @return {@code this} instance
     */
    NutsExecCommandFormat setRedirectError(boolean redirectError);

    /**
     * return value to format
     * @return value to format
     */
    NutsExecCommand getValue();

    /**
     * set value to format
     * @param value value to format
     * @return {@code this} instance
     */
    NutsExecCommandFormat setValue(NutsExecCommand value);

    /**
     * return argument filter
     * @return argument filter
     */
    Predicate<ArgEntry> getArgumentFilter();

    /**
     * set arg filter
     * @param filter arg filter
     * @return {@code this} instance
     */
    NutsExecCommandFormat setArgumentFilter(Predicate<ArgEntry> filter);

    /**
     * return argument replacer
     * @return argument replacer
     */
    Function<ArgEntry, String> getArgumentReplacer();

    /**
     * set arg replacer
     * @param replacer arg replacer
     * @return {@code this} instance
     */
    NutsExecCommandFormat setArgumentReplacer(Function<ArgEntry, String> replacer);

    /**
     * return env filter
     * @return env filter
     */
    Predicate<EnvEntry> getEnvFilter();

    /**
     * set env filter
     * @param filter env filter
     * @return {@code this} instance
     */
    NutsExecCommandFormat setEnvFilter(Predicate<EnvEntry> filter);

    /**
     * return env replacer
     * @return env replacer
     */
    Function<EnvEntry, String> getEnvReplacer();

    /**
     * set env replacer
     * @param replacer env replacer
     * @return {@code this} instance
     */
    NutsExecCommandFormat setEnvReplacer(Function<EnvEntry, String> replacer);


    NutsExecCommandFormat setNtf(boolean ntf);
    /**
     * env entry
     * @app.category Format
     */
    interface EnvEntry {
        /**
         * env name
         * @return env name
         */
        String getName();

        /**
         * env value
         * @return env value
         */
        String getValue();
    }

    /**
     * argument entry
     * @app.category Format
     */
    interface ArgEntry {
        /**
         * argument index
         * @return argument index
         */
        int getIndex();

        /**
         * argument value
         * @return argument value
         */
        String getValue();
    }
}

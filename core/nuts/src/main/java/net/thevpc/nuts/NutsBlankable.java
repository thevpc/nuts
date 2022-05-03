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

import net.thevpc.nuts.util.NutsApiUtils;

/**
 * Capable of being blank
 *
 * @author thevpc
 * @app.category Command Line
 * @since 0.8.3
 */
public interface NutsBlankable {

    /**
     * true if the value is null or blank (trimmed to empty)
     *
     * @param value value to check
     * @return true if the value is null or blank
     */
    static boolean isBlank(String value) {
        return NutsApiUtils.isBlank(value);
    }

    /**
     * true if the value is null or blank (trimmed to empty)
     *
     * @param value value to check
     * @return true if the value is null or blank
     */
    static boolean isBlank(CharSequence value) {
        return NutsApiUtils.isBlank(value);
    }

    /**
     * true if the value is null or blank (trimmed to empty)
     *
     * @param value value to check
     * @return true if the value is null or blank
     */
    static boolean isBlank(char[] value) {
        return NutsApiUtils.isBlank(value);
    }

    /**
     * true if the value is null or blank
     *
     * @param value value to check
     * @return true if the value is null or blank
     */
    static boolean isBlank(NutsBlankable value) {
        return value == null || value.isBlank();
    }

    /**
     * return true if null or, as a supported blankable object, is blankable
     * @param value value
     * @return true if null or, as a supported blankable object, is blankable
     */
    static boolean isBlank(Object value) {
        return NutsApiUtils.isBlank(value);
    }

    /**
     * true if blank
     *
     * @return argument value
     */
    boolean isBlank();
}

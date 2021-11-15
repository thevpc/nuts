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

import net.thevpc.nuts.boot.NutsApiUtils;

/**
 * Equivalent to FileVisitOption
 *
 * @author thevpc
 * @app.category Base
 * @since 0.8.3
 */
public enum NutsPathOption implements NutsEnum {
    /**
     * follow links
     */
    FOLLOW_LINKS,

    /**
     * replace existing file
     */
    REPLACE_EXISTING,

    /**
     * copy attributes to the new file.
     */
    COPY_ATTRIBUTES,

    /**
     * move the file as an atomic file system operation.
     */
    ATOMIC,

    /**
     * operations involving the path should be interruptible.
     * @see NutsCp#interrupt()
     */
    INTERRUPTIBLE,

    /**
     * operations are implemented in two passes. copy/move will use a temporary file
     */
    SAFE,

    /**
     * log the copy/move progress
     */
    LOG
    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsPathOption() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsPathOption parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsPathOption parseLenient(String value, NutsPathOption emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsPathOption parseLenient(String value, NutsPathOption emptyValue, NutsPathOption errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsPathOption.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }

    public static NutsPathOption parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsPathOption parse(String value, NutsPathOption emptyValue, NutsSession session) {
        NutsPathOption v = parseLenient(value, emptyValue, null);
        NutsApiUtils.checkNonNullEnum(v, value, NutsPathOption.class, session);
        return v;
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}

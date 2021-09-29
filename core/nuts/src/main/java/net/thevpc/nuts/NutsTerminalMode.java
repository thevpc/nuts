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

import net.thevpc.nuts.boot.NutsApiUtils;

/**
 *
 * @author thevpc
 * @since 0.5.4
 * @app.category Base
 */
public enum NutsTerminalMode implements NutsEnum {
    /**
     * streams in inherited mode will <strong>not process</strong> the content but delegate processing to it parents
     */
    INHERITED,

    /**
     * stream supporting ansi escapes!
     */
    ANSI,

    /**
     * streams in formatted mode will process Nuts Stream Format
     * and render in a <strong>colorful</strong> way the its content.
     */
    FORMATTED,

    /**
     * streams in filtered mode will process Nuts Stream Format
     * by filtering (removing) or format characters so that the content is rendered as a <strong>plain</strong> text.
     */
    FILTERED;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NutsTerminalMode() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsTerminalMode parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsTerminalMode parseLenient(String value, NutsTerminalMode emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsTerminalMode parseLenient(String value, NutsTerminalMode emptyValue, NutsTerminalMode errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsTerminalMode.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }

    public static NutsTerminalMode parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsTerminalMode parse(String value, NutsTerminalMode emptyValue, NutsSession session) {
        NutsTerminalMode v = parseLenient(value, emptyValue, null);
        NutsApiUtils.checkNonNullEnum(v,value,NutsTerminalMode.class,session);
        return v;
    }

    /**
     * lower cased identifier.
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}

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

/**
 * user interaction mode. Some operations may require user confirmation before
 * performing critical operations such as overriding existing values, deleting
 * sensitive information ; in such cases several modes are available : either
 * to require user interaction (ASK mode, the default value) or force the
 * processing (YES mode), or ignoring the processing and continuing the next
 * (NO) or cancel the processing and exit with an error message (ERROR)
 *
 * @author thevpc
 * @since 0.5.5
 * @app.category Base
 */
public enum NutsConfirmationMode implements NutsEnum {
    /**
     * force interactive mode
     */
    ASK,
    /**
     * non interactive mode, always perform operation
     */
    YES,
    /**
     * non interactive mode, ignore operation and process next
     */
    NO,
    /**
     * non interactive mode, throw exception
     */
    ERROR;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsConfirmationMode() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsConfirmationMode parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsConfirmationMode parseLenient(String value, NutsConfirmationMode emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsConfirmationMode parseLenient(String value, NutsConfirmationMode emptyValue, NutsConfirmationMode errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsConfirmationMode.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }

    /**
     * lower cased identifier.
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}

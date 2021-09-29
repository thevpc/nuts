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
 * Artifacts are organized according to {@code NutsIdType} to reflect how the artifact
 * should be managed by the workspace.
 * This information is available in {@link NutsDefinition}
 * @app.category Base
 */
public enum NutsIdType implements NutsEnum {
    /**
     * the id denotes a nuts api id artifact.
     * an artifact is a valid nuts api if it corresponds to {@link NutsConstants.Ids#NUTS_API}
     */
    API,

    /**
     * the id denotes a nuts runtime id artifact.
     * an artifact is a valid nuts api if it corresponds to {@link NutsConstants.Ids#NUTS_RUNTIME}
     */
    RUNTIME,

    /**
     * the id denotes a nuts known extension artifact.
     * an artifact is a valid nuts extension if it contains a property "nuts-extension" equal to
     * true in its descriptor.
     */
    EXTENSION,

    /**
     * the id denotes a nuts companion id artifact.
     * Default companions are
     * <ul>
     *     <li>net.thevpc.nuts.toolbox:nsh</li>
     * </ul>
     *
     */
    COMPANION,

    /**
     * the id is none of the previous types
     */
    REGULAR;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsIdType() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsIdType parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsIdType parseLenient(String value, NutsIdType emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsIdType parseLenient(String value, NutsIdType emptyValue, NutsIdType errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsIdType.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }

    public static NutsIdType parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsIdType parse(String value, NutsIdType emptyValue, NutsSession session) {
        NutsIdType v = parseLenient(value, emptyValue, null);
        NutsApiUtils.checkNonNullEnum(v,value,NutsIdType.class,session);
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

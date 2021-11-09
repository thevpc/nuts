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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsEnum;
import net.thevpc.nuts.NutsParseEnumException;
import net.thevpc.nuts.NutsSession;

public enum PrivateNutsRepositorySelectorOp implements NutsEnum {
    INCLUDE,
    EXCLUDE,
    EXACT;
    private final String id;

    PrivateNutsRepositorySelectorOp() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static PrivateNutsRepositorySelectorOp parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static PrivateNutsRepositorySelectorOp parseLenient(String value, PrivateNutsRepositorySelectorOp emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static PrivateNutsRepositorySelectorOp parseLenient(String value, PrivateNutsRepositorySelectorOp emptyValue, PrivateNutsRepositorySelectorOp errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return PrivateNutsRepositorySelectorOp.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }

    public static PrivateNutsRepositorySelectorOp parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static PrivateNutsRepositorySelectorOp parse(String value, PrivateNutsRepositorySelectorOp emptyValue, NutsSession session) {
        PrivateNutsRepositorySelectorOp v = parseLenient(value, emptyValue, null);
        if (v == null) {
            if (!NutsBlankable.isBlank(value)) {
                throw new NutsParseEnumException(session, value, PrivateNutsRepositorySelectorOp.class);
            }
        }
        return v;
    }

    @Override
    public String id() {
        return id;
    }
}

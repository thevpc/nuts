/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

/**
 * uniform permissions
 *
 * @author thevpc
 * @app.category Base
 * @since 0.8.3
 */
public enum NutsPathPermission implements NutsEnum {
    CAN_READ,
    CAN_WRITE,
    CAN_EXECUTE,
    OWNER_READ,

    /**
     * Write permission, owner.
     */
    OWNER_WRITE,

    /**
     * Execute/search permission, owner.
     */
    OWNER_EXECUTE,

    /**
     * Read permission, group.
     */
    GROUP_READ,

    /**
     * Write permission, group.
     */
    GROUP_WRITE,

    /**
     * Execute/search permission, group.
     */
    GROUP_EXECUTE,

    /**
     * Read permission, others.
     */
    OTHERS_READ,

    /**
     * Write permission, others.
     */
    OTHERS_WRITE,

    /**
     * Execute/search permission, others.
     */
    OTHERS_EXECUTE;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsPathPermission() {
        this.id = name().toLowerCase();//.replace('_', '-');
    }

    public static NutsPathPermission parseLenient(String value) {
        return parseLenient(value, null, null);
    }

    public static NutsPathPermission parseLenient(String arch, NutsPathPermission emptyValue) {
        return parseLenient(arch, emptyValue, emptyValue);
    }

    public static NutsPathPermission parseLenient(String value, NutsPathPermission emptyValue, NutsPathPermission errorValue) {
        value = value == null ? "" : value.toUpperCase().replace('-', '_').trim();

        try {
            return valueOf(value);
        }catch (Exception ex){
            return errorValue;
        }

    }

    public static NutsPathPermission parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsPathPermission parse(String value, NutsPathPermission emptyValue, NutsSession session) {
        NutsPathPermission v = parseLenient(value, emptyValue, null);
        NutsApiUtils.checkNonNullEnum(v, value, NutsPathPermission.class, session);
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

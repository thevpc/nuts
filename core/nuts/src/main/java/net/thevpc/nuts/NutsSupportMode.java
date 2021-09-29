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

public enum NutsSupportMode implements NutsEnum {
    UNSUPPORTED,
    SUPPORTED,
    PREFERRED;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsSupportMode() {
        this.id = name().toLowerCase().replace('_', '-');
    }


    public static NutsSupportMode parseLenient(String any) {
        return parseLenient(any, null);
    }

    public static NutsSupportMode parseLenient(String any, NutsSupportMode emptyOrErrorValue) {
        return parseLenient(any, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsSupportMode parseLenient(String any, NutsSupportMode emptyValue, NutsSupportMode errorValue) {
        if (any == null) {
            any = "";
        }
        any = any.toLowerCase();
        switch (any) {
            case "unsupported":
                return UNSUPPORTED;
            case "supported":
                return SUPPORTED;
            case "preferred":
            case "always":
                return PREFERRED;
            case "":
                return emptyValue;
            default:{
                Boolean b = NutsUtilStrings.parseBoolean(any, null, null);
                if(b!=null){
                    return b?SUPPORTED:UNSUPPORTED;
                }
            }
        }
        return errorValue;
    }

    public static NutsSupportMode parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsSupportMode parse(String value, NutsSupportMode emptyValue, NutsSession session) {
        NutsSupportMode v = parseLenient(value, emptyValue, null);
        NutsApiUtils.checkNonNullEnum(v,value,NutsSupportMode.class,session);
        return v;
    }

    public boolean acceptCondition(NutsSupportCondition request, NutsSession session) {
        if (session == null) {
            throw new IllegalArgumentException("missing session");
        }
        if (request == null) {
            request = NutsSupportCondition.NEVER;
        }
        switch (this) {
            case UNSUPPORTED: {
                return false;
            }
            case SUPPORTED: {
                switch (request) {
                    case NEVER:
                        return false;
                    case ALWAYS:
                    case SUPPORTED: {
                        return true;
                    }
                    case PREFERRED: {
                        return false;
                    }
                    default: {
                        throw new NutsUnsupportedEnumException(session, request);
                    }
                }
            }
            case PREFERRED: {
                switch (request) {
                    case NEVER:
                        return false;
                    case ALWAYS:
                    case PREFERRED:
                    case SUPPORTED: {
                        return true;
                    }
                    default: {
                        throw new NutsUnsupportedEnumException(session, request);
                    }
                }
            }
            default: {
                throw new NutsUnsupportedEnumException(session, this);
            }
        }
    }

    @Override
    public String id() {
        return id;
    }
}

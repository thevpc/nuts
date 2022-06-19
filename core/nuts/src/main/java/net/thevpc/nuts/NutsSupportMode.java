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

import net.thevpc.nuts.util.NutsEnum;
import net.thevpc.nuts.util.NutsNameFormat;
import net.thevpc.nuts.util.NutsStringUtils;

public enum NutsSupportMode implements NutsEnum {
    SUPPORTED,
    PREFERRED,
    ALWAYS,
    NEVER
    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsSupportMode() {
        this.id = NutsNameFormat.ID_NAME.formatName(name());
    }

    public static NutsOptional<NutsSupportMode> parse(String value) {
        return NutsStringUtils.parseEnum(value, NutsSupportMode.class, s -> {
            switch (s.getNormalizedValue()){
                case "UNSUPPORTED":
                case "NO":
                case "FALSE":
                    return NutsOptional.of(NEVER);
                case "YES":
                case "TRUE":
                    return NutsOptional.of(ALWAYS);
            }
            return null;
        });
    }


    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }

    public boolean acceptCondition(NutsSupportMode request, NutsSession session) {
        if (session == null) {
            throw new NutsMissingSessionException();
        }
        if (request == null) {
            request = NutsSupportMode.NEVER;
        }
        switch (this) {
            case NEVER: {
                return false;
            }
            case ALWAYS:{
                return true;
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

}

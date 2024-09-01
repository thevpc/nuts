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
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.NMissingSessionException;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NUnsupportedEnumException;

public enum NSupportMode implements NEnum {
    SUPPORTED,
    PREFERRED,
    ALWAYS,
    NEVER
    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NSupportMode() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NSupportMode> parse(String value) {
        return NEnumUtils.parseEnum(value, NSupportMode.class, s -> {
            switch (s.getNormalizedValue()){
                case "UNSUPPORTED":
                case "NO":
                case "FALSE":
                    return NOptional.of(NEVER);
                case "YES":
                case "TRUE":
                    return NOptional.of(ALWAYS);
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

    public boolean acceptCondition(NSupportMode request, NSession session) {
        if (session == null) {
            throw new NMissingSessionException();
        }
        if (request == null) {
            request = NSupportMode.NEVER;
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
                        throw new NUnsupportedEnumException(session, request);
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
                        throw new NUnsupportedEnumException(session, request);
                    }
                }
            }
            default: {
                throw new NUnsupportedEnumException(session, this);
            }
        }
    }

}

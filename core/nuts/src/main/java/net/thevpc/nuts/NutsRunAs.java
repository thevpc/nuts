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

import java.util.Objects;

public class NutsRunAs {
    public static final NutsRunAs CURRENT_USER = new NutsRunAs(Mode.CURRENT_USER, null);
    public static final NutsRunAs ROOT = new NutsRunAs(Mode.ROOT, null);
    public static final NutsRunAs SUDO = new NutsRunAs(Mode.SUDO, null);
    private final Mode mode;
    private final String user;

    private NutsRunAs(Mode mode, String user) {
        this.mode = mode;
        this.user = user;
    }

    public static NutsRunAs currentUser() {
        return CURRENT_USER;
    }

    public static NutsRunAs root() {
        return ROOT;
    }

    public static NutsRunAs sudo() {
        return SUDO;
    }

    public static NutsRunAs user(String name) {
        if (NutsBlankable.isBlank(name)) {
            throw new IllegalArgumentException("invalid user name");
        }
        return new NutsRunAs(Mode.SUDO, name);
    }

    public Mode getMode() {
        return mode;
    }

    public String getUser() {
        return user;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsRunAs nutsRunAs = (NutsRunAs) o;
        return mode == nutsRunAs.mode && Objects.equals(user, nutsRunAs.user);
    }

    @Override
    public String toString() {
        switch (mode) {
            case CURRENT_USER:
                return "run-as:current-user";
            case SUDO:
                return "run-as:sudo";
            case ROOT:
                return "run-as:root";
            case USER:
                return "run-as:" + user;
        }
        return "run-as:" + mode + " , user='" + user + '\'';
    }

    public enum Mode implements NutsEnum {
        CURRENT_USER,
        USER,
        ROOT,
        SUDO;
        private final String id;

        Mode() {
            this.id = name().toLowerCase().replace('_', '-');
        }

        public static Mode parse(String value, NutsSession session) {
            return parse(value, null, session);
        }

        public static Mode parse(String value, Mode emptyValue, NutsSession session) {
            Mode v = parseLenient(value, emptyValue, null);
            if (v == null) {
                if (!NutsBlankable.isBlank(value)) {
                    throw new NutsParseEnumException(session, value, Mode.class);
                }
            }
            return v;
        }

        public static Mode parseLenient(String value) {
            return parseLenient(value, null);
        }

        public static Mode parseLenient(String value, Mode emptyOrErrorValue) {
            return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
        }

        public static Mode parseLenient(String value, Mode emptyValue, Mode errorValue) {
            if (value == null) {
                value = "";
            } else {
                value = value.toUpperCase().trim().replace('-', '_');
            }
            if (value.isEmpty()) {
                return emptyValue;
            }
            try {
                return Mode.valueOf(value.toUpperCase());
            } catch (Exception notFound) {
                return errorValue;
            }
        }

        @Override
        public String id() {
            return id;
        }

    }
}

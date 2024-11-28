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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.*;

import java.util.Objects;

public class NRunAs {
    public static final NRunAs CURRENT_USER = new NRunAs(Mode.CURRENT_USER, null);
    public static final NRunAs ROOT = new NRunAs(Mode.ROOT, null);
    public static final NRunAs SUDO = new NRunAs(Mode.SUDO, null);
    private final Mode mode;
    private final String user;

    private NRunAs(Mode mode, String user) {
        this.mode = mode;
        this.user = user;
    }

    public static NRunAs currentUser() {
        return CURRENT_USER;
    }

    public static NRunAs root() {
        return ROOT;
    }

    public static NRunAs sudo() {
        return SUDO;
    }

    public static NRunAs user(String name) {
        if (NBlankable.isBlank(name)) {
            throw new IllegalArgumentException("invalid user name");
        }
        return new NRunAs(Mode.SUDO, name);
    }

    public static NOptional<NRunAs> parse(String runAs) {
        if(NBlankable.isBlank(runAs)){
            return NOptional.ofNamedEmpty("NRunAs "+runAs);
        }
        runAs=runAs.trim();
        String urunAs=runAs.toUpperCase();
        switch (urunAs){
            case "ROOT":
            case "RUN-AS:ROOT":
            case "RUN_AS:ROOT":
                return NOptional.of(ROOT);
            case "CURRENT_USER":
            case "RUN-AS:CURRENT_USER":
            case "RUN_AS:CURRENT_USER":
                return NOptional.of(CURRENT_USER);
            case "SUDO":
            case "RUN-AS:SUDO":
            case "RUN_AS:SUDO":
                return NOptional.of(SUDO);
        }
        if(urunAs.startsWith("RUN-AS:")){
            return NOptional.of(user(runAs.substring("RUN-AS:".length())));
        }
        return NOptional.ofNamedEmpty("NRunAs "+runAs);
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
        NRunAs nRunAs = (NRunAs) o;
        return mode == nRunAs.mode && Objects.equals(user, nRunAs.user);
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

    public enum Mode implements NEnum {
        CURRENT_USER,
        USER,
        ROOT,
        SUDO;
        private final String id;

        Mode() {
            this.id = NNameFormat.ID_NAME.format(name());
        }

        public static NOptional<Mode> parse(String value) {
            return NEnumUtils.parseEnum(value, Mode.class);
        }

        @Override
        public String id() {
            return id;
        }

    }
}

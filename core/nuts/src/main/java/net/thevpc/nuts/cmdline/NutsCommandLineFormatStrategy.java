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
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.NutsEnum;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.reserved.NutsReservedLangUtils;

/**
 * uniform platform architecture impl-note: list updated from
 * https://github.com/trustin/os-maven-plugin
 *
 * @author thevpc
 * @app.category Base
 * @since 0.8.1
 */
public enum NutsCommandLineFormatStrategy implements NutsEnum {
    DEFAULT,
    NO_QUOTES,
    REQUIRE_QUOTES,
    SUPPORT_QUOTES;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsCommandLineFormatStrategy() {
        this.id = name().toLowerCase();//.replace('_', '-');
    }

    public static NutsCommandLineFormatStrategy getCurrent() {
        return SUPPORT_QUOTES;
    }


    public static NutsOptional<NutsCommandLineFormatStrategy> parse(String value) {
        return NutsReservedLangUtils.parseEnum(value, NutsCommandLineFormatStrategy.class, arch->{
            arch = arch.toLowerCase();
            switch (arch) {
                case "default":
                    return NutsOptional.of(DEFAULT);
                case "no_quotes":
                    return NutsOptional.of(NO_QUOTES);
                case "require_quotes":
                    return NutsOptional.of(REQUIRE_QUOTES);
                case "support_quotes":
                    return NutsOptional.of(SUPPORT_QUOTES);
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
}

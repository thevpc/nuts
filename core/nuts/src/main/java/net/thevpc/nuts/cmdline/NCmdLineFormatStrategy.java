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
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;

/**
 * uniform platform architecture impl-note: list updated from
 * https://github.com/trustin/os-maven-plugin
 *
 * @author thevpc
 * @app.category Base
 * @since 0.8.1
 */
public enum NCmdLineFormatStrategy implements NEnum {
    DEFAULT,
    NO_QUOTES,
    REQUIRE_QUOTES,
    SUPPORT_QUOTES;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NCmdLineFormatStrategy() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NCmdLineFormatStrategy getCurrent() {
        return SUPPORT_QUOTES;
    }


    public static NOptional<NCmdLineFormatStrategy> parse(String value) {
        return NEnumUtils.parseEnum(value, NCmdLineFormatStrategy.class);
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

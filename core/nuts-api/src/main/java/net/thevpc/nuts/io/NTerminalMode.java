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
package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;

/**
 * @author thevpc
 * @app.category Base
 * @since 0.5.4
 */
public enum NTerminalMode implements NEnum {
    /**
     * default value
     */
    DEFAULT,
    /**
     * streams in inherited mode will <strong>not process</strong> the content but delegate processing to it parents
     */
    INHERITED,

    /**
     * stream supporting ansi escapes!
     */
    ANSI,

    /**
     * streams in formatted mode will process Nuts Stream Format
     * and render in a <strong>colorful</strong> way the its content.
     */
    FORMATTED,

    /**
     * streams in filtered mode will process Nuts Stream Format
     * by filtering (removing) or format characters so that the content is rendered as a <strong>plain</strong> text.
     */
    FILTERED;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NTerminalMode() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NTerminalMode> parse(String value) {
        return NEnumUtils.parseEnum(value, NTerminalMode.class, s -> {
            String normalizedValue = s.getNormalizedValue();
            switch (normalizedValue){
                case "SYSTEM":
                case "S":
                case "AUTO":
                case "D":
                    return NOptional.of(DEFAULT);
                case "H":
                    return NOptional.of(INHERITED);
                case "A":
                    return NOptional.of(ANSI);
                default:{
                    Boolean b = NLiteral.of(normalizedValue).asBoolean().orNull();
                    if(b!=null){
                        return NOptional.of(b?FORMATTED:FILTERED);
                    }
                    break;
                }
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

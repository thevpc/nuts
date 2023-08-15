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

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * Formats supported by Nuts
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.4
 */
public enum NContentType implements NEnum {
    /**
     * json format
     */
    JSON,

    /**
     * xml format
     */
    XML,

    /**
     * java properties
     */
    PROPS,

    /**
     * (terminal/ascii) table format
     */
    TABLE,

    /**
     * (terminal/ascii) tree format
     */
    TREE,

    /**
     * plain (no) format
     */
    PLAIN,

    /**
     * Type Safe Object Notation
     *
     * @since 0.8.1
     */
    TSON,

    /**
     * YAML Ain't Markup Language
     *
     * @since 0.8.1
     */
    YAML,
    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * private constructor
     */
    NContentType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NContentType> parse(String value) {
        return NEnumUtils.parseEnum(value, NContentType.class);
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

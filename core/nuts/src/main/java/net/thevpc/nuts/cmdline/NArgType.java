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
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;

/**
 * Argument parse Type
 *
 * @author thevpc
 * @app.category Command Line
 * @since 0.5.5
 */
public enum NArgType implements NEnum {
    /**
     * argument that may or may not accept value.
     */
    DEFAULT,
    /**
     * argument that accepts a string as value. Either the string is included in
     * the argument itself (--option=value) or succeeds it (--option value).
     */
    ENTRY,
    /**
     * argument that accepts a boolean as value. Either the boolean is not
     * defined (--option), is included in the argument itself (--option=true) or
     * succeeds it (--option true). Parsing boolean is also aware of negated
     * options (--!option) that will be interpreted as (--option=false).
     */
    FLAG;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * Default constructor
     */
    NArgType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NArgType> parse(String value) {
        return NEnumUtils.parseEnum(value, NArgType.class);
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

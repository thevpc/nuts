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
package net.thevpc.nuts.format;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NStringUtils;

/**
 * @app.category Format
 */
public enum NTableSeparator implements NEnum {
    FIRST_ROW_START,
    FIRST_ROW_LINE,
    FIRST_ROW_SEP,
    FIRST_ROW_END,
    ROW_START,
    ROW_SEP,
    ROW_END,
    MIDDLE_ROW_START,
    MIDDLE_ROW_LINE,
    MIDDLE_ROW_SEP,
    MIDDLE_ROW_END,
    LAST_ROW_START,
    LAST_ROW_LINE,
    LAST_ROW_SEP,
    LAST_ROW_END;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NTableSeparator() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NTableSeparator> parse(String value) {
        return NStringUtils.parseEnum(value, NTableSeparator.class);
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

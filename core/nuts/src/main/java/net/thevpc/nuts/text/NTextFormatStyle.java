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
package net.thevpc.nuts.text;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Locale;

/**
 * @author thevpc
 * @app.category Format
 */
public enum NTextFormatStyle implements NEnum {
    /**
     * @see java.util.Formatter
     * @see String#format(Locale, String, Object...)
     */
    CSTYLE,
    /**
     * @see java.text.MessageFormat
     */
    JSTYLE,
    /**
     * with var place holders
     */
    VSTYLE,
    /**
     * plain text
     */
    PLAIN,
    /**
     * NTF format without arguments.
     * was (formatted)
     */
    NTF,
    /**
     * plain format given style
     *
     * @since 0.8.4
     */
    STYLED,
    /**
     * code
     */
    CODE,
    ;
    private final String id;

    NTextFormatStyle() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NTextFormatStyle> parse(String value) {
        return NStringUtils.parseEnum(value, NTextFormatStyle.class);
    }


    @Override
    public String id() {
        return id;
    }
}
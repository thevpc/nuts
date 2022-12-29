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

import net.thevpc.nuts.text.NTextFormatStyle;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.util.NUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;

public class NMsg {

    public static final Object[] NO_PARAMS = new Object[0];
    private final String codeLang;
    private final Object message;
    private final Level level;
    private final NTextFormatStyle format;
    private final Object[] params;
    private final NTextStyles styles;

    private static NMsg of(NTextFormatStyle format, Object message, Object[] params, NTextStyles styles, String codeLang, Level level) {
        return new NMsg(format, message, params, styles, codeLang, level);
    }

    private NMsg(NTextFormatStyle format, Object message, Object[] params, NTextStyles styles, String codeLang, Level level) {
        NUtils.requireNonNull(message, "message");
        NUtils.requireNonNull(format, "format");
        NUtils.requireNonNull(params, "params");
        this.level = level;
        this.format = format;
        this.styles = styles;
        if (format == NTextFormatStyle.PLAIN
                || format == NTextFormatStyle.NTF
                || format == NTextFormatStyle.STYLED
                || format == NTextFormatStyle.CODE
        ) {
            if (params.length > 0) {
                throw new IllegalArgumentException("arguments are not supported for " + format);
            }
        }
        if (format == NTextFormatStyle.STYLED) {
            if (styles == null) {
                throw new IllegalArgumentException("missing style for " + format);
            }
        } else {
            if (styles != null) {
                throw new IllegalArgumentException("styles not supported for " + format);
            }
        }
        this.codeLang = NStringUtils.trimToNull(codeLang);
        this.message = message;
        this.params = params;
    }

    public static NMsg ofNtf(String message) {
        return of(NTextFormatStyle.NTF, message, NO_PARAMS, null, null, null);
    }

    public static NMsg ofCode(String lang, String text) {
        return of(NTextFormatStyle.CODE, text, NO_PARAMS, null, lang, null);
    }

    public static NMsg ofCode(String text) {
        return of(NTextFormatStyle.CODE, text, NO_PARAMS, null, null, null);
    }

    public static NMsg ofStyled(String message, NTextStyle style) {
        return of(NTextFormatStyle.STYLED, message, NO_PARAMS, style == null ? null : NTextStyles.of(style), null, null);
    }

    public static NMsg ofStyled(String message, NTextStyles styles) {
        return of(NTextFormatStyle.STYLED, message, NO_PARAMS, styles, null, null);
    }

    public static NMsg ofStyled(NMsg message, NTextStyle style) {
        return of(NTextFormatStyle.STYLED, message, NO_PARAMS, style == null ? null : NTextStyles.of(style), null, null);
    }

    public static NMsg ofStyled(NMsg message, NTextStyles styles) {
        return of(NTextFormatStyle.STYLED, message, NO_PARAMS, styles, null, null);
    }

    public static NMsg ofStyled(NString message, NTextStyle style) {
        return of(NTextFormatStyle.STYLED, message, NO_PARAMS, style == null ? null : NTextStyles.of(style), null, null);
    }

    public static NMsg ofStyled(NString message, NTextStyles styles) {
        return of(NTextFormatStyle.STYLED, message, NO_PARAMS, styles, null, null);
    }

    public static NMsg ofNtf(NString message) {
        return of(NTextFormatStyle.NTF, message, NO_PARAMS, null, null, null);
    }

    public static NMsg ofPlain(String message) {
        return of(NTextFormatStyle.PLAIN, message, NO_PARAMS, null, null, null);
    }

    @Deprecated
    public static NMsg ofCstyle(String message) {
        return of(NTextFormatStyle.CSTYLE, message, NO_PARAMS, null, null, null);
    }

    public static NMsg ofCstyle(String message, Object... params) {
        return of(NTextFormatStyle.CSTYLE, message, params, null, null, null);
    }

    public static NMsg ofVstyle(String message, Map<String, ?> vars) {
        return of(NTextFormatStyle.VSTYLE, message, new Object[]{vars}, null, null, null);
    }

    @Deprecated
    public static NMsg ofJstyle(String message) {
        return of(NTextFormatStyle.JSTYLE, message, NO_PARAMS, null, null, null);
    }

    public static NMsg ofJstyle(String message, Object... params) {
        return of(NTextFormatStyle.JSTYLE, message, params, null, null, null);
    }

    public NTextFormatStyle getFormat() {
        return format;
    }

    public NTextStyles getStyles() {
        return styles;
    }

    public Object getMessage() {
        return message;
    }

    public Object[] getParams() {
        return params;
    }

    public String getCodeLang() {
        return codeLang;
    }

    public Level getLevel() {
        return level;
    }

    @Override
    public String toString() {
        switch (format) {
            case CSTYLE: {
                StringBuilder sb = new StringBuilder();
                new Formatter(sb).format((String) message, params);
                return sb.toString();
            }
            case JSTYLE: {
                return MessageFormat.format((String) message, params);
            }
            case VSTYLE: {
                return formatAsVStyle();
            }
            case NTF:
            case STYLED:
            case CODE:
            case PLAIN: {
                return String.valueOf(message); //ignore any style
            }
        }
        return "NMsg{" + "message=" + message + ", style=" + format + ", params=" + Arrays.toString(params) + '}';
    }

    private String formatAsVStyle() {
        return NStringUtils.replaceDollarString((String) message,
                s -> {
                    Map<String, ?> m = (Map<String, ?>) (params[0]);
                    Object v = m.get(s);
                    if (v != null) {
                        return String.valueOf(v);
                    }
                    return "${" + s + "}";
                }
        );
    }

    public NMsg withLevel(Level level) {
        return of(format, message, params, styles, codeLang, level);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NMsg that = (NMsg) o;
        return Objects.equals(codeLang, that.codeLang)
                && Objects.equals(message, that.message)
                && format == that.format
                && Arrays.deepEquals(params, that.params)
                && Objects.equals(styles, that.styles)
                && Objects.equals(level, that.level)
                ;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(codeLang, message, format, styles, level);
        result = 31 * result + Arrays.hashCode(params);
        return result;
    }
}

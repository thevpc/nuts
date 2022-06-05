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

import net.thevpc.nuts.text.NutsTextFormatStyle;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTextStyles;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsStringUtils;
import net.thevpc.nuts.util.NutsUtils;

import java.text.MessageFormat;
import java.util.*;

public class NutsMessage {

    public static final Object[] NO_PARAMS = new Object[0];
    private final String codeLang;
    private final Object message;
    private final NutsTextFormatStyle format;
    private final Object[] params;
    private final NutsTextStyles styles;

    private static NutsMessage of(NutsTextFormatStyle format, Object message, Object[] params, NutsTextStyles styles, String codeLang) {
        return new NutsMessage(format, message, params, styles, codeLang);
    }

    private NutsMessage(NutsTextFormatStyle format, Object message, Object[] params, NutsTextStyles styles, String codeLang) {
        NutsUtils.requireNonNull(message, "message");
        NutsUtils.requireNonNull(format, "format");
        NutsUtils.requireNonNull(params, "params");
        this.format = format;
        this.styles = styles;
        if (format == NutsTextFormatStyle.PLAIN
                || format == NutsTextFormatStyle.NTF
                || format == NutsTextFormatStyle.STYLED
                || format == NutsTextFormatStyle.CODE
        ) {
            if (params.length > 0) {
                throw new IllegalArgumentException("arguments are not supported for " + format);
            }
        }
        if (format == NutsTextFormatStyle.STYLED) {
            if (styles == null) {
                throw new IllegalArgumentException("missing style for " + format);
            }
        } else {
            if (styles != null) {
                throw new IllegalArgumentException("styles not supported for " + format);
            }
        }
        this.codeLang = NutsStringUtils.trimToNull(codeLang);
        this.message = message;
        this.params = params;
    }

    public static NutsMessage ofNtf(String message) {
        return of(NutsTextFormatStyle.NTF, message, NO_PARAMS, null, null);
    }

    public static NutsMessage ofCode(String lang, String text) {
        return of(NutsTextFormatStyle.CODE, text, NO_PARAMS, null, lang);
    }

    public static NutsMessage ofCode(String text) {
        return of(NutsTextFormatStyle.CODE, text, NO_PARAMS, null, null);
    }

    public static NutsMessage ofStyled(String message, NutsTextStyle style) {
        return of(NutsTextFormatStyle.STYLED, message, NO_PARAMS, style == null ? null : NutsTextStyles.of(style), null);
    }

    public static NutsMessage ofStyled(String message, NutsTextStyles styles) {
        return of(NutsTextFormatStyle.STYLED, message, NO_PARAMS, styles, null);
    }

    public static NutsMessage ofStyled(NutsMessage message, NutsTextStyle style) {
        return of(NutsTextFormatStyle.STYLED, message, NO_PARAMS, style == null ? null : NutsTextStyles.of(style), null);
    }

    public static NutsMessage ofStyled(NutsMessage message, NutsTextStyles styles) {
        return of(NutsTextFormatStyle.STYLED, message, NO_PARAMS, styles, null);
    }

    public static NutsMessage ofStyled(NutsString message, NutsTextStyle style) {
        return of(NutsTextFormatStyle.STYLED, message, NO_PARAMS, style == null ? null : NutsTextStyles.of(style), null);
    }

    public static NutsMessage ofStyled(NutsString message, NutsTextStyles styles) {
        return of(NutsTextFormatStyle.STYLED, message, NO_PARAMS, styles, null);
    }

    public static NutsMessage ofNtf(NutsString message) {
        return of(NutsTextFormatStyle.NTF, message, NO_PARAMS, null, null);
    }

    public static NutsMessage ofPlain(String message) {
        return of(NutsTextFormatStyle.PLAIN, message, NO_PARAMS, null, null);
    }

    @Deprecated
    public static NutsMessage ofCstyle(String message) {
        return of(NutsTextFormatStyle.CSTYLE, message, NO_PARAMS, null, null);
    }

    public static NutsMessage ofCstyle(String message, Object... params) {
        return of(NutsTextFormatStyle.CSTYLE, message, params, null, null);
    }

    public static NutsMessage ofVstyle(String message, Map<String, ?> vars) {
        return of(NutsTextFormatStyle.VSTYLE, message, new Object[]{vars}, null, null);
    }

    @Deprecated
    public static NutsMessage ofJstyle(String message) {
        return of(NutsTextFormatStyle.JSTYLE, message, NO_PARAMS, null, null);
    }

    public static NutsMessage ofJstyle(String message, Object... params) {
        return of(NutsTextFormatStyle.JSTYLE, message, params, null, null);
    }

    public NutsTextFormatStyle getFormat() {
        return format;
    }

    public NutsTextStyles getStyles() {
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
        return "NutsMessage{" + "message=" + message + ", style=" + format + ", params=" + Arrays.toString(params) + '}';
    }

    private String formatAsVStyle() {
        return NutsStringUtils.replaceDollarString((String) message,
                s -> {
                    Map<String, ?> m = (Map<String, ?>) (params[0]);
                    Object v = m.get(s);
                    if (v != null) {
                        return String.valueOf(v);
                    }
                    return "${"+s+"}";
                }
        );
    }

}

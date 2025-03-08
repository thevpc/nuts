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
package net.thevpc.nuts.util;

import net.thevpc.nuts.NExceptionHandler;
import net.thevpc.nuts.format.NMsgFormattable;
import net.thevpc.nuts.text.*;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

public class NMsg {

    public static final Object[] NO_PARAMS = new Object[0];
    private final String codeLang;
    private final Object message;
    private final Level level;
    private final NTextFormatType format;
    private final Object[] params;
    private final NTextStyles styles;

    public static NMsg ofMissingValue() {
        return ofMissingValue((String) null);
    }

    public static NMsg ofMissingValue(String valueName) {
        if (NBlankable.isBlank(valueName)) {
            return NMsg.ofPlain("missing value");
        }
        return NMsg.ofC("missing %s", valueName);
    }

    public static NMsg ofMissingValue(NMsg valueName) {
        if (NBlankable.isBlank(valueName)) {
            return NMsg.ofPlain("missing value");
        }
        return NMsg.ofC("missing %s", valueName);
    }

    public static NMsg ofInvalidValue() {
        return ofInvalidValue(null, (String)null);
    }

    public static NMsg ofInvalidValue(Throwable throwable) {
        return ofInvalidValue(throwable, (String)null);
    }

    public static NMsg ofInvalidValue(String valueName) {
        return ofInvalidValue(null, valueName);
    }

    public static NMsg ofInvalidValue(Throwable throwable, String valueName) {
        if (throwable == null) {
            if (NBlankable.isBlank(valueName)) {
                return NMsg.ofPlain("invalid value");
            }
            return NMsg.ofC("invalid %s", valueName);
        }
        if (NBlankable.isBlank(valueName)) {
            return ofC("invalid value : %s", NExceptionHandler.getErrorMessage(throwable));
        }
        return ofC("invalid %s : %s", valueName, NExceptionHandler.getErrorMessage(throwable));
    }

    public static NMsg ofInvalidValue(Throwable throwable, NMsg valueName) {
        if (throwable == null) {
            if (NBlankable.isBlank(valueName)) {
                return NMsg.ofPlain("invalid value");
            }
            return NMsg.ofC("invalid %s", valueName);
        }
        if (NBlankable.isBlank(valueName)) {
            return ofC("invalid value : %s", NExceptionHandler.getErrorMessage(throwable));
        }
        return ofC("invalid %s : %s", valueName, NExceptionHandler.getErrorMessage(throwable));
    }

    private static NMsg of(NTextFormatType format, Object message, Object[] params, NTextStyles styles, String codeLang, Level level) {
        return new NMsg(format, message, params, styles, codeLang, level);
    }

    private NMsg(NTextFormatType format, Object message, Object[] params, NTextStyles styles, String codeLang, Level level) {
        NAssert.requireNonNull(message, "message");
        NAssert.requireNonNull(format, "format");
        NAssert.requireNonNull(params, "params");
        this.level = level;
        this.format = format;
        this.styles = styles;
        if (format == NTextFormatType.PLAIN
                || format == NTextFormatType.NTF
                || format == NTextFormatType.STYLED
                || format == NTextFormatType.CODE
        ) {
            if (params.length > 0) {
                throw new IllegalArgumentException("arguments are not supported for " + format);
            }
        }
        if (format == NTextFormatType.STYLED) {
            NAssert.requireNonNull(styles, "styles for " + format);
        } else {
            NAssert.requireNull(styles, "styles for " + format + " (not supported)");
        }
        this.codeLang = NStringUtils.trimToNull(codeLang);
        this.message = message;
        this.params = params;
    }

    public static NMsg ofNtf(String message) {
        return of(NTextFormatType.NTF, message, NO_PARAMS, null, null, null);
    }

    public static NMsg ofCode(String lang, String text) {
        return of(NTextFormatType.CODE, text, NO_PARAMS, null, lang, null);
    }

    public static NMsg ofCode(String text) {
        return of(NTextFormatType.CODE, text, NO_PARAMS, null, null, null);
    }

    public static NMsg ofStringLiteral(String literal) {
        if (literal == null) {
            return NMsg.ofStyled("null", NTextStyle.primary1());
        }
        return NMsg.ofStyled(NStringUtils.formatStringLiteral(literal), NTextStyle.string());
    }

    public static NMsg ofStyled(String message, NTextStyle style) {
        return of(NTextFormatType.STYLED, message, NO_PARAMS, style == null ? null : NTextStyles.of(style), null, null);
    }

    public static NMsg ofStyled(String message, NTextStyles styles) {
        return of(NTextFormatType.STYLED, message, NO_PARAMS, styles, null, null);
    }

    public static NMsg ofStyled(NMsg message, NTextStyle style) {
        return of(NTextFormatType.STYLED, message, NO_PARAMS, style == null ? null : NTextStyles.of(style), null, null);
    }

    public static NMsg ofStyled(NMsg message, NTextStyles styles) {
        return of(NTextFormatType.STYLED, message, NO_PARAMS, styles, null, null);
    }

    public static NMsg ofStyled(NText message, NTextStyle style) {
        return of(NTextFormatType.STYLED, message, NO_PARAMS, style == null ? null : NTextStyles.of(style), null, null);
    }

    public static NMsg ofStyled(NText message, NTextStyles styles) {
        return of(NTextFormatType.STYLED, message, NO_PARAMS, styles, null, null);
    }

    public static NMsg ofNtf(NText message) {
        return of(NTextFormatType.NTF, message, NO_PARAMS, null, null, null);
    }

    public static NMsg ofPlain(String message) {
        return of(NTextFormatType.PLAIN, message, NO_PARAMS, null, null, null);
    }

    public static NMsg ofC(String message) {
        return of(NTextFormatType.CFORMAT, message, NO_PARAMS, null, null, null);
    }

    public static NMsg ofC(String message, Object... params) {
        return of(NTextFormatType.CFORMAT, message, params, null, null, null);
    }

    public static NMsg ofV(String message, NMsgParam... params) {
        Map<String, NMsgParam> vars = new LinkedHashMap<>();
        if (params != null) {
            for (NMsgParam param : params) {
                String e = param.getName();
                if (vars.containsKey(e)) {
                    throw new IllegalArgumentException("duplicate key " + e);
                }
                vars.put(e, param);
            }
        }
        return ofV(message, s -> {
            NMsgParam p = vars.get(s);
            if (p != null) {
                Supplier<?> ss = p.getValue();
                if (ss != null) {
                    return ss.get();
                }
            }
            return null;
        });
    }

    public static NMsg ofV(String message, Map<String, ?> vars) {
        return of(NTextFormatType.VFORMAT, message, new Object[]{vars}, null, null, null);
    }

    public static NMsg ofV(String message, Function<String, ?> vars) {
        return of(NTextFormatType.VFORMAT, message, new Object[]{vars}, null, null, null);
    }

    public static NMsg ofJ(String message, NMsgParam... params) {
        if (params == null) {
            return ofJ(message, new Object[]{null});
        }
        Object[] paramsAsObjects = Arrays.stream(params).map(NMsgParam::getValue).toArray();
        return ofJ(message, paramsAsObjects);
    }

    public static NMsg ofC(String message, NMsgParam... params) {
        if (params == null) {
            return ofC(message, new Object[]{null});
        }
        Object[] paramsAsObjects = Arrays.stream(params).map(NMsgParam::getValue).toArray();
        return ofC(message, paramsAsObjects);
    }

    @Deprecated
    public static NMsg ofJ(String message) {
        return of(NTextFormatType.JFORMAT, message, NO_PARAMS, null, null, null);
    }

    public static NMsg ofJ(String message, Object... params) {
        return of(NTextFormatType.JFORMAT, message, params, null, null, null);
    }

    public NTextFormatType getFormat() {
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

    private Object _preFormatOne(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof NMsgFormattable) {
            return ((NMsgFormattable) o).toMsg();
        }
        if (o instanceof Throwable) {
            return NExceptionHandler.getErrorMessage((Throwable) o);
        }
        return o;
    }

    private Object[] _preFormatArr(Object[] o) {
        if (o == null) {
            return o;
        }
        Object[] r = new Object[o.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = _preFormatOne(o[i]);
        }
        return r;
    }

    @Override
    public String toString() {
        try {
            switch (format) {
                case CFORMAT: {
                    StringBuilder sb = new StringBuilder();
                    new Formatter(sb).format((String) message, _preFormatArr(params));
                    return sb.toString();
                }
                case JFORMAT: {
                    //must process special case of {}
                    String sMsg = (String) message;
                    if (sMsg.contains("{}")) {
                        StringBuilder sb = new StringBuilder();
                        char[] chars = sMsg.toCharArray();
                        int currentIndex = 0;
                        for (int i = 0; i < chars.length; i++) {
                            char c = chars[i];
                            if (c == '{') {
                                StringBuilder sb2 = new StringBuilder();
                                i++;
                                while (i < chars.length) {
                                    char c2 = chars[i];
                                    if (c2 == '}') {
                                        break;
                                    } else if (c2 == '\\') {
                                        sb2.append(c2);
                                        i++;
                                        if (i < chars.length) {
                                            c2 = chars[i];
                                            sb2.append(c2);
                                        }
                                    } else {
                                        sb2.append(c2);
                                    }
                                }
                                String s2 = sb2.toString();
                                if (s2.isEmpty()) {
                                    s2 = String.valueOf(currentIndex);
                                } else if (s2.trim().startsWith(":")) {
                                    s2 = String.valueOf(currentIndex) + s2;
                                }
                                sb.append("{").append(s2).append("}");
                                currentIndex++;
                            } else if (c == '\\') {
                                sb.append(c);
                                i++;
                                if (i < chars.length) {
                                    sb.append(c);
                                }
                            } else {
                                sb.append(c);
                            }
                        }
                        sMsg = sb.toString();
                    }
                    return MessageFormat.format(sMsg, _preFormatArr(params));
                }
                case VFORMAT: {
                    return formatAsV();
                }
                case NTF:
                case STYLED:
                case CODE:
                case PLAIN: {
                    return String.valueOf(message); //ignore any style
                }
            }
            return "NMsg{" + "message=" + message + ", style=" + format + ", params=" + Arrays.toString(_preFormatArr(params)) + '}';

        } catch (Exception e) {
            List<Object> a = new ArrayList<>();
            if (params != null) {
                a.add(Arrays.asList(params));
            }
            return NMsg.ofC("[ERROR] Invalid %s message %s with params %s : %s", format, message, a, e).toString();
        }
    }

    private String formatAsV() {
        return NStringUtils.replaceDollarPlaceHolder((String) message,
                s -> {
                    Object param = params[0];
                    Function<String, ?> m = null;
                    if (param instanceof Map) {
                        m = x -> ((Map<String, ?>) param).get(x);
                    } else {
                        m = (Function<String, ?>) param;
                    }
                    Object v = m.apply(s);
                    if (v != null) {
                        return String.valueOf(v);
                    }
                    return "${" + s + "}";
                }
        );
    }

    public NMsg asSevere() {
        return withLevel(Level.SEVERE);
    }

    public NMsg asInfo() {
        return withLevel(Level.FINE);
    }

    public NMsg asConfig() {
        return withLevel(Level.CONFIG);
    }

    public NMsg asWarning() {
        return withLevel(Level.WARNING);
    }

    public NMsg asFinest() {
        return withLevel(Level.FINEST);
    }

    public NMsg asFine() {
        return withLevel(Level.FINE);
    }

    public NMsg asFiner() {
        return withLevel(Level.FINER);
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

    /// //////////////////////////////////////////////////////////////////
    public static NMsg ofStyledKeyword(String message) {
        return ofStyled(message, NTextStyle.keyword());
    }

    public static NMsg ofStyledPath(String message) {
        return ofStyled(message, NTextStyle.path());
    }

    public static NMsg ofStyledPale(String message) {
        return ofStyled(message, NTextStyle.pale());
    }

    public static NMsg ofStyledSeparator(String message) {
        return ofStyled(message, NTextStyle.separator());
    }

    public static NMsg ofStyledString(String message) {
        return ofStyled(message, NTextStyle.string());
    }

    public static NMsg ofStyledBlink(String message) {
        return ofStyled(message, NTextStyle.blink());
    }

    public static NMsg ofStyledBold(String message) {
        return ofStyled(message, NTextStyle.bold());
    }

    public static NMsg ofStyledBool(String message) {
        return ofStyled(message, NTextStyle.bool());
    }

    public static NMsg ofStyledComments(String message) {
        return ofStyled(message, NTextStyle.comments());
    }

    public static NMsg ofStyledConfig(String message) {
        return ofStyled(message, NTextStyle.config());
    }

    public static NMsg ofStyledDanger(String message) {
        return ofStyled(message, NTextStyle.danger());
    }

    public static NMsg ofStyledDate(String message) {
        return ofStyled(message, NTextStyle.date());
    }

    public static NMsg ofStyledError(String message) {
        return ofStyled(message, NTextStyle.error());
    }

    public static NMsg ofStyledFail(String message) {
        return ofStyled(message, NTextStyle.fail());
    }

    public static NMsg ofStyledInfo(String message) {
        return ofStyled(message, NTextStyle.info());
    }

    public static NMsg ofStyledInput(String message) {
        return ofStyled(message, NTextStyle.input());
    }

    public static NMsg ofStyledItalic(String message) {
        return ofStyled(message, NTextStyle.italic());
    }

    public static NMsg ofStyledNumber(String message) {
        return ofStyled(message, NTextStyle.number());
    }

    public static NMsg ofStyledOperator(String message) {
        return ofStyled(message, NTextStyle.operator());
    }

    public static NMsg ofStyledOption(String message) {
        return ofStyled(message, NTextStyle.option());
    }

    public static NMsg ofStyledPrimary1(String message) {
        return ofStyled(message, NTextStyle.primary1());
    }

    public static NMsg ofStyledPrimary2(String message) {
        return ofStyled(message, NTextStyle.primary2());
    }

    public static NMsg ofStyledPrimary3(String message) {
        return ofStyled(message, NTextStyle.primary3());
    }

    public static NMsg ofStyledPrimary4(String message) {
        return ofStyled(message, NTextStyle.primary4());
    }

    public static NMsg ofStyledPrimary5(String message) {
        return ofStyled(message, NTextStyle.primary5());
    }

    public static NMsg ofStyledPrimary6(String message) {
        return ofStyled(message, NTextStyle.primary6());
    }

    public static NMsg ofStyledPrimary7(String message) {
        return ofStyled(message, NTextStyle.primary7());
    }

    public static NMsg ofStyledPrimary8(String message) {
        return ofStyled(message, NTextStyle.primary8());
    }

    public static NMsg ofStyledPrimary9(String message) {
        return ofStyled(message, NTextStyle.primary9());
    }

    public static NMsg ofStyledSecondary1(String message) {
        return ofStyled(message, NTextStyle.secondary1());
    }

    public static NMsg ofStyledSecondary2(String message) {
        return ofStyled(message, NTextStyle.secondary2());
    }

    public static NMsg ofStyledSecondary3(String message) {
        return ofStyled(message, NTextStyle.secondary3());
    }

    public static NMsg ofStyledSecondary4(String message) {
        return ofStyled(message, NTextStyle.secondary4());
    }

    public static NMsg ofStyledSecondary5(String message) {
        return ofStyled(message, NTextStyle.secondary5());
    }

    public static NMsg ofStyledSecondary6(String message) {
        return ofStyled(message, NTextStyle.secondary6());
    }

    public static NMsg ofStyledSecondary7(String message) {
        return ofStyled(message, NTextStyle.secondary7());
    }

    public static NMsg ofStyledSecondary8(String message) {
        return ofStyled(message, NTextStyle.secondary8());
    }

    public static NMsg ofStyledSecondary9(String message) {
        return ofStyled(message, NTextStyle.secondary9());
    }

    public static NMsg ofStyledTitle1(String message) {
        return ofStyled(message, NTextStyle.title1());
    }

    public static NMsg ofStyledTitle2(String message) {
        return ofStyled(message, NTextStyle.title2());
    }

    public static NMsg ofStyledTitle3(String message) {
        return ofStyled(message, NTextStyle.title3());
    }

    public static NMsg ofStyledTitle4(String message) {
        return ofStyled(message, NTextStyle.title4());
    }

    public static NMsg ofStyledTitle5(String message) {
        return ofStyled(message, NTextStyle.title5());
    }

    public static NMsg ofStyledTitle6(String message) {
        return ofStyled(message, NTextStyle.title6());
    }

    public static NMsg ofStyledTitle7(String message) {
        return ofStyled(message, NTextStyle.title7());
    }

    public static NMsg ofStyledTitle8(String message) {
        return ofStyled(message, NTextStyle.title8());
    }

    public static NMsg ofStyledTitle9(String message) {
        return ofStyled(message, NTextStyle.title9());
    }

    public static NMsg ofStyledSuccess(String message) {
        return ofStyled(message, NTextStyle.success());
    }

    public static NMsg ofStyledStriked(String message) {
        return ofStyled(message, NTextStyle.striked());
    }

    public static NMsg ofStyledVariable(String message) {
        return ofStyled(message, NTextStyle.variable());
    }

    public static NMsg ofStyledWarn(String message) {
        return ofStyled(message, NTextStyle.warn());
    }

    public static NMsg ofStyledForegroundColor(String message, int color) {
        return ofStyled(message, NTextStyle.foregroundColor(color));
    }

    public static NMsg ofStyledForegroundTrueColor(String message, int color) {
        return ofStyled(message, NTextStyle.foregroundTrueColor(color));
    }

    public static NMsg ofStyledBackgroundColor(String message, int color) {
        return ofStyled(message, NTextStyle.backgroundColor(color));
    }

    public static NMsg ofStyledBackgroundTrueColor(String message, int color) {
        return ofStyled(message, NTextStyle.backgroundTrueColor(color));
    }

    /// //////////////////////////////////////////////////////////////////
    public static NMsg ofStyledKeyword(NMsg message) {
        return ofStyled(message, NTextStyle.keyword());
    }

    public static NMsg ofStyledPath(NMsg message) {
        return ofStyled(message, NTextStyle.path());
    }

    public static NMsg ofStyledPale(NMsg message) {
        return ofStyled(message, NTextStyle.pale());
    }

    public static NMsg ofStyledSeparator(NMsg message) {
        return ofStyled(message, NTextStyle.separator());
    }

    public static NMsg ofStyledString(NMsg message) {
        return ofStyled(message, NTextStyle.string());
    }

    public static NMsg ofStyledBlink(NMsg message) {
        return ofStyled(message, NTextStyle.blink());
    }

    public static NMsg ofStyledBold(NMsg message) {
        return ofStyled(message, NTextStyle.bold());
    }

    public static NMsg ofStyledBool(NMsg message) {
        return ofStyled(message, NTextStyle.bool());
    }

    public static NMsg ofStyledComments(NMsg message) {
        return ofStyled(message, NTextStyle.comments());
    }

    public static NMsg ofStyledConfig(NMsg message) {
        return ofStyled(message, NTextStyle.config());
    }

    public static NMsg ofStyledDanger(NMsg message) {
        return ofStyled(message, NTextStyle.danger());
    }

    public static NMsg ofStyledDate(NMsg message) {
        return ofStyled(message, NTextStyle.date());
    }

    public static NMsg ofStyledError(NMsg message) {
        return ofStyled(message, NTextStyle.error());
    }

    public static NMsg ofStyledFail(NMsg message) {
        return ofStyled(message, NTextStyle.fail());
    }

    public static NMsg ofStyledInfo(NMsg message) {
        return ofStyled(message, NTextStyle.info());
    }

    public static NMsg ofStyledInput(NMsg message) {
        return ofStyled(message, NTextStyle.input());
    }

    public static NMsg ofStyledItalic(NMsg message) {
        return ofStyled(message, NTextStyle.italic());
    }

    public static NMsg ofStyledNumber(NMsg message) {
        return ofStyled(message, NTextStyle.number());
    }

    public static NMsg ofStyledOperator(NMsg message) {
        return ofStyled(message, NTextStyle.operator());
    }

    public static NMsg ofStyledOption(NMsg message) {
        return ofStyled(message, NTextStyle.option());
    }

    public static NMsg ofStyledPrimary1(NMsg message) {
        return ofStyled(message, NTextStyle.primary1());
    }

    public static NMsg ofStyledPrimary2(NMsg message) {
        return ofStyled(message, NTextStyle.primary2());
    }

    public static NMsg ofStyledPrimary3(NMsg message) {
        return ofStyled(message, NTextStyle.primary3());
    }

    public static NMsg ofStyledPrimary4(NMsg message) {
        return ofStyled(message, NTextStyle.primary4());
    }

    public static NMsg ofStyledPrimary5(NMsg message) {
        return ofStyled(message, NTextStyle.primary5());
    }

    public static NMsg ofStyledPrimary6(NMsg message) {
        return ofStyled(message, NTextStyle.primary6());
    }

    public static NMsg ofStyledPrimary7(NMsg message) {
        return ofStyled(message, NTextStyle.primary7());
    }

    public static NMsg ofStyledPrimary8(NMsg message) {
        return ofStyled(message, NTextStyle.primary8());
    }

    public static NMsg ofStyledPrimary9(NMsg message) {
        return ofStyled(message, NTextStyle.primary9());
    }

    public static NMsg ofStyledSecondary1(NMsg message) {
        return ofStyled(message, NTextStyle.secondary1());
    }

    public static NMsg ofStyledSecondary2(NMsg message) {
        return ofStyled(message, NTextStyle.secondary2());
    }

    public static NMsg ofStyledSecondary3(NMsg message) {
        return ofStyled(message, NTextStyle.secondary3());
    }

    public static NMsg ofStyledSecondary4(NMsg message) {
        return ofStyled(message, NTextStyle.secondary4());
    }

    public static NMsg ofStyledSecondary5(NMsg message) {
        return ofStyled(message, NTextStyle.secondary5());
    }

    public static NMsg ofStyledSecondary6(NMsg message) {
        return ofStyled(message, NTextStyle.secondary6());
    }

    public static NMsg ofStyledSecondary7(NMsg message) {
        return ofStyled(message, NTextStyle.secondary7());
    }

    public static NMsg ofStyledSecondary8(NMsg message) {
        return ofStyled(message, NTextStyle.secondary8());
    }

    public static NMsg ofStyledSecondary9(NMsg message) {
        return ofStyled(message, NTextStyle.secondary9());
    }

    public static NMsg ofStyledTitle1(NMsg message) {
        return ofStyled(message, NTextStyle.title1());
    }

    public static NMsg ofStyledTitle2(NMsg message) {
        return ofStyled(message, NTextStyle.title2());
    }

    public static NMsg ofStyledTitle3(NMsg message) {
        return ofStyled(message, NTextStyle.title3());
    }

    public static NMsg ofStyledTitle4(NMsg message) {
        return ofStyled(message, NTextStyle.title4());
    }

    public static NMsg ofStyledTitle5(NMsg message) {
        return ofStyled(message, NTextStyle.title5());
    }

    public static NMsg ofStyledTitle6(NMsg message) {
        return ofStyled(message, NTextStyle.title6());
    }

    public static NMsg ofStyledTitle7(NMsg message) {
        return ofStyled(message, NTextStyle.title7());
    }

    public static NMsg ofStyledTitle8(NMsg message) {
        return ofStyled(message, NTextStyle.title8());
    }

    public static NMsg ofStyledTitle9(NMsg message) {
        return ofStyled(message, NTextStyle.title9());
    }

    public static NMsg ofStyledSuccess(NMsg message) {
        return ofStyled(message, NTextStyle.success());
    }

    public static NMsg ofStyledStriked(NMsg message) {
        return ofStyled(message, NTextStyle.striked());
    }

    public static NMsg ofStyledVariable(NMsg message) {
        return ofStyled(message, NTextStyle.variable());
    }

    public static NMsg ofStyledWarn(NMsg message) {
        return ofStyled(message, NTextStyle.warn());
    }

    public static NMsg ofStyledForegroundColor(NMsg message, int color) {
        return ofStyled(message, NTextStyle.foregroundColor(color));
    }

    public static NMsg ofStyledForegroundTrueColor(NMsg message, int color) {
        return ofStyled(message, NTextStyle.foregroundTrueColor(color));
    }

    public static NMsg ofStyledBackgroundColor(NMsg message, int color) {
        return ofStyled(message, NTextStyle.backgroundColor(color));
    }

    public static NMsg ofStyledBackgroundTrueColor(NMsg message, int color) {
        return ofStyled(message, NTextStyle.backgroundTrueColor(color));
    }

}

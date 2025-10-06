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

import net.thevpc.nuts.text.NMsgFormattable;
import net.thevpc.nuts.text.NTextFormattable;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.text.*;

import java.awt.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

public class NMsg implements NBlankable {

    public static final Object[] NO_PARAMS = new Object[0];
    private final String codeLang;
    private final Object message;
    private final Level level;
    private final NTextFormatType format;
    private final NMsgIntent intent;
    private final Object[] params;
    private final Function<String, ?> placeholderBindings;
    private final NTextStyles styles;
    private final Throwable throwable;
    private final long durationNano;

    public static Placeholder placeholder(String name) {
        NAssert.requireNonBlank(name, "name");
        return new Placeholder(name.trim());
    }

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
        return ofInvalidValue(null, (String) null);
    }

    public static NMsg ofInvalidValue(Throwable throwable) {
        return ofInvalidValue(throwable, (String) null);
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
            return ofC("invalid value : %s", NExceptions.getErrorMessage(throwable));
        }
        return ofC("invalid %s : %s", valueName, NExceptions.getErrorMessage(throwable));
    }

    public static NMsg ofInvalidValue(Throwable throwable, NMsg valueName) {
        if (throwable == null) {
            if (NBlankable.isBlank(valueName)) {
                return NMsg.ofPlain("invalid value");
            }
            return NMsg.ofC("invalid %s", valueName);
        }
        if (NBlankable.isBlank(valueName)) {
            return ofC("invalid value : %s", NExceptions.getErrorMessage(throwable));
        }
        return ofC("invalid %s : %s", valueName, NExceptions.getErrorMessage(throwable));
    }

    private static NMsg of(NTextFormatType format, Object message, Object[] params, NTextStyles styles, String codeLang, Level level, Throwable throwable, NMsgIntent intent, long time, Function<String, ?> placeholderBindings) {
        return new NMsg(format, message, params, styles, codeLang, level, throwable, intent, time, placeholderBindings);
    }

    private NMsg(NTextFormatType format, Object message, Object[] params, NTextStyles styles, String codeLang, Level level, Throwable throwable, NMsgIntent intent, long durationNano, Function<String, ?> placeholderBindings) {
        NAssert.requireNonNull(message, "message");
        NAssert.requireNonNull(format, "format");
        NAssert.requireNonNull(params, "params");
        this.level = level == null ? Level.INFO : level;
        this.format = format;
        this.throwable = throwable;
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
        this.intent = intent;
        this.durationNano = durationNano < 0 ? -1 : durationNano;
        this.placeholderBindings = placeholderBindings;
    }

    public static NMsg ofNtf(String message) {
        return of(NTextFormatType.NTF, NStringUtils.firstNonNull(message, ""), NO_PARAMS, null, null, null, null, null, -1, null);
    }

    public static NMsg ofCode(String lang, String text) {
        return of(NTextFormatType.CODE, NStringUtils.firstNonNull(text, ""), NO_PARAMS, null, lang, null, null, null, -1, null);
    }

    public static NMsg ofCode(String text) {
        return of(NTextFormatType.CODE, NStringUtils.firstNonNull(text, ""), NO_PARAMS, null, null, null, null, null, -1, null);
    }

    public static NMsg ofStringLiteral(String literal) {
        if (literal == null) {
            return NMsg.ofStyled("null", NTextStyle.primary1());
        }
        return NMsg.ofStyled(NStringUtils.formatStringLiteral(literal), NTextStyle.string());
    }

    public static NMsg ofStyled(String message, NTextStyle style) {
        return of(NTextFormatType.STYLED, NStringUtils.firstNonNull(message, ""), NO_PARAMS, style == null ? null : NTextStyles.of(style), null, null, null, null, -1, null);
    }

    public static NMsg ofStyled(String message, NTextStyles styles) {
        return of(NTextFormatType.STYLED, NStringUtils.firstNonNull(message, ""), NO_PARAMS, styles, null, null, null, null, -1, null);
    }

    public static NMsg ofStyled(NMsg message, NTextStyle style) {
        return of(NTextFormatType.STYLED, message, NO_PARAMS, style == null ? null : NTextStyles.of(style), null, null, null, null, -1, null);
    }

    public static NMsg ofStyled(NMsg message, NTextStyles styles) {
        return of(NTextFormatType.STYLED, message, NO_PARAMS, styles, null, null, null, null, -1, null);
    }

    public static NMsg ofStyled(NText message, NTextStyle style) {
        return of(NTextFormatType.STYLED, message, NO_PARAMS, style == null ? null : NTextStyles.of(style), null, null, null, null, -1, null);
    }

    public static NMsg ofStyled(NText message, NTextStyles styles) {
        return of(NTextFormatType.STYLED, message, NO_PARAMS, styles, null, null, null, null, -1, null);
    }

    public static NMsg ofNtf(NText message) {
        return of(NTextFormatType.NTF, message, NO_PARAMS, null, null, null, null, null, -1, null);
    }

    public static NMsg ofBlank() {
        return of(NTextFormatType.PLAIN, "", NO_PARAMS, null, null, null, null, null, -1, null);
    }

    public static NMsg ofPlain(String message) {
        return of(NTextFormatType.PLAIN, NStringUtils.firstNonNull(message, ""), NO_PARAMS, null, null, null, null, null, -1, null);
    }

    public static NMsg ofC(String message) {
        return of(NTextFormatType.CFORMAT, NStringUtils.firstNonNull(message, ""), NO_PARAMS, null, null, null, null, null, -1, null);
    }

    public static NMsg ofC(String message, Object... params) {
        return of(NTextFormatType.CFORMAT, NStringUtils.firstNonNull(message, ""), params, null, null, null, null, null, -1, null);
    }

    public static NMsg ofV(String message, NMsgParam... params) {
        if (params == null || params.length == 0) {
            return ofV(message, s -> null);
        }
        return ofV(message, new MapAsSupplier2(params));
    }

    public static NMsg ofV(String message, Map<String, ?> vars) {
        return of(NTextFormatType.VFORMAT, NStringUtils.firstNonNull(message, ""), new Object[]{vars}, null, null, null, null, null, -1, null);
    }

    public static NMsg ofV(String message, Function<String, ?> vars) {
        return of(NTextFormatType.VFORMAT, NStringUtils.firstNonNull(message, ""), new Object[]{vars}, null, null, null, null, null, -1, null);
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
        return of(NTextFormatType.JFORMAT, NStringUtils.firstNonNull(message, ""), NO_PARAMS, null, null, null, null, null, -1, null);
    }

    public static NMsg ofJ(String message, Object... params) {
        return of(NTextFormatType.JFORMAT, NStringUtils.firstNonNull(message, ""), params, null, null, null, null, null, -1, null);
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

    public Function<String, ?> getPlaceholders() {
        return placeholderBindings;
    }

    public Object[] getParams() {
        return params;
    }

    public String getCodeLang() {
        return codeLang;
    }

    public Level getNormalizedLevel() {
        if (level == null) {
            return Level.INFO;
        }
        int v = level.intValue();
        switch (v) {
            case Integer.MIN_VALUE:
                return Level.ALL;
            case 300:
                return Level.FINEST;
            case 400:
                return Level.FINER;
            case 500:
                return Level.FINE;
            case 700:
                return Level.CONFIG;
            case 800:
                return Level.INFO;
            case 900:
                return Level.WARNING;
            case 1000:
                return Level.SEVERE;
            case Integer.MAX_VALUE:
                return Level.OFF;
        }
        // Normalize arbitrary levels (301, 302, etc.) by bucketing intValue()/100
        switch (v / 100) {
            case 3:
                return Level.FINEST;  // 301-399
            case 4:
                return Level.FINER;    // 401-499
            case 5:
            case 6:
                return Level.FINE;     // 500-699
            case 7:
                return Level.CONFIG;   // 700-799
            case 8:
                return Level.INFO;     // 800-899
            case 9:
                return Level.WARNING;  // 900-999
            case 10:
                return Level.SEVERE;  // 1000+
            default: {
                if (v < Level.FINEST.intValue()) {
                    return Level.ALL;
                }
                return Level.SEVERE;
            }
        }
    }

    public boolean isError() {
        return level != null && level.intValue() >= Level.SEVERE.intValue() && level.intValue() < Integer.MAX_VALUE;
    }

    public boolean isWarning() {
        return level != null && level.intValue() >= Level.WARNING.intValue() && level.intValue() < Level.SEVERE.intValue();
    }

    public boolean isInfo() {
        return level == null || (level.intValue() >= Level.INFO.intValue() && level.intValue() < Level.WARNING.intValue());
    }

    public Level getLevel() {
        return level;
    }

    private Object _preFormatOne(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Placeholder) {
            if (placeholderBindings != null) {
                Object v = placeholderBindings.apply(((Placeholder) o).getName());
                if (v != null) {
                    o = v;
                }
            }
        }
        // this is to force calling synthetic suppliers
        if (o instanceof Supplier && o.getClass().isSynthetic()) {
            o = ((Supplier) o).get();
        }
        if (o instanceof NMsgSupplier) {
            o = ((NMsgSupplier) o).apply(this);
        }
        if (o instanceof NTextFormattable) {
            return ((NTextFormattable) o).toText();
        }
        if (o instanceof NMsgFormattable) {
            return ((NMsgFormattable) o).toMsg();
        }
        if (o instanceof NMsg) {
            return ((NMsg) o).withPlaceholders(placeholderBindings);
        }
        if (o instanceof Throwable) {
            return NExceptions.getErrorMessage((Throwable) o);
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

    public String toFullString() {
        if (throwable == null) {
            return toString();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this).append("\n").append(NStringUtils.stacktrace(throwable));
        return sb.toString();
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
                    return null;// return default
                }
        );
    }

    public NMsg asSevere() {
        return withLevelAndDefaultIntent(Level.SEVERE, NMsgIntent.FAIL);
    }

    public NMsg asError() {
        return withLevelAndDefaultIntent(Level.SEVERE, NMsgIntent.FAIL);
    }

    public NMsg asError(Throwable throwable) {
        return withLevelAndDefaultIntent(Level.SEVERE, NMsgIntent.FAIL, throwable);
    }

    public NMsg asErrorAlert() {
        return withLevelAndIntent(Level.SEVERE, NMsgIntent.ALERT);
    }

    public NMsg asErrorAlert(Throwable throwable) {
        return withLevelAndIntent(Level.SEVERE, NMsgIntent.ALERT, throwable);
    }

    public NMsg asSevere(Throwable throwable) {
        return withLevelAndDefaultIntent(Level.SEVERE, NMsgIntent.FAIL, throwable);
    }

    public NMsg asWarning(Throwable throwable) {
        return withLevelAndDefaultIntent(Level.WARNING, NMsgIntent.ALERT, throwable);
    }

    public NMsg asFine(Throwable throwable) {
        return withLevelAndDefaultIntent(Level.FINE, NMsgIntent.DEBUG, throwable);
    }

    public NMsg asFinest(Throwable throwable) {
        return withLevelAndDefaultIntent(Level.FINEST, NMsgIntent.DEBUG, throwable);
    }

    private NMsg asLevelWithThrowable(Level level, Throwable throwable) {
        if (level == null) {
            level = Level.FINEST;
        }
        if (level == this.level && throwable == this.throwable) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, throwable, intent, durationNano, placeholderBindings);
    }


    public NMsg asInfo() {
        return withLevelAndDefaultIntent(Level.INFO, NMsgIntent.NOTICE);
    }

    public NMsg asConfig() {
        return withLevelAndDefaultIntent(Level.CONFIG, NMsgIntent.INIT);
    }

    public NMsg asWarning() {
        return withLevelAndDefaultIntent(Level.WARNING, NMsgIntent.ALERT);
    }

    public NMsg asFinest() {
        return withLevelAndDefaultIntent(Level.FINEST, NMsgIntent.DEBUG);
    }

    public NMsg asFinestFail() {
        return withLevelAndDefaultIntent(Level.FINEST, NMsgIntent.FAIL);
    }

    public NMsg asFineFail() {
        return withLevelAndIntent(Level.FINE, NMsgIntent.FAIL);
    }

    public NMsg asFinestFail(Throwable throwable) {
        return withLevelAndIntent(Level.FINEST, NMsgIntent.FAIL, throwable);
    }

    public NMsg asFineFail(Throwable throwable) {
        return withLevelAndIntent(Level.FINE, NMsgIntent.FAIL, throwable);
    }

    public NMsg asInfoFail(Throwable throwable) {
        return withLevelAndIntent(Level.INFO, NMsgIntent.FAIL, throwable);
    }

    public NMsg asInfoFail() {
        return withLevelAndIntent(Level.INFO, NMsgIntent.FAIL);
    }

    public NMsg asFinerFail(Throwable throwable) {
        return withLevelAndIntent(Level.FINER, NMsgIntent.FAIL, throwable);
    }

    public NMsg asFinerFail() {
        return withLevelAndIntent(Level.FINER, NMsgIntent.FAIL);
    }


    public NMsg asWarningFail(Throwable throwable) {
        return withLevelAndIntent(Level.WARNING, NMsgIntent.FAIL, throwable);
    }

    public NMsg asWarningFail() {
        return withLevelAndIntent(Level.WARNING, NMsgIntent.FAIL);
    }


    public NMsg asFinestAlert() {
        return withLevelAndDefaultIntent(Level.FINEST, NMsgIntent.ALERT);
    }

    public NMsg asFineAlert() {
        return withLevelAndIntent(Level.FINE, NMsgIntent.ALERT);
    }

    public NMsg asFinestAlert(Throwable throwable) {
        return withLevelAndIntent(Level.FINEST, NMsgIntent.ALERT, throwable);
    }

    public NMsg asFineAlert(Throwable throwable) {
        return withLevelAndIntent(Level.FINE, NMsgIntent.ALERT, throwable);
    }

    public NMsg asInfoAlert(Throwable throwable) {
        return withLevelAndIntent(Level.INFO, NMsgIntent.ALERT, throwable);
    }

    public NMsg asInfoAlert() {
        return withLevelAndIntent(Level.INFO, NMsgIntent.ALERT);
    }

    public NMsg asFinerAlert(Throwable throwable) {
        return withLevelAndIntent(Level.FINER, NMsgIntent.ALERT, throwable);
    }

    public NMsg asFinerAlert() {
        return withLevelAndIntent(Level.FINER, NMsgIntent.ALERT);
    }


    public NMsg asWarningAlert(Throwable throwable) {
        return withLevelAndIntent(Level.WARNING, NMsgIntent.ALERT, throwable);
    }

    public NMsg asWarningAlert() {
        return withLevelAndIntent(Level.WARNING, NMsgIntent.ALERT);
    }


    public NMsg asFine() {
        return withLevelAndDefaultIntent(Level.FINE, NMsgIntent.DEBUG);
    }

    public NMsg asFiner() {
        return withLevelAndDefaultIntent(Level.FINER, NMsgIntent.DEBUG);
    }

    public NMsg asDebug() {
        return withLevelAndDefaultIntent(Level.FINEST, NMsgIntent.DEBUG);
    }

    public NMsg withoutPlaceholders() {
        return of(format, message, params, styles, codeLang, level, null, intent, durationNano, null);
    }

    public NMsg withPlaceholders(Function<String, ?> placeholderSupplier) {
        if (placeholderSupplier == null) {
            return this;
        }
        Function<String, ?> oldPlaceholderBindings = placeholderBindings;
        return of(format, message, params, styles, codeLang, level, null, intent, durationNano, s -> {
            Object r = placeholderSupplier.apply(s);
            if (r != null) {
                return r;
            }
            if (oldPlaceholderBindings != null) {
                return oldPlaceholderBindings.apply(s);
            }
            return null;
        });
    }

    public NMsg withPlaceholders(NMsgParam... params) {
        if (params == null || params.length == 0) {
            return this;
        }
        if (placeholderBindings == null) {
            return of(format, message, params, styles, codeLang, level, null, intent, durationNano, new MapAsSupplier2(params));
        }
        if (placeholderBindings instanceof MapAsSupplier2) {
            Map<String, Supplier<?>> newMap = new LinkedHashMap<>(((MapAsSupplier2) placeholderBindings).content);
            for (NMsgParam param : params) {
                NAssert.requireNonNull(param, "param");
                NAssert.requireNonNull(param.getName(), "param.name");
                newMap.put(param.getName(), new ConstSupplier<>(param.getValue()));
            }
            return of(format, message, params, styles, codeLang, level, null, intent, durationNano, new MapAsSupplier2(newMap));
        }
        if (placeholderBindings instanceof MapAsSupplier) {
            Map<String, Supplier<?>> newMap = new LinkedHashMap<>();
            for (Map.Entry<String, ?> e : ((MapAsSupplier) placeholderBindings).content.entrySet()) {
                newMap.put(e.getKey(), e::getValue);
            }
            for (NMsgParam param : params) {
                NAssert.requireNonNull(param, "param");
                NAssert.requireNonNull(param.getName(), "param.name");
                newMap.put(param.getName(), new ConstSupplier<>(param.getValue()));
            }
            return of(format, message, params, styles, codeLang, level, null, intent, durationNano, new MapAsSupplier2(newMap));
        }
        MapAsSupplier2 p2 = new MapAsSupplier2(params);
        Function<String, ?> oldPlaceholderBindings = placeholderBindings;
        return of(format, message, params, styles, codeLang, level, null, intent, durationNano, s -> {
            if (p2.content.containsKey(s)) {
                return p2.apply(s);
            }
            if (oldPlaceholderBindings != null) {
                return oldPlaceholderBindings.apply(s);
            }
            return null;
        });
    }

    public NMsg withPlaceholder(String key, Object value) {
        return withPlaceholders(NMaps.of(key, value));
    }

    public NMsg withPlaceholders(Map<String, ?> placeholderMap) {
        if (placeholderMap == null) {
            return this;
        }
        if (placeholderBindings == null) {
            return of(format, message, params, styles, codeLang, level, null, intent, durationNano, new MapAsSupplier(new LinkedHashMap<>(placeholderMap)));
        }
        if (placeholderBindings instanceof MapAsSupplier2) {
            Map<String, Supplier<?>> newMap = new LinkedHashMap<>(((MapAsSupplier2) placeholderBindings).content);
            for (Map.Entry<String, ?> e : placeholderMap.entrySet()) {
                NAssert.requireNonNull(e.getKey(), "param.name");
                newMap.put(e.getKey(), new ConstSupplier<>(e.getValue()));
            }
            return of(format, message, params, styles, codeLang, level, null, intent, durationNano, new MapAsSupplier2(newMap));
        }
        if (placeholderBindings instanceof MapAsSupplier) {
            Map<String, Object> newMap = new LinkedHashMap<>(((MapAsSupplier) placeholderBindings).content);
            for (Map.Entry<String, ?> e : placeholderMap.entrySet()) {
                Object v = e.getValue();
                if (v == null) {
                    newMap.remove(e.getKey());
                } else {
                    newMap.put(e.getKey(), v);
                }
            }
            return of(format, message, params, styles, codeLang, level, null, intent, durationNano, new MapAsSupplier(newMap));
        }
        Function<String, ?> oldPlaceholderBindings = placeholderBindings;
        return of(format, message, params, styles, codeLang, level, null, intent, durationNano, s -> {
            if (placeholderMap.containsKey(s)) {
                return placeholderMap.get(s);
            }
            if (oldPlaceholderBindings != null) {
                return oldPlaceholderBindings.apply(s);
            }
            return null;
        });
    }

    public NMsg withLevel(Level level) {
        if (level == this.level) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, null, intent, durationNano, placeholderBindings);
    }

    private NMsg withLevelAndDefaultIntent(Level level, NMsgIntent intent) {
        if (this.intent != null) {
            intent = this.intent;
        }
        if (level == this.level && Objects.equals(intent, this.intent)) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, null, intent, durationNano, placeholderBindings);
    }

    private NMsg withLevelAndIntent(Level level, NMsgIntent intent) {
        if (level == this.level && Objects.equals(intent, this.intent)) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, null, intent, durationNano, placeholderBindings);
    }

    private NMsg withLevelAndDefaultIntent(Level level, NMsgIntent intent, Throwable throwable) {
        if (this.intent != null) {
            intent = this.intent;
        }
        if (level == this.level && Objects.equals(intent, this.intent) && this.throwable == throwable) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, throwable, intent, durationNano, placeholderBindings);
    }

    private NMsg withLevelAndIntent(Level level, NMsgIntent intent, Throwable throwable) {
        if (level == this.level && Objects.equals(intent, this.intent) && this.throwable == throwable) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, throwable, intent, durationNano, placeholderBindings);
    }

    public NMsg withIntent(NMsgIntent intent) {
        if (Objects.equals(intent, this.intent)) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, null, intent, durationNano, placeholderBindings);
    }

    public NMsg withDefaultIntent(NMsgIntent intent) {
        if (this.intent != intent) {
            return this;
        }
        if (Objects.equals(intent, this.intent)) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, null, intent, durationNano, placeholderBindings);
    }

    public NMsg withThrowable(Throwable throwable) {
        if (throwable == this.throwable) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, throwable, intent, durationNano, placeholderBindings);
    }

    public NMsg withDurationMillis(long elapsedTimeMillis) {
        if (elapsedTimeMillis < 0) {
            elapsedTimeMillis = -1;
        }
        return withDurationNanos(elapsedTimeMillis * 1000000L);
    }

    public NMsg withDurationNanos(long elapsedTimeNanos) {
        if (elapsedTimeNanos < 0) {
            elapsedTimeNanos = -1;
        }
        if (elapsedTimeNanos == this.durationNano) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, throwable, intent, elapsedTimeNanos, placeholderBindings);
    }

    public NMsg withPrefix(NMsg prefixMessage) {
        if (NBlankable.isBlank(prefixMessage)) {
            return this;
        }
        if (NBlankable.isBlank(this)) {
            return prefixMessage;
        }
        //this if fast way to inherit level,intent, duration and throwable
        return of(NTextFormatType.CFORMAT, "%s %s", new Object[]{prefixMessage, cloneWithoutMeta()}, null, null, level, throwable, intent, durationNano, null);
    }

    public NMsg withSuffix(NMsg suffixMessage) {
        if (NBlankable.isBlank(suffixMessage)) {
            return this;
        }
        if (NBlankable.isBlank(this)) {
            return suffixMessage;
        }
        //this if fast way to inherit level,intent, duration and throwable
        return of(NTextFormatType.CFORMAT, "%s %s", new Object[]{cloneWithoutMeta(), suffixMessage}, null, null, level, throwable, intent, durationNano, null);
    }

    public NMsg withPrefix(NMsgSupplier<NMsg> prefixMessage) {
        if (prefixMessage == null) {
            return this;
        }
        //this if fast way to inherit level,intent, duration and throwable
        Supplier<NMsg> prefixSupplier = () -> prefixMessage.apply(this /**/);
        return of(NTextFormatType.CFORMAT, "%s %s", new Object[]{prefixSupplier, cloneWithoutMeta()}, null, null, level, throwable, intent, durationNano, null);
    }

    public NMsg withSuffix(NMsgSupplier<NMsg> suffixMessage) {
        if (NBlankable.isBlank(suffixMessage)) {
            return this;
        }
        //this if fast way to inherit level,intent, duration and throwable
        Supplier<NMsg> suffixSupplier = () -> suffixMessage.apply(this /**/);
        return of(NTextFormatType.CFORMAT, "%s %s", new Object[]{cloneWithoutMeta(), suffixSupplier}, null, null, level, throwable, intent, durationNano, null);
    }

    private NMsg cloneWithoutMeta() {
        return of(format, message, params, styles, codeLang, null, null, null, -1, placeholderBindings);
    }

    // ---------------------------------------------------------------
    // STYLING
    // ---------------------------------------------------------------

    /**
     * @return -1 if not specified
     */
    public long getDurationNanos() {
        return durationNano;
    }

    /**
     * @return -1 if not specified
     */
    public long getDurationMillis() {
        return durationNano < 0 ? -1 : durationNano / 1000000L;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public NMsgIntent getIntent() {
        return intent;
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
                && Objects.equals(throwable, that.throwable)
                ;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(codeLang, message, format, styles, level, throwable);
        result = 31 * result + Arrays.hashCode(params);
        return result;
    }


    // ---------------------------------------------------------------
    // STYLING
    // ---------------------------------------------------------------

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

    public static NMsg ofStyledForegroundTrueColor(String message, NColor color) {
        return ofStyled(message, NTextStyle.foregroundTrueColor(color.toColor()));
    }

    public static NMsg ofStyledForegroundTrueColor(NMsg message, NColor color) {
        return ofStyled(message, NTextStyle.foregroundTrueColor(color));
    }

    public static NMsg ofStyledForegroundTrueColor(String message, Color color) {
        return ofStyled(message, NTextStyle.foregroundTrueColor(color));
    }

    public static NMsg ofStyledForegroundTrueColor(NMsg message, Color color) {
        return ofStyled(message, NTextStyle.foregroundTrueColor(color));
    }

    public static NMsg ofStyledBackgroundTrueColor(String message, NColor color) {
        return ofStyled(message, NTextStyle.backgroundTrueColor(color.toColor()));
    }

    public static NMsg ofStyledBackgroundTrueColor(NMsg message, NColor color) {
        return ofStyled(message, NTextStyle.backgroundTrueColor(color));
    }

    public static NMsg ofStyledBackgroundTrueColor(String message, Color color) {
        return ofStyled(message, NTextStyle.backgroundTrueColor(color));
    }

    public static NMsg ofStyledBackgroundTrueColor(NMsg message, Color color) {
        return ofStyled(message, NTextStyle.backgroundTrueColor(color));
    }

    public static NMsg ofStyledBackgroundColor(String message, NColor color) {
        return ofStyled(message, NTextStyle.backgroundColor(color));
    }

    public static NMsg ofStyledBackgroundColor(NMsg message, NColor color) {
        return ofStyled(message, NTextStyle.backgroundColor(color));
    }

    public static NMsg ofStyledForegroundColor(String message, NColor color) {
        return ofStyled(message, NTextStyle.foregroundColor(color));
    }

    public static NMsg ofStyledForegroundColor(NMsg message, NColor color) {
        return ofStyled(message, NTextStyle.foregroundColor(color));
    }

    public static NMsg ofStyledBackgroundColor(String message, Color color) {
        return ofStyled(message, NTextStyle.backgroundTrueColor(color));
    }

    public static NMsg ofStyledBackgroundColor(NMsg message, Color color) {
        return ofStyled(message, NTextStyle.backgroundTrueColor(color));
    }

    public static NMsg ofStyledForegroundColor(String message, Color color) {
        return ofStyled(message, NTextStyle.backgroundTrueColor(color));
    }

    public static NMsg ofStyledForegroundColor(NMsg message, Color color) {
        return ofStyled(message, NTextStyle.backgroundTrueColor(color));
    }

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

    // ---------------------------------------------------------------
    // PRIVATE CLASSES
    // ---------------------------------------------------------------

    public static final class Placeholder {
        private String name;

        private Placeholder(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Placeholder that = (Placeholder) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }

        @Override
        public String toString() {
            return "placeholder(" + name + ")";
        }
    }

    private static class MapAsSupplier implements Function<String, Object> {
        Map<String, ?> content;

        public MapAsSupplier(Map<String, ?> other) {
            this.content = other;
        }

        @Override
        public Object apply(String ker) {
            return content.get(ker);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            MapAsSupplier that = (MapAsSupplier) o;
            return Objects.equals(content, that.content);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(content);
        }

        @Override
        public String toString() {
            return "MapAsSupplier{" +
                    "content=" + content +
                    '}';
        }
    }

    @Override
    public boolean isBlank() {
        if (message == null) {
            return true;
        }
        switch (format) {
            case PLAIN:
            case JFORMAT:
            case VFORMAT:
            case CFORMAT:
            case CODE:
                return NStringUtils.isEmpty((String) message);
            case STYLED:
            case NTF: {
                if (message instanceof NMsg) {
                    NMsg m = (NMsg) message;
                    return m == null || m.isBlank();
                }
                if (message instanceof NText) {
                    NText m = (NText) message;
                    return m.isBlank();
                }
                if (message instanceof String) {
                    return NStringUtils.isEmpty((String) message);
                }
                return false;
            }
        }
        return false;
    }

    private static class ConstSupplier<T> implements Supplier<T> {
        private T value;

        public ConstSupplier(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }
    }

    private static class MapAsSupplier2 implements Function<String, Object> {
        Map<String, Supplier<?>> content;

        public MapAsSupplier2(Map<String, Supplier<?>> other) {
            this.content = other;
        }

        public MapAsSupplier2(NMsgParam... params) {
            this.content = new LinkedHashMap<>();
            if (params != null) {
                for (NMsgParam param : params) {
                    NAssert.requireNonNull(param, "param");
                    String e = param.getName();
                    NAssert.requireNonNull(e, "param.name");
                    if (content.containsKey(e)) {
                        throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC("duplicate key %s", e));
                    }
                    content.put(e, new ConstSupplier<>(param.getValue()));
                }
            }
        }

        @Override
        public Object apply(String key) {
            Supplier<?> p = content.get(key);
            if (p != null) {
                return p.get();
            }
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            MapAsSupplier2 that = (MapAsSupplier2) o;
            return Objects.equals(content, that.content);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(content);
        }

        @Override
        public String toString() {
            return "MapAsSupplier2{" +
                    "content=" + content +
                    '}';
        }
    }
}

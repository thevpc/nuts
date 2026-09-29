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
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.text;

import net.thevpc.nuts.elem.NElementSimple;
import net.thevpc.nuts.internal.rpi.NTextRPI;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.util.*;

import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.collections.NMaps;

/**
 * NMsg class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NMsg implements NBlankable, NElementSimple {

    public static final Object[] NO_PARAMS = new Object[0];
    private final String codeLang;
    private final Object message;
    private final Level level;
    private final NMsgType format;
    private final NMsgIntent intent;
    private final Object[] params;
    private final Function<String, ?> placeholderBindings;
    private final NTextStyles styles;
    private final Throwable throwable;
    private final NDuration duration;
    private final String customFormatId;
    private final boolean ntf;

    /**
     * Placeholder.
     *
     * @param name name
     * @return placeholder result
     */
    public static Placeholder placeholder(String name) {
        NAssert.requireNamedNonBlank(name, "name");
        return new Placeholder(NStringUtils.strip(name));
    }

    /**
     * Creates a new instance of missing value.
     *
     * @return of missing value result
     */
    public static NMsg ofMissingValue() {
        return ofMissingValue((String) null);
    }

    /**
     * Creates a new instance of missing value.
     *
     * @param valueName value name
     * @return of missing value result
     */
    public static NMsg ofMissingValue(String valueName) {
        if (NBlankable.isBlank(valueName)) {
            return NMsg.ofP("missing value");
        }
        return NMsg.ofC("missing %s", valueName);
    }

    /**
     * Creates a new instance of missing value.
     *
     * @param valueName value name
     * @return of missing value result
     */
    public static NMsg ofMissingValue(NMsg valueName) {
        if (NBlankable.isBlank(valueName)) {
            return NMsg.ofP("missing value");
        }
        return NMsg.ofC("missing %s", valueName);
    }

    /**
     * Creates a new instance of invalid value.
     *
     * @return of invalid value result
     */
    public static NMsg ofInvalidValue() {
        return ofInvalidValue(null, (String) null);
    }

    /**
     * Creates a new instance of invalid value.
     *
     * @param throwable throwable
     * @return of invalid value result
     */
    public static NMsg ofInvalidValue(Throwable throwable) {
        return ofInvalidValue(throwable, (String) null);
    }

    /**
     * Creates a new instance of invalid value.
     *
     * @param valueName value name
     * @return of invalid value result
     */
    public static NMsg ofInvalidValue(String valueName) {
        return ofInvalidValue(null, valueName);
    }

    /**
     * Creates a new instance of invalid value.
     *
     * @param throwable throwable
     * @param valueName value name
     * @return of invalid value result
     */
    public static NMsg ofInvalidValue(Throwable throwable, String valueName) {
        if (throwable == null) {
            if (NBlankable.isBlank(valueName)) {
                return NMsg.ofP("invalid value");
            }
            return NMsg.ofC("invalid %s", valueName);
        }
        if (NBlankable.isBlank(valueName)) {
            return ofC("invalid value : %s", NException.getErrorMessage(throwable));
        }
        return ofC("invalid %s : %s", valueName, NException.getErrorMessage(throwable));
    }

    /**
     * Creates a new instance of invalid value.
     *
     * @param throwable throwable
     * @param valueName value name
     * @return of invalid value result
     */
    public static NMsg ofInvalidValue(Throwable throwable, NMsg valueName) {
        if (throwable == null) {
            if (NBlankable.isBlank(valueName)) {
                return NMsg.ofP("invalid value");
            }
            return NMsg.ofC("invalid %s", valueName);
        }
        if (NBlankable.isBlank(valueName)) {
            return ofC("invalid value : %s", NException.getErrorMessage(throwable));
        }
        return ofC("invalid %s : %s", valueName, NException.getErrorMessage(throwable));
    }

    /**
     * Creates a new instance.
     *
     * @param format format
     * @param message message
     * @param params params
     * @param styles styles
     * @param codeLang code lang
     * @param level level
     * @param throwable throwable
     * @param intent intent
     * @param duration duration
     * @param placeholderBindings placeholder bindings
     * @param customFormatId custom format id
     * @param ntf ntf
     * @return of result
     */
    private static NMsg of(NMsgType format, Object message, Object[] params, NTextStyles styles, String codeLang, Level level, Throwable throwable, NMsgIntent intent, NDuration duration, Function<String, ?> placeholderBindings, String customFormatId, boolean ntf) {
        return new NMsg(format, message, params, styles, codeLang, level, throwable, intent, duration, placeholderBindings, customFormatId, ntf);
    }

    /**
     * N msg.
     *
     * @param format format
     * @param message message
     * @param params params
     * @param styles styles
     * @param codeLang code lang
     * @param level level
     * @param throwable throwable
     * @param intent intent
     * @param duration duration
     * @param placeholderBindings placeholder bindings
     * @param customFormatId custom format id
     * @param ntf ntf
     * @return n msg result
     */
    private NMsg(NMsgType format, Object message, Object[] params, NTextStyles styles, String codeLang, Level level, Throwable throwable, NMsgIntent intent, NDuration duration, Function<String, ?> placeholderBindings, String customFormatId, boolean ntf) {
        NAssert.requireNamedNonNull(message, "message");
        NAssert.requireNamedNonNull(format, "format");
        NAssert.requireNamedNonNull(params, "params");
        this.level = level == null ? Level.INFO : level;
        this.format = format;
        this.ntf = ntf;
        this.throwable = throwable;
        this.styles = styles;
        if (format == NMsgType.PLAIN
                || format == NMsgType.STYLED
                || format == NMsgType.CODE) {
            if (params.length > 0) {
                throw new IllegalArgumentException("arguments are not supported for " + format);
            }
        }
        if (format == NMsgType.CUSTOM) {
            NAssert.requireNamedNonBlank(customFormatId, "customFormatId");
            NAssert.requireNamedNonNull(params, "params");
            this.customFormatId = NStringUtils.strip(customFormatId);
        } else {
            this.customFormatId = customFormatId;
        }
        if (format == NMsgType.STYLED) {
            NAssert.requireNamedNonNull(styles, "styles for " + format);
        } else {
            NAssert.requireNamedNull(styles, "styles for " + format + " (not supported)");
        }
        this.codeLang = NStringUtils.stripToNull(codeLang);
        this.message = message;
        this.params = params;
        this.intent = intent;
        this.duration = duration;
        this.placeholderBindings = placeholderBindings;
    }

    /**
     * Creates a new instance of ntf.
     *
     * @param message message
     * @return of ntf result
     */
    public static NMsg ofNtf(String message) {
        return of(NMsgType.PLAIN, NStringUtils.firstNonNull(message, ""), NO_PARAMS, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of code.
     *
     * @param lang lang
     * @param text text
     * @return of code result
     */
    public static NMsg ofCode(String lang, String text) {
        return of(NMsgType.CODE, NStringUtils.firstNonNull(text, ""), NO_PARAMS, null, lang, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of code.
     *
     * @param text text
     * @return of code result
     */
    public static NMsg ofCode(String text) {
        return of(NMsgType.CODE, NStringUtils.firstNonNull(text, ""), NO_PARAMS, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of string literal.
     *
     * @param literal literal
     * @return of string literal result
     */
    public static NMsg ofStringLiteral(String literal) {
        if (literal == null) {
            return NMsg.ofStyled("null", NTextStyle.primary1());
        }
        return NMsg.ofStyled(NStringUtils.formatStringLiteral(literal), NTextStyle.string());
    }

    /**
     * Creates a new instance of styled.
     *
     * @param message message
     * @param style style
     * @return of styled result
     */
    public static NMsg ofStyled(String message, NTextStyle style) {
        return of(NMsgType.STYLED, NStringUtils.firstNonNull(message, ""), NO_PARAMS, style == null ? null : NTextStyles.of(style), null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of styled.
     *
     * @param message message
     * @param styles styles
     * @return of styled result
     */
    public static NMsg ofStyled(String message, NTextStyles styles) {
        return of(NMsgType.STYLED, NStringUtils.firstNonNull(message, ""), NO_PARAMS, styles, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of styled.
     *
     * @param message message
     * @param style style
     * @return of styled result
     */
    public static NMsg ofStyled(NMsg message, NTextStyle style) {
        return of(NMsgType.STYLED, message, NO_PARAMS, style == null ? null : NTextStyles.of(style), null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of styled.
     *
     * @param message message
     * @param styles styles
     * @return of styled result
     */
    public static NMsg ofStyled(NMsg message, NTextStyles styles) {
        return of(NMsgType.STYLED, message, NO_PARAMS, styles, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of styled.
     *
     * @param message message
     * @param style style
     * @return of styled result
     */
    public static NMsg ofStyled(NText message, NTextStyle style) {
        return of(NMsgType.STYLED, message, NO_PARAMS, style == null ? null : NTextStyles.of(style), null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of styled.
     *
     * @param message message
     * @param styles styles
     * @return of styled result
     */
    public static NMsg ofStyled(NText message, NTextStyles styles) {
        return of(NMsgType.STYLED, message, NO_PARAMS, styles, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of ntf.
     *
     * @param message message
     * @return of ntf result
     */
    public static NMsg ofNtf(NText message) {
        return of(NMsgType.PLAIN, message, NO_PARAMS, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of blank.
     *
     * @return of blank result
     */
    public static NMsg ofBlank() {
        return of(NMsgType.PLAIN, "", NO_PARAMS, null, null, null, null, null, null, null, null, false);
    }

    /**
     * Creates a new instance of plain.
     *
     * @param message message
     * @return of plain result
     */
    public static NMsg ofP(String message) {
        return of(NMsgType.PLAIN, NStringUtils.firstNonNull(message, ""), NO_PARAMS, null, null, null, null, null, null, null, null, false);
    }

    /**
     * Creates a new instance of c.
     *
     * @param message message
     * @return of c result
     */
    public static NMsg ofC(String message) {
        return of(NMsgType.CFORMAT, NStringUtils.firstNonNull(message, ""), NO_PARAMS, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of c.
     *
     * @param message message
     * @param params params
     * @return of c result
     */
    public static NMsg ofC(String message, Object... params) {
        return of(NMsgType.CFORMAT, NStringUtils.firstNonNull(message, ""), params, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of v.
     *
     * @param message message
     * @param params params
     * @return of v result
     */
    public static NMsg ofV(String message, NMsgParam... params) {
        if (params == null || params.length == 0) {
            return ofV(message, s -> null);
        }
        return ofV(message, new MapAsSupplier2(params));
    }

    /**
     * Creates a new instance of v.
     *
     * @param message message
     * @param vars vars
     * @return of v result
     */
    public static NMsg ofV(String message, Map<String, ?> vars) {
        return of(NMsgType.VFORMAT, NStringUtils.firstNonNull(message, ""), new Object[]{vars}, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of v.
     *
     * @param message message
     * @param vars vars
     * @return of v result
     */
    public static NMsg ofV(String message, Function<String, ?> vars) {
        return of(NMsgType.VFORMAT, NStringUtils.firstNonNull(message, ""), new Object[]{vars}, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of m.
     *
     * @param message message
     * @param params params
     * @return of m result
     */
    public static NMsg ofM(String message, NMsgParam... params) {
        if (params == null || params.length == 0) {
            return ofM(message, s -> null);
        }
        return ofM(message, new MapAsSupplier2(params));
    }

    /**
     * Creates a new instance of m.
     *
     * @param message message
     * @param vars vars
     * @return of m result
     */
    public static NMsg ofM(String message, Map<String, ?> vars) {
        return of(NMsgType.MFORMAT, NStringUtils.firstNonNull(message, ""), new Object[]{vars}, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of m.
     *
     * @param message message
     * @param vars vars
     * @return of m result
     */
    public static NMsg ofM(String message, Function<String, ?> vars) {
        return of(NMsgType.MFORMAT, NStringUtils.firstNonNull(message, ""), new Object[]{vars}, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of j.
     *
     * @param message message
     * @param params params
     * @return of j result
     */
    public static NMsg ofJ(String message, NMsgParam... params) {
        if (params == null) {
            return ofJ(message, new Object[]{null});
        }
        Object[] paramsAsObjects = Arrays.stream(params).map(NMsgParam::value).toArray();
        return ofJ(message, paramsAsObjects);
    }

    /**
     * Creates a new instance of c.
     *
     * @param message message
     * @param params params
     * @return of c result
     */
    public static NMsg ofC(String message, NMsgParam... params) {
        if (params == null) {
            return ofC(message, new Object[]{null});
        }
        Object[] paramsAsObjects = Arrays.stream(params).map(NMsgParam::value).toArray();
        return ofC(message, paramsAsObjects);
    }

    /**
     * Creates a new instance of j.
     *
     * @param message message
     * @return of j result
     */
    public static NMsg ofJ(String message) {
        return of(NMsgType.JFORMAT, NStringUtils.firstNonNull(message, ""), NO_PARAMS, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of j.
     *
     * @param message message
     * @param params params
     * @return of j result
     */
    public static NMsg ofJ(String message, Object... params) {
        return of(NMsgType.JFORMAT, NStringUtils.firstNonNull(message, ""), params, null, null, null, null, null, null, null, null, true);
    }

    /**
     * defaults to no ntf because usually used in SQL
     *
     * @param sql    sql template
     * @param params sql params in '?' or ':param' format
     * @return new NMsg instance
     */
    public static NMsg ofS(String sql, Object... params) {
        return of(NMsgType.SFORMAT, NStringUtils.firstNonNull(sql, ""), params, null, null, null, null, null, null, null, null, false);
    }

    /**
     * defaults to no ntf because usually used in SQL
     *
     * @param sql         sql template
     * @param namedParams sql params in '?' or ':param' format
     * @return new NMsg instance
     */
    public static NMsg ofS(String sql, Map<String, ?> namedParams) {
        return of(NMsgType.SFORMAT, NStringUtils.firstNonNull(sql, ""), NO_PARAMS, null, null, null, null, null, null,
                namedParams == null ? null : namedParams::get, null, false);
    }

    /**
     * defaults to no ntf because usually used in SQL
     *
     * @param sql         sql template
     * @param namedParams sql params in '?' or ':param' format
     * @return new NMsg instance
     */
    public static NMsg ofS(String sql, Function<String, ?> namedParams) {
        return of(NMsgType.SFORMAT, NStringUtils.firstNonNull(sql, ""), NO_PARAMS, null, null, null, null, null, null, namedParams, null, false);
    }

    /**
     * defaults to no ntf because usually used in SQL
     *
     * @param sql    sql template
     * @param params sql params in '?' or ':param' format
     * @return new NMsg instance
     */
    public static NMsg ofS(String sql, NMsgParam... params) {
        return ofS(sql, new MapAsSupplier2(params)); // reuse existing named-lookup plumbing
    }

    /**
     * Format.
     *
     * @return format result
     */
    public NMsgType format() {
        return format;
    }

    /**
     * Styles.
     *
     * @return styles result
     */
    public NTextStyles styles() {
        return styles;
    }

    /**
     * Message.
     *
     * @return message result
     */
    public Object message() {
        return message;
    }

    /**
     * Placeholders.
     *
     * @return placeholders result
     */
    public Function<String, ?> placeholders() {
        return placeholderBindings;
    }

    /**
     * Params.
     *
     * @return params result
     */
    public Object[] params() {
        return params == null ? null : Arrays.copyOf(params, params.length);
    }

    /**
     * Code lang.
     *
     * @return code lang result
     */
    public String codeLang() {
        return codeLang;
    }

    /**
     * Normalized level.
     *
     * @return normalized level result
     */
    public Level normalizedLevel() {
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

    /**
     * Checks if is error.
     *
     * @return is error result
     */
    public boolean isError() {
        return level != null && level.intValue() >= Level.SEVERE.intValue() && level.intValue() < Integer.MAX_VALUE;
    }

    /**
     * Checks if is warning.
     *
     * @return is warning result
     */
    public boolean isWarning() {
        return level != null && level.intValue() >= Level.WARNING.intValue() && level.intValue() < Level.SEVERE.intValue();
    }

    /**
     * Checks if is info.
     *
     * @return is info result
     */
    public boolean isInfo() {
        return level == null || (level.intValue() >= Level.INFO.intValue() && level.intValue() < Level.WARNING.intValue());
    }

    /**
     * Level.
     *
     * @return level result
     */
    public Level level() {
        return level;
    }

    /**
     * _pre format one.
     *
     * @param o o
     * @param plain plain
     * @return _pre format one result
     */
    private Object _preFormatOne(Object o, boolean plain) {
        if (o == null) {
            return null;
        }
        if (o instanceof Placeholder) {
            if (placeholderBindings != null) {
                Object v = placeholderBindings.apply(((Placeholder) o).name());
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
            o = ((NTextFormattable) o).toText();
        } else if (o instanceof NMsgFormattable) {
            o = ((NMsgFormattable) o).toMsg();
        } else if (o instanceof NMsg) {
            o = ((NMsg) o).withPlaceholders(placeholderBindings);
        } else if (o instanceof Throwable) {
            o = NException.getErrorMessage((Throwable) o);
        }
        if (o instanceof NText) {
            if (plain) {
                return ((NText) o).filteredText();
            }
        }
        if (o instanceof NMsg) {
            return ((NMsg) o).toString(plain);
        }
        return o;
    }

    /**
     * _pre format arr.
     *
     * @param o o
     * @param plain plain
     * @return _pre format arr result
     */
    private Object[] _preFormatArr(Object[] o, boolean plain) {
        if (o == null) {
            return o;
        }
        Object[] r = new Object[o.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = _preFormatOne(o[i],plain);
        }
        return r;
    }

    /**
     * Converts to full string.
     *
     * @return to full string result
     */
    public String toFullString() {
        if (throwable == null) {
            return toString();
        }
        return this + "\n" + NStringUtils.stacktrace(throwable);
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean plain) {
        try {
            switch (format) {
                case CFORMAT: {
                    return formatAsC(plain);
                }
                case JFORMAT: {
                    return formatAsJ(plain);
                }
                case VFORMAT: {
                    return formatAsV(plain);
                }
                case SFORMAT:
                case MFORMAT:
                case CUSTOM: {
                    return formatCustom(plain);
                }
                case PLAIN: {
                    if (plain || !ntf) {
                        if (message instanceof NText) {
                            return ((NText) message).filteredText();
                        } else if (message instanceof NMsg) {
                            return NText.of((NMsg) message).filteredText();
                        } else {
                            return String.valueOf(message);
                        }
                    } else {
                        return String.valueOf(message);
                    }
                }
                case STYLED:
                case CODE: {
                    return String.valueOf(message); //ignore any style
                }
            }
            return "NMsg{" + "message=" + message + ", style=" + format + ", params=" + Arrays.toString(_preFormatArr(params,plain)) + '}';

        } catch (Exception e) {
            List<Object> a = new ArrayList<>();
            if (params != null) {
                a.add(Arrays.asList(params));
            }
            return NMsg.ofC("[ERROR] Invalid %s message %s with params %s : %s", format, message, a, e).toString();
        }
    }


    /**
     * Creates a new instance of custom.
     *
     * @param formatId format id
     * @param message message
     * @param params params
     * @return of custom result
     */
    public static NMsg ofCustom(String formatId, String message, Object... params) {
        return of(NMsgType.CUSTOM, NStringUtils.firstNonNull(message, ""), params, null, null, null, null, null, null, null, formatId, false);
    }

    /**
     * Creates a new instance of custom.
     *
     * @param formatId format id
     * @param message message
     * @param namedParams named params
     * @return of custom result
     */
    public static NMsg ofCustom(String formatId, String message, Map<String, ?> namedParams) {
        return of(NMsgType.CUSTOM, NStringUtils.firstNonNull(message, ""), NO_PARAMS, null, null, null, null, null, null,
                namedParams == null ? null : namedParams::get, formatId, false);
    }

    /**
     * Creates a new instance of custom.
     *
     * @param formatId format id
     * @param message message
     * @param namedParams named params
     * @return of custom result
     */
    public static NMsg ofCustom(String formatId, String message, Function<String, ?> namedParams) {
        return of(NMsgType.CUSTOM, NStringUtils.firstNonNull(message, ""), NO_PARAMS, null, null, null, null, null, null, namedParams, formatId, false);
    }

    /**
     * Returns the custom format id.
     *
     * @return get custom format id result
     */
    public String getCustomFormatId() {
        return customFormatId;
    }

    /**
     * Format as j.
     *
     * @param plain plain
     * @return format as j result
     */
    private String formatAsJ(boolean plain) {
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
                    } else if (NStringUtils.strip(s2).startsWith(":")) {
                        s2 = currentIndex + s2;
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
        return MessageFormat.format(sMsg, _preFormatArr(params,plain));
    }

    /**
     * Format as c.
     *
     * @param plain plain
     * @return format as c result
     */
    private String formatAsC(boolean plain) {
        StringBuilder sb = new StringBuilder();
        new Formatter(sb).format((String) message, _preFormatArr(params,plain));
        return sb.toString();
    }

    /**
     * Format custom.
     *
     * @param plain plain
     * @return format custom result
     */
    private String formatCustom(boolean plain) {
        try {
            NText t = NTextRPI.of().createText(this);
            if(plain){
                return t.filteredText();
            }
            return t.filteredText();
        } catch (Exception e) {
            return String.valueOf(message);
        }
    }

    /**
     * Format as v.
     *
     * @param plain plain
     * @return format as v result
     */
    private String formatAsV(boolean plain) {
        Object[] params2 = _preFormatArr(params, plain);
        return NStringUtils.replaceDollarPlaceHolder((String) message,
                s -> {
                    Object param = params2[0];
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

    /**
     * As severe.
     *
     * @return as severe result
     */
    public NMsg asSevere() {
        return withLevelAndDefaultIntent(Level.SEVERE, NMsgIntent.FAIL);
    }

    /**
     * As error.
     *
     * @return as error result
     */
    public NMsg asError() {
        return withLevelAndDefaultIntent(Level.SEVERE, NMsgIntent.FAIL);
    }

    /**
     * As error.
     *
     * @param throwable throwable
     * @return as error result
     */
    public NMsg asError(Throwable throwable) {
        return withLevelAndDefaultIntent(Level.SEVERE, NMsgIntent.FAIL, throwable);
    }

    /**
     * As error alert.
     *
     * @return as error alert result
     */
    public NMsg asErrorAlert() {
        return withLevelAndIntent(Level.SEVERE, NMsgIntent.ALERT);
    }

    /**
     * As error alert.
     *
     * @param throwable throwable
     * @return as error alert result
     */
    public NMsg asErrorAlert(Throwable throwable) {
        return withLevelAndIntent(Level.SEVERE, NMsgIntent.ALERT, throwable);
    }

    /**
     * As severe.
     *
     * @param throwable throwable
     * @return as severe result
     */
    public NMsg asSevere(Throwable throwable) {
        return withLevelAndDefaultIntent(Level.SEVERE, NMsgIntent.FAIL, throwable);
    }

    /**
     * As warning.
     *
     * @param throwable throwable
     * @return as warning result
     */
    public NMsg asWarning(Throwable throwable) {
        return withLevelAndDefaultIntent(Level.WARNING, NMsgIntent.ALERT, throwable);
    }

    /**
     * As fine.
     *
     * @param throwable throwable
     * @return as fine result
     */
    public NMsg asFine(Throwable throwable) {
        return withLevelAndDefaultIntent(Level.FINE, NMsgIntent.DEBUG, throwable);
    }

    /**
     * As finest.
     *
     * @param throwable throwable
     * @return as finest result
     */
    public NMsg asFinest(Throwable throwable) {
        return withLevelAndDefaultIntent(Level.FINEST, NMsgIntent.DEBUG, throwable);
    }

    /**
     * As level with throwable.
     *
     * @param level level
     * @param throwable throwable
     * @return as level with throwable result
     */
    private NMsg asLevelWithThrowable(Level level, Throwable throwable) {
        if (level == null) {
            level = Level.FINEST;
        }
        if (level == this.level && throwable == this.throwable) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, throwable, intent, duration, placeholderBindings, customFormatId, ntf);
    }

    /**
     * As info.
     *
     * @return as info result
     */
    public NMsg asInfo() {
        return withLevelAndDefaultIntent(Level.INFO, NMsgIntent.NOTICE);
    }

    /**
     * As config.
     *
     * @return as config result
     */
    public NMsg asConfig() {
        return withLevelAndDefaultIntent(Level.CONFIG, NMsgIntent.INIT);
    }

    /**
     * As warning.
     *
     * @return as warning result
     */
    public NMsg asWarning() {
        return withLevelAndDefaultIntent(Level.WARNING, NMsgIntent.ALERT);
    }

    /**
     * As finest.
     *
     * @return as finest result
     */
    public NMsg asFinest() {
        return withLevelAndDefaultIntent(Level.FINEST, NMsgIntent.DEBUG);
    }

    /**
     * As finest fail.
     *
     * @return as finest fail result
     */
    public NMsg asFinestFail() {
        return withLevelAndDefaultIntent(Level.FINEST, NMsgIntent.FAIL);
    }

    /**
     * As fine fail.
     *
     * @return as fine fail result
     */
    public NMsg asFineFail() {
        return withLevelAndIntent(Level.FINE, NMsgIntent.FAIL);
    }

    /**
     * As finest fail.
     *
     * @param throwable throwable
     * @return as finest fail result
     */
    public NMsg asFinestFail(Throwable throwable) {
        return withLevelAndIntent(Level.FINEST, NMsgIntent.FAIL, throwable);
    }

    /**
     * As fine fail.
     *
     * @param throwable throwable
     * @return as fine fail result
     */
    public NMsg asFineFail(Throwable throwable) {
        return withLevelAndIntent(Level.FINE, NMsgIntent.FAIL, throwable);
    }

    /**
     * As info fail.
     *
     * @param throwable throwable
     * @return as info fail result
     */
    public NMsg asInfoFail(Throwable throwable) {
        return withLevelAndIntent(Level.INFO, NMsgIntent.FAIL, throwable);
    }

    /**
     * As info fail.
     *
     * @return as info fail result
     */
    public NMsg asInfoFail() {
        return withLevelAndIntent(Level.INFO, NMsgIntent.FAIL);
    }

    /**
     * As finer fail.
     *
     * @param throwable throwable
     * @return as finer fail result
     */
    public NMsg asFinerFail(Throwable throwable) {
        return withLevelAndIntent(Level.FINER, NMsgIntent.FAIL, throwable);
    }

    /**
     * As finer fail.
     *
     * @return as finer fail result
     */
    public NMsg asFinerFail() {
        return withLevelAndIntent(Level.FINER, NMsgIntent.FAIL);
    }

    /**
     * As warning fail.
     *
     * @param throwable throwable
     * @return as warning fail result
     */
    public NMsg asWarningFail(Throwable throwable) {
        return withLevelAndIntent(Level.WARNING, NMsgIntent.FAIL, throwable);
    }

    /**
     * As warning fail.
     *
     * @return as warning fail result
     */
    public NMsg asWarningFail() {
        return withLevelAndIntent(Level.WARNING, NMsgIntent.FAIL);
    }

    /**
     * As finest alert.
     *
     * @return as finest alert result
     */
    public NMsg asFinestAlert() {
        return withLevelAndDefaultIntent(Level.FINEST, NMsgIntent.ALERT);
    }

    /**
     * As fine alert.
     *
     * @return as fine alert result
     */
    public NMsg asFineAlert() {
        return withLevelAndIntent(Level.FINE, NMsgIntent.ALERT);
    }

    /**
     * As finest alert.
     *
     * @param throwable throwable
     * @return as finest alert result
     */
    public NMsg asFinestAlert(Throwable throwable) {
        return withLevelAndIntent(Level.FINEST, NMsgIntent.ALERT, throwable);
    }

    /**
     * As fine alert.
     *
     * @param throwable throwable
     * @return as fine alert result
     */
    public NMsg asFineAlert(Throwable throwable) {
        return withLevelAndIntent(Level.FINE, NMsgIntent.ALERT, throwable);
    }

    /**
     * As info alert.
     *
     * @param throwable throwable
     * @return as info alert result
     */
    public NMsg asInfoAlert(Throwable throwable) {
        return withLevelAndIntent(Level.INFO, NMsgIntent.ALERT, throwable);
    }

    /**
     * As info alert.
     *
     * @return as info alert result
     */
    public NMsg asInfoAlert() {
        return withLevelAndIntent(Level.INFO, NMsgIntent.ALERT);
    }

    /**
     * As finer alert.
     *
     * @param throwable throwable
     * @return as finer alert result
     */
    public NMsg asFinerAlert(Throwable throwable) {
        return withLevelAndIntent(Level.FINER, NMsgIntent.ALERT, throwable);
    }

    /**
     * As finer alert.
     *
     * @return as finer alert result
     */
    public NMsg asFinerAlert() {
        return withLevelAndIntent(Level.FINER, NMsgIntent.ALERT);
    }

    /**
     * As warning alert.
     *
     * @param throwable throwable
     * @return as warning alert result
     */
    public NMsg asWarningAlert(Throwable throwable) {
        return withLevelAndIntent(Level.WARNING, NMsgIntent.ALERT, throwable);
    }

    /**
     * As warning alert.
     *
     * @return as warning alert result
     */
    public NMsg asWarningAlert() {
        return withLevelAndIntent(Level.WARNING, NMsgIntent.ALERT);
    }

    /**
     * As fine.
     *
     * @return as fine result
     */
    public NMsg asFine() {
        return withLevelAndDefaultIntent(Level.FINE, NMsgIntent.DEBUG);
    }

    /**
     * As finer.
     *
     * @return as finer result
     */
    public NMsg asFiner() {
        return withLevelAndDefaultIntent(Level.FINER, NMsgIntent.DEBUG);
    }

    /**
     * As debug.
     *
     * @return as debug result
     */
    public NMsg asDebug() {
        return withLevelAndDefaultIntent(Level.FINEST, NMsgIntent.DEBUG);
    }

    /**
     * Without placeholders.
     *
     * @return without placeholders result
     */
    public NMsg withoutPlaceholders() {
        return of(format, message, params, styles, codeLang, level, throwable, intent, duration, null, customFormatId, ntf);
    }

    /**
     * With placeholders.
     *
     * @param placeholderSupplier placeholder supplier
     * @return with placeholders result
     */
    public NMsg withPlaceholders(Function<String, ?> placeholderSupplier) {
        if (placeholderSupplier == null) {
            return this;
        }
        Function<String, ?> oldPlaceholderBindings = placeholderBindings;
        return of(format, message, params, styles, codeLang, level, throwable, intent, duration, s -> {
            Object r = placeholderSupplier.apply(s);
            if (r != null) {
                return r;
            }
            if (oldPlaceholderBindings != null) {
                return oldPlaceholderBindings.apply(s);
            }
            return null;
        }, customFormatId, ntf);
    }

    /**
     * With placeholders.
     *
     * @param params params
     * @return with placeholders result
     */
    public NMsg withPlaceholders(NMsgParam... params) {
        if (params == null || params.length == 0) {
            return this;
        }
        if (placeholderBindings == null) {
            return of(format, message, params, styles, codeLang, level, throwable, intent, duration, new MapAsSupplier2(params), customFormatId, ntf);
        }
        if (placeholderBindings instanceof MapAsSupplier2) {
            Map<String, Supplier<?>> newMap = new LinkedHashMap<>(((MapAsSupplier2) placeholderBindings).content);
            for (NMsgParam param : params) {
                NAssert.requireNamedNonNull(param, "param");
                NAssert.requireNamedNonNull(param.name(), "param.name");
                newMap.put(param.name(), new ConstSupplier<>(param.value()));
            }
            return of(format, message, params, styles, codeLang, level, throwable, intent, duration, new MapAsSupplier2(newMap), customFormatId, ntf);
        }
        if (placeholderBindings instanceof MapAsSupplier) {
            Map<String, Supplier<?>> newMap = new LinkedHashMap<>();
            for (Map.Entry<String, ?> e : ((MapAsSupplier) placeholderBindings).content.entrySet()) {
                newMap.put(e.getKey(), e::getValue);
            }
            for (NMsgParam param : params) {
                NAssert.requireNamedNonNull(param, "param");
                NAssert.requireNamedNonNull(param.name(), "param.name");
                newMap.put(param.name(), new ConstSupplier<>(param.value()));
            }
            return of(format, message, params, styles, codeLang, level, throwable, intent, duration, new MapAsSupplier2(newMap), customFormatId, ntf);
        }
        MapAsSupplier2 p2 = new MapAsSupplier2(params);
        Function<String, ?> oldPlaceholderBindings = placeholderBindings;
        return of(format, message, params, styles, codeLang, level, throwable, intent, duration, s -> {
            if (p2.content.containsKey(s)) {
                return p2.apply(s);
            }
            if (oldPlaceholderBindings != null) {
                return oldPlaceholderBindings.apply(s);
            }
            return null;
        }, customFormatId, ntf);
    }

    /**
     * With placeholder.
     *
     * @param key key
     * @param value value
     * @return with placeholder result
     */
    public NMsg withPlaceholder(String key, Object value) {
        return withPlaceholders(NMaps.of(key, value));
    }

    /**
     * With placeholders.
     *
     * @param placeholderMap placeholder map
     * @return with placeholders result
     */
    public NMsg withPlaceholders(Map<String, ?> placeholderMap) {
        if (placeholderMap == null) {
            return this;
        }
        if (placeholderBindings == null) {
            return of(format, message, params, styles, codeLang, level, throwable, intent, duration, new MapAsSupplier(new LinkedHashMap<>(placeholderMap)), customFormatId, ntf);
        }
        if (placeholderBindings instanceof MapAsSupplier2) {
            Map<String, Supplier<?>> newMap = new LinkedHashMap<>(((MapAsSupplier2) placeholderBindings).content);
            for (Map.Entry<String, ?> e : placeholderMap.entrySet()) {
                NAssert.requireNamedNonNull(e.getKey(), "param.name");
                newMap.put(e.getKey(), new ConstSupplier<>(e.getValue()));
            }
            return of(format, message, params, styles, codeLang, level, throwable, intent, duration, new MapAsSupplier2(newMap), customFormatId, ntf);
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
            return of(format, message, params, styles, codeLang, level, throwable, intent, duration, new MapAsSupplier(newMap), customFormatId, ntf);
        }
        Function<String, ?> oldPlaceholderBindings = placeholderBindings;
        return of(format, message, params, styles, codeLang, level, throwable, intent, duration, s -> {
            if (placeholderMap.containsKey(s)) {
                return placeholderMap.get(s);
            }
            if (oldPlaceholderBindings != null) {
                return oldPlaceholderBindings.apply(s);
            }
            return null;
        }, customFormatId, ntf);
    }

    /**
     * With level.
     *
     * @param level level
     * @return with level result
     */
    public NMsg withLevel(Level level) {
        if (level == this.level) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, throwable, intent, duration, placeholderBindings, customFormatId, ntf);
    }

    /**
     * With level and default intent.
     *
     * @param level level
     * @param intent intent
     * @return with level and default intent result
     */
    private NMsg withLevelAndDefaultIntent(Level level, NMsgIntent intent) {
        if (this.intent != null) {
            intent = this.intent;
        }
        if (level == this.level && Objects.equals(intent, this.intent)) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, throwable, intent, duration, placeholderBindings, customFormatId, ntf);
    }

    /**
     * With level and intent.
     *
     * @param level level
     * @param intent intent
     * @return with level and intent result
     */
    private NMsg withLevelAndIntent(Level level, NMsgIntent intent) {
        if (level == this.level && Objects.equals(intent, this.intent)) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, throwable, intent, duration, placeholderBindings, customFormatId, ntf);
    }

    /**
     * With level and default intent.
     *
     * @param level level
     * @param intent intent
     * @param throwable throwable
     * @return with level and default intent result
     */
    private NMsg withLevelAndDefaultIntent(Level level, NMsgIntent intent, Throwable throwable) {
        if (this.intent != null) {
            intent = this.intent;
        }
        if (level == this.level && Objects.equals(intent, this.intent) && this.throwable == throwable) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, throwable, intent, duration, placeholderBindings, customFormatId, ntf);
    }

    /**
     * With level and intent.
     *
     * @param level level
     * @param intent intent
     * @param throwable throwable
     * @return with level and intent result
     */
    private NMsg withLevelAndIntent(Level level, NMsgIntent intent, Throwable throwable) {
        if (level == this.level && Objects.equals(intent, this.intent) && this.throwable == throwable) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, throwable, intent, duration, placeholderBindings, customFormatId, ntf);
    }

    /**
     * With intent.
     *
     * @param intent intent
     * @return with intent result
     */
    public NMsg withIntent(NMsgIntent intent) {
        if (Objects.equals(intent, this.intent)) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, throwable, intent, duration, placeholderBindings, customFormatId, ntf);
    }

    /**
     * With default intent.
     *
     * @param intent intent
     * @return with default intent result
     */
    public NMsg withDefaultIntent(NMsgIntent intent) {
        if (this.intent != intent) {
            return this;
        }
        if (Objects.equals(intent, this.intent)) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, throwable, intent, duration, placeholderBindings, customFormatId, ntf);
    }

    /**
     * With throwable.
     *
     * @param throwable throwable
     * @return with throwable result
     */
    public NMsg withThrowable(Throwable throwable) {
        if (throwable == this.throwable) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, throwable, intent, duration, placeholderBindings, customFormatId, ntf);
    }

    /**
     * With duration millis.
     *
     * @param elapsedTimeMillis elapsed time millis
     * @return with duration millis result
     */
    public NMsg withDurationMillis(long elapsedTimeMillis) {
        if (elapsedTimeMillis < 0) {
            return withDuration(null);
        }
        return withDuration(NDuration.ofMillis(elapsedTimeMillis));
    }

    /**
     * With duration nanos.
     *
     * @param elapsedTimeNanos elapsed time nanos
     * @return with duration nanos result
     */
    public NMsg withDurationNanos(long elapsedTimeNanos) {
        if (elapsedTimeNanos < 0) {
            return withDuration(null);
        }
        return withDuration(NDuration.ofNanos(elapsedTimeNanos));
    }

    /**
     * With duration.
     *
     * @param duration duration
     * @return with duration result
     */
    public NMsg withDuration(NDuration duration) {
        if (Objects.equals(duration, this.duration)) {
            return this;
        }
        return of(format, message, params, styles, codeLang, level, throwable, intent, duration, placeholderBindings, customFormatId, ntf);
    }

    /**
     * With prefix.
     *
     * @param prefixMessage prefix message
     * @return with prefix result
     */
    public NMsg withPrefix(NMsg prefixMessage) {
        if (NBlankable.isBlank(prefixMessage)) {
            return this;
        }
        if (NBlankable.isBlank(this)) {
            return prefixMessage;
        }
        //this if fast way to inherit level,intent, duration and throwable
        return of(NMsgType.CFORMAT, "%s %s", new Object[]{prefixMessage, cloneWithoutMeta()}, null, null, level, throwable, intent, duration, null, customFormatId, ntf);
    }

    /**
     * With suffix.
     *
     * @param suffixMessage suffix message
     * @return with suffix result
     */
    public NMsg withSuffix(NMsg suffixMessage) {
        if (NBlankable.isBlank(suffixMessage)) {
            return this;
        }
        if (NBlankable.isBlank(this)) {
            return suffixMessage;
        }
        //this if fast way to inherit level,intent, duration and throwable
        return of(NMsgType.CFORMAT, "%s %s", new Object[]{cloneWithoutMeta(), suffixMessage}, null, null, level, throwable, intent, duration, null, customFormatId, ntf);
    }

    /**
     * With prefix.
     *
     * @param prefixMessage prefix message
     * @return with prefix result
     */
    public NMsg withPrefix(NMsgSupplier<NMsg> prefixMessage) {
        if (prefixMessage == null) {
            return this;
        }
        //this if fast way to inherit level,intent, duration and throwable
        Supplier<NMsg> prefixSupplier = () -> prefixMessage.apply(this /**/);
        return of(NMsgType.CFORMAT, "%s %s", new Object[]{prefixSupplier, cloneWithoutMeta()}, null, null, level, throwable, intent, duration, null, customFormatId, ntf);
    }

    /**
     * With suffix.
     *
     * @param suffixMessage suffix message
     * @return with suffix result
     */
    public NMsg withSuffix(NMsgSupplier<NMsg> suffixMessage) {
        if (NBlankable.isBlank(suffixMessage)) {
            return this;
        }
        //this if fast way to inherit level,intent, duration and throwable
        Supplier<NMsg> suffixSupplier = () -> suffixMessage.apply(this /**/);
        return of(NMsgType.CFORMAT, "%s %s", new Object[]{cloneWithoutMeta(), suffixSupplier}, null, null, level, throwable, intent, duration, null, customFormatId, ntf);
    }

    /**
     * Clone without meta.
     *
     * @return clone without meta result
     */
    private NMsg cloneWithoutMeta() {
        return of(format, message, params, styles, codeLang, null, null, null, null, placeholderBindings, customFormatId, ntf);
    }

    // ---------------------------------------------------------------
    // STYLING
    // ---------------------------------------------------------------

    /**
     * Duration.
     *
     * @return duration result
     */
    public NDuration duration() {
        return duration;
    }

    /**
     * Throwable.
     *
     * @return throwable result
     */
    public Throwable throwable() {
        return throwable;
    }

    /**
     * Intent.
     *
     * @return intent result
     */
    public NMsgIntent intent() {
        return intent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NMsg that = (NMsg) o;
        return Objects.equals(codeLang, that.codeLang)
                && Objects.equals(message, that.message)
                && format == that.format
                && Arrays.deepEquals(params, that.params)
                && Objects.equals(styles, that.styles)
                && Objects.equals(level, that.level)
                && Objects.equals(throwable, that.throwable)
                && Objects.equals(customFormatId, that.customFormatId)
                ;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(codeLang, message, format, styles, level, throwable, customFormatId);
        result = 31 * result + Arrays.hashCode(params);
        return result;
    }

    // ---------------------------------------------------------------
    // STYLING
    // ---------------------------------------------------------------
    /**
     * Creates a new instance of styled keyword.
     *
     * @param message message
     * @return of styled keyword result
     */
    public static NMsg ofStyledKeyword(String message) {
        return ofStyled(message, NTextStyle.keyword());
    }

    /**
     * Creates a new instance of styled path.
     *
     * @param message message
     * @return of styled path result
     */
    public static NMsg ofStyledPath(String message) {
        return ofStyled(message, NTextStyle.path());
    }

    /**
     * Creates a new instance of styled pale.
     *
     * @param message message
     * @return of styled pale result
     */
    public static NMsg ofStyledPale(String message) {
        return ofStyled(message, NTextStyle.pale());
    }

    /**
     * Creates a new instance of styled separator.
     *
     * @param message message
     * @return of styled separator result
     */
    public static NMsg ofStyledSeparator(String message) {
        return ofStyled(message, NTextStyle.separator());
    }

    /**
     * Creates a new instance of styled string.
     *
     * @param message message
     * @return of styled string result
     */
    public static NMsg ofStyledString(String message) {
        return ofStyled(message, NTextStyle.string());
    }

    /**
     * Creates a new instance of styled blink.
     *
     * @param message message
     * @return of styled blink result
     */
    public static NMsg ofStyledBlink(String message) {
        return ofStyled(message, NTextStyle.blink());
    }

    /**
     * Creates a new instance of styled bold.
     *
     * @param message message
     * @return of styled bold result
     */
    public static NMsg ofStyledBold(String message) {
        return ofStyled(message, NTextStyle.bold());
    }

    /**
     * Creates a new instance of styled bool.
     *
     * @param message message
     * @return of styled bool result
     */
    public static NMsg ofStyledBool(String message) {
        return ofStyled(message, NTextStyle.bool());
    }

    /**
     * Creates a new instance of styled comments.
     *
     * @param message message
     * @return of styled comments result
     */
    public static NMsg ofStyledComments(String message) {
        return ofStyled(message, NTextStyle.comments());
    }

    /**
     * Creates a new instance of styled config.
     *
     * @param message message
     * @return of styled config result
     */
    public static NMsg ofStyledConfig(String message) {
        return ofStyled(message, NTextStyle.config());
    }

    /**
     * Creates a new instance of styled danger.
     *
     * @param message message
     * @return of styled danger result
     */
    public static NMsg ofStyledDanger(String message) {
        return ofStyled(message, NTextStyle.danger());
    }

    /**
     * Creates a new instance of styled date.
     *
     * @param message message
     * @return of styled date result
     */
    public static NMsg ofStyledDate(String message) {
        return ofStyled(message, NTextStyle.date());
    }

    /**
     * Creates a new instance of styled error.
     *
     * @param message message
     * @return of styled error result
     */
    public static NMsg ofStyledError(String message) {
        return ofStyled(message, NTextStyle.error());
    }

    /**
     * Creates a new instance of styled fail.
     *
     * @param message message
     * @return of styled fail result
     */
    public static NMsg ofStyledFail(String message) {
        return ofStyled(message, NTextStyle.fail());
    }

    /**
     * Creates a new instance of styled info.
     *
     * @param message message
     * @return of styled info result
     */
    public static NMsg ofStyledInfo(String message) {
        return ofStyled(message, NTextStyle.info());
    }

    /**
     * Creates a new instance of styled input.
     *
     * @param message message
     * @return of styled input result
     */
    public static NMsg ofStyledInput(String message) {
        return ofStyled(message, NTextStyle.input());
    }

    /**
     * Creates a new instance of styled italic.
     *
     * @param message message
     * @return of styled italic result
     */
    public static NMsg ofStyledItalic(String message) {
        return ofStyled(message, NTextStyle.italic());
    }

    /**
     * Creates a new instance of styled number.
     *
     * @param message message
     * @return of styled number result
     */
    public static NMsg ofStyledNumber(String message) {
        return ofStyled(message, NTextStyle.number());
    }

    /**
     * Creates a new instance of styled operator.
     *
     * @param message message
     * @return of styled operator result
     */
    public static NMsg ofStyledOperator(String message) {
        return ofStyled(message, NTextStyle.operator());
    }

    /**
     * Creates a new instance of styled option.
     *
     * @param message message
     * @return of styled option result
     */
    public static NMsg ofStyledOption(String message) {
        return ofStyled(message, NTextStyle.option());
    }

    /**
     * Creates a new instance of styled placeholder.
     *
     * @param message message
     * @return of styled placeholder result
     */
    public static NMsg ofStyledPlaceholder(String message) {
        return ofStyled(message, NTextStyle.placeholder());
    }

    /**
     * Creates a new instance of styled entity.
     *
     * @param message message
     * @return of styled entity result
     */
    public static NMsg ofStyledEntity(String message) {
        return ofStyled(message, NTextStyle.entity());
    }

    /**
     * Creates a new instance of styled action.
     *
     * @param message message
     * @return of styled action result
     */
    public static NMsg ofStyledAction(String message) {
        return ofStyled(message, NTextStyle.action());
    }

    /**
     * Creates a new instance of styled annotation.
     *
     * @param message message
     * @return of styled annotation result
     */
    public static NMsg ofStyledAnnotation(String message) {
        return ofStyled(message, NTextStyle.annotation());
    }

    /**
     * Creates a new instance of styled primary1.
     *
     * @param message message
     * @return of styled primary1 result
     */
    public static NMsg ofStyledPrimary1(String message) {
        return ofStyled(message, NTextStyle.primary1());
    }

    /**
     * Creates a new instance of styled primary2.
     *
     * @param message message
     * @return of styled primary2 result
     */
    public static NMsg ofStyledPrimary2(String message) {
        return ofStyled(message, NTextStyle.primary2());
    }

    /**
     * Creates a new instance of styled primary3.
     *
     * @param message message
     * @return of styled primary3 result
     */
    public static NMsg ofStyledPrimary3(String message) {
        return ofStyled(message, NTextStyle.primary3());
    }

    /**
     * Creates a new instance of styled primary4.
     *
     * @param message message
     * @return of styled primary4 result
     */
    public static NMsg ofStyledPrimary4(String message) {
        return ofStyled(message, NTextStyle.primary4());
    }

    /**
     * Creates a new instance of styled primary5.
     *
     * @param message message
     * @return of styled primary5 result
     */
    public static NMsg ofStyledPrimary5(String message) {
        return ofStyled(message, NTextStyle.primary5());
    }

    /**
     * Creates a new instance of styled primary6.
     *
     * @param message message
     * @return of styled primary6 result
     */
    public static NMsg ofStyledPrimary6(String message) {
        return ofStyled(message, NTextStyle.primary6());
    }

    /**
     * Creates a new instance of styled primary7.
     *
     * @param message message
     * @return of styled primary7 result
     */
    public static NMsg ofStyledPrimary7(String message) {
        return ofStyled(message, NTextStyle.primary7());
    }

    /**
     * Creates a new instance of styled primary8.
     *
     * @param message message
     * @return of styled primary8 result
     */
    public static NMsg ofStyledPrimary8(String message) {
        return ofStyled(message, NTextStyle.primary8());
    }

    /**
     * Creates a new instance of styled primary9.
     *
     * @param message message
     * @return of styled primary9 result
     */
    public static NMsg ofStyledPrimary9(String message) {
        return ofStyled(message, NTextStyle.primary9());
    }

    /**
     * Creates a new instance of styled secondary1.
     *
     * @param message message
     * @return of styled secondary1 result
     */
    public static NMsg ofStyledSecondary1(String message) {
        return ofStyled(message, NTextStyle.secondary1());
    }

    /**
     * Creates a new instance of styled secondary2.
     *
     * @param message message
     * @return of styled secondary2 result
     */
    public static NMsg ofStyledSecondary2(String message) {
        return ofStyled(message, NTextStyle.secondary2());
    }

    /**
     * Creates a new instance of styled secondary3.
     *
     * @param message message
     * @return of styled secondary3 result
     */
    public static NMsg ofStyledSecondary3(String message) {
        return ofStyled(message, NTextStyle.secondary3());
    }

    /**
     * Creates a new instance of styled secondary4.
     *
     * @param message message
     * @return of styled secondary4 result
     */
    public static NMsg ofStyledSecondary4(String message) {
        return ofStyled(message, NTextStyle.secondary4());
    }

    /**
     * Creates a new instance of styled secondary5.
     *
     * @param message message
     * @return of styled secondary5 result
     */
    public static NMsg ofStyledSecondary5(String message) {
        return ofStyled(message, NTextStyle.secondary5());
    }

    /**
     * Creates a new instance of styled secondary6.
     *
     * @param message message
     * @return of styled secondary6 result
     */
    public static NMsg ofStyledSecondary6(String message) {
        return ofStyled(message, NTextStyle.secondary6());
    }

    /**
     * Creates a new instance of styled secondary7.
     *
     * @param message message
     * @return of styled secondary7 result
     */
    public static NMsg ofStyledSecondary7(String message) {
        return ofStyled(message, NTextStyle.secondary7());
    }

    /**
     * Creates a new instance of styled secondary8.
     *
     * @param message message
     * @return of styled secondary8 result
     */
    public static NMsg ofStyledSecondary8(String message) {
        return ofStyled(message, NTextStyle.secondary8());
    }

    /**
     * Creates a new instance of styled secondary9.
     *
     * @param message message
     * @return of styled secondary9 result
     */
    public static NMsg ofStyledSecondary9(String message) {
        return ofStyled(message, NTextStyle.secondary9());
    }

    /**
     * Creates a new instance of styled title1.
     *
     * @param message message
     * @return of styled title1 result
     */
    public static NMsg ofStyledTitle1(String message) {
        return ofStyled(message, NTextStyle.title1());
    }

    /**
     * Creates a new instance of styled title2.
     *
     * @param message message
     * @return of styled title2 result
     */
    public static NMsg ofStyledTitle2(String message) {
        return ofStyled(message, NTextStyle.title2());
    }

    /**
     * Creates a new instance of styled title3.
     *
     * @param message message
     * @return of styled title3 result
     */
    public static NMsg ofStyledTitle3(String message) {
        return ofStyled(message, NTextStyle.title3());
    }

    /**
     * Creates a new instance of styled title4.
     *
     * @param message message
     * @return of styled title4 result
     */
    public static NMsg ofStyledTitle4(String message) {
        return ofStyled(message, NTextStyle.title4());
    }

    /**
     * Creates a new instance of styled title5.
     *
     * @param message message
     * @return of styled title5 result
     */
    public static NMsg ofStyledTitle5(String message) {
        return ofStyled(message, NTextStyle.title5());
    }

    /**
     * Creates a new instance of styled title6.
     *
     * @param message message
     * @return of styled title6 result
     */
    public static NMsg ofStyledTitle6(String message) {
        return ofStyled(message, NTextStyle.title6());
    }

    /**
     * Creates a new instance of styled title7.
     *
     * @param message message
     * @return of styled title7 result
     */
    public static NMsg ofStyledTitle7(String message) {
        return ofStyled(message, NTextStyle.title7());
    }

    /**
     * Creates a new instance of styled title8.
     *
     * @param message message
     * @return of styled title8 result
     */
    public static NMsg ofStyledTitle8(String message) {
        return ofStyled(message, NTextStyle.title8());
    }

    /**
     * Creates a new instance of styled title9.
     *
     * @param message message
     * @return of styled title9 result
     */
    public static NMsg ofStyledTitle9(String message) {
        return ofStyled(message, NTextStyle.title9());
    }

    /**
     * Creates a new instance of styled success.
     *
     * @param message message
     * @return of styled success result
     */
    public static NMsg ofStyledSuccess(String message) {
        return ofStyled(message, NTextStyle.success());
    }

    /**
     * Creates a new instance of styled striked.
     *
     * @param message message
     * @return of styled striked result
     */
    public static NMsg ofStyledStriked(String message) {
        return ofStyled(message, NTextStyle.striked());
    }

    /**
     * Creates a new instance of styled variable.
     *
     * @param message message
     * @return of styled variable result
     */
    public static NMsg ofStyledVariable(String message) {
        return ofStyled(message, NTextStyle.variable());
    }

    /**
     * Creates a new instance of styled warn.
     *
     * @param message message
     * @return of styled warn result
     */
    public static NMsg ofStyledWarn(String message) {
        return ofStyled(message, NTextStyle.warn());
    }

    /**
     * Creates a new instance of styled foreground color.
     *
     * @param message message
     * @param color color
     * @return of styled foreground color result
     */
    public static NMsg ofStyledForegroundColor(String message, int color) {
        return ofStyled(message, NTextStyle.foregroundColor(color));
    }

    /**
     * Creates a new instance of styled foreground true color.
     *
     * @param message message
     * @param color color
     * @return of styled foreground true color result
     */
    public static NMsg ofStyledForegroundTrueColor(String message, int color) {
        return ofStyled(message, NTextStyle.foregroundTrueColor(color));
    }

    /**
     * Creates a new instance of styled background color.
     *
     * @param message message
     * @param color color
     * @return of styled background color result
     */
    public static NMsg ofStyledBackgroundColor(String message, int color) {
        return ofStyled(message, NTextStyle.backgroundColor(color));
    }

    /**
     * Creates a new instance of styled background true color.
     *
     * @param message message
     * @param color color
     * @return of styled background true color result
     */
    public static NMsg ofStyledBackgroundTrueColor(String message, int color) {
        return ofStyled(message, NTextStyle.backgroundTrueColor(color));
    }

    /**
     * Creates a new instance of styled foreground true color.
     *
     * @param message message
     * @param color color
     * @return of styled foreground true color result
     */
    public static NMsg ofStyledForegroundTrueColor(String message, NColor color) {
        return ofStyled(message, NTextStyle.foregroundTrueColor(color));
    }

    /**
     * Creates a new instance of styled foreground true color.
     *
     * @param message message
     * @param color color
     * @return of styled foreground true color result
     */
    public static NMsg ofStyledForegroundTrueColor(NMsg message, NColor color) {
        return ofStyled(message, NTextStyle.foregroundTrueColor(color));
    }

    /**
     * Creates a new instance of styled background true color.
     *
     * @param message message
     * @param color color
     * @return of styled background true color result
     */
    public static NMsg ofStyledBackgroundTrueColor(String message, NColor color) {
        return ofStyled(message, NTextStyle.backgroundTrueColor(color));
    }

    /**
     * Creates a new instance of styled background true color.
     *
     * @param message message
     * @param color color
     * @return of styled background true color result
     */
    public static NMsg ofStyledBackgroundTrueColor(NMsg message, NColor color) {
        return ofStyled(message, NTextStyle.backgroundTrueColor(color));
    }

    /**
     * Creates a new instance of styled background color.
     *
     * @param message message
     * @param color color
     * @return of styled background color result
     */
    public static NMsg ofStyledBackgroundColor(String message, NColor color) {
        return ofStyled(message, NTextStyle.backgroundColor(color));
    }

    /**
     * Creates a new instance of styled background color.
     *
     * @param message message
     * @param color color
     * @return of styled background color result
     */
    public static NMsg ofStyledBackgroundColor(NMsg message, NColor color) {
        return ofStyled(message, NTextStyle.backgroundColor(color));
    }

    /**
     * Creates a new instance of styled foreground color.
     *
     * @param message message
     * @param color color
     * @return of styled foreground color result
     */
    public static NMsg ofStyledForegroundColor(String message, NColor color) {
        return ofStyled(message, NTextStyle.foregroundColor(color));
    }

    /**
     * Creates a new instance of styled foreground color.
     *
     * @param message message
     * @param color color
     * @return of styled foreground color result
     */
    public static NMsg ofStyledForegroundColor(NMsg message, NColor color) {
        return ofStyled(message, NTextStyle.foregroundColor(color));
    }

    /**
     * Creates a new instance of styled keyword.
     *
     * @param message message
     * @return of styled keyword result
     */
    public static NMsg ofStyledKeyword(NMsg message) {
        return ofStyled(message, NTextStyle.keyword());
    }

    /**
     * Creates a new instance of styled path.
     *
     * @param message message
     * @return of styled path result
     */
    public static NMsg ofStyledPath(NMsg message) {
        return ofStyled(message, NTextStyle.path());
    }

    /**
     * Creates a new instance of styled pale.
     *
     * @param message message
     * @return of styled pale result
     */
    public static NMsg ofStyledPale(NMsg message) {
        return ofStyled(message, NTextStyle.pale());
    }

    /**
     * Creates a new instance of styled separator.
     *
     * @param message message
     * @return of styled separator result
     */
    public static NMsg ofStyledSeparator(NMsg message) {
        return ofStyled(message, NTextStyle.separator());
    }

    /**
     * Creates a new instance of styled string.
     *
     * @param message message
     * @return of styled string result
     */
    public static NMsg ofStyledString(NMsg message) {
        return ofStyled(message, NTextStyle.string());
    }

    /**
     * Creates a new instance of styled blink.
     *
     * @param message message
     * @return of styled blink result
     */
    public static NMsg ofStyledBlink(NMsg message) {
        return ofStyled(message, NTextStyle.blink());
    }

    /**
     * Creates a new instance of styled bold.
     *
     * @param message message
     * @return of styled bold result
     */
    public static NMsg ofStyledBold(NMsg message) {
        return ofStyled(message, NTextStyle.bold());
    }

    /**
     * Creates a new instance of styled bool.
     *
     * @param message message
     * @return of styled bool result
     */
    public static NMsg ofStyledBool(NMsg message) {
        return ofStyled(message, NTextStyle.bool());
    }

    /**
     * Creates a new instance of styled comments.
     *
     * @param message message
     * @return of styled comments result
     */
    public static NMsg ofStyledComments(NMsg message) {
        return ofStyled(message, NTextStyle.comments());
    }

    /**
     * Creates a new instance of styled config.
     *
     * @param message message
     * @return of styled config result
     */
    public static NMsg ofStyledConfig(NMsg message) {
        return ofStyled(message, NTextStyle.config());
    }

    /**
     * Creates a new instance of styled danger.
     *
     * @param message message
     * @return of styled danger result
     */
    public static NMsg ofStyledDanger(NMsg message) {
        return ofStyled(message, NTextStyle.danger());
    }

    /**
     * Creates a new instance of styled date.
     *
     * @param message message
     * @return of styled date result
     */
    public static NMsg ofStyledDate(NMsg message) {
        return ofStyled(message, NTextStyle.date());
    }

    /**
     * Creates a new instance of styled error.
     *
     * @param message message
     * @return of styled error result
     */
    public static NMsg ofStyledError(NMsg message) {
        return ofStyled(message, NTextStyle.error());
    }

    /**
     * Creates a new instance of styled fail.
     *
     * @param message message
     * @return of styled fail result
     */
    public static NMsg ofStyledFail(NMsg message) {
        return ofStyled(message, NTextStyle.fail());
    }

    /**
     * Creates a new instance of styled info.
     *
     * @param message message
     * @return of styled info result
     */
    public static NMsg ofStyledInfo(NMsg message) {
        return ofStyled(message, NTextStyle.info());
    }

    /**
     * Creates a new instance of styled input.
     *
     * @param message message
     * @return of styled input result
     */
    public static NMsg ofStyledInput(NMsg message) {
        return ofStyled(message, NTextStyle.input());
    }

    /**
     * Creates a new instance of styled italic.
     *
     * @param message message
     * @return of styled italic result
     */
    public static NMsg ofStyledItalic(NMsg message) {
        return ofStyled(message, NTextStyle.italic());
    }

    /**
     * Creates a new instance of styled number.
     *
     * @param message message
     * @return of styled number result
     */
    public static NMsg ofStyledNumber(NMsg message) {
        return ofStyled(message, NTextStyle.number());
    }

    /**
     * Creates a new instance of styled operator.
     *
     * @param message message
     * @return of styled operator result
     */
    public static NMsg ofStyledOperator(NMsg message) {
        return ofStyled(message, NTextStyle.operator());
    }

    /**
     * Creates a new instance of styled option.
     *
     * @param message message
     * @return of styled option result
     */
    public static NMsg ofStyledOption(NMsg message) {
        return ofStyled(message, NTextStyle.option());
    }

    /**
     * Creates a new instance of styled placeholder.
     *
     * @param message message
     * @return of styled placeholder result
     */
    public static NMsg ofStyledPlaceholder(NMsg message) {
        return ofStyled(message, NTextStyle.placeholder());
    }

    /**
     * Creates a new instance of styled entity.
     *
     * @param message message
     * @return of styled entity result
     */
    public static NMsg ofStyledEntity(NMsg message) {
        return ofStyled(message, NTextStyle.entity());
    }

    /**
     * Creates a new instance of styled action.
     *
     * @param message message
     * @return of styled action result
     */
    public static NMsg ofStyledAction(NMsg message) {
        return ofStyled(message, NTextStyle.action());
    }

    /**
     * Creates a new instance of styled annotation.
     *
     * @param message message
     * @return of styled annotation result
     */
    public static NMsg ofStyledAnnotation(NMsg message) {
        return ofStyled(message, NTextStyle.annotation());
    }

    /**
     * Creates a new instance of styled primary1.
     *
     * @param message message
     * @return of styled primary1 result
     */
    public static NMsg ofStyledPrimary1(NMsg message) {
        return ofStyled(message, NTextStyle.primary1());
    }

    /**
     * Creates a new instance of styled primary2.
     *
     * @param message message
     * @return of styled primary2 result
     */
    public static NMsg ofStyledPrimary2(NMsg message) {
        return ofStyled(message, NTextStyle.primary2());
    }

    /**
     * Creates a new instance of styled primary3.
     *
     * @param message message
     * @return of styled primary3 result
     */
    public static NMsg ofStyledPrimary3(NMsg message) {
        return ofStyled(message, NTextStyle.primary3());
    }

    /**
     * Creates a new instance of styled primary4.
     *
     * @param message message
     * @return of styled primary4 result
     */
    public static NMsg ofStyledPrimary4(NMsg message) {
        return ofStyled(message, NTextStyle.primary4());
    }

    /**
     * Creates a new instance of styled primary5.
     *
     * @param message message
     * @return of styled primary5 result
     */
    public static NMsg ofStyledPrimary5(NMsg message) {
        return ofStyled(message, NTextStyle.primary5());
    }

    /**
     * Creates a new instance of styled primary6.
     *
     * @param message message
     * @return of styled primary6 result
     */
    public static NMsg ofStyledPrimary6(NMsg message) {
        return ofStyled(message, NTextStyle.primary6());
    }

    /**
     * Creates a new instance of styled primary7.
     *
     * @param message message
     * @return of styled primary7 result
     */
    public static NMsg ofStyledPrimary7(NMsg message) {
        return ofStyled(message, NTextStyle.primary7());
    }

    /**
     * Creates a new instance of styled primary8.
     *
     * @param message message
     * @return of styled primary8 result
     */
    public static NMsg ofStyledPrimary8(NMsg message) {
        return ofStyled(message, NTextStyle.primary8());
    }

    /**
     * Creates a new instance of styled primary9.
     *
     * @param message message
     * @return of styled primary9 result
     */
    public static NMsg ofStyledPrimary9(NMsg message) {
        return ofStyled(message, NTextStyle.primary9());
    }

    /**
     * Creates a new instance of styled secondary1.
     *
     * @param message message
     * @return of styled secondary1 result
     */
    public static NMsg ofStyledSecondary1(NMsg message) {
        return ofStyled(message, NTextStyle.secondary1());
    }

    /**
     * Creates a new instance of styled secondary2.
     *
     * @param message message
     * @return of styled secondary2 result
     */
    public static NMsg ofStyledSecondary2(NMsg message) {
        return ofStyled(message, NTextStyle.secondary2());
    }

    /**
     * Creates a new instance of styled secondary3.
     *
     * @param message message
     * @return of styled secondary3 result
     */
    public static NMsg ofStyledSecondary3(NMsg message) {
        return ofStyled(message, NTextStyle.secondary3());
    }

    /**
     * Creates a new instance of styled secondary4.
     *
     * @param message message
     * @return of styled secondary4 result
     */
    public static NMsg ofStyledSecondary4(NMsg message) {
        return ofStyled(message, NTextStyle.secondary4());
    }

    /**
     * Creates a new instance of styled secondary5.
     *
     * @param message message
     * @return of styled secondary5 result
     */
    public static NMsg ofStyledSecondary5(NMsg message) {
        return ofStyled(message, NTextStyle.secondary5());
    }

    /**
     * Creates a new instance of styled secondary6.
     *
     * @param message message
     * @return of styled secondary6 result
     */
    public static NMsg ofStyledSecondary6(NMsg message) {
        return ofStyled(message, NTextStyle.secondary6());
    }

    /**
     * Creates a new instance of styled secondary7.
     *
     * @param message message
     * @return of styled secondary7 result
     */
    public static NMsg ofStyledSecondary7(NMsg message) {
        return ofStyled(message, NTextStyle.secondary7());
    }

    /**
     * Creates a new instance of styled secondary8.
     *
     * @param message message
     * @return of styled secondary8 result
     */
    public static NMsg ofStyledSecondary8(NMsg message) {
        return ofStyled(message, NTextStyle.secondary8());
    }

    /**
     * Creates a new instance of styled secondary9.
     *
     * @param message message
     * @return of styled secondary9 result
     */
    public static NMsg ofStyledSecondary9(NMsg message) {
        return ofStyled(message, NTextStyle.secondary9());
    }

    /**
     * Creates a new instance of styled title1.
     *
     * @param message message
     * @return of styled title1 result
     */
    public static NMsg ofStyledTitle1(NMsg message) {
        return ofStyled(message, NTextStyle.title1());
    }

    /**
     * Creates a new instance of styled title2.
     *
     * @param message message
     * @return of styled title2 result
     */
    public static NMsg ofStyledTitle2(NMsg message) {
        return ofStyled(message, NTextStyle.title2());
    }

    /**
     * Creates a new instance of styled title3.
     *
     * @param message message
     * @return of styled title3 result
     */
    public static NMsg ofStyledTitle3(NMsg message) {
        return ofStyled(message, NTextStyle.title3());
    }

    /**
     * Creates a new instance of styled title4.
     *
     * @param message message
     * @return of styled title4 result
     */
    public static NMsg ofStyledTitle4(NMsg message) {
        return ofStyled(message, NTextStyle.title4());
    }

    /**
     * Creates a new instance of styled title5.
     *
     * @param message message
     * @return of styled title5 result
     */
    public static NMsg ofStyledTitle5(NMsg message) {
        return ofStyled(message, NTextStyle.title5());
    }

    /**
     * Creates a new instance of styled title6.
     *
     * @param message message
     * @return of styled title6 result
     */
    public static NMsg ofStyledTitle6(NMsg message) {
        return ofStyled(message, NTextStyle.title6());
    }

    /**
     * Creates a new instance of styled title7.
     *
     * @param message message
     * @return of styled title7 result
     */
    public static NMsg ofStyledTitle7(NMsg message) {
        return ofStyled(message, NTextStyle.title7());
    }

    /**
     * Creates a new instance of styled title8.
     *
     * @param message message
     * @return of styled title8 result
     */
    public static NMsg ofStyledTitle8(NMsg message) {
        return ofStyled(message, NTextStyle.title8());
    }

    /**
     * Creates a new instance of styled title9.
     *
     * @param message message
     * @return of styled title9 result
     */
    public static NMsg ofStyledTitle9(NMsg message) {
        return ofStyled(message, NTextStyle.title9());
    }

    /**
     * Creates a new instance of styled success.
     *
     * @param message message
     * @return of styled success result
     */
    public static NMsg ofStyledSuccess(NMsg message) {
        return ofStyled(message, NTextStyle.success());
    }

    /**
     * Creates a new instance of styled striked.
     *
     * @param message message
     * @return of styled striked result
     */
    public static NMsg ofStyledStriked(NMsg message) {
        return ofStyled(message, NTextStyle.striked());
    }

    /**
     * Creates a new instance of styled variable.
     *
     * @param message message
     * @return of styled variable result
     */
    public static NMsg ofStyledVariable(NMsg message) {
        return ofStyled(message, NTextStyle.variable());
    }

    /**
     * Creates a new instance of styled warn.
     *
     * @param message message
     * @return of styled warn result
     */
    public static NMsg ofStyledWarn(NMsg message) {
        return ofStyled(message, NTextStyle.warn());
    }

    /**
     * Creates a new instance of styled foreground color.
     *
     * @param message message
     * @param color color
     * @return of styled foreground color result
     */
    public static NMsg ofStyledForegroundColor(NMsg message, int color) {
        return ofStyled(message, NTextStyle.foregroundColor(color));
    }

    /**
     * Creates a new instance of styled foreground true color.
     *
     * @param message message
     * @param color color
     * @return of styled foreground true color result
     */
    public static NMsg ofStyledForegroundTrueColor(NMsg message, int color) {
        return ofStyled(message, NTextStyle.foregroundTrueColor(color));
    }

    /**
     * Creates a new instance of styled background color.
     *
     * @param message message
     * @param color color
     * @return of styled background color result
     */
    public static NMsg ofStyledBackgroundColor(NMsg message, int color) {
        return ofStyled(message, NTextStyle.backgroundColor(color));
    }

    /**
     * Creates a new instance of styled background true color.
     *
     * @param message message
     * @param color color
     * @return of styled background true color result
     */
    public static NMsg ofStyledBackgroundTrueColor(NMsg message, int color) {
        return ofStyled(message, NTextStyle.backgroundTrueColor(color));
    }

    /**
     * Checks if is ntf.
     *
     * @return is ntf result
     */
    public boolean isNtf() {
        return ntf;
    }

    // ---------------------------------------------------------------
    // PRIVATE CLASSES
    // ---------------------------------------------------------------
    /**
     * Placeholder class.
     *
     * @author thevpc
     * @since 0.8.0
     */
    public static final class Placeholder {

        private final String name;

        /**
         * Placeholder.
         *
         * @param name name
         * @return placeholder result
         */
        private Placeholder(String name) {
            this.name = name;
        }

        /**
         * Name.
         *
         * @return name result
         */
        public String name() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
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

        /**
         * Map as supplier.
         *
         * @param other other
         * @return map as supplier result
         */
        public MapAsSupplier(Map<String, ?> other) {
            this.content = other;
        }

        @Override
        public Object apply(String ker) {
            return content.get(ker);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MapAsSupplier that = (MapAsSupplier) o;
            return Objects.equals(content, that.content);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(content);
        }

        @Override
        public String toString() {
            return "MapAsSupplier{"
                    + "content=" + content
                    + '}';
        }
    }

    @Override
    public boolean isBlank() {
        if (message == null) {
            return true;
        }
        switch (format) {
            case JFORMAT:
            case VFORMAT:
            case CFORMAT:
            case CODE:
            case SFORMAT:
            case CUSTOM:
                return NStringUtils.isEmpty((String) message);
            case STYLED:
            case PLAIN:{
                if (message instanceof NMsg) {
                    NMsg m = (NMsg) message;
                    return m.isBlank();
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

        private final T value;

        /**
         * Const supplier.
         *
         * @param value value
         * @return const supplier result
         */
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

        /**
         * Map as supplier2.
         *
         * @param other other
         * @return map as supplier2 result
         */
        public MapAsSupplier2(Map<String, Supplier<?>> other) {
            this.content = other;
        }

        /**
         * Map as supplier2.
         *
         * @param params params
         * @return map as supplier2 result
         */
        public MapAsSupplier2(NMsgParam... params) {
            this.content = new LinkedHashMap<>();
            if (params != null) {
                for (NMsgParam param : params) {
                    NAssert.requireNamedNonNull(param, "param");
                    String e = param.name();
                    NAssert.requireNamedNonNull(e, "param.name");
                    if (content.containsKey(e)) {
                        throw NException.ofSafeIllegalArgumentException(NMsg.ofC("duplicate key %s", e));
                    }
                    content.put(e, param.value());
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
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MapAsSupplier2 that = (MapAsSupplier2) o;
            return Objects.equals(content, that.content);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(content);
        }

        @Override
        public String toString() {
            return "MapAsSupplier2{"
                    + "content=" + content
                    + '}';
        }
    }
}

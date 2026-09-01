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
     * Creates a new instance of of missing value.
     *
     * @return of missing value result
     */
    public static NMsg ofMissingValue() {
        /**
         * Creates a new instance of of missing value.
         *
         * @param null null
         * @return of missing value result
         */
        return ofMissingValue((String) null);
    }

    /**
     * Creates a new instance of of missing value.
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
     * Creates a new instance of of missing value.
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
     * Creates a new instance of of invalid value.
     *
     * @return of invalid value result
     */
    public static NMsg ofInvalidValue() {
        /**
         * Creates a new instance of of invalid value.
         *
         * @param null null
         * @param null null
         * @return of invalid value result
         */
        return ofInvalidValue(null, (String) null);
    }

    /**
     * Creates a new instance of of invalid value.
     *
     * @param throwable throwable
     * @return of invalid value result
     */
    public static NMsg ofInvalidValue(Throwable throwable) {
        /**
         * Creates a new instance of of invalid value.
         *
         * @param throwable throwable
         * @param null null
         * @return of invalid value result
         */
        return ofInvalidValue(throwable, (String) null);
    }

    /**
     * Creates a new instance of of invalid value.
     *
     * @param valueName value name
     * @return of invalid value result
     */
    public static NMsg ofInvalidValue(String valueName) {
        /**
         * Creates a new instance of of invalid value.
         *
         * @param null null
         * @param valueName value name
         * @return of invalid value result
         */
        return ofInvalidValue(null, valueName);
    }

    /**
     * Creates a new instance of of invalid value.
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
            /**
             * Creates a new instance of of c.
             *
             * @param %s" %s"
             * @param NException.getErrorMessage(throwable) n exception.get error message(throwable)
             * @return of c result
             */
            return ofC("invalid value : %s", NException.getErrorMessage(throwable));
        }
        /**
         * Creates a new instance of of c.
         *
         * @param %s" %s"
         * @param valueName value name
         * @param NException.getErrorMessage(throwable) n exception.get error message(throwable)
         * @return of c result
         */
        return ofC("invalid %s : %s", valueName, NException.getErrorMessage(throwable));
    }

    /**
     * Creates a new instance of of invalid value.
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
            /**
             * Creates a new instance of of c.
             *
             * @param %s" %s"
             * @param NException.getErrorMessage(throwable) n exception.get error message(throwable)
             * @return of c result
             */
            return ofC("invalid value : %s", NException.getErrorMessage(throwable));
        }
        /**
         * Creates a new instance of of c.
         *
         * @param %s" %s"
         * @param valueName value name
         * @param NException.getErrorMessage(throwable) n exception.get error message(throwable)
         * @return of c result
         */
        return ofC("invalid %s : %s", valueName, NException.getErrorMessage(throwable));
    }

    /**
     * Creates a new instance of of.
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
                /**
                 * Illegal argument exception.
                 *
                 * @param format format
                 * @return illegal argument exception result
                 */
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
     * Creates a new instance of of ntf.
     *
     * @param message message
     * @return of ntf result
     */
    public static NMsg ofNtf(String message) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.PLAIN n msg type.plain
         * @param "") "")
         * @param NO_PARAMS no_params
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.PLAIN, NStringUtils.firstNonNull(message, ""), NO_PARAMS, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of code.
     *
     * @param lang lang
     * @param text text
     * @return of code result
     */
    public static NMsg ofCode(String lang, String text) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.CODE n msg type.code
         * @param "") "")
         * @param NO_PARAMS no_params
         * @param null null
         * @param lang lang
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.CODE, NStringUtils.firstNonNull(text, ""), NO_PARAMS, null, lang, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of code.
     *
     * @param text text
     * @return of code result
     */
    public static NMsg ofCode(String text) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.CODE n msg type.code
         * @param "") "")
         * @param NO_PARAMS no_params
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.CODE, NStringUtils.firstNonNull(text, ""), NO_PARAMS, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of string literal.
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
     * Creates a new instance of of styled.
     *
     * @param message message
     * @param style style
     * @return of styled result
     */
    public static NMsg ofStyled(String message, NTextStyle style) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.STYLED n msg type.styled
         * @param "") "")
         * @param NO_PARAMS no_params
         * @param NTextStyles.of(style) n text styles.of(style)
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.STYLED, NStringUtils.firstNonNull(message, ""), NO_PARAMS, style == null ? null : NTextStyles.of(style), null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of styled.
     *
     * @param message message
     * @param styles styles
     * @return of styled result
     */
    public static NMsg ofStyled(String message, NTextStyles styles) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.STYLED n msg type.styled
         * @param "") "")
         * @param NO_PARAMS no_params
         * @param styles styles
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.STYLED, NStringUtils.firstNonNull(message, ""), NO_PARAMS, styles, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of styled.
     *
     * @param message message
     * @param style style
     * @return of styled result
     */
    public static NMsg ofStyled(NMsg message, NTextStyle style) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.STYLED n msg type.styled
         * @param message message
         * @param NO_PARAMS no_params
         * @param NTextStyles.of(style) n text styles.of(style)
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.STYLED, message, NO_PARAMS, style == null ? null : NTextStyles.of(style), null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of styled.
     *
     * @param message message
     * @param styles styles
     * @return of styled result
     */
    public static NMsg ofStyled(NMsg message, NTextStyles styles) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.STYLED n msg type.styled
         * @param message message
         * @param NO_PARAMS no_params
         * @param styles styles
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.STYLED, message, NO_PARAMS, styles, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of styled.
     *
     * @param message message
     * @param style style
     * @return of styled result
     */
    public static NMsg ofStyled(NText message, NTextStyle style) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.STYLED n msg type.styled
         * @param message message
         * @param NO_PARAMS no_params
         * @param NTextStyles.of(style) n text styles.of(style)
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.STYLED, message, NO_PARAMS, style == null ? null : NTextStyles.of(style), null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of styled.
     *
     * @param message message
     * @param styles styles
     * @return of styled result
     */
    public static NMsg ofStyled(NText message, NTextStyles styles) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.STYLED n msg type.styled
         * @param message message
         * @param NO_PARAMS no_params
         * @param styles styles
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.STYLED, message, NO_PARAMS, styles, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of ntf.
     *
     * @param message message
     * @return of ntf result
     */
    public static NMsg ofNtf(NText message) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.PLAIN n msg type.plain
         * @param message message
         * @param NO_PARAMS no_params
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.PLAIN, message, NO_PARAMS, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of blank.
     *
     * @return of blank result
     */
    public static NMsg ofBlank() {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.PLAIN n msg type.plain
         * @param "" ""
         * @param NO_PARAMS no_params
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param false false
         * @return of result
         */
        return of(NMsgType.PLAIN, "", NO_PARAMS, null, null, null, null, null, null, null, null, false);
    }

    /**
     * Creates a new instance of of plain.
     *
     * @param message message
     * @return of plain result
     */
    public static NMsg ofP(String message) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.PLAIN n msg type.plain
         * @param "") "")
         * @param NO_PARAMS no_params
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param false false
         * @return of result
         */
        return of(NMsgType.PLAIN, NStringUtils.firstNonNull(message, ""), NO_PARAMS, null, null, null, null, null, null, null, null, false);
    }

    /**
     * Creates a new instance of of c.
     *
     * @param message message
     * @return of c result
     */
    public static NMsg ofC(String message) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.CFORMAT n msg type.cformat
         * @param "") "")
         * @param NO_PARAMS no_params
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.CFORMAT, NStringUtils.firstNonNull(message, ""), NO_PARAMS, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of c.
     *
     * @param message message
     * @param params params
     * @return of c result
     */
    public static NMsg ofC(String message, Object... params) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.CFORMAT n msg type.cformat
         * @param "") "")
         * @param params params
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.CFORMAT, NStringUtils.firstNonNull(message, ""), params, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of v.
     *
     * @param message message
     * @param params params
     * @return of v result
     */
    public static NMsg ofV(String message, NMsgParam... params) {
        if (params == null || params.length == 0) {
            /**
             * Creates a new instance of of v.
             *
             * @param message message
             * @param null null
             * @return of v result
             */
            return ofV(message, s -> null);
        }
        /**
         * Creates a new instance of of v.
         *
         * @param message message
         * @param MapAsSupplier2(params) map as supplier2(params)
         * @return of v result
         */
        return ofV(message, new MapAsSupplier2(params));
    }

    /**
     * Creates a new instance of of v.
     *
     * @param message message
     * @param vars vars
     * @return of v result
     */
    public static NMsg ofV(String message, Map<String, ?> vars) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.VFORMAT n msg type.vformat
         * @param "") "")
         * @param Object[]{vars} object[]{vars}
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.VFORMAT, NStringUtils.firstNonNull(message, ""), new Object[]{vars}, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of v.
     *
     * @param message message
     * @param vars vars
     * @return of v result
     */
    public static NMsg ofV(String message, Function<String, ?> vars) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.VFORMAT n msg type.vformat
         * @param "") "")
         * @param Object[]{vars} object[]{vars}
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.VFORMAT, NStringUtils.firstNonNull(message, ""), new Object[]{vars}, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of m.
     *
     * @param message message
     * @param params params
     * @return of m result
     */
    public static NMsg ofM(String message, NMsgParam... params) {
        if (params == null || params.length == 0) {
            /**
             * Creates a new instance of of m.
             *
             * @param message message
             * @param null null
             * @return of m result
             */
            return ofM(message, s -> null);
        }
        /**
         * Creates a new instance of of m.
         *
         * @param message message
         * @param MapAsSupplier2(params) map as supplier2(params)
         * @return of m result
         */
        return ofM(message, new MapAsSupplier2(params));
    }

    /**
     * Creates a new instance of of m.
     *
     * @param message message
     * @param vars vars
     * @return of m result
     */
    public static NMsg ofM(String message, Map<String, ?> vars) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.MFORMAT n msg type.mformat
         * @param "") "")
         * @param Object[]{vars} object[]{vars}
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.MFORMAT, NStringUtils.firstNonNull(message, ""), new Object[]{vars}, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of m.
     *
     * @param message message
     * @param vars vars
     * @return of m result
     */
    public static NMsg ofM(String message, Function<String, ?> vars) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.MFORMAT n msg type.mformat
         * @param "") "")
         * @param Object[]{vars} object[]{vars}
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.MFORMAT, NStringUtils.firstNonNull(message, ""), new Object[]{vars}, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of j.
     *
     * @param message message
     * @param params params
     * @return of j result
     */
    public static NMsg ofJ(String message, NMsgParam... params) {
        if (params == null) {
            /**
             * Creates a new instance of of j.
             *
             * @param message message
             * @param Object[]{null} object[]{null}
             * @return of j result
             */
            return ofJ(message, new Object[]{null});
        }
        Object[] paramsAsObjects = Arrays.stream(params).map(NMsgParam::value).toArray();
        /**
         * Creates a new instance of of j.
         *
         * @param message message
         * @param paramsAsObjects params as objects
         * @return of j result
         */
        return ofJ(message, paramsAsObjects);
    }

    /**
     * Creates a new instance of of c.
     *
     * @param message message
     * @param params params
     * @return of c result
     */
    public static NMsg ofC(String message, NMsgParam... params) {
        if (params == null) {
            /**
             * Creates a new instance of of c.
             *
             * @param message message
             * @param Object[]{null} object[]{null}
             * @return of c result
             */
            return ofC(message, new Object[]{null});
        }
        Object[] paramsAsObjects = Arrays.stream(params).map(NMsgParam::value).toArray();
        /**
         * Creates a new instance of of c.
         *
         * @param message message
         * @param paramsAsObjects params as objects
         * @return of c result
         */
        return ofC(message, paramsAsObjects);
    }

    /**
     * Creates a new instance of j.
     *
     * @param message message
     * @return of j result
     */
    public static NMsg ofJ(String message) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.JFORMAT n msg type.jformat
         * @param "") "")
         * @param NO_PARAMS no_params
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
        return of(NMsgType.JFORMAT, NStringUtils.firstNonNull(message, ""), NO_PARAMS, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Creates a new instance of of j.
     *
     * @param message message
     * @param params params
     * @return of j result
     */
    public static NMsg ofJ(String message, Object... params) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.JFORMAT n msg type.jformat
         * @param "") "")
         * @param params params
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param true true
         * @return of result
         */
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
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.SFORMAT n msg type.sformat
         * @param "") "")
         * @param params params
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param false false
         * @return of result
         */
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
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.SFORMAT n msg type.sformat
         * @param "") "")
         * @param NO_PARAMS no_params
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param namedParams named params
         * @param null null
         * @param false false
         * @return of result
         */
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
        /**
         * Creates a new instance of of s.
         *
         * @param sql sql
         * @param MapAsSupplier2(params) map as supplier2(params)
         * @return of s result
         */
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
              /**
               * Return.
               *
               * @param o).filteredText( o).filtered text(
               */
                return ((NText) o).filteredText();
            }
        }
        if (o instanceof NMsg) {
          /**
           * Return.
           *
           * @param o).toString(plain o).to string(plain
           */
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
                    /**
                     * Format as c.
                     *
                     * @param plain plain
                     * @return format as c result
                     */
                    return formatAsC(plain);
                }
                case JFORMAT: {
                    /**
                     * Format as j.
                     *
                     * @param plain plain
                     * @return format as j result
                     */
                    return formatAsJ(plain);
                }
                case VFORMAT: {
                    /**
                     * Format as v.
                     *
                     * @param plain plain
                     * @return format as v result
                     */
                    return formatAsV(plain);
                }
                case SFORMAT:
                case MFORMAT:
                case CUSTOM: {
                    /**
                     * Format custom.
                     *
                     * @param plain plain
                     * @return format custom result
                     */
                    return formatCustom(plain);
                }
                case PLAIN: {
                    if (plain || !ntf) {
                        if (message instanceof NText) {
                          /**
                           * Return.
                           *
                           * @param message).filteredText( message).filtered text(
                           */
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
     * Creates a new instance of of custom.
     *
     * @param formatId format id
     * @param message message
     * @param params params
     * @return of custom result
     */
    public static NMsg ofCustom(String formatId, String message, Object... params) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.CUSTOM n msg type.custom
         * @param "") "")
         * @param params params
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param formatId format id
         * @param false false
         * @return of result
         */
        return of(NMsgType.CUSTOM, NStringUtils.firstNonNull(message, ""), params, null, null, null, null, null, null, null, formatId, false);
    }

    /**
     * Creates a new instance of of custom.
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
     * Creates a new instance of of custom.
     *
     * @param formatId format id
     * @param message message
     * @param namedParams named params
     * @return of custom result
     */
    public static NMsg ofCustom(String formatId, String message, Function<String, ?> namedParams) {
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.CUSTOM n msg type.custom
         * @param "") "")
         * @param NO_PARAMS no_params
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param namedParams named params
         * @param formatId format id
         * @param false false
         * @return of result
         */
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
        /**
         * Formatter.
         *
         * @param message message
         * @param _preFormatArr(params,plain) _pre format arr(params,plain)
         * @return formatter result
         */
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
        /**
         * With level and default intent.
         *
         * @param Level.SEVERE level.severe
         * @param NMsgIntent.FAIL n msg intent.fail
         * @return with level and default intent result
         */
        return withLevelAndDefaultIntent(Level.SEVERE, NMsgIntent.FAIL);
    }

    /**
     * As error.
     *
     * @return as error result
     */
    public NMsg asError() {
        /**
         * With level and default intent.
         *
         * @param Level.SEVERE level.severe
         * @param NMsgIntent.FAIL n msg intent.fail
         * @return with level and default intent result
         */
        return withLevelAndDefaultIntent(Level.SEVERE, NMsgIntent.FAIL);
    }

    /**
     * As error.
     *
     * @param throwable throwable
     * @return as error result
     */
    public NMsg asError(Throwable throwable) {
        /**
         * With level and default intent.
         *
         * @param Level.SEVERE level.severe
         * @param NMsgIntent.FAIL n msg intent.fail
         * @param throwable throwable
         * @return with level and default intent result
         */
        return withLevelAndDefaultIntent(Level.SEVERE, NMsgIntent.FAIL, throwable);
    }

    /**
     * As error alert.
     *
     * @return as error alert result
     */
    public NMsg asErrorAlert() {
        /**
         * With level and intent.
         *
         * @param Level.SEVERE level.severe
         * @param NMsgIntent.ALERT n msg intent.alert
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.SEVERE, NMsgIntent.ALERT);
    }

    /**
     * As error alert.
     *
     * @param throwable throwable
     * @return as error alert result
     */
    public NMsg asErrorAlert(Throwable throwable) {
        /**
         * With level and intent.
         *
         * @param Level.SEVERE level.severe
         * @param NMsgIntent.ALERT n msg intent.alert
         * @param throwable throwable
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.SEVERE, NMsgIntent.ALERT, throwable);
    }

    /**
     * As severe.
     *
     * @param throwable throwable
     * @return as severe result
     */
    public NMsg asSevere(Throwable throwable) {
        /**
         * With level and default intent.
         *
         * @param Level.SEVERE level.severe
         * @param NMsgIntent.FAIL n msg intent.fail
         * @param throwable throwable
         * @return with level and default intent result
         */
        return withLevelAndDefaultIntent(Level.SEVERE, NMsgIntent.FAIL, throwable);
    }

    /**
     * As warning.
     *
     * @param throwable throwable
     * @return as warning result
     */
    public NMsg asWarning(Throwable throwable) {
        /**
         * With level and default intent.
         *
         * @param Level.WARNING level.warning
         * @param NMsgIntent.ALERT n msg intent.alert
         * @param throwable throwable
         * @return with level and default intent result
         */
        return withLevelAndDefaultIntent(Level.WARNING, NMsgIntent.ALERT, throwable);
    }

    /**
     * As fine.
     *
     * @param throwable throwable
     * @return as fine result
     */
    public NMsg asFine(Throwable throwable) {
        /**
         * With level and default intent.
         *
         * @param Level.FINE level.fine
         * @param NMsgIntent.DEBUG n msg intent.debug
         * @param throwable throwable
         * @return with level and default intent result
         */
        return withLevelAndDefaultIntent(Level.FINE, NMsgIntent.DEBUG, throwable);
    }

    /**
     * As finest.
     *
     * @param throwable throwable
     * @return as finest result
     */
    public NMsg asFinest(Throwable throwable) {
        /**
         * With level and default intent.
         *
         * @param Level.FINEST level.finest
         * @param NMsgIntent.DEBUG n msg intent.debug
         * @param throwable throwable
         * @return with level and default intent result
         */
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
        /**
         * Creates a new instance of of.
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
        return of(format, message, params, styles, codeLang, level, throwable, intent, duration, placeholderBindings, customFormatId, ntf);
    }

    /**
     * As info.
     *
     * @return as info result
     */
    public NMsg asInfo() {
        /**
         * With level and default intent.
         *
         * @param Level.INFO level.info
         * @param NMsgIntent.NOTICE n msg intent.notice
         * @return with level and default intent result
         */
        return withLevelAndDefaultIntent(Level.INFO, NMsgIntent.NOTICE);
    }

    /**
     * As config.
     *
     * @return as config result
     */
    public NMsg asConfig() {
        /**
         * With level and default intent.
         *
         * @param Level.CONFIG level.config
         * @param NMsgIntent.INIT n msg intent.init
         * @return with level and default intent result
         */
        return withLevelAndDefaultIntent(Level.CONFIG, NMsgIntent.INIT);
    }

    /**
     * As warning.
     *
     * @return as warning result
     */
    public NMsg asWarning() {
        /**
         * With level and default intent.
         *
         * @param Level.WARNING level.warning
         * @param NMsgIntent.ALERT n msg intent.alert
         * @return with level and default intent result
         */
        return withLevelAndDefaultIntent(Level.WARNING, NMsgIntent.ALERT);
    }

    /**
     * As finest.
     *
     * @return as finest result
     */
    public NMsg asFinest() {
        /**
         * With level and default intent.
         *
         * @param Level.FINEST level.finest
         * @param NMsgIntent.DEBUG n msg intent.debug
         * @return with level and default intent result
         */
        return withLevelAndDefaultIntent(Level.FINEST, NMsgIntent.DEBUG);
    }

    /**
     * As finest fail.
     *
     * @return as finest fail result
     */
    public NMsg asFinestFail() {
        /**
         * With level and default intent.
         *
         * @param Level.FINEST level.finest
         * @param NMsgIntent.FAIL n msg intent.fail
         * @return with level and default intent result
         */
        return withLevelAndDefaultIntent(Level.FINEST, NMsgIntent.FAIL);
    }

    /**
     * As fine fail.
     *
     * @return as fine fail result
     */
    public NMsg asFineFail() {
        /**
         * With level and intent.
         *
         * @param Level.FINE level.fine
         * @param NMsgIntent.FAIL n msg intent.fail
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.FINE, NMsgIntent.FAIL);
    }

    /**
     * As finest fail.
     *
     * @param throwable throwable
     * @return as finest fail result
     */
    public NMsg asFinestFail(Throwable throwable) {
        /**
         * With level and intent.
         *
         * @param Level.FINEST level.finest
         * @param NMsgIntent.FAIL n msg intent.fail
         * @param throwable throwable
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.FINEST, NMsgIntent.FAIL, throwable);
    }

    /**
     * As fine fail.
     *
     * @param throwable throwable
     * @return as fine fail result
     */
    public NMsg asFineFail(Throwable throwable) {
        /**
         * With level and intent.
         *
         * @param Level.FINE level.fine
         * @param NMsgIntent.FAIL n msg intent.fail
         * @param throwable throwable
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.FINE, NMsgIntent.FAIL, throwable);
    }

    /**
     * As info fail.
     *
     * @param throwable throwable
     * @return as info fail result
     */
    public NMsg asInfoFail(Throwable throwable) {
        /**
         * With level and intent.
         *
         * @param Level.INFO level.info
         * @param NMsgIntent.FAIL n msg intent.fail
         * @param throwable throwable
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.INFO, NMsgIntent.FAIL, throwable);
    }

    /**
     * As info fail.
     *
     * @return as info fail result
     */
    public NMsg asInfoFail() {
        /**
         * With level and intent.
         *
         * @param Level.INFO level.info
         * @param NMsgIntent.FAIL n msg intent.fail
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.INFO, NMsgIntent.FAIL);
    }

    /**
     * As finer fail.
     *
     * @param throwable throwable
     * @return as finer fail result
     */
    public NMsg asFinerFail(Throwable throwable) {
        /**
         * With level and intent.
         *
         * @param Level.FINER level.finer
         * @param NMsgIntent.FAIL n msg intent.fail
         * @param throwable throwable
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.FINER, NMsgIntent.FAIL, throwable);
    }

    /**
     * As finer fail.
     *
     * @return as finer fail result
     */
    public NMsg asFinerFail() {
        /**
         * With level and intent.
         *
         * @param Level.FINER level.finer
         * @param NMsgIntent.FAIL n msg intent.fail
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.FINER, NMsgIntent.FAIL);
    }

    /**
     * As warning fail.
     *
     * @param throwable throwable
     * @return as warning fail result
     */
    public NMsg asWarningFail(Throwable throwable) {
        /**
         * With level and intent.
         *
         * @param Level.WARNING level.warning
         * @param NMsgIntent.FAIL n msg intent.fail
         * @param throwable throwable
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.WARNING, NMsgIntent.FAIL, throwable);
    }

    /**
     * As warning fail.
     *
     * @return as warning fail result
     */
    public NMsg asWarningFail() {
        /**
         * With level and intent.
         *
         * @param Level.WARNING level.warning
         * @param NMsgIntent.FAIL n msg intent.fail
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.WARNING, NMsgIntent.FAIL);
    }

    /**
     * As finest alert.
     *
     * @return as finest alert result
     */
    public NMsg asFinestAlert() {
        /**
         * With level and default intent.
         *
         * @param Level.FINEST level.finest
         * @param NMsgIntent.ALERT n msg intent.alert
         * @return with level and default intent result
         */
        return withLevelAndDefaultIntent(Level.FINEST, NMsgIntent.ALERT);
    }

    /**
     * As fine alert.
     *
     * @return as fine alert result
     */
    public NMsg asFineAlert() {
        /**
         * With level and intent.
         *
         * @param Level.FINE level.fine
         * @param NMsgIntent.ALERT n msg intent.alert
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.FINE, NMsgIntent.ALERT);
    }

    /**
     * As finest alert.
     *
     * @param throwable throwable
     * @return as finest alert result
     */
    public NMsg asFinestAlert(Throwable throwable) {
        /**
         * With level and intent.
         *
         * @param Level.FINEST level.finest
         * @param NMsgIntent.ALERT n msg intent.alert
         * @param throwable throwable
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.FINEST, NMsgIntent.ALERT, throwable);
    }

    /**
     * As fine alert.
     *
     * @param throwable throwable
     * @return as fine alert result
     */
    public NMsg asFineAlert(Throwable throwable) {
        /**
         * With level and intent.
         *
         * @param Level.FINE level.fine
         * @param NMsgIntent.ALERT n msg intent.alert
         * @param throwable throwable
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.FINE, NMsgIntent.ALERT, throwable);
    }

    /**
     * As info alert.
     *
     * @param throwable throwable
     * @return as info alert result
     */
    public NMsg asInfoAlert(Throwable throwable) {
        /**
         * With level and intent.
         *
         * @param Level.INFO level.info
         * @param NMsgIntent.ALERT n msg intent.alert
         * @param throwable throwable
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.INFO, NMsgIntent.ALERT, throwable);
    }

    /**
     * As info alert.
     *
     * @return as info alert result
     */
    public NMsg asInfoAlert() {
        /**
         * With level and intent.
         *
         * @param Level.INFO level.info
         * @param NMsgIntent.ALERT n msg intent.alert
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.INFO, NMsgIntent.ALERT);
    }

    /**
     * As finer alert.
     *
     * @param throwable throwable
     * @return as finer alert result
     */
    public NMsg asFinerAlert(Throwable throwable) {
        /**
         * With level and intent.
         *
         * @param Level.FINER level.finer
         * @param NMsgIntent.ALERT n msg intent.alert
         * @param throwable throwable
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.FINER, NMsgIntent.ALERT, throwable);
    }

    /**
     * As finer alert.
     *
     * @return as finer alert result
     */
    public NMsg asFinerAlert() {
        /**
         * With level and intent.
         *
         * @param Level.FINER level.finer
         * @param NMsgIntent.ALERT n msg intent.alert
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.FINER, NMsgIntent.ALERT);
    }

    /**
     * As warning alert.
     *
     * @param throwable throwable
     * @return as warning alert result
     */
    public NMsg asWarningAlert(Throwable throwable) {
        /**
         * With level and intent.
         *
         * @param Level.WARNING level.warning
         * @param NMsgIntent.ALERT n msg intent.alert
         * @param throwable throwable
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.WARNING, NMsgIntent.ALERT, throwable);
    }

    /**
     * As warning alert.
     *
     * @return as warning alert result
     */
    public NMsg asWarningAlert() {
        /**
         * With level and intent.
         *
         * @param Level.WARNING level.warning
         * @param NMsgIntent.ALERT n msg intent.alert
         * @return with level and intent result
         */
        return withLevelAndIntent(Level.WARNING, NMsgIntent.ALERT);
    }

    /**
     * As fine.
     *
     * @return as fine result
     */
    public NMsg asFine() {
        /**
         * With level and default intent.
         *
         * @param Level.FINE level.fine
         * @param NMsgIntent.DEBUG n msg intent.debug
         * @return with level and default intent result
         */
        return withLevelAndDefaultIntent(Level.FINE, NMsgIntent.DEBUG);
    }

    /**
     * As finer.
     *
     * @return as finer result
     */
    public NMsg asFiner() {
        /**
         * With level and default intent.
         *
         * @param Level.FINER level.finer
         * @param NMsgIntent.DEBUG n msg intent.debug
         * @return with level and default intent result
         */
        return withLevelAndDefaultIntent(Level.FINER, NMsgIntent.DEBUG);
    }

    /**
     * As debug.
     *
     * @return as debug result
     */
    public NMsg asDebug() {
        /**
         * With level and default intent.
         *
         * @param Level.FINEST level.finest
         * @param NMsgIntent.DEBUG n msg intent.debug
         * @return with level and default intent result
         */
        return withLevelAndDefaultIntent(Level.FINEST, NMsgIntent.DEBUG);
    }

    /**
     * Without placeholders.
     *
     * @return without placeholders result
     */
    public NMsg withoutPlaceholders() {
        /**
         * Creates a new instance of of.
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
         * @param null null
         * @param customFormatId custom format id
         * @param ntf ntf
         * @return of result
         */
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
            /**
             * Creates a new instance of of.
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
             * @param MapAsSupplier2(params) map as supplier2(params)
             * @param customFormatId custom format id
             * @param ntf ntf
             * @return of result
             */
            return of(format, message, params, styles, codeLang, level, throwable, intent, duration, new MapAsSupplier2(params), customFormatId, ntf);
        }
        if (placeholderBindings instanceof MapAsSupplier2) {
            Map<String, Supplier<?>> newMap = new LinkedHashMap<>(((MapAsSupplier2) placeholderBindings).content);
            for (NMsgParam param : params) {
                NAssert.requireNamedNonNull(param, "param");
                NAssert.requireNamedNonNull(param.name(), "param.name");
                newMap.put(param.name(), new ConstSupplier<>(param.value()));
            }
            /**
             * Creates a new instance of of.
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
             * @param MapAsSupplier2(newMap) map as supplier2(new map)
             * @param customFormatId custom format id
             * @param ntf ntf
             * @return of result
             */
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
            /**
             * Creates a new instance of of.
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
             * @param MapAsSupplier2(newMap) map as supplier2(new map)
             * @param customFormatId custom format id
             * @param ntf ntf
             * @return of result
             */
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
        /**
         * With placeholders.
         *
         * @param value) value)
         * @return with placeholders result
         */
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
            /**
             * Creates a new instance of of.
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
             * @param LinkedHashMap<>(placeholderMap)) linked hash map<>(placeholder map))
             * @param customFormatId custom format id
             * @param ntf ntf
             * @return of result
             */
            return of(format, message, params, styles, codeLang, level, throwable, intent, duration, new MapAsSupplier(new LinkedHashMap<>(placeholderMap)), customFormatId, ntf);
        }
        if (placeholderBindings instanceof MapAsSupplier2) {
            Map<String, Supplier<?>> newMap = new LinkedHashMap<>(((MapAsSupplier2) placeholderBindings).content);
            for (Map.Entry<String, ?> e : placeholderMap.entrySet()) {
                NAssert.requireNamedNonNull(e.getKey(), "param.name");
                newMap.put(e.getKey(), new ConstSupplier<>(e.getValue()));
            }
            /**
             * Creates a new instance of of.
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
             * @param MapAsSupplier2(newMap) map as supplier2(new map)
             * @param customFormatId custom format id
             * @param ntf ntf
             * @return of result
             */
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
            /**
             * Creates a new instance of of.
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
             * @param MapAsSupplier(newMap) map as supplier(new map)
             * @param customFormatId custom format id
             * @param ntf ntf
             * @return of result
             */
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
        /**
         * Creates a new instance of of.
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
        /**
         * Creates a new instance of of.
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
        /**
         * Creates a new instance of of.
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
        /**
         * Creates a new instance of of.
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
        /**
         * Creates a new instance of of.
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
        /**
         * Creates a new instance of of.
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
        /**
         * Creates a new instance of of.
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
        /**
         * Creates a new instance of of.
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
            /**
             * With duration.
             *
             * @param null null
             * @return with duration result
             */
            return withDuration(null);
        }
        /**
         * With duration.
         *
         * @param NDuration.ofMillis(elapsedTimeMillis) n duration.of millis(elapsed time millis)
         * @return with duration result
         */
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
            /**
             * With duration.
             *
             * @param null null
             * @return with duration result
             */
            return withDuration(null);
        }
        /**
         * With duration.
         *
         * @param NDuration.ofNanos(elapsedTimeNanos) n duration.of nanos(elapsed time nanos)
         * @return with duration result
         */
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
        /**
         * Creates a new instance of of.
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
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.CFORMAT n msg type.cformat
         * @param %s" %s"
         * @param Object[]{prefixMessage object[]{prefix message
         * @param cloneWithoutMeta()} clone without meta()}
         * @param null null
         * @param null null
         * @param level level
         * @param throwable throwable
         * @param intent intent
         * @param duration duration
         * @param null null
         * @param customFormatId custom format id
         * @param ntf ntf
         * @return of result
         */
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
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.CFORMAT n msg type.cformat
         * @param %s" %s"
         * @param Object[]{cloneWithoutMeta() object[]{clone without meta()
         * @param suffixMessage} suffix message}
         * @param null null
         * @param null null
         * @param level level
         * @param throwable throwable
         * @param intent intent
         * @param duration duration
         * @param null null
         * @param customFormatId custom format id
         * @param ntf ntf
         * @return of result
         */
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
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.CFORMAT n msg type.cformat
         * @param %s" %s"
         * @param Object[]{prefixSupplier object[]{prefix supplier
         * @param cloneWithoutMeta()} clone without meta()}
         * @param null null
         * @param null null
         * @param level level
         * @param throwable throwable
         * @param intent intent
         * @param duration duration
         * @param null null
         * @param customFormatId custom format id
         * @param ntf ntf
         * @return of result
         */
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
        /**
         * Creates a new instance of of.
         *
         * @param NMsgType.CFORMAT n msg type.cformat
         * @param %s" %s"
         * @param Object[]{cloneWithoutMeta() object[]{clone without meta()
         * @param suffixSupplier} suffix supplier}
         * @param null null
         * @param null null
         * @param level level
         * @param throwable throwable
         * @param intent intent
         * @param duration duration
         * @param null null
         * @param customFormatId custom format id
         * @param ntf ntf
         * @return of result
         */
        return of(NMsgType.CFORMAT, "%s %s", new Object[]{cloneWithoutMeta(), suffixSupplier}, null, null, level, throwable, intent, duration, null, customFormatId, ntf);
    }

    /**
     * Clone without meta.
     *
     * @return clone without meta result
     */
    private NMsg cloneWithoutMeta() {
        /**
         * Creates a new instance of of.
         *
         * @param format format
         * @param message message
         * @param params params
         * @param styles styles
         * @param codeLang code lang
         * @param null null
         * @param null null
         * @param null null
         * @param null null
         * @param placeholderBindings placeholder bindings
         * @param customFormatId custom format id
         * @param ntf ntf
         * @return of result
         */
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
     * Creates a new instance of of styled keyword.
     *
     * @param message message
     * @return of styled keyword result
     */
    public static NMsg ofStyledKeyword(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.keyword() n text style.keyword()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.keyword());
    }

    /**
     * Creates a new instance of of styled path.
     *
     * @param message message
     * @return of styled path result
     */
    public static NMsg ofStyledPath(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.path() n text style.path()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.path());
    }

    /**
     * Creates a new instance of of styled pale.
     *
     * @param message message
     * @return of styled pale result
     */
    public static NMsg ofStyledPale(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.pale() n text style.pale()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.pale());
    }

    /**
     * Creates a new instance of of styled separator.
     *
     * @param message message
     * @return of styled separator result
     */
    public static NMsg ofStyledSeparator(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.separator() n text style.separator()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.separator());
    }

    /**
     * Creates a new instance of of styled string.
     *
     * @param message message
     * @return of styled string result
     */
    public static NMsg ofStyledString(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.string() n text style.string()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.string());
    }

    /**
     * Creates a new instance of of styled blink.
     *
     * @param message message
     * @return of styled blink result
     */
    public static NMsg ofStyledBlink(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.blink() n text style.blink()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.blink());
    }

    /**
     * Creates a new instance of of styled bold.
     *
     * @param message message
     * @return of styled bold result
     */
    public static NMsg ofStyledBold(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.bold() n text style.bold()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.bold());
    }

    /**
     * Creates a new instance of of styled bool.
     *
     * @param message message
     * @return of styled bool result
     */
    public static NMsg ofStyledBool(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.bool() n text style.bool()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.bool());
    }

    /**
     * Creates a new instance of of styled comments.
     *
     * @param message message
     * @return of styled comments result
     */
    public static NMsg ofStyledComments(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.comments() n text style.comments()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.comments());
    }

    /**
     * Creates a new instance of of styled config.
     *
     * @param message message
     * @return of styled config result
     */
    public static NMsg ofStyledConfig(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.config() n text style.config()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.config());
    }

    /**
     * Creates a new instance of of styled danger.
     *
     * @param message message
     * @return of styled danger result
     */
    public static NMsg ofStyledDanger(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.danger() n text style.danger()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.danger());
    }

    /**
     * Creates a new instance of of styled date.
     *
     * @param message message
     * @return of styled date result
     */
    public static NMsg ofStyledDate(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.date() n text style.date()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.date());
    }

    /**
     * Creates a new instance of of styled error.
     *
     * @param message message
     * @return of styled error result
     */
    public static NMsg ofStyledError(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.error() n text style.error()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.error());
    }

    /**
     * Creates a new instance of of styled fail.
     *
     * @param message message
     * @return of styled fail result
     */
    public static NMsg ofStyledFail(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.fail() n text style.fail()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.fail());
    }

    /**
     * Creates a new instance of of styled info.
     *
     * @param message message
     * @return of styled info result
     */
    public static NMsg ofStyledInfo(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.info() n text style.info()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.info());
    }

    /**
     * Creates a new instance of of styled input.
     *
     * @param message message
     * @return of styled input result
     */
    public static NMsg ofStyledInput(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.input() n text style.input()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.input());
    }

    /**
     * Creates a new instance of of styled italic.
     *
     * @param message message
     * @return of styled italic result
     */
    public static NMsg ofStyledItalic(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.italic() n text style.italic()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.italic());
    }

    /**
     * Creates a new instance of of styled number.
     *
     * @param message message
     * @return of styled number result
     */
    public static NMsg ofStyledNumber(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.number() n text style.number()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.number());
    }

    /**
     * Creates a new instance of of styled operator.
     *
     * @param message message
     * @return of styled operator result
     */
    public static NMsg ofStyledOperator(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.operator() n text style.operator()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.operator());
    }

    /**
     * Creates a new instance of of styled option.
     *
     * @param message message
     * @return of styled option result
     */
    public static NMsg ofStyledOption(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.option() n text style.option()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.option());
    }

    /**
     * Creates a new instance of of styled placeholder.
     *
     * @param message message
     * @return of styled placeholder result
     */
    public static NMsg ofStyledPlaceholder(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.placeholder() n text style.placeholder()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.placeholder());
    }

    /**
     * Creates a new instance of of styled entity.
     *
     * @param message message
     * @return of styled entity result
     */
    public static NMsg ofStyledEntity(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.entity() n text style.entity()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.entity());
    }

    /**
     * Creates a new instance of of styled action.
     *
     * @param message message
     * @return of styled action result
     */
    public static NMsg ofStyledAction(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.action() n text style.action()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.action());
    }

    /**
     * Creates a new instance of of styled annotation.
     *
     * @param message message
     * @return of styled annotation result
     */
    public static NMsg ofStyledAnnotation(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.annotation() n text style.annotation()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.annotation());
    }

    /**
     * Creates a new instance of of styled primary1.
     *
     * @param message message
     * @return of styled primary1 result
     */
    public static NMsg ofStyledPrimary1(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary1() n text style.primary1()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary1());
    }

    /**
     * Creates a new instance of of styled primary2.
     *
     * @param message message
     * @return of styled primary2 result
     */
    public static NMsg ofStyledPrimary2(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary2() n text style.primary2()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary2());
    }

    /**
     * Creates a new instance of of styled primary3.
     *
     * @param message message
     * @return of styled primary3 result
     */
    public static NMsg ofStyledPrimary3(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary3() n text style.primary3()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary3());
    }

    /**
     * Creates a new instance of of styled primary4.
     *
     * @param message message
     * @return of styled primary4 result
     */
    public static NMsg ofStyledPrimary4(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary4() n text style.primary4()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary4());
    }

    /**
     * Creates a new instance of of styled primary5.
     *
     * @param message message
     * @return of styled primary5 result
     */
    public static NMsg ofStyledPrimary5(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary5() n text style.primary5()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary5());
    }

    /**
     * Creates a new instance of of styled primary6.
     *
     * @param message message
     * @return of styled primary6 result
     */
    public static NMsg ofStyledPrimary6(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary6() n text style.primary6()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary6());
    }

    /**
     * Creates a new instance of of styled primary7.
     *
     * @param message message
     * @return of styled primary7 result
     */
    public static NMsg ofStyledPrimary7(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary7() n text style.primary7()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary7());
    }

    /**
     * Creates a new instance of of styled primary8.
     *
     * @param message message
     * @return of styled primary8 result
     */
    public static NMsg ofStyledPrimary8(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary8() n text style.primary8()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary8());
    }

    /**
     * Creates a new instance of of styled primary9.
     *
     * @param message message
     * @return of styled primary9 result
     */
    public static NMsg ofStyledPrimary9(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary9() n text style.primary9()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary9());
    }

    /**
     * Creates a new instance of of styled secondary1.
     *
     * @param message message
     * @return of styled secondary1 result
     */
    public static NMsg ofStyledSecondary1(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary1() n text style.secondary1()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary1());
    }

    /**
     * Creates a new instance of of styled secondary2.
     *
     * @param message message
     * @return of styled secondary2 result
     */
    public static NMsg ofStyledSecondary2(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary2() n text style.secondary2()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary2());
    }

    /**
     * Creates a new instance of of styled secondary3.
     *
     * @param message message
     * @return of styled secondary3 result
     */
    public static NMsg ofStyledSecondary3(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary3() n text style.secondary3()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary3());
    }

    /**
     * Creates a new instance of of styled secondary4.
     *
     * @param message message
     * @return of styled secondary4 result
     */
    public static NMsg ofStyledSecondary4(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary4() n text style.secondary4()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary4());
    }

    /**
     * Creates a new instance of of styled secondary5.
     *
     * @param message message
     * @return of styled secondary5 result
     */
    public static NMsg ofStyledSecondary5(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary5() n text style.secondary5()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary5());
    }

    /**
     * Creates a new instance of of styled secondary6.
     *
     * @param message message
     * @return of styled secondary6 result
     */
    public static NMsg ofStyledSecondary6(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary6() n text style.secondary6()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary6());
    }

    /**
     * Creates a new instance of of styled secondary7.
     *
     * @param message message
     * @return of styled secondary7 result
     */
    public static NMsg ofStyledSecondary7(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary7() n text style.secondary7()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary7());
    }

    /**
     * Creates a new instance of of styled secondary8.
     *
     * @param message message
     * @return of styled secondary8 result
     */
    public static NMsg ofStyledSecondary8(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary8() n text style.secondary8()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary8());
    }

    /**
     * Creates a new instance of of styled secondary9.
     *
     * @param message message
     * @return of styled secondary9 result
     */
    public static NMsg ofStyledSecondary9(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary9() n text style.secondary9()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary9());
    }

    /**
     * Creates a new instance of of styled title1.
     *
     * @param message message
     * @return of styled title1 result
     */
    public static NMsg ofStyledTitle1(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title1() n text style.title1()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title1());
    }

    /**
     * Creates a new instance of of styled title2.
     *
     * @param message message
     * @return of styled title2 result
     */
    public static NMsg ofStyledTitle2(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title2() n text style.title2()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title2());
    }

    /**
     * Creates a new instance of of styled title3.
     *
     * @param message message
     * @return of styled title3 result
     */
    public static NMsg ofStyledTitle3(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title3() n text style.title3()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title3());
    }

    /**
     * Creates a new instance of of styled title4.
     *
     * @param message message
     * @return of styled title4 result
     */
    public static NMsg ofStyledTitle4(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title4() n text style.title4()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title4());
    }

    /**
     * Creates a new instance of of styled title5.
     *
     * @param message message
     * @return of styled title5 result
     */
    public static NMsg ofStyledTitle5(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title5() n text style.title5()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title5());
    }

    /**
     * Creates a new instance of of styled title6.
     *
     * @param message message
     * @return of styled title6 result
     */
    public static NMsg ofStyledTitle6(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title6() n text style.title6()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title6());
    }

    /**
     * Creates a new instance of of styled title7.
     *
     * @param message message
     * @return of styled title7 result
     */
    public static NMsg ofStyledTitle7(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title7() n text style.title7()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title7());
    }

    /**
     * Creates a new instance of of styled title8.
     *
     * @param message message
     * @return of styled title8 result
     */
    public static NMsg ofStyledTitle8(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title8() n text style.title8()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title8());
    }

    /**
     * Creates a new instance of of styled title9.
     *
     * @param message message
     * @return of styled title9 result
     */
    public static NMsg ofStyledTitle9(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title9() n text style.title9()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title9());
    }

    /**
     * Creates a new instance of of styled success.
     *
     * @param message message
     * @return of styled success result
     */
    public static NMsg ofStyledSuccess(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.success() n text style.success()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.success());
    }

    /**
     * Creates a new instance of of styled striked.
     *
     * @param message message
     * @return of styled striked result
     */
    public static NMsg ofStyledStriked(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.striked() n text style.striked()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.striked());
    }

    /**
     * Creates a new instance of of styled variable.
     *
     * @param message message
     * @return of styled variable result
     */
    public static NMsg ofStyledVariable(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.variable() n text style.variable()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.variable());
    }

    /**
     * Creates a new instance of of styled warn.
     *
     * @param message message
     * @return of styled warn result
     */
    public static NMsg ofStyledWarn(String message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.warn() n text style.warn()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.warn());
    }

    /**
     * Creates a new instance of of styled foreground color.
     *
     * @param message message
     * @param color color
     * @return of styled foreground color result
     */
    public static NMsg ofStyledForegroundColor(String message, int color) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.foregroundColor(color) n text style.foreground color(color)
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.foregroundColor(color));
    }

    /**
     * Creates a new instance of of styled foreground true color.
     *
     * @param message message
     * @param color color
     * @return of styled foreground true color result
     */
    public static NMsg ofStyledForegroundTrueColor(String message, int color) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.foregroundTrueColor(color) n text style.foreground true color(color)
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.foregroundTrueColor(color));
    }

    /**
     * Creates a new instance of of styled background color.
     *
     * @param message message
     * @param color color
     * @return of styled background color result
     */
    public static NMsg ofStyledBackgroundColor(String message, int color) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.backgroundColor(color) n text style.background color(color)
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.backgroundColor(color));
    }

    /**
     * Creates a new instance of of styled background true color.
     *
     * @param message message
     * @param color color
     * @return of styled background true color result
     */
    public static NMsg ofStyledBackgroundTrueColor(String message, int color) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.backgroundTrueColor(color) n text style.background true color(color)
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.backgroundTrueColor(color));
    }

    /**
     * Creates a new instance of of styled foreground true color.
     *
     * @param message message
     * @param color color
     * @return of styled foreground true color result
     */
    public static NMsg ofStyledForegroundTrueColor(String message, NColor color) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.foregroundTrueColor(color) n text style.foreground true color(color)
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.foregroundTrueColor(color));
    }

    /**
     * Creates a new instance of of styled foreground true color.
     *
     * @param message message
     * @param color color
     * @return of styled foreground true color result
     */
    public static NMsg ofStyledForegroundTrueColor(NMsg message, NColor color) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.foregroundTrueColor(color) n text style.foreground true color(color)
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.foregroundTrueColor(color));
    }

    /**
     * Creates a new instance of of styled background true color.
     *
     * @param message message
     * @param color color
     * @return of styled background true color result
     */
    public static NMsg ofStyledBackgroundTrueColor(String message, NColor color) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.backgroundTrueColor(color) n text style.background true color(color)
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.backgroundTrueColor(color));
    }

    /**
     * Creates a new instance of of styled background true color.
     *
     * @param message message
     * @param color color
     * @return of styled background true color result
     */
    public static NMsg ofStyledBackgroundTrueColor(NMsg message, NColor color) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.backgroundTrueColor(color) n text style.background true color(color)
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.backgroundTrueColor(color));
    }

    /**
     * Creates a new instance of of styled background color.
     *
     * @param message message
     * @param color color
     * @return of styled background color result
     */
    public static NMsg ofStyledBackgroundColor(String message, NColor color) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.backgroundColor(color) n text style.background color(color)
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.backgroundColor(color));
    }

    /**
     * Creates a new instance of of styled background color.
     *
     * @param message message
     * @param color color
     * @return of styled background color result
     */
    public static NMsg ofStyledBackgroundColor(NMsg message, NColor color) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.backgroundColor(color) n text style.background color(color)
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.backgroundColor(color));
    }

    /**
     * Creates a new instance of of styled foreground color.
     *
     * @param message message
     * @param color color
     * @return of styled foreground color result
     */
    public static NMsg ofStyledForegroundColor(String message, NColor color) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.foregroundColor(color) n text style.foreground color(color)
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.foregroundColor(color));
    }

    /**
     * Creates a new instance of of styled foreground color.
     *
     * @param message message
     * @param color color
     * @return of styled foreground color result
     */
    public static NMsg ofStyledForegroundColor(NMsg message, NColor color) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.foregroundColor(color) n text style.foreground color(color)
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.foregroundColor(color));
    }

    /**
     * Creates a new instance of of styled keyword.
     *
     * @param message message
     * @return of styled keyword result
     */
    public static NMsg ofStyledKeyword(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.keyword() n text style.keyword()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.keyword());
    }

    /**
     * Creates a new instance of of styled path.
     *
     * @param message message
     * @return of styled path result
     */
    public static NMsg ofStyledPath(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.path() n text style.path()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.path());
    }

    /**
     * Creates a new instance of of styled pale.
     *
     * @param message message
     * @return of styled pale result
     */
    public static NMsg ofStyledPale(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.pale() n text style.pale()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.pale());
    }

    /**
     * Creates a new instance of of styled separator.
     *
     * @param message message
     * @return of styled separator result
     */
    public static NMsg ofStyledSeparator(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.separator() n text style.separator()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.separator());
    }

    /**
     * Creates a new instance of of styled string.
     *
     * @param message message
     * @return of styled string result
     */
    public static NMsg ofStyledString(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.string() n text style.string()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.string());
    }

    /**
     * Creates a new instance of of styled blink.
     *
     * @param message message
     * @return of styled blink result
     */
    public static NMsg ofStyledBlink(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.blink() n text style.blink()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.blink());
    }

    /**
     * Creates a new instance of of styled bold.
     *
     * @param message message
     * @return of styled bold result
     */
    public static NMsg ofStyledBold(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.bold() n text style.bold()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.bold());
    }

    /**
     * Creates a new instance of of styled bool.
     *
     * @param message message
     * @return of styled bool result
     */
    public static NMsg ofStyledBool(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.bool() n text style.bool()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.bool());
    }

    /**
     * Creates a new instance of of styled comments.
     *
     * @param message message
     * @return of styled comments result
     */
    public static NMsg ofStyledComments(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.comments() n text style.comments()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.comments());
    }

    /**
     * Creates a new instance of of styled config.
     *
     * @param message message
     * @return of styled config result
     */
    public static NMsg ofStyledConfig(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.config() n text style.config()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.config());
    }

    /**
     * Creates a new instance of of styled danger.
     *
     * @param message message
     * @return of styled danger result
     */
    public static NMsg ofStyledDanger(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.danger() n text style.danger()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.danger());
    }

    /**
     * Creates a new instance of of styled date.
     *
     * @param message message
     * @return of styled date result
     */
    public static NMsg ofStyledDate(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.date() n text style.date()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.date());
    }

    /**
     * Creates a new instance of of styled error.
     *
     * @param message message
     * @return of styled error result
     */
    public static NMsg ofStyledError(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.error() n text style.error()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.error());
    }

    /**
     * Creates a new instance of of styled fail.
     *
     * @param message message
     * @return of styled fail result
     */
    public static NMsg ofStyledFail(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.fail() n text style.fail()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.fail());
    }

    /**
     * Creates a new instance of of styled info.
     *
     * @param message message
     * @return of styled info result
     */
    public static NMsg ofStyledInfo(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.info() n text style.info()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.info());
    }

    /**
     * Creates a new instance of of styled input.
     *
     * @param message message
     * @return of styled input result
     */
    public static NMsg ofStyledInput(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.input() n text style.input()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.input());
    }

    /**
     * Creates a new instance of of styled italic.
     *
     * @param message message
     * @return of styled italic result
     */
    public static NMsg ofStyledItalic(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.italic() n text style.italic()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.italic());
    }

    /**
     * Creates a new instance of of styled number.
     *
     * @param message message
     * @return of styled number result
     */
    public static NMsg ofStyledNumber(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.number() n text style.number()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.number());
    }

    /**
     * Creates a new instance of of styled operator.
     *
     * @param message message
     * @return of styled operator result
     */
    public static NMsg ofStyledOperator(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.operator() n text style.operator()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.operator());
    }

    /**
     * Creates a new instance of of styled option.
     *
     * @param message message
     * @return of styled option result
     */
    public static NMsg ofStyledOption(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.option() n text style.option()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.option());
    }

    /**
     * Creates a new instance of of styled placeholder.
     *
     * @param message message
     * @return of styled placeholder result
     */
    public static NMsg ofStyledPlaceholder(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.placeholder() n text style.placeholder()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.placeholder());
    }

    /**
     * Creates a new instance of of styled entity.
     *
     * @param message message
     * @return of styled entity result
     */
    public static NMsg ofStyledEntity(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.entity() n text style.entity()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.entity());
    }

    /**
     * Creates a new instance of of styled action.
     *
     * @param message message
     * @return of styled action result
     */
    public static NMsg ofStyledAction(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.action() n text style.action()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.action());
    }

    /**
     * Creates a new instance of of styled annotation.
     *
     * @param message message
     * @return of styled annotation result
     */
    public static NMsg ofStyledAnnotation(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.annotation() n text style.annotation()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.annotation());
    }

    /**
     * Creates a new instance of of styled primary1.
     *
     * @param message message
     * @return of styled primary1 result
     */
    public static NMsg ofStyledPrimary1(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary1() n text style.primary1()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary1());
    }

    /**
     * Creates a new instance of of styled primary2.
     *
     * @param message message
     * @return of styled primary2 result
     */
    public static NMsg ofStyledPrimary2(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary2() n text style.primary2()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary2());
    }

    /**
     * Creates a new instance of of styled primary3.
     *
     * @param message message
     * @return of styled primary3 result
     */
    public static NMsg ofStyledPrimary3(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary3() n text style.primary3()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary3());
    }

    /**
     * Creates a new instance of of styled primary4.
     *
     * @param message message
     * @return of styled primary4 result
     */
    public static NMsg ofStyledPrimary4(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary4() n text style.primary4()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary4());
    }

    /**
     * Creates a new instance of of styled primary5.
     *
     * @param message message
     * @return of styled primary5 result
     */
    public static NMsg ofStyledPrimary5(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary5() n text style.primary5()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary5());
    }

    /**
     * Creates a new instance of of styled primary6.
     *
     * @param message message
     * @return of styled primary6 result
     */
    public static NMsg ofStyledPrimary6(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary6() n text style.primary6()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary6());
    }

    /**
     * Creates a new instance of of styled primary7.
     *
     * @param message message
     * @return of styled primary7 result
     */
    public static NMsg ofStyledPrimary7(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary7() n text style.primary7()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary7());
    }

    /**
     * Creates a new instance of of styled primary8.
     *
     * @param message message
     * @return of styled primary8 result
     */
    public static NMsg ofStyledPrimary8(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary8() n text style.primary8()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary8());
    }

    /**
     * Creates a new instance of of styled primary9.
     *
     * @param message message
     * @return of styled primary9 result
     */
    public static NMsg ofStyledPrimary9(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.primary9() n text style.primary9()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.primary9());
    }

    /**
     * Creates a new instance of of styled secondary1.
     *
     * @param message message
     * @return of styled secondary1 result
     */
    public static NMsg ofStyledSecondary1(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary1() n text style.secondary1()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary1());
    }

    /**
     * Creates a new instance of of styled secondary2.
     *
     * @param message message
     * @return of styled secondary2 result
     */
    public static NMsg ofStyledSecondary2(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary2() n text style.secondary2()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary2());
    }

    /**
     * Creates a new instance of of styled secondary3.
     *
     * @param message message
     * @return of styled secondary3 result
     */
    public static NMsg ofStyledSecondary3(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary3() n text style.secondary3()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary3());
    }

    /**
     * Creates a new instance of of styled secondary4.
     *
     * @param message message
     * @return of styled secondary4 result
     */
    public static NMsg ofStyledSecondary4(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary4() n text style.secondary4()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary4());
    }

    /**
     * Creates a new instance of of styled secondary5.
     *
     * @param message message
     * @return of styled secondary5 result
     */
    public static NMsg ofStyledSecondary5(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary5() n text style.secondary5()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary5());
    }

    /**
     * Creates a new instance of of styled secondary6.
     *
     * @param message message
     * @return of styled secondary6 result
     */
    public static NMsg ofStyledSecondary6(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary6() n text style.secondary6()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary6());
    }

    /**
     * Creates a new instance of of styled secondary7.
     *
     * @param message message
     * @return of styled secondary7 result
     */
    public static NMsg ofStyledSecondary7(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary7() n text style.secondary7()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary7());
    }

    /**
     * Creates a new instance of of styled secondary8.
     *
     * @param message message
     * @return of styled secondary8 result
     */
    public static NMsg ofStyledSecondary8(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary8() n text style.secondary8()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary8());
    }

    /**
     * Creates a new instance of of styled secondary9.
     *
     * @param message message
     * @return of styled secondary9 result
     */
    public static NMsg ofStyledSecondary9(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.secondary9() n text style.secondary9()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.secondary9());
    }

    /**
     * Creates a new instance of of styled title1.
     *
     * @param message message
     * @return of styled title1 result
     */
    public static NMsg ofStyledTitle1(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title1() n text style.title1()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title1());
    }

    /**
     * Creates a new instance of of styled title2.
     *
     * @param message message
     * @return of styled title2 result
     */
    public static NMsg ofStyledTitle2(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title2() n text style.title2()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title2());
    }

    /**
     * Creates a new instance of of styled title3.
     *
     * @param message message
     * @return of styled title3 result
     */
    public static NMsg ofStyledTitle3(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title3() n text style.title3()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title3());
    }

    /**
     * Creates a new instance of of styled title4.
     *
     * @param message message
     * @return of styled title4 result
     */
    public static NMsg ofStyledTitle4(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title4() n text style.title4()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title4());
    }

    /**
     * Creates a new instance of of styled title5.
     *
     * @param message message
     * @return of styled title5 result
     */
    public static NMsg ofStyledTitle5(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title5() n text style.title5()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title5());
    }

    /**
     * Creates a new instance of of styled title6.
     *
     * @param message message
     * @return of styled title6 result
     */
    public static NMsg ofStyledTitle6(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title6() n text style.title6()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title6());
    }

    /**
     * Creates a new instance of of styled title7.
     *
     * @param message message
     * @return of styled title7 result
     */
    public static NMsg ofStyledTitle7(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title7() n text style.title7()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title7());
    }

    /**
     * Creates a new instance of of styled title8.
     *
     * @param message message
     * @return of styled title8 result
     */
    public static NMsg ofStyledTitle8(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title8() n text style.title8()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title8());
    }

    /**
     * Creates a new instance of of styled title9.
     *
     * @param message message
     * @return of styled title9 result
     */
    public static NMsg ofStyledTitle9(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.title9() n text style.title9()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.title9());
    }

    /**
     * Creates a new instance of of styled success.
     *
     * @param message message
     * @return of styled success result
     */
    public static NMsg ofStyledSuccess(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.success() n text style.success()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.success());
    }

    /**
     * Creates a new instance of of styled striked.
     *
     * @param message message
     * @return of styled striked result
     */
    public static NMsg ofStyledStriked(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.striked() n text style.striked()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.striked());
    }

    /**
     * Creates a new instance of of styled variable.
     *
     * @param message message
     * @return of styled variable result
     */
    public static NMsg ofStyledVariable(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.variable() n text style.variable()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.variable());
    }

    /**
     * Creates a new instance of of styled warn.
     *
     * @param message message
     * @return of styled warn result
     */
    public static NMsg ofStyledWarn(NMsg message) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.warn() n text style.warn()
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.warn());
    }

    /**
     * Creates a new instance of of styled foreground color.
     *
     * @param message message
     * @param color color
     * @return of styled foreground color result
     */
    public static NMsg ofStyledForegroundColor(NMsg message, int color) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.foregroundColor(color) n text style.foreground color(color)
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.foregroundColor(color));
    }

    /**
     * Creates a new instance of of styled foreground true color.
     *
     * @param message message
     * @param color color
     * @return of styled foreground true color result
     */
    public static NMsg ofStyledForegroundTrueColor(NMsg message, int color) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.foregroundTrueColor(color) n text style.foreground true color(color)
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.foregroundTrueColor(color));
    }

    /**
     * Creates a new instance of of styled background color.
     *
     * @param message message
     * @param color color
     * @return of styled background color result
     */
    public static NMsg ofStyledBackgroundColor(NMsg message, int color) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.backgroundColor(color) n text style.background color(color)
         * @return of styled result
         */
        return ofStyled(message, NTextStyle.backgroundColor(color));
    }

    /**
     * Creates a new instance of of styled background true color.
     *
     * @param message message
     * @param color color
     * @return of styled background true color result
     */
    public static NMsg ofStyledBackgroundTrueColor(NMsg message, int color) {
        /**
         * Creates a new instance of of styled.
         *
         * @param message message
         * @param NTextStyle.backgroundTrueColor(color) n text style.background true color(color)
         * @return of styled result
         */
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

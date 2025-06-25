/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NLiteral;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Command Line Argument
 *
 * @author thevpc
 * @app.category Command Line
 * @since 0.5.5
 */
public interface NArg extends NBlankable /*extends NLiteral*/ {

    /**
     * create instance for the given value and with the given session
     *
     * @param value value
     * @return new instance
     */
    static NArg of(String value) {
        return new DefaultNArg(value);
    }

    /**
     * true if the argument starts with '-' or '+'
     *
     * @return true if the argument starts with '-'
     */
    boolean isOption();

    /**
     * true if the argument do not start with '-' or '+' or is blank. this is
     * equivalent to {@code !isOption()}.
     *
     * @return true if the argument do not start with '-' or '+'
     */
    boolean isNonOption();

    /**
     * equivalent to getStringKey().orElse("")
     *
     * @return non null key as string
     */
    String key();

    /**
     * equivalent to getStringValue().orElse(null)
     *
     * @return string as string
     */
    String value();

    NOptional<String> getStringKey();

    NOptional<String> getStringValue();

    String stringValue();

    /**
     * true if option is in one of the following forms :
     * <ul>
     * <li>-!name[=...]</li>
     * <li>--!name[=...]</li>
     * <li>!name[=...]</li>
     * </ul>
     * where name is any valid identifier
     *
     * @return true if the argument is negated
     */
    boolean isNegated();

    /**
     * true if not negated
     *
     * @return true if not negated
     */
    boolean isEnabled();

    /**
     * false if option is in one of the following forms :
     * <ul>
     * <li>-//name</li>
     * <li>--//name</li>
     * </ul>
     * where name is any valid identifier
     *
     * @return true if the argument is enable and false if It's commented
     */
    boolean isNonCommented();

    /**
     * true if option is in one of the following forms :
     * <ul>
     * <li>-//name</li>
     * <li>--//name</li>
     * </ul>
     * where name is any valid identifier
     *
     * @return true if the argument is enable and false if It's commented
     */
    boolean isCommented();

    /**
     * Throw an exception if the argument is null
     *
     * @return {@code this} instance
     */
    NArg required();

    /**
     * true if the argument is in the form key=value
     *
     * @return true if the argument is in the form key=value
     */
    boolean isKeyValue();

    /**
     * return option prefix part ('-' and '--')
     *
     * @return option prefix part ('-' and '--')
     * @since 0.5.7
     */
    NLiteral getOptionPrefix();

    /**
     * return query value separator
     *
     * @return query value separator
     * @since 0.5.7
     */
    String getSeparator();

    /**
     * return option key part excluding prefix ('-' and '--')
     *
     * @return option key part excluding prefix ('-' and '--')
     * @since 0.5.7
     */
    NLiteral getOptionName();

    /**
     * return new instance (never null) of the value part of the argument (after
     * =). However Argument's value may be null (
     * {@code getArgumentValue().getString() == null}). Here are some examples
     * of getArgumentValue() result for some common arguments
     * <ul>
     * <li>Argument("key").getArgumentValue() ==&gt; Argument(null) </li>
     * <li>Argument("key=value").getArgumentValue() ==&gt; Argument("value")
     * </li>
     * <li>Argument("key=").getArgumentValue() ==&gt; Argument("") </li>
     * <li>Argument("--key=value").getArgumentValue() ==&gt; Argument("value")
     * </li>
     * <li>Argument("--!key=value").getArgumentValue() ==&gt; Argument("value")
     * </li>
     * <li>Argument("--!//key=value").getArgumentValue() ==&gt;
     * Argument("value") </li>
     * </ul>
     *
     * @return new instance (never null) of the value part of the argument
     * (after =)
     */
    NLiteral getValue();

    NOptional<Boolean> getBooleanValue();

    boolean booleanValue();

    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<Integer> getIntValue();

    /**
     * @return value
     * @since 0.8.6
     */
    int intValue();

    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<Long> getLongValue();

    /**
     * @return value
     * @since 0.8.6
     */
    double doubleValue();

    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<Double> getDoubleValue();

    /**
     * @return value
     * @since 0.8.6
     */
    float floatValue();

    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<Float> getFloatValue();


    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<BigInteger> getBigIntValue();

    /**
     * @return value
     * @since 0.8.6
     */
    BigInteger bigIntValue();


    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<BigDecimal> getBigDecimalValue();

    /**
     * @return value
     * @since 0.8.6
     */
    BigDecimal bigDecimalValue();

    /**
     * @return value
     * @since 0.8.6
     */
    long longValue();

    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<LocalDate> getLocalDateValue();

    /**
     * @return value
     * @since 0.8.6
     */
    LocalDate localDateValue();

    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<LocalTime> getLocalTimeValue();

    /**
     * @return value
     * @since 0.8.6
     */
    LocalDateTime localTimeValue();

    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<LocalDateTime> getLocalDateTimeValue();

    /**
     * @return value
     * @since 0.8.6
     */
    LocalDateTime localDateTimeValue();

    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<Instant> getInstantValue();

    /**
     * @return value
     * @since 0.8.6
     */
    Instant instantValue();

    /**
     * return key part (never null) of the argument. The key does not include
     * neither ! nor // or = argument parts as they are parsed separately. Here
     * are some examples of getStringKey() result for some common arguments
     * <ul>
     * <li>Argument("key").getKey() ==&gt; "key" </li>
     * <li>Argument("key=value").getKey() ==&gt; "key" </li>
     * <li>Argument("--key=value").getKey() ==&gt; "--key"
     * </li>
     * <li>Argument("--!key=value").getKey() ==&gt; "--key"
     * </li>
     * <li>Argument("--!//key=value").getKey() ==&gt; "--key"
     * </li>
     * <li>Argument("--//!key=value").getKey() ==&gt; "--key"
     * </li>
     * </ul>
     * equivalent to {@code getKey().getString()}
     *
     * @return string key
     */
    NLiteral getKey();

    String getImage();

    boolean isFlagOption();

    NOptional<String> asString();

    NOptional<Boolean> asBoolean();

    boolean isBoolean();

    boolean isInt();

    NOptional<Integer> asInt();

    boolean isLong();

    NOptional<Long> asLong();
}

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

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NSessionProvider;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.reserved.rpi.NIORPI;

import java.util.List;

/**
 * Question is helpful object that permits user interaction by reading a typed object from
 * standard input or an equivalent input system.
 *
 * @param <T> value type returned by this question object
 * @author thevpc
 * @app.category Toolkit
 * @since 0.5.4
 */
public interface NAsk<T> extends NCmdLineConfigurable, NSessionProvider {

    static <T> NAsk<T> of(NSession session) {
        return NIORPI.of(session).createQuestion(session);
    }

    static <T> NAsk<T> of(NSessionTerminal terminal) {
        return NIORPI.of(terminal.getSession()).createQuestion(terminal);
    }

    boolean isResetLine();

    NAsk<T> resetLine();

    NAsk<T> resetLine(boolean resetLine);

    /**
     * ask for message of type Boolean
     *
     * @param msg message
     * @return ask for message of type Boolean
     * @since 0.8.3
     */
    NAsk<Boolean> forBoolean(NMsg msg);

    /**
     * ask for message of type Password
     *
     * @param msg message
     * @return ask for message of type Password
     * @since 0.8.3
     */
    NAsk<char[]> forPassword(NMsg msg);

    /**
     * ask for message of type String
     *
     * @param msg message
     * @return ask for message of type String
     * @since 0.8.3
     */
    NAsk<String> forString(NMsg msg);

    /**
     * ask for message of type int
     *
     * @param msg message
     * @return ask for message of type int
     * @since 0.8.3
     */
    NAsk<Integer> forInteger(NMsg msg);

    /**
     * ask for message of type long
     *
     * @param msg message
     * @return ask for message of type long
     * @since 0.8.3
     */
    NAsk<Long> forLong(NMsg msg);

    /**
     * ask for message of type float
     *
     * @param msg message
     * @return ask for message of type float
     * @since 0.8.3
     */
    NAsk<Float> forFloat(NMsg msg);

    /**
     * ask for message of type double
     *
     * @param msg message
     * @return ask for message of type double
     * @since 0.8.3
     */
    NAsk<Double> forDouble(NMsg msg);

    /**
     * ask for message of type enum
     *
     * @param msg      message
     * @param enumType enumType
     * @return ask for message of type enum
     * @since 0.8.3
     */
    <K extends Enum> NAsk<K> forEnum(Class<K> enumType, NMsg msg);


    NMsg getHintMessage();

    NAsk<T> setHintMessage(NMsg message);

    NMsg getMessage();

    /**
     * set message
     *
     * @param message message
     * @return {@code this} instance
     * @since 0.8.3
     */
    NAsk<T> setMessage(NMsg message);

    NMsg getCancelMessage();

    NAsk<T> setCancelMessage(NMsg message);

    List<Object> getAcceptedValues();

    NAsk<T> setAcceptedValues(List<Object> acceptedValues);

    T getDefaultValue();

    NAsk<T> setDefaultValue(T defaultValue);

    Class<T> getValueType();

    NAsk<T> setValueType(Class<T> valueType);

    NAskFormat<T> getFormat();

    NAsk<T> setFormat(NAskFormat<T> format);

    NAskParser<T> getParser();

    NAsk<T> setParser(NAskParser<T> parser);

    NAskValidator<T> getValidator();

    NAsk<T> setValidator(NAskValidator<T> validator);

    NAsk<T> run();

    /**
     * equivalent to (Boolean) getValue() as type dereferencing may cause some
     * troubles
     *
     * @return true or false or null
     */
    Boolean getBooleanValue();

    T getValue();

    NAsk<T> setSession(NSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NAsk<T> configure(boolean skipUnsupported, String... args);

}

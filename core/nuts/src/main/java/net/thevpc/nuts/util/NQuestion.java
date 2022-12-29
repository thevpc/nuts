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
package net.thevpc.nuts.util;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCommandLineConfigurable;

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
public interface NQuestion<T> extends NCommandLineConfigurable {

    boolean isResetLine();

    NQuestion<T> resetLine();

    NQuestion<T> resetLine(boolean resetLine);

    /**
     * ask for message of type Boolean
     *
     * @param msg message
     * @return ask for message of type Boolean
     * @since 0.8.3
     */
    NQuestion<Boolean> forBoolean(NMsg msg);

    /**
     * ask for message of type Password
     *
     * @param msg message
     * @return ask for message of type Password
     * @since 0.8.3
     */
    NQuestion<char[]> forPassword(NMsg msg);

    /**
     * ask for message of type String
     *
     * @param msg message
     * @return ask for message of type String
     * @since 0.8.3
     */
    NQuestion<String> forString(NMsg msg);

    /**
     * ask for message of type int
     *
     * @param msg message
     * @return ask for message of type int
     * @since 0.8.3
     */
    NQuestion<Integer> forInteger(NMsg msg);

    /**
     * ask for message of type long
     *
     * @param msg message
     * @return ask for message of type long
     * @since 0.8.3
     */
    NQuestion<Long> forLong(NMsg msg);

    /**
     * ask for message of type float
     *
     * @param msg message
     * @return ask for message of type float
     * @since 0.8.3
     */
    NQuestion<Float> forFloat(NMsg msg);

    /**
     * ask for message of type double
     *
     * @param msg message
     * @return ask for message of type double
     * @since 0.8.3
     */
    NQuestion<Double> forDouble(NMsg msg);

    /**
     * ask for message of type enum
     *
     * @param msg message
     * @param enumType enumType
     * @return ask for message of type enum
     * @since 0.8.3
     */
    <K extends Enum> NQuestion<K> forEnum(Class<K> enumType, NMsg msg);


    NMsg getHintMessage();

    NQuestion<T> setHintMessage(NMsg message);

    NMsg getMessage();

    /**
     * set message
     *
     * @param message message
     * @return {@code this} instance
     * @since 0.8.3
     */
    NQuestion<T> setMessage(NMsg message);

    NMsg getCancelMessage();

    NQuestion<T> setCancelMessage(NMsg message);

    List<Object> getAcceptedValues();

    NQuestion<T> setAcceptedValues(List<Object> acceptedValues);

    T getDefaultValue();

    NQuestion<T> setDefaultValue(T defaultValue);

    Class<T> getValueType();

    NQuestion<T> setValueType(Class<T> valueType);

    NQuestionFormat<T> getFormat();

    NQuestion<T> setFormat(NQuestionFormat<T> format);

    NQuestionParser<T> getParser();

    NQuestion<T> setParser(NQuestionParser<T> parser);

    NQuestionValidator<T> getValidator();

    NQuestion<T> setValidator(NQuestionValidator<T> validator);

    NQuestion<T> run();

    /**
     * equivalent to (Boolean) getValue() as type dereferencing may cause some
     * troubles
     *
     * @return true or false or null
     */
    Boolean getBooleanValue();

    T getValue();

    NSession getSession();

    NQuestion<T> setSession(NSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCommandLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NQuestion<T> configure(boolean skipUnsupported, String... args);

}

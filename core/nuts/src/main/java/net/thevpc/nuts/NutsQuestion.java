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
package net.thevpc.nuts;

/**
 * Question is helpful object that permits user interaction by reading a typed object from
 * standard input or an equivalent input system.
 * @param <T> value type returned by this question object
 * @author thevpc
 * @since 0.5.4
 * @app.category Toolkit
 */
public interface NutsQuestion<T> extends NutsCommandLineConfigurable {

    boolean isResetLine();

    NutsQuestion<T> resetLine();

    NutsQuestion<T> resetLine(boolean resetLine);

    NutsQuestion<Boolean> forBoolean(String msg, Object... params);

    NutsQuestion<char[]> forPassword(String msg, Object... params);

    NutsQuestion<String> forString(String msg, Object... params);

    NutsQuestion<Integer> forInteger(String msg, Object... params);

    NutsQuestion<Long> forLong(String msg, Object... params);

    NutsQuestion<Float> forFloat(String msg, Object... params);

    NutsQuestion<Double> forDouble(String msg, Object... params);

    <K extends Enum> NutsQuestion<K> forEnum(Class<K> enumType, String msg, Object... params);

    /**
     * ask for message of type Boolean
     * @param msg message
     * @return ask for message of type Boolean
     * @since 0.8.3
     * */
    NutsQuestion<Boolean> forBoolean(NutsMessage msg);

    /**
     * ask for message of type Password
     * @param msg message
     * @return ask for message of type Password
     * @since 0.8.3
     * */
    NutsQuestion<char[]> forPassword(NutsMessage msg);

    /**
     * ask for message of type String
     * @param msg message
     * @return ask for message of type String
     * @since 0.8.3
     * */
    NutsQuestion<String> forString(NutsMessage msg);

    /**
     * ask for message of type int
     * @param msg message
     * @return ask for message of type int
     * @since 0.8.3
     * */
    NutsQuestion<Integer> forInteger(NutsMessage msg);

    /**
     * ask for message of type long
     * @param msg message
     * @return ask for message of type long
     * @since 0.8.3
     * */
    NutsQuestion<Long> forLong(NutsMessage msg);

    /**
     * ask for message of type float
     * @param msg message
     * @return ask for message of type float
     * @since 0.8.3
     * */
    NutsQuestion<Float> forFloat(NutsMessage msg);

    /**
     * ask for message of type double
     * @param msg message
     * @return ask for message of type double
     * @since 0.8.3
     * */
    NutsQuestion<Double> forDouble(NutsMessage msg);

    /**
     * ask for message of type enum
     * @param msg message
     * @return ask for message of type enum
     * @since 0.8.3
     * */
    <K extends Enum> NutsQuestion<K> forEnum(Class<K> enumType, NutsMessage msg);


    NutsMessage getHintMessage();

    NutsMessage getMessage();

    NutsMessage getCancelMessage();

    /**
     * set message
     * @param message message
     * @return {@code this} instance
     * @since 0.8.3
     * */
    NutsQuestion<T> setMessage(NutsMessage message);
    NutsQuestion<T> setHintMessage(NutsMessage message);

    Object[] getAcceptedValues();

    NutsQuestion<T> setAcceptedValues(Object[] acceptedValues);

    T getDefaultValue();


    NutsQuestion<T> setDefaultValue(T defaultValue);

    Class<T> getValueType();

    NutsQuestion<T> setValueType(Class<T> valueType);

    NutsQuestionFormat<T> getFormat();

    NutsQuestion<T> setFormat(NutsQuestionFormat<T> format);

    NutsQuestionParser<T> getParser();

    NutsQuestion<T> setParser(NutsQuestionParser<T> parser);

    NutsQuestionValidator<T> getValidator();

    NutsQuestion<T> setValidator(NutsQuestionValidator<T> validator);

    NutsQuestion<T> run();

    /**
     * equivalent to (Boolean) getValue() as type dereferencing may cause some
     * troubles
     *
     * @return true or false or null
     */
    Boolean getBooleanValue();

    T getValue();

    NutsSession getSession();

    NutsQuestion<T> setSession(NutsSession session);

    NutsQuestion<T> setCancelMessage(NutsMessage message);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsQuestion<T> configure(boolean skipUnsupported, String... args);

}

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
 *
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
 * @category Base
 */
public interface NutsQuestion<T> extends NutsConfigurable {

    NutsQuestion<Boolean> forBoolean(String msg, Object... params);

    NutsQuestion<char[]> forPassword(String msg, Object... params);

    NutsQuestion<String> forString(String msg, Object... params);

    NutsQuestion<Integer> forInteger(String msg, Object... params);

    NutsQuestion<Long> forLong(String msg, Object... params);

    NutsQuestion<Float> forFloat(String msg, Object... params);

    NutsQuestion<Double> forDouble(String msg, Object... params);

    <K extends Enum> NutsQuestion<K> forEnum(Class<K> enumType, String msg, Object... params);


    String getHintMessage();

    Object[] getHintMessageParameters();

    String getMessage();

    Object[] getMessageParameters();

    String getCancelMessage();

    Object[] getCancelMessageParameters();

    NutsQuestion<T> message(String message, Object... messageParameters);

    NutsQuestion<T> setMessage(String message, Object... messageParameters);

    NutsQuestion<T> hintMessage(String message, Object... messageParameters);

    NutsQuestion<T> setHintMessage(String message, Object... messageParameters);

    Object[] getAcceptedValues();

    NutsQuestion<T> acceptedValues(Object[] acceptedValues);

    NutsQuestion<T> setAcceptedValues(Object[] acceptedValues);

    T getDefaultValue();

    NutsQuestion<T> defaultValue(T defautValue);

    NutsQuestion<T> setDefaultValue(T defaultValue);

    Class<T> getValueType();

    NutsQuestion<T> valueType(Class<T> valueType);

    NutsQuestion<T> setValueType(Class<T> valueType);

    NutsQuestionFormat<T> getFormat();

    NutsQuestion<T> format(NutsQuestionFormat<T> format);

    NutsQuestion<T> setFormat(NutsQuestionFormat<T> format);

    NutsQuestionParser<T> getParser();

    NutsQuestion<T> parser(NutsQuestionParser<T> parser);

    NutsQuestion<T> setParser(NutsQuestionParser<T> parser);

    NutsQuestion<T> validator(NutsQuestionValidator<T> validator);

    NutsQuestion<T> setValidator(NutsQuestionValidator<T> validator);

    NutsQuestionValidator<T> getValidator();

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

    NutsQuestion<T> setCancelMessage(String message,Object... params);

    NutsQuestion<T> setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsQuestion<T> configure(boolean skipUnsupported, String... args);

}

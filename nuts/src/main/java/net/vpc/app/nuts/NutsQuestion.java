/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 * Question is helpful object that permits user interaction by reading a type object from
 * standard input or an equivalent input system.
 * @param <T> value type returned by this question object
 * @author vpc
 * @since 0.5.4
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

    String getMessage();

    NutsQuestion<T> message(String message, Object... messageParameters);

    NutsQuestion<T> setMessage(String message, Object... messageParameters);

    NutsQuestion<T> message(String message);

    NutsQuestion<T> setMessage(String message);

    Object[] getMessageParameters();

    NutsQuestion<T> messageParameters(Object... messageParameters);

    NutsQuestion<T> setMessageParameters(Object... messageParameters);

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

    NutsQuestion<T> session(NutsSession session);

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

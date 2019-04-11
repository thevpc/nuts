/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

public class NutsQuestion<T> {

    private String message;
    private Object[] messageParameters;
    private Object[] acceptedValues;
    private Object defautValue;
    private Class<T> valueType;
    private NutsResponseParser parser;

    public static NutsQuestion<Boolean> forBoolean(String msg, Object... params) {
        return new NutsQuestion<>(Boolean.class).setMessage(msg, params);
    }

    public static NutsQuestion<String> forString(String msg, Object... params) {
        return new NutsQuestion<>(String.class).setMessage(msg, params);
    }

    public static NutsQuestion<Integer> forInteger(String msg, Object... params) {
        return new NutsQuestion<>(Integer.class).setMessage(msg, params);
    }

    public static NutsQuestion<Long> forLong(String msg, Object... params) {
        return new NutsQuestion<>(Long.class).setMessage(msg, params);
    }

    public static NutsQuestion<Float> forFloat(String msg, Object... params) {
        return new NutsQuestion<>(Float.class).setMessage(msg, params);
    }

    public static NutsQuestion<Double> forDouble(String msg, Object... params) {
        return new NutsQuestion<>(Double.class).setMessage(msg, params);
    }

    public static <K extends Enum> NutsQuestion<K> forEnum(Class<K> enumType, String msg, Object... params) {
        K[] values = enumType.getEnumConstants();
        return new NutsQuestion<>(enumType)
                .setMessage(msg, params)
                .setAcceptedValues(values);
    }

    public NutsQuestion(Class<T> valueType) {
        this.valueType = valueType;
    }

    public String getMessage() {
        return message;
    }

    public NutsQuestion<T> message(String message, Object... messageParameters) {
        return setMessage(message, messageParameters);
    }

    public NutsQuestion<T> setMessage(String message, Object... messageParameters) {
        this.message = message;
        this.messageParameters = messageParameters;
        return this;
    }

    public NutsQuestion<T> message(String message) {
        return setMessage(message);
    }

    public NutsQuestion<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public Object[] getMessageParameters() {
        return messageParameters;
    }

    public NutsQuestion<T> messageParameters(Object... messageParameters) {
        return setMessageParameters(messageParameters);
    }

    public NutsQuestion<T> setMessageParameters(Object... messageParameters) {
        this.messageParameters = messageParameters;
        return this;
    }

    public Object[] getAcceptedValues() {
        return acceptedValues;
    }

    public NutsQuestion<T> acceptedValues(Object[] acceptedValues) {
        return setAcceptedValues(acceptedValues);
    }

    public NutsQuestion<T> setAcceptedValues(Object[] acceptedValues) {
        this.acceptedValues = acceptedValues;
        return this;
    }

    public Object getDefautValue() {
        return defautValue;
    }

    public NutsQuestion<T> defautValue(Object defautValue) {
        return setDefautValue(defautValue);
    }

    public NutsQuestion<T> setDefautValue(Object defautValue) {
        this.defautValue = defautValue;
        return this;
    }

    public Class getValueType() {
        return valueType;
    }

    public NutsQuestion<T> valueType(Class valueType) {
        return setValueType(valueType);
    }

    public NutsQuestion<T> setValueType(Class valueType) {
        this.valueType = valueType;
        return this;
    }

    public NutsResponseParser getParser() {
        return parser;
    }

    public NutsQuestion<T> parser(NutsResponseParser parser) {
        return setParser(parser);
    }

    public NutsQuestion<T> setParser(NutsResponseParser parser) {
        this.parser = parser;
        return this;
    }
}

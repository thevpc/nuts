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

/**
 *
 * @author vpc
 * @param <T>
 * @since 0.5.4
 */
public interface NutsQuestion<T> extends NutsConfigurable {

    public NutsQuestion<Boolean> forBoolean(String msg, Object... params);

    public NutsQuestion<String> forString(String msg, Object... params);

    public NutsQuestion<Integer> forInteger(String msg, Object... params);

    public NutsQuestion<Long> forLong(String msg, Object... params);

    public NutsQuestion<Float> forFloat(String msg, Object... params);

    public NutsQuestion<Double> forDouble(String msg, Object... params);

    public <K extends Enum> NutsQuestion<K> forEnum(Class<K> enumType, String msg, Object... params);

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

    Object getDefaultValue();

    NutsQuestion<T> defaultValue(Object defautValue);

    NutsQuestion<T> setDefaultValue(Object defaultValue);

    Class getValueType();

    NutsQuestion<T> valueType(Class valueType);

    NutsQuestion<T> setValueType(Class valueType);

    NutsResponseParser getParser();

    NutsQuestion<T> parser(NutsResponseParser parser);

    NutsQuestion<T> setParser(NutsResponseParser parser);

    NutsQuestion<T> run();

    /**
     * equivalent to (Boolean) getResult()
     * as type dereferencing may cause some troubles
     * @return 
     */
    Boolean getBooleanResult();
    
    <T> T getResult();

    NutsSession getSession();
    
    NutsQuestion<T> session(NutsSession session);

    NutsQuestion<T> setSession(NutsSession session);

    @Override
    NutsQuestion<T> configure(String... args);
    
}

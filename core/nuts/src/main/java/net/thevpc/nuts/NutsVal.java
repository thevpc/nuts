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
 * NutsVal is a simple null-safe wrapper for objects and strings that can be parsed as numbers or other common types.
 * This wrapper is helpful when manipulating "arguments", "options", and "properties".
 *
 * @see net.thevpc.nuts.NutsArgument
 * @see net.thevpc.nuts.NutsBootManager
 * @see net.thevpc.nuts.NutsWorkspaceEnvManager
 * @see net.thevpc.nuts.NutsUtilManager
 */
public interface NutsVal extends NutsBlankable {
    /**
     * create a wrapped instance for the given value
     * @param value value
     * @param session session
     * @return a wrapped instance for the given value
     */
    static NutsVal of(Object value, NutsSession session) {
        return session.getWorkspace().util().valOf(value);
    }

    /**
     * return true if is value is blank
     *
     * @return true if is value is blank
     */
    boolean isBlank();

    /**
     * return true if is value is blank
     *
     * @return true if is value is blank
     */
    boolean isNull();

    /**
     * return best effort to convert the value to an int. Throws an error when fails
     *
     * @return best effort to convert the value to an int
     */
    int getInt();

    /**
     * return best effort to convert the value to an int. return {@code emptyOrErrorValue} when blank or  fails
     *
     * @return best effort to convert the value to an int
     */
    Integer getInt(Integer emptyOrErrorValue);

    /**
     * return best effort to convert the value to an int. return {@code emptyValue} whe blank and {@code errorValue} when fails
     *
     * @return best effort to convert the value to an int
     */
    Integer getInt(Integer emptyValue, Integer errorValue);

    /**
     * return true if the object can be resolved as a valid long
     * @return true if the object can be resolved as a valid integer
     */
    boolean isLong();

    /**
     * return best effort to convert the value to a long. return {@code emptyOrErrorValue} when blank or  fails
     *
     * @return best effort to convert the value to a long
     */
    Long getLong(Long emptyOrErrorValue);

    /**
     * return best effort to convert the value to a long. return {@code emptyValue} whe blank and {@code errorValue} when fails
     *
     * @return best effort to convert the value to a long
     */
    Long getLong(Long emptyValue, Long errorValue);

    /**
     * return best effort to convert the value to a long. Throws an error when fails
     *
     * @return best effort to convert the value to a long
     */
    long getLong();

    /**
     * return true if the object can be resolved as a valid double
     * @return return true if the object can be resolved as a valid double
     */
    boolean isDouble();

    /**
     * return true if the object can be resolved as a valid float
     * @return true if the object can be resolved as a valid integer
     */
    boolean isFloat();

    /**
     * return best effort to convert the value to a double. return {@code emptyOrErrorValue} when blank or  fails
     *
     * @return best effort to convert the value to a double
     */
    Double getDouble(Double emptyOrErrorValue);

    /**
     * return best effort to convert the value to a double. return {@code emptyValue} whe blank and {@code errorValue} when fails
     *
     * @return best effort to convert the value to a double
     */
    Double getDouble(Double emptyValue, Double errorValue);

    /**
     * return best effort to convert the value to a double. Throws an error when fails
     *
     * @return best effort to convert the value to a double
     */
    double getDouble();


    /**
     * return best effort to convert the value to a float. return {@code emptyOrErrorValue} when blank or  fails
     *
     * @return best effort to convert the value to a float
     */
    Float getFloat(Float emptyOrErrorValue);

    /**
     * return best effort to convert the value to a float. return {@code emptyValue} whe blank and {@code errorValue} when fails
     *
     * @return best effort to convert the value to a float
     */
    Float getFloat(Float emptyValue, Float errorValue);

    /**
     * return best effort to convert the value to a float. Throws an error when fails
     *
     * @return best effort to convert the value to a float
     */
    float getFloat();

    /**
     * return true if the object can be resolved as a valid boolean
     * @return true if the object can be resolved as a valid integer
     */
    boolean isBoolean();

    /**
     * return true if the object can be resolved as a valid integer
     * @return true if the object can be resolved as a valid integer
     */
    boolean isInt();

    /**
     * return best effort to convert the value to a boolean. When fails returns a default value depending on the semantics.
     *
     * @return best effort to convert the value to a boolean
     */
    boolean getBoolean();

    /**
     * return best effort to convert the value to a boolean. return {@code emptyOrErrorValue} when blank or  fails
     *
     * @return best effort to convert the value to a boolean
     */
    Boolean getBoolean(Boolean emptyOrErrorValue);

    /**
     * return best effort to convert the value to a boolean. return {@code emptyValue} whe blank and {@code errorValue} when fails
     *
     * @return best effort to convert the value to a boolean
     */
    Boolean getBoolean(Boolean emptyValue, Boolean errorValue);


    /**
     * return true if the object can be resolved as a valid string (is a CharSequence)
     * @return true if the object can be resolved as a valid integer
     */
    boolean isString();

    /**
     * return null if the value is null or calls Object.toString()
     * @return null if the value is null or calls Object.toString()
     */
    String getString();

    /**
     *  return {@code defaultValue} if the value is null or calls {@code Object.toString()}
     * @param defaultValue defaultValue
     * @return {@code defaultValue} if the value is null or calls {@code Object.toString()}
     */
    String getString(String defaultValue);

    /**
     * return the related object defining the current value
     * @return the related object defining the current value
     */
    Object getObject();

    /**
     *  return {@code defaultValue} if the value is null or the current value
     * @param defaultValue defaultValue
     * @return {@code defaultValue} if the value is null or the current value
     */
    Object getObject(Object defaultValue);

}

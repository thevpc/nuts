/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
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

import java.time.Instant;

/**
 * primitive values implementation of Nuts Element type. Nuts Element types are
 * generic JSON like parsable objects.
 *
 * @author thevpc
 * @since 0.5.6
 * @category Elements
 */
public interface NutsPrimitiveElement extends NutsElement {

    /**
     * value as any java Object
     *
     * @return value as any java Object
     */
    Object getValue();

    /**
     * value as any java date. Best effort is applied to convert to this type.
     *
     * @return value as java Date
     */
    Instant getInstant();

    /**
     * value as any java Number. Best effort is applied to convert to this type.
     *
     * @return value as java Number
     */
    Number getNumber();

    /**
     * value as any java Boolean. Best effort is applied to convert to this
     * type.
     *
     * @return value as java Boolean
     */
    boolean getBoolean();

    /**
     * value as any java Double. Best effort is applied to convert to this type.
     *
     * @return value as java double
     */
    double getDouble();

    /**
     * value as any java Float. Best effort is applied to convert to this type.
     *
     * @return value as java float
     */
    float getFloat();

    /**
     * value as any java Integer. Best effort is applied to convert to this
     * type.
     *
     * @return value as java integer
     */
    int getInt();

    /**
     * value as any java Integer. Best effort is applied to convert to this
     * type.
     *
     * @return value as java integer
     */
    byte getByte();

    /**
     * value as any java Integer. Best effort is applied to convert to this
     * type.
     *
     * @return value as java integer
     */
    short getShort();

    /**
     * value as any java Long. Best effort is applied to convert to this type.
     *
     * @return value as java long
     */
    long getLong();

//    /**
//     * value as Nuts String.
//     *
//     * @return value as Nuts String
//     */
//    NutsString getNutsString();

    /**
     * value as any java string. Best effort is applied to convert to this type.
     *
     * @return value as java string
     */
    String getString();

    /**
     * true if the value is or can be converted to double
     *
     * @return true if the value is or can be converted to double
     */
    boolean isDouble();

    /**
     * true if the value is or can be converted to float
     *
     * @return true if the value is or can be converted to double
     */
    boolean isFloat();

    /**
     * true if the value is or can be converted to int.
     *
     * @return true if the value is or can be converted to int
     */
    boolean isByte();

    /**
     * true if the value is or can be converted to int.
     *
     * @return true if the value is or can be converted to int
     */
    boolean isShort();

    /**
     * true if the value is or can be converted to int.
     *
     * @return true if the value is or can be converted to int
     */
    boolean isInt();

    /**
     * true if the value is or can be converted to long.
     *
     * @return true if the value is or can be converted to long
     */
    boolean isLong();

    /**
     * true if the value is null (in which case, the type should be NULL)
     *
     * @return true if the value is null (in which case, the type is NULL)
     */
    boolean isNull();

}

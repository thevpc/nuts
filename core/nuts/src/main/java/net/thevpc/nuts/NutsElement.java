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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import java.time.Instant;

/**
 * Nuts Element types are generic JSON like parsable objects.
 *
 * @author thevpc
 * @app.category Elements
 * @since 0.5.6
 */
public interface NutsElement {

    /**
     * element type
     *
     * @return element type
     */
    NutsElementType type();

    /**
     * convert this element to {@link NutsPrimitiveElement} or throw
     * ClassCastException
     *
     * @return {@link NutsPrimitiveElement}
     */
    NutsPrimitiveElement asPrimitive();

    /**
     * convert this element to {@link NutsObjectElement} or throw
     * ClassCastException
     *
     * @return {@link NutsObjectElement}
     */
    NutsObjectElement asObject();

    /**
     * convert this element to {@link NutsArrayElement} or throw
     * ClassCastException
     *
     * @return {@link NutsArrayElement}
     */
    NutsArrayElement asArray();

    NutsObjectElement asSafeObject();

    NutsArrayElement asSafeArray();

    boolean isPrimitive();

    boolean isNumber();

    boolean isNull();

    boolean isString();

//    public boolean isNutsString();

    boolean isByte();

    boolean isInt();

    boolean isLong();

    boolean isShort();

    boolean isFloat();

    boolean isDouble();

    boolean isObject();

    boolean isArray();

    boolean isInstant();

    String asString();

    boolean isEmpty();

//    NutsString asNutsString();

    boolean asBoolean();

    byte asByte();

    double asDouble();

    float asFloat();

    Instant asInstant();

    Integer asSafeInt();

    int asSafeInt(int def);

    Long asSafeLong();

    long asSafeLong(long def);

    Double asSafeDouble();

    short asSafeShort(short def);

    Short asSafeShort();

    byte asSafeByte(byte def);

    Byte asSafeByte();

    double asSafeDouble(double def);

    Float asSafeFloat();

    float asSafeFloat(float def);

    String asSafeString(String def);

    String asSafeString();

    int asInt();

    long asLong();

    short asShort();


}

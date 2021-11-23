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
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Object implementation of Nuts Element type. Nuts Element types are generic
 * JSON like parsable objects.
 *
 * @author thevpc
 * @app.category Elements
 * @since 0.5.6
 */
public interface NutsObjectElement extends NutsElement, Iterable<NutsElementEntry> {

    /**
     * return value for name or null. If multiple values are available return
     * any of them.
     *
     * @param key key name
     * @return value for name or null
     */
    NutsElement get(String key);

    NutsElement get(NutsElement key);

    NutsElement getSafe(String key);

    Integer getSafeInt(String key);

    Integer getSafeInt(String key, int def);

    String getSafeString(String key);

    String getSafeString(String key, String def);

    NutsElement getSafe(NutsElement key);

    NutsArrayElement getSafeArray(String key);

    NutsArrayElement getSafeArray(NutsElement key);

    NutsObjectElement getSafeObject(String key);

    NutsObjectElement getSafeObject(NutsElement key);

    NutsArrayElement getArray(String key);

    NutsArrayElement getArray(NutsElement key);

    NutsObjectElement getObject(String key);

    NutsObjectElement getObject(NutsElement key);

    String getString(String key);

    String getString(NutsElement key);

    boolean getBoolean(String key);

    boolean getBoolean(NutsElement key);

    Number getNumber(String key);

    Number getNumber(NutsElement key);

    byte getByte(String key);

    byte getByte(NutsElement key);

    int getInt(String key);

    int getInt(NutsElement key);

    long getLong(String key);

    long getLong(NutsElement key);

    short getShort(String key);

    short getShort(NutsElement key);

    Instant getInstant(String key);

    Instant getInstant(NutsElement key);

    float getFloat(String key);

    float getFloat(NutsElement key);

    double getDouble(String key);

    double getDouble(NutsElement key);

    /**
     * object (key,value) attributes
     *
     * @return object attributes
     */
    Collection<NutsElementEntry> children();

    Stream<NutsElementEntry> stream();

    /**
     * element count
     *
     * @return element count
     */
    int size();

    /**
     * return new builder initialized with this instance
     * @return new builder initialized with this instance
     */
    NutsObjectElementBuilder builder();
}

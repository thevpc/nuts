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

import java.time.Instant;
import java.util.Date;

/**
 * Nuts Element builder that helps creating element instances.
 * @author thevpc
 * %category Elements
 */
public interface NutsElementBuilder {

    /**
     * create primitive boolean element
     * @param value value
     * @return primitive boolean element
     */
    NutsPrimitiveElement forBoolean(String value);

    /**
     * create primitive boolean element
     * @param value value
     * @return primitive boolean element
     */
    NutsPrimitiveElement forBoolean(boolean value);

    /**
     * create primitive date element
     * @param value value
     * @return primitive date element
     */
    NutsPrimitiveElement forDate(Date value);

    /**
     * create primitive date element
     * @param value value
     * @return primitive date element
     */
    NutsPrimitiveElement forDate(Instant value);

    /**
     * create primitive date element
     * @param value value
     * @return primitive date element
     */
    NutsPrimitiveElement forDate(String value);

    /**
     * create primitive null element
     * @return primitive null element
     */
    NutsPrimitiveElement forNull();

    /**
     * create primitive number element
     * @param value value
     * @return primitive number element
     */
    NutsPrimitiveElement forNumber(Number value);

    NutsPrimitiveElement forInt(int value);

    NutsPrimitiveElement forLong(long value);

    NutsPrimitiveElement forDouble(double value);

    NutsPrimitiveElement forFloat(float value);

    NutsPrimitiveElement forByte(byte value);

    NutsPrimitiveElement forChar(char value);

    /**
     * create primitive number element
     * @param value value
     * @return primitive number element
     */
    NutsPrimitiveElement forNumber(String value);

    /**
     * create primitive string element
     * @param value value
     * @return primitive string element
     */
    NutsPrimitiveElement forString(String value);

    /**
     * create object element builder (mutable)
     * @return object element
     */
    NutsObjectElementBuilder forObject();

    /**
     * create array element builder (mutable)
     * @return array element
     */
    NutsArrayElementBuilder forArray();


}

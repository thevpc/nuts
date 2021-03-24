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

import java.time.Instant;
import java.util.Date;

/**
 * Array element Builder is a mutable NutsArrayElement that helps 
 * manipulating arrays.
 * @author thevpc
 * @category Elements
 */
public interface NutsPrimitiveElementBuilder extends NutsElementBuilder{

   
    NutsPrimitiveElement buildString(String str);

    NutsPrimitiveElement buildBoolean(String value);

    NutsPrimitiveElement buildBoolean(boolean value);

    NutsPrimitiveElement buildByte(byte value);

    NutsPrimitiveElement buildChar(char value);

    NutsPrimitiveElement buildDouble(double value);

    NutsPrimitiveElement buildFalse();

    NutsPrimitiveElement buildFloat(float value);

    NutsPrimitiveElement buildInstant(Date value);

    NutsPrimitiveElement buildInstant(Instant value);

    NutsPrimitiveElement buildInstant(String value);

    NutsPrimitiveElement buildInt(int value);

    NutsPrimitiveElement buildLong(long value);

    NutsPrimitiveElement buildNull();

    NutsPrimitiveElement buildNumber(Number value);

    NutsPrimitiveElement buildNumber(String value);

    NutsPrimitiveElement buildTrue();
}

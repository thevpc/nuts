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
package net.thevpc.nuts.runtime.core.format;

import java.util.Map;
import static net.thevpc.nuts.runtime.core.util.CoreCommonUtils.stringValue;

import net.thevpc.nuts.NutsUtilStrings;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementEntry;

/**
 *
 * @author thevpc
 */
public class NutsFormatUtils {

    public static void putAllInProps(String prefix, Map<String, String> dest, NutsElement value) {
        switch (value.type()) {
            case BOOLEAN:
            case INSTANT:
            case INTEGER:
            case FLOAT:
            case STRING:
//            case NUTS_STRING:
            case NULL:
            {
                dest.put(prefix, stringValue(value.asPrimitive().getValue()));
                break;
            }
            case OBJECT: {
                if (!NutsUtilStrings.isBlank(prefix)) {
                    prefix += ".";
                } else {
                    prefix = "";
                }
                for (NutsElementEntry e : value.asObject().children()) {
                    putAllInProps(prefix + e.getKey(), dest, e.getValue());
                }
                break;
            }
            case ARRAY: {
                if (!NutsUtilStrings.isBlank(prefix)) {
                    prefix += ".";
                } else {
                    prefix = "";
                }
                int i = 0;
                for (NutsElement e : value.asArray().children()) {
                    putAllInProps(prefix + (i + 1), dest, e);
                    i++;
                }
                break;
            }
        }
    }
}

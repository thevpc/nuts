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
package net.thevpc.nuts.runtime.standalone.format;

import java.util.Map;
import static net.thevpc.nuts.runtime.standalone.util.CoreStringUtils.stringValue;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementEntry;
import net.thevpc.nuts.NutsSession;

/**
 *
 * @author thevpc
 */
public class NutsFormatUtils {

    public static void putAllInProps(String prefix, Map<String, String> dest, NutsElement value, NutsSession session) {

        switch (value.type()) {
            case BOOLEAN:
            case INSTANT:
            case INTEGER:
            case FLOAT:
            case STRING:
//            case NUTS_STRING:
            case NULL:
            {
                dest.put(prefix, stringValue(value.asPrimitive().get(session).getRaw()));
                break;
            }
            case OBJECT: {
                if (!NutsBlankable.isBlank(prefix)) {
                    prefix += ".";
                } else {
                    prefix = "";
                }
                for (NutsElementEntry e : value.asObject().get(session).entries()) {
                    putAllInProps(prefix + e.getKey(), dest, e.getValue(), session);
                }
                break;
            }
            case ARRAY: {
                if (!NutsBlankable.isBlank(prefix)) {
                    prefix += ".";
                } else {
                    prefix = "";
                }
                int i = 0;
                for (NutsElement e : value.asArray().get(session).items()) {
                    putAllInProps(prefix + (i + 1), dest, e, session);
                    i++;
                }
                break;
            }
        }
    }
}

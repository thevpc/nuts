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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
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

import net.thevpc.nuts.NUnsupportedOperationException;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NPairElement;
import net.thevpc.nuts.util.NMsg;

/**
 * @author thevpc
 */
public class NFormatUtils {

    public static void putAllInProps(String prefix, Map<String, String> dest, NElement value) {

        switch (value.type()) {
            case BOOLEAN:
            case INSTANT:
            case INT:
            case FLOAT:
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING:
            case NULL:
            case BYTE:
            case REGEX:
            case SHORT:
            case CHAR:
            case NAME:
            case DOUBLE:
            case LONG:
            case BIG_INT:
            case BIG_DECIMAL:
            {
                dest.put(prefix, stringValue(value.asPrimitive().get().asLiteral().asObject().orNull()));
                break;
            }
            case PAIR: {
                NPairElement ee = (NPairElement) value;
                putAllInProps(prefix + ee.key(), dest, ee.value());
                break;
            }
            case OBJECT: {
                if (!NBlankable.isBlank(prefix)) {
                    prefix += ".";
                } else {
                    prefix = "";
                }
                int i = 0;
                for (NElement e : value.asObject().get().children()) {
                    if (e instanceof NPairElement) {
                        NPairElement ee = (NPairElement) e;
                        putAllInProps(prefix + ee.key(), dest, ee.value());
                    } else {
                        putAllInProps(prefix + (i + 1), dest, e);
                        i++;
                    }
                }
                break;
            }
            case ARRAY: {
                if (!NBlankable.isBlank(prefix)) {
                    prefix += ".";
                } else {
                    prefix = "";
                }
                int i = 0;
                for (NElement e : value.asArray().get().children()) {
                    putAllInProps(prefix + (i + 1), dest, e);
                    i++;
                }
                break;
            }
//            case UPLET: {
//                if (!NBlankable.isBlank(prefix)) {
//                    prefix += ".";
//                } else {
//                    prefix = "";
//                }
//                int i = 0;
//                for (NElement e : value.asUplet().get().items()) {
//                    putAllInProps(prefix + (i + 1), dest, e);
//                    i++;
//                }
//                break;
//            }
            case CUSTOM: {
                throw new NUnsupportedOperationException(NMsg.ofC("unable flatten custom element to properties"));
            }

            default: {
                throw new NUnsupportedOperationException();
            }
        }
    }
}

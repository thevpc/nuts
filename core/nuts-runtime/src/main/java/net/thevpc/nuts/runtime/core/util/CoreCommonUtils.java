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
 *
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
package net.thevpc.nuts.runtime.core.util;

import net.thevpc.nuts.*;
import java.lang.reflect.Array;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class CoreCommonUtils {


    

    

    public static NutsString stringValueFormatted(Object o, boolean escapeString, NutsSession session) {
        NutsWorkspace ws = session.getWorkspace();
        NutsFormatManager txt = ws.formats();
        if (o == null) {
            return txt.text().plain("");
        }
        if (o instanceof NutsPrimitiveElement) {
            o = ((NutsPrimitiveElement) o).getValue();
        } else if (o instanceof NutsArrayElement) {
            o = ((NutsArrayElement) o).children();
        } else if (o instanceof NutsObjectElement) {
            Collection<NutsNamedElement> c= ((NutsObjectElement) o).children();
            Object[] a = c.toArray();
            if (a.length == 0) {
                return txt.text().plain("");
            }
            if (a.length == 1) {
                return txt.text().plain(stringValue(a[0]));
            }
            return txt.text().builder()
                    .append("{")
                    .appendJoined(
                            txt.text().plain(", "),
                            c.stream().map(x -> stringValueFormatted(x, escapeString, session)).collect(Collectors.toList())
                    )
                    .append("}")
            ;
            
        } else if (o instanceof NutsNamedElement) {
            NutsNamedElement ne = (NutsNamedElement) o;
            NutsTextNodeBuilder sb = ws.formats().text().builder();
            sb.append(stringValueFormatted(ne.getName(), escapeString, session));
            sb.append("=");
            if (ne.getValue().type() == NutsElementType.STRING) {
                sb.append(
                        txt.text().nodeFor(
                        CoreStringUtils.dblQuote(stringValueFormatted(ne.getValue(), escapeString, session).toString())
                        ));
            } else {
                sb.append(stringValueFormatted(ne.getValue(), escapeString, session));
            }
            o = sb.toString();
        } else if (o instanceof Map.Entry) {
            Map.Entry ne = (Map.Entry) o;
            NutsTextNodeBuilder sb = ws.formats().text().builder();
            sb.append(stringValueFormatted(ne.getKey(), escapeString, session));
            sb.append("=");
            if (ne.getValue() instanceof String || (ne.getValue() instanceof NutsPrimitiveElement && ((NutsPrimitiveElement) ne.getValue()).type() == NutsElementType.STRING)) {
                sb.append(
                        txt.text().nodeFor(
                        CoreStringUtils.dblQuote(stringValueFormatted(ne.getValue(), escapeString, session).toString())
                        )
                );
            } else {
                sb.append(stringValueFormatted(ne.getValue(), escapeString, session));
            }
            return sb.immutable();
        } else if (o instanceof Map) {
            o = ((Map) o).entrySet();
        }
        if (o == null) {
            return txt.text().blank();
        }
        if (o instanceof Boolean) {
            txt.text().plain(String.valueOf(o));
        }
        if (o.getClass().isEnum()) {
            return txt.text().plain(CoreEnumUtils.getEnumString((Enum) o));
        }
        if (o instanceof Instant) {
            return txt.text().plain(
                    CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(((Instant) o))
            );
        }
        if (o instanceof Temporal) {
            return txt.text().plain(
                    CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(((Temporal) o))
            );
        }
        if (o instanceof Date) {
            return txt.text().plain(
                    CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(((Date) o).toInstant())
            );
        }
        if (o instanceof NutsFormattable) {
            return txt.text().nodeFor(o);
        }
        if (o instanceof Collection) {
            Collection c = ((Collection) o);
            Object[] a = c.toArray();
            if (a.length == 0) {
                return txt.text().plain("");
            }
            if (a.length == 1) {
                return txt.text().plain(stringValue(a[0]));
            }
            List<NutsString> ll = ((Collection<Object>) c).stream().map(x -> stringValueFormatted(x, escapeString, session)).collect(Collectors.toList());
            return txt.text().builder()
                    .append("[")
                    .appendJoined(
                            txt.text().plain(", "),
                            ll
                    )
                    .append("]")
                    ;
        }
        if (o instanceof Map) {
            Map c = ((Map) o);
            Map.Entry[] a = (Map.Entry[]) c.entrySet().toArray(new Map.Entry[0]);
            if (a.length == 0) {
                return txt.text().blank();
            }
            if (a.length == 1) {
                return txt.text().plain(stringValue(a[0]));
            }
            List<NutsString> ll = Arrays.stream(a).map(x -> stringValueFormatted(x, escapeString, session)).collect(Collectors.toList());
            return txt.text().builder()
                    .append("{")
                    .appendJoined(
                            txt.text().plain(", "),
                            ll
                    )
                    .append("}")
                    ;
        }
        if (o.getClass().isArray()) {
            int len = Array.getLength(o);
            if (len == 0) {
                return txt.text().blank();
            }
            if (len == 1) {
                return stringValueFormatted(Array.get(o, 0), escapeString, session);
            }
            List<NutsString> all = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                all.add(stringValueFormatted(Array.get(o, i), escapeString, session));
            }
            return txt.text().builder()
                    .append("[")
                    .appendJoined(
                            txt.text().plain(", "),
                            all
                    )
                    .append("]")
                    ;

        }
//        if (o instanceof Iterable) {
//            Iterable x = (Iterable) o;
//            return stringValueFormatted(x.iterator(), escapeString, session);
//        }
        if (o instanceof Iterator) {
            Iterator x = (Iterator) o;
            List<String> all = new ArrayList<>();
            while (x.hasNext()) {
                all.add(stringValueFormatted(x.next(), escapeString, session).toString());
            }
            return stringValueFormatted(all, escapeString, session);
        }
        return txt.text().plain(o.toString());
    }

    public static String stringValue(Object o) {
        if (o == null) {
            return ("");
        }
        if (o.getClass().isEnum()) {
            return (CoreEnumUtils.getEnumString((Enum) o));
        }
        if (o instanceof Instant) {
            return (CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(((Instant) o)));
        }
        if (o instanceof Date) {
            return (CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(((Date) o).toInstant()));
        }
        if (o instanceof Collection) {
            Collection c = ((Collection) o);
            Object[] a = c.toArray();
            if (a.length == 0) {
                return ("");
            }
            if (a.length == 1) {
                return stringValue(a[0]);
            }
            return ("[" + String.join(", ", (List) c.stream().map(x -> stringValue(x)).collect(Collectors.toList())) + "]");
        }
        if (o.getClass().isArray()) {
            int len = Array.getLength(o);
            if (len == 0) {
                return ("");
            }
            if (len == 1) {
                return stringValue(Array.get(o, 0));
            }
            List<String> all = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                all.add(stringValue(Array.get(o, i)).toString());
            }
            return ("[" + String.join(", ", all) + "]");
        }
        return (o.toString());
    }

}

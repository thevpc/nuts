/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.text.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.text.NutsTextBuilder;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsStringUtils;

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

public class NutsTextUtils {

    public static NutsString stringValueFormatted(Object o, boolean escapeString, NutsSession session) {
        NutsTexts txt = NutsTexts.of(session);
        if (o == null) {
            return txt.ofBlank();
        }
        if (o instanceof NutsFormattable) {
            return txt.ofText(o);
        }
        if (o instanceof NutsPrimitiveElement) {
            o = ((NutsPrimitiveElement) o).getRaw();
        } else if (o instanceof NutsArrayElement) {
            o = ((NutsArrayElement) o).items();
        } else if (o instanceof NutsObjectElement) {
            Collection<NutsElementEntry> c = ((NutsObjectElement) o).entries();
            Object[] a = c.toArray();
            if (a.length == 0) {
                return txt.ofBlank();
            }
            if (a.length == 1) {
                return txt.ofPlain(CoreStringUtils.stringValue(a[0]));
            }
            return txt.ofBuilder()
                    .append("{")
                    .appendJoined(
                            txt.ofPlain(", "),
                            c.stream().map(x -> stringValueFormatted(x, escapeString, session)).collect(Collectors.toList())
                    )
                    .append("}");

        } else if (o instanceof NutsElementEntry) {
            NutsElementEntry ne = (NutsElementEntry) o;
            NutsTextBuilder sb = NutsTexts.of(session).ofBuilder();
            sb.append(stringValueFormatted(ne.getKey(), escapeString, session));
            sb.append("=");
            if (ne.getValue().type() == NutsElementType.STRING) {
                sb.append(
                        txt.ofText(
                                NutsStringUtils.formatStringLiteral(stringValueFormatted(ne.getValue(), escapeString, session).toString(), NutsStringUtils.QuoteType.DOUBLE)
                        ));
//            } else if (ne.getValue().type() == NutsElementType.NUTS_STRING) {
//                sb.append(ne.getValue().asNutsString());
            } else {
                sb.append(stringValueFormatted(ne.getValue(), escapeString, session));
            }
            o = sb.toString();
        } else if (o instanceof Map.Entry) {
            Map.Entry ne = (Map.Entry) o;
            NutsTextBuilder sb = NutsTexts.of(session).ofBuilder();
            sb.append(stringValueFormatted(ne.getKey(), escapeString, session));
            sb.append("=");
            if (ne.getValue() instanceof String
                    || (ne.getValue() instanceof NutsElement && ((NutsElement) ne.getValue()).isString())) {
                sb.append(
                        txt.ofText(
                                NutsStringUtils.formatStringLiteral(stringValueFormatted(ne.getValue(), escapeString, session).toString(), NutsStringUtils.QuoteType.DOUBLE)
                        )
                );
//            } else if (ne.getValue() instanceof NutsElement && ((NutsElement) ne.getValue()).isNutsString()) {
//                sb.append(((NutsElement) ne.getValue()).asNutsString());
            } else {
                sb.append(stringValueFormatted(ne.getValue(), escapeString, session));
            }
            return sb.immutable();
        } else if (o instanceof Map) {
            o = ((Map) o).entrySet();
        }
        if (o == null) {
            return txt.ofBlank();
        }
        if (o instanceof Instant) {
            return txt.ofPlain(
                    CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(((Instant) o))
            );
        }
        if (o instanceof Temporal) {
            return txt.ofPlain(
                    CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(((Temporal) o))
            );
        }
        if (o instanceof Date) {
            return txt.ofPlain(
                    CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(((Date) o).toInstant())
            );
        }
        if (o instanceof Collection) {
            Collection c = ((Collection) o);
            Object[] a = c.toArray();
            if (a.length == 0) {
                return txt.ofBlank();
            }
            if (a.length == 1) {
                return txt.ofPlain(CoreStringUtils.stringValue(a[0]));
            }
            List<NutsString> ll = ((Collection<Object>) c).stream().map(x -> stringValueFormatted(x, escapeString, session)).collect(Collectors.toList());
            return txt.ofBuilder()
                    .append("[")
                    .appendJoined(
                            txt.ofPlain(", "),
                            ll
                    )
                    .append("]");
        }
        if (o instanceof Map) {
            Map c = ((Map) o);
            Map.Entry[] a = (Map.Entry[]) c.entrySet().toArray(new Map.Entry[0]);
            if (a.length == 0) {
                return txt.ofBlank();
            }
            if (a.length == 1) {
                return txt.ofPlain(CoreStringUtils.stringValue(a[0]));
            }
            List<NutsString> ll = Arrays.stream(a).map(x -> stringValueFormatted(x, escapeString, session)).collect(Collectors.toList());
            return txt.ofBuilder()
                    .append("{")
                    .appendJoined(
                            txt.ofPlain(", "),
                            ll
                    )
                    .append("}");
        }
        if (o.getClass().isArray()) {
            int len = Array.getLength(o);
            if (len == 0) {
                return txt.ofBlank();
            }
            if (len == 1) {
                return stringValueFormatted(Array.get(o, 0), escapeString, session);
            }
            List<NutsString> all = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                all.add(stringValueFormatted(Array.get(o, i), escapeString, session));
            }
            return txt.ofBuilder()
                    .append("[")
                    .appendJoined(
                            txt.ofPlain(", "),
                            all
                    )
                    .append("]");

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
        return txt.ofText(o);
    }

    public static NutsString formatLogValue(NutsTexts text, Object unresolved, Object resolved) {
        NutsString a = desc(unresolved, text);
        NutsString b = desc(resolved, text);
        if (a.equals(b)) {
            return a;
        } else {
            return
                    text.ofBuilder()
                            .append(a)
                            .append(" => ")
                            .append(b)
                    ;
        }
    }

    public static NutsString desc(Object s, NutsTexts text) {
        if (s == null || (s instanceof String && ((String) s).isEmpty())) {
            return text.ofStyled("<EMPTY>", NutsTextStyle.option());
        }
        return text.ofText(s);
    }
}

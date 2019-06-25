/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.format.elem;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.vpc.app.nuts.NutsArrayElementBuilder;
import net.vpc.app.nuts.NutsElement;
import net.vpc.app.nuts.NutsElementBuilder;
import net.vpc.app.nuts.NutsElementPath;
import net.vpc.app.nuts.NutsElementType;
import net.vpc.app.nuts.NutsNamedElement;
import net.vpc.app.nuts.NutsSession;

/**
 *
 * @author vpc
 */
public class NutsElementPathFilter {

    private static final NutsElementNameMatcherFalse NUTS_ELEMENT_NAME_MATCHER_FALSE = new NutsElementNameMatcherFalse();
    private static final NutsElementNameMatcherEven NUTS_ELEMENT_NAME_MATCHER_EVEN = new NutsElementNameMatcherEven();
    private static final NutsElementNameMatcherOdd NUTS_ELEMENT_NAME_MATCHER_ODD = new NutsElementNameMatcherOdd();
    private static final NutsElementNameMatcherValue NUTS_ELEMENT_NAME_MATCHER_VALUE_ZERO = new NutsElementNameMatcherValue(0);
    private static final NutsElementNameMatcherValue NUTS_ELEMENT_NAME_MATCHER_VALUE_MINUS_ONE = new NutsElementNameMatcherValue(-1);
    private static final NutsElementNameMatcherTrue NUTS_ELEMENT_NAME_MATCHER_TRUE = new NutsElementNameMatcherTrue();
    private static final NutsElementIndexMatcherFalse NUTS_ELEMENT_INDEX_MATCHER_FALSE = new NutsElementIndexMatcherFalse();
    private static final NutsElementIndexMatcherUnique NUTS_ELEMENT_INDEX_MATCHER_UNIQUE = new NutsElementIndexMatcherUnique();
    private static final NutsElementIndexMatcherEven NUTS_ELEMENT_INDEX_MATCHER_EVEN = new NutsElementIndexMatcherEven();
    private static final NutsElementIndexMatcherOdd NUTS_ELEMENT_INDEX_MATCHER_ODD = new NutsElementIndexMatcherOdd();
    private static final NutsElementIndexMatcherForValue NUTS_ELEMENT_INDEX_MATCHER_FOR_VALUE_0 = new NutsElementIndexMatcherForValue(0);
    private static final NutsElementIndexMatcherForValue NUTS_ELEMENT_INDEX_MATCHER_FOR_VALUE_MINUS_ONE = new NutsElementIndexMatcherForValue(-1);
    private static final NutsElementIndexMatcherTrue NUTS_ELEMENT_INDEX_MATCHER_TRUE = new NutsElementIndexMatcherTrue();

    private static void compile_readChar(StreamTokenizer st, char c) throws IOException {
        int i = st.nextToken();
        if (i != c) {
            throw new IllegalArgumentException("Expected " + c + ". got " + ((char) i));
        }
    }

    private static String compile_readArrItem(StreamTokenizer st) throws IOException {
        compile_readChar(st, '[');
        st.nextToken();
        String value = null;
        switch (st.ttype) {
            case ']':
                return "";
            case StreamTokenizer.TT_WORD:
                value = st.sval;
                compile_readChar(st, ']');
                return value;
            default:
                throw new IllegalArgumentException("Expected string, got " + st);
        }
    }

    /**
     * aa.bb.cc aa[bb].cc
     *
     * @param jpath path
     * @param session session
     * @return
     */
    public static NutsElementPath compile(String jpath, NutsSession session) {
        StreamTokenizer st = new StreamTokenizer(new StringReader(jpath));
        st.resetSyntax();
        st.wordChars(33, 255);
        st.ordinaryChar('.');
        st.ordinaryChar('[');
        st.ordinaryChar(']');
        QueueJsonPath q = new QueueJsonPath();
        try {
            boolean wasNotDotName = false;
            while (st.nextToken() != StreamTokenizer.TT_EOF) {
                switch (st.ttype) {
                    case StreamTokenizer.TT_WORD: {
                        wasNotDotName = true;
                        q.queue.add(new SubItemJsonPath(st.sval, session));
                        break;
                    }
                    case '.': {
                        if (!wasNotDotName) {
                            q.queue.add(new SubItemJsonPath("*", session));
                        }
                        wasNotDotName = false;
                        break;
                    }
                    case '[': {
                        wasNotDotName = true;
                        st.pushBack();
                        String p = compile_readArrItem(st);
                        if (p.isEmpty()) {
                            q.queue.add(new ArrItemCollectorJsonPath(session));
                        } else {
                            q.queue.add(new SubItemJsonPath(p, session));
                        }
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Unexpected " + st);
                    }
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return q;
    }

    private static class QueueJsonPath implements NutsElementPath {

        List<NutsElementPath> queue = new ArrayList<>();

        @Override
        public List<NutsElement> filter(List<NutsElement> elements) {
            for (NutsElementPath jsonPath : queue) {
                elements = jsonPath.filter(elements);
            }
            return elements;
        }

        @Override
        public List<NutsElement> filter(NutsElement element) {
            List<NutsElement> a = new ArrayList<>();
            a.add(element);
            return filter(a);
        }

        @Override
        public String toString() {
            return queue.stream().map(Object::toString).collect(Collectors.joining("."));
        }

    }

    private static class ArrItemCollectorJsonPath implements NutsElementPath {

        private NutsSession session;
        private NutsElementBuilder builder;

        public ArrItemCollectorJsonPath(NutsSession session) {
            this.session = session;
            builder = session.workspace().element().builder();
        }

        @Override
        public List<NutsElement> filter(NutsElement element) {
            NutsArrayElementBuilder aa = builder.forArray();
            aa.add(element);
            return Arrays.asList(aa);
        }

        @Override
        public List<NutsElement> filter(List<NutsElement> elements) {
            NutsArrayElementBuilder aa = builder.forArray();
            for (NutsElement element : elements) {
                aa.add(element);
            }
            return Arrays.asList(aa);
        }

    }

    private static class SubItemJsonPath extends AbstractJsonPath {

        private String pattern;

        public SubItemJsonPath(String subItem, NutsSession session) {
            super(session);
            this.pattern = subItem;
        }

        @Override
        public String toString() {
            return "match(" + pattern + ')';
        }

        @Override
        public List<NutsElement> filter(NutsElement element) {
            if (element.type() == NutsElementType.ARRAY) {
                List<NutsElement> arr = new ArrayList<>(element.array().children());
                List<NutsElement> result = new ArrayList<>();
                int len = arr.size();
                NutsElementIndexMatcher indexMatcher = matchesIndex(pattern);
                Map<String, Object> matchContext = new HashMap<>();
                for (int i = 0; i < arr.size(); i++) {
                    NutsElement value = arr.get(i);
                    if (indexMatcher.matches(value, i, len, matchContext, session)) {
                        result.add(value);
                    }
                }
                return result;
            } else if (element.type() == NutsElementType.OBJECT) {
                List<NutsElement> result = new ArrayList<>();
                Collection<NutsNamedElement> aa0 = element.object().children();
                int len = aa0.size();
                int index = 0;
                Map<String, Object> matchContext = new HashMap<>();
                NutsElementNameMatcher nameMatcher = matchesName(pattern);
                for (NutsNamedElement se : aa0) {
                    if (nameMatcher.matches(index, se.getName(), len, matchContext, session)) {
                        result.add(se.getValue());
                    }
                    index++;
                }
                return result;
            }
            return null;
        }

        private NutsElementNameMatcher matchesName(String s) {
            switch (s) {
                case "*":
                case ":*":
                case ":#*": {
                    return NUTS_ELEMENT_NAME_MATCHER_TRUE;
                }
                case ":last": {
                    return NUTS_ELEMENT_NAME_MATCHER_VALUE_MINUS_ONE;
                }
                case ":first": {
                    return NUTS_ELEMENT_NAME_MATCHER_VALUE_ZERO;
                }
                case ":odd": {
                    return NUTS_ELEMENT_NAME_MATCHER_ODD;
                }
                case ":even": {
                    return NUTS_ELEMENT_NAME_MATCHER_EVEN;
                }
                default: {
                    if (s.startsWith(":#")) {
                        s = s.substring(2);
                        List<NutsElementNameMatcher> ors = new ArrayList<>();
                        for (String vir : s.split(",")) {
                            vir = vir.trim();
                            if (vir.length() > 0) {
                                if (vir.indexOf('-') > 0) {
                                    String[] inter = vir.split("-");
                                    if (inter.length == 2 && isInt(inter[0]) && isInt(inter[1])) {
                                        int a = Integer.parseInt(inter[0]);
                                        int b = Integer.parseInt(inter[1]);
                                        ors.add(new NutsElementNameMatcherValueInterval(a, b));
                                    }
                                } else {
                                    if (isInt(vir)) {
                                        int a = Integer.parseInt(vir);
                                        ors.add(new NutsElementNameMatcherValue(a));
                                    }
                                }
                            }
                        }
                        if (ors.size() == 1) {
                            return ors.get(0);
                        } else if (ors.size() > 1) {
                            return new OrNutsElementNameMatcher(ors.toArray(new NutsElementNameMatcher[0]));
                        } else {
                            return NUTS_ELEMENT_NAME_MATCHER_FALSE;
                        }
                    } else if (s.startsWith(":nocase=")) {
                        return new NutsElementNameMatcherString(s.substring(":nocase=".length()), true);
                    } else {
                        return new NutsElementNameMatcherString(pattern, false);
                    }
                }
            }
        }

        private NutsElementIndexMatcher matchesIndex(String s) {
            switch (s) {
                case "*":
                case ":*":
                case ":#*": {
                    return NUTS_ELEMENT_INDEX_MATCHER_TRUE;
                }
                case ":last": {
                    return NUTS_ELEMENT_INDEX_MATCHER_FOR_VALUE_MINUS_ONE;
                }
                case ":first": {
                    return NUTS_ELEMENT_INDEX_MATCHER_FOR_VALUE_0;
                }
                case ":odd": {
                    return NUTS_ELEMENT_INDEX_MATCHER_ODD;
                }
                case ":even": {
                    return NUTS_ELEMENT_INDEX_MATCHER_EVEN;
                }
                case ":unique": {
                    return NUTS_ELEMENT_INDEX_MATCHER_UNIQUE;
                }
                default: {
                    if (s.startsWith(":#")) {
                        s = s.substring(2);
                        return createIndexValueInervalMatcher(s);
                    } else if (isInt(s)) {
                        return new NutsElementIndexMatcherForValue(Integer.parseInt(s));
                    } else if (s.matches("[0-9][0-9,-]+")) {
                        return createIndexValueInervalMatcher(s);
                    } else {
                        return NUTS_ELEMENT_INDEX_MATCHER_FALSE;
                    }
                }
            }
        }

        private NutsElementIndexMatcher createIndexValueInervalMatcher(String s) throws NumberFormatException {
            List<NutsElementIndexMatcher> ors = new ArrayList<>();
            for (String vir : s.split(",")) {
                vir = vir.trim();
                if (vir.length() > 0) {
                    if (vir.indexOf('-') > 0) {
                        String[] inter = vir.split("-");
                        if (inter.length == 2 && isInt(inter[0]) && isInt(inter[1])) {
                            int a = Integer.parseInt(inter[0]);
                            int b = Integer.parseInt(inter[1]);
                            ors.add(new NutsElementIndexMatcherValueInterval(a, b));
                        }
                    } else {
                        if (isInt(vir)) {
                            ors.add(new NutsElementIndexMatcherForValue(Integer.parseInt(vir)));
                        }
                    }
                }
            }
            if (ors.size() == 1) {
                return ors.get(0);
            } else if (ors.size() > 1) {
                return new OrNutsElementIndexMatcher(ors.toArray(new NutsElementIndexMatcher[0]));
            } else {
                return new NutsElementIndexMatcherFalse();
            }
        }

    }

    private static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static abstract class AbstractJsonPath implements NutsElementPath {

        NutsSession session;

        public AbstractJsonPath(NutsSession session) {
            this.session = session;
        }

        public abstract List<NutsElement> filter(NutsElement element);

        @Override
        public List<NutsElement> filter(List<NutsElement> elements) {
            List<NutsElement> a = new ArrayList<>();
            for (NutsElement element : elements) {
                List<NutsElement> ff = filter(element);
                if (ff != null) {
                    a.addAll(ff);
                }
            }
            return a;
        }

    }

    public static class OrNutsElementNameMatcher implements NutsElementNameMatcher {

        List<NutsElementNameMatcher> all = new ArrayList<>();

        public OrNutsElementNameMatcher(NutsElementNameMatcher[] all) {
            this.all.addAll(Arrays.asList(all));
        }

        @Override
        public boolean matches(int index, String name, int len, Map<String, Object> matchContext, NutsSession session) {
            for (NutsElementNameMatcher any : all) {
                if (any.matches(index, name, len, matchContext, session)) {
                    return true;
                }
            }
            return false;
        }

    }

    public static class OrNutsElementIndexMatcher implements NutsElementIndexMatcher {

        List<NutsElementIndexMatcher> all = new ArrayList<>();

        public OrNutsElementIndexMatcher(NutsElementIndexMatcher[] all) {
            this.all.addAll(Arrays.asList(all));
        }

        @Override
        public boolean matches(NutsElement value, int index, int len, Map<String, Object> matchContext, NutsSession session) {
            for (NutsElementIndexMatcher any : all) {
                if (any.matches(value, index, len, matchContext, session)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "(" + all.stream().map(Object::toString).collect(Collectors.joining(" or ")) + ')';
        }

    }

    public interface NutsElementNameMatcher {

        boolean matches(int index, String name, int len, Map<String, Object> matchContext, NutsSession session);
    }

    public interface NutsElementIndexMatcher {

        boolean matches(NutsElement value, int index, int len, Map<String, Object> matchContext, NutsSession session);
    }

    private static class NutsElementIndexMatcherEven implements NutsElementIndexMatcher {

        public NutsElementIndexMatcherEven() {
        }

        @Override
        public boolean matches(NutsElement value, int index, int len, Map<String, Object> matchContext, NutsSession session) {
            return index % 2 == 0;
        }
    }

    private static class NutsElementIndexMatcherOdd implements NutsElementIndexMatcher {

        public NutsElementIndexMatcherOdd() {
        }

        @Override
        public boolean matches(NutsElement value, int index, int len, Map<String, Object> matchContext, NutsSession session) {
            return index % 2 == 1;
        }
    }

    private static class NutsElementIndexMatcherTrue implements NutsElementIndexMatcher {

        public NutsElementIndexMatcherTrue() {
        }

        @Override
        public boolean matches(NutsElement value, int index, int len, Map<String, Object> matchContext, NutsSession session) {
            return true;
        }
    }

    private static class NutsElementNameMatcherTrue implements NutsElementNameMatcher {

        public NutsElementNameMatcherTrue() {
        }

        @Override
        public boolean matches(int index, String s, int len, Map<String, Object> matchContext, NutsSession session) {
            return true;
        }
    }

    private static class NutsElementNameMatcherValue implements NutsElementNameMatcher {

        private final int a;

        public NutsElementNameMatcherValue(int a) {
            this.a = a;
        }

        @Override
        public boolean matches(int index, String name, int len, Map<String, Object> matchContext, NutsSession session) {
            if (a < 0) {
                return index == len + a;
            }
            return index == a;
        }
    }

    private static class NutsElementNameMatcherOdd implements NutsElementNameMatcher {

        public NutsElementNameMatcherOdd() {
        }

        @Override
        public boolean matches(int index, String name, int len, Map<String, Object> matchContext, NutsSession session) {
            return index % 2 == 1;
        }
    }

    private static class NutsElementNameMatcherEven implements NutsElementNameMatcher {

        public NutsElementNameMatcherEven() {
        }

        @Override
        public boolean matches(int index, String name, int len, Map<String, Object> matchContext, NutsSession session) {
            return index % 2 == 0;
        }
    }

    private static class NutsElementNameMatcherValueInterval implements NutsElementNameMatcher {

        private final int a;
        private final int b;

        public NutsElementNameMatcherValueInterval(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean matches(int index, String name, int len, Map<String, Object> matchContext, NutsSession session) {
            return index >= a && index <= b;
        }
    }

    private static class NutsElementNameMatcherFalse implements NutsElementNameMatcher {

        public NutsElementNameMatcherFalse() {
        }

        @Override
        public boolean matches(int index, String name, int len, Map<String, Object> matchContext, NutsSession session) {
            return false;
        }
    }

    private static class NutsElementNameMatcherString implements NutsElementNameMatcher {

        private final String pat;
        private final boolean lower;

        public NutsElementNameMatcherString(String pat, boolean lower) {
            this.lower = lower;
            this.pat = lower ? pat.toLowerCase() : pat;
        }

        @Override
        public boolean matches(int index, String name, int len, Map<String, Object> matchContext, NutsSession session) {
            return lower
                    ? name.toLowerCase().matches(pat)
                    : name.matches(pat);
        }
    }

    private static class NutsElementIndexMatcherForValue implements NutsElementIndexMatcher {

        private final int a;

        public NutsElementIndexMatcherForValue(int a) {
            this.a = a;
        }

        @Override
        public boolean matches(NutsElement value, int index, int len, Map<String, Object> matchContext, NutsSession session) {
            if (a < 0) {
                return index == len + a;
            }
            return index == a;
        }
    }

    private static class NutsElementIndexMatcherFalse implements NutsElementIndexMatcher {

        public NutsElementIndexMatcherFalse() {
        }

        @Override
        public boolean matches(NutsElement value, int index, int len, Map<String, Object> matchContext, NutsSession session) {
            return false;
        }
    }

    private static class NutsElementIndexMatcherValueInterval implements NutsElementIndexMatcher {

        private final int a;
        private final int b;

        public NutsElementIndexMatcherValueInterval(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean matches(NutsElement value, int index, int len, Map<String, Object> matchContext, NutsSession session) {
            return index >= a && index <= b;
        }
    }

    private static class NutsElementIndexMatcherUnique implements NutsElementIndexMatcher {

        public NutsElementIndexMatcherUnique() {
        }

        @Override
        public boolean matches(NutsElement value, int index, int len, Map<String, Object> matchContext, NutsSession session) {
            Set<String> u = (Set<String>) matchContext.get("unique");
            if (u == null) {
                u = new HashSet<>();
                matchContext.put("unique", u);
            }
            String v = session.workspace().json().value(value).format();
            if (u.contains(v)) {
                return false;
            }
            u.add(v);
            return true;
        }
    }
}

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
 *
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
package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NToken;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NStreamTokenizer;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class NElementPathFilter {

    private static final NElementNameMatcherFalse NUTS_ELEMENT_NAME_MATCHER_FALSE = new NElementNameMatcherFalse();
    private static final NElementNameMatcherEven NUTS_ELEMENT_NAME_MATCHER_EVEN = new NElementNameMatcherEven();
    private static final NElementNameMatcherOdd NUTS_ELEMENT_NAME_MATCHER_ODD = new NElementNameMatcherOdd();
    private static final NElementNameMatcherValue NUTS_ELEMENT_NAME_MATCHER_VALUE_ZERO = new NElementNameMatcherValue(0);
    private static final NElementNameMatcherValue NUTS_ELEMENT_NAME_MATCHER_VALUE_MINUS_ONE = new NElementNameMatcherValue(-1);
    private static final NElementNameMatcherTrue NUTS_ELEMENT_NAME_MATCHER_TRUE = new NElementNameMatcherTrue();
    private static final NElementIndexMatcherFalse NUTS_ELEMENT_INDEX_MATCHER_FALSE = new NElementIndexMatcherFalse();
    private static final NElementIndexMatcherUnique NUTS_ELEMENT_INDEX_MATCHER_UNIQUE = new NElementIndexMatcherUnique();
    private static final NElementIndexMatcherEven NUTS_ELEMENT_INDEX_MATCHER_EVEN = new NElementIndexMatcherEven();
    private static final NElementIndexMatcherOdd NUTS_ELEMENT_INDEX_MATCHER_ODD = new NElementIndexMatcherOdd();
    private static final NElementIndexMatcherForValue NUTS_ELEMENT_INDEX_MATCHER_FOR_VALUE_0 = new NElementIndexMatcherForValue(0);
    private static final NElementIndexMatcherForValue NUTS_ELEMENT_INDEX_MATCHER_FOR_VALUE_MINUS_ONE = new NElementIndexMatcherForValue(-1);
    private static final NElementIndexMatcherTrue NUTS_ELEMENT_INDEX_MATCHER_TRUE = new NElementIndexMatcherTrue();

    private static void compile_readChar(NStreamTokenizer st, char c) throws IOException {
        int i = st.nextToken();
        if (i != c) {
            throw new IllegalArgumentException("Expected " + c + ". got " + ((char) i));
        }
    }

    private static String compile_readArrItem(NStreamTokenizer st) throws IOException {
        compile_readChar(st, '[');
        st.nextToken();
        String value = null;
        switch (st.ttype) {
            case ']':
                return "";
            case NToken.TT_WORD:
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
     * @return element path
     */
    public static NElementPath compile(String jpath) {
        NStreamTokenizer st = new NStreamTokenizer(new StringReader(jpath));
        st.resetSyntax();
        st.wordChars(33, 255);
        st.ordinaryChar('.');
        st.ordinaryChar('[');
        st.ordinaryChar(']');
        QueueJsonPath q = new QueueJsonPath();
        try {
            boolean wasNotDotName = false;
            while (st.nextToken() != NToken.TT_EOF) {
                switch (st.ttype) {
                    case NToken.TT_WORD: {
                        wasNotDotName = true;
                        q.queue.add(new SubItemJsonPath(st.sval));
                        break;
                    }
                    case '.': {
                        if (!wasNotDotName) {
                            q.queue.add(new SubItemJsonPath("*"));
                        }
                        wasNotDotName = false;
                        break;
                    }
                    case '[': {
                        wasNotDotName = true;
                        st.pushBack();
                        String p = compile_readArrItem(st);
                        if (p.isEmpty()) {
                            q.queue.add(new ArrItemCollectorJsonPath());
                        } else {
                            q.queue.add(new SubItemJsonPath(p));
                        }
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Unexpected " + st);
                    }
                }
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
        return q;
    }

    public interface NElementNameMatcher {

        boolean matches(int index, NElement name, int len, Map<String, Object> matchContext);
    }

    public interface NElementIndexMatcher {

        boolean matches(NElement value, int index, int len, Map<String, Object> matchContext);
    }

    private static class QueueJsonPath implements NElementPath {

        List<NElementPath> queue = new ArrayList<>();

        @Override
        public List<NElement> filter(NElement element) {
            List<NElement> a = new ArrayList<>();
            a.add(element);
            return filter(a);
        }

        @Override
        public List<NElement> filter(List<NElement> elements) {
            for (NElementPath jsonPath : queue) {
                elements = jsonPath.filter(elements);
            }
            return elements;
        }

        @Override
        public String toString() {
            return queue.stream().map(Object::toString).collect(Collectors.joining("."));
        }

    }

    private static class ArrItemCollectorJsonPath implements NElementPath {
        private final NElements builder;

        public ArrItemCollectorJsonPath() {
            builder = NElements.of();
        }

        @Override
        public List<NElement> filter(NElement element) {
            NArrayElementBuilder aa = builder.ofArrayBuilder();
            aa.add(element);
            return aa.items();
        }

        @Override
        public List<NElement> filter(List<NElement> elements) {
            NArrayElementBuilder aa = builder.ofArrayBuilder();
            for (NElement element : elements) {
                aa.add(element);
            }
            return aa.items();
        }

    }

    private static class SubItemJsonPath extends AbstractJsonPath {

        private final String pattern;

        public SubItemJsonPath(String subItem) {
            super();
            this.pattern = subItem;
        }

        @Override
        public String toString() {
            return "match(" + pattern + ')';
        }

        @Override
        public List<NElement> filter(NElement element) {
            if (element.type() == NElementType.ARRAY) {
                List<NElement> arr = new ArrayList<>(element.asArray().get().children());
                List<NElement> result = new ArrayList<>();
                int len = arr.size();
                NElementIndexMatcher indexMatcher = matchesIndex(pattern);
                Map<String, Object> matchContext = new HashMap<>();
                for (int i = 0; i < arr.size(); i++) {
                    NElement value = arr.get(i);
                    if (indexMatcher.matches(value, i, len, matchContext)) {
                        result.add(value);
                    } else if (indexMatcher.matches(value, i, len, matchContext)) {
                        result.add(value);
                    }
                }
                return result;
            } else if (element.type() == NElementType.OBJECT) {
                List<NElement> result = new ArrayList<>();
                Collection<NElement> aa0 = element.asObject().get().children();
                int len = aa0.size();
                int index = 0;
                Map<String, Object> matchContext = new HashMap<>();
                NElementNameMatcher nameMatcher = matchesName(pattern);
                NElementIndexMatcher indexMatcher = matchesIndex(pattern);
                for (NElement e : aa0) {
                    if (e instanceof NPairElement) {
                        NPairElement se = (NPairElement) e;
                        if (nameMatcher.matches(index, se.key(), len, matchContext)) {
                            result.add(se.value());
                        } else if (indexMatcher.matches(e, index, len, matchContext)) {
                            result.add(e);
                        }
                    } else {
                        if (indexMatcher.matches(e, index, len, matchContext)) {
                            result.add(e);
                        }
                    }
                    index++;
                }
                return result;
            }
            return null;
        }

        private NElementNameMatcher matchesName(String s) {
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
                        List<NElementNameMatcher> ors = new ArrayList<>();
                        for (String vir : s.split(",")) {
                            vir = vir.trim();
                            if (vir.length() > 0) {
                                if (vir.indexOf('-') > 0) {
                                    String[] inter = vir.split("-");
                                    if (inter.length == 2
                                            && NLiteral.of(inter[0]).asInt().isPresent()
                                            && NLiteral.of(inter[1]).asInt().isPresent()) {
                                        int a = Integer.parseInt(inter[0]);
                                        int b = Integer.parseInt(inter[1]);
                                        ors.add(new NElementNameMatcherValueInterval(a, b));
                                    }
                                } else {
                                    if (NLiteral.of(vir).asInt().isPresent()) {
                                        int a = NLiteral.of(vir).asInt().get();
                                        ors.add(new NElementNameMatcherValue(a));
                                    }
                                }
                            }
                        }
                        if (ors.size() == 1) {
                            return ors.get(0);
                        } else if (ors.size() > 1) {
                            return new OrNElementNameMatcher(ors.toArray(new NElementNameMatcher[0]));
                        } else {
                            return NUTS_ELEMENT_NAME_MATCHER_FALSE;
                        }
                    } else if (s.startsWith(":nocase=")) {
                        return new NElementNameMatcherString(s.substring(":nocase=".length()), true);
                    } else {
                        return new NElementNameMatcherString(pattern, false);
                    }
                }
            }
        }

        private NElementIndexMatcher matchesIndex(String s) {
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
                    } else if (NLiteral.of(s).asInt().isPresent()) {
                        return new NElementIndexMatcherForValue(NLiteral.of(s).asInt().get());
                    } else if (s.matches("[0-9][0-9,-]+")) {
                        return createIndexValueInervalMatcher(s);
                    } else {
                        return NUTS_ELEMENT_INDEX_MATCHER_FALSE;
                    }
                }
            }
        }

        private NElementIndexMatcher createIndexValueInervalMatcher(String s) throws NumberFormatException {
            List<NElementIndexMatcher> ors = new ArrayList<>();
            for (String vir : s.split(",")) {
                vir = vir.trim();
                if (vir.length() > 0) {
                    if (vir.indexOf('-') > 0) {
                        String[] inter = vir.split("-");
                        if (inter.length == 2 && NLiteral.of(inter[0]).asInt().isPresent() && NLiteral.of(inter[1]).asInt().isPresent()) {
                            int a = Integer.parseInt(inter[0]);
                            int b = Integer.parseInt(inter[1]);
                            ors.add(new NElementIndexMatcherValueInterval(a, b));
                        }
                    } else {
                        if (NLiteral.of(vir).asInt().isPresent()) {
                            ors.add(new NElementIndexMatcherForValue(Integer.parseInt(vir)));
                        }
                    }
                }
            }
            if (ors.size() == 1) {
                return ors.get(0);
            } else if (ors.size() > 1) {
                return new OrNElementIndexMatcher(ors.toArray(new NElementIndexMatcher[0]));
            } else {
                return new NElementIndexMatcherFalse();
            }
        }

    }

    private static abstract class AbstractJsonPath implements NElementPath {

        public AbstractJsonPath() {

        }

        public abstract List<NElement> filter(NElement element);

        @Override
        public List<NElement> filter(List<NElement> elements) {
            List<NElement> a = new ArrayList<>();
            for (NElement element : elements) {
                List<NElement> ff = filter(element);
                if (ff != null) {
                    a.addAll(ff);
                }
            }
            return a;
        }

    }

    public static class OrNElementNameMatcher implements NElementNameMatcher {

        List<NElementNameMatcher> all = new ArrayList<>();

        public OrNElementNameMatcher(NElementNameMatcher[] all) {
            this.all.addAll(Arrays.asList(all));
        }

        @Override
        public boolean matches(int index, NElement name, int len, Map<String, Object> matchContext) {
            for (NElementNameMatcher any : all) {
                if (any.matches(index, name, len, matchContext)) {
                    return true;
                }
            }
            return false;
        }

    }

    public static class OrNElementIndexMatcher implements NElementIndexMatcher {

        List<NElementIndexMatcher> all = new ArrayList<>();

        public OrNElementIndexMatcher(NElementIndexMatcher[] all) {
            this.all.addAll(Arrays.asList(all));
        }

        @Override
        public boolean matches(NElement value, int index, int len, Map<String, Object> matchContext) {
            for (NElementIndexMatcher any : all) {
                if (any.matches(value, index, len, matchContext)) {
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

    private static class NElementIndexMatcherEven implements NElementIndexMatcher {

        public NElementIndexMatcherEven() {
        }

        @Override
        public boolean matches(NElement value, int index, int len, Map<String, Object> matchContext) {
            return index % 2 == 0;
        }
    }

    private static class NElementIndexMatcherOdd implements NElementIndexMatcher {

        public NElementIndexMatcherOdd() {
        }

        @Override
        public boolean matches(NElement value, int index, int len, Map<String, Object> matchContext) {
            return index % 2 == 1;
        }
    }

    private static class NElementIndexMatcherTrue implements NElementIndexMatcher {

        public NElementIndexMatcherTrue() {
        }

        @Override
        public boolean matches(NElement value, int index, int len, Map<String, Object> matchContext) {
            return true;
        }
    }

    private static class NElementNameMatcherTrue implements NElementNameMatcher {

        public NElementNameMatcherTrue() {
        }

        @Override
        public boolean matches(int index, NElement s, int len, Map<String, Object> matchContext) {
            return true;
        }
    }

    private static class NElementNameMatcherValue implements NElementNameMatcher {

        private final int a;

        public NElementNameMatcherValue(int a) {
            this.a = a;
        }

        @Override
        public boolean matches(int index, NElement name, int len, Map<String, Object> matchContext) {
            if (a < 0) {
                return index == len + a;
            }
            return index == a;
        }
    }

    private static class NElementNameMatcherOdd implements NElementNameMatcher {

        public NElementNameMatcherOdd() {
        }

        @Override
        public boolean matches(int index, NElement name, int len, Map<String, Object> matchContext) {
            return index % 2 == 1;
        }
    }

    private static class NElementNameMatcherEven implements NElementNameMatcher {

        public NElementNameMatcherEven() {
        }

        @Override
        public boolean matches(int index, NElement name, int len, Map<String, Object> matchContext) {
            return index % 2 == 0;
        }
    }

    private static class NElementNameMatcherValueInterval implements NElementNameMatcher {

        private final int a;
        private final int b;

        public NElementNameMatcherValueInterval(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean matches(int index, NElement name, int len, Map<String, Object> matchContext) {
            return index >= a && index <= b;
        }
    }

    private static class NElementNameMatcherFalse implements NElementNameMatcher {

        public NElementNameMatcherFalse() {
        }

        @Override
        public boolean matches(int index, NElement name, int len, Map<String, Object> matchContext) {
            return false;
        }
    }

    private static class NElementNameMatcherString implements NElementNameMatcher {

        private final String pat;
        private final boolean lower;

        public NElementNameMatcherString(String pat, boolean lower) {
            this.lower = lower;
            this.pat = lower ? pat.toLowerCase() : pat;
        }

        @Override
        public boolean matches(int index, NElement name, int len, Map<String, Object> matchContext) {
            if (name.isAnyString()) {
                String sname = name.asStringValue().get();
                return lower
                        ? sname.toLowerCase().matches(pat)
                        : sname.matches(pat);
            }
            return false;
        }
    }

    private static class NElementIndexMatcherForValue implements NElementIndexMatcher {

        private final int a;

        public NElementIndexMatcherForValue(int a) {
            this.a = a;
        }

        @Override
        public boolean matches(NElement value, int index, int len, Map<String, Object> matchContext) {
            if (a < 0) {
                return index == len + a;
            }
            return index == a;
        }
    }

    private static class NElementIndexMatcherFalse implements NElementIndexMatcher {

        public NElementIndexMatcherFalse() {
        }

        @Override
        public boolean matches(NElement value, int index, int len, Map<String, Object> matchContext) {
            return false;
        }
    }

    private static class NElementIndexMatcherValueInterval implements NElementIndexMatcher {

        private final int a;
        private final int b;

        public NElementIndexMatcherValueInterval(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean matches(NElement value, int index, int len, Map<String, Object> matchContext) {
            return index >= a && index <= b;
        }
    }

    private static class NElementIndexMatcherUnique implements NElementIndexMatcher {

        public NElementIndexMatcherUnique() {
        }

        @Override
        public boolean matches(NElement value, int index, int len, Map<String, Object> matchContext) {
            Set<String> u = (Set<String>) matchContext.get("unique");
            if (u == null) {
                u = new HashSet<>();
                matchContext.put("unique", u);
            }
            String v = NElements.of().json().setNtf(false).setValue(value).format()
                    .filteredText();
            if (u.contains(v)) {
                return false;
            }
            u.add(v);
            return true;
        }
    }
}

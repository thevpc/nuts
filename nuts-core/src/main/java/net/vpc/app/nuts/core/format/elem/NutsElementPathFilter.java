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
import java.util.List;
import java.util.stream.Collectors;
import net.vpc.app.nuts.NutsElement;
import net.vpc.app.nuts.NutsElementPath;
import net.vpc.app.nuts.NutsElementType;
import net.vpc.app.nuts.NutsNamedElement;
import net.vpc.app.nuts.NutsSession;

/**
 *
 * @author vpc
 */
public class NutsElementPathFilter {

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
        st.wordChars('0', '9');
        st.wordChars('-', '-');
        st.wordChars('*', '+');
        st.wordChars('+', '+');
        st.wordChars('/', '/');
        st.wordChars('=', '=');
        st.wordChars(':', ':');
        st.wordChars('_', '_');
        st.wordChars('@', '@');
        st.wordChars('#', '#');
        st.wordChars('&', '&');
        st.ordinaryChar('.');
        QueueJsonPath q = new QueueJsonPath();
        try {
            while (st.nextToken() != StreamTokenizer.TT_EOF) {
                switch (st.ttype) {
                    case StreamTokenizer.TT_WORD: {
                        q.queue.add(new SubItemJsonPath(st.sval, session));
                        break;
                    }
                    case '.': {
                        break;
                    }
                    case '[': {
                        st.pushBack();
                        q.queue.add(new SubItemJsonPath(compile_readArrItem(st), session));
                        break;
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
                for (int i = 0; i < arr.size(); i++) {
                    if (indexMatcher.matches(i, len)) {
                        result.add(arr.get(i));
                    }
                }
                return result;
            } else if (element.type() == NutsElementType.OBJECT) {
                List<NutsElement> result = new ArrayList<>();
                Collection<NutsNamedElement> aa0 = element.object().children();
                int len = aa0.size();
                int index = 0;
                NutsElementNameMatcher nameMatcher = matchesName(pattern);
                for (NutsNamedElement se : aa0) {
                    if (nameMatcher.matches(index, se.getName(), len)) {
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
                    return new NutsElementNameMatcher() {
                        @Override
                        public boolean matches(int index, String s, int len) {
                            return true;
                        }
                    };
                }
                case ":last": {
                    return new NutsElementNameMatcher() {
                        @Override
                        public boolean matches(int index, String name, int len) {
                            return index == len - 1;
                        }
                    };
                }
                case ":first": {
                    return new NutsElementNameMatcher() {
                        @Override
                        public boolean matches(int index, String name, int len) {
                            return index == 0;
                        }
                    };
                }
                case ":odd": {
                    return new NutsElementNameMatcher() {
                        @Override
                        public boolean matches(int index, String name, int len) {
                            return index % 2 == 1;
                        }
                    };
                }
                case ":even": {
                    return new NutsElementNameMatcher() {
                        @Override
                        public boolean matches(int index, String name, int len) {
                            return index % 2 == 0;
                        }
                    };
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
                                        ors.add(new NutsElementNameMatcher() {
                                            @Override
                                            public boolean matches(int index, String name, int len) {
                                                return index >= a && index <= b;
                                            }
                                        });
                                    }
                                } else {
                                    if (isInt(vir)) {
                                        int a = Integer.parseInt(vir);
                                        ors.add(new NutsElementNameMatcher() {
                                            @Override
                                            public boolean matches(int index, String name, int len) {
                                                return index == a;
                                            }
                                        });
                                    }
                                }
                            }
                        }
                        if (ors.size() == 1) {
                            return ors.get(0);
                        } else if (ors.size() > 1) {
                            return new OrNutsElementNameMatcher(ors.toArray(new NutsElementNameMatcher[0]));
                        } else {
                            return new NutsElementNameMatcher() {
                                @Override
                                public boolean matches(int index, String name, int len) {
                                    return false;
                                }
                            };
                        }
                    } else if (s.startsWith(":nocase=")) {
                        s = s.substring(":nocase=".length()).toLowerCase();
                        String pat = s;
                        return new NutsElementNameMatcher() {
                            @Override
                            public boolean matches(int index, String name, int len) {
                                return name.toLowerCase().matches(pat);
                            }
                        };
                    } else {
                        String pat = pattern;
                        return new NutsElementNameMatcher() {
                            @Override
                            public boolean matches(int index, String name, int len) {
                                return name.matches(pat);
                            }
                        };
                    }
                }
            }
        }

        private NutsElementIndexMatcher matchesIndex(String s) {
            switch (s) {
                case "*":
                case ":*":
                case ":#*": {
                    return new NutsElementIndexMatcher() {
                        @Override
                        public boolean matches(int index, int len) {
                            return true;
                        }
                    };
                }
                case ":last": {
                    return new NutsElementIndexMatcher() {
                        @Override
                        public boolean matches(int index, int len) {
                            return index == len - 1;
                        }
                    };
                }
                case ":first": {
                    return new NutsElementIndexMatcher() {
                        @Override
                        public boolean matches(int index, int len) {
                            return index == 0;
                        }
                    };
                }
                case ":odd": {
                    return new NutsElementIndexMatcher() {
                        @Override
                        public boolean matches(int index, int len) {
                            return index % 2 == 1;
                        }
                    };
                }
                case ":even": {
                    return new NutsElementIndexMatcher() {
                        @Override
                        public boolean matches(int index, int len) {
                            return index % 2 == 0;
                        }
                    };
                }
                default: {
                    if (s.startsWith(":#")) {
                        s = s.substring(2);
                        List<NutsElementIndexMatcher> ors = new ArrayList<>();
                        for (String vir : s.split(",")) {
                            vir = vir.trim();
                            if (vir.length() > 0) {
                                if (vir.indexOf('-') > 0) {
                                    String[] inter = vir.split("-");
                                    if (inter.length == 2 && isInt(inter[0]) && isInt(inter[1])) {
                                        int a = Integer.parseInt(inter[0]);
                                        int b = Integer.parseInt(inter[1]);
                                        ors.add(new NutsElementIndexMatcher() {
                                            @Override
                                            public boolean matches(int index, int len) {
                                                return index >= a && index <= b;
                                            }
                                        });
                                    }
                                } else {
                                    if (isInt(vir)) {
                                        int a = Integer.parseInt(vir);
                                        ors.add(new NutsElementIndexMatcher() {
                                            @Override
                                            public boolean matches(int index, int len) {
                                                return index == a;
                                            }
                                        });
                                    }
                                }
                            }
                        }
                        if (ors.size() == 1) {
                            return ors.get(0);
                        } else if (ors.size() > 1) {
                            return new OrNutsElementIndexMatcher(ors.toArray(new NutsElementIndexMatcher[0]));
                        } else {
                            return new NutsElementIndexMatcher() {
                                @Override
                                public boolean matches(int index, int len) {
                                    return false;
                                }
                            };
                        }
                    } else {
                        return new NutsElementIndexMatcher() {
                            @Override
                            public boolean matches(int index, int len) {
                                return false;
                            }
                        };
                    }
                }
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
        public boolean matches(int index, String name, int len) {
            for (NutsElementNameMatcher any : all) {
                if (any.matches(index, name, len)) {
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
        public boolean matches(int index, int len) {
            for (NutsElementIndexMatcher any : all) {
                if (any.matches(index, len)) {
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

        boolean matches(int index, String name, int len);
    }

    public interface NutsElementIndexMatcher {

        boolean matches(int index, int len);
    }

}

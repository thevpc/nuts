/**
 * ====================================================================
 *            thevpc-common-md : Simple Markdown Manipulation Library
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
package net.thevpc.nuts.lib.md;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author thevpc
 */
public class PropertiesParser {

    public static final int EOF = -1;
    public static final int WHITE = -2;
    public static final int WORD = -3;
    public static final int NUMBER = -4;
    private StringBuilder st;
    public int st_ttype;
    public String st_ttypeName;
    public String st_image;
    private boolean pushedBack;

    public PropertiesParser(String str) {
        this.st = new StringBuilder(str == null ? "" : str);
    }

    public Map<String, String> parseMap() {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        while (true) {
            String k = parseKey();
            if (k == null) {
                if (st_ttype == EOF) {
                    break;
                } else {
                    throw new IllegalArgumentException("missing key");
                }
            }
            if (parseEq()) {
                String v = parseKey();
                if (v == null) {
                    if (st_ttype == EOF) {
                        m.put(k, "");
                        break;
                    } else {
                        pushBack();
                    }
                } else {
                    m.put(k, v);
                }
            } else {
                m.put(k, null);
            }
        }
        return m;
    }

    private void pushBack() {
        pushedBack = true;
    }

    public int nextToken() {
        if (pushedBack) {
            pushedBack = false;
            return st_ttype;
        }
        if (st.length() == 0) {
            st_ttype = EOF;
            st_ttypeName = "<EOF>";
            st_image = "";
            return EOF;
        }
        char c = st.charAt(0);
        if (isWhite(c)) {
            StringBuilder sb = new StringBuilder();
            while (st.length() > 0 && isWhite(st.charAt(0))) {
                sb.append(st.charAt(0));
                st_remove(1);
            }
            st_image = sb.toString();
            st_ttype = WHITE;
            st_ttypeName = "<WHITE>";
            return st_ttype;
        }
        if (isNumber(c)) {
            Pattern p = Pattern.compile("^(?<x>[-]?[0-9]+(.[0-9]+)?).*");
            Matcher matcher = p.matcher(st.toString());
            if (matcher.find()) {
                String a = matcher.group("x");
                st_image = a;
                st_ttype = NUMBER;
                st_ttypeName = "<NUMBER>";
                st_remove(a.length());
                return st_ttype;
            }
        }
        if (c == '\'') {
            Pattern p = Pattern.compile("^(?<x>[']([^']|(\\'))+[']).*");
            Matcher matcher = p.matcher(st.toString());
            if (matcher.find()) {
                String a = matcher.group("x");
                st_image = a;
                st_ttype = '\'';
                st_ttypeName = "<SIMPLE_QUOTE>";
                st_remove(a.length());
                return st_ttype;
            }
        }
        if (c == '\"') {
            Pattern p = Pattern.compile("^(?<x>[\"]([^']|(\\\"))+[\"]).*");
            Matcher matcher = p.matcher(st.toString());
            if (matcher.find()) {
                String a = matcher.group("x");
                st_image = a;
                st_ttype = '\'';
                st_ttypeName = "<DOUBLE_QUOTE>";
                st_remove(a.length());
                return st_ttype;
            }
        }
        if (c == '{') {
            st_remove(1);
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            while (true) {
                int s = nextToken();
                if (s == EOF) {
                    break;
                }
                if (s == '}') {
                    sb.append((char) s);
                    break;
                } else {
                    sb.append(st_image);
                }
            }
            st_ttype = c;
            st_ttypeName = "{...}";
            st_image = sb.toString();
            return c;
        }
        if (c == '(') {
            st_remove(1);
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            while (true) {
                int s = nextToken();
                if (s == EOF) {
                    break;
                }
                if (s == ')') {
                    sb.append((char) s);
                    break;
                } else {
                    sb.append(st_image);
                }
            }
            st_ttype = c;
            st_ttypeName = "(...)";
            st_image = sb.toString();
            return c;
        }
        if (c == '[') {
            st_remove(1);
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            while (true) {
                int s = nextToken();
                if (s == EOF) {
                    break;
                }
                if (s == ']') {
                    sb.append((char) s);
                    break;
                } else {
                    sb.append(st_image);
                }
            }
            st_ttype = c;
            st_ttypeName = "[...]";
            st_image = sb.toString();
            return c;
        }
        if (isSeparator(c)) {
            st_remove(1);
            st_ttype = c;
            st_ttypeName = "<SEPARATOR>";
            st_image = String.valueOf((char) c);
            return c;
        }
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (st.length() == 0) {
                break;
            }
            char cc = st.charAt(0);
            if (isWhite(cc) || isSeparator(cc)) {
                break;
            }
            sb.append(cc);
            st_remove(1);
        }
        st_image = sb.toString();
        st_ttype = WORD;
        st_ttypeName = "<WORD>";
        return st_ttype;
    }

    private StringBuilder st_remove(int x) {
        return st.delete(0, x);
    }

    private static boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isWhite(char c) {
        return c >= 0 && c <= 32;
    }

    private static boolean isSeparator(char c) {
        return c == '}'
                || c == '.'
                || c == ','
                || c == ';'
                || c == ']'
                || c == ')'
                || c == ':'
                || c == ','
                || c == ';'
                || c == '|'
                || c == '&'
                || c == '-'
                || c == '*'
                || c == '/'
                || c == '+'
                || c == '~'
                || c == '#'
                || c == '`'
                || c == '='
                || c == '<'
                || c == '>'
                || c == '?'
                || c == '!';
    }

//    private String parseUntilStop(char stop, boolean include) {
//        StringBuilder sb = new StringBuilder();
//        while (true) {
//            int i = _nextToken();
//            if (i == EOF) {
//                break;
//            }
//            if (i == stop) {
//                if (include) {
//                    sb.append((char) st_ttype);
//                    break;
//                } else {
//                    pushBack();
//                    break;
//                }
//            } else {
//                pushBack();
//            }
//            String k = parseKey();
//            if (k == null) {
//                break;
//            }
//            if (sb.length() > 0) {
//                sb.append(" ");
//            }
//            sb.append(k);
//        }
//        return sb.toString();
//    }
    private void skipSpaces() {
        while (true) {
            int i = nextToken();
            if (i == EOF) {
                return;
            }
            if (i != WHITE) {
                pushBack();
                return;
            }
        }
    }

    private boolean parseEq() {
        skipSpaces();
        int i = nextToken();
        if (i == '=') {
            return true;
        }
        pushBack();
        return false;
    }

    private String parseKey() {
        skipSpaces();
        int i = nextToken();
        if (i == EOF) {
            return null;
        }
        return st_image;
    }
}

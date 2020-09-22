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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * (Very Simple) JSON parser
 *
 * @author vpc
 * @category Internal
 */
final class PrivateNutsJsonParser {

    private StreamTokenizer st;

    public PrivateNutsJsonParser(Reader r) {
        st = new StreamTokenizer(r);
        st.ordinaryChar('/');
    }

    public Map<String, Object> parseObject() {
        return (Map<String, Object>) parse();
    }

    public List<Object> parseArray() {
        return (List<Object>) parse();
    }

    public Object parse() {
        try {
            Object a = nextElement();
            int p = st.nextToken();
            if (p != StreamTokenizer.TT_EOF) {
                throw new IllegalArgumentException("Json Syntax Error :  encountred " + st);
            }
            return a;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Json Syntax Error :  " + ex.getMessage(), ex);
        }
    }

    private Object nextElement() throws IOException {
        int p = st.nextToken();
        switch (p) {
            case StreamTokenizer.TT_NUMBER: {
                return st.nval;
            }
            case StreamTokenizer.TT_WORD: {
                switch (st.sval) {
                    case "true":
                        return true;
                    case "false":
                        return false;
                    case "null":
                        return null;
                    default:
                        throw new IllegalArgumentException("Json Syntax Error : " + st.sval);
                }
            }
            case '\"': {
                return st.sval;
            }
            case '[': {
                st.pushBack();
                return nextArray();
            }
            case '{': {
                st.pushBack();
                return nextObject();
            }
            default: {
                throw new IllegalArgumentException("Json Syntax Error : " + str(p));
            }
        }
    }

    private List<Object> nextArray() throws IOException {
        List<Object> arr = new ArrayList<>();
        int p = -1;
        p = st.nextToken();
        if (p != '[') {
            throw new IllegalArgumentException("Json Syntax Error : " + str(p));
        }
        p = st.nextToken();
        if (p == ']') {
            return arr;
        } else {
            st.pushBack();
        }
        arr.add(nextElement());
        while ((p = st.nextToken()) != StreamTokenizer.TT_EOF) {
            switch (p) {
                case ']':
                    return arr;
                case ',':
                    arr.add(nextElement());
                    break;
                default:
                    throw new IllegalArgumentException("Json Syntax Error : " + str(p));
            }
        }
        throw new IllegalArgumentException("Json Syntax Error : Missing ]");
    }

    private void readChar(char expected) throws IOException {
        int encountred = st.nextToken();
        if (encountred != expected) {
            throw new IllegalArgumentException("Json Syntax Error : expected " + str(expected) + " , encountred " + str(encountred));
        }
    }

    private Object[] nextKeyValue() throws IOException {
        Object t = nextElement();
        if (!(t instanceof String)) {
            throw new IllegalArgumentException("Json Syntax Error : expected entry name, , encountred " + t);
        }
        readChar(':');
        Object v = nextElement();
        return new Object[]{t, v};
    }

    private Map<String, Object> nextObject() throws IOException {
        Map<String, Object> map = new LinkedHashMap<>();
        int p = -1;
        p = st.nextToken();
        if (p != '{') {
            throw new IllegalArgumentException("Json Syntax Error : " + p);
        }
        p = st.nextToken();
        if (p == '}') {
            return map;
        } else {
            st.pushBack();
        }
        Object[] kv = nextKeyValue();
        map.put((String) kv[0], kv[1]);
        while ((p = st.nextToken()) != StreamTokenizer.TT_EOF) {
            switch (p) {
                case '}':
                    return map;
                case ',':
                    kv = nextKeyValue();
                    map.put((String) kv[0], kv[1]);
                    break;
                default:
                    throw new IllegalArgumentException("Json Syntax Error : " + p);
            }
        }
        throw new IllegalArgumentException("Json Syntax Error : Missing }");
    }

    private String str(int a) {
        switch (a) {
            case StreamTokenizer.TT_EOF:
                return "EOF";
            case StreamTokenizer.TT_EOL:
                return "EOL";
            case StreamTokenizer.TT_NUMBER:
                return "NUMBER";
            case StreamTokenizer.TT_WORD:
                return "WORD";
            case '\"':
                return "DOUBLE_QUOTES";
            case '\'':
                return "SIMPLE_QUOTES";
            case '\r':
                return "\\r";
            default:
                return "'" + ((char) a) + "'";
        }
    }

    public static Map<String,Object> parse(Path path) {
        try(Reader r= Files.newBufferedReader(path)) {
            return new PrivateNutsJsonParser(r).parseObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

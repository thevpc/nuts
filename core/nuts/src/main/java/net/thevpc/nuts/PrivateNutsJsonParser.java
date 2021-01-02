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
package net.thevpc.nuts;

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
 * @author thevpc
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
                throw new NutsBootException("json syntax error :  encountered " + st);
            }
            return a;
        } catch (IOException ex) {
            throw new NutsBootException("json syntax error :  " + ex.getMessage(), ex);
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
                        throw new NutsBootException("json syntax error : " + st.sval);
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
                throw new NutsBootException("json syntax error : " + str(p));
            }
        }
    }

    private List<Object> nextArray() throws IOException {
        List<Object> arr = new ArrayList<>();
        int p = -1;
        p = st.nextToken();
        if (p != '[') {
            throw new NutsBootException("json syntax error : " + str(p));
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
                    throw new NutsBootException("json syntax error : " + str(p));
            }
        }
        throw new NutsBootException("json syntax error : missing ]");
    }

    private void readChar(char expected) throws IOException {
        int encountered = st.nextToken();
        if (encountered != expected) {
            throw new NutsBootException("json syntax error : expected " + str(expected) + " , encountered " + str(encountered));
        }
    }

    private Object[] nextKeyValue() throws IOException {
        Object t = nextElement();
        if (!(t instanceof String)) {
            throw new NutsBootException("json syntax error : expected entry name, , encountered " + t);
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
            throw new NutsBootException("json syntax error : " + p);
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
                    throw new NutsBootException("json syntax error : " + p);
            }
        }
        throw new NutsBootException("json syntax error : Missing }");
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

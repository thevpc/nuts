/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
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
package net.thevpc.nuts.runtime.core.expr;

import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsSession;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public class StringMapParser {
    private final String eqSeparators;
    private final String entrySeparators;

    /**
     *
     * @param eqSeparators equality separators, example '='
     * @param entrySeparators entry separators, example ','
     */
    public StringMapParser(String eqSeparators, String entrySeparators) {
        this.eqSeparators = eqSeparators;
        this.entrySeparators = entrySeparators;
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param reader reader
     * @param stopTokens stopTokens
     * @param result result
     * @return next token
     * @throws IOException IOException
     */
    private static int readToken(Reader reader, String stopTokens, StringBuilder result) throws IOException {
        while (true) {
            int r = reader.read();
            if (r == -1) {
                return -1;
            }
            if (r == '\"' || r == '\'') {
                char s = (char) r;
                while (true) {
                    r = reader.read();
                    if (r == -1) {
                        throw new RuntimeException("Expected " + '\"');
                    }
                    if (r == s) {
                        break;
                    }
                    if (r == '\\') {
                        r = reader.read();
                        if (r == -1) {
                            throw new RuntimeException("Expected " + '\"');
                        }
                        switch ((char) r) {
                            case 'n': {
                                result.append('\n');
                                break;
                            }
                            case 'r': {
                                result.append('\r');
                                break;
                            }
                            case 'f': {
                                result.append('\f');
                                break;
                            }
                            default: {
                                result.append((char) r);
                            }
                        }
                    } else {
                        char cr = (char) r;
                        result.append(cr);
                    }
                }
            } else {
                char cr = (char) r;
                if (stopTokens != null && stopTokens.indexOf(cr) >= 0) {
                    return cr;
                }
                result.append(cr);
            }
        }
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param text text to parse
     * @return parsed map
     */
    public Map<String, String> parseMap(String text,NutsSession session) {
        Map<String, String> m = new LinkedHashMap<>();
        StringReader reader = new StringReader(text == null ? "" : text);
        while (true) {
            StringBuilder key = new StringBuilder();
            int r = 0;
            try {
                r = readToken(reader, eqSeparators + entrySeparators, key);
            } catch (IOException e) {
                throw new NutsIOException(session, e);
            }
            String t = key.toString();
            if (r == -1) {
                if (!t.isEmpty()) {
                    m.put(t, null);
                }
                break;
            } else {
                char c = (char) r;
                if (eqSeparators.indexOf(c) >= 0) {
                    StringBuilder value = new StringBuilder();
                    try {
                        r = readToken(reader, entrySeparators, value);
                    } catch (IOException e) {
                        throw new NutsIOException(session, e);
                    }
                    m.put(t, value.toString());
                    if (r == -1) {
                        break;
                    }
                } else {
                    //
                }
            }
        }
        return m;
    }

}

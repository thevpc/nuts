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
package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NutsStringUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

public class NutsReservedStringMapParser {

    private final String equalsChars;
    private final String separatorChars;
    private final String escapeChars;

    public static NutsReservedStringMapParser of(String equalsChars, String separatorChars, String escapeChars) {
        return new NutsReservedStringMapParser(equalsChars, separatorChars, escapeChars);
    }

    /**
     * @param equalsChars    equality separators, example '='
     * @param separatorChars entry separators, example ','
     */
    public NutsReservedStringMapParser(String equalsChars, String separatorChars, String escapeChars) {
        if (NutsBlankable.isBlank(equalsChars)) {
            throw new IllegalArgumentException("missing equal separators");
        }
        if (NutsBlankable.isBlank(separatorChars)) {
            throw new IllegalArgumentException("missing entry separators");
        }
        this.equalsChars = equalsChars;
        this.separatorChars = separatorChars;
        this.escapeChars = escapeChars == null ? "" : escapeChars;
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param reader     reader
     * @param stopTokens stopTokens
     * @param result     result
     * @return next token
     * @throws IOException IOException
     */
    private static int readToken(Reader reader, String stopTokens, String escapedTokens, StringBuilder result) throws IOException {
        while (true) {
            int r = reader.read();
            if (r == -1) {
                return -1;
            }
            if (r == '\"' || r == '\'') {
                char cr = (char) r;
                while (true) {
                    r = reader.read();
                    if (r == -1) {
                        throw new RuntimeException("Expected " + cr);
                    }
                    if (r == cr) {
                        break;
                    }
                    if (r == '\\') {
                        r = reader.read();
                        if (r == -1) {
                            throw new RuntimeException("Expected " + cr);
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
                            case 't': {
                                result.append('\t');
                                break;
                            }
                            default: {
                                result.append('\\');
                                result.append((char) r);
                            }
                        }
                    } else {
                        result.append((char) r);
                    }
                }
            } else {
                char cr = (char) r;
                if (r == '\\') {
                    r = reader.read();
                    if (r == -1) {
                        result.append(cr);
                    } else {
                        if (stopTokens.indexOf(r) >= 0) {
                            result.append((char) r);
                        } else if (escapedTokens.indexOf(r) >= 0) {
                            result.append((char) r);
                        } else {
                            switch ((char) r) {
                                case ' ': {
                                    result.append(' ');
                                    break;
                                }
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
                                case 't': {
                                    result.append('\t');
                                    break;
                                }
                                default: {
                                    result.append('\\');
                                    result.append((char) r);
                                }
                            }
                        }
                    }
                } else if (stopTokens.indexOf(cr) >= 0) {
                    return cr;
                } else {
                    result.append(cr);
                }
            }
        }
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param text text to parse
     * @return parsed map
     */
    public NutsOptional<Map<String, String>> parse(String text) {
        Map<String, String> m = new LinkedHashMap<>();
        if (NutsBlankable.isBlank(text)) {
            return NutsOptional.of(m);
        }
        StringReader reader = new StringReader(text);
        String sepAndEsc = separatorChars + escapeChars;
        String eqAndSep = equalsChars + separatorChars;
        String eqAndEsc = equalsChars + escapeChars;
        while (true) {
            StringBuilder key = new StringBuilder();
            int r = 0;
            try {
                r = readToken(reader, eqAndSep, sepAndEsc, key);
            } catch (IOException e) {
                return NutsOptional.ofError(x -> NutsMessage.ofPlain("failed to read token"),e);
            }
            String t = key.toString();
            if (r == -1) {
                if (!t.isEmpty()) {
                    m.put(t, null);
                }
                break;
            } else {
                char c = (char) r;
                if (equalsChars.indexOf(c) >= 0) {
                    StringBuilder value = new StringBuilder();
                    try {
                        r = readToken(reader, separatorChars, eqAndEsc, value);
                    } catch (IOException e) {
                        return NutsOptional.ofError(x -> NutsMessage.ofPlain("failed to read token"),e);
                    }
                    m.put(t, value.toString());
                    if (r == -1) {
                        break;
                    }
                } else if (separatorChars.indexOf(c) >= 0) {
                    //this is a key without a value
                    m.put(t, null);
                } else {
                    //
                }
            }
        }
        return NutsOptional.of(m);
    }

    public String format(Map<String, String> map, boolean sort) {
        StringBuilder sb = new StringBuilder();
        if (map != null) {
            if (sort) {
                map = new TreeMap<>(map);
            }
            String escapedChars = separatorChars + equalsChars + escapeChars;
            Set<String> sortedKeys = map.keySet();
            for (String k : sortedKeys) {
                String v = map.get(k);
                if (v != null) {
                    if (sb.length() > 0) {
                        sb.append(separatorChars);
                    }
                    if (v.isEmpty()) {
                        sb.append(
                                NutsStringUtils.formatStringLiteral(k, NutsStringUtils.QuoteType.SIMPLE, NutsSupportMode.PREFERRED, escapedChars)
                        );
                    } else {
                        sb.append(
                                        NutsStringUtils.formatStringLiteral(k, NutsStringUtils.QuoteType.SIMPLE, NutsSupportMode.PREFERRED, escapedChars))
                                .append(equalsChars)
                                .append(NutsStringUtils.formatStringLiteral(v, NutsStringUtils.QuoteType.SIMPLE, NutsSupportMode.PREFERRED, escapedChars)
                                );
                    }
                }
            }
        }
        return NutsStringUtils.trimToNull(sb.toString());
    }
}

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
package net.thevpc.nuts.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

public class NStringMapFormat {
    public static NStringMapFormat URL_FORMAT = new NStringMapFormat("=", "&", "?", true);
    public static NStringMapFormat COMMA_FORMAT = new NStringMapFormat("=", ",", "", true);
    public static NStringMapFormat DEFAULT = URL_FORMAT;

    private final String equalsChars;
    private final String separatorChars;
    private final String escapeChars;
    private final boolean sort;

    public static NStringMapFormat of(String equalsChars, String separatorChars, String escapeChars, boolean sort) {
        return new NStringMapFormat(equalsChars, separatorChars, escapeChars, sort);
    }

    /**
     * @param equalsChars    equality separators, example '='
     * @param separatorChars entry separators, example ','
     */
    public NStringMapFormat(String equalsChars, String separatorChars, String escapeChars, boolean sort) {
        this.sort = sort;
        if (NBlankable.isBlank(equalsChars)) {
            throw new IllegalArgumentException("missing equal separators");
        }
        if (NBlankable.isBlank(separatorChars)) {
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
    public NOptional<Map<String, String>> parse(String text) {
        NOptional<Map<String, List<String>>> d = parseDuplicates(text);
        return d.map(x->{
            Map<String,String> r=new HashMap<>();
            for (Map.Entry<String, List<String>> e : x.entrySet()) {
                r.put(e.getKey(),e.getValue().get(e.getValue().size()-1));
            }
            return r;
        });
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param text text to parse
     * @return parsed map
     */
    public NOptional<Map<String, List<String>>> parseDuplicates(String text) {
        Map<String, List<String>> m = new LinkedHashMap<>();
        if (NBlankable.isBlank(text)) {
            return NOptional.of(m);
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
                return NOptional.ofError(x -> NMsg.ofPlain("failed to read token"), e);
            }
            String t = key.toString();
            if (r == -1) {
                if (!t.isEmpty()) {
                    m.computeIfAbsent(t,v->new ArrayList<>()).add(null);
                }
                break;
            } else {
                char c = (char) r;
                if (equalsChars.indexOf(c) >= 0) {
                    StringBuilder value = new StringBuilder();
                    try {
                        r = readToken(reader, separatorChars, eqAndEsc, value);
                    } catch (IOException e) {
                        return NOptional.ofError(x -> NMsg.ofPlain("failed to read token"), e);
                    }
                    m.computeIfAbsent(t,v->new ArrayList<>()).add(value.toString());
                    if (r == -1) {
                        break;
                    }
                } else if (separatorChars.indexOf(c) >= 0) {
                    //this is a key without a value
                    m.computeIfAbsent(t,v->new ArrayList<>()).add(null);
                } else {
                    //
                }
            }
        }
        return NOptional.of(m);
    }

    public String format(Map<String, String> map) {
        if (map != null) {
            Map<String, List<String>> map2=new HashMap<>();
            for (Map.Entry<String, String> e : map.entrySet()) {
                map2.put(e.getKey(),Arrays.asList(e.getValue()));
            }
            return formatDuplicates(map2);
        }
        return "";
    }

    public String formatDuplicates(Map<String, List<String>> map) {
        StringBuilder sb = new StringBuilder();
        if (map != null) {
            if (sort) {
                map = new TreeMap<>(map);
            }
            String escapedChars = separatorChars + equalsChars + escapeChars;
            Set<String> sortedKeys = map.keySet();
            for (String k : sortedKeys) {
                List<String> strings = map.get(k);
                for (String v : strings) {
                    if (v != null) {
                        if (sb.length() > 0) {
                            sb.append(separatorChars);
                        }
                        if (v.isEmpty()) {
                            sb.append(
                                    NStringUtils.formatStringLiteral(k, NQuoteType.SIMPLE, NSupportMode.PREFERRED, escapedChars)
                            );
                        } else {
                            sb.append(
                                            NStringUtils.formatStringLiteral(k, NQuoteType.SIMPLE, NSupportMode.PREFERRED, escapedChars))
                                    .append(equalsChars)
                                    .append(NStringUtils.formatStringLiteral(v, NQuoteType.SIMPLE, NSupportMode.PREFERRED, escapedChars)
                                    );
                        }
                    } else {
                        if (sb.length() > 0) {
                            sb.append(separatorChars);
                        }
                        sb.append(
                                NStringUtils.formatStringLiteral(k, NQuoteType.SIMPLE, NSupportMode.PREFERRED, escapedChars)
                        );
                    }
                }
            }
        }
        return NStringUtils.trimToNull(sb.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NStringMapFormat that = (NStringMapFormat) o;
        return sort == that.sort && Objects.equals(equalsChars, that.equalsChars) && Objects.equals(separatorChars, that.separatorChars) && Objects.equals(escapeChars, that.escapeChars);
    }

    @Override
    public int hashCode() {
        return Objects.hash(equalsChars, separatorChars, escapeChars, sort);
    }

    public String getEqualsChars() {
        return equalsChars;
    }

    public String getSeparatorChars() {
        return separatorChars;
    }

    public String getEscapeChars() {
        return escapeChars;
    }

    public boolean isSort() {
        return sort;
    }

    public NStringMapFormatBuilder builder() {
        return NStringMapFormatBuilder.of()
                .setEscapeChars(getEscapeChars())
                .setEqualsChars(getEqualsChars())
                .setSeparatorChars(getSeparatorChars())
                .setSort(isSort())
                ;
    }
}

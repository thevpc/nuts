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
package net.thevpc.nuts.util;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.*;

public class NStringMapFormat {
    public static NStringMapFormat URL_FORMAT = NStringMapFormat.of("=", "&", "\\", true);
    public static NStringMapFormat COMMA_FORMAT = NStringMapFormat.of("=", ",", "\\", true);
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
        if (equalsChars != null) {
            for (char c : equalsChars.toCharArray()) {
                if (isWhitespace(c)) {
                    throw new IllegalArgumentException("eq chars could not include whitespaces");
                }
            }
        }
        if (escapeChars != null) {
            for (char c : escapeChars.toCharArray()) {
                if (isWhitespace(c)) {
                    throw new IllegalArgumentException("eq chars could not include whitespaces");
                }
            }
        }
        if (separatorChars != null) {
            for (char c : separatorChars.toCharArray()) {
                if (isWhitespace(c)) {
                    throw new IllegalArgumentException("eq chars could not include whitespaces");
                }
            }
        }
        this.equalsChars = equalsChars == null ? "" : equalsChars;
        this.separatorChars = separatorChars == null ? "" : separatorChars;
        this.escapeChars = escapeChars == null ? "" : escapeChars;
    }

    private enum TokenType {
        DOUBLE_QUOTED,
        SIMPLE_QUOTED,
        WORD,
        EQ,
        SEP;

        boolean isAnyWord() {
            return this == DOUBLE_QUOTED || this == SIMPLE_QUOTED || this == WORD;
        }
    }

    private static class TokenConfig {
        String eqChars;
        String sepChars;
        String escapeChars;

        public String getEqChars() {
            return eqChars;
        }

        public TokenConfig setEqChars(String eqChars) {
            this.eqChars = eqChars;
            return this;
        }

        public String getSepChars() {
            return sepChars;
        }

        public TokenConfig setSepChars(String sepChars) {
            this.sepChars = sepChars;
            return this;
        }

        public String getEscapeChars() {
            return escapeChars;
        }

        public TokenConfig setEscapeChars(String escapeChars) {
            this.escapeChars = escapeChars;
            return this;
        }
    }

    private static class Token {
        TokenType type;
        String value;
        String image;

        public Token(TokenType type, String value) {
            this(type, value, value);
        }

        public Token(TokenType type, String value, String image) {
            this.type = type;
            this.value = value;
            this.image = image;
        }
    }

    private static Token readToken(PushbackReader reader, TokenConfig conf) throws IOException {
        String escapedTokens = conf.getEscapeChars();
        String eqChars = conf.getEqChars();
        String sepChars = conf.getSepChars();
        StringBuilder value = new StringBuilder();
        StringBuilder image = new StringBuilder();
        int r = reader.read();
        if (r == -1) {
            return null;
        }
        char r1 = (char) r;
        if (isWhitespace(r1)) {
            while (true) {
                r = reader.read();
                if (r == -1) {
                    return null;
                }
                if (!isWhitespace((char) r)) {
                    r1 = (char) r;
                    break;
                }
            }
        } else if (eqChars.indexOf(r1) >= 0) {
            return new Token(TokenType.EQ, String.valueOf(r1));
        } else if (sepChars.indexOf(r1) >= 0) {
            return new Token(TokenType.SEP, String.valueOf(r1));
        }
        if (r == '\"' || r == '\'') {
            char cr = (char) r;
            image.append(cr);
            while (true) {
                r = reader.read();
                if (r == -1) {
                    throw new RuntimeException("Expected " + cr);
                }
                image.append(cr);
                if (r == cr) {
                    return new Token(cr == '\"' ? TokenType.SIMPLE_QUOTED : TokenType.DOUBLE_QUOTED, value.toString());
                }
                if (r == '\\') {
                    r = reader.read();
                    if (r == -1) {
                        throw new RuntimeException("Expected " + cr);
                    }
                    image.append((char) r);
                    switch ((char) r) {
                        case 'n': {
                            value.append('\n');
                            break;
                        }
                        case 'r': {
                            value.append('\r');
                            break;
                        }
                        case 'f': {
                            value.append('\f');
                            break;
                        }
                        case 't': {
                            value.append('\t');
                            break;
                        }
                        default: {
                            value.append('\\');
                            value.append((char) r);
                        }
                    }
                } else {
                    value.append((char) r);
                }
            }
        } else {
            reader.unread(r);
            while (true) {
                r = reader.read();
                if (r < 0) {
                    return new Token(TokenType.WORD, value.toString(), image.toString());
                }
                char cr = (char) r;
                if (escapedTokens.indexOf(cr) >= 0) {
                    image.append(cr);
                    r = reader.read();
                    if (r == -1) {
                        value.append(cr);
                        return new Token(TokenType.WORD, value.toString(), image.toString());
                    } else {
                        cr = (char) r;

//                        r = reader.read();
//                        if (r == -1) {
//                            value.append(cr);
//                            return new Token(TokenType.WORD, value.toString(),image.toString());
//                        }
//                        cr = (char) r;
                        image.append(cr);
                        if (escapedTokens.indexOf(cr) >= 0 || isWhitespace(cr) || eqChars.indexOf(cr) >= 0 || sepChars.indexOf(cr) >= 0) {
                            value.append(cr);
                        } else {
                            switch ((char) r) {
                                case ' ': {
                                    value.append(' ');
                                    break;
                                }
                                case 'n': {
                                    value.append('\n');
                                    break;
                                }
                                case 'r': {
                                    value.append('\r');
                                    break;
                                }
                                case 'f': {
                                    value.append('\f');
                                    break;
                                }
                                case 't': {
                                    value.append('\t');
                                    break;
                                }
                                default: {
                                    value.append(cr);
                                    value.append((char)r);
                                }
                            }
                        }
                    }
                } else if (isWhitespace(cr) || eqChars.indexOf(cr) >= 0 || sepChars.indexOf(cr) >= 0) {
                    reader.unread(cr);
                    return new Token(TokenType.WORD, value.toString(), image.toString());
                } else {
                    value.append(cr);
                    image.append(cr);
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
        return d.map(x -> {
            Map<String, String> r = new HashMap<>();
            for (Map.Entry<String, List<String>> e : x.entrySet()) {
                r.put(e.getKey(), e.getValue().get(e.getValue().size() - 1));
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
        PushbackReader reader = new PushbackReader(new StringReader(text));
        TokenConfig conf = new TokenConfig()
                .setSepChars(separatorChars)
                .setEqChars(equalsChars)
                .setEscapeChars(escapeChars);
        List<Token> tokens = new ArrayList<>();
        while (true) {
            Token r = null;
            try {
                r = readToken(reader, conf);
            } catch (IOException e) {
                return NOptional.ofError(() -> NMsg.ofPlain("failed to read token"), e);
            }
            if (r != null) {
                tokens.add(r);
            } else {
                break;
            }
        }
        if (skipSeparator(tokens, conf)) {
            m.computeIfAbsent(null, v -> new ArrayList<>()).add(null);
        }
        while (true) {
            skipSeparator(tokens, conf);
            Map.Entry<String, String> u;
            if ((u = readEntry(tokens, conf)) != null) {
                m.computeIfAbsent(u.getKey(), v -> new ArrayList<>()).add(u.getValue());
            } else {
                break;
            }
        }
        return NOptional.of(m);
    }

    private static boolean skipSeparator(List<Token> tokens, TokenConfig conf) {
        if (!tokens.isEmpty()) {
            if (tokens.get(0).type == TokenType.SEP) {
                tokens.remove(0);
                return true;
            }
        }
        return false;
    }

    private static Map.Entry<String, String> readEntry(List<Token> tokens, TokenConfig conf) {
        if (!tokens.isEmpty()) {
            if (tokens.get(0).type.isAnyWord()) {
                String k = tokens.remove(0).value;
                if (!tokens.isEmpty()) {
                    if (tokens.get(0).type == TokenType.EQ) {
                        tokens.remove(0);
                        if (!tokens.isEmpty()) {
                            if (tokens.get(0).type.isAnyWord()) {
                                Token v = tokens.remove(0);
                                return new AbstractMap.SimpleEntry<>(k, v.value);
                            } else {
                                return new AbstractMap.SimpleEntry<>(k, null);
                            }
                        } else {
                            return new AbstractMap.SimpleEntry<>(k, null);
                        }
                    } else if (tokens.get(0).type == TokenType.SEP) {
                        tokens.remove(0);
                        return new AbstractMap.SimpleEntry<>(k, null);
                    } else if (conf.getEqChars().isEmpty() && tokens.get(0).type.isAnyWord()) {
                        String v = tokens.remove(0).value;
                        return new AbstractMap.SimpleEntry<>(k, v);
                    } else {
                        return new AbstractMap.SimpleEntry<>(k, null);
                    }
                } else {
                    return new AbstractMap.SimpleEntry<>(k, null);
                }
            } else if (tokens.get(0).type == TokenType.SEP) {
                tokens.remove(0);
                return new AbstractMap.SimpleEntry<>(null, null);
            } else if (tokens.get(0).type == TokenType.EQ) {
                tokens.remove(0);
                if (!tokens.isEmpty()) {
                    if (tokens.get(0).type.isAnyWord()) {
                        Token v = tokens.remove(0);
                        return new AbstractMap.SimpleEntry<>(null, v.value);
                    } else {
                        return new AbstractMap.SimpleEntry<>(null, null);
                    }
                } else {
                    return new AbstractMap.SimpleEntry<>(null, null);
                }
            } else {
                return new AbstractMap.SimpleEntry<>(null, null);
            }
        } else {
            return null;
        }
    }

    private static boolean isWhitespace(char c) {
        if (c <= 32) {
            return true;
        }
        return Character.isWhitespace(c);
    }

    public String format(Map<String, String> map) {
        if (map != null) {
            Map<String, List<String>> map2 = new HashMap<>();
            for (Map.Entry<String, String> e : map.entrySet()) {
                map2.put(e.getKey(), Arrays.asList(e.getValue()));
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

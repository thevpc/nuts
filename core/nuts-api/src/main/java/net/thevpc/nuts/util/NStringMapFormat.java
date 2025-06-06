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

import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.io.NIOException;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Function;

public class NStringMapFormat {
    public static final Function<String, String> URL_ENCODER = x -> {
        try {
            return URLEncoder.encode(x, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    };
    public static final Function<String, String> URL_DECODER = x -> {
        try {
            return URLDecoder.decode(x, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    };
    public static NStringMapFormat URL_FORMAT = NStringMapFormatBuilder.of().setEqualsChars("=").setSeparatorChars("&").setSort(true).setEncoder(URL_ENCODER).setDecoder(URL_DECODER).setAcceptNullKeys(false).build();
    public static NStringMapFormat HTTP_HEADER_FORMAT = NStringMapFormatBuilder.of().setEqualsChars("=").setSeparatorChars(";").setDoubleQuoteSupported(true).setSort(false).setEncoder(URL_ENCODER).setDecoder(URL_DECODER).setAcceptNullKeys(false).build();
    public static NStringMapFormat COMMA_FORMAT = NStringMapFormatBuilder.of().setEqualsChars("=").setSeparatorChars(",").setEscapeChars("\\").setSort(true).setQuoteSupported(true).setAcceptNullKeys(false).build();
    public static NStringMapFormat DEFAULT = URL_FORMAT;

    private final String equalsChars;
    private final String separatorChars;
    private final String escapeChars;
    private final boolean sort;
    private final Function<String, String> decoder;
    private final Function<String, String> encoder;
    private boolean doubleQuoteSupported;
    private boolean simpleQuoteSupported;
    private boolean acceptNullKeys;


    NStringMapFormat(NStringMapFormatBuilder builder) {
        if (builder == null) {
            builder = new NStringMapFormatBuilder();
        }
        this.sort = builder.isSort();
        this.encoder = builder.getEncoder();
        this.decoder = builder.getDecoder();
        if (builder.getEqualsChars() != null) {
            for (char c : builder.getEqualsChars().toCharArray()) {
                if (isWhitespace(c)) {
                    throw new IllegalArgumentException("eq chars could not include whitespaces");
                }
            }
        }
        if (builder.getEscapeChars() != null) {
            for (char c : builder.getEscapeChars().toCharArray()) {
                if (isWhitespace(c)) {
                    throw new IllegalArgumentException("eq chars could not include whitespaces");
                }
            }
        }
        if (builder.getSeparatorChars() != null) {
            for (char c : builder.getSeparatorChars().toCharArray()) {
                if (isWhitespace(c)) {
                    throw new IllegalArgumentException("eq chars could not include whitespaces");
                }
            }
        }
        this.equalsChars = builder.getEqualsChars() == null ? "" : builder.getEqualsChars();
        this.separatorChars = builder.getSeparatorChars() == null ? "" : builder.getSeparatorChars();
        this.escapeChars = builder.getEscapeChars() == null ? "" : builder.getEscapeChars();
        this.doubleQuoteSupported = builder.isDoubleQuoteSupported();
        this.simpleQuoteSupported = builder.isSimpleQuoteSupported();
        this.acceptNullKeys = builder.isAcceptNullKeys();
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

        @Override
        public String toString() {
            return "Token{" +
                    "type=" + type +
                    ", value='" + value + '\'' +
                    ", image='" + image + '\'' +
                    '}';
        }
    }

    private Token readToken(PushbackReader reader, Function<String, String> decoder) {
        try {
            if (decoder == null) {
                decoder = x -> x;
            }
            String escapedTokens = this.escapeChars;
            String eqChars = this.equalsChars;
            String sepChars = this.separatorChars;
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
                return new Token(TokenType.EQ, decoder.apply(String.valueOf(r1)), String.valueOf(r1));
            } else if (sepChars.indexOf(r1) >= 0) {
                return new Token(TokenType.SEP, decoder.apply(String.valueOf(r1)), String.valueOf(r1));
            }
            if ((r == '\"' && doubleQuoteSupported)
                    ||
                    (r == '\'' && simpleQuoteSupported)
            ) {
                char cr = (char) r;
                image.append(cr);
                while (true) {
                    r = reader.read();
                    if (r == -1) {
                        throw new RuntimeException("Expected " + cr);
                    }
                    image.append(cr);
                    if (r == cr) {
                        return new Token(cr == '\"' ? TokenType.SIMPLE_QUOTED : TokenType.DOUBLE_QUOTED, decoder.apply(value.toString()), value.toString());
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
                        return new Token(TokenType.WORD, decoder.apply(value.toString()), image.toString());
                    }
                    char cr = (char) r;
                    if (escapedTokens.indexOf(cr) >= 0) {
                        image.append(cr);
                        r = reader.read();
                        if (r == -1) {
                            value.append(cr);
                            return new Token(TokenType.WORD, decoder.apply(value.toString()), image.toString());
                        } else {
                            cr = (char) r;

//                        r = reader.read();
//                        if (r == -1) {
//                            value.append(cr);
//                            return new Token(TokenType.WORD, decoder.apply(value.toString()),image.toString());
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
                                        value.append((char) r);
                                    }
                                }
                            }
                        }
                    } else if (isWhitespace(cr) || eqChars.indexOf(cr) >= 0 || sepChars.indexOf(cr) >= 0) {
                        reader.unread(cr);
                        return new Token(TokenType.WORD, decoder.apply(value.toString()), image.toString());
                    } else {
                        value.append(cr);
                        image.append(cr);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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
            Map<String, String> r = new LinkedHashMap<>();
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
        Function<String, String> decoder = this.decoder;
        if (decoder == null) {
            decoder = x -> x;
        }
        Map<String, List<String>> m = new LinkedHashMap<>();
        if (NBlankable.isBlank(text)) {
            return NOptional.of(m);
        }
        PushbackReader reader = new PushbackReader(new StringReader(text));
        List<Token> tokens = new ArrayList<>();
        while (true) {
            Token r = null;
            try {
                r = readToken(reader, decoder);
            } catch (UncheckedIOException | NIOException e) {
                return NOptional.ofError(() -> NMsg.ofPlain("failed to read token"), e);
            }
            if (r != null) {
                tokens.add(r);
            } else {
                break;
            }
        }
        if (skipSeparator(tokens)) {
            m.computeIfAbsent(null, v -> new ArrayList<>()).add(null);
        }
        while (true) {
            skipSeparator(tokens);
            Map.Entry<String, String> u;
            if ((u = readEntry(tokens)) != null) {
                m.computeIfAbsent(u.getKey(), v -> new ArrayList<>()).add(u.getValue());
            } else {
                break;
            }
        }
        return NOptional.of(m);
    }

    private boolean skipSeparator(List<Token> tokens) {
        if (!tokens.isEmpty()) {
            if (tokens.get(0).type == TokenType.SEP) {
                tokens.remove(0);
                return true;
            }
        }
        return false;
    }

    private Map.Entry<String, String> readEntry(List<Token> tokens) {
        boolean acceptNullKeys = this.acceptNullKeys;
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
                    } else if (getEqualsChars().isEmpty() && tokens.get(0).type.isAnyWord()) {
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
                return new AbstractMap.SimpleEntry<>(acceptNullKeys ? null : "", null);
            } else if (tokens.get(0).type == TokenType.EQ) {
                tokens.remove(0);
                if (!tokens.isEmpty()) {
                    if (tokens.get(0).type.isAnyWord()) {
                        Token v = tokens.remove(0);
                        return new AbstractMap.SimpleEntry<>(acceptNullKeys ? null : "", v.value);
                    } else {
                        return new AbstractMap.SimpleEntry<>(acceptNullKeys ? null : "", null);
                    }
                } else {
                    return new AbstractMap.SimpleEntry<>(acceptNullKeys ? null : "", null);
                }
            } else {
                return new AbstractMap.SimpleEntry<>(acceptNullKeys ? null : "", null);
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
        Function<String, String> encoder = this.encoder == null ? x -> x : this.encoder;
        StringBuilder sb = new StringBuilder();
        if (map != null) {
            if (sort) {
                map = new TreeMap<>(map);
            }
//            String escapedChars = separatorChars + equalsChars + escapeChars;
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
                                    //NStringUtils.formatStringLiteral(encoder.apply(k), NElementType.SINGLE_QUOTED_STRING, NSupportMode.PREFERRED, escapedChars)
                                    encoder.apply(k)
                            );
                        } else {
                            sb
//                                    .append(NStringUtils.formatStringLiteral(encoder.apply(k), NElementType.SINGLE_QUOTED_STRING, NSupportMode.PREFERRED, escapedChars))
//                                    .append(equalsChars)
//                                    .append(NStringUtils.formatStringLiteral(encoder.apply(v), NElementType.SINGLE_QUOTED_STRING, NSupportMode.PREFERRED, escapedChars))
                                    .append(encoder.apply(k))
                                    .append(equalsChars)
                                    .append(encoder.apply(v))
                            ;
                        }
                    } else {
                        if (sb.length() > 0) {
                            sb.append(separatorChars);
                        }
                        sb.append(
//                                NStringUtils.formatStringLiteral(encoder.apply(k), NElementType.SINGLE_QUOTED_STRING, NSupportMode.PREFERRED, escapedChars)
                                encoder.apply(k)
                        );
                    }
                }
            }
        }
        return NStringUtils.trimToNull(sb.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NStringMapFormat that = (NStringMapFormat) o;
        return sort == that.sort && Objects.equals(equalsChars, that.equalsChars) && Objects.equals(separatorChars, that.separatorChars) && Objects.equals(escapeChars, that.escapeChars) && Objects.equals(decoder, that.decoder) && Objects.equals(encoder, that.encoder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(equalsChars, separatorChars, escapeChars, sort, decoder, encoder);
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

    public Function<String, String> getDecoder() {
        return decoder;
    }

    public Function<String, String> getEncoder() {
        return encoder;
    }

    public NStringMapFormatBuilder builder() {
        return NStringMapFormatBuilder.of()
                .copyFrom(this)
                ;
    }
}

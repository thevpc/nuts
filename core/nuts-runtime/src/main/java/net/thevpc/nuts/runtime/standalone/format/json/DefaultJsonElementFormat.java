/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.format.json;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.elem.NElementStreamFormat;
import net.thevpc.nuts.util.NHex;
import net.thevpc.nuts.util.NMsg;

import java.io.*;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultJsonElementFormat implements NElementStreamFormat {

    private NWorkspace ws;

    public DefaultJsonElementFormat(NWorkspace ws) {
        this.ws = ws;
    }

    public NElement parseElement(String string, NElementFactoryContext context) {
        if (string == null) {
            throw new NullPointerException("string is null");
        }
        return parseElement(new StringReader(string), context);
    }

    public void write(NPrintStream out, NElement data, boolean compact) {
        writeSafe(out, ensureJson(data), compact ? null : "");
    }

    private void write(NPrintStream out, NElement data, String indent) {
        writeSafe(out, ensureJson(data), indent);
    }

    private void writeSafe(NPrintStream out, NElement data, String indent) {

        switch (data.type()) {
            case NULL: {
                out.print("null");
                break;
            }
            case BOOLEAN: {
                out.print(data.asBoolean().orElse(false));
                break;
            }
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG: {
                out.print(data.asNumber().orElse(0));
                break;
            }
            case FLOAT:
            case DOUBLE: {
                out.print(data.asNumber().orElse(0.0));
                break;
            }
            case INSTANT:
            case STRING:
            case NAME:
//            case NUTS_STRING:
            {
                StringBuilder sb = new StringBuilder("\"");
                final String str = data.asString().orElse("");
                char[] chars = str.toCharArray();

                for (int i = 0; i < chars.length; i++) {
                    char c = chars[i];
                    if (c < 32) {
                        switch (c) {
                            case '\n': {
                                sb.append('\\').append('n');
                                break;
                            }
                            case '\f': {
                                sb.append('\\').append('f');
                                break;
                            }
                            case '\t': {
                                sb.append('\\').append('t');
                                break;
                            }
                            case '\r': {
                                sb.append('\\').append('r');
                                break;
                            }
                            case '\b': {
                                sb.append('\\').append('b');
                                break;
                            }
                            default: {
                                sb.append('\\');
                                sb.append('u');
                                sb.append(NHex.toHexChar((c >> 12) & 0xF));
                                sb.append(NHex.toHexChar((c >> 8) & 0xF));
                                sb.append(NHex.toHexChar((c >> 4) & 0xF));
                                sb.append(NHex.toHexChar(c & 0xF));
                            }
                        }
                    } else {
                        switch (c) {
                            case '\\': {
                                sb.append(c).append(c);
                                break;
                            }
                            case '"': {
                                sb.append('\\').append('"');
                                break;
                            }
                            default: {
                                if (c > 0x007e) {
                                    sb.append('\\');
                                    sb.append('u');
                                    sb.append(NHex.toHexChar((c >> 12) & 0xF));
                                    sb.append(NHex.toHexChar((c >> 8) & 0xF));
                                    sb.append(NHex.toHexChar((c >> 4) & 0xF));
                                    sb.append(NHex.toHexChar(c & 0xF));
                                } else {
                                    sb.append(c);
                                }
                            }
                        }
                    }
                }
                sb.append('\"');
                out.print(sb);
                break;
            }

            case ARRAY: {
                NArrayElement arr = data.asArray().get();
                if (arr.size() == 0) {
                    out.print("[]");
                } else {
                    out.print('[');
                    boolean first = true;
                    String indent2 = indent + "  ";
                    for (NElement e : arr.items()) {
                        if (first) {
                            first = false;
                        } else {
                            out.print(',');
                        }
                        if (indent != null) {
                            out.print('\n');
                            out.print(indent2);
                            writeSafe(out, e, indent2);
                        } else {
                            writeSafe(out, e, null);
                        }
                    }
                    if (indent != null) {
                        out.print('\n');
                        out.print(indent);
                    }
                    out.print(']');
                }
                break;
            }
            case OBJECT: {
                NObjectElement obj = data.asObject().get();
                if (obj.size() == 0) {
                    out.print("{}");
                } else {
                    out.print('{');
                    boolean first = true;
                    String indent2 = indent + "  ";
                    for (NElement e : obj.children()) {
                        if (first) {
                            first = false;
                        } else {
                            out.print(',');
                        }
                        if (indent != null) {
                            out.print('\n');
                            out.print(indent2);
                            if (e instanceof NPairElement) {
                                NPairElement ee = (NPairElement) e;
                                writeSafe(out, ee.key(), indent2);
                                out.print(':');
                                out.print(' ');
                                writeSafe(out, ee.value(), indent2);
                            } else {
                                writeSafe(out, e, indent2);
                            }
                        } else {
                            if (e instanceof NPairElement) {
                                NPairElement ee = (NPairElement) e;
                                writeSafe(out, ee.key(), null);
                                out.print(':');
                                writeSafe(out, ee.value(), null);
                            } else {
                                writeSafe(out, e, null);
                            }
                        }
                    }
                    if (indent != null) {
                        out.print('\n');
                        out.print(indent);
                    }
                    out.print('}');
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("unsupported JSON format for " + data.type());
            }
        }
    }

    private NElement _jsonAnnotations(List<NElementAnnotation> a) {
        NElements elems = NElements.of();
        return elems.ofArray(
                a.stream().map(x -> _jsonAnnotation(x)).toArray(NElement[]::new)
        );
    }

    private NElement _jsonAnnotation(NElementAnnotation a) {
        NElements elems = NElements.of();
        NObjectElementBuilder u = elems.ofObjectBuilder()
                .add("annotationName", a.name());
        if (a.params() != null) {
            u.add("annotationParams",
                    elems.ofArray(
                            a.params().stream().map(x -> ensureJson(x)).toArray(NElement[]::new)
                    )
            );
        }
        return u.build();
    }

    private NElement ensureJson(NElement e) {
        NElements elems = NElements.of();
        switch (e.type()) {
            case NULL:
            case INTEGER:
            case LONG:
            case SHORT:
            case BYTE:
            case DOUBLE:
            case FLOAT:
            case BOOLEAN:
            case BIG_DECIMAL:
            case BIG_INTEGER: {
                List<NElementAnnotation> a = e.annotations();
                if (a.isEmpty()) {
                    return e;
                } else {
                    return elems.ofObjectBuilder()
                            .add("value", e.builder().clearAnnotations().build())
                            .add(a.isEmpty() ? null : elems.ofPair("@annotations", _jsonAnnotations(a)))
                            .build();
                }
            }
            case REGEX:
            case NAME:
            case INSTANT:
            case BIG_COMPLEX:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX:
            case CUSTOM:
            case CHAR_STREAM:
            case BINARY_STREAM:
            case LOCAL_TIME:
            case LOCAL_DATE:
            case LOCAL_DATETIME:
            case CHAR:
            case STRING:
                // TODO FIXE ME LATER
            {
                List<NElementAnnotation> a = e.annotations();
                if (a.isEmpty()) {
                    return elems.ofString(e.asString().get());
                } else {
                    return elems.ofObjectBuilder()
                            .add("value", e.builder().clearAnnotations().build())
                            .add(a.isEmpty() ? null : elems.ofPair("@annotations", _jsonAnnotations(a)))
                            .build();
                }
            }
            case ALIAS: {
                List<NElementAnnotation> a = e.annotations();
                if (a.isEmpty()) {
                    return elems.ofString("&" + e.asString().get());
                } else {
                    return elems.ofObjectBuilder()
                            .add("value", "&" + e.builder().clearAnnotations().build().asString().get())
                            .add(a.isEmpty() ? null : elems.ofPair("@annotations", _jsonAnnotations(a)))
                            .build();
                }
            }
            case PAIR: {
                List<NElementAnnotation> a = e.annotations();
                NPairElement p0 = e.asPair().get();
                NPairElementBuilder p = p0.builder().clearAnnotations()
                        .key(ensureJson(p0.key()))
                        .value(ensureJson(p0.value()));
                if (p.key().isPrimitive()) {
                    if (a.isEmpty()) {
                        return p.build();
                    } else {
                        return elems.ofObjectBuilder()
                                .add("value", p.build())
                                .add(a.isEmpty() ? null : elems.ofPair("@annotations", _jsonAnnotations(a)))
                                .build();
                    }
                } else {
                    if (a.isEmpty()) {
                        return elems.ofObjectBuilder()
                                .add("key", p.key())
                                .add("value", p.value())
                                .add(a.isEmpty() ? null : elems.ofPair("@annotations", _jsonAnnotations(a)))
                                .build();
                    } else {
                        return elems.ofObjectBuilder()
                                .add("key", p.key())
                                .add("value", p.value())
                                .add(a.isEmpty() ? null : elems.ofPair("@annotations", _jsonAnnotations(a)))
                                .build();
                    }
                }
            }
            case ARRAY:
            case NAMED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case PARAMETRIZED_ARRAY: {
                List<NElementAnnotation> a = e.annotations();

                NArrayElement p0 = e.asArray().get();
                NArrayElementBuilder p = elems.ofArrayBuilder()
                        .addAll(p0.children().stream().map(x -> ensureJson(x)).toArray(NElement[]::new));

                if (a.isEmpty() && !p0.isNamed() && !p0.isParametrized()) {
                    return p.build();
                } else {
                    return elems.ofObjectBuilder()
                            .add("value", p.build())
                            .add(a.isEmpty() ? null : elems.ofPair("@annotations", _jsonAnnotations(a)))
                            .add(!p0.isNamed() ? null : elems.ofPair("@name", elems.ofString(p0.name())))
                            .add(!p0.isParametrized() ? null : elems.ofPair("@params", elems.ofArray(p0.params().stream().map(x -> ensureJson(x)).toArray(NElement[]::new))))
                            .build();
                }
            }
            case OBJECT:
            case NAMED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case PARAMETRIZED_OBJECT: {
                List<NElementAnnotation> a = e.annotations();

                NObjectElement p0 = e.asObject().get();
                NObjectElementBuilder p = elems.ofObjectBuilder()
                        .addAll(p0.children().stream().map(x -> ensureJson(x)).toArray(NElement[]::new));

                if (a.isEmpty() && !p0.isNamed() && !p0.isParametrized()) {
                    return p.build();
                } else {
                    return elems.ofObjectBuilder()
                            .add("value", p.build())
                            .add(a.isEmpty() ? null : elems.ofPair("@annotations", _jsonAnnotations(a)))
                            .add(!p0.isNamed() ? null : elems.ofPair("@name", elems.ofString(p0.name())))
                            .add(!p0.isParametrized() ? null : elems.ofPair("@params", elems.ofArray(p0.params().stream().map(x -> ensureJson(x)).toArray(NElement[]::new))))
                            .build();
                }
            }
            case UPLET:
            case NAMED_UPLET: {
                List<NElementAnnotation> a = e.annotations();

                NUpletElement p0 = e.asUplet().get();
                NArrayElementBuilder p = elems.ofArrayBuilder()
                        .addAll(p0.children().stream().map(x -> ensureJson(x)).toArray(NElement[]::new));

                if (a.isEmpty() && !p0.isNamed() && !p0.isParametrized()) {
                    return p.build();
                } else {
                    return elems.ofObjectBuilder()
                            .add("value", p.build())
                            .add(a.isEmpty() ? null : elems.ofPair("@annotations", _jsonAnnotations(a)))
                            .add(!p0.isNamed() ? null : elems.ofPair("@name", elems.ofString(p0.name())))
                            .build();
                }
            }
            case MATRIX:
            case NAMED_MATRIX:
            case NAMED_PARAMETRIZED_MATRIX:
            case OP:
            default: {
                throw new NUnsupportedOperationException(NMsg.ofC("unsupported ensureJson for %s", e.type()));
            }
        }
    }

    @Override
    public NElement parseElement(Reader reader, NElementFactoryContext context) {
        return new JsonElementParser(context).parseElement(reader);
    }

    @Override
    public void printElement(NElement value, NPrintStream out, boolean compact, NElementFactoryContext context) {
        write(out, value, compact);
    }

    private static class JsonElementParser {

        private BufferedReader reader;
        private NElementFactoryContext context;
        private int fileOffset;
        private int lineNumber;
        private int lineOffset;
        private int current;
        private boolean skipLF;
        private NElements ebuilder;

        public JsonElementParser(NElementFactoryContext context) {
            this.context = context;
        }

        public NElement parseElement(Reader reader) {
            if (reader == null) {
                throw new NullPointerException("reader is null");
            }
            this.reader = (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);
            fileOffset = 0;
            lineNumber = 1;
            lineOffset = 0;
            current = 0;
            readNext();
            skipWhiteSpaceAndComments();
            NElement e = readValue();
            skipWhiteSpaceAndComments();
            if (current != -1) {
                throw error("unexpected character");
            }
            return e;
        }

        private NElement readValue() {
            switch (current) {
                case 'n': {
                    String n = readStringLiteralUnQuoted();
                    if ("null".equals(n)) {
                        return builder().ofNull();
                    }
                    return builder().ofString(n);
                }
                case 't': {
                    String n = readStringLiteralUnQuoted();
                    if ("true".equals(n)) {
                        return builder().ofTrue();
                    }
                    return builder().ofString(n);
                }
                case 'f': {
                    String n = readStringLiteralUnQuoted();
                    if ("false".equals(n)) {
                        return builder().ofFalse();
                    }
                    return builder().ofString(n);
                }
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '.':
                case '-': {
                    return readNumber();
                }
                case '"':
                case '\'':
                case '`': {
                    return readJsonString();
                }
                case '[': {
                    return readJsonArray();
                }
                case '{': {
                    return readJsonObject();
                }
                default: {
                    if (Character.isAlphabetic(current)) {
                        return readJsonString();
                    }
                    throw expected("value");
                }
            }
        }

        private NElement readJsonArray() {
            NArrayElementBuilder array = builder().ofArrayBuilder();
            readNext();
            skipWhiteSpaceAndComments();
            if (readChar(']')) {
                return array.build();
            }
            do {
                skipWhiteSpaceAndComments();
                //this happens with trailing ',]'
                if (current == ']') {
                    break;
                }
                array.add(readValue());
                skipWhiteSpaceAndComments();
            } while (readChar(','));
            skipWhiteSpaceAndComments();
            if (!readChar(']')) {
                throw expected("',' or ']'");
            }
            return array.build();
        }

        private NElement readJsonObject() {
            NSession session = context.getSession();
            NObjectElementBuilder object = builder().ofObjectBuilder();
            readNext();
            skipWhiteSpaceAndComments();
            if (readChar('}')) {
                return object.build();
            }
            do {
                skipWhiteSpaceAndComments();
                //this happens with trailing ',}'
                if (current == '}') {
                    break;
                }
                NElement k = readValue();
                String name;
                switch (k.type()) {
                    case ARRAY:
                    case OBJECT: {
                        throw expected("name");
                    }
                    case NULL: {
                        name = "null";
                        break;
                    }
                    default: {
                        name = k.asString().get();
                    }
                }
                skipWhiteSpaceAndComments();
                if (!readChar(':')) {
                    throw expected("':'");
                }
                skipWhiteSpaceAndComments();
                NElement v = readValue();
                object.set(name, v);
                skipWhiteSpaceAndComments();
            } while (readChar(','));
            if (!readChar('}')) {
                throw expected("',' or '}'");
            }
            return object.build();
        }

        private void readTerminal(String s) {
            final int len = s.length();
            for (int i = 0; i < len; i++) {
                char ch = s.charAt(i);
                if (!readChar(ch)) {
                    throw expected("'" + ch + "'");
                }
            }
        }

        private NElement readJsonString() {
            return builder().ofString(readStringLiteral());
        }

        private String readStringLiteral() {
            if (current == '"') {
                return readStringLiteralDblQuoted();
            }
            if (current == '\'') {
                return readStringLiteralSimpleQuoted();
            }
            if (current == '`') {
                return readStringLiteralAntiQuoted();
            }
            return readStringLiteralUnQuoted();
        }

        private String readStringLiteralDblQuoted() {
            readNext();
            StringBuilder sb = new StringBuilder();
            while (current != '"') {
                if (current == '\\') {
                    readNext();
                    switch (current) {
                        case '\'':
                        case '"':
                        case '/':
                        case '\\':
                            sb.append((char) current);
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'u':
                            char[] hexChars = new char[4];
                            for (int i = 0; i < 4; i++) {
                                readNext();
                                if (!isHexDigit()) {
                                    throw expected("hexadecimal digit");
                                }
                                hexChars[i] = (char) current;
                            }
                            sb.append((char) Integer.parseInt(new String(hexChars), 16));
                            break;
                        default:
                            throw expected("valid escape sequence");
                    }
                    readNext();
                } else if (current < 0x20) {
                    throw expected("valid string character");
                } else {
                    sb.append((char) current);
                    readNext();
                }
            }
            readNext();
            return sb.toString();
        }

        private String readStringLiteralSimpleQuoted() {
            readNext();
            StringBuilder sb = new StringBuilder();
            while (current != '\'') {
                if (current == '\\') {
                    readNext();
                    switch (current) {
                        case '\'':
                        case '"':
                        case '/':
                        case '\\':
                            sb.append((char) current);
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'u':
                            char[] hexChars = new char[4];
                            for (int i = 0; i < 4; i++) {
                                readNext();
                                if (!isHexDigit()) {
                                    throw expected("hexadecimal digit");
                                }
                                hexChars[i] = (char) current;
                            }
                            sb.append((char) Integer.parseInt(new String(hexChars), 16));
                            break;
                        default:
                            throw expected("valid escape sequence");
                    }
                    readNext();
                } else if (current < 0x20) {
                    throw expected("valid string character");
                } else {
                    sb.append((char) current);
                    readNext();
                }
            }
            readNext();
            return sb.toString();
        }

        private String readStringLiteralAntiQuoted() {
            readNext();
            StringBuilder sb = new StringBuilder();
            while (current != '`') {
                if (current == '\\') {
                    readNext();
                    switch (current) {
                        case '\'':
                        case '`':
                        case '"':
                        case '/':
                        case '\\':
                            sb.append((char) current);
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'u':
                            char[] hexChars = new char[4];
                            for (int i = 0; i < 4; i++) {
                                readNext();
                                if (!isHexDigit()) {
                                    throw expected("hexadecimal digit");
                                }
                                hexChars[i] = (char) current;
                            }
                            sb.append((char) Integer.parseInt(new String(hexChars), 16));
                            break;
                        default:
                            throw expected("valid escape sequence");
                    }
                    readNext();
                } else if (current < 0x20) {
                    throw expected("valid string character");
                } else {
                    sb.append((char) current);
                    readNext();
                }
            }
            readNext();
            return sb.toString();
        }

        private String readStringLiteralUnQuotedPar(char end) {
            readNext();
            StringBuilder sb = new StringBuilder();
            while (current != -1 && current != end) {
                sb.append(skipWhiteSpaceAndComments());
                String str = readStringLiteralUnQuoted();
                if(str.isEmpty()){
                    break;
                }
                sb.append(str);
            }
            if (current != -1) {
                readNext();
            }
            return sb.toString();
        }

        private String readStringLiteralUnQuoted() {
            StringBuilder sb = new StringBuilder();
            while (current > 0x20) {
                if (current == '\\') {
                    readNext();
                    switch (current) {
                        case '\'':
                        case '"':
                        case '/':
                        case '\\':
                            sb.append((char) current);
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'u':
                            char[] hexChars = new char[4];
                            for (int i = 0; i < 4; i++) {
                                readNext();
                                if (!isHexDigit()) {
                                    throw expected("hexadecimal digit");
                                }
                                hexChars[i] = (char) current;
                            }
                            sb.append((char) Integer.parseInt(new String(hexChars), 16));
                            break;
                        default:
                            throw expected("valid escape sequence");
                    }
                    readNext();
                } else if (current == '(') {
                    sb.append(readStringLiteralUnQuotedPar(')'));
                } else if (current == '{') {
                    sb.append(readStringLiteralUnQuotedPar('}'));
                } else if (current == '[') {
                    sb.append(readStringLiteralUnQuotedPar(']'));
                } else if (current == '\"' || current == '\'' || current == '`') {
                    sb.append(readStringLiteral());
                } else if (current != ':' && current != ','
                        && current != ')' && current != '}' && current != ']'
                ) {
                    sb.append((char) current);
                    readNext();
                } else {
                    break;
                }
            }
            return sb.toString();
        }

        private NElement readNumber() {
            StringBuilder sb = new StringBuilder();
            boolean inWhile = true;
            while (inWhile) {
                switch (current) {
                    case -1: {
                        throw expected("number");
                    }
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case '+':
                    case '-':
                    case 'e':
                    case 'E':
                    case '.': {
                        sb.append((char) current);
                        readNext();
                        break;
                    }
                    default: {
                        inWhile = false;
                    }
                }
            }
            return builder().ofNumber(sb.toString());
        }

        private boolean readChar(char ch) {
            if (current != ch) {
                return false;
            }
            readNext();
            return true;
        }

        private String skipWhiteSpaceAndComments() {
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (current == ' ' || current == '\t' || current == '\n' || current == '\r') {
                    sb.append((char) current);
                    readNext();
                } else if (current == '/') {
                    String s = foreSeek(2);
                    if ("//".equals(s)) {
                        sb.append((char) current);
                        readNext();
                        sb.append((char) current);
                        readNext();//skip //
                        while (current > 0 && current != '\r' && current != '\n') {
                            sb.append((char) current);
                            readNext();
                        }
                    } else if ("/*".equals(s)) {
                        sb.append((char) current);
                        readNext();
                        sb.append((char) current);
                        readNext();//skip /*
                        while (current > 0) {
                            if (current == '*' && "*/".equals(foreSeek(2))) {
                                sb.append((char) current);
                                readNext();
                                sb.append((char) current);
                                readNext();//skip */
                                break;
                            }
                            sb.append((char) current);
                            readNext();
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
            return sb.toString();
        }

        private String foreSeek(int count) {
            StringBuilder sb = new StringBuilder();
            if (current > 0) {
                sb.append((char) current);
                count--;
            }
            if (count > 0) {
                try {
                    reader.mark(count);
                    for (int i = 0; i < count; i++) {
                        int r = reader.read();
                        if (r >= 0) {
                            sb.append((char) r);
                        } else {
                            break;
                        }
                    }
                    if (sb.length() > 0) {
                        reader.reset();
                    }
                } catch (IOException ex) {
                    throw new NIOException(ex);
                }
            }
            return sb.toString();
        }

        private void readNext() {
            try {
                current = reader.read();
                if (current != -1) {
                    lineOffset++;
                    fileOffset++;
                    if (skipLF) {
                        if (current == '\n') {
                            current = reader.read();
                        }
                        skipLF = false;
                    }
                    switch (current) {
                        case '\r': {
                            skipLF = true;
                        }
                        case '\n': {
                            // Fall through
                            lineNumber++;
                            lineOffset = 0;
                            current = '\n';
                        }
                    }
                }
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        }

        ReaderLocation getLocation() {
            return new ReaderLocation(fileOffset, lineNumber, lineOffset);
        }

        private RuntimeException expected(String expected) {
            if (current == -1) {
                return error("unexpected end of input");
            }
            return error("expected " + expected);
        }

        private RuntimeException error(String message) {
            return new NParseException(NMsg.ofC("%s : %s", message, getLocation().toString()));
        }

        private boolean isHexDigit() {
            return current >= '0' && current <= '9'
                    || current >= 'a' && current <= 'f'
                    || current >= 'A' && current <= 'F';
        }

        public NElements builder() {
            if (ebuilder == null) {
                ebuilder = NElements.of();
            }
            return ebuilder;
        }

    }
}

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
package net.thevpc.nuts.runtime.standalone.format.tson;

import net.thevpc.nuts.NParseException;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.elem.NElementStreamFormat;
import net.thevpc.nuts.runtime.standalone.format.json.ReaderLocation;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.tson.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultTsonElementFormat implements NElementStreamFormat {

    private NWorkspace ws;

    public DefaultTsonElementFormat(NWorkspace ws) {
        this.ws = ws;
    }

    public NElement parseElement(String string, NElementFactoryContext context) {
        if (string == null) {
            throw new NullPointerException("string is null");
        }
        return parseElement(new StringReader(string), context);
    }

    public void write(NPrintStream out, NElement data, boolean compact) {
        TsonWriter w = Tson.writer();
        w.setOptionCompact(compact);
        w.write(out.asPrintStream(), toTson(data));
    }

    public TsonElement toTson(NElement elem) {
        throw new IllegalArgumentException("not implemented");
    }

    private NElementAnnotation toNElemAnn(TsonAnnotation elem) {
        throw new IllegalArgumentException("not implemented");
    }

    private NElement toNElem(TsonElement elem) {
        NElements txt = NElements.of();
        switch (elem.type()) {
            case NULL: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = txt.ofNull();
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case BYTE: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = txt.ofByte(elem.byteValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case SHORT: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = txt.ofShort(elem.shortValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case CHAR: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = txt.ofChar(elem.charValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case INT: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = txt.ofInt(elem.intValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case LONG: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = txt.ofLong(elem.longValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case FLOAT: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = txt.ofFloat(elem.floatValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case DOUBLE: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = txt.ofDouble(elem.doubleValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case BIG_INT: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = txt.ofBigInteger(elem.bigIntegerValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case BIG_DECIMAL: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = txt.ofBigDecimal(elem.bigDecimalValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case STRING: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = txt.ofString(elem.stringValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case BOOLEAN: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = txt.ofBoolean(elem.booleanValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case REGEX: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = txt.ofRegex(elem.stringValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case NAME: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = txt.ofName(elem.stringValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case ARRAY: {
                TsonAnnotation[] annotations = elem.annotations();
                TsonArray array = elem.toArray();
                TsonElementHeader header = array.header();

                NPrimitiveElement u = txt.ofName(elem.stringValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
        }
        throw new IllegalArgumentException("not implemented");
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
            NArrayElementBuilder array = builder().ofArray();
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
            NObjectElementBuilder object = builder().ofObject();
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
                sb.append(readStringLiteralUnQuoted());
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

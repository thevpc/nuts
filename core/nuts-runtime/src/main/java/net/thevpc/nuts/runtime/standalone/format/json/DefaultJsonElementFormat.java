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
package net.thevpc.nuts.runtime.standalone.format.json;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.elem.NutsElementStreamFormat;

import java.io.*;

/**
 * @author thevpc
 */
public class DefaultJsonElementFormat implements NutsElementStreamFormat {

    private NutsWorkspace ws;

    public DefaultJsonElementFormat(NutsWorkspace ws) {
        this.ws = ws;
    }

    public NutsElement parseElement(String string, NutsElementFactoryContext context) {
        if (string == null) {
            throw new NullPointerException("string is null");
        }
        return parseElement(new StringReader(string), context);
    }

    public void write(NutsPrintStream out, NutsElement data, boolean compact) {
        write(out, data, compact ? null : "");
    }

    private void write(NutsPrintStream out, NutsElement data, String indent) {
        switch (data.type()) {
            case NULL: {
                out.print("null");
                break;
            }
            case BOOLEAN: {
                out.print(data.asPrimitive().getBoolean());
                break;
            }
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
            case FLOAT:
            case DOUBLE: {
                out.print(data.asPrimitive().getNumber());
                break;
            }
            case INSTANT:
            case STRING:
//            case NUTS_STRING:
            {
                StringBuilder sb = new StringBuilder("\"");
                final String str = data.asPrimitive().getString();
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
                                sb.append(NutsUtilStrings.toHexChar((c >> 12) & 0xF));
                                sb.append(NutsUtilStrings.toHexChar((c >> 8) & 0xF));
                                sb.append(NutsUtilStrings.toHexChar((c >> 4) & 0xF));
                                sb.append(NutsUtilStrings.toHexChar(c & 0xF));
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
                                    sb.append(NutsUtilStrings.toHexChar((c >> 12) & 0xF));
                                    sb.append(NutsUtilStrings.toHexChar((c >> 8) & 0xF));
                                    sb.append(NutsUtilStrings.toHexChar((c >> 4) & 0xF));
                                    sb.append(NutsUtilStrings.toHexChar(c & 0xF));
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
                if (data.asArray().size() == 0) {
                    out.print("[]");
                } else {
                    out.print('[');
                    boolean first = true;
                    String indent2 = indent + "  ";
                    for (NutsElement e : data.asArray().children()) {
                        if (first) {
                            first = false;
                        } else {
                            out.print(',');
                        }
                        if (indent != null) {
                            out.print('\n');
                            out.print(indent2);
                            write(out, e, indent2);
                        } else {
                            write(out, e, null);
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
                if (data.asObject().size() == 0) {
                    out.print("{}");
                } else {
                    out.print('{');
                    boolean first = true;
                    String indent2 = indent + "  ";
                    for (NutsElementEntry e : data.asObject().children()) {
                        if (first) {
                            first = false;
                        } else {
                            out.print(',');
                        }
                        if (indent != null) {
                            out.print('\n');
                            out.print(indent2);
                            write(out, e.getKey(), indent2);
                            out.print(':');
                            out.print(' ');
                            write(out, e.getValue(), indent2);
                        } else {
                            write(out, e.getKey(), null);
                            out.print(':');
                            write(out, e.getValue(), null);
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
                throw new IllegalArgumentException("unsupported");
            }
        }
    }

    @Override
    public NutsElement parseElement(Reader reader, NutsElementFactoryContext context) {
        return new ElementParser(context).parseElement(reader);
    }

    @Override
    public void printElement(NutsElement value, NutsPrintStream out, boolean compact, NutsElementFactoryContext context) {
        write(out, value, compact);
    }

    private static class ElementParser {

        private BufferedReader reader;
        private NutsElementFactoryContext context;
        private int fileOffset;
        private int lineNumber;
        private int lineOffset;
        private int current;
        private boolean skipLF;
        private NutsElements ebuilder;

        public ElementParser(NutsElementFactoryContext context) {
            this.context = context;
        }

        public NutsElement parseElement(Reader reader) {
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
            NutsElement e = readValue();
            skipWhiteSpaceAndComments();
            if (current != -1) {
                throw error("unexpected character");
            }
            return e;
        }

        private NutsElement readValue() {
            switch (current) {
                case 'n': {
                    String n = readStringLiteralUnQuoted();
                    if ("null".equals(n)) {
                        return builder().forNull();
                    }
                    return builder().forString(n);
                }
                case 't': {
                    String n = readStringLiteralUnQuoted();
                    if ("true".equals(n)) {
                        return builder().forTrue();
                    }
                    return builder().forString(n);
                }
                case 'f': {
                    String n = readStringLiteralUnQuoted();
                    if ("false".equals(n)) {
                        return builder().forFalse();
                    }
                    return builder().forString(n);
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

        private NutsElement readJsonArray() {
            NutsArrayElementBuilder array = builder().forArray();
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

        private NutsElement readJsonObject() {
            NutsObjectElementBuilder object = builder().forObject();
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
                NutsElement k = readValue();
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
                        name = k.asString();
                    }
                }
                skipWhiteSpaceAndComments();
                if (!readChar(':')) {
                    throw expected("':'");
                }
                skipWhiteSpaceAndComments();
                NutsElement v = readValue();
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

        private NutsElement readJsonString() {
            return builder().forString(readStringLiteral());
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
            if(current!=-1){
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

        private NutsElement readNumber() {
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
            return builder().forNumber(sb.toString());
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
                    throw new NutsIOException(context.getSession(),ex);
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
                throw new NutsIOException(context.getSession(),ex);
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
            return new RuntimeException(message + ":" + getLocation().toString());
        }

        private boolean isHexDigit() {
            return current >= '0' && current <= '9'
                    || current >= 'a' && current <= 'f'
                    || current >= 'A' && current <= 'F';
        }

        public NutsElements builder() {
            if (ebuilder == null) {
                ebuilder = NutsElements.of(context.getSession());
            }
            return ebuilder;
        }

    }
}

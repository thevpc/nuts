/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.core.format.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import net.thevpc.nuts.NutsArrayElementBuilder;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsObjectElementBuilder;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.elem.NutsElementStreamFormat;
import net.thevpc.nuts.NutsElementEntry;
import net.thevpc.nuts.NutsElementFormat;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

/**
 *
 * @author vpc
 */
public class SimpleJson implements NutsElementStreamFormat {

    private NutsWorkspace ws;

    public SimpleJson(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public void printElement(NutsElement value, PrintStream out, boolean compact, NutsElementFactoryContext context) {
        write(out, value, compact);
    }

    public NutsElement parseElement(String string, NutsElementFactoryContext context) {
        if (string == null) {
            throw new NullPointerException("string is null");
        }
        return parseElement(new StringReader(string), context);
    }

    public void write(PrintStream out, NutsElement data, boolean compact) {
        write(out, data, compact ? null : "");
    }

    private void write(PrintStream out, NutsElement data, String indent) {
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
                                sb.append(CoreIOUtils.toHex((c >> 12) & 0xF));
                                sb.append(CoreIOUtils.toHex((c >> 8) & 0xF));
                                sb.append(CoreIOUtils.toHex((c >> 4) & 0xF));
                                sb.append(CoreIOUtils.toHex(c & 0xF));
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
                                    sb.append(CoreIOUtils.toHex((c >> 12) & 0xF));
                                    sb.append(CoreIOUtils.toHex((c >> 8) & 0xF));
                                    sb.append(CoreIOUtils.toHex((c >> 4) & 0xF));
                                    sb.append(CoreIOUtils.toHex(c & 0xF));
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

    private static class ElementParser {

        private BufferedReader reader;
        private NutsElementFactoryContext context;
        private int fileOffset;
        private int lineNumber;
        private int lineOffset;
        private int current;
        private boolean skipLF;
        private NutsElementFormat ebuilder;

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
            skipWhiteSpace();
            NutsElement e = readValue();
            skipWhiteSpace();
            if (current != -1) {
                throw error("unexpected character");
            }
            return e;
        }

        private NutsElement readValue() {
            switch (current) {
                case 'n': {
                    readTerminal("null");
                    return builder().forNull();
                }
                case 't': {
                    readTerminal("true");
                    return builder().forTrue();
                }
                case 'f': {
                    readTerminal("false");
                    return builder().forFalse();
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
                case '"': {
                    return readJsonString();
                }
                case '[': {
                    return readJsonArray();
                }
                case '{': {
                    return readJsonObject();
                }
                default:
                    throw expected("value");
            }
        }

        private NutsElement readJsonArray() {
            NutsArrayElementBuilder array = builder().forArray();
            readNext();
            skipWhiteSpace();
            if (readChar(']')) {
                return array.build();
            }
            do {
                skipWhiteSpace();
                array.add(readValue());
                skipWhiteSpace();
            } while (readChar(','));
            if (!readChar(']')) {
                throw expected("',' or ']'");
            }
            return array.build();
        }

        private NutsElement readJsonObject() {
            NutsObjectElementBuilder object = builder().forObject();
            readNext();
            skipWhiteSpace();
            if (readChar('}')) {
                return object.build();
            }
            do {
                skipWhiteSpace();
                if (current != '"') {
                    throw expected("name");
                }
                String name = readStringLiteral();
                skipWhiteSpace();
                if (!readChar(':')) {
                    throw expected("':'");
                }
                skipWhiteSpace();
                NutsElement v = readValue();
                object.set(name, v);
                skipWhiteSpace();
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
            readNext();
            StringBuilder sb = new StringBuilder();
            while (current != '"') {
                if (current == '\\') {
                    readNext();
                    switch (current) {
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

        private void skipWhiteSpace() {
            while (current == ' ' || current == '\t' || current == '\n' || current == '\r') {
                readNext();
            }
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
                throw new UncheckedIOException(ex);
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

        public NutsElementFormat builder() {
            if (ebuilder == null) {
                ebuilder = context.getSession().getWorkspace().elem();
            }
            return ebuilder;
        }

    }
}

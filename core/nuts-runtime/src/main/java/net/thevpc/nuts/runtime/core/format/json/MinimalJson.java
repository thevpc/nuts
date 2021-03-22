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

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import net.thevpc.nuts.NutsArrayElementBuilder;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementBuilder;
import net.thevpc.nuts.NutsObjectElementBuilder;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.elem.NutsElementStreamFormat;
import net.thevpc.nuts.NutsElementEntry;

/**
 *
 * @author vpc
 */
public class MinimalJson implements NutsElementStreamFormat {

    private static final int MIN_BUFFER_SIZE = 10;
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private Reader reader;
    private char[] buffer;
    private int bufferOffset;
    private int index;
    private int fill;
    private int line;
    private int lineOffset;
    private int current;
    private StringBuilder captureBuffer;
    private int captureStart;
    private NutsWorkspace ws;
    private NutsElementBuilder ebuilder;

    public MinimalJson(NutsWorkspace ws) {
        this.ws = ws;
    }

    public NutsElementBuilder builder() {
        if (ebuilder == null) {
            ebuilder = ws.formats().element().elements();
        }
        return ebuilder;
    }

    @Override
    public NutsElement parseElement(Reader reader, NutsSession session) {
        return parse(reader);
    }

    @Override
    public void printElement(NutsElement value, PrintStream out, boolean compact, NutsSession session) {
        write(out, value, compact);
    }

    /**
     * Parses the given input string. The input must contain a valid JSON value,
     * optionally padded with white-spaces.
     *
     * @param string the input string, must be valid JSON
     */
    public NutsElement parse(String string) {
        if (string == null) {
            throw new NullPointerException("string is null");
        }
        int bufferSize = Math.max(MIN_BUFFER_SIZE, Math.min(DEFAULT_BUFFER_SIZE, string.length()));
        return parse(new StringReader(string), bufferSize);
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
            case STRING: {
                StringBuilder sb = new StringBuilder("\"");
                char[] chars = data.asPrimitive().getString().toCharArray();
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
                            default: {
                                sb.append(c);
                            }
                        }
                    } else {
                        switch (c) {
                            case '\\': {
                                sb.append(c).append(c);
                                break;
                            }
                            default: {
                                sb.append(c);
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

    public NutsElement parse(Reader reader) {
        return parse(reader, DEFAULT_BUFFER_SIZE);
    }

    public NutsElement parse(Reader reader, int buffersize) {
        if (reader == null) {
            throw new NullPointerException("reader is null");
        }
        if (buffersize <= 0) {
            throw new IllegalArgumentException("buffersize is zero or negative");
        }
        this.reader = reader;
        buffer = new char[buffersize];
        bufferOffset = 0;
        index = 0;
        fill = 0;
        line = 1;
        lineOffset = 0;
        current = 0;
        captureStart = -1;
        read();
        skipWhiteSpace();
        NutsElement e = readValue();
        skipWhiteSpace();
        if (!isEndOfText()) {
            throw error("Unexpected character");
        }
        return e;
    }

    private NutsElement readValue() {
        switch (current) {
            case 'n': {
                read();
                readRequiredChar('u');
                readRequiredChar('l');
                readRequiredChar('l');
                return builder().forNull();
            }
            case 't': {
                read();
                readRequiredChar('r');
                readRequiredChar('u');
                readRequiredChar('e');
                return builder().forBoolean(true);
            }
            case 'f': {
                read();
                readRequiredChar('a');
                readRequiredChar('l');
                readRequiredChar('s');
                readRequiredChar('e');
                return builder().forBoolean(false);
            }
            case '"':
                return readString();
            case '[':
                return readArray();
            case '{':
                return readObject();
            case '-':
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
                return readNumber();
            default:
                throw expected("value");
        }
    }

    private NutsElement readArray() {
        NutsArrayElementBuilder array = builder().forArray();
        read();
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

    private NutsElement readObject() {
        NutsObjectElementBuilder object = builder().forObject();
        read();
        skipWhiteSpace();
        if (readChar('}')) {
            return object.build();
        }
        do {
            skipWhiteSpace();
            String name = readName();
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

    private String readName() {
        if (current != '"') {
            throw expected("name");
        }
        return readStringInternal();
    }

    private void readRequiredChar(char ch) {
        if (!readChar(ch)) {
            throw expected("'" + ch + "'");
        }
    }

    private NutsElement readString() {
        return builder().forString(readStringInternal());
    }

    private String readStringInternal() {
        read();
        startCapture();
        while (current != '"') {
            if (current == '\\') {
                pauseCapture();
                readEscape();
                startCapture();
            } else if (current < 0x20) {
                throw expected("valid string character");
            } else {
                read();
            }
        }
        String string = endCapture();
        read();
        return string;
    }

    private void readEscape() {
        read();
        switch (current) {
            case '"':
            case '/':
            case '\\':
                captureBuffer.append((char) current);
                break;
            case 'b':
                captureBuffer.append('\b');
                break;
            case 'f':
                captureBuffer.append('\f');
                break;
            case 'n':
                captureBuffer.append('\n');
                break;
            case 'r':
                captureBuffer.append('\r');
                break;
            case 't':
                captureBuffer.append('\t');
                break;
            case 'u':
                char[] hexChars = new char[4];
                for (int i = 0; i < 4; i++) {
                    read();
                    if (!isHexDigit()) {
                        throw expected("hexadecimal digit");
                    }
                    hexChars[i] = (char) current;
                }
                captureBuffer.append((char) Integer.parseInt(new String(hexChars), 16));
                break;
            default:
                throw expected("valid escape sequence");
        }
        read();
    }

    private NutsElement readNumber() {
        startCapture();
        readChar('-');
        int firstDigit = current;
        if (!readDigit()) {
            throw expected("digit");
        }
        if (firstDigit != '0') {
            while (readDigit()) {
            }
        }
        readFraction();
        readExponent();
        return builder().forNumber(endCapture());
    }

    private boolean readFraction() {
        if (!readChar('.')) {
            return false;
        }
        if (!readDigit()) {
            throw expected("digit");
        }
        while (readDigit()) {
        }
        return true;
    }

    private boolean readExponent() {
        if (!readChar('e') && !readChar('E')) {
            return false;
        }
        if (!readChar('+')) {
            readChar('-');
        }
        if (!readDigit()) {
            throw expected("digit");
        }
        while (readDigit()) {
        }
        return true;
    }

    private boolean readChar(char ch) {
        if (current != ch) {
            return false;
        }
        read();
        return true;
    }

    private boolean readDigit() {
        if (!isDigit()) {
            return false;
        }
        read();
        return true;
    }

    private void skipWhiteSpace() {
        while (isWhiteSpace()) {
            read();
        }
    }

    private void read() {
        if (index == fill) {
            try {
                if (captureStart != -1) {
                    captureBuffer.append(buffer, captureStart, fill - captureStart);
                    captureStart = 0;
                }
                bufferOffset += fill;
                fill = reader.read(buffer, 0, buffer.length);
                index = 0;
                if (fill == -1) {
                    current = -1;
                    index++;
                    return;
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        if (current == '\n') {
            line++;
            lineOffset = bufferOffset + index;
        }
        current = buffer[index++];
    }

    private void startCapture() {
        if (captureBuffer == null) {
            captureBuffer = new StringBuilder();
        }
        captureStart = index - 1;
    }

    private void pauseCapture() {
        int end = current == -1 ? index : index - 1;
        captureBuffer.append(buffer, captureStart, end - captureStart);
        captureStart = -1;
    }

    private String endCapture() {
        int start = captureStart;
        int end = index - 1;
        captureStart = -1;
        if (captureBuffer.length() > 0) {
            captureBuffer.append(buffer, start, end - start);
            String captured = captureBuffer.toString();
            captureBuffer.setLength(0);
            return captured;
        }
        return new String(buffer, start, end - start);
    }

    ReaderLocation getLocation() {
        int offset = bufferOffset + index - 1;
        int column = offset - lineOffset + 1;
        return new ReaderLocation(offset, line, column);
    }

    private RuntimeException expected(String expected) {
        if (isEndOfText()) {
            return error("Unexpected end of input");
        }
        return error("Expected " + expected);
    }

    private RuntimeException error(String message) {
        return new RuntimeException(message + ":" + getLocation().toString());
    }

    private boolean isWhiteSpace() {
        return current == ' ' || current == '\t' || current == '\n' || current == '\r';
    }

    private boolean isDigit() {
        return current >= '0' && current <= '9';
    }

    private boolean isHexDigit() {
        return current >= '0' && current <= '9'
                || current >= 'a' && current <= 'f'
                || current >= 'A' && current <= 'F';
    }

    private boolean isEndOfText() {
        return current == -1;
    }

    public static class ReaderLocation {

        int offset;
        int line;
        int column;

        public ReaderLocation(int offset, int line, int column) {
            this.offset = offset;
            this.line = line;
            this.column = column;
        }

        public int getOffset() {
            return offset;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return "ReaderLocation{" + "offset=" + offset + ", line=" + line + ", column=" + column + '}';
        }

    }
}

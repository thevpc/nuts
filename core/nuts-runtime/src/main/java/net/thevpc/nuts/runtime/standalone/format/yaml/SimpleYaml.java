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
package net.thevpc.nuts.runtime.standalone.format.yaml;

/**
 *
 * @author thevpc
 */

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.elem.NElementStreamFormat;
import net.thevpc.nuts.runtime.standalone.format.json.ReaderLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class SimpleYaml implements NElementStreamFormat {

    private final NWorkspace ws;

    public SimpleYaml(NWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NElement parseElement(Reader reader, NElementFactoryContext context) {
        return new ElementParser(context).parseElement(reader);
    }

    @Override
    public void printElement(NElement value, NPrintStream out, boolean compact, NElementFactoryContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private enum NodeType {
        LITERAL,
        ARRAY_ELEMENT,
        OBJECT_ELEMENT,
    }

    private static class Node {

        NodeType type;
        Object value;

        private Node(NodeType type, Object value) {
            this.type = type;
            this.value = value;
        }

        private static Node forLiteral(NElement value) {
            return new Node(NodeType.LITERAL, value);
        }

        private static Node forArrayElement(NElement value) {
            return new Node(NodeType.ARRAY_ELEMENT, value);
        }

        private static Node forObjectElement(NPairElement value) {
            return new Node(NodeType.OBJECT_ELEMENT, value);
        }

        public NodeType getType() {
            return type;
        }

        public NElement getElement() {
            return (NElement) value;
        }

        public NPairElement getEntry() {
            return (NPairElement) value;
        }

        public Object getValue() {
            return value;
        }

    }

    private static class ElementParser {

        private BufferedReader reader;
        private final NElementFactoryContext context;
        private NElements ebuilder;
        private int fileOffset;
        private int lineNumber;
        private int lineOffset;
        private int current;
        private boolean skipLF;

        public ElementParser(NElementFactoryContext context) {
            this.context = context;
        }

        public NElement parseElement(Reader reader) {
            this.reader = (reader instanceof BufferedReader) ? this.reader : new BufferedReader(reader);
            readNext();
            return readElement(0);
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

        private int readNext() {
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
            return current;
        }

        private RuntimeException expected(String expected) {
            if (current == -1) {
                return error("unexpected end of input");
            }
            return error("expected " + expected);
        }

        private RuntimeException error(String message) {
            return new RuntimeException(message + ":" + getLocation());
        }

        private boolean isHexDigit() {
            return current >= '0' && current <= '9'
                    || current >= 'a' && current <= 'f'
                    || current >= 'A' && current <= 'F';
        }

        ReaderLocation getLocation() {
            return new ReaderLocation(fileOffset, lineNumber, lineOffset);
        }

        private boolean isNumberChar(char c) {
            return (c >= '0' && c <= '9') || c == '.' || c == '+' || c == '-' || c == 'e' || c == 'E';
        }

        private void readerMark() {
            try {
                reader.mark(1024);
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        }

        private void readerReset() {
            try {
                reader.reset();
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        }

        public NElements builder() {
            if (ebuilder == null) {
                ebuilder = NElements.of();
            }
            return ebuilder;
        }

        private boolean readChar(char ch) {
            if (current != ch) {
                return false;
            }
            readNext();
            return true;
        }

        private int computeIndent() {
            int indent = 0;
            while (true) {
                if (current == ' ' || current == '\t') {
                    indent++;
                    readNext();
                } else {
                    break;
                }
            }
            return indent;
        }

        private boolean readNewLine() {
            boolean someNL = false;
            while (true) {
                if (current == '\n' || current == ';') {
                    readNext();
                    someNL = true;
                } else {
                    break;
                }
            }
            return someNL;
        }

        private String readComments() {
            if (current == '#') {
                StringBuilder c = new StringBuilder();
                readNext();
                while (current != -1 && current != '\n') {
                    c.append((char) current);
                    readNext();
                }
                while (true) {
                    if (current == '#') {
                        c.append("\n");
                        readNext();
                        while (current != -1 && current != '\n') {
                            c.append((char) current);
                            readNext();
                        }
                    } else {
                        break;
                    }
                }
                return c.toString();
            }
            return null;
        }

        private void skipWhiteSpace() {
            while (current == ' ' || current == '\t') {
                readNext();
            }
        }

        private int peekIndent() {
            int c = current;
            readerMark();
            int indent = computeIndent();
            readerReset();
            current = c;
            return indent;
        }

        private NElement readLiteral(boolean asValue) {
            if (current == '"') {
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

                skipWhiteSpace();
                String postComment = readComments();
                skipWhiteSpace();

                return builder().ofString(sb.toString());
            } else {
                StringBuilder sb = new StringBuilder();
                boolean str = false;
                boolean dot = false;
                boolean E = false;
                while (current != -1 && current != '\n') {
                    if (current == '#' || !asValue && current == ':') {
                        break;
                    }
                    final char c = (char) current;
                    sb.append(c);
                    if (!str) {
                        if (!isNumberChar(c)) {
                            str = true;
                        } else if (c == '.') {
                            dot = true;
                        } else if (c == 'e' || c == 'E') {
                            E = true;
                        }
                    }
                    readNext();
                }
                skipWhiteSpace();
                String postComment = readComments();
                skipWhiteSpace();
                String trimmed = sb.toString().trim();
                if (!str && sb.length() > 0) {
                    return builder().ofNumber(trimmed);
//                    if (dot || E) {
//                        try {
//                            double d = Double.parseDouble(trimmed);
//                            return builder().forPrimitive().buildDouble(d);
//                        } catch (Exception e) {
//                            //any error
//                        }
//                    } else {
//                        try {
//                            int d = Integer.parseInt(trimmed);
//                            return builder().forInt(d);
//                        } catch (Exception e) {
//                            long d = Integer.parseInt(trimmed);
//                            return builder().forLong(d);
//                        }
//                    }
                }
                if (sb.length() == 0) {
                    return null;
                }
                switch (trimmed) {
                    case "true":
                        return builder().ofTrue();
                    case "false":
                        return builder().ofFalse();
                }
                return builder().ofString(trimmed);
            }
        }

        public NElement readElement(int indent) {
            Node a = readNode(indent, false);
            switch (a.type) {
                case LITERAL:
                    return a.getElement();
                case OBJECT_ELEMENT: {
                    NObjectElementBuilder obj = builder().ofObjectBuilder().add(a.getEntry());
                    while (true) {
                        readNewLine();
                        int newIndent = peekIndent();
                        if (newIndent < indent) {
                            break;
                        }
                        Node o = readNode(indent, false);
                        if (o == null) {
                            break;
                        } else {
                            if (o.type == NodeType.OBJECT_ELEMENT) {
                                obj.add(o.getEntry());
                            } else {
                                throw expected("object entry");
                            }
                        }
                    }
                    return obj.build();
                }
                case ARRAY_ELEMENT: {
                    NArrayElementBuilder arr = builder().ofArrayBuilder().add(a.getElement());
                    while (true) {
                        readNewLine();
                        int newIndent = peekIndent();
                        if (newIndent < indent) {
                            break;
                        }
                        Node o = readNode(indent, false);
                        if (o == null) {
                            break;
                        } else {
                            if (o.type == NodeType.ARRAY_ELEMENT) {
                                arr.add(o.getElement());
                            } else {
                                throw expected("array entry");
                            }
                        }
                    }
                    return arr.build();
                }

            }
            final Object r = readNode(indent, true);
            return (NElement) r;
        }

        public Node readNode(int indent, boolean asValue) {
            skipWhiteSpace();
            String preComment = readComments();
            skipWhiteSpace();
            if (current == -1) {
                return null;
            }
            char c = (char) current;
            switch (c) {
                case '-': {
                    if (asValue) {
                        NElement li = readLiteral(true);
                        return Node.forLiteral(li);
                    }
                    readerMark();
                    readNext();
                    if (current == ' ' || current == '\t') {
                        readNext();
                        NElement li = readNode(indent, true).getElement();
                        return Node.forArrayElement(li);
                    } else if (current == '\n' || current == ';') {
                        readNewLine();
                        int newIndent = peekIndent();
                        if (newIndent > indent) {
                            skipWhiteSpace();
                            NElement li = readElement(newIndent);
                            return Node.forArrayElement(li);
                        } else {
                            return Node.forArrayElement(builder().ofString(""));
                        }
                    } else {
                        readerReset();
                        readNext();
                        NElement li = readLiteral(true);
                        return Node.forLiteral(li);
                    }
                }
                case '{': {
                    throw new IllegalArgumentException("not supported yet");
                }
                case '[': {
                    throw new IllegalArgumentException("not supported yet");
                }
                case '?': {
                    if (asValue) {
                        NElement li = readLiteral(true);
                        return Node.forLiteral(li);
                    }
                    readerMark();
                    readNext();
                    if (current == ' ' || current == '\t') {
                        readNext();
                        NElement li = readNode(indent, true).getElement();
                        skipWhiteSpace();
                        readNewLine();
                        skipWhiteSpace();
                        readChar(':');
                        skipWhiteSpace();
                        readNewLine();
                        skipWhiteSpace();
                        NElement v = readNode(indent, true).getElement();
                        return Node.forObjectElement(builder().ofPair(li, v));
                    } else if (current == '\n' || current == ';') {
                        readNewLine();
                        int newIndent = peekIndent();
                        if (newIndent > indent) {
                            skipWhiteSpace();
                            NElement li = readNode(newIndent, true).getElement();
                            skipWhiteSpace();
                            readNewLine();
                            skipWhiteSpace();
                            readChar(':');
                            skipWhiteSpace();
                            readNewLine();
                            skipWhiteSpace();
                            NElement v = readNode(newIndent, true).getElement();
                            return Node.forObjectElement(builder().ofPair(li, v));
                        } else {
                            NElement li = readNode(indent, true).getElement();
                            skipWhiteSpace();
                            readNewLine();
                            skipWhiteSpace();
                            readChar(':');
                            skipWhiteSpace();
                            readNewLine();
                            skipWhiteSpace();
                            NElement v = readNode(indent, true).getElement();
                            return Node.forObjectElement(builder().ofPair(li, v));
                        }
                    } else {
                        readerReset();
                        readNext();
                        NElement li = readLiteral(true);
                        return Node.forLiteral(li);
                    }
                }
                default: {
                    NElement li = readLiteral(false);
                    if (asValue) {
                        return Node.forLiteral(li);
                    }
                    skipWhiteSpace();
                    if (current == ':') {
                        readNext();
                        if (readNewLine()) {
                            int newIndent = peekIndent();
                            if (newIndent > indent) {
                                skipWhiteSpace();
                                NElement v = readElement(newIndent);
                                return Node.forObjectElement(builder().ofPair(li, v));
                            } else {
                                return Node.forObjectElement(builder().ofPair(li, builder().ofString("")));
                            }
                        } else {
                            Node n = readNode(indent, true);
                            NElement v = n.getElement();
                            return Node.forObjectElement(builder().ofPair(li, v));
                        }
                    }
                    return Node.forLiteral(li);
                }
            }
        }
    }
}

package net.thevpc.nuts.runtime.standalone.format.yaml;

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.elem.NArrayElementBuilder;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NObjectElementBuilder;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.format.json.ReaderLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Stack;

class YamlParser {
    public static void main(String[] args) {
        Nuts.require();
        YamlParser e = new YamlParser();
        NElement nElement = e.parseElement(new BufferedReader(new StringReader("\n" +
                "id: changelog070\n" +
                "title: Version 0.7.2.0 released\n" +
                "sub_title: Publishing 0.7.2.0 version\n" +
                "author: thevpc\n" +
                "author_title: Criticize the world Casually...\n" +
                "author_url: https://github.com/thevpc\n" +
                "author_image_url: https://avatars3.githubusercontent.com/u/10106809?s=460&u=28d1736bdf0b6e6f81981b3a2ebbd2db369b25c8&v=4\n" +
                "tags: [nuts]\n" +
                "publish_date: 2020-09-23")));
        System.out.println(nElement);
    }

    private BufferedReader reader;
    private int fileOffset;
    private int lineNumber;
    private int lineOffset;
    private int current;
    private boolean skipLF;
    private Stack<Integer> indentStack = new Stack<>();

    public NElement parseElement(Reader reader) {
        this.reader = (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);
        this.fileOffset = 0;
        this.lineNumber = 1;
        this.lineOffset = 0;
        readNext();
        skipEmptyLinesAndComments();
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
                fileOffset++;
                if (skipLF) {
                    if (current == '\n') {
                        current = reader.read();
                        if (current != -1) {
                            fileOffset++;
                        }
                    }
                    skipLF = false;
                }
                if (current == '\r') {
                    skipLF = true;
                    lineNumber++;
                    lineOffset = 0;
                    current = '\n';
                } else if (current == '\n') {
                    lineNumber++;
                    lineOffset = 0;
                } else {
                    lineOffset++;
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

    private boolean readChar(char ch) {
        if (current != ch) {
            return false;
        }
        readNext();
        return true;
    }

    private int consumeIndent() {
        int indent = 0;
        while (true) {
            if (current == ' ') {
                indent++;
                readNext();
            } else if (current == '\t') {
                indent += 2; // Tabs are typically treated as 2 spaces in YAML
                readNext();
            } else {
                break;
            }
        }
        return indent;
    }

    private int peekIndent() {
        readerMark();
        int indent = consumeIndent();
        readerReset();
        return indent;
    }

    private boolean readNewLine() {
        boolean someNL = false;
        while (current == '\n' || current == ';') {
            readNext();
            someNL = true;
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
            return c.toString();
        }
        return null;
    }

    private void skipWhiteSpace() {
        while (current == ' ' || current == '\t') {
            readNext();
        }
    }

    private void skipEmptyLinesAndComments() {
        while (true) {
            if (current == '\n') {
                readNext();
                continue;
            }
            if (current == '#') {
                readComments();
                continue;
            }
            if (current == ' ' || current == '\t') {
                skipWhiteSpace();
                continue;
            }
            break;
        }
    }

    private NElement readLiteral(boolean asValue) {
        if (current == '"') {
            readNext();
            StringBuilder sb = new StringBuilder();
            while (current != '"') {
                if (current == -1) {
                    throw expected("'\"'");
                }
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
            return NElement.ofString(sb.toString());
        } else if (current == '\'') {
            readNext();
            StringBuilder sb = new StringBuilder();
            while (current != '\'') {
                if (current == -1) {
                    throw expected("\"'\"");
                }
                if (current == '\'' && readNext() == '\'') {
                    // Handle escaped single quote
                    sb.append('\'');
                    readNext();
                } else if (current < 0x20) {
                    throw expected("valid string character");
                } else {
                    sb.append((char) current);
                    readNext();
                }
            }
            readNext();
            return NElement.ofString(sb.toString());
        } else {
            StringBuilder sb = new StringBuilder();
            boolean str = false;
            boolean dot = false;
            boolean E = false;

            while (current != -1 && current != '\n' && current != '\r') {
                if (current == '#') {
                    break; // Comment
                }
                if (!asValue && current == ':') {
                    break; // Key-value separator
                }
                if (!asValue && current == '-' && sb.length() == 0) {
                    break; // This might be an array element
                }
                // For flow-style arrays and objects, stop at delimiters
                if (asValue && (current == ',' || current == ']' || current == '}')) {
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

            String trimmed = sb.toString().trim();
            if (trimmed.isEmpty()) {
                return NElement.ofString("");
            }

            if (!str && trimmed.length() > 0) {
                try {
                    if (dot || E) {
                        return NElement.ofNumber(Double.parseDouble(trimmed));
                    } else {
                        return NElement.ofNumber(Long.parseLong(trimmed));
                    }
                } catch (NumberFormatException e) {
                    // Fall through to string handling
                }
            }

            switch (trimmed) {
                case "true":
                    return NElement.ofTrue();
                case "false":
                    return NElement.ofFalse();
                case "null":
                    return NElement.ofNull();
                case "~":
                    return NElement.ofNull();
            }
            return NElement.ofString(trimmed);
        }
    }

    public NElement readElement(int expectedIndent) {
        if (current == -1) {
            return NElement.ofNull();
        }

        // Check if we have content at the current position
        int currentIndent = peekIndent();
        if (currentIndent < expectedIndent) {
            return NElement.ofNull();
        }

        YamlNode firstNode = readNode(expectedIndent, false);
        if (firstNode == null) {
            return NElement.ofNull();
        }

        switch (firstNode.type) {
            case LITERAL:
                return firstNode.getElement();
            case OBJECT_ELEMENT: {
                NObjectElementBuilder obj = NElement.ofObjectBuilder();
                obj.add(firstNode.getEntry());

                while (true) {
                    skipEmptyLinesAndComments();
                    if (current == -1) {
                        break;
                    }

                    int newIndent = peekIndent();
                    if (newIndent < expectedIndent) {
                        break;
                    }
                    if (newIndent > expectedIndent) {
                        break; // This would be nested content
                    }
                    if (newIndent == expectedIndent) {
                        YamlNode nextNode = readNode(expectedIndent, false);
                        if (nextNode == null) {
                            break;
                        }

                        if (nextNode.type == NodeType.OBJECT_ELEMENT) {
                            obj.add(nextNode.getEntry());
                        } else if (nextNode.type == NodeType.LITERAL) {
                            // Treat as key with empty value
                            String key = nextNode.getElement().asStringValue().get();
                            obj.set(key, NElement.ofString(""));
                        } else {
                            throw expected("object entry");
                        }
                    } else {
                        // If we get here, something's wrong with indentation logic
                        break;
                    }
                }
                return obj.build();
            }
            case ARRAY_ELEMENT: {
                NArrayElementBuilder arr = NElement.ofArrayBuilder();
                arr.add(firstNode.getElement());

                while (true) {
                    skipEmptyLinesAndComments();
                    if (current == -1) {
                        break;
                    }

                    int newIndent = peekIndent();
                    if (newIndent < expectedIndent) {
                        break;
                    }
                    if (newIndent > expectedIndent) {
                        break; // Nested content
                    }
                    if (newIndent == expectedIndent) {
                        YamlNode nextNode = readNode(expectedIndent, false);
                        if (nextNode == null || nextNode.type != NodeType.ARRAY_ELEMENT) {
                            break;
                        }
                        arr.add(nextNode.getElement());
                    } else {
                        break;
                    }
                }
                return arr.build();
            }
            default:
                throw new IllegalStateException("Unexpected node type: " + firstNode.type);
        }
    }

    public YamlNode readNode(int expectedIndent, boolean asValue) {
        if (!asValue) {
            skipEmptyLinesAndComments();
            if (current == -1) {
                return null;
            }

            int actualIndent = peekIndent(); // Just peek, don't consume yet
            if (actualIndent < expectedIndent) {
                return null; // Not enough indentation
            }

            consumeIndent(); // Now consume the indentation
        }

        skipWhiteSpace();
        readComments();
        skipWhiteSpace();

        if (current == -1) {
            return null;
        }

        char c = (char) current;
        switch (c) {
            case '-': {
                if (asValue) {
                    NElement li = readLiteral(true);
                    return YamlNode.forLiteral(li);
                }
                readNext();
                skipWhiteSpace();
                if (current == '\n' || current == -1) {
                    return YamlNode.forArrayElement(NElement.ofString(""));
                } else {
                    YamlNode elementNode = readNode(expectedIndent, true);
                    NElement element = (elementNode != null) ? elementNode.getElement() : NElement.ofString("");
                    return YamlNode.forArrayElement(element);
                }
            }
            case '{': {
                // Flow style object
                readNext();
                skipWhiteSpace();
                NObjectElementBuilder obj = NElement.ofObjectBuilder();

                if (current == '}') {
                    // Empty object
                    readNext();
                    return YamlNode.forLiteral(obj.build());
                }

                while (current != '}' && current != -1) {
                    skipWhiteSpace();
                    YamlNode keyNode = readNode(expectedIndent, true);
                    if (keyNode == null) break;

                    NElement key = keyNode.getElement();
                    skipWhiteSpace();
                    if (!readChar(':')) {
                        throw expected("':'");
                    }
                    skipWhiteSpace();
                    YamlNode valueNode = readNode(expectedIndent, true);
                    NElement value = (valueNode != null) ? valueNode.getElement() : NElement.ofNull();

                    obj.set(key.asStringValue().get(), value);
                    skipWhiteSpace();
                    if (current == ',') {
                        readNext();
                        skipWhiteSpace();
                    } else if (current != '}') {
                        break;
                    }
                }
                if (!readChar('}')) {
                    throw expected("'}'");
                }
                return YamlNode.forLiteral(obj.build());
            }
            case '[': {
                // Flow style array
                readNext();
                skipWhiteSpace();
                NArrayElementBuilder arr = NElement.ofArrayBuilder();

                if (current == ']') {
                    // Empty array
                    readNext();
                    return YamlNode.forLiteral(arr.build());
                }

                while (current != ']' && current != -1) {
                    skipWhiteSpace();

                    // Skip newlines in flow style
                    if (current == '\n') {
                        readNext();
                        skipWhiteSpace();
                        continue;
                    }

                    if (current == ']') {
                        break; // End of array
                    }

                    // Read element as literal for flow-style arrays
                    NElement element = readLiteral(true);
                    arr.add(element);
                    skipWhiteSpace();

                    // Skip newlines after element
                    while (current == '\n') {
                        readNext();
                        skipWhiteSpace();
                    }

                    if (current == ',') {
                        readNext();
                        skipWhiteSpace();
                        // Skip newlines after comma
                        while (current == '\n') {
                            readNext();
                            skipWhiteSpace();
                        }
                    } else if (current == ']') {
                        // We're at the end, let the loop condition handle it
                        break;
                    } else if (current == -1) {
                        throw expected("']'");
                    }
                }

                if (current != ']') {
                    throw expected("']'");
                }
                readNext(); // consume the ']'
                return YamlNode.forLiteral(arr.build());
            }
            case '?': {
                if (asValue) {
                    NElement li = readLiteral(true);
                    return YamlNode.forLiteral(li);
                }
                readNext();
                skipWhiteSpace();
                YamlNode keyNode = readNode(expectedIndent, true);
                NElement key = (keyNode != null) ? keyNode.getElement() : NElement.ofString("");
                skipWhiteSpace();
                if (!readChar(':')) {
                    throw expected("':'");
                }
                skipWhiteSpace();
                YamlNode valueNode = readNode(expectedIndent, true);
                NElement value = (valueNode != null) ? valueNode.getElement() : NElement.ofString("");
                return YamlNode.forObjectElement(NElement.ofPair(key, value));
            }
            default: {
                NElement key = readLiteral(false);
                if (asValue) {
                    return YamlNode.forLiteral(key);
                }
                skipWhiteSpace();
                if (current == ':') {
                    readNext();
                    skipWhiteSpace();
                    if (current == '\n' || current == -1) {
                        return YamlNode.forObjectElement(NElement.ofPair(key, NElement.ofString("")));
                    } else {
                        // Check if the value starts with structural characters
                        if (current == '[' || current == '{') {
                            YamlNode valueNode = readNode(expectedIndent, true);
                            NElement value = (valueNode != null) ? valueNode.getElement() : NElement.ofString("");
                            return YamlNode.forObjectElement(NElement.ofPair(key, value));
                        } else {
                            // For regular values, use readLiteral to avoid colon issues
                            NElement value = readLiteral(true);
                            return YamlNode.forObjectElement(NElement.ofPair(key, value));
                        }
                    }
                }
                return YamlNode.forLiteral(key);
            }
        }
    }
}

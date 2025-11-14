package net.thevpc.nuts.runtime.standalone.format.yaml;

import net.thevpc.nuts.runtime.standalone.format.json.ReaderLocation;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringBuilder;
import net.thevpc.nuts.util.NStringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class YamlTokenizer {
    private BufferedReader reader;
    private List<YamlToken> buffer = new ArrayList<>();
    private YamlToken last;


    private Step stored = new Step();
    private Step curr = new Step();
    private boolean pushedBack;

    private static class Step implements Cloneable {
        private int fileOffset;
        private int lineNumber;
        private int lineOffset;
        private int current;

        @Override
        public Step clone() {
            try {
                return (Step) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString() {
            return "Step{" +
                    "fileOffset=" + fileOffset +
                    ", lineNumber=" + lineNumber +
                    ", lineOffset=" + lineOffset +
                    ", current=" + (current == -1 ? "-1" : "'" + ((char) current) + "'") +
                    '}';
        }
    }

    private Stack<String> flowMode = new Stack<>();

    public YamlTokenizer(Reader reader) {
        this.reader = (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);
    }

    void pushBack(YamlToken token) {
        buffer.add(0, token);
    }

    YamlToken next() {
        if (!buffer.isEmpty()) {
            return buffer.remove(0);
        }
        YamlToken u = next0();
        last = u;
        return u;
    }

    YamlToken next0() {
        try {
            boolean wasNewline = false;
            int indentation = 0;
            while (true) {
                int icurr = read();
                if (icurr != -1) {
                    char ccurr = (char) icurr;
                    if (icurr == '\n' || icurr == '\r') {
                        indentation = readIndents();
                        wasNewline = true;
                    } else {
                        boolean wasNewLinePrevious = wasNewline;
                        int seenIndentation = wasNewline ? indentation : -1;
                        wasNewline = false;
                        switch (icurr) {
                            case ' ':
                            case '\t': {
                                //just ignore
                                break;
                            }
                            case '\"': {
                                return continueReadDoubleQuotedString(seenIndentation);
                            }
                            case '\'': {
                                return continueReadSingleQuotedString(seenIndentation);
                            }
                            case '#': {
                                continueSkipComments();
                                break;
                            }
                            case '[': {
                                flowMode.push("[");
                                return new YamlToken("[", "[", YamlToken.Type.OPEN_BRACKET, seenIndentation);
                            }
                            case '{': {
                                flowMode.push("{");
                                return new YamlToken("{", "{", YamlToken.Type.OPEN_BRACE, seenIndentation);
                            }
                            case '}': {
                                if (!flowMode.isEmpty()) {
                                    String s = flowMode.peek();
                                    if (s.equals("[")) {
                                        throw expected("']'");
                                    }
                                    flowMode.pop();
                                    return new YamlToken("}", "}", YamlToken.Type.CLOSE_BRACE, seenIndentation);
                                } else {
                                    return continueReadOpenString('}', seenIndentation, wasNewLinePrevious);
                                }
                            }
                            case ']': {
                                if (!flowMode.isEmpty()) {
                                    String s = flowMode.peek();
                                    if (s.equals("{")) {
                                        throw expected("'}'");
                                    }
                                    flowMode.pop();
                                    return new YamlToken("]", "]", YamlToken.Type.CLOSE_BRACKET, seenIndentation);
                                } else {
                                    return continueReadOpenString(']', seenIndentation, wasNewLinePrevious);
                                }
                            }
                            case ',': {
                                if (!flowMode.isEmpty()) {
                                    return new YamlToken(",", ",", YamlToken.Type.COMMA, seenIndentation);
                                } else {
                                    return continueReadOpenString(',', seenIndentation, wasNewLinePrevious);
                                }
                            }
                            case ':': {
                                return new YamlToken(":", ":", YamlToken.Type.COLON, seenIndentation);
                            }
                            case '-': {
                                if (!flowMode.isEmpty()) {
                                    throw error("invalid character " + (char) icurr);
                                } else if (wasNewLinePrevious) {
                                    int n = read();
                                    if (n == '\t' || n == ' ') {
                                        return new YamlToken("-", "-", YamlToken.Type.DASH, seenIndentation);
                                    } else {
                                        return continueReadOpenString('-', seenIndentation, wasNewLinePrevious);
                                    }
                                } else {
                                    return continueReadOpenString('-', seenIndentation, wasNewLinePrevious);
                                }
                            }
                            case '>':
                            case '|': {
                                String scalarType = String.valueOf((char)icurr); // '>' or '|'
                                int indent = seenIndentation;
                                reader.mark(1);
                                int n = reader.read();
                                if(n==-1){
                                    //
                                }else if(n=='+' ||n=='-'){
                                    scalarType+=String.valueOf((char) n);
                                }else{
                                    reader.reset();
                                }
                                String value = readBlockScalar(scalarType, indent);
                                return new YamlToken(
                                        value,
                                        value,
                                        YamlToken.Type.BLOCK_SCALAR,
                                        indent
                                );
                            }
                            default: {
                                if (icurr < 32) {
                                    throw error("invalid character " + (char) icurr);
                                } else {
                                    return continueReadOpenString((char) icurr, seenIndentation, wasNewLinePrevious);
                                }
                            }
                        }
                    }
                } else {
                    return null;
                }
            }
        } catch (IOException ex) {
            return null;
        }
    }

    private String readBlockScalar(String type, int parentIndent) throws IOException {
        NStringBuilder sb = new NStringBuilder();
        int scalarIndent = -1;

        // Read all lines and process them
        List<String> lines = new ArrayList<>();
        List<Integer> indents = new ArrayList<>();

        while (true) {
            reader.mark(1024);
            int currLineLength=0;
            // Read indentation
            int c = reader.read();
            if (c == -1) break;

            int lineIndent = 0;
            while (c == ' ' || c == '\t') {
                lineIndent += (c == ' ') ? 1 : 2;
                currLineLength++;
                c = reader.read();
            }
            if(scalarIndent>=0 && lineIndent<scalarIndent) {
                reader.reset();
                break;
            }
            // Read line content
            StringBuilder line = new StringBuilder();
            while (c != -1 && c != '\n' && c != '\r') {
                line.append((char) c);
                currLineLength++;
                c = reader.read();
            }

            // Skip newlines
            while (c == '\n' || c == '\r') {
                currLineLength++;
                c = reader.read();
            }

            String lineContent = line.toString();
            boolean isBlankLine = NBlankable.isBlank(lineContent);

            // First non-blank line determines scalar indentation
            if (scalarIndent == -1 && !isBlankLine) {
                scalarIndent = lineIndent;
            }

            if (c != -1) {
                reader.reset();
                reader.skip(currLineLength);
            }
            // Skip blank lines before we know the scalar indentation
            if (scalarIndent == -1 && isBlankLine) {
                continue;
            }
            // Store line for processing
            lines.add(lineContent);
            indents.add(lineIndent);
        }

        // Process collected lines
        for (int i = 0; i < lines.size(); i++) {
            String lineContent = lines.get(i);
            boolean isBlankLine = NBlankable.isBlank(lineContent);

            if (type.startsWith(">")) {
                // Folded scalar: spaces instead of newlines, except for blank lines
                if (i > 0 && !sb.toString().isEmpty() && !isBlankLine) {
                    sb.append(' ');
                }
                if (!isBlankLine) {
                    sb.append(NStringUtils.trim(lineContent));
                }
            } else {
                // Literal scalar: preserve newlines and indentation
                if (i > 0) {
                    sb.append('\n');
                }
                sb.append(lineContent);
            }
        }

        // Apply chomping indicators
        String result = sb.toString();
        switch (type) {
            case "|-":
            case ">-":
                return result;
            case "|+":
            case ">+":
                return result + "\n";
            case "|":
            case ">":
            default:
                return result + "\n";
        }
    }

    private YamlToken continueReadOpenString(char current, int indentation, boolean wasNewLinePrevious) throws IOException {
        StringBuilder image = new StringBuilder();
        image.append(current);
        StringBuilder value = new StringBuilder();
        value.append(current);

        WHILE:
        while (true) {
            int next = read();
            switch (next) {
                case '#':
                case '\n':
                case '\r': {
                    unread();
                    break WHILE;
                }
                case ':': {
                    if (!wasNewLinePrevious && last != null && last.type == YamlToken.Type.COLON) {
                        image.append((char) next);
                        value.append((char) next);
                    } else {
                        unread();
                        break WHILE;
                    }
                    break;
                }
                case ',':
                case ']':
                case '}': {
                    if (!flowMode.isEmpty()) {
                        unread();
                        break WHILE;
                    } else {
                        image.append((char) next);
                        value.append((char) next);
                    }
                    break;
                }
                case -1: {
                    break WHILE;
                }
                default: {
                    image.append((char) next);
                    value.append((char) next);
                }
            }
        }

        String trimmed = value.toString().trim();
        if (trimmed.isEmpty()) {
            return new YamlToken(image.toString(), "", YamlToken.Type.NAME, indentation);
        }

        if (trimmed.length() > 0) {
            boolean digit = true;
            boolean dec = false;
            char[] charArray = trimmed.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                char c = charArray[i];
                if (c == 'e' || c == 'E' || c == '.') {
                    // digit
                    dec = true;
                } else if (c >= '0' && c <= '9') {
                    // digit
                } else if (i == 0 && (c == '-' || c == '+')) {
                    // digit
                } else {
                    digit = false;
                }
            }
            if (digit) {
                if (dec) {
                    try {
                        return new YamlToken(image.toString(), Double.parseDouble(trimmed), YamlToken.Type.DECIMAL, indentation);
                    } catch (Exception ex) {
                        try {
                            return new YamlToken(image.toString(), Long.parseLong(trimmed), YamlToken.Type.INTEGER, indentation);
                        } catch (Exception ex2) {
                            return new YamlToken(image.toString(), trimmed, isName(trimmed) ? YamlToken.Type.NAME : YamlToken.Type.OPEN_STRING, indentation);
                        }
                    }
                } else {
                    try {
                        return new YamlToken(image.toString(), Long.parseLong(trimmed), YamlToken.Type.INTEGER, indentation);
                    } catch (Exception ex2) {
                        return new YamlToken(image.toString(), trimmed, isName(trimmed) ? YamlToken.Type.NAME : YamlToken.Type.OPEN_STRING, indentation);
                    }
                }
            } else {
                switch (trimmed) {
                    case "true":
                        return new YamlToken(image.toString(), true, YamlToken.Type.TRUE, indentation);
                    case "false":
                        return new YamlToken(image.toString(), false, YamlToken.Type.FALSE, indentation);
                    case "~":
                    case "null":
                        return new YamlToken(image.toString(), false, YamlToken.Type.NULL, indentation);
                }
                return new YamlToken(image.toString(), trimmed, isName(trimmed) ? YamlToken.Type.NAME : YamlToken.Type.OPEN_STRING, indentation);
            }
        }
        return new YamlToken(image.toString(), trimmed, isName(trimmed) ? YamlToken.Type.NAME : YamlToken.Type.OPEN_STRING, indentation);
    }

    private boolean isName(String a) {
        if (a.length() == 0) {
            return false;
        }
        return a.matches("[a-zA-Z_]([a-zA-Z0-9_-])*");
    }


    private void continueSkipComments() throws IOException {
        while (true) {
            int c = read();
            if (c == '\n' || c == '\r') {
                unread();
                return;
            } else if (c == -1) {
                return;
            }
        }
    }

    private RuntimeException expected(String expected) {
        return error("expected " + expected);
    }

    private RuntimeException error(String message) {
        return new RuntimeException(message + ":" + getLocation());
    }

    ReaderLocation getLocation() {
        return new ReaderLocation(curr.fileOffset, curr.lineNumber, curr.lineOffset);
    }

    private boolean isHexDigit(int current) {
        return current >= '0' && current <= '9'
                || current >= 'a' && current <= 'f'
                || current >= 'A' && current <= 'F';
    }

    private YamlToken continueReadSingleQuotedString(int indentation) throws IOException {
        StringBuilder image = new StringBuilder();
        StringBuilder value = new StringBuilder();
        image.append("'");
        while (true) {
            int current = read();
            if (current == -1) {
                throw expected("\"'\"");
            }
            if (current == '\'') {
                current = read();
                if (current == '\'') {
                    value.append((char) current);
                    image.append((char) current);
                } else if (current == -1) {
                    break;
                } else {
                    unread();
                    break;
                }
            } else if (current < 0x20) {
                throw expected("valid string character");
            } else {
                image.append((char) current);
                value.append((char) current);
            }
        }
        return new YamlToken(image.toString(), value.toString(), YamlToken.Type.SINGLE_STRING, indentation);
    }

    private YamlToken continueReadDoubleQuotedString(int indentation) throws IOException {
        StringBuilder image = new StringBuilder();
        StringBuilder value = new StringBuilder();
        image.append("\"");
        while (true) {
            int current = read();
            if (current == -1) {
                throw expected("'\"'");
            }
            if (current == '\"') {
                break;
            }
            image.append((char) current);
            if (current == '\\') {
                switch (current) {
                    case '"':
                    case '/':
                    case '\\':
                        value.append((char) current);
                        break;
                    case 'b':
                        value.append('\b');
                        break;
                    case 'f':
                        value.append('\f');
                        break;
                    case 'n':
                        value.append('\n');
                        break;
                    case 'r':
                        value.append('\r');
                        break;
                    case 't':
                        value.append('\t');
                        break;
                    case 'u':
                        char[] hexChars = new char[4];
                        for (int i = 0; i < 4; i++) {
                            current = read();
                            if (!isHexDigit(current)) {
                                throw expected("hexadecimal digit");
                            }
                            hexChars[i] = (char) current;
                        }
                        value.append((char) Integer.parseInt(new String(hexChars), 16));
                        break;
                    default:
                        throw expected("valid escape sequence");
                }
            } else if (current < 0x20) {
                throw expected("valid string character");
            } else {
                value.append((char) current);
            }
        }
        return new YamlToken(image.toString(), value.toString(), YamlToken.Type.DOUBLE_STRING, indentation);
    }

    /**
     * return indentation
     *
     * @return
     * @throws IOException
     */
    private int readIndents() throws IOException {
        int indent = 0;
        while (true) {
            int c = read();
            if (c == ' ') {
                indent++;
            } else if (c == '\t') {
                indent += 2; // Tabs are typically treated as 2 spaces in YAML
            } else if (c != -1) {
                unread();
                break;
            } else {
                break;
            }
        }
        return indent;
    }

    private void unread() throws IOException {
        if (!pushedBack) {
            stored = curr.clone();
            pushedBack = true;
        } else {
            throw new IllegalArgumentException("cannot pushback");
        }
    }

    private int read() throws IOException {
        if (pushedBack) {
            curr = stored;
            pushedBack = false;
            return curr.current;
        } else {
            curr.current = reader.read();
            if (curr.current == -1) {
                return -1;
            }
            curr.fileOffset++;
            if (curr.current == '\n') {
                curr.lineNumber++;
                curr.lineOffset = 0;
                return curr.current;
            } else if (curr.current == '\r') {
                curr.lineNumber++;
                curr.lineOffset = 0;
                reader.mark(1);
                int n = reader.read();
                if (n == -1) {
                    return curr.current;
                } else if (n == '\n') {
                    curr.fileOffset++;
                    return curr.current;
                } else {
                    reader.reset();
                }
                return curr.current;
            } else {
                curr.lineOffset++;
                return curr.current;
            }
        }
    }
}

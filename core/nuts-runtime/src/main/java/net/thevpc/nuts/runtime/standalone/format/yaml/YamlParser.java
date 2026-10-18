package net.thevpc.nuts.runtime.standalone.format.yaml;

import net.thevpc.nuts.elem.*;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dependency-free YAML 1.2 reader. The implementation parses YAML syntax into
 * NElement values; YAML graph identity and arbitrary application tag objects
 * are retained as NElement annotations where the NElement model permits it.
 */
class YamlParser {
    public NElement parseElement(Reader reader) {
        StringBuilder source = new StringBuilder();
        char[] buffer = new char[4096];
        try {
            int count;
            while ((count = reader.read(buffer)) >= 0) {
                source.append(buffer, 0, count);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read YAML input", e);
        }
        return new SourceParser(source.toString()).parse();
    }

    private static final class Line {
        final String text;
        final String content;
        final int indent;
        final int number;

        Line(String text, int number) {
            this.text = text;
            this.number = number;
            int i = 0;
            while (i < text.length() && text.charAt(i) == ' ') {
                i++;
            }
            if (i < text.length() && text.charAt(i) == '\t') {
                throw error(number, "tabs are not permitted for indentation");
            }
            this.indent = i;
            this.content = text.substring(i);
        }
    }

    private static final class SourceParser {
        private final List<Line> lines = new ArrayList<>();
        private final Map<String, NElement> anchors = new LinkedHashMap<>();
        private final Map<String, String> tagHandles = new LinkedHashMap<>();
        private int line;

        SourceParser(String source) {
            String[] split = source.split("\\r\\n|\\n|\\r", -1);
            for (int i = 0; i < split.length; i++) {
                lines.add(new Line(split[i], i + 1));
            }
            tagHandles.put("!!", "tag:yaml.org,2002:");
            tagHandles.put("!", "!");
        }

        NElement parse() {
            List<NElement> documents = new ArrayList<>();
            while (true) {
                skipTrivia();
                if (line >= lines.size()) {
                    break;
                }
                Line current = lines.get(line);
                if (current.content.startsWith("%")) {
                    parseDirective(current);
                    line++;
                    continue;
                }
                NElement inlineDocument = null;
                if (current.content.equals("---") || current.content.startsWith("--- ")) {
                    String afterMarker = stripComment(current.content.substring(3)).trim();
                    line++;
                    skipTrivia();
                    if (!afterMarker.isEmpty()) {
                        inlineDocument = parseInline(afterMarker, false, current.number);
                    }
                }
                anchors.clear();
                if (inlineDocument != null) {
                    documents.add(inlineDocument);
                } else if (line < lines.size() && !isDocumentEnd(lines.get(line))) {
                    int indent = lines.get(line).indent;
                    documents.add(parseBlock(indent));
                } else {
                    documents.add(NElement.ofNull());
                }
                skipTrivia();
                if (line < lines.size() && isDocumentEnd(lines.get(line))) {
                    line++;
                }
                tagHandles.clear();
                tagHandles.put("!!", "tag:yaml.org,2002:");
                tagHandles.put("!", "!");
                if (line < lines.size() && isDocumentStart(lines.get(line))) {
                    continue;
                }
                if (line >= lines.size()) {
                    break;
                }
                throw error(lines.get(line).number, "expected a document boundary");
            }
            if (documents.isEmpty()) {
                return NElement.ofNull();
            }
            if (documents.size() == 1) {
                return documents.get(0);
            }
            return NElement.ofFragment(documents.toArray(new NElement[0]));
        }

        private void parseDirective(Line directive) {
            String text = directive.content;
            if (text.startsWith("%YAML ")) {
                if (!text.substring(6).trim().equals("1.2")) {
                    throw error(directive.number, "only YAML version 1.2 is supported");
                }
            } else if (text.startsWith("%TAG ")) {
                String[] parts = text.trim().split("\\s+", 3);
                if (parts.length != 3 || !parts[1].matches("![A-Za-z0-9_-]*!")) {
                    throw error(directive.number, "invalid %TAG directive");
                }
                tagHandles.put(parts[1], parts[2]);
            } else {
                throw error(directive.number, "unsupported YAML directive " + text);
            }
        }

        private NElement parseBlock(int indent) {
            skipTrivia();
            if (line >= lines.size()) {
                return NElement.ofNull();
            }
            Line current = lines.get(line);
            if (current.indent < indent) {
                return NElement.ofNull();
            }
            if (current.indent != indent) {
                throw error(current.number, "unexpected indentation");
            }
            if (isSequenceEntry(current.content)) {
                return parseBlockSequence(indent);
            }
            if (mappingColon(current.content) >= 0 || current.content.startsWith("? ")) {
                return parseBlockMapping(indent);
            }
            line++;
            return parseInlineOrBlockScalar(stripComment(current.content), indent, current.number);
        }

        private NElement parseBlockSequence(int indent) {
            List<NElement> values = new ArrayList<>();
            while (true) {
                skipTrivia();
                if (line >= lines.size()) {
                    break;
                }
                Line current = lines.get(line);
                if (current.indent != indent || !isSequenceEntry(current.content)) {
                    break;
                }
                String rest = current.content.length() == 1 ? "" : current.content.substring(2);
                line++;
                rest = stripComment(rest).trim();
                if (rest.isEmpty()) {
                    skipTrivia();
                    if (line < lines.size() && lines.get(line).indent > indent) {
                        values.add(parseBlock(lines.get(line).indent));
                    } else {
                        values.add(NElement.ofNull());
                    }
                } else if (mappingColon(rest) >= 0 || rest.startsWith("? ")) {
                    values.add(parseSequenceMappingItem(rest, indent, current.number));
                } else {
                    values.add(parseInlineOrBlockScalar(rest, indent, current.number));
                    skipTrivia();
                    if (line < lines.size() && lines.get(line).indent > indent) {
                        throw error(lines.get(line).number, "scalar sequence item cannot have nested content");
                    }
                }
            }
            return NElement.ofArray(values.toArray(new NElement[0]));
        }

        private NElement parseSequenceMappingItem(String first, int sequenceIndent, int sourceLine) {
            int mappingIndent = sequenceIndent + 2;
            List<NElement> pairs = new ArrayList<>();
            parseMappingEntry(first, mappingIndent, sourceLine, pairs);
            while (true) {
                skipTrivia();
                if (line >= lines.size() || lines.get(line).indent != mappingIndent) {
                    break;
                }
                Line next = lines.get(line);
                int colon = mappingColon(next.content);
                if (colon < 0) {
                    break;
                }
                line++;
                parseMappingEntry(next.content, mappingIndent, next.number, pairs);
            }
            return NElement.ofObject(pairs.toArray(new NElement[0]));
        }

        private NElement parseBlockMapping(int indent) {
            List<NElement> pairs = new ArrayList<>();
            while (true) {
                skipTrivia();
                if (line >= lines.size()) {
                    break;
                }
                Line current = lines.get(line);
                if (current.indent != indent || isSequenceEntry(current.content)) {
                    break;
                }
                int colon = mappingColon(current.content);
                if (colon < 0) {
                    break;
                }
                line++;
                parseMappingEntry(current.content, indent, current.number, pairs);
            }
            return NElement.ofObject(pairs.toArray(new NElement[0]));
        }

        private void parseMappingEntry(String text, int indent, int sourceLine, List<NElement> pairs) {
            int colon = mappingColon(text);
            if (colon < 0) {
                throw error(sourceLine, "expected ':' in mapping entry");
            }
            String keyText = text.substring(0, colon).trim();
            String valueText = stripComment(text.substring(colon + 1)).trim();
            NElement key = keyText.isEmpty() ? NElement.ofNull() : parseInline(keyText, false, sourceLine);
            NElement value;
            if (valueText.startsWith("|") || valueText.startsWith(">")) {
                value = parseBlockScalar(valueText, indent, sourceLine);
            } else if (!valueText.isEmpty()) {
                value = parseInline(valueText, false, sourceLine);
            } else {
                skipTrivia();
                if (line < lines.size() && lines.get(line).indent > indent) {
                    value = parseBlock(lines.get(line).indent);
                } else {
                    value = NElement.ofNull();
                }
            }
            pairs.add(NElement.ofPair(key, value));
        }

        private NElement parseInlineOrBlockScalar(String text, int parentIndent, int sourceLine) {
            if (text.startsWith("|") || text.startsWith(">")) {
                return parseBlockScalar(text, parentIndent, sourceLine);
            }
            return parseInline(text, false, sourceLine);
        }

        private NElement parseBlockScalar(String header, int parentIndent, int sourceLine) {
            String indicator = header.substring(0, 1);
            int explicitIndent = -1;
            char chomping = 0;
            for (int i = 1; i < header.length(); i++) {
                char c = header.charAt(i);
                if (c == '+' || c == '-') {
                    chomping = c;
                } else if (c >= '1' && c <= '9') {
                    explicitIndent = parentIndent + (c - '0');
                } else if (c != ' ' && c != '\t' && c != '#') {
                    throw error(sourceLine, "invalid block scalar header");
                }
            }
            List<String> content = new ArrayList<>();
            int scalarIndent = explicitIndent;
            int firstNonBlank = -1;
            while (line < lines.size()) {
                Line next = lines.get(line);
                if (!next.content.trim().isEmpty() && next.indent <= parentIndent) {
                    break;
                }
                if (!next.content.trim().isEmpty() && scalarIndent < 0) {
                    scalarIndent = next.indent;
                    firstNonBlank = content.size();
                }
                if (next.content.trim().isEmpty()) {
                    content.add("");
                } else {
                    if (scalarIndent >= 0 && next.indent < scalarIndent) {
                        break;
                    }
                    content.add(next.text.substring(Math.min(scalarIndent, next.text.length())));
                }
                line++;
            }
            if (scalarIndent < 0) {
                scalarIndent = parentIndent + 1;
            }
            if (firstNonBlank > 0) {
                for (int i = 0; i < firstNonBlank; i++) {
                    content.remove(0);
                }
            }
            String value;
            if (indicator.equals("|")) {
                value = joinLiteral(content);
            } else {
                value = joinFolded(content);
            }
            if (chomping == '-') {
                value = stripTrailingNewlines(value);
            } else if (chomping != '+') {
                value = stripTrailingNewlines(value);
                if (!content.isEmpty()) {
                    value += "\n";
                }
            }
            return NElement.ofString(value, NElementType.DOUBLE_QUOTED_STRING);
        }

        private NElement parseInline(String text, boolean flow, int sourceLine) {
            return new FlowParser(text, sourceLine).parse();
        }

        private final class FlowParser {
            private final String text;
            private final int sourceLine;
            private int pos;

            FlowParser(String text, int sourceLine) {
                this.text = text;
                this.sourceLine = sourceLine;
            }

            NElement parse() {
                NElement value = parseNode(false);
                whitespace();
                if (pos < text.length()) {
                    throw error(sourceLine, "unexpected character '" + text.charAt(pos) + "'");
                }
                return value;
            }

            private NElement parseNode(boolean inFlow) {
                whitespace();
                List<String> prefixes = new ArrayList<>();
                while (pos < text.length() && (text.charAt(pos) == '&' || text.charAt(pos) == '!')) {
                    char marker = text.charAt(pos++);
                    int start = pos;
                    if (marker == '!' && pos < text.length() && text.charAt(pos) == '<') {
                        pos++;
                        while (pos < text.length() && text.charAt(pos) != '>') {
                            pos++;
                        }
                        if (pos >= text.length()) {
                            throw error(sourceLine, "unterminated verbatim tag");
                        }
                        pos++;
                    } else {
                        while (pos < text.length() && !Character.isWhitespace(text.charAt(pos))
                                && ",[]{}:".indexOf(text.charAt(pos)) < 0) {
                            pos++;
                        }
                    }
                    prefixes.add(marker + text.substring(start, pos));
                    whitespace();
                }
                if (pos >= text.length()) {
                    return applyPrefixes(NElement.ofNull(), prefixes, sourceLine);
                }
                char c = text.charAt(pos);
                if (c == '*') {
                    pos++;
                    String anchor = readWord();
                    NElement target = anchors.get(anchor);
                    if (target == null) {
                        throw error(sourceLine, "unknown or forward alias '*" + anchor + "'");
                    }
                    NElement alias = target.builder().addAnnotation("yamlAlias", NElement.ofString(anchor)).build();
                    return applyPrefixes(alias, prefixes, sourceLine);
                }
                NElement result;
                if (c == '[') {
                    result = parseFlowSequence();
                } else if (c == '{') {
                    result = parseFlowMapping();
                } else if (c == '\'') {
                    result = NElement.ofString(singleQuoted(), NElementType.SINGLE_QUOTED_STRING);
                } else if (c == '"') {
                    result = NElement.ofString(doubleQuoted(), NElementType.DOUBLE_QUOTED_STRING);
                } else {
                    String plain = readPlain(inFlow);
                    String explicitTag = explicitStandardTag(prefixes, sourceLine);
                    result = "str".equals(explicitTag)
                            ? NElement.ofString(plain) : resolvePlain(plain, sourceLine);
                }
                return applyPrefixes(result, prefixes, sourceLine);
            }

            private String explicitStandardTag(List<String> prefixes, int sourceLine) {
                for (String prefix : prefixes) {
                    if (prefix.startsWith("!")) {
                        String expanded = expandTag(prefix.substring(1), sourceLine);
                        if (expanded.startsWith("tag:yaml.org,2002:")) {
                            return expanded.substring("tag:yaml.org,2002:".length());
                        }
                    }
                }
                return null;
            }

            private NElement parseFlowSequence() {
                pos++;
                List<NElement> values = new ArrayList<>();
                whitespace();
                if (take(']')) {
                    return NElement.ofArray();
                }
                while (true) {
                    values.add(parseNode(true));
                    whitespace();
                    if (take(']')) {
                        break;
                    }
                    expect(',', "expected ',' or ']' in flow sequence");
                    whitespace();
                    if (take(']')) {
                        break;
                    }
                }
                return NElement.ofArray(values.toArray(new NElement[0]));
            }

            private NElement parseFlowMapping() {
                pos++;
                List<NElement> pairs = new ArrayList<>();
                whitespace();
                if (take('}')) {
                    return NElement.ofObject();
                }
                while (true) {
                    NElement key = parseNode(true);
                    whitespace();
                    expect(':', "expected ':' in flow mapping");
                    whitespace();
                    NElement value;
                    if (pos >= text.length() || text.charAt(pos) == ',' || text.charAt(pos) == '}') {
                        value = NElement.ofNull();
                    } else {
                        value = parseNode(true);
                    }
                    pairs.add(NElement.ofPair(key, value));
                    whitespace();
                    if (take('}')) {
                        break;
                    }
                    expect(',', "expected ',' or '}' in flow mapping");
                    whitespace();
                    if (take('}')) {
                        break;
                    }
                }
                return NElement.ofObject(pairs.toArray(new NElement[0]));
            }

            private String readPlain(boolean inFlow) {
                int start = pos;
                while (pos < text.length()) {
                    char c = text.charAt(pos);
                    if (inFlow && ",[]{}".indexOf(c) >= 0) {
                        break;
                    }
                    if (c == ':' && (pos + 1 == text.length() || Character.isWhitespace(text.charAt(pos + 1))
                            || (inFlow && ",[]{}".indexOf(text.charAt(pos + 1)) >= 0))) {
                        break;
                    }
                    if (c == '#' && (pos == start || Character.isWhitespace(text.charAt(pos - 1)))) {
                        break;
                    }
                    pos++;
                }
                return text.substring(start, pos).trim();
            }

            private String singleQuoted() {
                pos++;
                StringBuilder value = new StringBuilder();
                while (pos < text.length()) {
                    char c = text.charAt(pos++);
                    if (c == '\'') {
                        if (pos < text.length() && text.charAt(pos) == '\'') {
                            pos++;
                            value.append('\'');
                        } else {
                            return value.toString();
                        }
                    } else {
                        value.append(c);
                    }
                }
                throw error(sourceLine, "unterminated single-quoted scalar");
            }

            private String doubleQuoted() {
                pos++;
                StringBuilder value = new StringBuilder();
                while (pos < text.length()) {
                    char c = text.charAt(pos++);
                    if (c == '"') {
                        return value.toString();
                    }
                    if (c != '\\') {
                        value.append(c);
                        continue;
                    }
                    if (pos >= text.length()) {
                        break;
                    }
                    char e = text.charAt(pos++);
                    switch (e) {
                        case '0': value.append('\0'); break;
                        case 'a': value.append('\u0007'); break;
                        case 'b': value.append('\b'); break;
                        case 't': value.append('\t'); break;
                        case 'n': value.append('\n'); break;
                        case 'v': value.append('\u000b'); break;
                        case 'f': value.append('\f'); break;
                        case 'r': value.append('\r'); break;
                        case 'e': value.append('\u001b'); break;
                        case ' ': value.append(' '); break;
                        case '"': value.append('"'); break;
                        case '/': value.append('/'); break;
                        case '\\': value.append('\\'); break;
                        case 'N': value.append('\u0085'); break;
                        case '_': value.append('\u00a0'); break;
                        case 'L': value.append('\u2028'); break;
                        case 'P': value.append('\u2029'); break;
                        case 'x': value.append((char) readHex(2)); break;
                        case 'u': value.append((char) readHex(4)); break;
                        case 'U': value.appendCodePoint(readHex(8)); break;
                        case '\n':
                            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                                pos++;
                            }
                            break;
                        default: throw error(sourceLine, "invalid double-quoted escape '\\" + e + "'");
                    }
                }
                throw error(sourceLine, "unterminated double-quoted scalar");
            }

            private int readHex(int digits) {
                if (pos + digits > text.length()) {
                    throw error(sourceLine, "incomplete hexadecimal escape");
                }
                try {
                    int value = Integer.parseInt(text.substring(pos, pos + digits), 16);
                    pos += digits;
                    return value;
                } catch (NumberFormatException e) {
                    throw error(sourceLine, "invalid hexadecimal escape");
                }
            }

            private String readWord() {
                int start = pos;
                while (pos < text.length() && !Character.isWhitespace(text.charAt(pos))
                        && ",[]{}:".indexOf(text.charAt(pos)) < 0) {
                    pos++;
                }
                if (start == pos) {
                    throw error(sourceLine, "expected anchor name");
                }
                return text.substring(start, pos);
            }

            private void whitespace() {
                while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                    pos++;
                }
            }

            private boolean take(char c) {
                if (pos < text.length() && text.charAt(pos) == c) {
                    pos++;
                    return true;
                }
                return false;
            }

            private void expect(char c, String message) {
                if (!take(c)) {
                    throw error(sourceLine, message);
                }
            }
        }

        private NElement applyPrefixes(NElement element, List<String> prefixes, int sourceLine) {
            for (String prefix : prefixes) {
                if (prefix.startsWith("&")) {
                    String anchor = prefix.substring(1);
                    anchors.put(anchor, element);
                    element = element.builder().addAnnotation("yamlAnchor", NElement.ofString(anchor)).build();
                    anchors.put(anchor, element);
                } else {
                    String tag = expandTag(prefix.substring(1), sourceLine);
                    element = applyTag(element, tag, sourceLine);
                }
            }
            return element;
        }

        private String expandTag(String handleAndSuffix, int sourceLine) {
            if (handleAndSuffix.startsWith("<") && handleAndSuffix.endsWith(">")) {
                return handleAndSuffix.substring(1, handleAndSuffix.length() - 1);
            }
            if (handleAndSuffix.startsWith("!")) {
                return "tag:yaml.org,2002:" + handleAndSuffix.substring(1);
            }
            int bang = handleAndSuffix.indexOf('!');
            if (bang >= 0) {
                String handle = "!" + handleAndSuffix.substring(0, bang + 1);
                String suffix = handleAndSuffix.substring(bang + 1);
                String prefix = tagHandles.get(handle);
                if (prefix == null) {
                    throw error(sourceLine, "undefined tag handle " + handle);
                }
                return prefix + suffix;
            }
            return "!" + handleAndSuffix;
        }

        private NElement applyTag(NElement element, String tag, int sourceLine) {
            String shortTag = tag.startsWith("tag:yaml.org,2002:")
                    ? tag.substring("tag:yaml.org,2002:".length()) : null;
            if (shortTag != null) {
                Object rawValue = element.isPrimitive() ? element.asPrimitive().get().value() : null;
                String value = rawValue == null ? null : rawValue.toString();
                switch (shortTag) {
                    case "str": return NElement.ofString(value == null ? "" : value);
                    case "null": return NElement.ofNull();
                    case "bool":
                        if ("true".equalsIgnoreCase(value)) return NElement.ofTrue();
                        if ("false".equalsIgnoreCase(value)) return NElement.ofFalse();
                        throw error(sourceLine, "invalid !!bool value");
                    case "int": return number(value, sourceLine);
                    case "float": return number(value, sourceLine);
                    case "seq":
                        if (element.isArray()) return element;
                        throw error(sourceLine, "!!seq requires a sequence node");
                    case "map":
                        if (element.isObject()) return element;
                        throw error(sourceLine, "!!map requires a mapping node");
                }
            }
            if ("tag:thevpc.net,nuts/name".equals(tag) && element.isAnyStringOrName()) {
                String value = element.asStringValue().get();
                if (!value.matches("[a-zA-Z_]([a-zA-Z0-9_-])*")) {
                    throw error(sourceLine, "Nuts name tag requires a valid name");
                }
                return NElement.ofName(value);
            }
            return element.builder().addAnnotation("yamlTag", NElement.ofString(tag)).build();
        }

        private NElement resolvePlain(String value, int sourceLine) {
            if (value.isEmpty() || value.equals("~") || value.equalsIgnoreCase("null")) {
                return NElement.ofNull();
            }
            if (value.equalsIgnoreCase("true")) {
                return NElement.ofTrue();
            }
            if (value.equalsIgnoreCase("false")) {
                return NElement.ofFalse();
            }
            NElement n = numberOrNull(value);
            if (n != null) {
                return n;
            }
            if (value.matches("[a-zA-Z_]([a-zA-Z0-9_-])*")) {
                return NElement.ofName(value);
            }
            return NElement.ofString(value);
        }

        private NElement numberOrNull(String value) {
            try {
                if (value.matches("[-+]?0[xX][0-9a-fA-F_]+")) {
                    BigInteger integer = new BigInteger(value.replace("_", "").replaceFirst("^[-+]?0[xX]", ""), 16)
                            .multiply(value.startsWith("-") ? BigInteger.valueOf(-1) : BigInteger.ONE);
                    return integerElement(integer);
                }
                if (value.matches("[-+]?0[oO][0-7_]+")) {
                    String digits = value.replace("_", "").replaceFirst("^[-+]?0[oO]", "");
                    BigInteger v = new BigInteger(digits, 8);
                    return integerElement(value.startsWith("-") ? v.negate() : v);
                }
                if (value.matches("[-+]?(0|[1-9][0-9_]*)(\\.[0-9_]*)?([eE][-+]?[0-9]+)?")
                        && (value.indexOf('.') >= 0 || value.indexOf('e') >= 0 || value.indexOf('E') >= 0)) {
                    return NElement.ofDouble(Double.parseDouble(value.replace("_", "")));
                }
                if (value.matches("[-+]?(0|[1-9][0-9_]*)")) {
                    return integerElement(new BigInteger(value.replace("_", "")));
                }
                if (value.matches("[-+]?\\.(inf|Inf|INF)")) {
                    return NElement.ofDouble(value.startsWith("-") ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
                }
                if (value.matches("\\.(nan|NaN|NAN)")) {
                    return NElement.ofDouble(Double.NaN);
                }
            } catch (NumberFormatException ignored) {
                return null;
            }
            return null;
        }

        private NElement integerElement(BigInteger value) {
            if (value.bitLength() < 63) {
                return NElement.ofNumber(value.longValue());
            }
            if (value.bitLength() == 63 && value.signum() < 0) {
                return NElement.ofNumber(value.longValue());
            }
            return NElement.ofNumber(value);
        }

        private NElement number(String value, int sourceLine) {
            NElement n = numberOrNull(value == null ? "" : value);
            if (n == null) {
                throw error(sourceLine, "invalid numeric tag value");
            }
            return n;
        }

        private void skipTrivia() {
            while (line < lines.size()) {
                Line current = lines.get(line);
                if (current.content.trim().isEmpty() || current.content.startsWith("#")) {
                    line++;
                } else {
                    break;
                }
            }
        }

        private boolean isSequenceEntry(String content) {
            return content.equals("-") || (content.startsWith("-") && content.length() > 1
                    && Character.isWhitespace(content.charAt(1)));
        }

        private boolean isDocumentStart(Line current) {
            return current.indent == 0 && current.content.startsWith("---")
                    && (current.content.length() == 3 || Character.isWhitespace(current.content.charAt(3)));
        }

        private boolean isDocumentEnd(Line current) {
            return current.indent == 0 && current.content.equals("...");
        }

        private int mappingColon(String text) {
            char quote = 0;
            int depth = 0;
            boolean escaped = false;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (quote != 0) {
                    if (quote == '"' && c == '\\' && !escaped) {
                        escaped = true;
                        continue;
                    }
                    if (c == quote && !escaped) {
                        if (quote == '\'' && i + 1 < text.length() && text.charAt(i + 1) == '\'') {
                            i++;
                            continue;
                        }
                        quote = 0;
                    }
                    escaped = false;
                    continue;
                }
                if (c == '\'' || c == '"') {
                    quote = c;
                } else if (c == '[' || c == '{') {
                    depth++;
                } else if (c == ']' || c == '}') {
                    depth--;
                } else if (c == ':' && depth == 0
                        && (i + 1 == text.length() || Character.isWhitespace(text.charAt(i + 1)))) {
                    return i;
                }
            }
            return -1;
        }

        private String stripComment(String text) {
            char quote = 0;
            int depth = 0;
            boolean escaped = false;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (quote != 0) {
                    if (quote == '"' && c == '\\' && !escaped) {
                        escaped = true;
                        continue;
                    }
                    if (c == quote && !escaped) quote = 0;
                    escaped = false;
                    continue;
                }
                if (c == '\'' || c == '"') quote = c;
                else if (c == '[' || c == '{') depth++;
                else if (c == ']' || c == '}') depth--;
                else if (c == '#' && depth == 0 && (i == 0 || Character.isWhitespace(text.charAt(i - 1)))) {
                    return text.substring(0, i);
                }
            }
            return text;
        }

        private String joinLiteral(List<String> lines) {
            if (lines.isEmpty()) return "";
            StringBuilder b = new StringBuilder();
            for (String s : lines) {
                b.append(s).append('\n');
            }
            return b.toString();
        }

        private String joinFolded(List<String> lines) {
            if (lines.isEmpty()) return "";
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < lines.size(); i++) {
                String current = lines.get(i);
                b.append(current);
                if (i + 1 < lines.size()) {
                    String next = lines.get(i + 1);
                    b.append(current.isEmpty() || next.isEmpty() ? '\n' : ' ');
                }
            }
            return b.append('\n').toString();
        }

        private String stripTrailingNewlines(String value) {
            int end = value.length();
            while (end > 0 && value.charAt(end - 1) == '\n') end--;
            return value.substring(0, end);
        }
    }

    private static IllegalArgumentException error(int line, String message) {
        return new IllegalArgumentException("YAML line " + line + ": " + message);
    }
}

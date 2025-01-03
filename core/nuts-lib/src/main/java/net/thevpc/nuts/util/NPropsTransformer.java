package net.thevpc.nuts.util;

import net.thevpc.nuts.io.NIOException;

import java.io.*;
import java.util.*;
import java.util.function.Function;

public class NPropsTransformer {
    private Map<String, Function<String, String>> replacements = new LinkedHashMap<>();
    private boolean sort = false;
    private boolean distinct = false;

    public NPropsTransformer() {
    }

    public static String encodeKey(String theString) {
        return encodeString(theString, true, true, false);
    }

    public static String encodeValue(String theString) {
        return encodeString(theString, false, false, false);
    }

    /*
     * Converts unicodes to encoded &#92;uxxxx and escapes
     * special characters with a preceding slash.
     * This is a modified method from java.util.Properties because the method
     * is private but we need call it handle special properties files
     */
    public static String encodeString(String theString,
                                      boolean escapeSpace,
                                      boolean escapeSep,
                                      boolean escapeComment
    ) {
        if (theString == null) {
            theString = "";
        }
        char[] chars = theString.toCharArray();
        StringBuilder buffer = new StringBuilder(chars.length);
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '\\': {
                    buffer.append("\\\\");
                    break;
                }
                case ' ': {
                    if (i == 0 || i == chars.length - 1 || escapeSpace) {
                        buffer.append('\\');
                    }
                    buffer.append(' ');
                    break;
                }
                case '\t': {
                    if (i == 0 || i == chars.length - 1 || escapeSpace) {
                        buffer.append("\\t");
                    } else {
                        buffer.append(c);
                    }
                    break;
                }
                case '\n': {
                    buffer.append("\\n");
                    break;
                }
                case '\r': {
                    buffer.append("\\r");
                    break;
                }
                case '\f': {
                    buffer.append("\\f");
                    break;
                }
                case '#':
                case '!': {
                    if (escapeComment || i == 0) {
                        buffer.append('\\');
                    }
                    buffer.append(c);
                    break;
                }
                case ':':
                case '=': {
                    if (escapeSep) {
                        buffer.append('\\');
                    }
                    buffer.append(c);
                    break;
                }
                default: {
                    if ((c > 61) && (c < 127)) {
                        buffer.append(c);
                    } else if (((c < 0x0020) || (c > 0x007e))) {
                        buffer.append('\\');
                        buffer.append('u');
                        buffer.append(NHex.toHexChar((c >> 12) & 0xF));
                        buffer.append(NHex.toHexChar((c >> 8) & 0xF));
                        buffer.append(NHex.toHexChar((c >> 4) & 0xF));
                        buffer.append(NHex.toHexChar(c & 0xF));
                    } else {
                        buffer.append(c);
                    }
                }
            }
        }
        return buffer.toString();
    }

    public static void storeProperties(Map<String, String> props, OutputStream out, boolean sort) {
        storeProperties(props, new OutputStreamWriter(out), sort);
    }

    public static void storeProperties(Map<String, String> props, Writer w, boolean sort) {
        try {
            Set<String> keys = props.keySet();
            if (sort) {
                keys = new TreeSet<>(keys);
            }
            for (String key : keys) {
                String value = props.get(key);
                w.write(encodeKey(key));
                w.write("=");
                w.write(encodeValue(value));
                w.write("\n");
                w.flush();
            }
            w.flush();
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public boolean isSort() {
        return sort;
    }

    public NPropsTransformer sort() {
        this.sort = true;
        return this;
    }

    public NPropsTransformer sort(boolean sort) {
        this.sort = sort;
        return this;
    }

    public NPropsTransformer setSort(boolean sort) {
        this.sort = sort;
        return this;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public NPropsTransformer distinct() {
        this.distinct = true;
        return this;
    }

    public NPropsTransformer distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public NPropsTransformer setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public NPropsTransformer remove(String varName) {
        replacements.put(varName, s -> null);
        return this;
    }

    public NPropsTransformer replace(String varName, String replacement) {
        validateKeyName(varName);
        if (replacement == null) {
            replacements.put(varName, null);
        } else {
            replacements.put(varName, s -> replacement);
        }
        return this;
    }

    public NPropsTransformer unreplace(String varName) {
        replacements.remove(varName);
        return this;
    }

    public NPropsTransformer replace(String varName, Function<String, String> replacement) {
        validateKeyName(varName);
        if (replacement == null) {
            replacements.put(varName, null);
        } else {
            replacements.put(varName, replacement);
        }
        return this;
    }

    private static void validateKeyName(String varName) {
        for (char c : varName.toCharArray()) {
            if (Character.isWhitespace(c)) {
                throw new IllegalArgumentException("invalid variable name " + varName);
            }
            if (c == '=') {
                throw new IllegalArgumentException("invalid variable name " + varName);
            }
            if (c == ':') {
                throw new IllegalArgumentException("invalid variable name " + varName);
            }
        }
    }

    public void transform(Reader reader, PrintStream out) {
        BufferedReader bReader = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
        List<Row> rows = new ArrayList<>();
        while (true) {
            String line = null;
            NStringBuilder sb = null;
            while (true) {
                try {
                    line = bReader.readLine();
                } catch (IOException e) {
                    //
                }
                if (line == null) {
                    break;
                } else {
                    if (line.endsWith("\\")) {
                        if (sb == null) {
                            sb = new NStringBuilder();
                        }
                        sb.println(line);
                    } else {
                        if (sb == null) {
                            sb = new NStringBuilder();
                        }
                        sb.append(line);
                        break;
                    }
                }
            }
            if (sb == null) {
                break;
            }
            line = sb.toString();
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                rows.add(new Row(RowType.EMPTY, null, line));
            } else if (trimmedLine.startsWith("#") || trimmedLine.startsWith("!")) {
                rows.add(new Row(RowType.COMMENT, null, line));
            } else {
                if (!processLine(line, rows)) {
                    rows.add(newKeyVal(extractKey(line), line, rows));
                }
            }
        }
        //preprocesss
        // perhaps, sort, remove duplicates, etc..
        if (distinct || sort) {
            int index = 1;
            for (Row row : rows) {
                row.index = index++;
            }
            if (sort) {
                rows.sort((a, b) -> {
                    if (a.type == RowType.KEY_VAL && b.type == RowType.KEY_VAL) {
                        int x = a.key.compareTo(b.key);
                        if (x != 0) {
                            return x;
                        }
                        return a.index - b.index;
                    } else {
                        if (a.index != b.index) {
                            return a.index - b.index;
                        }
                        return a.key.compareTo(b.key);
                    }
                });
            }
            if (distinct) {
                HashMap<String, Integer> keyToPos = new HashMap<>();
                for (int i = 0; i < rows.size(); i++) {
                    Row row = rows.get(i);
                    if (row.type == RowType.KEY_VAL) {
                        Integer pos = keyToPos.get(row.key);
                        if (pos != null) {
                            rows.set(pos, row);
                            i--;
                        } else {
                            keyToPos.put(row.key, i);
                        }
                    }
                }
            }
        }
        // finally write content...
        for (Row row : rows) {
            if (row.headers != null) {
                for (Row header : row.headers) {
                    out.println(header.row);
                }
            }
            out.println(row.row);
        }
        out.flush();
    }

    private String extractKey(String line) {
        if (line == null) {
            return null;
        }
        line = line.trim();
        StringBuilder sb = new StringBuilder();
        StringBuilder pending = new StringBuilder();
        char[] charArray = line.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (Character.isWhitespace(c)) {
                pending.append(c);
            } else if (c == '=' || c == ':') {
                break;
            } else if (c == '\\') {
                sb.append(c);
                i++;
                if (i < charArray.length) {
                    sb.append(charArray[i]);
                }
            } else {
                if (pending.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append(pending);
                    }
                    pending.setLength(0);
                }
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Row newKeyVal(String key, String value, List<Row> rows) {
        List<Row> comments = new ArrayList<>();
        while (true) {
            if (rows.isEmpty()) {
                break;
            }
            if (rows.get(rows.size() - 1).type == RowType.COMMENT) {
                comments.add(0, rows.remove(rows.size() - 1));
            }else{
                break;
            }
        }
        Row row = new Row(RowType.KEY_VAL, key, value);
        if (comments.size() > 0) {
            row.headers = comments;
        }
        return row;
    }

    private boolean processLine(String line, List<Row> rows) {
        for (Map.Entry<String, Function<String, String>> e : replacements.entrySet()) {
            if (replaceVar(e.getKey(), e.getValue(), line, rows)) {
                return true;
            }
        }
        return false;
    }

    private enum RowType {
        KEY_VAL,
        EMPTY,
        COMMENT,
    }

    private static class Row {
        List<Row> headers;
        String key;
        String row;
        RowType type;
        int index;

        public Row(RowType type, String key, String row) {
            this.type = type;
            this.key = key;
            this.row = row;
        }
    }

    private boolean replaceVar(String varName, Function<String, String> suffix, String line, List<Row> rows) {
        String lineTrimmed = line.trim();
        if (lineTrimmed.startsWith(varName)) {
            String ext = lineTrimmed.substring(varName.length());
            if (ext.trim().startsWith("=")) {
                int e = line.indexOf('=');
                String oldValue = NStringUtils.trimLeft(ext.substring(1));
                String nv = suffix.apply(decodeString(oldValue));
                if (nv == null) {
                    //do nothing
                } else {
                    rows.add(newKeyVal(varName, line.substring(0, e + 1) + encodeValue(nv), rows));
                }
                return true;
            } else if (ext.trim().startsWith(":")) {
                int e = line.indexOf(':');
                String oldValue = NStringUtils.trimLeft(ext.substring(1));
                String nv = suffix.apply(decodeString(oldValue));
                if (nv == null) {
                    //do nothing
                } else {
                    rows.add(newKeyVal(varName, line.substring(0, e + 1) + encodeValue(nv), rows));
                }
                return true;
            }
        }
        return false;
    }

    public static String decodeString(String str) {
        int i = 0;
        char[] in = str.toCharArray();
        int len = str.length();
        int bLen = len * 2;
        if (bLen < 0) {
            bLen = Integer.MAX_VALUE;
        }
        char[] out = new char[bLen];
        int oi = 0;
        int end = i + len;
        while (i < end) {
            char c = in[i++];
            if (c == '\\') {
                c = in[i++];
                if (c == 'u') {
                    int nc = 0;
                    for (int j = 0; j < 4; ++j) {
                        c = in[i++];
                        if (c >= '0' && c <= '9') {
                            nc = (nc << 4) + c - 48;
                        } else if (c >= 'A' && c <= 'F') {
                            nc = (nc << 4) + 10 + c - 65;
                        } else if (c >= 'a' && c <= 'f') {
                            nc = (nc << 4) + 10 + c - 97;
                        } else {
                            throw new IllegalArgumentException("Invalid \\uxxxx encoding");
                        }
                    }
                    out[oi++] = (char) nc;
                } else {
                    switch (c) {
                        case 't': {
                            out[oi++] = '\t';
                            break;
                        }
                        case 'r': {
                            out[oi++] = '\r';
                            break;
                        }
                        case 'n': {
                            out[oi++] = '\n';
                            break;
                        }
                        case 'f': {
                            out[oi++] = '\f';
                            break;
                        }
                        case '\\': {
                            out[oi++] = '\\';
                            break;
                        }
                        default: {
                            out[oi++] = c;
                        }
                    }
                }
            } else {
                out[oi++] = c;
            }
        }
        return new String(out, 0, oi);
    }
}

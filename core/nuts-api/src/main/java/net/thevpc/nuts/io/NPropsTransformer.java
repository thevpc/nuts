package net.thevpc.nuts.io;

import net.thevpc.nuts.util.*;

import java.io.*;
import java.util.*;
import java.util.function.Function;

/**
 * NPropsTransformer class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NPropsTransformer {
    private Map<String, Function<String, String>> replacements = new LinkedHashMap<>();
    private boolean sort = false;
    private boolean distinct = false;

    /**
     * N props transformer.
     *
     * @return n props transformer result
     */
    public NPropsTransformer() {
    }

    /**
     * Encode key.
     *
     * @param theString the string
     * @return encode key result
     */
    public static String encodeKey(String theString) {
        /**
         * Encode string.
         *
         * @param theString the string
         * @param true true
         * @param true true
         * @param false false
         * @return encode string result
         */
        return encodeString(theString, true, true, false);
    }

    /**
     * Encode value.
     *
     * @param theString the string
     * @return encode value result
     */
    public static String encodeValue(String theString) {
        /**
         * Encode string.
         *
         * @param theString the string
         * @param false false
         * @param false false
         * @param false false
         * @return encode string result
         */
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

    /**
     * Store properties.
     *
     * @param props props
     * @param out out
     * @param sort sort
     */
    public static void storeProperties(Map<String, String> props, OutputStream out, boolean sort) {
      /**
       * Store properties.
       *
       * @param props props
       * @param OutputStreamWriter(out) output stream writer(out)
       * @param sort sort
       */
        storeProperties(props, new OutputStreamWriter(out), sort);
    }

    /**
     * Store properties.
     *
     * @param props props
     * @param w w
     * @param sort sort
     */
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
            /**
             * Nio exception.
             *
             * @param ex ex
             * @return nio exception result
             */
            throw new NIOException(ex);
        }
    }

    /**
     * Checks if is sort.
     *
     * @return is sort result
     */
    public boolean isSort() {
        return sort;
    }

    /**
     * Sort.
     *
     * @return sort result
     */
    public NPropsTransformer sort() {
        this.sort = true;
        return this;
    }

    /**
     * Sort.
     *
     * @param sort sort
     * @return sort result
     */
    public NPropsTransformer sort(boolean sort) {
        this.sort = sort;
        return this;
    }

    /**
     * Sets the sort.
     *
     * @param sort sort
     * @return set sort result
     */
    public NPropsTransformer setSort(boolean sort) {
        this.sort = sort;
        return this;
    }

    /**
     * Checks if is distinct.
     *
     * @return is distinct result
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * Distinct.
     *
     * @return distinct result
     */
    public NPropsTransformer distinct() {
        this.distinct = true;
        return this;
    }

    /**
     * Distinct.
     *
     * @param distinct distinct
     * @return distinct result
     */
    public NPropsTransformer distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    /**
     * Sets the distinct.
     *
     * @param distinct distinct
     * @return set distinct result
     */
    public NPropsTransformer setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    /**
     * Removes remove.
     *
     * @param varName var name
     * @return remove result
     */
    public NPropsTransformer remove(String varName) {
        replacements.put(varName, s -> null);
        return this;
    }

    /**
     * Replace.
     *
     * @param varName var name
     * @param replacement replacement
     * @return replace result
     */
    public NPropsTransformer replace(String varName, String replacement) {
      /**
       * Validate key name.
       *
       * @param varName var name
       */
        validateKeyName(varName);
        if (replacement == null) {
            replacements.put(varName, null);
        } else {
            replacements.put(varName, s -> replacement);
        }
        return this;
    }

    /**
     * Unreplace.
     *
     * @param varName var name
     * @return unreplace result
     */
    public NPropsTransformer unreplace(String varName) {
        replacements.remove(varName);
        return this;
    }

    /**
     * Replace.
     *
     * @param varName var name
     * @param replacement replacement
     * @return replace result
     */
    public NPropsTransformer replace(String varName, Function<String, String> replacement) {
      /**
       * Validate key name.
       *
       * @param varName var name
       */
        validateKeyName(varName);
        if (replacement == null) {
            replacements.put(varName, null);
        } else {
            replacements.put(varName, replacement);
        }
        return this;
    }

    /**
     * Validate key name.
     *
     * @param varName var name
     * @return validate key name result
     */
    private static void validateKeyName(String varName) {
        for (char c : varName.toCharArray()) {
            if (Character.isWhitespace(c)) {
                /**
                 * Illegal argument exception.
                 *
                 * @param varName var name
                 * @return illegal argument exception result
                 */
                throw new IllegalArgumentException("invalid variable name " + varName);
            }
            if (c == '=') {
                /**
                 * Illegal argument exception.
                 *
                 * @param varName var name
                 * @return illegal argument exception result
                 */
                throw new IllegalArgumentException("invalid variable name " + varName);
            }
            if (c == ':') {
                /**
                 * Illegal argument exception.
                 *
                 * @param varName var name
                 * @return illegal argument exception result
                 */
                throw new IllegalArgumentException("invalid variable name " + varName);
            }
        }
    }

    /**
     * Transform.
     *
     * @param reader reader
     * @param out out
     */
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
            String strippedLine = NStringUtils.strip(line);
            if (strippedLine.isEmpty()) {
                rows.add(new Row(RowType.EMPTY, null, line));
            } else if (strippedLine.startsWith("#") || strippedLine.startsWith("!")) {
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

    /**
     * Extract key.
     *
     * @param line line
     * @return extract key result
     */
    private String extractKey(String line) {
        if (line == null) {
            return null;
        }
        line = NStringUtils.strip(line);
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

    /**
     * New key val.
     *
     * @param key key
     * @param value value
     * @param rows rows
     * @return new key val result
     */
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

    /**
     * Process line.
     *
     * @param line line
     * @param rows rows
     * @return process line result
     */
    private boolean processLine(String line, List<Row> rows) {
        for (Map.Entry<String, Function<String, String>> e : replacements.entrySet()) {
            if (replaceVar(e.getKey(), e.getValue(), line, rows)) {
                return true;
            }
        }
        return false;
    }

    private enum RowType implements NEnum{
        KEY_VAL,
        EMPTY,
        COMMENT;
        private final String id;

      /**
       * Row type.
       */
        RowType() {
            this.id = NNameFormat.ID_NAME.format(name());
        }

        @Override
        public String id() {
            return id;
        }

        /**
         * Parse.
         *
         * @param value value
         * @return parse result
         */
        public static NOptional<RowType> parse(String value) {
            return NEnumUtils.parseEnum(value, RowType.class);
        }
    }

    private static class Row {
        List<Row> headers;
        String key;
        String row;
        RowType type;
        int index;

        /**
         * Row.
         *
         * @param type type
         * @param key key
         * @param row row
         * @return row result
         */
        public Row(RowType type, String key, String row) {
            this.type = type;
            this.key = key;
            this.row = row;
        }
    }

    /**
     * Replace var.
     *
     * @param varName var name
     * @param suffix suffix
     * @param line line
     * @param rows rows
     * @return replace var result
     */
    private boolean replaceVar(String varName, Function<String, String> suffix, String line, List<Row> rows) {
        String lineStripped = NStringUtils.strip(line);
        if (lineStripped.startsWith(varName)) {
            String ext = lineStripped.substring(varName.length());
            if (NStringUtils.strip(ext).startsWith("=")) {
                int e = line.indexOf('=');
                String oldValue = NStringUtils.stripLeft(ext.substring(1));
                String nv = suffix.apply(decodeString(oldValue));
                if (nv == null) {
                    //do nothing
                } else {
                    rows.add(newKeyVal(varName, line.substring(0, e + 1) + encodeValue(nv), rows));
                }
                return true;
            } else if (NStringUtils.strip(ext).startsWith(":")) {
                int e = line.indexOf(':');
                String oldValue = NStringUtils.stripLeft(ext.substring(1));
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

    /**
     * Decode string.
     *
     * @param str str
     * @return decode string result
     */
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
                            /**
                             * Illegal argument exception.
                             *
                             * @param encoding" encoding"
                             * @return illegal argument exception result
                             */
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

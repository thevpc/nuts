package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElementType;

import java.util.*;
import java.util.function.Predicate;

/**
 * NToStringBuilder class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NToStringBuilder {

    private String name;
    private List<Map.Entry<String, Object>> str = new ArrayList<>();
    private int rowSize = 60;
    private String indentString = "    ";

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    public static NToStringBuilder of() {
        return new NToStringBuilder();
    }

    /**
     * Creates a new instance of of.
     *
     * @param name name
     * @return of result
     */
    public static NToStringBuilder of(String name) {
        return new NToStringBuilder(name);
    }

    /**
     * N to string builder.
     *
     * @return n to string builder result
     */
    public NToStringBuilder() {
    }

    /**
     * N to string builder.
     *
     * @param name name
     * @return n to string builder result
     */
    public NToStringBuilder(String name) {
        this.name = NStringUtils.stripToNull(name);
    }

    /**
     * Row size.
     *
     * @param rowSize row size
     * @return row size result
     */
    public NToStringBuilder rowSize(int rowSize) {
        this.rowSize = rowSize <= 1 ? 1 : rowSize;
        return this;
    }

    /**
     * Indent string.
     *
     * @param indentString indent string
     * @return indent string result
     */
    public NToStringBuilder indentString(String indentString) {
        this.indentString = indentString == null ? "" : indentString;
        return this;
    }

    /**
     * Build entry.
     *
     * @param k k
     * @param value value
     * @return build entry result
     */
    private NStringBox buildEntry(String k, Object value) {
        int len = 0;
        String ss = value.toString();
        len += k.length();
        len += 3;
        len += ss.length();
        if (len < rowSize) {
            return new NStringBox(k + " : " + ss);
        } else {
            return new NStringBox(k + " : \n" + indentString + ss);
        }
    }

    /**
     * Adds the specified if non blank.
     *
     * @param key key
     * @param value value
     * @return add if non blank result
     */
    public NToStringBuilder addIfNonBlank(String key, Object value) {
        /**
         * Adds the specified if.
         *
         * @param key key
         * @param value value
         * @param NBlankable::isNonBlank n blankable::is non blank
         * @return add if result
         */
        return addIf(key, value, NBlankable::isNonBlank);
    }

    /**
     * Adds the specified if non empty.
     *
     * @param key key
     * @param value value
     * @return add if non empty result
     */
    public NToStringBuilder addIfNonEmpty(String key, String value) {
        /**
         * Adds the specified if.
         *
         * @param key key
         * @param value value
         * @param NStringUtils.isEmpty(value) n string utils.is empty(value)
         * @return add if result
         */
        return addIf(key, value, v -> NStringUtils.isEmpty(value));
    }

    /**
     * Adds the specified if non empty.
     *
     * @param key key
     * @param value value
     * @return add if non empty result
     */
    public NToStringBuilder addIfNonEmpty(String key, Collection<?> value) {
        /**
         * Adds the specified if.
         *
         * @param key key
         * @param value value
         * @param !v.isEmpty() !v.is empty()
         * @return add if result
         */
        return addIf(key, value, v -> v != null && !v.isEmpty());
    }

    /**
     * Adds the specified if non empty.
     *
     * @param key key
     * @param value value
     * @return add if non empty result
     */
    public NToStringBuilder addIfNonEmpty(String key, Map<?, ?> value) {
        /**
         * Adds the specified if.
         *
         * @param key key
         * @param value value
         * @param !v.isEmpty() !v.is empty()
         * @return add if result
         */
        return addIf(key, value, v -> v != null && !v.isEmpty());
    }

    /**
     * Adds the specified if non null.
     *
     * @param key key
     * @param value value
     * @return add if non null result
     */
    public NToStringBuilder addIfNonNull(String key, Object value) {
        /**
         * Adds the specified if.
         *
         * @param key key
         * @param value value
         * @param Objects::nonNull objects::non null
         * @return add if result
         */
        return addIf(key, value, Objects::nonNull);
    }

    /**
     * Adds the specified if.
     *
     * @param key key
     * @param value value
     * @param condition condition
     * @return add if result
     */
    public <T> NToStringBuilder addIf(String key, T value, Predicate<T> condition) {
        if (condition == null || condition.test(value)) {
          /**
           * Adds add.
           *
           * @param key key
           * @param value value
           */
            add(key, value);
        }
        return this;
    }

    /**
     * Adds add.
     *
     * @param key key
     * @param value value
     * @return add result
     */
    public NToStringBuilder add(String key, Object value) {
        String className = value == null ? "null" : value.getClass().getName();
        switch (className) {
            case "null":
                str.add(new AbstractMap.SimpleEntry<>(key, "null"));
                break;
            case "java.lang.String":
              /**
               * Adds add.
               *
               * @param key key
               * @param value).toString() value).to string()
               */
                add(key, ((CharSequence) value).toString());
                break;
            case "double[]":
                str.add(new AbstractMap.SimpleEntry<>(key, Arrays.toString((double[]) value)));
                break;
            case "boolean[]":
                str.add(new AbstractMap.SimpleEntry<>(key, Arrays.toString((boolean[]) value)));
                break;
            case "char[]":
                str.add(new AbstractMap.SimpleEntry<>(key, Arrays.toString((char[]) value)));
                break;
            case "byte[]":
                str.add(new AbstractMap.SimpleEntry<>(key, Arrays.toString((byte[]) value)));
                break;
            case "short[]":
                str.add(new AbstractMap.SimpleEntry<>(key, Arrays.toString((short[]) value)));
                break;
            case "int[]":
                str.add(new AbstractMap.SimpleEntry<>(key, Arrays.toString((int[]) value)));
                break;
            case "long[]":
                str.add(new AbstractMap.SimpleEntry<>(key, Arrays.toString((long[]) value)));
                break;
            case "float[]":
                str.add(new AbstractMap.SimpleEntry<>(key, Arrays.toString((float[]) value)));
                break;
            default: {
                if (value instanceof CharSequence) {
                    /**
                     * Adds add.
                     *
                     * @param key key
                     * @param value).toString() value).to string()
                     * @return add result
                     */
                    return add(key, ((CharSequence) value).toString());
                }
                if (value instanceof Object[]) {
                    str.add(new AbstractMap.SimpleEntry<>(key, Arrays.deepToString((Object[]) value)));
                    return this;
                }
                str.add(new AbstractMap.SimpleEntry<>(key, value.toString()));
                break;
            }
        }
        return this;
    }

    /**
     * Adds add.
     *
     * @param key key
     * @param value value
     * @return add result
     */
    public NToStringBuilder add(String key, String value) {
        str.add(new AbstractMap.SimpleEntry<>(key, value == null ? "null" : NStringUtils.formatStringLiteral(value, NElementType.DOUBLE_QUOTED_STRING)));
        return this;
    }

    /**
     * Build.
     *
     * @return build result
     */
    public String build() {
        if (str.isEmpty()) {
            if (name == null) {
                return "{}";
            } else {
                return name + "{}";
            }
        }
        String indent = "   ";
        NStringBuilder sb = new NStringBuilder(name == null ? 20 : name.length() + 20);
        if (name != null) {
            sb.append(name);
        }
        sb.append("{");
        int count = 0;
        Map<String, String> str2 = new LinkedHashMap<>();
        List<NStringBox> rows = new ArrayList<>();
        boolean multiLine = false;
        int cols = 0;
        for (Map.Entry<String, Object> e : str) {
            NStringBox t = buildEntry(e.getKey(), e.getValue());
            cols += t.columns();
            rows.add(t);
            if (!multiLine && t.rows() > 1) {
                multiLine = true;
            }
        }
        if (!multiLine && cols < rowSize) {
            for (int i = 0; i < rows.size(); i++) {
                NStringBox row = rows.get(i);
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(row.value());
            }
        } else {
            for (int i = 0; i < rows.size(); i++) {
                NStringBox row = rows.get(i);
                sb.append(new NStringBuilder(row.value()).indent(indent));
                if (i + 1 < rows.size()) {
                    sb.append(",\n");
                } else {
                    sb.append("\n");
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Builds and returns string representation.
     *
     * @return formatted string
     */
    @Override
    public String toString() {
        return build();
    }

    /**
     * Name.
     *
     * @return name result
     */
    public String name() {
        return name;
    }

    /**
     * Row size.
     *
     * @return row size result
     */
    public int rowSize() {
        return rowSize;
    }

    /**
     * Indent string.
     *
     * @return indent string result
     */
    public String indentString() {
        return indentString;
    }
}

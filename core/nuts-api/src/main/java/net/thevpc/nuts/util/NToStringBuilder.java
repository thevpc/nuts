package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElementType;

import java.util.*;
import java.util.function.Predicate;

public class NToStringBuilder {

    private String name;
    private List<Map.Entry<String, Object>> str = new ArrayList<>();
    private int rowSize = 60;
    private String indentString = "    ";

    public static final NToStringBuilder of() {
        return new NToStringBuilder();
    }

    public static final NToStringBuilder of(String name) {
        return new NToStringBuilder(name);
    }

    public NToStringBuilder() {
    }

    public NToStringBuilder(String name) {
        this.name = NStringUtils.stripToNull(name);
    }

    public NToStringBuilder rowSize(int rowSize) {
        this.rowSize = rowSize <= 1 ? 1 : rowSize;
        return this;
    }

    public NToStringBuilder indentString(String indentString) {
        this.indentString = indentString == null ? "" : indentString;
        return this;
    }

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

    public NToStringBuilder addIfNonBlank(String key, Object value) {
        return addIf(key, value, NBlankable::isNonBlank);
    }

    public NToStringBuilder addIfNonEmpty(String key, String value) {
        return addIf(key, value, v -> NStringUtils.isEmpty(value));
    }

    public NToStringBuilder addIfNonEmpty(String key, Collection<?> value) {
        return addIf(key, value, v -> v != null && !v.isEmpty());
    }

    public NToStringBuilder addIfNonEmpty(String key, Map<?, ?> value) {
        return addIf(key, value, v -> v != null && !v.isEmpty());
    }

    public NToStringBuilder addIfNonNull(String key, Object value) {
        return addIf(key, value, Objects::nonNull);
    }

    public <T> NToStringBuilder addIf(String key, T value, Predicate<T> condition) {
        if (condition == null || condition.test(value)) {
            add(key, value);
        }
        return this;
    }

    public NToStringBuilder add(String key, Object value) {
        String className = value == null ? "null" : value.getClass().getName();
        switch (className) {
            case "null":
                str.add(new AbstractMap.SimpleEntry<>(key, "null"));
                break;
            case "java.lang.String":
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

    public NToStringBuilder add(String key, String value) {
        str.add(new AbstractMap.SimpleEntry<>(key, value == null ? "null" : NStringUtils.formatStringLiteral(value, NElementType.DOUBLE_QUOTED_STRING)));
        return this;
    }

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

    public String toString() {
        return build();
    }

    public String name() {
        return name;
    }

    public int rowSize() {
        return rowSize;
    }

    public String indentString() {
        return indentString;
    }
}

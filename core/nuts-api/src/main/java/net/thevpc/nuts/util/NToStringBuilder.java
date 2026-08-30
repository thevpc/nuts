package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElementType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Fluent builder for creating structured, readable, and configurable {@code toString()} representations of objects.
 * Supports auto-wrapping between single-line and multi-line modes, nested builders, primitive arrays,
 * optional values, conditional field inclusions, string literal formatting, and custom separators.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NToStringBuilder {

    private String name;
    private List<Entry> entries;
    private int rowSize = 60;
    private String indentString = "    ";
    private String separator = " : ";
    private Boolean multiLine;
    private boolean omitNulls;
    private boolean omitBlanks;
    private boolean omitEmpty;
    private boolean omitProcessingSuppliers;
    private boolean quoteStrings = true;

    /**
     * Creates a new anonymous {@code NToStringBuilder}.
     *
     * @return a new {@code NToStringBuilder} instance
     */
    public static NToStringBuilder of() {
        return new NToStringBuilder();
    }

    /**
     * Creates a new {@code NToStringBuilder} with the given type/class name.
     *
     * @param name object or type name to prefix the output with
     * @return a new {@code NToStringBuilder} instance
     */
    public static NToStringBuilder of(String name) {
        return new NToStringBuilder(name);
    }

    /**
     * Creates a new {@code NToStringBuilder} using the resolved class name of the specified object or class.
     * Correctly handles member nested classes (e.g. {@code "Outer.Inner"}), anonymous inner classes,
     * and array classes.
     *
     * @param obj object whose class name to use, or {@link Class} instance
     * @return a new {@code NToStringBuilder} instance
     */
    public static NToStringBuilder of(Object obj) {
        if (obj == null) {
            return new NToStringBuilder();
        }
        if (obj instanceof Class) {
            return new NToStringBuilder(resolveClassName((Class<?>) obj));
        }
        return new NToStringBuilder(resolveClassName(obj.getClass()));
    }

    /**
     * Creates a new {@code NToStringBuilder} using the resolved class name of the given class.
     * Correctly handles member nested classes (e.g. {@code "Outer.Inner"}), anonymous inner classes,
     * and array classes.
     *
     * @param clazz class whose name to use
     * @return a new {@code NToStringBuilder} instance
     */
    public static NToStringBuilder of(Class<?> clazz) {
        return new NToStringBuilder(resolveClassName(clazz));
    }

    /**
     * Resolves a clean, human-readable name for any class, handling top-level classes,
     * member nested/inner classes (e.g. {@code "Outer.Inner"}), local classes,
     * anonymous inner classes (e.g. {@code "Outer$anonymous"}), and array classes (e.g. {@code "String[]"}).
     *
     * @param clazz the class to resolve
     * @return resolved readable class name, or {@code null} if null
     */
    public static String resolveClassName(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        if (clazz.isArray()) {
            return resolveClassName(clazz.getComponentType()) + "[]";
        }
        String simpleName = clazz.getSimpleName();
        if (simpleName.isEmpty()) {
            Class<?> enclosing = clazz.getEnclosingClass();
            if (enclosing != null) {
                return resolveClassName(enclosing) + "$anonymous";
            }
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass != Object.class) {
                return resolveClassName(superclass) + "$anonymous";
            }
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces.length > 0) {
                return resolveClassName(interfaces[0]) + "$anonymous";
            }
            return "anonymous";
        }
        Class<?> enclosing = clazz.getEnclosingClass();
        if (enclosing != null) {
            return resolveClassName(enclosing) + "." + simpleName;
        }
        return simpleName;
    }

    /**
     * Creates a new anonymous {@code NToStringBuilder}.
     */
    public NToStringBuilder() {
    }

    /**
     * Creates a new {@code NToStringBuilder} with the specified name.
     *
     * @param name object or type name
     */
    public NToStringBuilder(String name) {
        this.name = NStringUtils.stripToNull(name);
    }

    /**
     * Sets the type or object name.
     *
     * @param name object or type name
     * @return this instance
     */
    public NToStringBuilder name(String name) {
        this.name = NStringUtils.stripToNull(name);
        return this;
    }

    /**
     * Sets the type or object name.
     *
     * @param name object or type name
     * @return this instance
     */
    public NToStringBuilder setName(String name) {
        return name(name);
    }

    /**
     * Returns the configured object or type name.
     *
     * @return the configured name, or {@code null} if anonymous
     */
    public String name() {
        return name;
    }

    /**
     * Returns the configured object or type name.
     *
     * @return the configured name, or {@code null} if anonymous
     */
    public String getName() {
        return name();
    }

    /**
     * Sets the maximum column width before switching to multi-line mode in auto mode.
     *
     * @param rowSize maximum row size in characters (minimum 1)
     * @return this instance
     */
    public NToStringBuilder rowSize(int rowSize) {
        this.rowSize = rowSize <= 1 ? 1 : rowSize;
        return this;
    }

    /**
     * Sets the maximum column width before switching to multi-line mode in auto mode.
     *
     * @param rowSize maximum row size in characters (minimum 1)
     * @return this instance
     */
    public NToStringBuilder setRowSize(int rowSize) {
        return rowSize(rowSize);
    }

    /**
     * Returns the maximum column width.
     *
     * @return row size in characters
     */
    public int rowSize() {
        return rowSize;
    }

    /**
     * Returns the maximum column width.
     *
     * @return row size in characters
     */
    public int getRowSize() {
        return rowSize();
    }

    /**
     * Sets the indentation string used for multi-line formatting.
     *
     * @param indentString indentation string (e.g. {@code "    "} or {@code "\t"})
     * @return this instance
     */
    public NToStringBuilder indentString(String indentString) {
        this.indentString = indentString == null ? "" : indentString;
        return this;
    }

    /**
     * Sets the indentation string used for multi-line formatting.
     *
     * @param indentString indentation string
     * @return this instance
     */
    public NToStringBuilder setIndentString(String indentString) {
        return indentString(indentString);
    }

    /**
     * Returns the indentation string.
     *
     * @return indentation string
     */
    public String indentString() {
        return indentString;
    }

    /**
     * Returns the indentation string.
     *
     * @return indentation string
     */
    public String getIndentString() {
        return indentString();
    }

    /**
     * Sets the key-value separator string (e.g. {@code " : "} or {@code "="}).
     *
     * @param separator key-value separator
     * @return this instance
     */
    public NToStringBuilder separator(String separator) {
        this.separator = separator == null ? "" : separator;
        return this;
    }

    /**
     * Sets the key-value separator string.
     *
     * @param separator key-value separator
     * @return this instance
     */
    public NToStringBuilder setSeparator(String separator) {
        return separator(separator);
    }

    /**
     * Returns the key-value separator string.
     *
     * @return separator string
     */
    public String separator() {
        return separator;
    }

    /**
     * Returns the key-value separator string.
     *
     * @return separator string
     */
    public String getSeparator() {
        return separator();
    }

    /**
     * Explicitly enables or disables multi-line mode, or sets it to auto ({@code null}).
     *
     * @param multiLine {@code Boolean.TRUE} for multi-line, {@code Boolean.FALSE} for single-line, {@code null} for auto
     * @return this instance
     */
    public NToStringBuilder multiLine(Boolean multiLine) {
        this.multiLine = multiLine;
        return this;
    }

    /**
     * Explicitly enables or disables multi-line mode.
     *
     * @param multiLine {@code Boolean.TRUE} for multi-line, {@code Boolean.FALSE} for single-line, {@code null} for auto
     * @return this instance
     */
    public NToStringBuilder setMultiLine(Boolean multiLine) {
        return multiLine(multiLine);
    }

    /**
     * Returns the multi-line mode setting.
     *
     * @return {@code Boolean.TRUE}, {@code Boolean.FALSE}, or {@code null} if auto
     */
    public Boolean multiLine() {
        return multiLine;
    }

    /**
     * Returns the multi-line mode setting.
     *
     * @return {@code Boolean.TRUE}, {@code Boolean.FALSE}, or {@code null} if auto
     */
    public Boolean isMultiLine() {
        return multiLine();
    }

    /**
     * Forces single-line formatting.
     *
     * @return this instance
     */
    public NToStringBuilder singleLine() {
        return multiLine(Boolean.FALSE);
    }

    /**
     * Configures compact single-line formatting with {@code "="} separator.
     *
     * @return this instance
     */
    public NToStringBuilder compact() {
        return multiLine(Boolean.FALSE).separator("=").quoteStrings(false);
    }

    /**
     * Sets whether to automatically omit null values across all {@code add} calls.
     *
     * @param omitNulls {@code true} to omit null values
     * @return this instance
     */
    public NToStringBuilder omitNulls(boolean omitNulls) {
        this.omitNulls = omitNulls;
        return this;
    }

    /**
     * Sets whether to automatically omit processing suppliers {@code add} calls.
     *
     * @param omitProcessingSuppliers {@code true} to omit processing suppliers
     * @return this instance
     */
    public NToStringBuilder omitProcessingSuppliers(boolean omitProcessingSuppliers) {
        this.omitProcessingSuppliers = omitProcessingSuppliers;
        return this;
    }

    /**
     * Sets whether to automatically omit null values across all {@code add} calls.
     *
     * @param omitNulls {@code true} to omit null values
     * @return this instance
     */
    public NToStringBuilder setOmitNulls(boolean omitNulls) {
        return omitNulls(omitNulls);
    }

    /**
     * Returns whether null values are omitted.
     *
     * @return {@code true} if null values are omitted
     */
    public boolean omitNulls() {
        return omitNulls;
    }

    /**
     * Returns whether null values are omitted.
     *
     * @return {@code true} if null values are omitted
     */
    public boolean isOmitNulls() {
        return omitNulls();
    }

    /**
     * Sets whether to automatically omit blank values (strings containing only whitespace, empty/blank objects) across all {@code add} calls.
     *
     * @param omitBlanks {@code true} to omit blank values
     * @return this instance
     */
    public NToStringBuilder omitBlanks(boolean omitBlanks) {
        this.omitBlanks = omitBlanks;
        return this;
    }

    /**
     * Sets whether to automatically omit blank values across all {@code add} calls.
     *
     * @param omitBlanks {@code true} to omit blank values
     * @return this instance
     */
    public NToStringBuilder setOmitBlanks(boolean omitBlanks) {
        return omitBlanks(omitBlanks);
    }

    /**
     * Returns whether blank values are omitted.
     *
     * @return {@code true} if blank values are omitted
     */
    public boolean omitBlanks() {
        return omitBlanks;
    }

    /**
     * Returns whether blank values are omitted.
     *
     * @return {@code true} if blank values are omitted
     */
    public boolean isOmitBlanks() {
        return omitBlanks();
    }

    /**
     * Sets whether to automatically omit empty values (empty strings, collections, maps, arrays, optionals) across all {@code add} calls.
     *
     * @param omitEmpty {@code true} to omit empty values
     * @return this instance
     */
    public NToStringBuilder omitEmpty(boolean omitEmpty) {
        this.omitEmpty = omitEmpty;
        return this;
    }

    /**
     * Sets whether to automatically omit empty values across all {@code add} calls.
     *
     * @param omitEmpty {@code true} to omit empty values
     * @return this instance
     */
    public NToStringBuilder setOmitEmpty(boolean omitEmpty) {
        return omitEmpty(omitEmpty);
    }

    /**
     * Returns whether empty values are omitted.
     *
     * @return {@code true} if empty values are omitted
     */
    public boolean omitEmpty() {
        return omitEmpty;
    }

    /**
     * Returns whether empty values are omitted.
     *
     * @return {@code true} if empty values are omitted
     */
    public boolean isOmitEmpty() {
        return omitEmpty();
    }

    /**
     * Sets whether string values are formatted as double-quoted string literals.
     *
     * @param quoteStrings {@code true} to quote string values
     * @return this instance
     */
    public NToStringBuilder quoteStrings(boolean quoteStrings) {
        this.quoteStrings = quoteStrings;
        return this;
    }

    /**
     * Sets whether string values are formatted as double-quoted string literals.
     *
     * @param quoteStrings {@code true} to quote string values
     * @return this instance
     */
    public NToStringBuilder setQuoteStrings(boolean quoteStrings) {
        return quoteStrings(quoteStrings);
    }

    /**
     * Returns whether string values are quoted.
     *
     * @return {@code true} if string values are quoted
     */
    public boolean quoteStrings() {
        return quoteStrings;
    }

    /**
     * Returns whether string values are quoted.
     *
     * @return {@code true} if string values are quoted
     */
    public boolean isQuoteStrings() {
        return quoteStrings();
    }

    /**
     * Clears all added entries.
     *
     * @return this instance
     */
    public NToStringBuilder clear() {
        if (this.entries != null) {
            this.entries.clear();
        }
        return this;
    }

    /**
     * Returns whether no entries have been added.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return entries == null || entries.isEmpty();
    }

    /**
     * Returns the number of entries currently added.
     *
     * @return number of entries
     */
    public int size() {
        return entries == null ? 0 : entries.size();
    }

    // =========================================================================
    // Add Methods
    // =========================================================================

    private void addEntry(Entry entry) {
        if (entries == null) {
            entries = new ArrayList<>(8);
        }
        entries.add(entry);
    }

    /**
     * Adds an entry with the specified key and value.
     *
     * @param key   field name / key
     * @param value field value
     * @return this instance
     */
    public NToStringBuilder add(String key, Object value) {
        if (omitNulls && value == null) {
            return this;
        }
        if (omitBlanks && NBlankable.isBlank(value)) {
            return this;
        }
        if (omitEmpty && isObjectEmpty(value)) {
            return this;
        }
        addEntry(new Entry(key, value, false));
        return this;
    }

    /**
     * Adds an entry with the specified key and string value.
     *
     * @param key   field name / key
     * @param value string value
     * @return this instance
     */
    public NToStringBuilder add(String key, String value) {
        if (omitNulls && value == null) {
            return this;
        }
        if (omitBlanks && NBlankable.isBlank(value)) {
            return this;
        }
        if (omitEmpty && (value == null || value.isEmpty())) {
            return this;
        }
        addEntry(new Entry(key, value, false));
        return this;
    }

    /**
     * Adds an entry without string quoting/literal escaping.
     *
     * @param key   field name / key
     * @param value raw value
     * @return this instance
     */
    public NToStringBuilder addRaw(String key, Object value) {
        if (omitNulls && value == null) {
            return this;
        }
        if (omitBlanks && NBlankable.isBlank(value)) {
            return this;
        }
        if (omitEmpty && isObjectEmpty(value)) {
            return this;
        }
        addEntry(new Entry(key, value, true));
        return this;
    }

    /**
     * Adds an unnamed value entry.
     *
     * @param value value
     * @return this instance
     */
    public NToStringBuilder add(Object value) {
        return add(null, value);
    }

    /**
     * Adds an unnamed raw value entry.
     *
     * @param value raw value
     * @return this instance
     */
    public NToStringBuilder addRaw(Object value) {
        return addRaw(null, value);
    }

    /**
     * Adds a boolean primitive field.
     *
     * @param key   field name
     * @param value boolean value
     * @return this instance
     */
    public NToStringBuilder add(String key, boolean value) {
        return addRaw(key, value);
    }

    /**
     * Adds a byte primitive field.
     *
     * @param key   field name
     * @param value byte value
     * @return this instance
     */
    public NToStringBuilder add(String key, byte value) {
        return addRaw(key, value);
    }

    /**
     * Adds a short primitive field.
     *
     * @param key   field name
     * @param value short value
     * @return this instance
     */
    public NToStringBuilder add(String key, short value) {
        return addRaw(key, value);
    }

    /**
     * Adds a char primitive field.
     *
     * @param key   field name
     * @param value char value
     * @return this instance
     */
    public NToStringBuilder add(String key, char value) {
        return add(key, String.valueOf(value));
    }

    /**
     * Adds an int primitive field.
     *
     * @param key   field name
     * @param value int value
     * @return this instance
     */
    public NToStringBuilder add(String key, int value) {
        return addRaw(key, value);
    }

    /**
     * Adds a long primitive field.
     *
     * @param key   field name
     * @param value long value
     * @return this instance
     */
    public NToStringBuilder add(String key, long value) {
        return addRaw(key, value);
    }

    /**
     * Adds a float primitive field.
     *
     * @param key   field name
     * @param value float value
     * @return this instance
     */
    public NToStringBuilder add(String key, float value) {
        return addRaw(key, value);
    }

    /**
     * Adds a double primitive field.
     *
     * @param key   field name
     * @param value double value
     * @return this instance
     */
    public NToStringBuilder add(String key, double value) {
        return addRaw(key, value);
    }

    /**
     * Adds all entries from the specified map.
     *
     * @param map map of entries to add
     * @return this instance
     */
    public NToStringBuilder addAll(Map<String, ?> map) {
        if (map != null) {
            for (Map.Entry<String, ?> e : map.entrySet()) {
                add(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    /**
     * Adds all entries from another {@code NToStringBuilder}.
     *
     * @param other other builder
     * @return this instance
     */
    public NToStringBuilder addAll(NToStringBuilder other) {
        if (other != null && other.entries != null) {
            if (this.entries == null) {
                this.entries = new ArrayList<>(other.entries.size());
            }
            this.entries.addAll(other.entries);
        }
        return this;
    }

    // =========================================================================
    // Conditional Add Methods
    // =========================================================================

    /**
     * Adds the entry if the given boolean condition is true.
     *
     * @param condition condition to check
     * @param key       field name
     * @param value     field value
     * @return this instance
     */
    public NToStringBuilder addIf(boolean condition, String key, Object value) {
        if (condition) {
            add(key, value);
        }
        return this;
    }

    /**
     * Adds the entry with lazily evaluated value supplier if condition is true.
     *
     * @param condition     condition to check
     * @param key           field name
     * @param valueSupplier supplier for the field value
     * @return this instance
     */
    public NToStringBuilder addIf(boolean condition, String key, Supplier<?> valueSupplier) {
        if (condition && valueSupplier != null) {
            add(key, valueSupplier.get());
        }
        return this;
    }

    /**
     * Adds the entry if the specified predicate tests true against the value.
     *
     * @param key       field name
     * @param value     field value
     * @param condition predicate condition
     * @param <T>       value type
     * @return this instance
     */
    public <T> NToStringBuilder addIf(String key, T value, Predicate<T> condition) {
        if (condition == null || condition.test(value)) {
            add(key, value);
        }
        return this;
    }

    /**
     * Adds the entry with a mapped value.
     *
     * @param key    field name
     * @param value  source value
     * @param mapper mapping function
     * @param <T>    source value type
     * @param <V>    target value type
     * @return this instance
     */
    public <T, V> NToStringBuilder addMapped(String key, T value, Function<T, V> mapper) {
        add(key, mapper == null ? value : mapper.apply(value));
        return this;
    }

    /**
     * Adds the entry with a mapped value if the predicate tests true on the source value.
     *
     * @param key       field name
     * @param value     source value
     * @param mapper    mapping function
     * @param condition predicate condition
     * @param <T>       source value type
     * @param <V>       target value type
     * @return this instance
     */
    public <T, V> NToStringBuilder addMappedIf(String key, T value, Function<T, V> mapper, Predicate<T> condition) {
        if (condition == null || condition.test(value)) {
            add(key, mapper == null ? value : mapper.apply(value));
        }
        return this;
    }

    /**
     * Adds the entry if the value is non-null.
     *
     * @param key   field name
     * @param value field value
     * @return this instance
     */
    public NToStringBuilder addIfNonNull(String key, Object value) {
        return addIf(key, value, Objects::nonNull);
    }

    /**
     * Adds the entry with lazily evaluated value supplier if the returned value is non-null.
     *
     * @param key           field name
     * @param valueSupplier supplier for the value
     * @return this instance
     */
    public NToStringBuilder addIfNonNull(String key, Supplier<?> valueSupplier) {
        if (valueSupplier != null) {
            Object val = valueSupplier.get();
            if (val != null) {
                add(key, val);
            }
        }
        return this;
    }

    /**
     * Adds the entry if the value is non-blank (via {@link NBlankable#isNonBlank(Object)}).
     *
     * @param key   field name
     * @param value field value
     * @return this instance
     */
    public NToStringBuilder addIfNonBlank(String key, Object value) {
        return addIf(key, value, NBlankable::isNonBlank);
    }

    /**
     * Adds the entry if the string value is non-blank (not null and contains non-whitespace).
     *
     * @param key   field name
     * @param value string value
     * @return this instance
     */
    public NToStringBuilder addIfNonBlank(String key, String value) {
        return addIf(key, value, NBlankable::isNonBlank);
    }

    /**
     * Adds the entry if the string value is non-empty (not null and length &gt; 0).
     *
     * @param key   field name
     * @param value string value
     * @return this instance
     */
    public NToStringBuilder addIfNonEmpty(String key, String value) {
        return addIf(key, value, v -> v != null && !v.isEmpty());
    }

    /**
     * Adds the entry if the collection is non-null and not empty.
     *
     * @param key   field name
     * @param value collection value
     * @return this instance
     */
    public NToStringBuilder addIfNonEmpty(String key, Collection<?> value) {
        return addIf(key, value, v -> v != null && !v.isEmpty());
    }

    /**
     * Adds the entry if the map is non-null and not empty.
     *
     * @param key   field name
     * @param value map value
     * @return this instance
     */
    public NToStringBuilder addIfNonEmpty(String key, Map<?, ?> value) {
        return addIf(key, value, v -> v != null && !v.isEmpty());
    }

    /**
     * Adds the entry if the array is non-null and not empty.
     *
     * @param key   field name
     * @param value array value
     * @return this instance
     */
    public NToStringBuilder addIfNonEmpty(String key, Object[] value) {
        return addIf(key, value, v -> v != null && v.length > 0);
    }

    /**
     * Adds the entry if the primitive array is non-null and not empty.
     *
     * @param key   field name
     * @param value array value
     * @return this instance
     */
    public NToStringBuilder addIfNonEmpty(String key, boolean[] value) {
        if (value != null && value.length > 0) {
            add(key, value);
        }
        return this;
    }

    /**
     * Adds the entry if the primitive array is non-null and not empty.
     *
     * @param key   field name
     * @param value array value
     * @return this instance
     */
    public NToStringBuilder addIfNonEmpty(String key, byte[] value) {
        if (value != null && value.length > 0) {
            add(key, value);
        }
        return this;
    }

    /**
     * Adds the entry if the primitive array is non-null and not empty.
     *
     * @param key   field name
     * @param value array value
     * @return this instance
     */
    public NToStringBuilder addIfNonEmpty(String key, short[] value) {
        if (value != null && value.length > 0) {
            add(key, value);
        }
        return this;
    }

    /**
     * Adds the entry if the primitive array is non-null and not empty.
     *
     * @param key   field name
     * @param value array value
     * @return this instance
     */
    public NToStringBuilder addIfNonEmpty(String key, char[] value) {
        if (value != null && value.length > 0) {
            add(key, value);
        }
        return this;
    }

    /**
     * Adds the entry if the primitive array is non-null and not empty.
     *
     * @param key   field name
     * @param value array value
     * @return this instance
     */
    public NToStringBuilder addIfNonEmpty(String key, int[] value) {
        if (value != null && value.length > 0) {
            add(key, value);
        }
        return this;
    }

    /**
     * Adds the entry if the primitive array is non-null and not empty.
     *
     * @param key   field name
     * @param value array value
     * @return this instance
     */
    public NToStringBuilder addIfNonEmpty(String key, long[] value) {
        if (value != null && value.length > 0) {
            add(key, value);
        }
        return this;
    }

    /**
     * Adds the entry if the primitive array is non-null and not empty.
     *
     * @param key   field name
     * @param value array value
     * @return this instance
     */
    public NToStringBuilder addIfNonEmpty(String key, float[] value) {
        if (value != null && value.length > 0) {
            add(key, value);
        }
        return this;
    }

    /**
     * Adds the entry if the primitive array is non-null and not empty.
     *
     * @param key   field name
     * @param value array value
     * @return this instance
     */
    public NToStringBuilder addIfNonEmpty(String key, double[] value) {
        if (value != null && value.length > 0) {
            add(key, value);
        }
        return this;
    }

    /**
     * Adds the entry if the {@link Optional} is non-null and present.
     *
     * @param key   field name
     * @param value optional value
     * @return this instance
     */
    public NToStringBuilder addIfNonEmpty(String key, Optional<?> value) {
        return addIf(key, value, v -> v != null && v.isPresent());
    }

    /**
     * Adds the entry if the {@link NOptional} is non-null and present.
     *
     * @param key   field name
     * @param value optional value
     * @return this instance
     */
    public NToStringBuilder addIfNonEmpty(String key, NOptional<?> value) {
        return addIf(key, value, v -> v != null && v.isPresent());
    }

    /**
     * Adds the entry if the value is not empty (string, collection, map, array, optional, blankable).
     *
     * @param key   field name
     * @param value object value
     * @return this instance
     */
    public NToStringBuilder addIfNonEmpty(String key, Object value) {
        return addIf(key, value, v -> !isObjectEmpty(v));
    }

    /**
     * Adds the boolean field only if it is {@code true}.
     *
     * @param key   field name
     * @param value boolean value
     * @return this instance
     */
    public NToStringBuilder addIfTrue(String key, boolean value) {
        if (value) {
            add(key, true);
        }
        return this;
    }

    /**
     * Adds the boolean field only if it is {@code false}.
     *
     * @param key   field name
     * @param value boolean value
     * @return this instance
     */
    public NToStringBuilder addIfFalse(String key, boolean value) {
        if (!value) {
            add(key, false);
        }
        return this;
    }

    /**
     * Adds the integer field only if it is non-zero.
     *
     * @param key   field name
     * @param value int value
     * @return this instance
     */
    public NToStringBuilder addIfNonZero(String key, int value) {
        if (value != 0) {
            add(key, value);
        }
        return this;
    }

    /**
     * Adds the long field only if it is non-zero.
     *
     * @param key   field name
     * @param value long value
     * @return this instance
     */
    public NToStringBuilder addIfNonZero(String key, long value) {
        if (value != 0L) {
            add(key, value);
        }
        return this;
    }

    /**
     * Adds the double field only if it is non-zero.
     *
     * @param key   field name
     * @param value double value
     * @return this instance
     */
    public NToStringBuilder addIfNonZero(String key, double value) {
        if (value != 0.0) {
            add(key, value);
        }
        return this;
    }

    // =========================================================================
    // Build and Formatting
    // =========================================================================

    /**
     * Builds and returns the formatted string representation.
     *
     * @return formatted string
     */
    public String build() {
        if (entries == null || entries.isEmpty()) {
            return (name == null) ? "{}" : name + "{}";
        }

        int size = entries.size();
        List<String> builtEntries = new ArrayList<>(size);
        boolean hasMultiLineEntry = false;
        int totalCols = (name == null ? 0 : name.length()) + 2; // name + {}

        for (int i = 0; i < size; i++) {
            Entry entry = entries.get(i);
            String entryStr = buildEntry(entry.key, entry.value, entry.raw);
            builtEntries.add(entryStr);
            if (entryStr.indexOf('\n') != -1) {
                hasMultiLineEntry = true;
            }
            totalCols += entryStr.length();
        }
        totalCols += Math.max(0, size - 1) * 2; // ", "

        boolean isMulti = (multiLine != null) ? multiLine : (hasMultiLineEntry || totalCols >= rowSize);

        StringBuilder sb = new StringBuilder(Math.max(64, totalCols + size * indentString.length() + 16));
        if (name != null) {
            sb.append(name);
        }

        if (!isMulti) {
            sb.append("{");
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(builtEntries.get(i));
            }
            sb.append("}");
        } else {
            sb.append("{\n");
            for (int i = 0; i < size; i++) {
                String entryStr = builtEntries.get(i);
                if (entryStr.indexOf('\n') == -1) {
                    sb.append(indentString).append(entryStr);
                } else {
                    sb.append(NStringUtils.indent(entryStr, indentString, false));
                }
                if (i + 1 < size) {
                    sb.append(",\n");
                } else {
                    sb.append("\n");
                }
            }
            sb.append("}");
        }

        return sb.toString();
    }

    /**
     * Formats a single entry key and value into a string.
     *
     * @param k     key (may be null)
     * @param value value object
     * @param raw   whether to format as raw
     * @return formatted entry string
     */
    private String buildEntry(String k, Object value, boolean raw) {
        String valStr = formatValue(value, raw);
        if (k == null || k.isEmpty()) {
            return valStr;
        }
        return k + separator + valStr;
    }

    /**
     * Formats a value object into its string representation according to the builder's configuration.
     *
     * @param value value object
     * @param raw   whether to bypass literal quoting
     * @return formatted string
     */
    private String formatValue(Object value, boolean raw) {
        if (value == null) {
            return "null";
        }
        if (raw) {
            return String.valueOf(value);
        }
        if (value instanceof CharSequence) {
            return formatString(value.toString());
        }
        if (value instanceof Character) {
            return formatString(String.valueOf(value));
        }
        if (value instanceof boolean[]) {
            return Arrays.toString((boolean[]) value);
        }
        if (value instanceof byte[]) {
            return Arrays.toString((byte[]) value);
        }
        if (value instanceof short[]) {
            return Arrays.toString((short[]) value);
        }
        if (value instanceof char[]) {
            return Arrays.toString((char[]) value);
        }
        if (value instanceof int[]) {
            return Arrays.toString((int[]) value);
        }
        if (value instanceof long[]) {
            return Arrays.toString((long[]) value);
        }
        if (value instanceof float[]) {
            return Arrays.toString((float[]) value);
        }
        if (value instanceof double[]) {
            return Arrays.toString((double[]) value);
        }
        if (value instanceof Object[]) {
            return Arrays.deepToString((Object[]) value);
        }
        if (value instanceof Optional) {
            Optional<?> opt = (Optional<?>) value;
            return opt.isPresent() ? formatValue(opt.get(), raw) : "Optional.empty";
        }
        if (value instanceof NOptional) {
            NOptional<?> opt = (NOptional<?>) value;
            if (opt.isPresent()) {
                return formatValue(opt.get(), raw);
            }
            if (opt.isError()) {
                return "NOptional.error";
            }
            return "NOptional.empty";
        }
        if (!omitProcessingSuppliers && value instanceof Supplier) {
            return formatValue(((Supplier<?>) value).get(), raw);
        }
        return String.valueOf(value);
    }

    /**
     * Fast-path string quoting.
     *
     * @param str string to quote
     * @return formatted quoted or unquoted string
     */
    private String formatString(String str) {
        if (str == null) {
            return "null";
        }
        if (!quoteStrings) {
            return str;
        }
        int len = str.length();
        boolean needsEscaping = false;
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c == '"' || c == '\\' || c == '\n' || c == '\r' || c == '\t' || c < 32) {
                needsEscaping = true;
                break;
            }
        }
        if (!needsEscaping) {
            return "\"" + str + "\"";
        }
        return NStringUtils.formatStringLiteral(str, NElementType.DOUBLE_QUOTED_STRING);
    }

    /**
     * Helper to test if an object is considered empty.
     *
     * @param value value to test
     * @return {@code true} if null or empty
     */
    private static boolean isObjectEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length() == 0;
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        }
        if (value instanceof Object[]) {
            return ((Object[]) value).length == 0;
        }
        if (value instanceof boolean[]) return ((boolean[]) value).length == 0;
        if (value instanceof byte[]) return ((byte[]) value).length == 0;
        if (value instanceof short[]) return ((short[]) value).length == 0;
        if (value instanceof char[]) return ((char[]) value).length == 0;
        if (value instanceof int[]) return ((int[]) value).length == 0;
        if (value instanceof long[]) return ((long[]) value).length == 0;
        if (value instanceof float[]) return ((float[]) value).length == 0;
        if (value instanceof double[]) return ((double[]) value).length == 0;
        if (value instanceof Optional) {
            return !((Optional<?>) value).isPresent();
        }
        if (value instanceof NOptional) {
            return !((NOptional<?>) value).isPresent();
        }
        if (value instanceof NBlankable) {
            return ((NBlankable) value).isBlank();
        }
        return false;
    }

    /**
     * Builds and returns the formatted string representation.
     *
     * @return formatted string
     */
    @Override
    public String toString() {
        return build();
    }

    // =========================================================================
    // Internal Entry Model
    // =========================================================================

    private static class Entry {
        final String key;
        final Object value;
        final boolean raw;

        Entry(String key, Object value, boolean raw) {
            this.key = key;
            this.value = value;
            this.raw = raw;
        }
    }
}

package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

import java.util.*;

/**
 * NOpenEnum class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public abstract class NOpenEnum implements NEnum{
    private static final Map<Class<?>, List<NOpenEnum>> VALUES_REGISTRY = new LinkedHashMap<>();
    private static final Map<Class<?>, Map<String, NOpenEnum>> ID_REGISTRY = new LinkedHashMap<>();
    private final String id;
    private final String name;
    private final int ordinal;
    /**
     * Values.
     *
     * @param type type
     * @return values result
     */
    public static <T extends NOpenEnum> List<T> values(Class<T> type) {
        return (List<T>) Collections.unmodifiableList(
                VALUES_REGISTRY.getOrDefault(type, Collections.emptyList())
        );
    }

    /**
     * Parse.
     *
     * @param type type
     * @param value value
     * @return parse result
     */
    public static <T extends NOpenEnum> NOptional<T> parse(Class<T> type, String value) {
        return NEnumUtils.parseEnum(value, type,values(type), null);
    }

    /**
     * N open enum.
     *
     * @param id id
     * @return n open enum result
     */
    protected NOpenEnum(String id) {
        this.id = NAssert.requireNamedNonNull(id, "id");
        this.name = NNameFormat.CONST_NAME.format(id);  // mirrors Java enum name() convention
        Map<String, NOpenEnum> byId = ID_REGISTRY.computeIfAbsent(getClass(), k -> new LinkedHashMap<>());
        if (byId.containsKey(id)) {
            /**
             * N illegal argument exception.
             *
             * @param %s",id,getClass().getSimpleName()) %s",id,get class().get simple name())
             * @return n illegal argument exception result
             */
            throw new NIllegalArgumentException(NMsg.ofC("duplicate id '%s' in %s",id,getClass().getSimpleName()));
        }
        List<NOpenEnum> valuesList = VALUES_REGISTRY.computeIfAbsent(getClass(), k -> new ArrayList<>());
        this.ordinal = valuesList.size();  // assigned at registration time, like Java enum
        byId.put(id, this);
        VALUES_REGISTRY.computeIfAbsent(getClass(), k -> new ArrayList<>()).add(this);
    }

    @Override
    public String id() { return id; }

    /**
     * Name.
     *
     * @return name result
     */
    public String name() { return name; }

    /**
     * Ordinal.
     *
     * @return ordinal result
     */
    public int ordinal() { return ordinal; }

    /**
     * Declaring class.
     *
     * @return declaring class result
     */
    public Class<? extends NOpenEnum> declaringClass() { return getClass(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id.equals(((NOpenEnum) o).id);
    }

    @Override public int hashCode() { return id.hashCode(); }
    @Override public String toString() { return id; }

}

package net.thevpc.nuts.util;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NValue;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Immutable enum set
 *
 * @param <T> enum type
 */
public class NEnumSet<T extends Enum<T>> implements Iterable<T> {
    private EnumSet<T> values;
    private Class<T> type;
    private int maxSize;
    private NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr;


    public static <T extends Enum<T>> NOptional<NEnumSet<T>> parse(String value, Class<T> type) {
        return parseType(DEFAULT_CTR(), value, type);
    }

    public static <T extends Enum<T>> NEnumSet<T> noneOf(Class<T> type) {
        return noneOfType(DEFAULT_CTR(), type);
    }

    public static <T extends Enum<T>> NEnumSet<T> of(Collection<T> value, Class<T> tt) {
        return ofType(DEFAULT_CTR(), value, tt);
    }

    public static <T extends Enum<T>> NEnumSet<T> of(T[] value, Class<T> tt) {
        return ofType(DEFAULT_CTR(), value, tt);
    }

    public static <T extends Enum<T>> NEnumSet<T> ofBitSet(long bits, Class<T> type) {
        return ofTypeBitSet(DEFAULT_CTR(), bits, type);
    }

    public static <T extends Enum<T>> NEnumSet<T> ofBitSet(BitSet bits, Class<T> type) {
        return ofTypeBitSet(DEFAULT_CTR(), bits, type);
    }

    public static <T extends Enum<T>> NEnumSet<T> ofBitSet(BigInteger bits, Class<T> type) {
        return ofTypeBitSet(DEFAULT_CTR(), bits, type);
    }

    public static <T extends Enum<T>> NEnumSet<T> allOf(Class<T> type) {
        return allOfType(DEFAULT_CTR(), type);
    }

    public static <T extends Enum<T>> NEnumSet<T> of(T value) {
        return ofType(DEFAULT_CTR(), value);
    }

    public static <T extends Enum<T>> NEnumSet<T> of(T... value) {
        return ofType(DEFAULT_CTR(), value);
    }

    public static <T extends Enum<T>> NEnumSet<T> of(Collection<T> value) {
        return ofType(DEFAULT_CTR(), value);
    }

    //////////////////////////////////////////////////////////////

    public static <T extends Enum<T>, V extends NEnumSet<T>> NOptional<V> parseType(Class<V> setType, String value, Class<T> type) {
        return (NOptional<V>) parseType(TYPED_CTR(setType), value, type);
    }

    public static <T extends Enum<T>, V extends NEnumSet<T>> V noneOfType(Class<V> setType, Class<T> type) {
        return (V) noneOfType(TYPED_CTR(setType), type);
    }

    public static <T extends Enum<T>, V extends NEnumSet<T>> V ofType(Class<V> setType, Collection<T> value, Class<T> tt) {
        return (V) ofType(TYPED_CTR(setType), value, tt);
    }

    public static <T extends Enum<T>, V extends NEnumSet<T>> V ofType(Class<V> setType, T[] value, Class<T> tt) {
        return (V) ofType(TYPED_CTR(setType), value, tt);
    }

    public static <T extends Enum<T>, V extends NEnumSet<T>> V ofTypeBitSet(Class<V> setType, long bits, Class<T> type) {
        return (V) ofTypeBitSet(TYPED_CTR(setType), bits, type);
    }

    public static <T extends Enum<T>, V extends NEnumSet<T>> V ofTypeBitSet(Class<V> setType, BitSet bits, Class<T> type) {
        return (V) ofTypeBitSet(TYPED_CTR(setType), bits, type);
    }

    public static <T extends Enum<T>, V extends NEnumSet<T>> V ofTypeBitSet(Class<V> setType, BigInteger bits, Class<T> type) {
        return (V) ofTypeBitSet(TYPED_CTR(setType), bits, type);
    }

    public static <T extends Enum<T>, V extends NEnumSet<T>> V allOfType(Class<V> setType, Class<T> type) {
        return (V) allOfType(TYPED_CTR(setType), type);
    }

    public static <T extends Enum<T>, V extends NEnumSet<T>> V ofType(Class<V> setType, T value) {
        return (V) ofType(TYPED_CTR(setType), value);
    }

    public static <T extends Enum<T>, V extends NEnumSet<T>> V ofType(Class<V> setType, T... value) {
        return (V) ofType(TYPED_CTR(setType), value);
    }

    public static <T extends Enum<T>, V extends NEnumSet<T>> V ofType(Class<V> setType, Collection<T> value) {
        return (V) ofType(TYPED_CTR(setType), value);
    }


    //////////////////////////////////////////////////////////////

    public static <T extends Enum<T>> NOptional<NEnumSet<T>> parseType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> setType, String value, Class<T> type) {
        if (value == null) {
            return NOptional.ofEmpty(s -> NMsg.ofPlain("null enum set"));
        }
        List<String> z = NStringUtils.split(value, ",;|+", true, true);
        if (z.size() == 1) {
            NOptional<BigInteger> lng = NValue.of(z.get(0)).asBigInt();
            if (lng.isPresent()) {
                return NOptional.of(ofTypeBitSet(setType, lng.get(), type));
            }
        }
        Set<T> set = new LinkedHashSet<>();
        if (NEnum.class.isAssignableFrom(type)) {
            for (String s : z) {
                NOptional<? extends NEnum> y = NEnum.parse((Class<? extends NEnum>) type, s);
                if (y.isPresent()) {
                    set.add((T) y.get());
                } else {
                    return NOptional.ofError(y.getMessage());
                }
            }
        } else {
            for (String s : z) {
                try {
                    T t = Enum.valueOf(type, s);
                    set.add(t);
                } catch (Exception e) {
                    return NOptional.ofError(session -> NMsg.ofPlain(e.getMessage()));
                }
            }
        }
        return NOptional.of(ofType(setType, set, type));
    }

    public static <T extends Enum<T>, V extends NEnumSet<T>> NEnumSet<T> ofType(Class<V> setType, Class<T> valueType) {
        return ofType(TYPED_CTR(setType), valueType);
    }

    public static <T extends Enum<T>> NEnumSet<T> allOfType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, Class<T> type) {
        return newInstance(ctr, EnumSet.allOf(type), type);
    }

    public static <T extends Enum<T>> NEnumSet<T> ofType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, T value) {
        return newInstance(ctr, EnumSet.of(value), (Class<T>) value.getClass());
    }

    public static <T extends Enum<T>> NEnumSet<T> noneOfType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, Class<T> type) {
        return newInstance(ctr, EnumSet.noneOf(type), type);
    }

    public static <T extends Enum<T>> NEnumSet<T> ofType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, T... value) {
        Class<T> t = (Class<T>) (value[0].getClass());
        EnumSet<T> e = EnumSet.noneOf(t);
        e.addAll(Arrays.asList(value));
        return newInstance(ctr, e, t);
    }

    public static <T extends Enum<T>> NEnumSet<T> ofType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, Collection<T> value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("unable to resolve enum type from empty collection");
        }
        T a = value.stream().findAny().get();
        Class<T> t = (Class<T>) a.getClass();
        return ctr.apply(asSet(value), t);
    }

    public static <T extends Enum<T>> NEnumSet<T> ofType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, Collection<T> value, Class<T> tt) {
        return newInstance(ctr, asSet(value), tt);
    }


    public static <T extends Enum<T>> NEnumSet<T> ofType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, T[] value, Class<T> tt) {
        return newInstance(ctr, asSet(Arrays.asList(value)), tt);
    }


    public static <T extends Enum<T>> NEnumSet<T> ofTypeBitSet(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, long bits, Class<T> type) {
        return newInstance(ctr, bitToSet(bits, type), type);
    }


    public static <T extends Enum<T>> NEnumSet<T> ofTypeBitSet(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, BigInteger bits, Class<T> type) {
        return newInstance(ctr, bitToSet(BitSet.valueOf(bits.toByteArray()), type), type);
    }


    public static <T extends Enum<T>> NEnumSet<T> ofTypeBitSet(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, BitSet bits, Class<T> type) {
        return newInstance(ctr, bitToSet(bits, type), type);
    }

    public static <T extends Enum<T>> NEnumSet<T> of(Class<T> type) {
        return noneOf(type);
    }


    public static <T extends Enum<T>> NEnumSet<T> ofType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> setType, Class<T> type) {
        return noneOfType(setType, type);
    }


    protected NEnumSet(Set<T> values, Class<T> type, NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr) {
        this.ctr = ctr;
        NAssert.requireNonNull(ctr, "ctr");
        this.maxSize = type.getEnumConstants().length;
        this.values = EnumSet.noneOf(type);
        this.type = type;
        if (values != null) {
            this.values.addAll(values);
        }
    }

    public boolean contains(T any) {
        return any != null && values.contains(any);
    }


    public NEnumSet<T> retainAll(T... any) {
        return retainAll(Arrays.asList(any));
    }

    public NEnumSet<T> retainAll(Collection<T> any) {
        if (any != null) {
            Set<T> values2 = new HashSet<>(values);
            if (values2.retainAll(any)) {
                return ctr.apply(values2, type);
            }
        }
        return this;
    }

    public boolean containsAll(T... any) {
        if (any != null) {
            return containsAll(Arrays.asList(any));
        }
        return false;
    }

    public boolean containsAll(Collection<T> any) {
        return any != null && values.containsAll(any);
    }

    public boolean containsNone(T... any) {
        if (any != null) {
            return containsNone(Arrays.asList(any));
        }
        return false;
    }

    public boolean containsNone(Collection<T> any) {
        if (any != null) {
            for (T t : any) {
                if (values.contains(t)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean containsAny(T... any) {
        if (any != null) {
            return containsAny(Arrays.asList(any));
        }
        return false;
    }

    public boolean containsAny(Collection<T> any) {
        if (any != null) {
            for (T t : any) {
                if (values.contains(t)) {
                    return true;
                }
            }
        }
        return false;
    }

    public NEnumSet<T> add(T any) {
        if (any != null && !values.contains(any)) {
            Set<T> values2 = new HashSet<>(values);
            values2.add(any);
            return ctr.apply(values2, type);
        }
        return this;
    }

    public NEnumSet<T> addAll(NEnumSet<T> other) {
        if(other!=null){
            return addAll(other.toSet());
        }
        return this;
    }

    public NEnumSet<T> addAll(T... any) {
        return addAll(Arrays.asList(any));
    }

    public NEnumSet<T> addAll(Collection<T> any) {
        Set<T> values2 = new HashSet<>(values);
        boolean changed = false;
        if (any != null) {
            for (T t : any) {
                if (values2.add(t)) {
                    changed = true;
                }
            }
        }
        if (changed) {
            return ctr.apply(values2, type);
        }
        return this;
    }

    public NEnumSet<T> remove(T any) {
        if (any != null && values.contains(any)) {
            Set<T> values2 = new HashSet<>(values);
            values2.remove(any);
            return ctr.apply(values2, type);
        }
        return this;
    }

    public NEnumSet<T> removeAll(T... any) {
        return removeAll(Arrays.asList(any));
    }

    public NEnumSet<T> complement() {
        return allOf(type).removeAll(values);
    }

    public NEnumSet<T> removeAll(Collection<T> any) {
        Set<T> values2 = new HashSet<>(values);
        boolean changed = false;
        if (any != null) {
            for (T t : any) {
                if (values2.remove(t)) {
                    changed = true;
                }
            }
        }
        if (changed) {
            return ctr.apply(values2, type);
        }
        return this;
    }

    public BitSet bitSet() {
        BitSet b = new BitSet();
        for (T value : values) {
            b.set(value.ordinal(), true);
        }
        return b;
    }

    public long bits() {
        long x = 0;
        for (T value : values) {
            x += ((value.ordinal() + 1L) << 2);
        }
        return x;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public int getMaxSize() {
        return maxSize;
    }

    public boolean isFull() {
        return values.size() == maxSize;
    }

    public int size() {
        return values.size();
    }

    public Iterator<T> iterator() {
        return values.iterator();
    }

    public Stream<T> stream() {
        return values.stream();
    }

    public Set<T> toSet() {
        return Collections.unmodifiableSet(values);
    }

    public T[] toArray() {
        return values.toArray((T[]) Array.newInstance(type, 0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NEnumSet<?> that = (NEnumSet<?>) o;
        return Objects.equals(values, that.values) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values, type);
    }

    @Override
    public String toString() {
        return "{" + values.stream().map(x -> (x instanceof NEnum) ? ((NEnum) x).id() : x.name())
                .collect(Collectors.joining(", ")) + "}";
    }

    private static <T extends Enum<T>> NEnumSet<T> newInstance(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, Set<T> set, Class<T> cls) {
        NAssert.requireNonNull(ctr, "constructor");
        NEnumSet<T> a = ctr.apply(set, cls);
        NAssert.requireNonNull(a, "instance");
        return a;
    }

    private static <T extends Enum<T>> NFunction2<Set<T>, Class<T>, NEnumSet<T>> DEFAULT_CTR() {
        return new NFunction2<Set<T>, Class<T>, NEnumSet<T>>() {
            @Override
            public NEnumSet<T> apply(Set<T> ts, Class<T> aClass) {
                return new NEnumSet<>(ts, aClass, this);
            }
        };
    }

    private static <T extends Enum<T>, V extends NEnumSet<T>> NFunction2<Set<T>, Class<T>, NEnumSet<T>> TYPED_CTR(Class<V> clz) {
        NAssert.requireNonNull(clz, "enum set class");
        Constructor<V> d;
        try {
            d = clz.getDeclaredConstructor(Set.class, Class.class, NFunction2.class);
            d.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("missing constructor for " + clz);
        }
        return new NFunction2<Set<T>, Class<T>, NEnumSet<T>>() {
            @Override
            public NEnumSet<T> apply(Set<T> ts, Class<T> aClass) {
                try {
                    return d.newInstance(ts, aClass, this);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    if (t instanceof RuntimeException) {
                        throw (RuntimeException) t;
                    }
                    throw new RuntimeException(t);
                }
            }
        };
    }

    private static <T> Set<T> bitToSet(long values, Class<T> type) {
        Set<T> v = new LinkedHashSet<>();
        T[] allValues = type.getEnumConstants();
        long x = 1;
        int index = 0;
        while (values != 0) {
            if ((values & x) != 0) {
                v.add(allValues[index]);
                values = values & (~x);
            }
            index++;
            x <<= 2;
        }
        return v;
    }

    private static <T> Set<T> bitToSet(BitSet values, Class<T> type) {
        Set<T> v = new LinkedHashSet<>();
        T[] allValues = type.getEnumConstants();
        int index = 0;
        values = (BitSet) values.clone();
        while (values.isEmpty()) {
            if (values.get(index)) {
                v.add(allValues[index]);
                values.set(0);
            }
            index++;
        }
        return v;
    }

    private static <T extends Enum<T>> LinkedHashSet<T> asSet(Collection<T> value) {
        if (value instanceof Set) {
            return (LinkedHashSet<T>) value;
        }
        return new LinkedHashSet<>(value);
    }
}

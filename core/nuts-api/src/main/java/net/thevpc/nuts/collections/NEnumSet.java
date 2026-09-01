package net.thevpc.nuts.collections;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

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


    /**
     * Parse.
     *
     * @param value value
     * @param type type
     * @return parse result
     */
    public static <T extends Enum<T>> NOptional<NEnumSet<T>> parse(String value, Class<T> type) {
        /**
         * Parse type.
         *
         * @param DEFAULT_CTR() default_ctr()
         * @param value value
         * @param type type
         * @return parse type result
         */
        return parseType(DEFAULT_CTR(), value, type);
    }

    /**
     * None of.
     *
     * @param type type
     * @return none of result
     */
    public static <T extends Enum<T>> NEnumSet<T> noneOf(Class<T> type) {
        /**
         * None of type.
         *
         * @param DEFAULT_CTR() default_ctr()
         * @param type type
         * @return none of type result
         */
        return noneOfType(DEFAULT_CTR(), type);
    }

    /**
     * Creates a new instance of of.
     *
     * @param value value
     * @param tt tt
     * @return of result
     */
    public static <T extends Enum<T>> NEnumSet<T> of(Collection<T> value, Class<T> tt) {
        /**
         * Creates a new instance of of type.
         *
         * @param DEFAULT_CTR() default_ctr()
         * @param value value
         * @param tt tt
         * @return of type result
         */
        return ofType(DEFAULT_CTR(), value, tt);
    }

    /**
     * Creates a new instance of of.
     *
     * @param value value
     * @param tt tt
     * @return of result
     */
    public static <T extends Enum<T>> NEnumSet<T> of(T[] value, Class<T> tt) {
        /**
         * Creates a new instance of of type.
         *
         * @param DEFAULT_CTR() default_ctr()
         * @param value value
         * @param tt tt
         * @return of type result
         */
        return ofType(DEFAULT_CTR(), value, tt);
    }

    /**
     * Creates a new instance of of bit set.
     *
     * @param bits bits
     * @param type type
     * @return of bit set result
     */
    public static <T extends Enum<T>> NEnumSet<T> ofBitSet(long bits, Class<T> type) {
        /**
         * Creates a new instance of of type bit set.
         *
         * @param DEFAULT_CTR() default_ctr()
         * @param bits bits
         * @param type type
         * @return of type bit set result
         */
        return ofTypeBitSet(DEFAULT_CTR(), bits, type);
    }

    /**
     * Creates a new instance of of bit set.
     *
     * @param bits bits
     * @param type type
     * @return of bit set result
     */
    public static <T extends Enum<T>> NEnumSet<T> ofBitSet(BitSet bits, Class<T> type) {
        /**
         * Creates a new instance of of type bit set.
         *
         * @param DEFAULT_CTR() default_ctr()
         * @param bits bits
         * @param type type
         * @return of type bit set result
         */
        return ofTypeBitSet(DEFAULT_CTR(), bits, type);
    }

    /**
     * Creates a new instance of of bit set.
     *
     * @param bits bits
     * @param type type
     * @return of bit set result
     */
    public static <T extends Enum<T>> NEnumSet<T> ofBitSet(BigInteger bits, Class<T> type) {
        /**
         * Creates a new instance of of type bit set.
         *
         * @param DEFAULT_CTR() default_ctr()
         * @param bits bits
         * @param type type
         * @return of type bit set result
         */
        return ofTypeBitSet(DEFAULT_CTR(), bits, type);
    }

    /**
     * All of.
     *
     * @param type type
     * @return all of result
     */
    public static <T extends Enum<T>> NEnumSet<T> allOf(Class<T> type) {
        /**
         * All of type.
         *
         * @param DEFAULT_CTR() default_ctr()
         * @param type type
         * @return all of type result
         */
        return allOfType(DEFAULT_CTR(), type);
    }

    /**
     * Creates a new instance of of.
     *
     * @param value value
     * @return of result
     */
    public static <T extends Enum<T>> NEnumSet<T> of(T value) {
        /**
         * Creates a new instance of of type.
         *
         * @param DEFAULT_CTR() default_ctr()
         * @param value value
         * @return of type result
         */
        return ofType(DEFAULT_CTR(), value);
    }

    /**
     * Creates a new instance of of.
     *
     * @param value value
     * @return of result
     */
    public static <T extends Enum<T>> NEnumSet<T> of(T... value) {
        /**
         * Creates a new instance of of type.
         *
         * @param DEFAULT_CTR() default_ctr()
         * @param value value
         * @return of type result
         */
        return ofType(DEFAULT_CTR(), value);
    }

    /**
     * Creates a new instance of of.
     *
     * @param value value
     * @return of result
     */
    public static <T extends Enum<T>> NEnumSet<T> of(Collection<T> value) {
        /**
         * Creates a new instance of of type.
         *
         * @param DEFAULT_CTR() default_ctr()
         * @param value value
         * @return of type result
         */
        return ofType(DEFAULT_CTR(), value);
    }

    //////////////////////////////////////////////////////////////

    /**
     * Parse type.
     *
     * @param setType set type
     * @param value value
     * @param type type
     * @return parse type result
     */
    public static <T extends Enum<T>, V extends NEnumSet<T>> NOptional<V> parseType(Class<V> setType, String value, Class<T> type) {
      /**
       * Return.
       *
       * @param parseType(TYPED_CTR(setType) parse type(typed_ctr(set type)
       * @param value value
       * @param type type
       */
        return (NOptional<V>) parseType(TYPED_CTR(setType), value, type);
    }

    /**
     * None of type.
     *
     * @param setType set type
     * @param type type
     * @return none of type result
     */
    public static <T extends Enum<T>, V extends NEnumSet<T>> V noneOfType(Class<V> setType, Class<T> type) {
      /**
       * Return.
       *
       * @param noneOfType(TYPED_CTR(setType) none of type(typed_ctr(set type)
       * @param type type
       */
        return (V) noneOfType(TYPED_CTR(setType), type);
    }

    /**
     * Creates a new instance of of type.
     *
     * @param setType set type
     * @param value value
     * @param tt tt
     * @return of type result
     */
    public static <T extends Enum<T>, V extends NEnumSet<T>> V ofType(Class<V> setType, Collection<T> value, Class<T> tt) {
      /**
       * Return.
       *
       * @param ofType(TYPED_CTR(setType) of type(typed_ctr(set type)
       * @param value value
       * @param tt tt
       */
        return (V) ofType(TYPED_CTR(setType), value, tt);
    }

    /**
     * Creates a new instance of of type.
     *
     * @param setType set type
     * @param value value
     * @param tt tt
     * @return of type result
     */
    public static <T extends Enum<T>, V extends NEnumSet<T>> V ofType(Class<V> setType, T[] value, Class<T> tt) {
      /**
       * Return.
       *
       * @param ofType(TYPED_CTR(setType) of type(typed_ctr(set type)
       * @param value value
       * @param tt tt
       */
        return (V) ofType(TYPED_CTR(setType), value, tt);
    }

    /**
     * Creates a new instance of of type bit set.
     *
     * @param setType set type
     * @param bits bits
     * @param type type
     * @return of type bit set result
     */
    public static <T extends Enum<T>, V extends NEnumSet<T>> V ofTypeBitSet(Class<V> setType, long bits, Class<T> type) {
      /**
       * Return.
       *
       * @param ofTypeBitSet(TYPED_CTR(setType) of type bit set(typed_ctr(set type)
       * @param bits bits
       * @param type type
       */
        return (V) ofTypeBitSet(TYPED_CTR(setType), bits, type);
    }

    /**
     * Creates a new instance of of type bit set.
     *
     * @param setType set type
     * @param bits bits
     * @param type type
     * @return of type bit set result
     */
    public static <T extends Enum<T>, V extends NEnumSet<T>> V ofTypeBitSet(Class<V> setType, BitSet bits, Class<T> type) {
      /**
       * Return.
       *
       * @param ofTypeBitSet(TYPED_CTR(setType) of type bit set(typed_ctr(set type)
       * @param bits bits
       * @param type type
       */
        return (V) ofTypeBitSet(TYPED_CTR(setType), bits, type);
    }

    /**
     * Creates a new instance of of type bit set.
     *
     * @param setType set type
     * @param bits bits
     * @param type type
     * @return of type bit set result
     */
    public static <T extends Enum<T>, V extends NEnumSet<T>> V ofTypeBitSet(Class<V> setType, BigInteger bits, Class<T> type) {
      /**
       * Return.
       *
       * @param ofTypeBitSet(TYPED_CTR(setType) of type bit set(typed_ctr(set type)
       * @param bits bits
       * @param type type
       */
        return (V) ofTypeBitSet(TYPED_CTR(setType), bits, type);
    }

    /**
     * All of type.
     *
     * @param setType set type
     * @param type type
     * @return all of type result
     */
    public static <T extends Enum<T>, V extends NEnumSet<T>> V allOfType(Class<V> setType, Class<T> type) {
      /**
       * Return.
       *
       * @param allOfType(TYPED_CTR(setType) all of type(typed_ctr(set type)
       * @param type type
       */
        return (V) allOfType(TYPED_CTR(setType), type);
    }

    /**
     * Creates a new instance of of type.
     *
     * @param setType set type
     * @param value value
     * @return of type result
     */
    public static <T extends Enum<T>, V extends NEnumSet<T>> V ofType(Class<V> setType, T value) {
      /**
       * Return.
       *
       * @param ofType(TYPED_CTR(setType) of type(typed_ctr(set type)
       * @param value value
       */
        return (V) ofType(TYPED_CTR(setType), value);
    }

    /**
     * Creates a new instance of of type.
     *
     * @param setType set type
     * @param value value
     * @return of type result
     */
    public static <T extends Enum<T>, V extends NEnumSet<T>> V ofType(Class<V> setType, T... value) {
      /**
       * Return.
       *
       * @param ofType(TYPED_CTR(setType) of type(typed_ctr(set type)
       * @param value value
       */
        return (V) ofType(TYPED_CTR(setType), value);
    }

    /**
     * Creates a new instance of of type.
     *
     * @param setType set type
     * @param value value
     * @return of type result
     */
    public static <T extends Enum<T>, V extends NEnumSet<T>> V ofType(Class<V> setType, Collection<T> value) {
      /**
       * Return.
       *
       * @param ofType(TYPED_CTR(setType) of type(typed_ctr(set type)
       * @param value value
       */
        return (V) ofType(TYPED_CTR(setType), value);
    }


    //////////////////////////////////////////////////////////////

    /**
     * Parse type.
     *
     * @param setType set type
     * @param value value
     * @param type type
     * @return parse type result
     */
    public static <T extends Enum<T>> NOptional<NEnumSet<T>> parseType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> setType, String value, Class<T> type) {
        if (value == null) {
            return NOptional.ofEmpty(() -> NMsg.ofP("null enum set"));
        }
        List<String> z = NStringUtils.split(value, ",;|+", true, true);
        if (z.size() == 1) {
            NOptional<BigInteger> lng = NLiteral.of(z.get(0)).asBigInt();
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
                    return NOptional.ofError(y.message());
                }
            }
        } else {
            for (String s : z) {
                try {
                    T t = Enum.valueOf(type, s);
                    set.add(t);
                } catch (Exception e) {
                    return NOptional.ofError(() -> NMsg.ofP(e.getMessage()));
                }
            }
        }
        return NOptional.of(ofType(setType, set, type));
    }

    /**
     * Creates a new instance of of type.
     *
     * @param setType set type
     * @param valueType value type
     * @return of type result
     */
    public static <T extends Enum<T>, V extends NEnumSet<T>> NEnumSet<T> ofType(Class<V> setType, Class<T> valueType) {
        /**
         * Creates a new instance of of type.
         *
         * @param TYPED_CTR(setType) typed_ctr(set type)
         * @param valueType value type
         * @return of type result
         */
        return ofType(TYPED_CTR(setType), valueType);
    }

    /**
     * All of type.
     *
     * @param ctr ctr
     * @param type type
     * @return all of type result
     */
    public static <T extends Enum<T>> NEnumSet<T> allOfType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, Class<T> type) {
        /**
         * New instance.
         *
         * @param ctr ctr
         * @param EnumSet.allOf(type) enum set.all of(type)
         * @param type type
         * @return new instance result
         */
        return newInstance(ctr, EnumSet.allOf(type), type);
    }

    /**
     * Creates a new instance of of type.
     *
     * @param ctr ctr
     * @param value value
     * @return of type result
     */
    public static <T extends Enum<T>> NEnumSet<T> ofType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, T value) {
        /**
         * New instance.
         *
         * @param ctr ctr
         * @param EnumSet.of(value) enum set.of(value)
         * @param value.getClass() value.get class()
         * @return new instance result
         */
        return newInstance(ctr, EnumSet.of(value), (Class<T>) value.getClass());
    }

    /**
     * None of type.
     *
     * @param ctr ctr
     * @param type type
     * @return none of type result
     */
    public static <T extends Enum<T>> NEnumSet<T> noneOfType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, Class<T> type) {
        /**
         * New instance.
         *
         * @param ctr ctr
         * @param EnumSet.noneOf(type) enum set.none of(type)
         * @param type type
         * @return new instance result
         */
        return newInstance(ctr, EnumSet.noneOf(type), type);
    }

    /**
     * Creates a new instance of of type.
     *
     * @param ctr ctr
     * @param value value
     * @return of type result
     */
    public static <T extends Enum<T>> NEnumSet<T> ofType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, T... value) {
        Class<T> t = (Class<T>) (value[0].getClass());
        EnumSet<T> e = EnumSet.noneOf(t);
        e.addAll(Arrays.asList(value));
        /**
         * New instance.
         *
         * @param ctr ctr
         * @param e e
         * @param t t
         * @return new instance result
         */
        return newInstance(ctr, e, t);
    }

    /**
     * Creates a new instance of of type.
     *
     * @param ctr ctr
     * @param value value
     * @return of type result
     */
    public static <T extends Enum<T>> NEnumSet<T> ofType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, Collection<T> value) {
        if (value == null || value.isEmpty()) {
            /**
             * Illegal argument exception.
             *
             * @param collection" collection"
             * @return illegal argument exception result
             */
            throw new IllegalArgumentException("unable to resolve enum type from empty collection");
        }
        T a = value.stream().findAny().get();
        Class<T> t = (Class<T>) a.getClass();
        return ctr.apply(asSet(value), t);
    }

    /**
     * Creates a new instance of of type.
     *
     * @param ctr ctr
     * @param value value
     * @param tt tt
     * @return of type result
     */
    public static <T extends Enum<T>> NEnumSet<T> ofType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, Collection<T> value, Class<T> tt) {
        /**
         * New instance.
         *
         * @param ctr ctr
         * @param asSet(value) as set(value)
         * @param tt tt
         * @return new instance result
         */
        return newInstance(ctr, asSet(value), tt);
    }


    /**
     * Creates a new instance of of type.
     *
     * @param ctr ctr
     * @param value value
     * @param tt tt
     * @return of type result
     */
    public static <T extends Enum<T>> NEnumSet<T> ofType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, T[] value, Class<T> tt) {
        /**
         * New instance.
         *
         * @param ctr ctr
         * @param asSet(Arrays.asList(value)) as set( arrays.as list(value))
         * @param tt tt
         * @return new instance result
         */
        return newInstance(ctr, asSet(Arrays.asList(value)), tt);
    }


    /**
     * Creates a new instance of of type bit set.
     *
     * @param ctr ctr
     * @param bits bits
     * @param type type
     * @return of type bit set result
     */
    public static <T extends Enum<T>> NEnumSet<T> ofTypeBitSet(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, long bits, Class<T> type) {
        /**
         * New instance.
         *
         * @param ctr ctr
         * @param type) type)
         * @param type type
         * @return new instance result
         */
        return newInstance(ctr, bitToSet(bits, type), type);
    }


    /**
     * Creates a new instance of of type bit set.
     *
     * @param ctr ctr
     * @param bits bits
     * @param type type
     * @return of type bit set result
     */
    public static <T extends Enum<T>> NEnumSet<T> ofTypeBitSet(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, BigInteger bits, Class<T> type) {
        /**
         * New instance.
         *
         * @param ctr ctr
         * @param type) type)
         * @param type type
         * @return new instance result
         */
        return newInstance(ctr, bitToSet(BitSet.valueOf(bits.toByteArray()), type), type);
    }


    /**
     * Creates a new instance of of type bit set.
     *
     * @param ctr ctr
     * @param bits bits
     * @param type type
     * @return of type bit set result
     */
    public static <T extends Enum<T>> NEnumSet<T> ofTypeBitSet(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, BitSet bits, Class<T> type) {
        /**
         * New instance.
         *
         * @param ctr ctr
         * @param type) type)
         * @param type type
         * @return new instance result
         */
        return newInstance(ctr, bitToSet(bits, type), type);
    }

    /**
     * Creates a new instance of of.
     *
     * @param type type
     * @return of result
     */
    public static <T extends Enum<T>> NEnumSet<T> of(Class<T> type) {
        /**
         * None of.
         *
         * @param type type
         * @return none of result
         */
        return noneOf(type);
    }


    /**
     * Creates a new instance of of type.
     *
     * @param setType set type
     * @param type type
     * @return of type result
     */
    public static <T extends Enum<T>> NEnumSet<T> ofType(NFunction2<Set<T>, Class<T>, NEnumSet<T>> setType, Class<T> type) {
        /**
         * None of type.
         *
         * @param setType set type
         * @param type type
         * @return none of type result
         */
        return noneOfType(setType, type);
    }


    /**
     * N enum set.
     *
     * @param values values
     * @param type type
     * @param ctr ctr
     * @return n enum set result
     */
    protected NEnumSet(Set<T> values, Class<T> type, NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr) {
        this.ctr = ctr;
        NAssert.requireNamedNonNull(ctr, "ctr");
        this.maxSize = type.getEnumConstants().length;
        this.values = EnumSet.noneOf(type);
        this.type = type;
        if (values != null) {
            this.values.addAll(values);
        }
    }

    /**
     * Contains.
     *
     * @param any any
     * @return contains result
     */
    public boolean contains(T any) {
        return any != null && values.contains(any);
    }


    /**
     * Retain all.
     *
     * @param any any
     * @return retain all result
     */
    public NEnumSet<T> retainAll(T... any) {
        /**
         * Retain all.
         *
         * @param Arrays.asList(any) arrays.as list(any)
         * @return retain all result
         */
        return retainAll(Arrays.asList(any));
    }

    /**
     * Retain all.
     *
     * @param any any
     * @return retain all result
     */
    public NEnumSet<T> retainAll(Collection<T> any) {
        if (any != null) {
            Set<T> values2 = new HashSet<>(values);
            if (values2.retainAll(any)) {
                return ctr.apply(values2, type);
            }
        }
        return this;
    }

    /**
     * Contains all.
     *
     * @param any any
     * @return contains all result
     */
    public boolean containsAll(T... any) {
        if (any != null) {
            /**
             * Contains all.
             *
             * @param Arrays.asList(any) arrays.as list(any)
             * @return contains all result
             */
            return containsAll(Arrays.asList(any));
        }
        return false;
    }

    /**
     * Contains all.
     *
     * @param any any
     * @return contains all result
     */
    public boolean containsAll(Collection<T> any) {
        return any != null && values.containsAll(any);
    }

    /**
     * Contains none.
     *
     * @param any any
     * @return contains none result
     */
    public boolean containsNone(T... any) {
        if (any != null) {
            /**
             * Contains none.
             *
             * @param Arrays.asList(any) arrays.as list(any)
             * @return contains none result
             */
            return containsNone(Arrays.asList(any));
        }
        return false;
    }

    /**
     * Contains none.
     *
     * @param any any
     * @return contains none result
     */
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

    /**
     * Contains any.
     *
     * @param any any
     * @return contains any result
     */
    public boolean containsAny(T... any) {
        if (any != null) {
            /**
             * Contains any.
             *
             * @param Arrays.asList(any) arrays.as list(any)
             * @return contains any result
             */
            return containsAny(Arrays.asList(any));
        }
        return false;
    }

    /**
     * Contains any.
     *
     * @param any any
     * @return contains any result
     */
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

    /**
     * Adds add.
     *
     * @param any any
     * @return add result
     */
    public NEnumSet<T> add(T any) {
        if (any != null && !values.contains(any)) {
            Set<T> values2 = new HashSet<>(values);
            values2.add(any);
            return ctr.apply(values2, type);
        }
        return this;
    }

    /**
     * Adds the specified all.
     *
     * @param other other
     * @return add all result
     */
    public NEnumSet<T> addAll(NEnumSet<T> other) {
        if(other!=null){
            /**
             * Adds the specified all.
             *
             * @param other.toSet() other.to set()
             * @return add all result
             */
            return addAll(other.toSet());
        }
        return this;
    }

    /**
     * Adds the specified all.
     *
     * @param any any
     * @return add all result
     */
    public NEnumSet<T> addAll(T... any) {
        /**
         * Adds the specified all.
         *
         * @param Arrays.asList(any) arrays.as list(any)
         * @return add all result
         */
        return addAll(Arrays.asList(any));
    }

    /**
     * Adds the specified all.
     *
     * @param any any
     * @return add all result
     */
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

    /**
     * Removes remove.
     *
     * @param any any
     * @return remove result
     */
    public NEnumSet<T> remove(T any) {
        if (any != null && values.contains(any)) {
            Set<T> values2 = new HashSet<>(values);
            values2.remove(any);
            return ctr.apply(values2, type);
        }
        return this;
    }

    /**
     * Removes the specified all.
     *
     * @param any any
     * @return remove all result
     */
    public NEnumSet<T> removeAll(T... any) {
        /**
         * Removes the specified all.
         *
         * @param Arrays.asList(any) arrays.as list(any)
         * @return remove all result
         */
        return removeAll(Arrays.asList(any));
    }

    /**
     * Complement.
     *
     * @return complement result
     */
    public NEnumSet<T> complement() {
        /**
         * All of.
         *
         * @param type).removeAll(values type).remove all(values
         * @return all of result
         */
        return allOf(type).removeAll(values);
    }

    /**
     * Removes the specified all.
     *
     * @param any any
     * @return remove all result
     */
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

    /**
     * Bit set.
     *
     * @return bit set result
     */
    public BitSet bitSet() {
        BitSet b = new BitSet();
        for (T value : values) {
            b.set(value.ordinal(), true);
        }
        return b;
    }

    /**
     * Bits.
     *
     * @return bits result
     */
    public long bits() {
        long x = 0;
        for (T value : values) {
            x += ((value.ordinal() + 1L) << 2);
        }
        return x;
    }

    /**
     * Checks if is empty.
     *
     * @return is empty result
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * Max size.
     *
     * @return max size result
     */
    public int maxSize() {
        return maxSize;
    }

    /**
     * Checks if is full.
     *
     * @return is full result
     */
    public boolean isFull() {
        return values.size() == maxSize;
    }

    /**
     * Size.
     *
     * @return size result
     */
    public int size() {
        return values.size();
    }

    /**
     * Iterator.
     *
     * @return iterator result
     */
    public Iterator<T> iterator() {
        return values.iterator();
    }

    /**
     * Stream.
     *
     * @return stream result
     */
    public Stream<T> stream() {
        return values.stream();
    }

    /**
     * Converts to set.
     *
     * @return to set result
     */
    public Set<T> toSet() {
        return Collections.unmodifiableSet(values);
    }

    /**
     * Converts to array.
     *
     * @return to array result
     */
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

    /**
     * New instance.
     *
     * @param ctr ctr
     * @param set set
     * @param cls cls
     * @return new instance result
     */
    private static <T extends Enum<T>> NEnumSet<T> newInstance(NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr, Set<T> set, Class<T> cls) {
        NAssert.requireNamedNonNull(ctr, "constructor");
        NEnumSet<T> a = ctr.apply(set, cls);
        NAssert.requireNamedNonNull(a, "instance");
        return a;
    }

    /**
     * Default_ctr.
     *
     * @return default_ctr result
     */
    private static <T extends Enum<T>> NFunction2<Set<T>, Class<T>, NEnumSet<T>> DEFAULT_CTR() {
        return new NFunction2<Set<T>, Class<T>, NEnumSet<T>>() {
            @Override
            public NEnumSet<T> apply(Set<T> ts, Class<T> aClass) {
                return new NEnumSet<>(ts, aClass, this);
            }
        };
    }

    /**
     * Typed_ctr.
     *
     * @param clz clz
     * @return typed_ctr result
     */
    private static <T extends Enum<T>, V extends NEnumSet<T>> NFunction2<Set<T>, Class<T>, NEnumSet<T>> TYPED_CTR(Class<V> clz) {
        NAssert.requireNamedNonNull(clz, "enum set class");
        Constructor<V> d;
        try {
            d = clz.getDeclaredConstructor(Set.class, Class.class, NFunction2.class);
            d.setAccessible(true);
        } catch (NoSuchMethodException e) {
            /**
             * Illegal argument exception.
             *
             * @param clz clz
             * @return illegal argument exception result
             */
            throw new IllegalArgumentException("missing constructor for " + clz);
        }
        return new NFunction2<Set<T>, Class<T>, NEnumSet<T>>() {
            @Override
            public NEnumSet<T> apply(Set<T> ts, Class<T> aClass) {
                try {
                    return d.newInstance(ts, aClass, this);
                } catch (InstantiationException | IllegalAccessException|InvocationTargetException e) {
                    throw NException.ofUncheckedException(e);
                }
            }
        };
    }

    /**
     * Bit to set.
     *
     * @param values values
     * @param type type
     * @return bit to set result
     */
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

    /**
     * Bit to set.
     *
     * @param values values
     * @param type type
     * @return bit to set result
     */
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

    /**
     * As set.
     *
     * @param value value
     * @return as set result
     */
    private static <T extends Enum<T>> LinkedHashSet<T> asSet(Collection<T> value) {
        if (value instanceof Set) {
            return (LinkedHashSet<T>) value;
        }
        return new LinkedHashSet<>(value);
    }
}

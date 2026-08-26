package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.Map;
import java.util.function.Function;

/**
 * NNormalizedStringMap interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NNormalizedStringMap<T> extends Map<String, T> {
    /**
     * Creates a new instance of of case insensitive.
     *
     * @return of case insensitive result
     */
    static <T> NNormalizedStringMap<T> ofCaseInsensitive() {
        return NUtilsRPI.of().createInsensitiveMap();
    }

    /**
     * Creates a new instance of of case insensitive.
     *
     * @param other other
     * @return of case insensitive result
     */
    static <T> NNormalizedStringMap<T> ofCaseInsensitive(Map<String, T> other) {
        NNormalizedStringMap<T> m = ofCaseInsensitive();
        if(other!=null){
            m.putAll(other);
        }
        return m;
    }

    /**
     * Creates a new instance of of format insensitive.
     *
     * @return of format insensitive result
     */
    static <T> NNormalizedStringMap<T> ofFormatInsensitive() {
        return NUtilsRPI.of().createFormatInsensitiveMap();
    }

    /**
     * Creates a new instance of of format insensitive.
     *
     * @param other other
     * @return of format insensitive result
     */
    static <T> NNormalizedStringMap<T> ofFormatInsensitive(Map<String, T> other) {
        NNormalizedStringMap<T> m = ofFormatInsensitive();
        if(other!=null){
            m.putAll(other);
        }
        return m;
    }

    /**
     * Creates a new instance of of.
     *
     * @param normalizer normalizer
     * @return of result
     */
    static <T> NNormalizedStringMap<T> of(Function<String,String> normalizer) {
        return NUtilsRPI.of().createNormalizedMap(normalizer);
    }

    /**
     * Creates a new instance of of.
     *
     * @param normalizer normalizer
     * @param other other
     * @return of result
     */
    static <T> NNormalizedStringMap<T> of(Function<String,String> normalizer,Map<String, T> other) {
        NNormalizedStringMap<T> m = of(normalizer);
        if(other!=null){
            m.putAll(other);
        }
        return m;
    }
}

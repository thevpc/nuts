package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NCollectionsRPI;

import java.util.Map;
import java.util.function.Function;

public interface NNormalizedStringMap<T> extends Map<String, T> {
    static <T> NNormalizedStringMap<T> ofCaseInsensitive() {
        return NCollectionsRPI.of().createInsensitiveMap();
    }

    static <T> NNormalizedStringMap<T> ofCaseInsensitive(Map<String, T> other) {
        NNormalizedStringMap<T> m = ofCaseInsensitive();
        if(other!=null){
            m.putAll(other);
        }
        return m;
    }

    static <T> NNormalizedStringMap<T> ofFormatInsensitive() {
        return NCollectionsRPI.of().createFormatInsensitiveMap();
    }

    static <T> NNormalizedStringMap<T> ofFormatInsensitive(Map<String, T> other) {
        NNormalizedStringMap<T> m = ofFormatInsensitive();
        if(other!=null){
            m.putAll(other);
        }
        return m;
    }

    static <T> NNormalizedStringMap<T> of(Function<String,String> normalizer) {
        return NCollectionsRPI.of().createNormalizedMap(normalizer);
    }

    static <T> NNormalizedStringMap<T> of(Function<String,String> normalizer,Map<String, T> other) {
        NNormalizedStringMap<T> m = of(normalizer);
        if(other!=null){
            m.putAll(other);
        }
        return m;
    }
}

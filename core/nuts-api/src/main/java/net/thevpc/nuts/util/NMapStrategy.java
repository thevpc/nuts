package net.thevpc.nuts.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NMapStrategy {
    private static final NMapStrategy[][] _CACHE = new NMapStrategy[NMapSideStrategy.values().length][NMapSideStrategy.values().length];
    public static final NMapStrategy ANY = of(NMapSideStrategy.ANY, NMapSideStrategy.ANY);
    public static final NMapStrategy TARGET_NON_NULL = of(NMapSideStrategy.ANY, NMapSideStrategy.NON_NULL);
    public static final NMapStrategy SOURCE_NON_NULL = of(NMapSideStrategy.NON_NULL, NMapSideStrategy.ANY);

    private NMapSideStrategy source;
    private NMapSideStrategy target;

    public static NMapStrategy of(NMapSideStrategy source, NMapSideStrategy target) {
        if (source == null) {
            source = NMapSideStrategy.ANY;
        }
        if (target == null) {
            target = NMapSideStrategy.ANY;
        }
        int so = source.ordinal();
        int to = target.ordinal();
        NMapStrategy o = _CACHE[so][to];
        if (o != null) {
            return o;
        }
        o = new NMapStrategy(source, target);
        _CACHE[so][to] = o;
        return o;
    }

    public NMapStrategy(NMapSideStrategy source, NMapSideStrategy target) {
        NAssert.requireNonNull(source, "source");
        NAssert.requireNonNull(target, "target");
        this.source = source;
        this.target = target;
    }

    public NMapSideStrategy source() {
        return source;
    }

    public NMapSideStrategy target() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NMapStrategy that = (NMapStrategy) o;
        return source == that.source && target == that.target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }

    @Override
    public String toString() {
        return "NMapStrategy{" +
                "source=" + source +
                ", target=" + target +
                '}';
    }

    public <T> boolean applyOptional(Supplier<NOptional<T>> sourceGetter, Supplier<NOptional<T>> targetGetter, Consumer<T> targetSetter) {
        return apply(()->sourceGetter.get().orNull(), ()->targetGetter.get().orNull(), targetSetter);
    }

    public <T> boolean apply(Supplier<T> sourceGetter, Supplier<T> targetGetter, Consumer<T> targetSetter) {
        Supplier<T> sourceGetter2;
        switch (source) {
            case ANY:{
                sourceGetter2=sourceGetter;
                //okkay
                break;
            }
            case NULL:{
                T s = sourceGetter.get();
                if(s==null) {
                    sourceGetter2 = () -> s;
                }else {
                    return false;
                }
                break;
            }
            case NON_NULL:{
                T s = sourceGetter.get();
                if(s!=null) {
                    sourceGetter2 = () -> s;
                }else {
                    return false;
                }
                break;
            }
            case BLANK:{
                T s = sourceGetter.get();
                if(NBlankable.isBlank(s)) {
                    sourceGetter2 = () -> s;
                }else {
                    return false;
                }
                break;
            }
            case NON_BLANK:{
                T s = sourceGetter.get();
                if(!NBlankable.isBlank(s)) {
                    sourceGetter2 = () -> s;
                }else {
                    return false;
                }
                break;
            }
            default:{
                return false;
            }
        }

        switch (target) {
            case ANY:{
                targetSetter.accept(sourceGetter2.get());
                return true;
            }
            case NULL:{
                T t = targetGetter.get();
                if(t==null) {
                    targetSetter.accept(sourceGetter2.get());
                    return true;
                }
                return false;
            }
            case BLANK:{
                T t = targetGetter.get();
                if(NBlankable.isBlank(t)) {
                    targetSetter.accept(sourceGetter2.get());
                    return true;
                }
                return false;
            }
            case NON_BLANK:{
                T t = targetGetter.get();
                if(!NBlankable.isBlank(t)) {
                    targetSetter.accept(sourceGetter2.get());
                    return true;
                }
                return false;
            }
            case NON_NULL:{
                T t = targetGetter.get();
                if(t!=null) {
                    targetSetter.accept(sourceGetter2.get());
                    return true;
                }
                return false;
            }
            default:{
                return false;
            }
        }
    }
}

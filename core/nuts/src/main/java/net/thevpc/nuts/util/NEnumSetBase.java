package net.thevpc.nuts.util;

import java.util.Collection;
import java.util.Set;

public class NEnumSetBase<T extends Enum<T>, V extends NEnumSetBase<T, V>> extends NEnumSet<T> {


    protected NEnumSetBase(Set<T> values, Class<T> type, NFunction2<Set<T>, Class<T>, NEnumSet<T>> ctr) {
        super(values, type, ctr);
    }

    @Override
    public V retainAll(T... any) {
        return (V) super.retainAll(any);
    }

    @Override
    public V retainAll(Collection<T> any) {
        return (V) super.retainAll(any);
    }

    @Override
    public V add(T any) {
        return (V) super.add(any);
    }

    @Override
    public V addAll(T... any) {
        return (V) super.addAll(any);
    }

    @Override
    public V addAll(Collection<T> any) {
        return (V) super.addAll(any);
    }

    @Override
    public V remove(T any) {
        return (V) super.remove(any);
    }

    @Override
    public V removeAll(T... any) {
        return (V) super.removeAll(any);
    }

    @Override
    public V complement() {
        return (V) super.complement();
    }

    @Override
    public V removeAll(Collection<T> any) {
        return (V) super.removeAll(any);
    }

}

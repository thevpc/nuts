package net.thevpc.nuts.runtime.standalone.tson.impl.util;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public final class UnmodifiableSingletonList<E> extends AbstractList<E>
        implements RandomAccess, java.io.Serializable {
    private static final long serialVersionUID = -2764017481108945198L;
    private final E value;

    private UnmodifiableSingletonList(E value) {
        this.value = value;
    }

    public static <E> UnmodifiableSingletonList<E> of(E item) {
        return new UnmodifiableSingletonList<>(item);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    @Override
    public Object[] toArray() {
        return new Object[]{value};
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < 1) {
            T[] t = (T[]) Array.newInstance(a.getClass().getComponentType(), 1);
            t[0] = (T) value;
        }
        a[0] = (T) value;
        return a;
    }

    @Override
    public E get(int index) {
        if (index == 0) {
            return value;
        }
        throw new ArrayIndexOutOfBoundsException(index);
//        return values[index];
    }

    @Override
    public E set(int index, E element) {
        throw new IllegalArgumentException("Unsupported");
//        E oldValue = values[index];
//        values[index] = element;
//        return oldValue;
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            if (value == null) {
                return 0;
            }
        } else {
            if (value.equals(o)) {
                return 0;
            }
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UnmodifiableSingletonList<?> that = (UnmodifiableSingletonList<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }

    public E getValue() {
        return value;
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        action.accept(value);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        throw new IllegalArgumentException("Unsupported");
//        Objects.requireNonNull(operator);
//        value=operator.apply(value);
    }

    @Override
    public void sort(Comparator<? super E> c) {
        //Arrays.sort(values, c);
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(new Object[]{value}, Spliterator.ORDERED);
    }
}

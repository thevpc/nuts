package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public final class UnmodifiableArrayList<E> extends AbstractList<E>
        implements RandomAccess, java.io.Serializable {
    private static final long serialVersionUID = -2764017481108945198L;
    private static final UnmodifiableArrayList EMPTY = new UnmodifiableArrayList(new Object[0]);
    private final E[] values;

    public static <E> UnmodifiableArrayList<E> empty() {
        return EMPTY;
    }
    public static <E> UnmodifiableArrayList<E> ofCopy(E[] array) {
        return new UnmodifiableArrayList<>(Arrays.copyOf(array, array.length, (Class<? extends E[]>) array.getClass()));
    }

    public static <E> UnmodifiableArrayList<E> ofRef(E[] array) {
        return new UnmodifiableArrayList<>(array);
    }

    private UnmodifiableArrayList(E[] array) {
        values = array;
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public Object[] toArray() {
        return values.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        int size = size();
        if (a.length < size) {
            return Arrays.copyOf(this.values, size,
                    (Class<? extends T[]>) a.getClass());
        }
        System.arraycopy(this.values, 0, a, 0, size);
        return a;
    }

    @Override
    public E get(int index) {
        return values[index];
    }

    @Override
    public E set(int index, E element) {
        E oldValue = values[index];
        values[index] = element;
        return oldValue;
    }

    @Override
    public int indexOf(Object o) {
        E[] a = this.values;
        if (o == null) {
            for (int i = 0; i < a.length; i++) {
                if (a[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < a.length; i++) {
                if (o.equals(a[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(values, Spliterator.ORDERED);
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        for (E e : values) {
            action.accept(e);
        }
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        E[] a = this.values;
        for (int i = 0; i < a.length; i++) {
            a[i] = operator.apply(a[i]);
        }
    }

    @Override
    public void sort(Comparator<? super E> c) {
        Arrays.sort(values, c);
    }
}

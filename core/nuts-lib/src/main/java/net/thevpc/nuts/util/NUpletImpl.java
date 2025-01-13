package net.thevpc.nuts.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

final class NUpletImpl<T> implements NUplet<T> {
    private T[] values;

    public NUpletImpl(T[] a) {
        this.values = a;
    }

    @Override
    public Iterator<T> iterator() {
        return Arrays.asList(values).iterator();
    }

    @Override
    public T get(int index) {
        return values[index];
    }

    @Override
    public NUplet<T> set(T newValue, int index) {
        T[] b = Arrays.copyOf(values, values.length);
        b[index] = newValue;
        return new NUpletImpl<T>(b);
    }

    @Override
    public List<T> toList() {
        return Arrays.asList(values);
    }

    @Override
    public T[] toArray() {
        return Arrays.copyOf(values, values.length);
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NUpletImpl<?> nUplet = (NUpletImpl<?>) o;
        return Objects.deepEquals(values, nUplet.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public String toString() {
        int iMax = values.length - 1;
        if (iMax == -1) {
            return "()";
        } else {
            StringBuilder b = new StringBuilder();
            b.append('(');
            int i = 0;

            while(true) {
                b.append(values[i]);
                if (i == iMax) {
                    return b.append(')').toString();
                }

                b.append(", ");
                ++i;
            }
        }
    }
}

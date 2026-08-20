package net.thevpc.nuts.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class NIntPair implements NPair<Integer, Integer, Integer> {
    private final int a;
    private final int b;

    public static NIntPair of(int a, int b) {
        return new NIntPair(a, b);
    }

    public NIntPair(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public int firstInt() {
        return a;
    }

    public int secondInt() {
        return b;
    }

    @Override
    public Integer first() {
        return a;
    }

    @Override
    public Integer second() {
        return b;
    }

    @Override
    public Integer get(int index) {
        switch (index) {
            case 0:
                return a;
            case 1:
                return b;
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public NIntPair set(int newValue, int index) {
        switch (index) {
            case 0:
                return new NIntPair(newValue, b);
            case 1:
                return new NIntPair(a, newValue);
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    @Override
    public NPair<Integer, Integer, Integer> set(Integer newValue, int index) {
        if (newValue == null) {
            switch (index) {
                case 0:
                    return new NIntPair(newValue, b);
                case 1:
                    return new NIntPair(a, newValue);
            }
        } else {
            switch (index) {
                case 0:
                    return new NIntPair(newValue, b);
                case 1:
                    return new NIntPair(a, newValue);
            }
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public NIntPair setFirst(int t) {
        return set(t, 0);
    }

    public NIntPair setSecond(int t) {
        return set(t, 0);
    }

    @Override
    public NPair<Integer, Integer, Integer> first(Integer t) {
        return set(t, 0);
    }

    @Override
    public NPair<Integer, Integer, Integer> second(Integer t) {
        return set(t, 1);
    }

    @Override
    public Iterator<Integer> iterator() {
        return Arrays.asList(a, b).iterator();
    }

    @Override
    public List<Integer> toList() {
        return Arrays.asList(a, b);
    }

    @Override
    public Integer[] toArray() {
        return new Integer[]{a, b};
    }

    public int[] toIntArray() {
        return new int[]{a, b};
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NIntPair tuple = (NIntPair) o;
        return (a == tuple.a) && (b == tuple.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ')';
    }
}

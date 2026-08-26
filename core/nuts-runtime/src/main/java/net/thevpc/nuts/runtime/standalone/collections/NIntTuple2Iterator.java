package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.util.NIntPair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class NIntTuple2Iterator implements Iterator<NIntPair> {
    public  static Comparator<NIntPair> COMPARATOR = new IntTuple2Comparator();
    private NIntPair current0;
    private NIntPair current;

    public NIntTuple2Iterator() {
        this(0, 0);
    }

    public NIntTuple2Iterator(int a, int b) {
        this.current0 = new NIntPair(a, b);
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public NIntPair next() {
        if (current == null) {
            current = current0;
            return current;
        } else {
            int a = current.firstInt();
            int b = current.secondInt();
            int sum = a + b;
            if (b == 0) {
                return current = new NIntPair(0, sum + 1);
            } else {
                return current = new NIntPair(a + 1, b - 1);
            }
        }
    }

    public List<NIntPair> next(int count) {
        List<NIntPair> a = new ArrayList<>(count);
        while (count > 0) {
            a.add(next());
            count--;
        }
        return a;
    }

    private static class IntTuple2Comparator implements Comparator<NIntPair> {
        @Override
        public int compare(NIntPair o1, NIntPair o2) {
            int s1 = o1.firstInt() + o1.secondInt();
            int s2 = o2.firstInt() + o2.secondInt();
            int c = Integer.compare(s1, s2);
            if (c != 0) {
                return c;
            }
            return Integer.compare(o1.firstInt(), o2.secondInt());
        }
    }
}

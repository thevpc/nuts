package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.util.NIntUplet2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class NIntUplet2Iterator implements Iterator<NIntUplet2> {
    public  static Comparator<NIntUplet2> COMPARATOR = new IntTuple2Comparator();
    private NIntUplet2 current0;
    private NIntUplet2 current;

    public NIntUplet2Iterator() {
        this(0, 0);
    }

    public NIntUplet2Iterator(int a, int b) {
        this.current0 = new NIntUplet2(a, b);
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public NIntUplet2 next() {
        if (current == null) {
            current = current0;
            return current;
        } else {
            int a = current.firstInt();
            int b = current.secondInt();
            int sum = a + b;
            if (b == 0) {
                return current = new NIntUplet2(0, sum + 1);
            } else {
                return current = new NIntUplet2(a + 1, b - 1);
            }
        }
    }

    public List<NIntUplet2> next(int count) {
        List<NIntUplet2> a = new ArrayList<>(count);
        while (count > 0) {
            a.add(next());
            count--;
        }
        return a;
    }

    private static class IntTuple2Comparator implements Comparator<NIntUplet2> {
        @Override
        public int compare(NIntUplet2 o1, NIntUplet2 o2) {
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

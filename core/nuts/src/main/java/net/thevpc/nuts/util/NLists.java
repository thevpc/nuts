package net.thevpc.nuts.util;

import java.util.*;

public class NLists {
    public static <A> List<A> append(Collection<A> a1, A b1) {
        List<A> li = new ArrayList<>(a1);
        li.add(b1);
        return li;
    }

    public static <A> List<A> appendAll(Collection<A> a1, Collection<A> b1) {
        List<A> li = new ArrayList<>(a1);
        li.addAll(b1);
        return li;
    }

    public static <A> List<A> prepend(A b1, Collection<A> a1) {
        List<A> li = new ArrayList<>();
        li.add(b1);
        li.addAll(a1);
        return li;
    }
}

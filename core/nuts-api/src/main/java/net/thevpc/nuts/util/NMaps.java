package net.thevpc.nuts.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class NMaps {
    public static <A, B> Map<A, B> of(A a1, B b1) {
        LinkedHashMap<A, B> li = new LinkedHashMap<A, B>();
        li.put(a1, b1);
        return li;
    }

    public static <A, B> Map<A, B> of(A a1, B b1, A a2, B b2) {
        LinkedHashMap<A, B> li = new LinkedHashMap<A, B>();
        li.put(a1, b1);
        li.put(a2, b2);
        return li;
    }

    public static <A, B> Map<A, B> of(A a1, B b1, A a2, B b2, A a3, B b3) {
        LinkedHashMap<A, B> li = new LinkedHashMap<A, B>();
        li.put(a1, b1);
        li.put(a2, b2);
        li.put(a3, b3);
        return li;
    }

    public static <A, B> Map<A, B> of(A a1, B b1, A a2, B b2, A a3, B b3, A a4, B b4) {
        LinkedHashMap<A, B> li = new LinkedHashMap<A, B>();
        li.put(a1, b1);
        li.put(a2, b2);
        li.put(a3, b3);
        li.put(a4, b4);
        return li;
    }

    public static <A, B> Map<A, B> of(A a1, B b1, A a2, B b2, A a3, B b3, A a4, B b4, A a5, B b5) {
        LinkedHashMap<A, B> li = new LinkedHashMap<A, B>();
        li.put(a1, b1);
        li.put(a2, b2);
        li.put(a3, b3);
        li.put(a4, b4);
        li.put(a5, b5);
        return li;
    }

    public static <A, B> Map<A, B> of(A a1, B b1, A a2, B b2, A a3, B b3, A a4, B b4, A a5, B b5, A a6, B b6) {
        LinkedHashMap<A, B> li = new LinkedHashMap<A, B>();
        li.put(a1, b1);
        li.put(a2, b2);
        li.put(a3, b3);
        li.put(a4, b4);
        li.put(a5, b5);
        li.put(a6, b6);
        return li;
    }

    public static <A, B> Map<A, B> of(A a1, B b1, A a2, B b2, A a3, B b3, A a4, B b4, A a5, B b5, A a6, B b6, A a7, B b7) {
        LinkedHashMap<A, B> li = new LinkedHashMap<A, B>();
        li.put(a1, b1);
        li.put(a2, b2);
        li.put(a3, b3);
        li.put(a4, b4);
        li.put(a5, b5);
        li.put(a6, b6);
        li.put(a7, b7);
        return li;
    }

    public static <A, B> Map<A, B> of(A a1, B b1, A a2, B b2, A a3, B b3, A a4, B b4, A a5, B b5, A a6, B b6, A a7, B b7, A a8, B b8) {
        LinkedHashMap<A, B> li = new LinkedHashMap<A, B>();
        li.put(a1, b1);
        li.put(a2, b2);
        li.put(a3, b3);
        li.put(a4, b4);
        li.put(a5, b5);
        li.put(a6, b6);
        li.put(a7, b7);
        li.put(a8, b8);
        return li;
    }

    public static <A, B> Map<A, B> of(A a1, B b1, A a2, B b2, A a3, B b3, A a4, B b4, A a5, B b5, A a6, B b6, A a7, B b7, A a8, B b8, A a9, B b9) {
        LinkedHashMap<A, B> li = new LinkedHashMap<A, B>();
        li.put(a1, b1);
        li.put(a2, b2);
        li.put(a3, b3);
        li.put(a4, b4);
        li.put(a5, b5);
        li.put(a6, b6);
        li.put(a7, b7);
        li.put(a8, b8);
        li.put(a9, b9);
        return li;
    }

    public static <K, V> Map<K, V> fill(Map<K, V> m, K a1, V b1) {
        m.put(a1, b1);
        return m;
    }

    public static <K, V> Map<K, V> fill(Map<K, V> m, K a1, V b1, K a2, V b2) {
        m.put(a1, b1);
        m.put(a2, b2);
        return m;
    }

    public static <K, V> Map<K, V> fill(Map<K, V> m, K a1, V b1, K a2, V b2, K a3, V b3) {
        m.put(a1, b1);
        m.put(a2, b2);
        m.put(a3, b3);
        return m;
    }

    public static <K, V> Map<K, V> fill(Map<K, V> m, K a1, V b1, K a2, V b2, K a3, V b3, K a4, V b4) {
        m.put(a1, b1);
        m.put(a2, b2);
        m.put(a3, b3);
        m.put(a4, b4);
        return m;
    }

    public static <K, V> Map<K, V> fill(Map<K, V> m, K a1, V b1, K a2, V b2, K a3, V b3, K a4, V b4, K a5, V b5) {
        m.put(a1, b1);
        m.put(a2, b2);
        m.put(a3, b3);
        m.put(a4, b4);
        m.put(a5, b5);
        return m;
    }

    public static <K, V> Map<K, V> fill(Map<K, V> m, K a1, V b1, K a2, V b2, K a3, V b3, K a4, V b4, K a5, V b5, K a6, V b6) {
        m.put(a1, b1);
        m.put(a2, b2);
        m.put(a3, b3);
        m.put(a4, b4);
        m.put(a5, b5);
        m.put(a6, b6);
        return m;
    }

    public static <K, V> Map<K, V> fill(Map<K, V> m, K a1, V b1, K a2, V b2, K a3, V b3, K a4, V b4, K a5, V b5, K a6, V b6, K a7, V b7) {
        m.put(a1, b1);
        m.put(a2, b2);
        m.put(a3, b3);
        m.put(a4, b4);
        m.put(a5, b5);
        m.put(a6, b6);
        m.put(a7, b7);
        return m;
    }

    public static <K, V> Map<K, V> fill(Map<K, V> m, K a1, V b1, K a2, V b2, K a3, V b3, K a4, V b4, K a5, V b5, K a6, V b6, K a7, V b7, K a8, V b8) {
        m.put(a1, b1);
        m.put(a2, b2);
        m.put(a3, b3);
        m.put(a4, b4);
        m.put(a5, b5);
        m.put(a6, b6);
        m.put(a7, b7);
        m.put(a8, b8);
        return m;
    }

    public static <K, V> Map<K, V> fill(Map<K, V> m, K a1, V b1, K a2, V b2, K a3, V b3, K a4, V b4, K a5, V b5, K a6, V b6, K a7, V b7, K a8, V b8, K a9, V b9) {
        m.put(a1, b1);
        m.put(a2, b2);
        m.put(a3, b3);
        m.put(a4, b4);
        m.put(a5, b5);
        m.put(a6, b6);
        m.put(a7, b7);
        m.put(a8, b8);
        m.put(a9, b9);
        return m;
    }
}

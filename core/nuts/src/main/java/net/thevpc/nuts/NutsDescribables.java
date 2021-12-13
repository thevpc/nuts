package net.thevpc.nuts;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class NutsDescribables {

    private NutsDescribables() {
    }

    public static NutsElement resolveOrToString(Object o, NutsElements elems) {
        return resolveOr(o, elems, () -> elems.ofString(o.toString()));
    }

    public static NutsElement resolveOr(Object o, NutsElements elems, Supplier<NutsElement> d) {
        NutsElement e = resolveOrNull(o, elems);
        if (e != null) {
            return e;
        }
        return d == null ? null : d.get();
    }

    public static NutsElement resolveOrDestruct(Object o, NutsElements elems) {
        NutsElement e = resolveOrNull(o, elems);
        if (e != null) {
            return e;
        }
        return elems.toElement(o);
    }

    public static void cast(Object o) {
        if (!isSupported(o)) {
            throw new IllegalArgumentException(NutsMessage.cstyle("not a valid NutsDescribable object %s : %s", o.getClass().getName(), o.toString()).toString());
        }
    }

    public static boolean isSupported(Object o) {
        if (o == null) {
            return true;
        }
        return o instanceof NutsDescribable;
    }

    public static NutsElement resolveOrNull(Object o, NutsElements elems) {
        if (o == null) {
            return null;
        }
        if (o instanceof NutsDescribable) {
            return ((NutsDescribable) o).describe(elems);
        }
        return null;
    }

    public static NutsRunnable ofRunnable(Runnable runnable, Function<NutsElements, NutsElement> nfo) {
        return new NamedRunnable(runnable, nfo);
    }

//    public static NutsRunnable ofRunnable(Runnable runnable, NutsElement n) {
//        return new NamedRunnable(runnable, e -> n);
//    }

//    public static NutsRunnable ofRunnable(Runnable runnable, String n) {
//        return new NamedRunnable(runnable, e -> e.ofString(n));
//    }

//    public static <F, T> NutsFunction<F, T> ofFunction(Function<F, T> fun, String name) {
//        return new NamedFunction<>(fun, e -> e.ofString(name));
//    }

    public static <F, T> NutsUnsafeFunction<F, T> ofUnsafeFunction(NutsUnsafeFunctionBase<F, T> fun, Function<NutsElements, NutsElement> nfo) {
        return new NamedUnsafeFunction<>(fun, nfo);
    }

//    public static <F, T> NutsUnsafeFunction<F, T> ofUnsafeFunction(NutsUnsafeFunctionBase<F, T> fun, String name) {
//        return new NamedUnsafeFunction<>(fun, e -> e.ofString(name));
//    }

//    public static <F, T> NutsFunction<F, T> ofFunction(Function<F, T> fun, NutsElement name) {
//        return new NamedFunction<>(fun, e -> name);
//    }

    public static <F, T> NutsFunction<F, T> ofFunction(Function<F, T> fun, Function<NutsElements, NutsElement> name) {
        return new NamedFunction<>(fun, name);
    }

    public static <T> NutsComparator<T> ofComparator(Comparator<T> fun, Function<NutsElements, NutsElement> name) {
        return new NamedComparator<>(fun, name);
    }

    public static <T> NutsPredicate<T> ofPredicate(Predicate<T> fun, Function<NutsElements, NutsElement> nfo) {
        return new NamedPredicate<>(fun, nfo);
    }

//    public static <T> NutsPredicate<T> ofPredicate(Predicate<T> fun, NutsElement name) {
//        return new NamedPredicate<>(fun, e -> name);
//    }

//    public static <T> NutsPredicate<T> ofPredicate(Predicate<T> fun, String name) {
//        return new NamedPredicate<>(fun, e -> e.ofString(name));
//    }

    public static <T> NutsIterator<T> ofIterator(Iterator<T> fun, Function<NutsElements, NutsElement> nfo) {
        return new NamedIterator<>(fun, nfo);
    }

//    public static <T> NutsIterator<T> ofIterator(Iterator<T> fun, NutsElement name) {
//        return new NamedIterator<>(fun, e -> name);
//    }

//    public static <T> NutsIterator<T> ofIterator(Iterator<T> fun, String name) {
//        return new NamedIterator<>(fun, e -> e.ofString(name));
//    }

    public static <T> NutsIterable<T> ofIterable(Iterable<T> fun, Function<NutsElements, NutsElement> nfo) {
        return new NamedIterable<>(fun, nfo);
    }

//    public static <T> NutsIterable<T> ofIterable(Iterable<T> fun, NutsElement name) {
//        return new NamedIterable<>(fun, e -> name);
//    }

//    public static <T> NutsIterable<T> ofIterable(Iterable<T> fun, String name) {
//        return new NamedIterable<>(fun, e -> e.ofString(name));
//    }

    private static class NamedPredicate<T> extends NutsPredicates.BasePredicate<T> {
        private final Predicate<T> base;
        private final Function<NutsElements, NutsElement> nfo;

        public NamedPredicate(Predicate<T> base, Function<NutsElements, NutsElement> nfo) {
            this.base = base;
            this.nfo = nfo;
        }

        @Override
        public boolean test(T t) {
            return base.test(t);
        }

        @Override
        public String toString() {
            return "NamedPredicate";
        }

        @Override
        public NutsElement describe(NutsElements elems) {
            NutsObjectElement b = NutsDescribables.resolveOr(base, elems, () -> elems.ofObject().build())
                    .asSafeObject(true);
            NutsElement a = nfo.apply(elems);
            if (b.isEmpty()) {
                return a;
            }
            if (a.isObject()) {
                return b.builder()
                        .addAll(a.asObject())
                        .build()
                        ;
            } else {
                return b.builder()
                        .set("name", a)
                        .build()
                        ;
            }
        }
    }

    private static class NamedIterator<T> implements NutsIterator<T> {
        private final Iterator<T> base;
        private final Function<NutsElements, NutsElement> nfo;

        public NamedIterator(Iterator<T> base, Function<NutsElements, NutsElement> nfo) {
            this.base = base;
            this.nfo = nfo;
        }

        @Override
        public boolean hasNext() {
            return base.hasNext();
        }

        @Override
        public T next() {
            return base.next();
        }

        @Override
        public String toString() {
            return "NamedIterator";
        }

        @Override
        public NutsElement describe(NutsElements elems) {
            NutsObjectElement b = NutsDescribables.resolveOr(base, elems, () -> elems.ofObject().build())
                    .asSafeObject(true);
            NutsElement a = nfo.apply(elems);
            if (b.isEmpty()) {
                return a;
            }
            if (a.isObject()) {
                return b.builder()
                        .addAll(a.asObject())
                        .build()
                        ;
            } else {
                return b.builder()
                        .set("name", a)
                        .build()
                        ;
            }
        }
    }

    private static class NamedIterable<T> implements NutsIterable<T> {
        private final Iterable<T> base;
        private final Function<NutsElements, NutsElement> nfo;

        public NamedIterable(Iterable<T> base, Function<NutsElements, NutsElement> nfo) {
            this.base = base;
            this.nfo = nfo;
        }

        @Override
        public NutsIterator<T> iterator() {
            return NutsIterator.of(base.iterator(), nfo);
        }

        @Override
        public String toString() {
            return "NamedIterable";
        }

        @Override
        public NutsElement describe(NutsElements elems) {
            NutsObjectElement b = NutsDescribables.resolveOr(base, elems, () -> elems.ofObject().build())
                    .asSafeObject(true);
            NutsElement a = nfo.apply(elems);
            if (b.isEmpty()) {
                return a;
            }
            if (a.isObject()) {
                return b.builder()
                        .addAll(a.asObject())
                        .build()
                        ;
            } else {
                return b.builder()
                        .set("name", a)
                        .build()
                        ;
            }
        }
    }

    private static class NamedFunction<F, T> implements NutsFunction<F, T> {
        private final Function<F, T> base;
        private final Function<NutsElements, NutsElement> nfo;

        public NamedFunction(Function<F, T> base, Function<NutsElements, NutsElement> nfo) {
            this.base = base;
            this.nfo = nfo;
        }

        @Override
        public T apply(F f) {
            return base.apply(f);
        }

        @Override
        public String toString() {
            return "NamedFunction{" + base + '}';
        }

        @Override
        public NutsElement describe(NutsElements elems) {
            return nfo.apply(elems);
//            NutsObjectElement b = NutsDescribables.resolveOrDestruct(base, elems)
//                    .asSafeObject(true);
//            NutsElement a = nfo.apply(elems);
//            if (b.isEmpty()) {
//                return a;
//            }
//            if (a.isObject()) {
//                return b.builder()
//                        .addAll(a.asObject())
//                        .build()
//                        ;
//            } else {
//                return b.builder()
//                        .set("name", a)
//                        .build()
//                        ;
//            }
        }
    }

    private static class NamedComparator<T> implements NutsComparator<T> {
        private final Comparator<T> base;
        private final Function<NutsElements, NutsElement> nfo;

        public NamedComparator(Comparator<T> base, Function<NutsElements, NutsElement> nfo) {
            this.base = base;
            this.nfo = nfo;
        }

        @Override
        public int compare(T o1, T o2) {
            return base.compare(o1, o2);
        }

        @Override
        public String toString() {
            return "NamedComparator{" + base + '}';
        }

        @Override
        public NutsElement describe(NutsElements elems) {
            NutsObjectElement b = NutsDescribables.resolveOrDestruct(base, elems)
                    .asSafeObject(true);
            NutsElement a = nfo.apply(elems);
            if (b.isEmpty()) {
                return a;
            }
            if (a.isObject()) {
                return b.builder()
                        .addAll(a.asObject())
                        .build()
                        ;
            } else {
                return b.builder()
                        .set("name", a)
                        .build()
                        ;
            }
        }
    }

    private static class NamedUnsafeFunction<F, T> implements NutsUnsafeFunction<F, T> {
        private final NutsUnsafeFunctionBase<F, T> base;
        private final Function<NutsElements, NutsElement> nfo;

        public NamedUnsafeFunction(NutsUnsafeFunctionBase<F, T> base, Function<NutsElements, NutsElement> nfo) {
            this.base = base;
            this.nfo = nfo;
        }

        @Override
        public T apply(F f) throws Exception {
            return base.apply(f);
        }

        @Override
        public String toString() {
            return "UnsafeFunction{" + base + '}';
        }

        @Override
        public NutsElement describe(NutsElements elems) {
            NutsObjectElement b = NutsDescribables.resolveOrDestruct(base, elems)
                    .asSafeObject(true);
            NutsElement a = nfo.apply(elems);
            if (b.isEmpty()) {
                return a;
            }
            if (a.isObject()) {
                return b.builder()
                        .addAll(a.asObject())
                        .build()
                        ;
            } else {
                return b.builder()
                        .set("name", a)
                        .build()
                        ;
            }
        }
    }

    private static class NamedRunnable implements NutsRunnable {
        private final Runnable base;
        private final Function<NutsElements, NutsElement> nfo;

        public NamedRunnable(Runnable base, Function<NutsElements, NutsElement> nfo) {
            this.base = base;
            this.nfo = nfo;
        }

        @Override
        public NutsElement describe(NutsElements elems) {
            return nfo.apply(elems);
        }

        @Override
        public void run() {
            base.run();
        }
    }
}

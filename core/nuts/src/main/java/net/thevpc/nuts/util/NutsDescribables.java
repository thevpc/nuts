/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.elem.NutsObjectElement;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * {@code NutsDescribable} Helper Class
 */
public final class NutsDescribables {

    private NutsDescribables() {
    }

    public static NutsElement resolveOrToString(Object o, NutsSession session) {
        return resolveOr(o, session, () -> NutsElements.of(session).ofString(o.toString()));
    }

    public static NutsElement resolveOr(Object o, NutsSession session, Supplier<NutsElement> d) {
        NutsElement e = resolveOrNull(o, session);
        if (e != null) {
            return e;
        }
        return d == null ? null : d.get();
    }

    public static NutsObjectElement resolveOrDestructAsObject(Object o, NutsSession session) {
        NutsElement e = resolveOrDestruct(o, session);
        if(e instanceof NutsObjectElement){
            return (NutsObjectElement) e;
        }
        return NutsElements.of(session)
                .ofObject()
                .set("value",e)
                .build();
    }
    public static NutsElement resolveOrDestruct(Object o, NutsSession session) {
        NutsElement e = resolveOrNull(o, session);
        if (e != null) {
            return e;
        }
        return NutsElements.of(session).toElement(o);
    }

    public static void cast(Object o) {
        if (!isSupported(o)) {
            throw new IllegalArgumentException(NutsMessage.ofCstyle("not a valid NutsDescribable object %s : %s", o.getClass().getName(), o.toString()).toString());
        }
    }

    public static boolean isSupported(Object o) {
        if (o == null) {
            return true;
        }
        return o instanceof NutsDescribable;
    }

    public static NutsElement resolveOrNull(Object o, NutsSession session) {
        if (o == null) {
            return null;
        }
        if (o instanceof NutsDescribable) {
            return ((NutsDescribable) o).describe(session);
        }
        return null;
    }

    public static NutsRunnable ofRunnable(Runnable runnable, Function<NutsSession, NutsElement> nfo) {
        return new NamedRunnable(runnable, nfo);
    }

    public static <F, T> NutsUnsafeFunction<F, T> ofUnsafeFunction(NutsUnsafeFunctionBase<F, T> fun, Function<NutsSession, NutsElement> nfo) {
        return new NamedUnsafeFunction<>(fun, nfo);
    }

    public static <F, T> NutsFunction<F, T> ofFunction(Function<F, T> fun, Function<NutsSession, NutsElement> name) {
        return new NamedFunction<>(fun, name);
    }

    public static <T> NutsComparator<T> ofComparator(Comparator<T> fun, Function<NutsSession, NutsElement> name) {
        return new NamedComparator<>(fun, name);
    }

    public static <T> NutsPredicate<T> ofPredicate(Predicate<T> fun, Function<NutsSession, NutsElement> nfo) {
        return new NamedPredicate<>(fun, nfo);
    }

    public static <T> NutsIterator<T> ofIterator(Iterator<T> fun, Function<NutsSession, NutsElement> nfo) {
        return new NamedIterator<>(fun, nfo);
    }

    public static <T> NutsIterable<T> ofIterable(Iterable<T> fun, Function<NutsSession, NutsElement> nfo) {
        return new NamedIterable<>(fun, nfo);
    }

    private static class NamedPredicate<T> extends NutsPredicates.BasePredicate<T> {
        private final Predicate<T> base;
        private final Function<NutsSession, NutsElement> nfo;

        public NamedPredicate(Predicate<T> base, Function<NutsSession, NutsElement> nfo) {
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
        public NutsElement describe(NutsSession session) {
            NutsObjectElement b = NutsDescribables.resolveOr(base, session, () -> NutsElements.of(session).ofObject().build())
                    .asObject().get(session);
            NutsElement a = nfo.apply(session);
            if (b.isEmpty()) {
                return a;
            }
            if (a.isObject()) {
                return b.builder()
                        .addAll(a.asObject().get(session))
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
        private final Function<NutsSession, NutsElement> nfo;

        public NamedIterator(Iterator<T> base, Function<NutsSession, NutsElement> nfo) {
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
        public NutsElement describe(NutsSession session) {
            NutsObjectElement b = NutsDescribables.resolveOr(base, session, () -> NutsElements.of(session).ofObject().build())
                    .asObject().get(session);
            NutsElement a = nfo.apply(session);
            if (b.isEmpty()) {
                return a;
            }
            if (a.isObject()) {
                return b.builder()
                        .addAll(a.asObject().get(session))
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
        private final Function<NutsSession, NutsElement> nfo;

        public NamedIterable(Iterable<T> base, Function<NutsSession, NutsElement> nfo) {
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
        public NutsElement describe(NutsSession session) {
            NutsObjectElement b = NutsDescribables.resolveOr(base, session, () -> NutsElements.of(session).ofObject().build())
                    .asObject().get(session);
            NutsElement a = nfo.apply(session);
            if (b.isEmpty()) {
                return a;
            }
            if (a.isObject()) {
                return b.builder()
                        .addAll(a.asObject().get(session))
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
        private final Function<NutsSession, NutsElement> nfo;

        public NamedFunction(Function<F, T> base, Function<NutsSession, NutsElement> nfo) {
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
        public NutsElement describe(NutsSession session) {
            return nfo.apply(session);
        }
    }

    private static class NamedComparator<T> implements NutsComparator<T> {
        private final Comparator<T> base;
        private final Function<NutsSession, NutsElement> nfo;

        public NamedComparator(Comparator<T> base, Function<NutsSession, NutsElement> nfo) {
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
        public NutsElement describe(NutsSession session) {
            NutsObjectElement b = NutsDescribables.resolveOrDestructAsObject(base, session);
            NutsElement a = nfo.apply(session);
            if (b.isEmpty()) {
                return a;
            }
            if (a.isObject()) {
                return b.builder()
                        .addAll(a.asObject().get(session))
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
        private final Function<NutsSession, NutsElement> nfo;

        public NamedUnsafeFunction(NutsUnsafeFunctionBase<F, T> base, Function<NutsSession, NutsElement> nfo) {
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
        public NutsElement describe(NutsSession session) {
            NutsObjectElement b = NutsDescribables.resolveOrDestructAsObject(base, session);
            NutsElement a = nfo.apply(session);
            if (b.isEmpty()) {
                return a;
            }
            if (a.isObject()) {
                return b.builder()
                        .addAll(a.asObject().get(session))
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
        private final Function<NutsSession, NutsElement> nfo;

        public NamedRunnable(Runnable base, Function<NutsSession, NutsElement> nfo) {
            this.base = base;
            this.nfo = nfo;
        }

        @Override
        public NutsElement describe(NutsSession session) {
            return nfo.apply(session);
        }

        @Override
        public void run() {
            base.run();
        }
    }
}

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
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElement;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * {@code NutsDescribable} Helper Class
 */
public final class NDescribables {

    private NDescribables() {
    }

    public static NElement resolveOrToString(Object o, NSession session) {
        return resolveOr(o, session, () -> NElements.of(session).ofString(o.toString()));
    }

    public static NElement resolveOr(Object o, NSession session, Supplier<NElement> d) {
        NElement e = resolveOrNull(o, session);
        if (e != null) {
            return e;
        }
        return d == null ? null : d.get();
    }

    public static NObjectElement resolveOrDestructAsObject(Object o, NSession session) {
        NElement e = resolveOrDestruct(o, session);
        if(e instanceof NObjectElement){
            return (NObjectElement) e;
        }
        return NElements.of(session)
                .ofObject()
                .set("value",e)
                .build();
    }
    public static NElement resolveOrDestruct(Object o, NSession session) {
        NElement e = resolveOrNull(o, session);
        if (e != null) {
            return e;
        }
        return NElements.of(session).toElement(o);
    }

    public static void cast(Object o) {
        if (!isSupported(o)) {
            throw new IllegalArgumentException(NMsg.ofC("not a valid NutsDescribable object %s : %s", o.getClass().getName(), o.toString()).toString());
        }
    }

    public static boolean isSupported(Object o) {
        if (o == null) {
            return true;
        }
        return o instanceof NDescribable;
    }

    public static NElement resolveOrNull(Object o, NSession session) {
        if (o == null) {
            return null;
        }
        if (o instanceof NDescribable) {
            return ((NDescribable) o).describe(session);
        }
        return null;
    }

    public static NRunnable ofRunnable(Runnable runnable, Function<NSession, NElement> nfo) {
        return new NamedRunnable(runnable, nfo);
    }

    public static <F, T> NUnsafeFunction<F, T> ofUnsafeFunction(NUnsafeFunctionBase<F, T> fun, Function<NSession, NElement> nfo) {
        return new NamedUnsafeFunction<>(fun, nfo);
    }

    public static <F, T> NFunction<F, T> ofFunction(Function<F, T> fun, Function<NSession, NElement> name) {
        return new NamedFunction<>(fun, name);
    }

    public static <T> NComparator<T> ofComparator(Comparator<T> fun, Function<NSession, NElement> name) {
        return new NamedComparator<>(fun, name);
    }

    public static <T> NPredicate<T> ofPredicate(Predicate<T> fun, Function<NSession, NElement> nfo) {
        return new NamedPredicate<>(fun, nfo);
    }

    public static <T> NIterator<T> ofIterator(Iterator<T> fun, Function<NSession, NElement> nfo) {
        return new NamedIterator<>(fun, nfo);
    }

    public static <T> NIterable<T> ofIterable(Iterable<T> fun, Function<NSession, NElement> nfo) {
        return new NamedIterable<>(fun, nfo);
    }

    private static class NamedPredicate<T> extends NPredicates.BasePredicate<T> {
        private final Predicate<T> base;
        private final Function<NSession, NElement> nfo;

        public NamedPredicate(Predicate<T> base, Function<NSession, NElement> nfo) {
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
        public NElement describe(NSession session) {
            NObjectElement b = NDescribables.resolveOr(base, session, () -> NElements.of(session).ofObject().build())
                    .asObject().get(session);
            NElement a = nfo.apply(session);
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

    private static class NamedIterator<T> implements NIterator<T> {
        private final Iterator<T> base;
        private final Function<NSession, NElement> nfo;

        public NamedIterator(Iterator<T> base, Function<NSession, NElement> nfo) {
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
        public NElement describe(NSession session) {
            NObjectElement b = NDescribables.resolveOr(base, session, () -> NElements.of(session).ofObject().build())
                    .asObject().get(session);
            NElement a = nfo.apply(session);
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

    private static class NamedIterable<T> implements NIterable<T> {
        private final Iterable<T> base;
        private final Function<NSession, NElement> nfo;

        public NamedIterable(Iterable<T> base, Function<NSession, NElement> nfo) {
            this.base = base;
            this.nfo = nfo;
        }

        @Override
        public NIterator<T> iterator() {
            return NIterator.of(base.iterator(), nfo);
        }

        @Override
        public String toString() {
            return "NamedIterable";
        }

        @Override
        public NElement describe(NSession session) {
            NObjectElement b = NDescribables.resolveOr(base, session, () -> NElements.of(session).ofObject().build())
                    .asObject().get(session);
            NElement a = nfo.apply(session);
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

    private static class NamedFunction<F, T> implements NFunction<F, T> {
        private final Function<F, T> base;
        private final Function<NSession, NElement> nfo;

        public NamedFunction(Function<F, T> base, Function<NSession, NElement> nfo) {
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
        public NElement describe(NSession session) {
            return nfo.apply(session);
        }
    }

    private static class NamedComparator<T> implements NComparator<T> {
        private final Comparator<T> base;
        private final Function<NSession, NElement> nfo;

        public NamedComparator(Comparator<T> base, Function<NSession, NElement> nfo) {
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
        public NElement describe(NSession session) {
            NObjectElement b = NDescribables.resolveOrDestructAsObject(base, session);
            NElement a = nfo.apply(session);
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

    private static class NamedUnsafeFunction<F, T> implements NUnsafeFunction<F, T> {
        private final NUnsafeFunctionBase<F, T> base;
        private final Function<NSession, NElement> nfo;

        public NamedUnsafeFunction(NUnsafeFunctionBase<F, T> base, Function<NSession, NElement> nfo) {
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
        public NElement describe(NSession session) {
            NObjectElement b = NDescribables.resolveOrDestructAsObject(base, session);
            NElement a = nfo.apply(session);
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

    private static class NamedRunnable implements NRunnable {
        private final Runnable base;
        private final Function<NSession, NElement> nfo;

        public NamedRunnable(Runnable base, Function<NSession, NElement> nfo) {
            this.base = base;
            this.nfo = nfo;
        }

        @Override
        public NElement describe(NSession session) {
            return nfo.apply(session);
        }

        @Override
        public void run() {
            base.run();
        }
    }
}

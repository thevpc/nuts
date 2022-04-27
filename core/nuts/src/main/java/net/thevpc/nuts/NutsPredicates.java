/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * NutsPredicate Helper Class
 */
public class NutsPredicates {
    private static final Never NEVER = new Never();
    private static final Always ALWAYS = new Always();
    private static final Null NULL = new Null();

    private static final Blank BLANK = new Blank();

    @SuppressWarnings("unchecked")
    public static <T> NutsPredicate<T> never() {
        return NEVER;
    }

    @SuppressWarnings("unchecked")
    public static NutsPredicate<String> blank() {
        return BLANK;
    }

    @SuppressWarnings("unchecked")
    public static <T> NutsPredicate<T> always() {
        return ALWAYS;
    }

    @SuppressWarnings("unchecked")
    public static <T> NutsPredicate<T> isNull() {
        return NULL;
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> nonNull() {
        return NULL.negate();
    }

    private static String withPars(String sl) {
        if (!sl.matches("\\w")) {
            return "(" + sl + ")";
        }
        return sl;
    }

    public static abstract class BaseOpPredicate<T> extends BasePredicate<T> {

    }

    public static abstract class BasePredicate<T> implements NutsPredicate<T> {
        @Override
        public NutsPredicate<T> and(Predicate<? super T> other) {
            return new And<T>(this, other);
        }

        @Override
        public NutsPredicate<T> negate() {
            return new Not<>(this);
        }

        @Override
        public NutsPredicate<T> or(Predicate<? super T> other) {
            return new Or<T>(this, other);
        }

        @Override
        public NutsElement describe(NutsSession session) {
            return NutsElements.of(session).ofString(toString());
        }
    }

    private static class Never<T> extends BaseOpPredicate<T> {
        @Override
        public boolean test(T t) {
            return false;
        }

        @Override
        public int hashCode() {
            return getClass().getName().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Never;
        }

        @Override
        public String toString() {
            return "never";
        }
    }

    private static class Always<T> extends BaseOpPredicate<T> {
        @Override
        public boolean test(T t) {
            return true;
        }

        @Override
        public int hashCode() {
            return getClass().getName().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Always;
        }

        @Override
        public String toString() {
            return "always";
        }
    }

    private static class Null<T> extends BaseOpPredicate<T> implements Serializable {
        @Override
        public boolean test(T t) {
            return t == null;
        }

        @Override
        public int hashCode() {
            return getClass().getName().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Null;
        }

        @Override
        public String toString() {
            return "isNull";
        }
    }

    private static class Blank<T> extends BaseOpPredicate<T> implements Serializable {
        @Override
        public boolean test(T t) {
            if (t == null) {
                return true;
            }
            if (t instanceof CharSequence) {
                return t.toString().trim().isEmpty();
            }
            if (t instanceof NutsBlankable) {
                return ((NutsBlankable) t).isBlank();
            }
            if (t instanceof char[]) {
                return NutsBlankable.isBlank((char[]) t);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getClass().getName().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Blank;
        }

        @Override
        public String toString() {
            return "blank";
        }
    }

    public static class Not<T> extends BaseOpPredicate<T> implements Serializable {
        private final Predicate<T> base;

        public Not(Predicate<T> base) {
            if (base == null) {
                throw new NullPointerException();
            }
            this.base = base;
        }

        @Override
        public boolean test(T t) {
            return !base.test(t);
        }

        @Override
        public int hashCode() {
            return Objects.hash(base);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Not<?> not = (Not<?>) o;
            return base.equals(not.base);
        }

        @Override
        public String toString() {
            return "!" + withPars(base.toString());
        }
    }

    public static class Or<T> extends BaseOpPredicate<T> {
        private final Predicate<T> left;
        private final Predicate<? super T> right;

        public Or(Predicate<T> left, Predicate<? super T> right) {
            if (left == null) {
                throw new NullPointerException();
            }
            if (right == null) {
                throw new NullPointerException();
            }
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean test(T t) {
            if (left.test(t)) {
                return true;
            }
            return right.test(t);
        }

        @Override
        public int hashCode() {
            return Objects.hash(left, right);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Or<?> or = (Or<?>) o;
            return Objects.equals(left, or.left) && Objects.equals(right, or.right);
        }

        @Override
        public String toString() {
            return withPars(left.toString()) + " | " + withPars(right.toString());
        }
    }

    public static class And<T> extends BaseOpPredicate<T> {
        private final Predicate<T> left;
        private final Predicate<? super T> right;

        public And(Predicate<T> left, Predicate<? super T> right) {
            if (left == null) {
                throw new NullPointerException();
            }
            if (right == null) {
                throw new NullPointerException();
            }
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean test(T t) {
            if (!left.test(t)) {
                return false;
            }
            return right.test(t);
        }

        @Override
        public int hashCode() {
            return Objects.hash(left, right);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            And<?> and = (And<?>) o;
            return Objects.equals(left, and.left) && Objects.equals(right, and.right);
        }

        @Override
        public String toString() {
            return withPars(left.toString()) + " & " + withPars(right.toString());
        }
    }
}

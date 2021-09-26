package net.thevpc.nuts;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Predicate;

public class NutsPredicates {
    private static final Never NEVER = new Never();
    private static final Always ALWAYS = new Always();
    private static final Null NULL = new Null();

    private static final Blank BLANK = new Blank();

    public static <T> Predicate<T> never() {
        return NEVER;
    }

    public static Predicate<String> blank() {
        return BLANK;
    }

    public static <T> Predicate<T> always() {
        return ALWAYS;
    }

    public static <T> Predicate<T> isNull() {
        return NULL;
    }

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

    public static abstract class BasePredicate<T> implements Predicate<T> {
        @Override
        public Predicate<T> and(Predicate<? super T> other) {
            return new And<T>(this, other);
        }

        @Override
        public Predicate<T> negate() {
            return new Not<>(this);
        }

        @Override
        public Predicate<T> or(Predicate<? super T> other) {
            return new Or<T>(this, other);
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

    private static class Blank extends BaseOpPredicate<String> implements Serializable {
        @Override
        public boolean test(String t) {
            return t == null || t.trim().isEmpty();
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
            return "bla,k";
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

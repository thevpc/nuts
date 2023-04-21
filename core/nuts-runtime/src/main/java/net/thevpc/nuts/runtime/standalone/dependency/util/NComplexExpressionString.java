package net.thevpc.nuts.runtime.standalone.dependency.util;

public interface NComplexExpressionString {
    static String toString(Object a) {
        if (a instanceof NComplexExpressionString) {
            return "(" + a + ")";
        }
        return String.valueOf(a);
    }
}

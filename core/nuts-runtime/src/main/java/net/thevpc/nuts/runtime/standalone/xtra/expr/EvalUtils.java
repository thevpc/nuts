package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.NutsExprNode;

import java.math.BigDecimal;
import java.math.BigInteger;

public class EvalUtils {

    public static String castToString(Object o) {
        if (o == null) {
            return null;
        }
        return o.toString();
    }

    public static boolean castToBoolean(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        if (o instanceof Number) {
            return ((Number) o).doubleValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(o));
    }

    public static double castToDouble(Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? 1 : 0;
        }
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        return Double.parseDouble(String.valueOf(o));
    }

    public static boolean isFloat(Number o) {
        return
                (o instanceof Float)
                        || (o instanceof Double)
                        || (o instanceof BigDecimal);
    }

    public static boolean isOrdinal(Number o) {
        return o != null && !isFloat(o);
    }

    public static boolean isNumber(Object o) {
        try {
            castToNumber(o);
            return true;
        } catch (Exception e) {
            //
        }
        return false;
    }

    public static Number castToNumber(Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? 1 : 0;
        }
        if (o instanceof Number) {
            return (Number) o;
        }
        String s = String.valueOf(o);
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            //
        }
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            //
        }
        try {
            return Float.parseFloat(s);
        } catch (Exception e) {
            //
        }
        return Double.parseDouble(s);
    }

    public static long castToLong(Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? 1 : 0;
        }
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        return Long.parseLong(String.valueOf(o));
    }

    public static boolean isBoolean(Object o) {
        return o instanceof Boolean ||
                ((o instanceof String)
                        && (o.toString().equalsIgnoreCase("true") || o.toString().equalsIgnoreCase("false"))
                );
    }

    public static boolean isBig(Number o) {
        return o instanceof BigDecimal || o instanceof BigInteger;
    }

    public static Number[] promoteNumbers(Number a, Number b) {
        if (EvalUtils.isBig(a) || EvalUtils.isBig(b)) {
            if (EvalUtils.isFloat(a) || EvalUtils.isFloat(b)) {
                BigDecimal aa = (a instanceof BigDecimal) ? ((BigDecimal) a) : new BigDecimal(a.toString());
                BigDecimal bb = (b instanceof BigDecimal) ? ((BigDecimal) b) : new BigDecimal(b.toString());
                return new Number[]{aa, bb};
            }
            BigInteger aa = (a instanceof BigInteger) ? ((BigInteger) a) : new BigInteger(a.toString());
            BigInteger bb = (b instanceof BigInteger) ? ((BigInteger) b) : new BigInteger(b.toString());
            return new Number[]{aa, bb};
        } else {
            if (EvalUtils.isFloat(a) || EvalUtils.isFloat(b)) {
                double aa = a.doubleValue();
                double bb = b.doubleValue();
                return new Number[]{aa, bb};
            }
            long aa = a.longValue();
            long bb = b.longValue();
            return new Number[]{aa, bb};
        }
    }

    public static String wrapPars(NutsExprNode n) {
        if (n instanceof DefaultLiteralNode) {
            String s = n.toString();
            if (s.charAt(0) == '-' || s.charAt(0) == '+') {
                return "(" + s + ")";
            }
            return s;
        }
        if (n instanceof DefaultVarNode) {
            return n.toString();
        }
        if (n instanceof DefaultOpNode) {
            String s = n.toString();
            switch (s.charAt(0)) {
                case '-':
                case '+':
                case '!': {
                    return "(" + s + ")";
                }
            }
            return s;
        }
        return "(" + n + ")";
    }
}

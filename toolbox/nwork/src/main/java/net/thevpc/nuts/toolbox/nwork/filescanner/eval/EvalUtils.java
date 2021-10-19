package net.thevpc.nuts.toolbox.nwork.filescanner.eval;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Pattern;

public class EvalUtils {
    public static Pattern glob(String o) {
        while(true) {
            if (o.endsWith("/**/*")) {
                o = o.substring(0, o.length() - 5);
            }else if (o.endsWith("/**")) {
                o = o.substring(0, o.length() - 3);
            }else if (o.endsWith("/*")) {
                o = o.substring(0, o.length() - 2);
            }else{
                break;
            }
        }
        if(o.isEmpty()){
            return Pattern.compile(".*");
        }
        StringBuilder sb=new StringBuilder();
        char[] chars = o.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '.':
                case '{':
                case '}':
                case '<':
                case '>':{
                    sb.append('\\');
                    sb.append(c);
                    break;
                }
                case '*': {
                    if(i+1 < chars.length && chars[i+1]=='*'){
                        if(i+2 < chars.length && chars[i+2]=='/'){
                            i++;
                            if(i+3 < chars.length) {
                                sb.append(".*/");
                            }else{
                                sb.append(".*");
                            }
                        }else {
                            i++;
                            sb.append(".*");
                        }
                    }else {
                        sb.append("[^/]*");
                    }
                    break;
                }
                case '?': {
                    sb.append("[^/]?");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return Pattern.compile(sb.toString());
    }
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
}

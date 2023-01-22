package net.thevpc.nuts;

import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.util.NStringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

public class DefaultNLiteral implements NLiteral {
    private static NLiteral NULL = new DefaultNLiteral(null);

    public static NLiteral of(Object any) {
        if (any == null) {
            return NULL;
        }
        if (any instanceof NLiteral) {
            return (NLiteral) any;
        }
        return new DefaultNLiteral(any);
    }

    public static final String[] DATE_FORMATS = {
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX"
    };

    private Object value;
    private NElementType type;

    public static NOptional<Instant> parseInstant(String text) {
        for (String f : DATE_FORMATS) {
            try {
                return NOptional.of(new SimpleDateFormat(f).parse(text).toInstant());
            } catch (Exception ex) {
                //
            }
        }
        return NOptional.ofError(session -> NMsg.ofC("invalid Instant %s", text));
    }

    public DefaultNLiteral(Object value) {
        this.value = value;
    }

    private static NElementType resolveType(Object value) {
        if (value == null) {
            return NElementType.NULL;
        } else {
            switch (value.getClass().getName()) {
                case "java.lang.Byte":
                    return NElementType.BYTE;
                case "java.lang.Short":
                    return NElementType.SHORT;
                case "java.lang.Integer":
                    return NElementType.INTEGER;
                case "java.lang.Long":
                    return NElementType.LONG;
                case "java.math.BigInteger":
                    return NElementType.BIG_INTEGER;
                case "java.lang.Float":
                    return NElementType.FLOAT;
                case "java.lang.Double":
                    return NElementType.DOUBLE;
                case "java.math.BigDecimal":
                    return NElementType.BIG_DECIMAL;
                case "java.lang.String":
                case "java.lang.StringBuilder":
                case "java.lang.StringBuffer":
                    return NElementType.STRING;
                case "java.util.Date":
                case "java.time.Instant":
                    return NElementType.INSTANT;
                case "java.lang.Boolean":
                    return NElementType.BOOLEAN;
            }
            if (value instanceof Number) {
                return NElementType.FLOAT;
            }
            if (value instanceof CharSequence) {
                return NElementType.STRING;
            }
            return NElementType.OBJECT;
        }
    }

    public NElementType type() {
        if (type == null) {
            type = resolveType(value);
        }
        return type;
    }

    @Override
    public Object getRaw() {
        return value;
    }

    @Override
    public NOptional<Instant> asInstant() {
        if (value == null) {
            return NOptional.ofEmpty(session -> NMsg.ofPlain("empty instant"));
        }
        if (value instanceof Boolean) {
            return NOptional.ofEmpty(session -> NMsg.ofPlain("cannot convert boolean to Instant"));
        }
        if (value instanceof Date) {
            return NOptional.of(((Date) value).toInstant());
        }
        if (value instanceof Instant) {
            return NOptional.of(((Instant) value));
        }
        String s = String.valueOf(value);
        if (s.trim().isEmpty()) {
            return NOptional.ofEmpty(session -> NMsg.ofPlain("empty instant"));
        }
        try {
            return NOptional.of(DateTimeFormatter.ISO_INSTANT.parse(s, Instant::from));
        } catch (Exception ex) {
            //
        }
        for (String f : DATE_FORMATS) {
            try {
                return NOptional.of(new SimpleDateFormat(f).parse(s).toInstant());
            } catch (Exception ex) {
                //
            }
        }
        if (isLong()) {
            try {
                try {
                    return NOptional.of(Instant.ofEpochMilli(((Number) value).longValue()));
                } catch (Exception any) {
                    //
                }
            } catch (Exception ex) {
                //
            }
        }
        if (isNumber()) {
            return NOptional.ofEmpty(session -> NMsg.ofPlain("cannot convert number to Instant"));
        }
        return NOptional.ofEmpty(session -> NMsg.ofPlain("cannot convert to Instant"));
    }

    @Override
    public NOptional<Number> asNumber() {
        if (value == null) {
            return NOptional.ofEmpty(session -> NMsg.ofPlain("empty number"));
        }
        if (value instanceof Boolean) {
            return NOptional.of(((Boolean) value) ? 1 : 0);
        }
        if (value instanceof Number) {
            return NOptional.of((Number) value);
        }
        if (value instanceof Date) {
            return NOptional.of(((Date) value).getTime());
        }
        if (value instanceof Instant) {
            return NOptional.of(((Instant) value).toEpochMilli());
        }
        String s = String.valueOf(value);
        if (s.indexOf('.') >= 0 || s.toLowerCase().indexOf('e') >= 0) {
            try {
                return NOptional.of(Double.parseDouble(s));
            } catch (NumberFormatException ex) {
                //just ignore!
            }
            try {
                return NOptional.of(new BigDecimal(s));
            } catch (NumberFormatException ex) {
                //just ignore!
            }
        } else {
            try {
                return NOptional.of(Integer.parseInt(s));
            } catch (NumberFormatException ex) {
                //just ignore!
            }
            try {
                return NOptional.of(Long.parseLong(s));
            } catch (NumberFormatException ex) {
                //just ignore!
            }
            try {
                return NOptional.of(new BigInteger(s));
            } catch (NumberFormatException ex) {
                //just ignore!
            }
        }
        return NOptional.ofError(session -> NMsg.ofPlain("cannot convert to Number"));
    }

    @Override
    public NOptional<Boolean> asBoolean() {
        if (value == null) {
            return NOptional.ofEmpty(session -> NMsg.ofPlain("empty boolean"));
        }
        if (value instanceof Boolean) {
            return NOptional.of((Boolean) value);
        }
        if (value instanceof Number) {
            if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof BigInteger) {
                return NOptional.of(((Number) value).longValue() != 0);
            }
            if (value instanceof Float || value instanceof Double || value instanceof BigDecimal) {
                double d = ((Number) value).doubleValue();
                return NOptional.of(
                        d != 0 && !Double.isNaN(d)
                );
            }
        }
        String svalue = String.valueOf(value).trim().toLowerCase();
        if (svalue.isEmpty()) {
            return NOptional.ofEmpty(session -> NMsg.ofPlain("empty boolean"));
        }
        if (svalue.matches("true|enable|enabled|yes|always|y|on|ok|t|o")) {
            return NOptional.of(true);
        }
        if (svalue.matches("false|disable|disabled|no|none|never|n|off|ko|f")) {
            return NOptional.of(false);
        }
        return NOptional.ofError(session -> NMsg.ofC("invalid boolean %s", svalue));
    }

    public NOptional<Long> asLong() {
        if (isBlank()) {
            return NOptional.ofEmpty(session -> NMsg.ofPlain("empty Long"));
        }
        if (value instanceof Number) {
            if (value instanceof BigInteger) {
                long ln = ((BigInteger) value).longValue();
                if (BigInteger.valueOf(ln).equals(value)) {
                    return NOptional.of(ln);
                }
            }
            return NOptional.of(((Number) value).longValue());
        }
        if (value instanceof Date) {
            return NOptional.of(((Date) value).getTime());
        }
        if (value instanceof CharSequence) {
            String s = value.toString();
            if (s.indexOf('.') >= 0 || s.toLowerCase().indexOf('e') >= 0) {
                try {
                    double a = Double.parseDouble(s);
                    if (a == (long) a) {
                        return NOptional.of((long) a);
                    }
                } catch (NumberFormatException ex) {
                    // ignore
                }
                return NOptional.ofError(session -> NMsg.ofC("invalid Long %s", value));
            } else {
                try {
                    if (s.startsWith("0x")) {
                        return NOptional.of(Long.parseLong(s.substring(2), 16));
                    }
                    return NOptional.of(Long.parseLong(s));
                } catch (NumberFormatException ex) {
                    // ignore
                }
                return NOptional.ofError(session -> NMsg.ofC("invalid Long %s", value));
            }
        }
        if (value instanceof Boolean) {
            return NOptional.of(((Boolean) value) ? 1L : 0L);
        }
        return NOptional.ofError(session -> NMsg.ofC("invalid Long %s", value));
    }

    @Override
    public NOptional<Double> asDouble() {
        if (isBlank()) {
            return NOptional.ofEmpty(session -> NMsg.ofPlain("empty Double"));
        }
        if (value instanceof Double || value instanceof Float) {
            return NOptional.of(((Number) value).doubleValue());
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            return NOptional.of((((Number) value).doubleValue()));
        }
        if (value instanceof BigDecimal) {
            try {
                BigDecimal bd = (BigDecimal) value;
                BigDecimal abs = bd.abs();
                if (
                        abs.compareTo(BigDecimal.valueOf(Double.MIN_VALUE)) >= 0
                                && abs.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) <= 0
                ) {
                    return NOptional.of(bd.doubleValue());
                }
            } catch (Exception any) {
                //
            }
            return NOptional.ofError(session -> NMsg.ofC("invalid Double %s", value));
        }
        if (value instanceof BigInteger) {
            try {
                BigInteger bi = (BigInteger) value;
                BigDecimal bd = new BigDecimal(bi);
                BigDecimal abs = bd.abs();
                if (
                        abs.compareTo(BigDecimal.valueOf(Double.MIN_VALUE)) >= 0
                                && abs.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) <= 0
                ) {
                    return NOptional.of(bd.doubleValue());
                }
            } catch (Exception any) {
                //
            }
            return NOptional.ofError(session -> NMsg.ofC("invalid Double %s", value));
        }
        if (value instanceof Date) {
            return NOptional.of((double) ((Date) value).getTime());
        }
        if (value instanceof CharSequence) {
            String s = value.toString();
            try {
                return NOptional.of(Double.parseDouble(s));
            } catch (NumberFormatException ex) {
                // ignore
            }
        }
        return NOptional.ofError(session -> NMsg.ofC("invalid Double %s", value));
    }

    @Override
    public NOptional<Float> asFloat() {
        if (isBlank()) {
            return NOptional.ofEmpty(session -> NMsg.ofPlain("empty Float"));
        }
        if (value instanceof Float) {
            return NOptional.of(((Number) value).floatValue());
        }
        if (value instanceof Double) {
            Double d = (Double) value;
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                return NOptional.of(d.floatValue());
            }
            double abs = Math.abs(d);
            if (abs >= Float.MIN_VALUE && abs <= Float.MIN_VALUE) {
                return NOptional.of(d.floatValue());
            }
            return NOptional.ofEmpty(session -> NMsg.ofC("invalid Float %s", value));
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            double d = ((Number) value).doubleValue();
            double abs = Math.abs(d);
            if (abs >= Float.MIN_VALUE && abs <= Float.MIN_VALUE) {
                return NOptional.of((float) d);
            }
            return NOptional.ofEmpty(session -> NMsg.ofC("invalid Float %s", value));
        }
        if (value instanceof BigDecimal) {
            try {
                BigDecimal bd = (BigDecimal) value;
                BigDecimal abs = bd.abs();
                if (
                        abs.compareTo(BigDecimal.valueOf(Float.MIN_VALUE)) >= 0
                                && abs.compareTo(BigDecimal.valueOf(Float.MAX_VALUE)) <= 0
                ) {
                    return NOptional.of(bd.floatValue());
                }
            } catch (Exception any) {
                //
            }
            return NOptional.ofEmpty(session -> NMsg.ofC("invalid Float %s", value));
        }
        if (value instanceof BigInteger) {
            try {
                BigInteger bi = (BigInteger) value;
                BigDecimal bd = new BigDecimal(bi);
                BigDecimal abs = bd.abs();
                if (
                        abs.compareTo(BigDecimal.valueOf(Float.MIN_VALUE)) >= 0
                                && abs.compareTo(BigDecimal.valueOf(Float.MAX_VALUE)) <= 0
                ) {
                    return NOptional.of(bd.floatValue());
                }
            } catch (Exception any) {
                //
            }
            return NOptional.ofEmpty(session -> NMsg.ofC("invalid Float %s", value));
        }
        if (value instanceof CharSequence) {
            String s = value.toString();
            try {
                return NOptional.of(Float.parseFloat(s));
            } catch (NumberFormatException ex) {
                // ignore
            }
            return NOptional.ofEmpty(session -> NMsg.ofC("invalid Float %s", value));
        }
        return NOptional.ofEmpty(session -> NMsg.ofC("invalid Float %s", value));
    }


    @Override
    public NOptional<Byte> asByte() {
        return asLong()
                .ifEmptyUse(() -> NOptional.ofEmpty(session -> NMsg.ofPlain("empty Byte")))
                .ifErrorUse(() -> NOptional.ofError(session -> NMsg.ofC("invalid Byte : %s", getRaw())))
                .flatMap(value -> {
                    byte smallValue = value.byteValue();
                    if (!Long.valueOf(smallValue).equals(value)) {
                        return NOptional.ofError(session -> NMsg.ofC("invalid Byte : %s", getRaw()));
                    }
                    return NOptional.of(smallValue);
                });
    }

    @Override
    public NOptional<Short> asShort() {
        return asLong()
                .ifEmptyUse(() -> NOptional.ofEmpty(session -> NMsg.ofPlain("empty Short")))
                .ifErrorUse(() -> NOptional.ofError(session -> NMsg.ofC("invalid Short : %s", getRaw())))
                .flatMap(value -> {
                    short smallValue = value.shortValue();
                    if (!Long.valueOf(smallValue).equals(value)) {
                        return NOptional.ofError(session -> NMsg.ofC("invalid Short : %s", getRaw()));
                    }
                    return NOptional.of(smallValue);
                });
    }

    @Override
    public NOptional<Integer> asInt() {
        return asLong()
                .ifEmptyUse(() -> NOptional.ofEmpty(session -> NMsg.ofPlain("empty Integer")))
                .ifErrorUse(() -> NOptional.ofError(session -> NMsg.ofC("invalid Integer : %s", getRaw())))
                .flatMap(value -> {
                    int smallValue = value.intValue();
                    if (!Long.valueOf(smallValue).equals(value)) {
                        return NOptional.ofError(session -> NMsg.ofC("invalid Integer : %s", getRaw()));
                    }
                    return NOptional.of(smallValue);
                });
    }


    @Override
    public NOptional<String> asString() {
        return NOptional.of(value == null ? null : value.toString());
    }

    @Override
    public boolean isBoolean() {
        return asBoolean().isPresent();
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    public boolean isByte() {
        return asByte().isPresent();
    }

    @Override
    public boolean isString() {
        return asString().isPresent();
    }

    @Override
    public boolean isInt() {
        return asInt().isPresent();
    }

    @Override
    public boolean isLong() {
        return asInt().isPresent();
    }

    @Override
    public boolean isShort() {
        return asShort().isPresent();
    }

    @Override
    public boolean isFloat() {
        return asFloat().isPresent();
    }

    @Override
    public boolean isDouble() {
        return asDouble().isPresent();
    }

    @Override
    public boolean isInstant() {
        return asInstant().isPresent();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultNLiteral other = (DefaultNLiteral) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        return Objects.toString(getRaw());
    }

    @Override
    public String toStringLiteral() {
        switch (type()) {
            case NULL:
                return "null";
            case STRING:
                return NStringUtils.formatStringLiteral(asString().get(), NStringUtils.QuoteType.DOUBLE);
            case BOOLEAN:
                return String.valueOf(asBoolean().get());
            case BYTE:
            case LONG:
            case BIG_DECIMAL:
            case BIG_INTEGER:
            case SHORT:
            case INTEGER:
            case FLOAT:
            case DOUBLE:
                return String.valueOf(asNumber().get());
            case INSTANT:
                return NStringUtils.formatStringLiteral(asInstant().get().toString(), NStringUtils.QuoteType.DOUBLE);
        }
        return asString().get();
    }

    @Override
    public boolean isEmpty() {
        switch (type()) {
            case NULL: {
                return true;
            }
            case STRING: {
                return toString().isEmpty();
            }
        }
        return false;
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(value);
    }

    @Override
    public NOptional<BigInteger> asBigInt() {
        if (isBlank()) {
            return NOptional.ofEmpty(session -> NMsg.ofPlain("empty BigInteger"));
        }
        if (value instanceof BigInteger) {
            return NOptional.of((BigInteger) value);
        }
        if (
                value instanceof Long
                        || value instanceof Integer
                        || value instanceof Short
                        || value instanceof Byte
        ) {
            return NOptional.of(BigInteger.valueOf(((Number) value).longValue()));
        }
        if (value instanceof Date) {
            return NOptional.of(BigInteger.valueOf(((Date) value).getTime()));
        }
        if (value instanceof Boolean) {
            return NOptional.of(BigInteger.valueOf(((Boolean) value) ? 1L : 0L));
        }
        if (value instanceof CharSequence) {
            String s = value.toString();
            if (s.indexOf('.') >= 0 || s.toLowerCase().indexOf('e') >= 0) {
                try {
                    double a = Double.parseDouble(s);
                    if (a == (long) a) {
                        return NOptional.of(BigInteger.valueOf((long) a));
                    }
                } catch (NumberFormatException ex) {
                    // ignore
                }
                try {
                    return NOptional.of(new BigDecimal(s).toBigInteger());
                } catch (NumberFormatException ex) {
                    // ignore
                }
                return NOptional.ofEmpty(session -> NMsg.ofC("invalid BigInteger %s", value));
            } else {
                try {
                    if (s.startsWith("0x")) {
                        return NOptional.of(new BigInteger(s.substring(2), 16));
                    }
                    return NOptional.of(new BigInteger(s));
                } catch (NumberFormatException ex) {
                    // ignore
                }
                return NOptional.ofEmpty(session -> NMsg.ofC("invalid BigInteger %s", value));
            }
        }
        return NOptional.ofEmpty(session -> NMsg.ofC("invalid BigInteger %s", value));
    }

    @Override
    public NOptional<BigDecimal> asBigDecimal() {
        if (isBlank()) {
            return NOptional.ofEmpty(session -> NMsg.ofPlain("empty BigDecimal"));
        }
        if (value instanceof BigDecimal) {
            return NOptional.of((BigDecimal) value);
        }
        if (value instanceof BigInteger) {
            return NOptional.of(new BigDecimal(((BigInteger) value)));
        }
        if (value instanceof Double || value instanceof Float) {
            return NOptional.of(BigDecimal.valueOf(((Number) value).doubleValue()));
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long
        ) {
            return NOptional.of(BigDecimal.valueOf(((Number) value).longValue()));
        }
        if (value instanceof Date) {
            return NOptional.of(BigDecimal.valueOf(((Date) value).getTime()));
        }
        if (value instanceof CharSequence) {
            String s = value.toString();
            if (s.indexOf('.') >= 0 || s.toLowerCase().indexOf('e') >= 0) {
                try {
                    return NOptional.of(new BigDecimal(s));
                } catch (NumberFormatException ex) {
                    // ignore
                }
                try {
                } catch (NumberFormatException ex) {
                    // ignore
                }
                return NOptional.ofEmpty(session -> NMsg.ofC("invalid BigDecimal %s", value));
            } else {
                try {
                    return NOptional.of(new BigDecimal(new BigInteger(s)));
                } catch (NumberFormatException ex) {
                    // ignore
                }
                return NOptional.ofEmpty(session -> NMsg.ofC("invalid BigDecimal %s", value));
            }
        }
        return NOptional.ofEmpty(session -> NMsg.ofC("invalid BigDecimal %s", value));
    }

    @Override
    public boolean isNumber() {
        NElementType t = type();
        switch (t) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
            case FLOAT:
            case DOUBLE:
                return true;
            case STRING: {
                String s = asString().get();
                s = s.trim();
                try {
                    new BigDecimal(s);
                    return true;
                } catch (NumberFormatException ex) {
                    //ignore
                }
                break;
            }
        }
        return false;
    }

}


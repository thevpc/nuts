package net.thevpc.nuts;

import net.thevpc.nuts.elem.NutsElementType;
import net.thevpc.nuts.util.NutsStringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

public class DefaultNutsValue implements NutsValue {
    private static NutsValue NULL = new DefaultNutsValue(null);
    public static NutsValue of(Object any) {
        if (any == null) {
            return NULL;
        }
        if (any instanceof NutsValue) {
            return (NutsValue) any;
        }
        return new DefaultNutsValue(any);
    }

    public static final String[] DATE_FORMATS = {
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX"
    };

    private Object value;
    private NutsElementType type;

    public static NutsOptional<Instant> parseInstant(String text){
        for (String f : DATE_FORMATS) {
            try {
                return NutsOptional.of(new SimpleDateFormat(f).parse(text).toInstant());
            } catch (Exception ex) {
                //
            }
        }
        return NutsOptional.ofError(session -> NutsMessage.cstyle("invalid Instant %s",text));
    }
    public DefaultNutsValue(Object value) {
        this.value = value;
    }

    private static NutsElementType resolveType(Object value) {
        if (value == null) {
            return NutsElementType.NULL;
        } else {
            switch (value.getClass().getName()){
                case "java.lang.Byte":
                    return NutsElementType.BYTE;
                case "java.lang.Short":
                    return NutsElementType.SHORT;
                case "java.lang.Integer":
                    return NutsElementType.INTEGER;
                case "java.lang.Long":
                    return NutsElementType.LONG;
                case "java.math.BigInteger":
                    return NutsElementType.BIG_INTEGER;
                case "java.lang.Float":
                    return NutsElementType.FLOAT;
                case "java.lang.Double":
                    return NutsElementType.DOUBLE;
                case "java.math.BigDecimal":
                    return NutsElementType.BIG_DECIMAL;
                case "java.lang.String":
                case "java.lang.StringBuilder":
                case "java.lang.StringBuffer":
                    return NutsElementType.STRING;
                case "java.util.Date":
                case "java.time.Instant":
                    return NutsElementType.INSTANT;
                case "java.lang.Boolean":
                    return NutsElementType.BOOLEAN;
            }
            if(value instanceof Number){
                return NutsElementType.FLOAT;
            }
            if(value instanceof CharSequence) {
                return NutsElementType.STRING;
            }
            return NutsElementType.OBJECT;
        }
    }

    public NutsElementType type() {
        if(type==null){
            type=resolveType(value);
        }
        return type;
    }

    @Override
    public Object getRaw() {
        return value;
    }

    @Override
    public NutsOptional<Instant> asInstant() {
        if (value == null) {
            return NutsOptional.ofEmpty(session -> NutsMessage.plain("empty instant"));
        }
        if (value instanceof Boolean) {
            return NutsOptional.ofEmpty(session -> NutsMessage.plain("cannot convert boolean to Instant"));
        }
        if (value instanceof Date) {
            return NutsOptional.of(((Date) value).toInstant());
        }
        if (value instanceof Instant) {
            return NutsOptional.of(((Instant) value));
        }
        String s = String.valueOf(value);
        if (s.trim().isEmpty()) {
            return NutsOptional.ofEmpty(session -> NutsMessage.plain("empty instant"));
        }
        try {
            return NutsOptional.of(DateTimeFormatter.ISO_INSTANT.parse(s, Instant::from));
        } catch (Exception ex) {
            //
        }
        for (String f : DATE_FORMATS) {
            try {
                return NutsOptional.of(new SimpleDateFormat(f).parse(s).toInstant());
            } catch (Exception ex) {
                //
            }
        }
        if (isLong()) {
            try {
                try {
                    return NutsOptional.of(Instant.ofEpochMilli(((Number) value).longValue()));
                } catch (Exception any) {
                    //
                }
            } catch (Exception ex) {
                //
            }
        }
        if (isNumber()) {
            return NutsOptional.ofEmpty(session -> NutsMessage.plain("cannot convert number to Instant"));
        }
        return NutsOptional.ofEmpty(session -> NutsMessage.plain("cannot convert to Instant"));
    }

    @Override
    public NutsOptional<Number> asNumber() {
        if (value == null) {
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("empty number"));
        }
        if (value instanceof Boolean) {
            return NutsOptional.of(((Boolean) value) ? 1 : 0);
        }
        if (value instanceof Number) {
            return NutsOptional.of((Number) value);
        }
        if (value instanceof Date) {
            return NutsOptional.of(((Date) value).getTime());
        }
        if (value instanceof Instant) {
            return NutsOptional.of(((Instant) value).toEpochMilli());
        }
        String s = String.valueOf(value);
        if (s.indexOf('.') >= 0 || s.toLowerCase().indexOf('e') >= 0) {
            try {
                return NutsOptional.of(Double.parseDouble(s));
            } catch (NumberFormatException ex) {
                //just ignore!
            }
            try {
                return NutsOptional.of(new BigDecimal(s));
            } catch (NumberFormatException ex) {
                //just ignore!
            }
        } else {
            try {
                return NutsOptional.of(Integer.parseInt(s));
            } catch (NumberFormatException ex) {
                //just ignore!
            }
            try {
                return NutsOptional.of(Long.parseLong(s));
            } catch (NumberFormatException ex) {
                //just ignore!
            }
            try {
                return NutsOptional.of(new BigInteger(s));
            } catch (NumberFormatException ex) {
                //just ignore!
            }
        }
        return NutsOptional.ofEmpty(session -> NutsMessage.plain("cannot convert to Number"));
    }

    @Override
    public NutsOptional<Boolean> asBoolean() {
        if (value == null) {
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("empty boolean"));
        }
        if (value instanceof Boolean) {
            return NutsOptional.of((Boolean) value);
        }
        if (value instanceof Number) {
            if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof BigInteger) {
                return NutsOptional.of(((Number) value).longValue() != 0);
            }
            if (value instanceof Float || value instanceof Double || value instanceof BigDecimal) {
                double d = ((Number) value).doubleValue();
                return NutsOptional.of(
                        d != 0 && !Double.isNaN(d)
                );
            }
        }
        String svalue=String.valueOf(value).trim().toLowerCase();
        if (svalue.isEmpty()) {
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("empty boolean"));
        }
        if (svalue.matches("true|enable|enabled|yes|always|y|on|ok|t|o")) {
            return NutsOptional.of(true);
        }
        if (svalue.matches("false|disable|disabled|no|none|never|n|off|ko|f")) {
            return NutsOptional.of(false);
        }
        return NutsOptional.ofError(session -> NutsMessage.cstyle("invalid boolean % ",svalue));
    }

    @Override
    public NutsOptional<Long> asLong() {
        if (isBlank()) {
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("empty Long"));
        }
        if (value instanceof Number) {
            if (value instanceof BigInteger) {
                long ln = ((BigInteger) value).longValue();
                if (BigInteger.valueOf(ln).equals(value)) {
                    return NutsOptional.of(ln);
                }
            }
            return NutsOptional.of(((Number) value).longValue());
        }
        if (value instanceof Date) {
            return NutsOptional.of(((Date) value).getTime());
        }
        if (value instanceof CharSequence) {
            String s = value.toString();
            if (s.indexOf('.') >= 0 || s.toLowerCase().indexOf('e') >= 0) {
                try {
                    double a = Double.parseDouble(s);
                    if (a == (long) a) {
                        return NutsOptional.of((long) a);
                    }
                } catch (NumberFormatException ex) {
                    // ignore
                }
                return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid Long %s", value));
            } else {
                try {
                    return NutsOptional.of(Long.parseLong(s));
                } catch (NumberFormatException ex) {
                    // ignore
                }
                return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid Long %s", value));
            }
        }
        if (value instanceof Boolean) {
            return NutsOptional.of(((Boolean) value) ? 1L : 0L);
        }
        return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid Long %s", value));
    }

    @Override
    public NutsOptional<Double> asDouble() {
        if (isBlank()) {
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("empty Double"));
        }
        if (value instanceof Double || value instanceof Float) {
            return NutsOptional.of(((Number) value).doubleValue());
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            return NutsOptional.of((((Number) value).doubleValue()));
        }
        if (value instanceof BigDecimal) {
            try {
                BigDecimal bd = (BigDecimal) value;
                BigDecimal abs = bd.abs();
                if (
                        abs.compareTo(BigDecimal.valueOf(Double.MIN_VALUE)) >= 0
                                && abs.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) <= 0
                ) {
                    return NutsOptional.of(bd.doubleValue());
                }
            } catch (Exception any) {
                //
            }
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid Double %s", value));
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
                    return NutsOptional.of(bd.doubleValue());
                }
            } catch (Exception any) {
                //
            }
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid Double %s", value));
        }
        if (value instanceof Date) {
            return NutsOptional.of((double) ((Date) value).getTime());
        }
        if (value instanceof CharSequence) {
            String s = value.toString();
            try {
                return NutsOptional.of(Double.parseDouble(s));
            } catch (NumberFormatException ex) {
                // ignore
            }
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid Double %s", value));
        }
        return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid Double %s", value));
    }

    @Override
    public NutsOptional<Float> asFloat() {
        if (isBlank()) {
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("empty Float"));
        }
        if (value instanceof Float) {
            return NutsOptional.of(((Number) value).floatValue());
        }
        if (value instanceof Double) {
            Double d = (Double) value;
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                return NutsOptional.of(d.floatValue());
            }
            double abs = Math.abs(d);
            if (abs >= Float.MIN_VALUE && abs <= Float.MIN_VALUE) {
                return NutsOptional.of(d.floatValue());
            }
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid Float %s", value));
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            double d = ((Number) value).doubleValue();
            double abs = Math.abs(d);
            if (abs >= Float.MIN_VALUE && abs <= Float.MIN_VALUE) {
                return NutsOptional.of((float) d);
            }
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid Float %s", value));
        }
        if (value instanceof BigDecimal) {
            try {
                BigDecimal bd = (BigDecimal) value;
                BigDecimal abs = bd.abs();
                if (
                        abs.compareTo(BigDecimal.valueOf(Float.MIN_VALUE)) >= 0
                                && abs.compareTo(BigDecimal.valueOf(Float.MAX_VALUE)) <= 0
                ) {
                    return NutsOptional.of(bd.floatValue());
                }
            } catch (Exception any) {
                //
            }
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid Float %s", value));
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
                    return NutsOptional.of(bd.floatValue());
                }
            } catch (Exception any) {
                //
            }
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid Float %s", value));
        }
        if (value instanceof CharSequence) {
            String s = value.toString();
            try {
                return NutsOptional.of(Float.parseFloat(s));
            } catch (NumberFormatException ex) {
                // ignore
            }
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid Float %s", value));
        }
        return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid Float %s", value));
    }


    @Override
    public NutsOptional<Byte> asByte() {
        return asLong()
                .ifEmptyUse(() -> NutsOptional.ofEmpty(session -> NutsMessage.cstyle("empty Byte")))
                .ifErrorUse(() -> NutsOptional.ofError(session -> NutsMessage.cstyle("invalid Byte : %s", getRaw())))
                .flatMap(value -> {
                    byte smallValue = value.byteValue();
                    if (!Long.valueOf(smallValue).equals(value)) {
                        return NutsOptional.ofError(session -> NutsMessage.cstyle("invalid Byte : %s", getRaw()));
                    }
                    return NutsOptional.of(smallValue);
                });
    }

    @Override
    public NutsOptional<Short> asShort() {
        return asLong()
                .ifEmptyUse(() -> NutsOptional.ofEmpty(session -> NutsMessage.cstyle("empty Short")))
                .ifErrorUse(() -> NutsOptional.ofError(session -> NutsMessage.cstyle("invalid Short : %s", getRaw())))
                .flatMap(value -> {
                    short smallValue = value.shortValue();
                    if (!Long.valueOf(smallValue).equals(value)) {
                        return NutsOptional.ofError(session -> NutsMessage.cstyle("invalid Short : %s", getRaw()));
                    }
                    return NutsOptional.of(smallValue);
                });
    }

    @Override
    public NutsOptional<Integer> asInt() {
        return asLong()
                .ifEmptyUse(() -> NutsOptional.ofEmpty(session -> NutsMessage.cstyle("empty Integer")))
                .ifErrorUse(() -> NutsOptional.ofError(session -> NutsMessage.cstyle("invalid Integer : %s", getRaw())))
                .flatMap(value -> {
                    int smallValue = value.intValue();
                    if (!Long.valueOf(smallValue).equals(value)) {
                        return NutsOptional.ofError(session -> NutsMessage.cstyle("invalid Integer : %s", getRaw()));
                    }
                    return NutsOptional.of(smallValue);
                });
    }


    @Override
    public NutsOptional<String> asString() {
        return NutsOptional.of(value == null ? null : value.toString());
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
        final DefaultNutsValue other = (DefaultNutsValue) obj;
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
                return NutsStringUtils.formatStringLiteral(asString().get(), NutsStringUtils.QuoteType.DOUBLE);
            case BOOLEAN:
                return String.valueOf(asBoolean());
            case BYTE:
            case LONG:
            case BIG_DECIMAL:
            case BIG_INTEGER:
            case SHORT:
            case INTEGER:
            case FLOAT:
            case DOUBLE:
                return String.valueOf(asNumber());
            case INSTANT:
                return NutsStringUtils.formatStringLiteral(asInstant().toString(), NutsStringUtils.QuoteType.DOUBLE);
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
        return NutsBlankable.isBlank(value);
    }

    @Override
    public NutsOptional<BigInteger> asBigInt() {
        if (isBlank()) {
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("empty BigInteger"));
        }
        if (value instanceof BigInteger) {
            return NutsOptional.of((BigInteger) value);
        }
        if (
                value instanceof Long
                        || value instanceof Integer
                        || value instanceof Short
                        || value instanceof Byte
        ) {
            return NutsOptional.of(BigInteger.valueOf(((Long) value)));
        }
        if (value instanceof Date) {
            return NutsOptional.of(BigInteger.valueOf(((Date) value).getTime()));
        }
        if (value instanceof Boolean) {
            return NutsOptional.of(BigInteger.valueOf(((Boolean) value) ? 1L : 0L));
        }
        if (value instanceof CharSequence) {
            String s = value.toString();
            if (s.indexOf('.') >= 0 || s.toLowerCase().indexOf('e') >= 0) {
                try {
                    double a = Double.parseDouble(s);
                    if (a == (long) a) {
                        return NutsOptional.of(BigInteger.valueOf((long) a));
                    }
                } catch (NumberFormatException ex) {
                    // ignore
                }
                try {
                    return NutsOptional.of(new BigDecimal(s).toBigInteger());
                } catch (NumberFormatException ex) {
                    // ignore
                }
                return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid BigInteger %s", value));
            } else {
                try {
                    return NutsOptional.of(new BigInteger(s));
                } catch (NumberFormatException ex) {
                    // ignore
                }
                return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid BigInteger %s", value));
            }
        }
        return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid BigInteger %s", value));
    }

    @Override
    public NutsOptional<BigDecimal> asBigDecimal() {
        if (isBlank()) {
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("empty BigDecimal"));
        }
        if (value instanceof BigDecimal) {
            return NutsOptional.of((BigDecimal) value);
        }
        if (value instanceof BigInteger) {
            return NutsOptional.of(new BigDecimal(((BigInteger) value)));
        }
        if (value instanceof Double || value instanceof Float) {
            return NutsOptional.of(BigDecimal.valueOf(((Number) value).doubleValue()));
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long
        ) {
            return NutsOptional.of(BigDecimal.valueOf(((Number) value).longValue()));
        }
        if (value instanceof Date) {
            return NutsOptional.of(BigDecimal.valueOf(((Date) value).getTime()));
        }
        if (value instanceof CharSequence) {
            String s = value.toString();
            if (s.indexOf('.') >= 0 || s.toLowerCase().indexOf('e') >= 0) {
                try {
                    return NutsOptional.of(new BigDecimal(s));
                } catch (NumberFormatException ex) {
                    // ignore
                }
                try {
                } catch (NumberFormatException ex) {
                    // ignore
                }
                return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid BigDecimal %s", value));
            } else {
                try {
                    return NutsOptional.of(new BigDecimal(new BigInteger(s)));
                } catch (NumberFormatException ex) {
                    // ignore
                }
                return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid BigDecimal %s", value));
            }
        }
        return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("invalid BigDecimal %s", value));
    }

    @Override
    public boolean isNumber() {
        NutsElementType t = type();
        switch (t) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
            case FLOAT:
            case DOUBLE:
                return true;
        }
        return false;
    }

}


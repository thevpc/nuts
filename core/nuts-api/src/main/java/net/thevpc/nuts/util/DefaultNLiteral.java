package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NInputStreamProvider;
import net.thevpc.nuts.io.NReaderProvider;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
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

    public static final String[] DATE_TIME_FORMATS = {
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy/MM/dd HH:mm:ss.SSS",
            "yyyy/MM/dd HH:mm:ss"
    };

    public static final String[] TIME_FORMATS = {
            "HH:mm:ss.SSSX",
            "HH:mm:ss.SSS",
            "HH:mm:ss",
            "HH:mm"
    };

    public static final String[] DATE_FORMATS = {
            "yyyy-MM-dd",
            "yyyy/MM/dd",
    };

    private Object value;
    private NElementType type;

    public static NOptional<Instant> parseInstant(String text) {
        for (String f : DATE_TIME_FORMATS) {
            try {
                return NOptional.of(new SimpleDateFormat(f).parse(text).toInstant());
            } catch (Exception ex) {
                //
            }
        }
        return NOptional.ofError(() -> NMsg.ofC("invalid Instant %s", text));
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
                    return NElementType.DOUBLE_QUOTED_STRING;
                case "java.util.Date":
                case "java.time.Instant":
                    return NElementType.INSTANT;
                case "java.time.LocalDateTime":
                    return NElementType.LOCAL_DATETIME;
                case "java.time.LocalDate":
                    return NElementType.LOCAL_DATE;
                case "java.time.LocalTime":
                    return NElementType.LOCAL_TIME;
                case "java.lang.Boolean":
                    return NElementType.BOOLEAN;
                case "net.thevpc.nuts.elem.NDoubleComplex":
                    return NElementType.DOUBLE_COMPLEX;
                case "net.thevpc.nuts.elem.NFloatComplex":
                    return NElementType.FLOAT_COMPLEX;
                case "net.thevpc.nuts.elem.NBigComplex":
                    return NElementType.BIG_COMPLEX;
                case "net.thevpc.nuts.elem.NName":
                    return NElementType.NAME;
                case "net.thevpc.nuts.elem.NAliasName":
                    return NElementType.ALIAS;
            }
            if (value instanceof Number) {
                return NElementType.DOUBLE;
            }
            if (value instanceof NInputStreamProvider) {
                return NElementType.BINARY_STREAM;
            }
            if (value instanceof NReaderProvider) {
                return NElementType.CHAR_STREAM;
            }
            if (value instanceof CharSequence) {
                return NElementType.DOUBLE_QUOTED_STRING;
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
    public NOptional<NFloatComplex> asFloatComplex() {
        if (value == null) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty FloatComplex"));
        }
        if (value instanceof NDoubleComplex) {
            return NOptional.of(new NFloatComplex(
                    (float) ((NDoubleComplex) value).real(),
                    (float) ((NDoubleComplex) value).imag()
            ));
        }
        if (value instanceof NFloatComplex) {
            return NOptional.of((NFloatComplex) value);
        }
        if (value instanceof NBigComplex) {
            return NOptional.of(new NFloatComplex(
                    ((NBigComplex) value).real().floatValue(),
                    ((NBigComplex) value).imag().floatValue()
            ));
        }
        return asFloat().map(x -> new NFloatComplex((float) value, 0));
    }

    @Override
    public NOptional<NDoubleComplex> asDoubleComplex() {
        if (value == null) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty DoubleComplex"));
        }
        if (value instanceof NDoubleComplex) {
            return NOptional.of((NDoubleComplex) value);
        }
        if (value instanceof NFloatComplex) {
            return NOptional.of(new NDoubleComplex(
                    ((NFloatComplex) value).real(),
                    ((NFloatComplex) value).imag()
            ));
        }
        if (value instanceof NBigComplex) {
            return NOptional.of(new NDoubleComplex(
                    ((NBigComplex) value).real().doubleValue(),
                    ((NBigComplex) value).imag().doubleValue()
            ));
        }
        return asDouble().map(x -> new NDoubleComplex((double) value, 0));
    }

    @Override
    public NOptional<NBigComplex> asBigComplex() {
        if (value == null) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty DoubleComplex"));
        }
        if (value instanceof NDoubleComplex) {
            return NOptional.of(new NBigComplex(
                    BigDecimal.valueOf(((NDoubleComplex) value).real()),
                    BigDecimal.valueOf(((NDoubleComplex) value).real())
            ));
        }
        if (value instanceof NFloatComplex) {
            return NOptional.of(new NBigComplex(
                    BigDecimal.valueOf(((NFloatComplex) value).real()),
                    BigDecimal.valueOf(((NFloatComplex) value).real())
            ));
        }
        if (value instanceof NBigComplex) {
            return NOptional.of(((NBigComplex) value));
        }
        return asBigDecimal().map(x -> new NBigComplex(
                BigDecimal.valueOf(x.doubleValue()),
                BigDecimal.ZERO
        ));
    }

    @Override
    public NOptional<LocalTime> asLocalTime() {
        if (value == null) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty instant"));
        }
        if (value instanceof LocalDateTime) {
            return NOptional.of(((LocalDateTime) value).toLocalTime());
        }
        if (value instanceof LocalDate) {
            return NOptional.of(LocalTime.MIN);
        }
        if (value instanceof LocalTime) {
            return NOptional.of((LocalTime) value);
        }
        return asInstant().map(x -> x.atZone(ZoneId.systemDefault()).toLocalTime());
    }


    @Override
    public NOptional<LocalDateTime> asLocalDateTime() {
        if (value == null) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty instant"));
        }
        if (value instanceof LocalDateTime) {
            return NOptional.of((LocalDateTime) value);
        }
        if (value instanceof LocalDate) {
            return NOptional.of(((LocalDate) value).atStartOfDay());
        }
        if (value instanceof LocalTime) {
            return NOptional.of(LocalDateTime.of(LocalDate.now(), (LocalTime) value));
        }
        return asInstant().map(x -> x.atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    @Override
    public NOptional<LocalDate> asLocalDate() {
        if (value == null) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty instant"));
        }
        if (value instanceof LocalDateTime) {
            return NOptional.of(((LocalDateTime) value).toLocalDate());
        }
        if (value instanceof LocalDate) {
            return NOptional.of((LocalDate) value);
        }
        if (value instanceof LocalTime) {
            return NOptional.of(LocalDate.now());
        }
        return asInstant().map(x -> x.atZone(ZoneId.systemDefault()).toLocalDate());
    }

    @Override
    public NOptional<Instant> asInstant() {
        if (value == null) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty instant"));
        }
        if (value instanceof Boolean) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("cannot convert boolean to Instant"));
        }
        if (value instanceof Date) {
            return NOptional.of(((Date) value).toInstant());
        }
        if (value instanceof Instant) {
            return NOptional.of(((Instant) value));
        }
        if (value instanceof LocalDateTime) {
            return NOptional.of(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant());
        }
        if (value instanceof LocalDate) {
            return NOptional.of(((LocalDate) value).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        if (value instanceof LocalTime) {
            return NOptional.of(LocalDateTime.of(LocalDate.now(), (LocalTime) value)
                    .atZone(ZoneId.systemDefault())
                    .toInstant());
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty()) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty instant"));
        }
        try {
            return NOptional.of(DateTimeFormatter.ISO_INSTANT.parse(s, Instant::from));
        } catch (Exception ex) {
            //
        }
        for (String f : DATE_TIME_FORMATS) {
            try {
                return NOptional.of(new SimpleDateFormat(f).parse(s).toInstant());
            } catch (Exception ex) {
                //
            }
        }
        try {
            String sd = s.replace("/", "-");
            if (sd.matches("\\d{2}-\\d{2}-\\d{4}")) {
                String y = sd.substring(6, 10);
                String m = sd.substring(3, 5);
                String d = sd.substring(0, 2);
                return NOptional.of(Instant.parse(y + "-" + m + "-" + d + "T00:00:00Z"));
            } else if (sd.matches("\\d{4}-\\d{2}-\\d{2}")) {
                sd = sd + "T00:00:00Z";
            } else if (sd.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                sd = sd.replace(" ", "T") + "Z";
            } else if (sd.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}")) {
                sd = sd.replace(" ", "T") + ":00:00Z";
            } else if (sd.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
                sd = sd.replace(" ", "T") + ":00Z";
            } else if (sd.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}[hH]\\d{2}")) {
                sd = sd.replace(" ", "T").replace("h", ":").replace("H", ":") + ":00Z";
            } else if (sd.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}[hH]")) {
                sd = sd.replace(" ", "T").substring(0, sd.length() - 1) + ":00:00Z";
            }
            return NOptional.of(Instant.parse(sd));
        } catch (Exception ex) {
            //
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
            return NOptional.ofEmpty(() -> NMsg.ofPlain("cannot convert number to Instant"));
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("cannot convert to Instant %s", value));
    }

    @Override
    public NOptional<Object> asObject() {
        return NOptional.of(value);
    }


    @Override
    public NOptional<Number> asNumber() {
        if (value == null) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty number"));
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
        if(isCouldBeNumber(s)) {
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
        }
        return NOptional.ofError(() -> NMsg.ofPlain("cannot convert to Number"));
    }

    @Override
    public NOptional<Boolean> asBoolean() {
        if (value == null) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty boolean"));
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
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty boolean"));
        }
        if (svalue.matches("true|enable|enabled|yes|always|y|on|ok|t|o")) {
            return NOptional.of(true);
        }
        if (svalue.matches("false|disable|disabled|no|none|never|n|off|ko|f")) {
            return NOptional.of(false);
        }
        return NOptional.ofError(() -> NMsg.ofC("invalid boolean %s", svalue));
    }

    public NOptional<Long> asLong() {
        if (isBlank()) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty Long"));
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
            String s = value.toString().trim();
            if(isCouldBeNumber(s)) {
                if (s.indexOf('.') >= 0 || s.toLowerCase().indexOf('e') >= 0) {
                    try {
                        double a = Double.parseDouble(s);
                        if (a == (long) a) {
                            return NOptional.of((long) a);
                        }
                    } catch (NumberFormatException ex) {
                        // ignore
                    }
                    return NOptional.ofError(() -> NMsg.ofC("invalid Long %s", value));
                } else {
                    try {
                        if (s.startsWith("0x")) {
                            return NOptional.of(Long.parseLong(s.substring(2), 16));
                        }
                        return NOptional.of(Long.parseLong(s));
                    } catch (NumberFormatException ex) {
                        // ignore
                    }
                    return NOptional.ofError(() -> NMsg.ofC("invalid Long %s", value));
                }
            }
        }
        if (value instanceof Boolean) {
            return NOptional.of(((Boolean) value) ? 1L : 0L);
        }
        return NOptional.ofError(() -> NMsg.ofC("invalid Long %s", value));
    }

    private boolean isCouldBeNumber(String s) {
        s = s.trim();
        if (!s.isEmpty()) {
            char c = s.charAt(0);
            if (c == '-' || c == '+') {
                s = s.substring(1);
                if (s.isEmpty()) {
                    return false;
                }
                c = s.charAt(0);
            }
            return (c >= '0' && c <= '9') || c == '.';
        }
        return false;
    }

    @Override
    public NOptional<Double> asDouble() {
        if (isBlank()) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty Double"));
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
            return NOptional.ofError(() -> NMsg.ofC("invalid Double %s", value));
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
            return NOptional.ofError(() -> NMsg.ofC("invalid Double %s", value));
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
        return NOptional.ofError(() -> NMsg.ofC("invalid Double %s", value));
    }

    @Override
    public NOptional<Float> asFloat() {
        if (isBlank()) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty Float"));
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
            return NOptional.ofEmpty(() -> NMsg.ofC("invalid Float %s", value));
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            double d = ((Number) value).doubleValue();
            double abs = Math.abs(d);
            if (abs >= Float.MIN_VALUE && abs <= Float.MIN_VALUE) {
                return NOptional.of((float) d);
            }
            return NOptional.ofEmpty(() -> NMsg.ofC("invalid Float %s", value));
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
            return NOptional.ofEmpty(() -> NMsg.ofC("invalid Float %s", value));
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
            return NOptional.ofEmpty(() -> NMsg.ofC("invalid Float %s", value));
        }
        if (value instanceof CharSequence) {
            String s = value.toString();
            try {
                return NOptional.of(Float.parseFloat(s));
            } catch (NumberFormatException ex) {
                // ignore
            }
            return NOptional.ofEmpty(() -> NMsg.ofC("invalid Float %s", value));
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("invalid Float %s", value));
    }


    @Override
    public NOptional<Byte> asByte() {
        return asLong()
                .ifEmptyUse(() -> NOptional.ofEmpty(() -> NMsg.ofPlain("empty Byte")))
                .ifErrorUse(() -> NOptional.ofError(() -> NMsg.ofC("invalid Byte : %s", asObject().orNull())))
                .flatMap(value -> {
                    byte smallValue = value.byteValue();
                    if (!Long.valueOf(smallValue).equals(value)) {
                        return NOptional.ofError(() -> NMsg.ofC("invalid Byte : %s", asObject().orNull()));
                    }
                    return NOptional.of(smallValue);
                });
    }

    @Override
    public NOptional<Short> asShort() {
        return asLong()
                .ifEmptyUse(() -> NOptional.ofEmpty(() -> NMsg.ofPlain("empty Short")))
                .ifErrorUse(() -> NOptional.ofError(() -> NMsg.ofC("invalid Short : %s", asObject().orNull())))
                .flatMap(value -> {
                    short smallValue = value.shortValue();
                    if (!Long.valueOf(smallValue).equals(value)) {
                        return NOptional.ofError(() -> NMsg.ofC("invalid Short : %s", asObject().orNull()));
                    }
                    return NOptional.of(smallValue);
                });
    }

    @Override
    public NOptional<Integer> asInt() {
        return asLong()
                .ifEmptyUse(() -> NOptional.ofEmpty(() -> NMsg.ofPlain("empty Integer")))
                .ifErrorUse(() -> NOptional.ofError(() -> NMsg.ofC("invalid Integer : %s", asObject().orNull())))
                .flatMap(value -> {
                    int smallValue = value.intValue();
                    if (!Long.valueOf(smallValue).equals(value)) {
                        return NOptional.ofError(() -> NMsg.ofC("invalid Integer : %s", asObject().orNull()));
                    }
                    return NOptional.of(smallValue);
                });
    }


    @Override
    public NOptional<String> asString() {
        if (value == null) {
            return NOptional.of(null);
        }
        return NOptional.of(value.toString());
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
        return value instanceof Byte;
    }

    @Override
    public boolean isString() {
        return asString().isPresent();
    }

    @Override
    public boolean isInt() {
        return value instanceof Integer;
    }

    @Override
    public boolean isLong() {
        return value instanceof Long;
    }

    @Override
    public boolean isShort() {
        return value instanceof Short;
    }

    @Override
    public boolean isFloat() {
        return value instanceof Float;
    }

    @Override
    public boolean isDouble() {
        return value instanceof Double;
    }

    @Override
    public boolean isInstant() {
        return value instanceof Instant;
    }

    @Override
    public boolean isDecimalNumber() {
        return value instanceof BigDecimal || value instanceof Float || value instanceof Double;
    }

    @Override
    public boolean isBigNumber() {
        return value instanceof BigDecimal || value instanceof BigInteger;
    }

    @Override
    public boolean isBigDecimal() {
        return value instanceof BigDecimal;
    }

    @Override
    public boolean isBigInt() {
        return value instanceof BigInteger;
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
        return Objects.toString(asObject().orNull());
    }

    @Override
    public String toStringLiteral() {
        switch (type()) {
            case NULL:
                return "null";
            case CHAR:
                return NStringUtils.formatStringLiteral(asString().get(), NElementType.SINGLE_QUOTED_STRING);
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING:
                return NStringUtils.formatStringLiteral(asString().get(), type());
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
            case LOCAL_TIME:
            case LOCAL_DATE:
            case LOCAL_DATETIME:
                return NStringUtils.formatStringLiteral(asInstant().get().toString(), NElementType.DOUBLE_QUOTED_STRING);
        }
        return asString().get();
    }

    @Override
    public boolean isEmpty() {
        switch (type()) {
            case NULL: {
                return true;
            }
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING: {
                return toString().isEmpty();
            }
        }
        return false;
    }

    @Override
    public NOptional<String> asStringAt(int index) {
        return asLiteralAt(index).asString();
    }

    @Override
    public NOptional<Long> asLongAt(int index) {
        return asLiteralAt(index).asLong();
    }

    @Override
    public NOptional<Integer> asIntAt(int index) {
        return asLiteralAt(index).asInt();
    }

    @Override
    public NOptional<Double> asDoubleAt(int index) {
        return asLiteralAt(index).asDouble();
    }

    @Override
    public boolean isNullAt(int index) {
        return asLiteralAt(index).isNull();
    }

    @Override
    public NLiteral asLiteralAt(int index) {
        return of(asObjectAt(index).orNull());
    }

    @Override
    public NOptional<Object> asObjectAt(int index) {
        if (value != null) {
            if (value instanceof Object[]) {
                Object[] e = (Object[]) value;
                if (index >= 0 && index < e.length) {
                    return NOptional.ofNamed(e[index], "object at " + index);
                }
            }
            if (value.getClass().isArray()) {
                int len = Array.getLength(value);
                if (index >= 0 && index < len) {
                    return NOptional.ofNamed(Array.get(value, index), "object at " + index);
                }
            }
            if (value instanceof List) {
                List li = (List) value;
                int len = li.size();
                if (index >= 0 && index < len) {
                    return NOptional.ofNamed(li.get(index), "object at " + index);
                }
            }
            if (value instanceof NElement) {
                return ((NElement) value).asElementAt(index).map(x -> x.asLiteral().asObject().orNull());
            }
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("invalid object at %s", index));
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(value);
    }

    @Override
    public NOptional<BigInteger> asBigInt() {
        if (isBlank()) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty BigInteger"));
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
                return NOptional.ofEmpty(() -> NMsg.ofC("invalid BigInteger %s", value));
            } else {
                try {
                    if (s.startsWith("0x")) {
                        return NOptional.of(new BigInteger(s.substring(2), 16));
                    }
                    return NOptional.of(new BigInteger(s));
                } catch (NumberFormatException ex) {
                    // ignore
                }
                return NOptional.ofEmpty(() -> NMsg.ofC("invalid BigInteger %s", value));
            }
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("invalid BigInteger %s", value));
    }

    @Override
    public NOptional<BigDecimal> asBigDecimal() {
        if (isBlank()) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty BigDecimal"));
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
                return NOptional.ofEmpty(() -> NMsg.ofC("invalid BigDecimal %s", value));
            } else {
                try {
                    return NOptional.of(new BigDecimal(new BigInteger(s)));
                } catch (NumberFormatException ex) {
                    // ignore
                }
                return NOptional.ofEmpty(() -> NMsg.ofC("invalid BigDecimal %s", value));
            }
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("invalid BigDecimal %s", value));
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
            case BIG_COMPLEX:
            case BIG_INTEGER:
            case BIG_DECIMAL:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX:
                return true;
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING: {
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


    @Override
    public NOptional<Character> asChar() {
        if (isBlank()) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty Character"));
        }
        if (value instanceof Character) {
            return NOptional.of((Character) value);
        }
        if (value instanceof Number) {
            return NOptional.of((char) ((Number) value).shortValue());
        }
        if (value instanceof CharSequence) {
            CharSequence e = (CharSequence) value;
            if (e.length() == 1) {
                return NOptional.of(e.charAt(0));
            }
            if (e.length() == 0) {
                return NOptional.ofEmpty(() -> NMsg.ofPlain("empty Character"));
            }
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("invalid character %s", value));
    }

    @Override
    public boolean isSupportedType(Class<?> type) {
        if (type == null) {
            return false;
        }
        switch (type.getName()) {
            case "java.lang.String":
            case "java.lang.Boolean":
            case "boolean":
            case "java.lang.Byte":
            case "byte":
            case "java.lang.Short":
            case "short":
            case "java.lang.Character":
            case "char":
            case "java.lang.Integer":
            case "int":
            case "java.lang.Long":
            case "long":
            case "java.lang.Float":
            case "float":
            case "java.lang.Double":
            case "double":
            case "java.time.Instant":
            case "java.lang.Number":
                return true;
        }
        return false;
    }

    @Override
    public <ET> NOptional<ET> asType(Type expectedType) {
        if (expectedType instanceof Class<?>) {
            return (NOptional<ET>) asType((Class<?>) expectedType);
        }
        if (expectedType instanceof ParameterizedType) {
            return (NOptional<ET>) asType(((ParameterizedType) expectedType).getRawType());
        }
        return NOptional.ofError(() -> NMsg.ofC("unsupported type %s", expectedType));
    }

    @Override
    public <ET> NOptional<ET> asType(Class<ET> type) {
        NAssert.requireNonNull(type, "type");
        switch (type.getName()) {
            case "java.lang.String":
                return (NOptional<ET>) NOptional.of(value);
            case "java.lang.Boolean": {
                if (NBlankable.isBlank(value)) {
                    return null;
                }
                return (NOptional<ET>) asBoolean();
            }
            case "boolean": {
                if (NBlankable.isBlank(value)) {
                    return (NOptional<ET>) NOptional.of(getDefaultValue(type));
                }
                return (NOptional<ET>) asBoolean();
            }
            case "java.lang.Byte": {
                if (NBlankable.isBlank(value)) {
                    return null;
                }
                return (NOptional<ET>) asByte();
            }
            case "byte": {
                if (NBlankable.isBlank(value)) {
                    return (NOptional<ET>) NOptional.of(getDefaultValue(type));
                }
                return (NOptional<ET>) asByte();
            }
            case "java.lang.Short": {
                if (NBlankable.isBlank(value)) {
                    return null;
                }
                return (NOptional<ET>) asShort();
            }
            case "short": {
                if (NBlankable.isBlank(value)) {
                    return (NOptional<ET>) NOptional.of(getDefaultValue(type));
                }
                return (NOptional<ET>) asShort();
            }
            case "java.lang.Character": {
                if (NBlankable.isBlank(value)) {
                    return null;
                }
                return (NOptional<ET>) asChar();
            }
            case "char": {
                if (NBlankable.isBlank(value)) {
                    return (NOptional<ET>) NOptional.of(getDefaultValue(type));
                }
                return (NOptional<ET>) asChar();
            }
            case "java.lang.Integer": {
                if (NBlankable.isBlank(value)) {
                    return null;
                }
                return (NOptional<ET>) asInt();
            }
            case "int": {
                if (NBlankable.isBlank(value)) {
                    return (NOptional<ET>) NOptional.of(getDefaultValue(type));
                }
                return (NOptional<ET>) asInt();
            }
            case "java.lang.Long": {
                if (NBlankable.isBlank(value)) {
                    return null;
                }
                return (NOptional<ET>) asLong();
            }
            case "long": {
                if (NBlankable.isBlank(value)) {
                    return (NOptional<ET>) NOptional.of(getDefaultValue(type));
                }
                return (NOptional<ET>) asLong();
            }
            case "java.lang.Float": {
                if (NBlankable.isBlank(value)) {
                    return null;
                }
                return (NOptional<ET>) asFloat();
            }
            case "float": {
                if (NBlankable.isBlank(value)) {
                    return (NOptional<ET>) NOptional.of(getDefaultValue(type));
                }
                return (NOptional<ET>) asFloat();
            }
            case "java.lang.Double": {
                if (NBlankable.isBlank(value)) {
                    return null;
                }
                return (NOptional<ET>) asDouble();
            }
            case "double": {
                if (NBlankable.isBlank(value)) {
                    return (NOptional<ET>) NOptional.of(getDefaultValue(type));
                }
                return (NOptional<ET>) asDouble();
            }
            case "java.time.Instant": {
                if (NBlankable.isBlank(value)) {
                    return NOptional.ofEmpty();
                }
                return (NOptional<ET>) asInstant();
            }
            case "java.lang.Number": {
                if (NBlankable.isBlank(value)) {
                    return NOptional.ofEmpty();
                }
                return (NOptional<ET>) asNumber();
            }
        }
        if (type.isEnum()) {
            if (NBlankable.isBlank(value)) {
                return NOptional.ofEmpty();
            }
            if (isInt()) {
                ET[] enumConstants = type.getEnumConstants();
                Integer ordinal = asInt().get();
                if (ordinal >= 0 && ordinal <= enumConstants.length) {
                    return (NOptional<ET>) NOptional.of(enumConstants[ordinal]);
                }
                NOptional.ofError(() -> NMsg.ofC("invalid ordinal %s for %s", ordinal, type));
            }
            if (NEnum.class.isAssignableFrom(type)) {
                try {
                    return (NOptional<ET>) NOptional.of(NEnum.parse((Class<? extends NEnum>) type, String.valueOf(value).trim()).get());
                } catch (RuntimeException ex) {
                    NOptional.ofError(() -> NMsg.ofC("unable to parse %s as %s", String.valueOf(value).trim(), type));
                }
            }
            try {
                return (NOptional<ET>) NOptional.of(Enum.valueOf((Class) type, String.valueOf(value).trim()));
            } catch (RuntimeException ex) {
                NOptional.ofError(() -> NMsg.ofC("unable to parse %s as %s", String.valueOf(value).trim(), type));
            }
        }
        return NOptional.ofError(() -> NMsg.ofC("unsupported type %s", type));
    }

    private static Object getDefaultValue(Class<?> anyType) {
        NAssert.requireNonNull(anyType, "type");
        switch (anyType.getName()) {
            case "boolean":
                return false;
            case "byte":
                return (byte) 0;
            case "short":
                return (short) 0;
            case "int":
                return (int) 0;
            case "long":
                return 0L;
            case "char":
                return '\0';
            case "float":
                return 0.0f;
            case "double":
                return 0.0;
            case "void":
                return null;
        }
        return null;
    }

    @Override
    public boolean isStream() {
        return value instanceof NInputStreamProvider || value instanceof NReaderProvider;
    }

    @Override
    public boolean isComplexNumber() {
        return
                value instanceof NDoubleComplex
                        || value instanceof NFloatComplex
                        || value instanceof NBigComplex
                ;
    }

    @Override
    public boolean isTemporal() {
        return
                value instanceof Instant
                        || value instanceof LocalTime
                        || value instanceof LocalDate
                        || value instanceof LocalDateTime
                ;
    }

    @Override
    public boolean isLocalTemporal() {
        return
                value instanceof LocalTime
                        || value instanceof LocalDate
                        || value instanceof LocalDateTime
                ;
    }

    @Override
    public boolean isOrdinalNumber() {
        return type().isOrdinalNumber();
    }

    @Override
    public boolean isFloatingNumber() {
        return type().isFloatingNumber();
    }
}


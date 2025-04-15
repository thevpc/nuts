package net.thevpc.nuts.runtime.standalone.tson.impl.parser;

import net.thevpc.nuts.runtime.standalone.tson.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Pattern;

public class TsonNumberHelper {
    public static final Pattern ANY_NUMBER_PATTERN_0 = Pattern.compile("^[-]?[0-9_E.+-Llf]+$");
    public static final Pattern ANY_NUMBER_PATTERN_1XU = Pattern.compile("^(?<sign>[-]?)(?<a>0u[1248][x])(?<n>[^L%]+)?(?<l>[lL]?)(?<c>([%][a-zA-Z]*)?)$");
    public static final Pattern ANY_NUMBER_PATTERN_2XU = Pattern.compile("^(?<sign>[-]?)(?<a>0u[1248][x])(?<n>[^a-fA-F0-9]+)?(?<l>[lL]?)(?<c>([_][a-zA-Z]+)?)$");
    public static final Pattern ANY_NUMBER_PATTERN_1U = Pattern.compile("^(?<sign>[-]?)(?<a>0u[1248][xbo_])(?<n>[^L%]+)?(?<l>[lL]?)(?<c>([%][a-zA-Z]*)?)$");
    public static final Pattern ANY_NUMBER_PATTERN_2U = Pattern.compile("^(?<sign>[-]?)(?<a>0u[1248][xbo_])(?<n>[^L%]+)?(?<l>[lL]?)(?<c>([_][a-zA-Z]+)?)$");
    private Number real;
    private Number imag;
    private boolean complex;
    private NumberType type;
    private String unit;
    private TsonNumberLayout layout;

    public static TsonNumberHelper of(Number real) {
        return real == null ? null : new TsonNumberHelper(real, NumberType.of(real.getClass()), TsonNumberLayout.DECIMAL, null);
    }

    public TsonNumberHelper(TsonNumber te) {
        this.unit = te.numberSuffix();
        this.layout = te.numberLayout();
        switch (te.type()) {
            case BIG_COMPLEX: {
                this.complex = true;
                TsonBigComplex bigComplex = te.toBigComplex();
                this.real = bigComplex.real();
                this.imag = bigComplex.imag();
                this.type = NumberType.BIG_INTEGER;
                break;
            }
            case DOUBLE_COMPLEX: {
                this.complex = true;
                TsonDoubleComplex bigComplex = te.toDoubleComplex();
                this.real = bigComplex.real();
                this.imag = bigComplex.imag();
                this.type = NumberType.DOUBLE;
                break;
            }
            case FLOAT_COMPLEX: {
                this.complex = true;
                TsonFloatComplex bigComplex = te.toFloatComplex();
                this.real = bigComplex.real();
                this.imag = bigComplex.imag();
                this.type = NumberType.FLOAT;
                break;
            }
            case BYTE: {
                this.real = te.byteValue();
                this.type = NumberType.BYTE;
                break;
            }
            case SHORT: {
                this.real = te.shortValue();
                this.type = NumberType.SHORT;
                break;
            }
            case INTEGER: {
                this.real = te.intValue();
                this.type = NumberType.INT;
                break;
            }
            case LONG: {
                this.real = te.longValue();
                this.type = NumberType.LONG;
                break;
            }
            case FLOAT: {
                this.real = te.floatValue();
                this.type = NumberType.FLOAT;
                break;
            }
            case DOUBLE: {
                this.real = te.doubleValue();
                this.type = NumberType.DOUBLE;
                break;
            }
            case BIG_INTEGER: {
                this.real = te.bigIntegerValue();
                this.type = NumberType.BIG_INTEGER;
                break;
            }
            case BIG_DECIMAL: {
                this.real = te.bigDecimalValue();
                this.type = NumberType.BIG_DECIMAL;
                break;
            }
            default: {
                throw new IllegalArgumentException("not supported number type " + te.type());
            }
        }
    }

    private TsonNumberHelper(Number real, Number imag, NumberType type, TsonNumberLayout layout, String unit) {
        this.real = real;
        this.imag = imag;
        this.complex = imag != null;
        this.type = type;
        this.unit = unit;
        this.layout = layout;
    }

    private TsonNumberHelper(Number real, NumberType type, TsonNumberLayout layout, String unit) {
        this.real = real;
        this.complex = false;
        this.type = type;
        this.layout = layout;
        this.unit = unit;
    }

    public TsonNumberHelper to(NumberType type) {
        if (type == this.type) {
            return this;
        }
        if (complex) {
            switch (type) {
                case BYTE:
                case SHORT:
                case INT:
                    return to(NumberType.FLOAT);
                case LONG:
                    return to(NumberType.DOUBLE);
                case FLOAT:
                    return new TsonNumberHelper(real.floatValue(), imag.floatValue(), NumberType.FLOAT, layout, unit);
                case DOUBLE:
                    return new TsonNumberHelper(real.doubleValue(), imag.doubleValue(), NumberType.DOUBLE, layout, unit);
                case BIG_INTEGER: {
                    return to(NumberType.BIG_DECIMAL);
                }
                case BIG_DECIMAL: {
                    switch (this.type) {
                        case BIG_INTEGER: {
                            return new TsonNumberHelper(new BigDecimal(((BigInteger) real)), NumberType.BIG_DECIMAL, layout, unit);
                        }
                        case BYTE:
                        case SHORT:
                        case INT:
                        case LONG: {
                            return new TsonNumberHelper(new BigDecimal(BigInteger.valueOf(real.longValue())), NumberType.BIG_DECIMAL, layout, unit);
                        }
                        case FLOAT:
                        case DOUBLE: {
                            return new TsonNumberHelper(BigDecimal.valueOf(real.doubleValue()), NumberType.BIG_INTEGER, layout, unit);
                        }
                    }
                    break;
                }
            }
        } else {
            switch (type) {
                case BYTE:
                    return new TsonNumberHelper(real.byteValue(), NumberType.BYTE, layout, unit);
                case SHORT:
                    return new TsonNumberHelper(real.shortValue(), NumberType.SHORT, layout, unit);
                case INT:
                    return new TsonNumberHelper(real.intValue(), NumberType.INT, layout, unit);
                case LONG:
                    return new TsonNumberHelper(real.longValue(), NumberType.LONG, layout, unit);
                case FLOAT:
                    return new TsonNumberHelper(real.floatValue(), NumberType.FLOAT, layout, unit);
                case DOUBLE:
                    return new TsonNumberHelper(real.doubleValue(), NumberType.DOUBLE, layout, unit);
                case BIG_INTEGER: {
                    switch (this.type) {
                        case BIG_DECIMAL: {
                            return new TsonNumberHelper(((BigDecimal) real).toBigInteger(), NumberType.BIG_INTEGER, layout, unit);
                        }
                        case BYTE:
                        case SHORT:
                        case INT:
                        case LONG: {
                            return new TsonNumberHelper(BigInteger.valueOf(real.longValue()), NumberType.BIG_INTEGER, layout, unit);
                        }
                        case FLOAT:
                        case DOUBLE: {
                            return new TsonNumberHelper(BigDecimal.valueOf(real.doubleValue()).toBigInteger(), NumberType.BIG_INTEGER, layout, unit);
                        }
                    }
                    break;
                }
                case BIG_DECIMAL: {
                    switch (this.type) {
                        case BIG_INTEGER: {
                            return new TsonNumberHelper(new BigDecimal(((BigInteger) real)), NumberType.BIG_DECIMAL, layout, unit);
                        }
                        case BYTE:
                        case SHORT:
                        case INT:
                        case LONG: {
                            return new TsonNumberHelper(new BigDecimal(BigInteger.valueOf(real.longValue())), NumberType.BIG_DECIMAL, layout, unit);
                        }
                        case FLOAT:
                        case DOUBLE: {
                            return new TsonNumberHelper(BigDecimal.valueOf(real.doubleValue()), NumberType.BIG_INTEGER, layout, unit);
                        }
                    }
                    break;
                }
            }
        }

        throw new IllegalArgumentException("unsupported conversion : " + this.type + "->" + type);
    }


    private static TsonNumberLayout detectRadix(String nbr) {
        int i = 0;
        if (nbr.charAt(0) == '-' || nbr.charAt(0) == '+') {
            i++;
        }
        int len = nbr.length();
        while (true) {
            if (!(i < len)) break;
            char c = nbr.charAt(i);
            if (
                    Character.isWhitespace(c)
                            || c == '+'
                            || c == '-'
            ) {
                i++;
            } else {
                if (c == '0') {
                    if (i + 1 < len) {
                        c = nbr.charAt(i + 1);
                        if (c == 'b') {
                            return TsonNumberLayout.BINARY;
                        }
                        if (c == 'x') {
                            return TsonNumberLayout.HEXADECIMAL;
                        }
                        return TsonNumberLayout.OCTAL;
                    } else {
                        return TsonNumberLayout.DECIMAL;
                    }
                } else {
                    return TsonNumberLayout.DECIMAL;
                }
            }
        }
        return TsonNumberLayout.DECIMAL;
    }

    private static TsonNumberHelper parseReal(String a, String unit, NumberType[] numberType) {
        TsonNumberLayout r = detectRadix(a);
        for (NumberType type : numberType) {
            TsonNumberHelper v = parseReal0(a, type, r, unit);
            if (v != null) {
                return v;
            }
        }
        throw new IllegalArgumentException("unable to parse number " + a);
    }

    private static TsonNumberHelper parseReal(String a, TsonNumberLayout layout, String unit, NumberType[] numberType) {
        for (NumberType type : numberType) {
            TsonNumberHelper v = parseReal0(a, type, layout, unit);
            if (v != null) {
                return v;
            }
        }
        throw new IllegalArgumentException("unable to parse number " + a);
    }

    private static TsonNumberHelper parseReal0(String a, NumberType numberType, TsonNumberLayout radix, String unit) {
        try {
            switch (numberType) {
                case BYTE: {
                    return new TsonNumberHelper(Byte.parseByte(a, radix.radix()), NumberType.BYTE, radix, unit);
                }
                case SHORT: {
                    return new TsonNumberHelper(Short.parseShort(a, radix.radix()), NumberType.SHORT, radix, unit);
                }
                case INT: {
                    return new TsonNumberHelper(Integer.parseInt(a, radix.radix()), NumberType.INT, radix, unit);
                }
                case LONG: {
                    return new TsonNumberHelper(Long.parseLong(a, radix.radix()), NumberType.LONG, radix, unit);
                }
                case FLOAT: {
                    return new TsonNumberHelper(Float.parseFloat(a), NumberType.FLOAT, radix, unit);
                }
                case DOUBLE: {
                    return new TsonNumberHelper(Double.parseDouble(a), NumberType.DOUBLE, radix, unit);
                }
                case BIG_DECIMAL: {
                    return new TsonNumberHelper(new BigDecimal(a), NumberType.BIG_DECIMAL, radix, unit);
                }
                case BIG_INTEGER: {
                    return new TsonNumberHelper(new BigInteger(a, radix.radix()), NumberType.BIG_INTEGER, radix, unit);
                }
            }
        } catch (Exception ex) {
            return null;
        }
        throw new IllegalArgumentException("unexpected precision: " + numberType);
    }


    public static TsonNumberHelper parse(String s) {
        if (s == null) {
            throw new IllegalArgumentException("null string cannot be parse as number");
        }
        s = s.trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("empty string cannot be parse as number");
        }


        StrFastReader br = new StrFastReader(s);
        Nbr realStr = readOneDouble(br);
        Nbr imagStr = null;
        if (realStr.getNh().unit == null && br.peekAny('+', '-') != null) {
            imagStr = readOneDouble(br);
            if (br.hasNext()) {
                throw new IllegalArgumentException("invalid number " + s + ". unable to read " + br + " in s");
            }
        } else if (br.hasNext()) {
            throw new IllegalArgumentException("invalid number " + s + ". unable to read " + br + " in s");
        }
        if (imagStr != null) {
            NumberType newType = realStr.type.combine(imagStr.type);
            switch (newType) {
                case BYTE:
                case SHORT:
                case INT: {
                    newType = NumberType.FLOAT;
                    break;
                }
                case BIG_INTEGER: {
                    newType = NumberType.BIG_DECIMAL;
                    break;
                }
            }
            realStr.setNh(realStr.getNh().to(newType));
            imagStr.setNh(imagStr.getNh().to(newType));
            return new TsonNumberHelper(realStr.getNh().real, imagStr.getNh().imag, newType, realStr.getNh().layout, imagStr.getNh().unit);
        } else {
            return realStr.getNh();
        }
    }

    private String lpj() {
        switch (layout) {
            case BINARY: {
                return ("0b");
            }
            case OCTAL: {
                return ("0");
            }
            case HEXADECIMAL: {
                return ("0x");
            }
        }
        return "";
    }

    private String lp() {
        switch (layout) {
            case DECIMAL:
                return "_";
            case HEXADECIMAL:
                return "x";
            case OCTAL:
                return "o";
            case BINARY:
                return "b";
        }
        throw new IllegalArgumentException("unexpected");
    }

    private String toStringPart(Number value, boolean compactMax) {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case BYTE: {
                byte t = value.byteValue();
                sb.append("0u1");
                sb.append(lp());
                sb.append(Long.toString(t, layout.radix()));
                break;
            }
            case SHORT: {
                short t = value.shortValue();
                sb.append("0u2");
                sb.append(lp());
                if (compactMax) {
                    switch (t) {
                        case Short.MIN_VALUE: {
                            sb.append("Min");
                            break;
                        }
                        case Short.MAX_VALUE: {
                            sb.append("Max");
                            break;
                        }
                        default: {
                            sb.append(Long.toString(t, layout.radix()));
                        }
                    }
                } else {
                    sb.append(Long.toString(t, layout.radix()));
                }
                break;
            }
            case INT: {
                int t = value.intValue();
                if (compactMax) {
                    switch (t) {
                        case Integer.MIN_VALUE: {
                            sb.append("0u4Min");
                            break;
                        }
                        case Integer.MAX_VALUE: {
                            sb.append("0u4Max");
                            break;
                        }
                        default: {
                            sb.append(lpj());
                            sb.append(Long.toString(t, layout.radix()));
                        }
                    }
                } else {
                    sb.append(lpj());
                    sb.append(Long.toString(t, layout.radix()));
                }
                break;
            }
            case LONG: {
                long t = value.longValue();
                if (compactMax) {
                    if (t == Long.MIN_VALUE) {
                        sb.append("0MinL");
                    } else if (t == Long.MAX_VALUE) {
                        sb.append("0MaxL");
                    } else {
                        sb.append(lpj());
                        sb.append(Long.toString(t, layout.radix()));
                        sb.append("L");
                    }
                } else {
                    sb.append(lpj());
                    sb.append(Long.toString(t, layout.radix()));
                    sb.append("L");
                }
                break;
            }
            case BIG_INTEGER: {
                sb.append(lpj());
                sb.append((((BigInteger) value).toString(layout.radix())));
                sb.append("LL");
                break;
            }
            case FLOAT: {
                float t = value.floatValue();
                if (Float.isNaN(t)) {
                    sb.append("NaNf");
                } else if (t == Float.NEGATIVE_INFINITY) {
                    sb.append("NInff");
                } else if (t == Float.POSITIVE_INFINITY) {
                    sb.append("PInff");
                } else {
                    sb.append(t);
                    sb.append("f");
                }
                break;
            }
            case DOUBLE: {
                double t = value.doubleValue();
                if (Double.isNaN(t)) {
                    sb.append("NaN");
                } else if (t == Double.NEGATIVE_INFINITY) {
                    sb.append("NInf");
                } else if (t == Double.POSITIVE_INFINITY) {
                    sb.append("PInf");
                } else {
                    sb.append(t);
                }
                break;
            }
            case BIG_DECIMAL: {
                sb.append(value);
                sb.append("LL");
                break;
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean compactMax) {
        StringBuilder sb = new StringBuilder();
        sb.append(toStringPart(real, compactMax));
        if (complex) {
            String i = toStringPart(imag, compactMax);
            if (!i.startsWith("+") && i.startsWith("-")) {
                sb.append("+");
            }
            sb.append(i);
        }
        if (unit != null && !unit.isEmpty()) {
            if (!unit.startsWith("%") && !unit.startsWith("_")) {
                sb.append("_");
            }
            sb.append(unit);
        }
        return sb.toString();
    }

    public Number getReal() {
        return real;
    }

    public Number getImag() {
        return imag;
    }

    public boolean isComplex() {
        return complex;
    }

    public NumberType getType() {
        return type;
    }

    public String getUnit() {
        return unit;
    }

    public TsonNumberLayout getRadix() {
        return layout;
    }

    private enum NbrSpecial {
        MIN, MAX, NAN, INF,
    }

    private static class Nbr {
        String sign;
        TsonNumberLayout layout;
        NumberType type;
        String nbr;
        String unit;
        StringBuilder sb = new StringBuilder();
        boolean decimal;
        String suffix;
        private TsonNumberHelper nh;
        NbrSpecial special;

        public void trimNbr() {
            StringBuilder sb = new StringBuilder();
            for (char c : this.nbr.toCharArray()) {
                if (c != '_') {
                    sb.append(c);
                }
            }
            this.nbr = sb.toString();
        }

        public void balanceHex() {
            if (unit.length() > 0 && !(nbr.endsWith("_"))) {
                int i = nbr.lastIndexOf('_');
                if (i > 0) {
                    unit = nbr.substring(i + 1) + unit;
                    nbr = nbr.substring(0, i);
                }
            }
        }

        public TsonNumberHelper getNh() {
            return nh;
        }

        public Nbr setNh(TsonNumberHelper nh) {
            this.nh = nh;
            return this;
        }

        interface NbrParse {
            Number parse(String s, int radix);
        }

        public void fillTsonNumberHelperAsInt() {
            fillTsonNumberHelper((s, radix) -> Integer.parseInt(s, radix));
        }

        public void fillTsonNumberHelperAsLong() {
            fillTsonNumberHelper((s, radix) -> Long.parseLong(s, radix));
        }

        public void fillTsonNumberHelperAsByte() {
            fillTsonNumberHelper((s, radix) -> Byte.parseByte(s, radix));
        }

        public void fillTsonNumberHelperAsShort() {
            fillTsonNumberHelper((s, radix) -> Short.parseShort(s, radix));
        }

        public void fillTsonNumberHelperTryAll(NumberType... all) {
            for (NumberType numberType : all) {
                try {
                    type = numberType;
                    fillTsonNumberHelper();
                    return;
                } catch (Exception ex) {
                    //
                }
            }
            type = all[0];
            fillTsonNumberHelper();
        }

        public void fillTsonNumberHelper() {
            switch (type) {
                case BYTE: {
                    fillTsonNumberHelper((s, radix) -> Byte.parseByte(s, radix));
                    break;
                }
                case SHORT: {
                    fillTsonNumberHelper((s, radix) -> Short.parseShort(s, radix));
                    break;
                }
                case INT: {
                    fillTsonNumberHelper((s, radix) -> Integer.parseInt(s, radix));
                    break;
                }
                case LONG: {
                    fillTsonNumberHelper((s, radix) -> Long.parseLong(s, radix));
                    break;
                }
                case BIG_INTEGER: {
                    fillTsonNumberHelper((val, radix) -> new BigInteger(val, radix));
                    break;
                }
                case FLOAT: {
                    fillTsonNumberHelper((s, r) -> Float.parseFloat(s));
                    break;
                }
                case DOUBLE: {
                    fillTsonNumberHelper((s, r) -> Double.parseDouble(s));
                    break;
                }
                case BIG_DECIMAL: {
                    fillTsonNumberHelper((s, r) -> new BigDecimal(s));
                    break;
                }
            }

        }

        public void fillTsonNumberHelperValue(Number pp) {
            setNh(new TsonNumberHelper(pp, type, layout, unit));
        }

        public void fillTsonNumberHelper(NbrParse pp) {
            setNh(new TsonNumberHelper(pp.parse(("-".equals(sign) ? "-" : "") + nbr, layout.radix()), type, layout, unit));
        }

        public void parse(TsonNumberLayout tsonNumberLayout, NumberType numberType, StrFastReader br) {
            layout = tsonNumberLayout;
            type = numberType;
            switch (tsonNumberLayout) {
                case DECIMAL: {
                    if (br.read("Min")) {
                        sb.append("Min");
                        special = NbrSpecial.MIN;
                    } else if (br.read("Max")) {
                        sb.append("Max");
                        special = NbrSpecial.MAX;
                    } else {
                        nbr = br.readWhile(c -> (c >= '0' && c <= '9') || c == '_');
                        sb.append(nbr);
                    }
                    unit = br.readWhile(c -> true);
                    sb.append(unit);
                    trimNbr();
                    break;
                }
                case BINARY: {
                    if (br.read("Min")) {
                        sb.append("Min");
                        special = NbrSpecial.MIN;
                    } else if (br.read("Max")) {
                        sb.append("Max");
                        special = NbrSpecial.MAX;
                    } else {
                        nbr = br.readWhile(c -> (c >= '0' && c <= '1') || c == '_');
                    }
                    sb.append(nbr);
                    unit = br.readWhile(c -> true);
                    sb.append(unit);
                    trimNbr();
                    break;
                }
                case OCTAL: {
                    if (br.read("Min")) {
                        sb.append("Min");
                        special = NbrSpecial.MIN;
                    } else if (br.read("Max")) {
                        sb.append("Max");
                        special = NbrSpecial.MAX;
                    } else {
                        nbr = br.readWhile(c -> (c >= '0' && c <= '7') || c == '_');
                    }
                    sb.append(nbr);
                    unit = br.readWhile(c -> true);
                    sb.append(unit);
                    trimNbr();
                    break;
                }
                case HEXADECIMAL: {
                    if (br.read("Min")) {
                        sb.append("Min");
                        special = NbrSpecial.MIN;
                    } else if (br.read("Max")) {
                        sb.append("Max");
                        special = NbrSpecial.MAX;
                    } else {
                        nbr = br.readWhile(c -> (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || c == '_');
                    }
                    sb.append(nbr);
                    unit = br.readWhile(c -> true);
                    sb.append(unit);
                    balanceHex();
                    trimNbr();
                    break;
                }
            }
        }
    }

    private static Nbr readOneDouble(StrFastReader br) {
        Nbr n = new Nbr();
        if (br.read('+')) {
            n.sign = "+";
            n.sb.append("+");
        } else if (br.read('-')) {
            n.sign = "-";
            n.sb.append("-");
        }
        if (br.read("0u")) {
            n.sb.append("0u");
            String u = br.peek(2);
            switch ("0u" + u) {
                case "0u1_": {
                    n.type = NumberType.BYTE;
                    br.read(2);
                    n.sb.append(u);
                    n.parse(TsonNumberLayout.DECIMAL, NumberType.BYTE, br);
                    if (n.special != null) {
                        switch (n.special) {
                            case MIN: {
                                n.fillTsonNumberHelperValue(Byte.MIN_VALUE);
                                break;
                            }
                            case MAX: {
                                n.fillTsonNumberHelperValue(Byte.MAX_VALUE);
                                break;
                            }
                        }
                    } else {
                        n.fillTsonNumberHelper();
                    }
                    return n;
                }
                case "0u1b": {
                    n.type = NumberType.BYTE;
                    br.read(2);
                    n.sb.append(u);
                    n.parse(TsonNumberLayout.BINARY, NumberType.BYTE, br);
                    if (n.special != null) {
                        switch (n.special) {
                            case MIN: {
                                n.fillTsonNumberHelperValue(Byte.MIN_VALUE);
                                break;
                            }
                            case MAX: {
                                n.fillTsonNumberHelperValue(Byte.MAX_VALUE);
                                break;
                            }
                        }
                    } else {
                        n.type = NumberType.BYTE;
                        n.fillTsonNumberHelper();
                        n.setNh(new TsonNumberHelper(Byte.parseByte(n.nbr, n.layout.radix()), n.type, n.layout, n.unit));
                    }
                    return n;
                }
                case "0u1o": {
                    n.type = NumberType.BYTE;
                    br.read(2);
                    n.sb.append(u);
                    n.parse(TsonNumberLayout.OCTAL, NumberType.BYTE, br);
                    if (n.special != null) {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Byte.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Byte.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                    } else {
                        n.setNh(new TsonNumberHelper(Byte.parseByte(n.nbr, n.layout.radix()), n.type, n.layout, n.unit));
                    }
                    return n;
                }
                case "0u1x": {
                    n.type = NumberType.BYTE;
                    br.read(2);
                    n.sb.append(u);
                    n.parse(TsonNumberLayout.HEXADECIMAL, NumberType.BYTE, br);
                    if (n.special != null) {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Byte.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Byte.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                    } else {
                        n.setNh(new TsonNumberHelper(Byte.parseByte(n.nbr, n.layout.radix()), n.type, n.layout, n.unit));
                    }
                    return n;
                }
                case "0u2_": {
                    n.type = NumberType.SHORT;
                    br.read(2);
                    n.sb.append(u);
                    n.parse(TsonNumberLayout.DECIMAL, NumberType.SHORT, br);
                    if (n.special != null) {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Short.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Short.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                    } else {
                        n.setNh(new TsonNumberHelper(Short.parseShort(n.nbr, n.layout.radix()), n.type, n.layout, n.unit));
                    }
                    return n;
                }
                case "0u2b": {
                    n.type = NumberType.SHORT;
                    br.read(2);
                    n.sb.append(u);
                    n.parse(TsonNumberLayout.BINARY, NumberType.SHORT, br);
                    if (n.special != null) {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Short.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Short.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                    } else {
                        n.setNh(new TsonNumberHelper(Short.parseShort(n.nbr, n.layout.radix()), n.type, n.layout, n.unit));
                    }
                    return n;
                }
                case "0u2o": {
                    n.type = NumberType.SHORT;
                    br.read(2);
                    n.sb.append(u);
                    n.parse(TsonNumberLayout.OCTAL, NumberType.SHORT, br);
                    if (n.special != null) {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Short.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Short.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                    } else {
                        n.setNh(new TsonNumberHelper(Short.parseShort(n.nbr, n.layout.radix()), n.type, n.layout, n.unit));
                    }
                    return n;
                }
                case "0u2x": {
                    n.type = NumberType.SHORT;
                    br.read(2);
                    n.sb.append(u);
                    n.parse(TsonNumberLayout.HEXADECIMAL, NumberType.SHORT, br);
                    if (n.special != null) {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Short.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Short.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                    } else {
                        n.setNh(new TsonNumberHelper(Short.parseShort(n.nbr, n.layout.radix()), n.type, n.layout, n.unit));
                    }
                    return n;
                }
                case "0u4_": {
                    n.type = NumberType.INT;
                    br.read(2);
                    n.sb.append(u);
                    n.parse(TsonNumberLayout.DECIMAL, NumberType.INT, br);
                    if (n.special != null) {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Integer.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Integer.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                    } else {
                        n.setNh(new TsonNumberHelper(Integer.parseInt(n.nbr, n.layout.radix()), n.type, n.layout, n.unit));
                    }
                    return n;
                }
                case "0u4b": {
                    n.type = NumberType.INT;
                    br.read(2);
                    n.sb.append(u);
                    n.parse(TsonNumberLayout.BINARY, NumberType.INT, br);
                    if (n.special != null) {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Integer.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Integer.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                    } else {
                        n.setNh(new TsonNumberHelper(Integer.parseInt(n.nbr, n.layout.radix()), n.type, n.layout, n.unit));
                    }
                    return n;
                }
                case "0u4o": {
                    n.type = NumberType.INT;
                    br.read(2);
                    n.sb.append(u);
                    n.parse(TsonNumberLayout.OCTAL, NumberType.INT, br);
                    if (n.special != null) {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Integer.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Integer.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                    } else {
                        n.setNh(new TsonNumberHelper(Integer.parseInt(n.nbr, n.layout.radix()), n.type, n.layout, n.unit));
                    }
                    return n;
                }
                case "0u4x": {
                    n.type = NumberType.INT;
                    br.read(2);
                    n.sb.append(u);
                    n.parse(TsonNumberLayout.HEXADECIMAL, NumberType.INT, br);
                    if (n.special != null) {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Integer.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Integer.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                    } else {
                        n.setNh(new TsonNumberHelper(Integer.parseInt(n.nbr, n.layout.radix()), n.type, n.layout, n.unit));
                    }
                    return n;
                }
                case "0u8_": {
                    n.type = NumberType.LONG;
                    br.read(2);
                    n.sb.append(u);
                    n.parse(TsonNumberLayout.DECIMAL, NumberType.LONG, br);
                    if (n.special != null) {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Long.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Long.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                    } else {
                        n.setNh(new TsonNumberHelper(Long.parseLong(n.nbr, n.layout.radix()), n.type, n.layout, n.unit));
                    }
                    return n;
                }
                case "0u8b": {
                    n.type = NumberType.LONG;
                    br.read(2);
                    n.sb.append(u);
                    n.parse(TsonNumberLayout.BINARY, NumberType.LONG, br);
                    if (n.special != null) {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Long.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Long.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                    } else {
                        n.setNh(new TsonNumberHelper(Long.parseLong(n.nbr, n.layout.radix()), n.type, n.layout, n.unit));
                    }
                    return n;
                }
                case "0u8o": {
                    n.type = NumberType.LONG;
                    br.read(2);
                    n.sb.append(u);
                    n.parse(TsonNumberLayout.OCTAL, NumberType.LONG, br);
                    if (n.special != null) {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Long.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Long.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                    } else {
                        n.setNh(new TsonNumberHelper(Long.parseLong(n.nbr, n.layout.radix()), n.type, n.layout, n.unit));
                    }
                    return n;
                }
                case "0u8x": {
                    n.type = NumberType.LONG;
                    br.read(2);
                    n.sb.append(u);
                    n.parse(TsonNumberLayout.HEXADECIMAL, NumberType.LONG, br);
                    if (n.special != null) {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Long.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Long.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                    } else {
                        n.setNh(new TsonNumberHelper(Long.parseLong(n.nbr, n.layout.radix()), n.type, n.layout, n.unit));
                    }
                    return n;
                }
            }
            throw new IllegalArgumentException("unexpected ");
        }
        if (br.read("0x")) {
            n.sb.append("0x");
            n.layout = TsonNumberLayout.HEXADECIMAL;
            n.type = NumberType.INT;
            if (br.read("Min")) {
                n.sb.append("Min");
                n.special = NbrSpecial.MIN;
            } else if (br.read("Max")) {
                n.sb.append("Max");
                n.special = NbrSpecial.MAX;
            } else {
                n.nbr = br.readWhile(c -> (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || c == '_');
                String wasL = br.readAny('l', 'L');
                if (wasL != null) {
                    String wasL2 = br.readAny('l', 'L');
                    if (wasL2 != null) {
                        n.suffix = wasL + wasL2;
                        n.type = NumberType.BIG_INTEGER;
                    } else {
                        n.suffix = wasL;
                        n.type = NumberType.LONG;
                    }
                }
            }
            n.sb.append(n.nbr);
            n.unit = br.readWhile(c -> true);
            n.sb.append(n.unit);
            n.balanceHex();
            n.trimNbr();
            if (n.special != null) {
                switch (n.type) {
                    case INT: {
                        switch (n.special) {
                            case MIN: {
                                n.fillTsonNumberHelperValue(Integer.MIN_VALUE);
                                break;
                            }
                            case MAX: {
                                n.fillTsonNumberHelperValue(Integer.MAX_VALUE);
                                break;
                            }
                        }
                        break;
                    }
                    case LONG: {
                        switch (n.special) {
                            case MIN: {
                                n.fillTsonNumberHelperValue(Long.MIN_VALUE);
                                break;
                            }
                            case MAX: {
                                n.fillTsonNumberHelperValue(Long.MAX_VALUE);
                                break;
                            }
                        }
                        break;
                    }
                }
            } else {
                switch (n.type) {
                    case INT: {
                        n.fillTsonNumberHelperTryAll(NumberType.INT, NumberType.LONG, NumberType.BIG_INTEGER);
                        return n;
                    }
                    case LONG:
                    case BIG_INTEGER: {
                        n.fillTsonNumberHelper();
                        break;
                    }
                }
            }
            return n;
        }
        if (br.read("0b")) {
            n.sb.append("0b");
            n.layout = TsonNumberLayout.BINARY;
            n.type = NumberType.INT;
            if (br.read("Min")) {
                n.sb.append("Min");
                n.special = NbrSpecial.MIN;
            } else if (br.read("Max")) {
                n.sb.append("Max");
                n.special = NbrSpecial.MAX;
            } else {
                n.nbr = br.readWhile(c -> (c >= '0' && c <= '1') || c == '_');
                String wasL = br.readAny('l', 'L');
                if (wasL != null) {
                    String wasL2 = br.readAny('l', 'L');
                    if (wasL2 != null) {
                        n.suffix = wasL + wasL2;
                        n.type = NumberType.BIG_INTEGER;
                    } else {
                        n.suffix = wasL;
                        n.type = NumberType.LONG;
                    }
                }
            }
            n.sb.append(n.nbr);
            n.unit = br.readWhile(c -> true);
            n.sb.append(n.unit);
            n.balanceHex();
            n.trimNbr();
            if (n.special != null) {
                switch (n.type) {
                    case INT: {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Integer.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Integer.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                        break;
                    }
                    case LONG: {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Long.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Long.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                        break;
                    }
                }
            } else {
                switch (n.type) {
                    case INT: {
                        for (NumberType tt : new NumberType[]{NumberType.INT, NumberType.LONG, NumberType.BIG_INTEGER}) {
                            try {
                                n.type = tt;
                                n.fillTsonNumberHelper();
                                return n;
                            } catch (Exception ex) {
                                //
                            }
                        }
                        n.type = NumberType.INT;
                        n.fillTsonNumberHelper((s, radix) -> Integer.parseInt(s, radix));
                        return n;
                    }
                    case LONG:
                    case BIG_INTEGER: {
                        n.fillTsonNumberHelper();
                        return n;
                    }
                }
            }
            return n;
        }
        if (br.read("0")) {
            n.sb.append("0");
            n.layout = TsonNumberLayout.OCTAL;
            n.type = NumberType.INT;
            if (br.read("Min")) {
                n.sb.append("Min");
                n.special = NbrSpecial.MIN;
            } else if (br.read("Max")) {
                n.sb.append("Max");
                n.special = NbrSpecial.MAX;
            } else {
                n.sb.append("0");
                readDblIEEE(br, n, "0");
            }
            n.unit = br.readWhile(c -> true);
            n.sb.append(n.unit);
            n.trimNbr();
            if (n.special != null) {
                switch (n.type) {
                    case INT: {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Integer.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Integer.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                        break;
                    }
                    case LONG: {
                        switch (n.special) {
                            case MIN: {
                                n.setNh(new TsonNumberHelper(Long.MIN_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                            case MAX: {
                                n.setNh(new TsonNumberHelper(Long.MAX_VALUE, n.type, n.layout, n.unit));
                                break;
                            }
                        }
                        break;
                    }
                }
            } else {
                switch (n.type) {
                    case INT: {
                        n.fillTsonNumberHelperTryAll(NumberType.INT, NumberType.LONG, NumberType.BIG_INTEGER);
                        return n;
                    }
                    case FLOAT:
                    case BIG_DECIMAL:
                    case DOUBLE:
                    case LONG:
                    case BIG_INTEGER: {
                        n.fillTsonNumberHelper();
                        break;
                    }
                }
            }
            return n;
        }
        if (br.read("NaNf")) {
            n.decimal = true;
            n.special = NbrSpecial.NAN;
            n.type = NumberType.FLOAT;
        } else if (br.read("Inff")) {
            n.decimal = true;
            n.special = NbrSpecial.INF;
            n.type = NumberType.FLOAT;
        } else if (br.read("NaN")) {
            n.decimal = true;
            n.special = NbrSpecial.NAN;
            n.type = NumberType.DOUBLE;
        } else if (br.read("Inf")) {
            n.decimal = true;
            n.special = NbrSpecial.INF;
            n.type = NumberType.DOUBLE;
        } else {
            readDblIEEE(br, n, "");
        }
        if (br.read('_')) {
            n.sb.append('_');
            n.unit = br.readWhile(c -> Character.isLetter(c));
            n.sb.append(n.unit);
        } else if (br.read('%')) {
            n.unit = "%" + br.readWhile(c -> Character.isLetter(c));
            n.sb.append(n.unit);
        }
        n.layout = TsonNumberLayout.DECIMAL;
        if (!n.decimal) {
            if (n.suffix != null) {
                switch (n.suffix.toUpperCase()) {
                    case "LL": {
                        n.type = NumberType.BIG_INTEGER;
                        n.fillTsonNumberHelper();
                        return n;
                    }
                    case "L": {
                        n.type = NumberType.LONG;
                        n.fillTsonNumberHelper();
                        return n;
                    }
                    case "F": {
                        n.type = NumberType.FLOAT;
                        n.fillTsonNumberHelper();
                        return n;
                    }
                }
            } else {
                n.fillTsonNumberHelperTryAll(NumberType.INT, NumberType.LONG, NumberType.BIG_INTEGER);
            }
        } else {
            if (n.special != null) {
                switch (n.special) {
                    case INF: {
                        switch (n.type) {
                            case DOUBLE: {
                                if ("-".equals(n.sign)) {
                                    n.setNh(new TsonNumberHelper(Double.NEGATIVE_INFINITY, n.type, n.layout, n.unit));
                                } else {
                                    n.setNh(new TsonNumberHelper(Double.POSITIVE_INFINITY, n.type, n.layout, n.unit));
                                }
                                return n;
                            }
                            case FLOAT: {
                                if ("-".equals(n.sign)) {
                                    n.setNh(new TsonNumberHelper(Float.NEGATIVE_INFINITY, n.type, n.layout, n.unit));
                                } else {
                                    n.setNh(new TsonNumberHelper(Float.POSITIVE_INFINITY, n.type, n.layout, n.unit));
                                }
                                return n;
                            }
                        }
                        return n;
                    }
                    case NAN: {
                        switch (n.type) {
                            case DOUBLE: {
                                n.setNh(new TsonNumberHelper(Double.NaN, n.type, n.layout, n.unit));
                                return n;
                            }
                            case FLOAT: {
                                n.setNh(new TsonNumberHelper(Float.NaN, n.type, n.layout, n.unit));
                                return n;
                            }
                        }
                    }
                }
            }
            if (n.suffix != null) {
                switch (n.suffix.toUpperCase()) {
                    case "LL": {
                        n.type = NumberType.BIG_DECIMAL;
                        n.fillTsonNumberHelper();
                        return n;
                    }
                    case "L": {
                        n.type = NumberType.DOUBLE;
                        n.fillTsonNumberHelper();
                        return n;
                    }
                    case "F": {
                        n.type = NumberType.FLOAT;
                        n.fillTsonNumberHelper();
                        return n;
                    }
                }
            } else {
                n.fillTsonNumberHelperTryAll(NumberType.DOUBLE, NumberType.BIG_DECIMAL);
                return n;
            }
        }
        return n;
    }

    private static void readDblIEEE(StrFastReader br, Nbr n, String prefix) {
        StringBuilder nbr = new StringBuilder();
        nbr.append(prefix);
        String beforeDot = br.readWhile(c -> (c >= '0' && c <= '9') || c == '_');
        if (beforeDot != null) {
            if (beforeDot.endsWith("_")) {
                beforeDot = beforeDot.substring(0, beforeDot.length() - 1);
                br.unread();
            }
            n.sb.append(beforeDot);
            nbr.append(beforeDot);
        }

        String afterDot = null;
        String expSign = null;
        String exp = null;
        String expNbr = null;
        if (br.read('.')) {
            n.decimal = true;
            n.sb.append(".");
            nbr.append(".");
            afterDot = br.readWhile(c -> (c >= '0' && c <= '9') || c == '_');
            if (afterDot != null) {
                if (afterDot.endsWith("_")) {
                    afterDot = afterDot.substring(0, afterDot.length() - 1);
                    br.unread();
                }
                n.sb.append(afterDot);
                nbr.append(afterDot);
            }
        }
        exp = br.readAny('e', 'E');
        if (exp != null) {
            n.decimal = true;
            n.sb.append(exp);
            nbr.append(exp);
            expSign = br.readAny('+', '-');
            if (expSign != null) {
                n.sb.append(expSign);
                nbr.append(expSign);
            }
            expNbr = br.readWhile(c -> c >= '0' && c <= '9');
            n.sb.append(expNbr);
        }
        n.nbr = nbr.toString();
        if (n.decimal) {
            n.layout = TsonNumberLayout.DECIMAL;
            n.suffix = br.readAnyIgnoreCase("LL", "L", "F");
            switch (n.suffix == null ? "" : n.suffix.toUpperCase()) {
                case "":
                case "L": {
                    n.type = NumberType.DOUBLE;
                    break;
                }
                case "LL": {
                    n.type = NumberType.BIG_DECIMAL;
                    break;
                }
                case "F": {
                    n.type = NumberType.FLOAT;
                    break;
                }
            }
        } else {
            n.suffix = br.readAnyIgnoreCase("LL", "L", "F");
            switch (n.suffix == null ? "" : n.suffix.toUpperCase()) {
                case "": {
                    n.type = NumberType.INT;
                    break;
                }
                case "L": {
                    n.type = NumberType.LONG;
                    break;
                }
                case "LL": {
                    n.type = NumberType.BIG_INTEGER;
                    break;
                }
                case "F": {
                    n.type = NumberType.FLOAT;
                    break;
                }
            }
        }
    }

    public TsonElement toTson() {
        if (isComplex()) {
            switch (getType()) {
                case BYTE:
                case SHORT:
                case INT:
                case FLOAT:
                    return Tson.ofFloatComplex(
                            getReal().floatValue(),
                            getImag().floatValue(),
                            getUnit());
                case LONG:
                case DOUBLE:
                    return Tson.ofDoubleComplex(
                            getReal().doubleValue(),
                            getImag().doubleValue(), getUnit());
                case BIG_INTEGER:
                    return Tson.ofBigComplex(
                            new BigDecimal((BigInteger) getReal()),
                            new BigDecimal((BigInteger) getImag()),
                            getUnit());
                case BIG_DECIMAL:
                    return Tson.ofBigComplex(
                            (BigDecimal) getReal(),
                            (BigDecimal) getImag(),
                            getUnit());
            }
        } else {
            switch (getType()) {
                case BYTE:
                    return Tson.ofByte(getReal().byteValue(), getRadix(), getUnit());
                case SHORT:
                    return Tson.ofShort(getReal().shortValue(), getRadix(), getUnit());
                case INT:
                    return Tson.ofInt(getReal().intValue(), getRadix(), getUnit());
                case LONG:
                    return Tson.ofLong(getReal().longValue(), getRadix(), getUnit());
                case FLOAT:
                    return Tson.ofFloat(getReal().floatValue(), getUnit());
                case DOUBLE:
                    return Tson.ofDouble(getReal().floatValue(), getUnit());
                case BIG_INTEGER:
                    return Tson.ofBigInt((BigInteger) getReal(), getRadix(), getUnit());
                case BIG_DECIMAL:
                    return Tson.ofBigDecimal((BigDecimal) getReal(), getUnit());
            }
        }
        throw new IllegalArgumentException("Unable to parse number " + toString());
    }


}

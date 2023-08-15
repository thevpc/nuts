package net.thevpc.nuts.runtime.standalone.text.util;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NStringUtils;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class DefaultUnitFormat {

    private static int[] ALL_POWS = {-12, -9, -6, -3, -2, -1, 0, 1, 2, 3, 6, 9, 12};
    boolean leadingZeros = false;
    boolean intermediateZeros = true;
    private boolean fixedLength = false;
    private boolean decimal = false;
    private int high = 6;
    private int low = -2;
    private int fractionDigits = 3;
    private int integerDigits = 3;
    private DecimalFormat decimalFormat = null;
    private int[] validPows = {};
    private Set<Integer> excludedPows = new HashSet<>();
    private String mainUnitName;

    public DefaultUnitFormat(String mainUnitName, boolean leadingZeros, boolean intermediateZeros, boolean fixedLength, int high, int low, boolean decimal) {
        this.mainUnitName = mainUnitName;
        this.leadingZeros = leadingZeros;
        this.intermediateZeros = intermediateZeros;
        this.fixedLength = fixedLength;
        this.high = high;
        this.low = low;
        this.decimal = decimal;
    }

    public DefaultUnitFormat(String format) {
        if(format==null){
            format="";
        }
        format=format.trim();
        int e = format.indexOf(' ');
        if (e < 0) {
            this.mainUnitName = format.trim();
            format = "";
        } else {
            this.mainUnitName = format.substring(0, e);
            format = format.substring(e + 1).trim();
        }
        if (this.mainUnitName.isEmpty()) {
            throw new IllegalArgumentException("expected unit name");
        }
        if (format.isEmpty()) {
            format = "M-3 M3 I2 D2";
        }
        leadingZeros = false;
        intermediateZeros = false;
        int low = Integer.MIN_VALUE;
        int high = Integer.MAX_VALUE;
        if (format != null) {
            boolean startInterval = true;
            char[] charArray = format.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                char c = Character.toUpperCase(charArray[i]);
                switch (c) {
                    case ' ': {
                        //ignore
                        break;
                    }
                    //exclude
                    case 'X': {
                        ValAndI t = readInt(charArray, i);
                        i = t.i;
                        requireValidMultiplier(t);
                        excludedPows.add(t.v);
                        break;
                    }
                    case 'M': {
                        ValAndI t = readInt(charArray, i);
                        i = t.i;
                        requireValidMultiplier(t);
                        if (startInterval) {
                            startInterval = false;
                            low = t.v;
                        } else {
                            high = t.v;
                        }
                        break;
                    }
                    case 'D': {
                        decimal = true;
                        if (i + 1 < charArray.length && Character.isDigit(charArray[i + 1])) {
                            i++;
                            fractionDigits = charArray[i] - '0';
                        }
                        break;
                    }
                    case 'F': {
                        fixedLength = true;
                        break;
                    }
                    case 'I': {
                        if (i + 1 < charArray.length && Character.isDigit(charArray[i + 1])) {
                            i++;
                            integerDigits = charArray[i] - '0';
                        }
                        break;
                    }
                    case '0': {
                        if (i == 0) {
                            leadingZeros = true;
                        } else {
                            intermediateZeros = true;
                        }
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Unsupported " + c);
                    }
                }
            }
        }
        this.validPows = IntStream.of(ALL_POWS).mapToObj(x -> x).filter(x -> !excludedPows.contains(x))
                .mapToInt(x -> x.intValue()).toArray();
        if (this.validPows.length == 0) {
            throw new IllegalArgumentException("invalid powers");
        }
        if (low == Integer.MIN_VALUE) {
            low = this.validPows[0];
        }
        if (high == Integer.MAX_VALUE) {
            high = this.validPows[this.validPows.length - 1];
        }
        this.low = (low);
        this.high = (high);
        if (this.high < this.low) {
            int t = this.low;
            this.low = this.high;
            this.high = t;
        }
        //force decimal
        decimal = true;
        if (fixedLength) {
            decimalFormat = new DecimalFormat("0." + NStringUtils.repeat('0', integerDigits));
        } else {
            decimalFormat = new DecimalFormat("0.0");
        }
    }

    private void requireValidMultiplier(ValAndI t) {
        if (!isValidGenPow(t.v)) {
            throw new IllegalArgumentException("Invalid Power " + t.v);
        }
    }

    private boolean isValidGenPow(int cc) {
        for (int pow : ALL_POWS) {
            if (pow == cc) {
                return true;
            }
        }
        return false;
    }

    public String toPattern() {
        StringBuilder sb = new StringBuilder();
        if (leadingZeros) {
            sb.append('0');
        }
        sb.append(' ').append('M').append(low);
        sb.append(' ').append('M').append(high);
        if (decimal) {
            sb.append('D').append(fractionDigits);
        }
        sb.append('I').append(integerDigits);
        if (fixedLength) {
            sb.append('F');
        }
        if (intermediateZeros) {
            sb.append(' ');
            sb.append('0');
        }

        return sb.toString();
    }
//    public static void main(String[] args) {
//        MetricFormat frt = new MetricFormat();
////        System.out.println(frt.format(0));
//        double f = 10E-6;
//        for (int i = 0; i < 10; i++) {
//            String s = frt.format(f);
//            System.out.println(s + " <== " + f);
//            f = f * 10;
//        }
//    }


    private String formatLeftPow(double number, int pow) {
        return formatLeft(number * 1.0 / Math.pow(10, pow));
    }

    private String formatLeft(double number) {
        String s = (decimal) ?
                formatLeft(number, integerDigits + 1 + fractionDigits)
                : formatLeft(number, integerDigits);
//        System.out.println("formatLeft " + s + " <= " + number);
        return s;
    }

    private String formatLeft(Object number, int size) {
        if (!decimal) {
            if (fixedLength) {
                return NStringUtils.alignLeft(String.valueOf(number), size);
            } else {
                return String.valueOf(number);
            }
        } else {
            String s = decimalFormat.format(number);
            if (fixedLength) {
                return NStringUtils.alignLeft(s, size);
            } else {
                return s;
            }
        }
    }

    public NText formatDouble(double value, NSession session) {
        return format(value, session);
    }


    private int evalInv(String pow) {
        if (pow.endsWith(mainUnitName)) {
            pow = pow.substring(0, pow.length() - mainUnitName.length());
        }
        switch (pow) {
            case "p":
                return -12;
            case "n":
                return -9;
            case "u":
                return -6;
            case "m":
                return -3;
            case "c":
                return -2;
            case "d":
                return -1;
            case "":
                return 0;
            case "da":
                return 1;
            case "h":
                return 2;
            case "k":
                return 3;
            case "M":
                return 6;
            case "G":
                return 9;
            case "T":
                return 12;
        }
        throw new IllegalArgumentException("Unsupported");
    }

    private String strUnit(int pow) {
        switch (pow) {
            case -12:
                return "p" + mainUnitName;
            case -9:
                return "n" + mainUnitName;
            case -6:
                return "u" + mainUnitName;
            case -3:
                return "m" + mainUnitName;
            case -2:
                return "c" + mainUnitName;
            case -1:
                return "d" + mainUnitName;
            case 0:
                return mainUnitName;
            case 1:
                return "da" + mainUnitName;
            case 2:
                return "h" + mainUnitName;
            case 3:
                return "k" + mainUnitName;
            case 6:
                return "M" + mainUnitName;
            case 9:
                return "G" + mainUnitName;
            case 12:
                return "T" + mainUnitName;
        }
        throw new IllegalArgumentException("Unsupported");
    }

    public NText format(double value, NSession session) {
        NTextBuilder sb = NTextBuilder.of(session);
        boolean neg = value < 0;
        int sign = neg ? -1 : 1;
        double v = value < 0 ? -value : value;
        double r = v;
        if (decimal) {
            if (v == 0) {
                sb.append(formatLeft(0), NTextStyle.number());
                sb.append(strUnit(low), NTextStyle.info());
            } else {
                if (v < Math.pow(10, low)) {
                    sb.append(formatLeftPow(v * sign, low), NTextStyle.number());
                    sb.append(strUnit(low), NTextStyle.info());
                } else if (v >= Math.pow(10, high)) {
                    sb.append(formatLeftPow(v * sign, high), NTextStyle.number())
                            .append(strUnit(high), NTextStyle.info());
                } else {
                    boolean ok = false;
                    for (int i = validPows.length - 1; i >= 0; i--) {
                        double b = Math.pow(10, validPows[i]);
                        if (v >= b) {
                            sb.append(formatLeftPow(v * sign, validPows[i]), NTextStyle.number())
                                    .append(strUnit(validPows[i]), NTextStyle.info());
                            ok = true;
                            break;
                        }
                    }
                    if (!ok) {
                        sb.append(formatLeftPow(v * sign, validPows[0]), NTextStyle.number())
                                .append(strUnit(validPows[0]), NTextStyle.info());
                    }
                }
            }
            return sb.toText();
        } else {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("Not supported yet"));
        }

    }

    private static class ValAndI {
        int v;
        int i;

        public ValAndI(int v, int i) {
            this.v = v;
            this.i = i;
        }
    }

    private static ValAndI readInt(char[] charArray, int i) {
        int v = 0;
        if (i + 1 < charArray.length) {
            i++;
            boolean nn = false;
            if (charArray[i] == '-') {
                nn = true;
                i++;
            }
            int r = 0;
            if (i < charArray.length && Character.isDigit(charArray[i])) {
                r = r * 10 + (charArray[i] - '0');
                while (i + 1 < charArray.length && Character.isDigit(charArray[i + 1])) {
                    i++;
                    r = r * 10 + (charArray[i] - '0');
                }
            } else {
                throw new IllegalArgumentException("Invalid");
            }
            v = nn ? -r : r;
        } else {
            throw new IllegalArgumentException("Invalid");
        }
        return new ValAndI(v, i);
    }

}

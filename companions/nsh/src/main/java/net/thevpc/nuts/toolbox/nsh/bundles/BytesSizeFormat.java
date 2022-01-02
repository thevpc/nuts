package net.thevpc.nuts.toolbox.nsh.bundles;

/**
 * Created by vpc on 3/20/17.
 */
public class BytesSizeFormat implements DoubleFormat {


    public static final BytesSizeFormat INSTANCE = new BytesSizeFormat();
    boolean leadingZeros = false;
    boolean intermediateZeros = true;
    boolean trailingZeros = false;
    boolean alignRight = false;
    private boolean fixedLength = false;
    private boolean binaryPrefix = false;
    private boolean standardUnit = false;
    private int high = 12;
    private int low = 1;
    private int depth = Integer.MAX_VALUE;

    public BytesSizeFormat(boolean leadingZeros, boolean intermediateZeros, boolean fixedLength, boolean binaryPrefix, char high, char low, int depth) {
        this.leadingZeros = leadingZeros;
        this.intermediateZeros = intermediateZeros;
        this.fixedLength = fixedLength;
        this.binaryPrefix = binaryPrefix;
        this.high = evalIndex(high);
        this.low = evalIndex(low);
        this.depth = depth <= 0 ? Integer.MAX_VALUE : depth;
    }

//    public static void main(String[] args) {
//        long value=Units.GiBYTE;
//        for (String s : new String[]{
//                "0I0BEF0","00BEF0","B0TD1F","B0TD2F","B0TD3F","B0TD1FI","B0TD2FI","B0TD3FI","0I0BEF0","I0BEF","IBEF","0IBTF"
//        }) {
//            System.out.println("");
//            System.out.println("System.out.println(new BytesSizeFormat(\""+s+"\").format("+value+"));");
//            System.out.println(new BytesSizeFormat(s).format(value));
//        }
//    }
    public BytesSizeFormat() {
        this("B0EF");
    }

    @Override
    public String formatDouble(double value) {
        return format((long) value);
    }

    /**
     * Size format is a sequence of commands :
     * <ul>
     * <li>B,K,M,G,T : Show Bytes/Kilo/Mega/Giga/Tera, if this is the first
     * multiplier it will be considered as the minimum multiplier otherwise it
     * will be considered as the maximum multiplier</li>
     * <li>I : binary prefix (use 1024 multipliers)</li>
     * <li>D : multiplier maximum depth, should be suffixed with an integer (i.e
     * BTD2 means that if number is in giga will not show further than
     * kilo)</li>
     * <li>F : fixed length</li>
     * <li>Z : if used in the very first position (0) consider leadingZeros, if
     * at the last position consider trailing zeros if anywhere else consider
     * intermediateZeros</li>
     * </ul>
     * examples
     * <pre>
     *       System.out.println(new MemorySizeFormatter("0I0BEF0").format(1073741824));
     *       0Ei   0Pi   0Ti   1Gi   0Mi   0Ki
     *
     *       System.out.println(new MemorySizeFormatter("00BEF0").format(1073741824));
     *       0E   0P   0T   1G  73M 741K 824B
     *
     *       System.out.println(new MemorySizeFormatter("B0TD1F").format(1073741824));
     *       1G
     *
     *       System.out.println(new MemorySizeFormatter("B0TD2F").format(1073741824));
     *       1G  73M
     *
     *       System.out.println(new MemorySizeFormatter("B0TD3F").format(1073741824));
     *       1G  73M 741K
     *
     *       System.out.println(new MemorySizeFormatter("B0TD1FI").format(1073741824));
     *       1Gi
     *
     *       System.out.println(new MemorySizeFormatter("B0TD2FI").format(1073741824));
     *       1Gi
     *
     *       System.out.println(new MemorySizeFormatter("B0TD3FI").format(1073741824));
     *       1Gi
     *
     *       System.out.println(new MemorySizeFormatter("0I0BEF0").format(1073741824));
     *       0Ei   0Pi   0Ti   1Gi   0Mi   0Ki
     *
     *       System.out.println(new MemorySizeFormatter("I0BEF").format(1073741824));
     *       1Gi
     *
     *       System.out.println(new MemorySizeFormatter("IBEF").format(1073741824));
     *       1Gi
     *
     *       System.out.println(new MemorySizeFormatter("0IBTF").format(1073741824));
     *       0Ti   1Gi
     *
     *
     * </pre>
     *
     * @param format size format
     */
    public BytesSizeFormat(String format) {
        leadingZeros = false;
        intermediateZeros = false;
        trailingZeros = false;
        alignRight = true;
        if (format != null) {
            boolean startInterval = true;
            char[] charArray = format.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                char c = charArray[i];//Character.toUpperCase(charArray[i]);
                switch (c) {
                    case 'B':
                    case 'K':
                    case 'M':
                    case 'G':
                    case 'T':
                    case 'P':
                    case 'E':
                    case 'Z':
                    case 'Y': {
                        if (startInterval) {
                            startInterval = false;
                            low = evalIndex(c);
                        } else {
                            high = evalIndex(c);
                        }
                        break;
                    }
                    case 'I': {
                        binaryPrefix = true;
                        standardUnit = false;
                        break;
                    }
                    case 'i': {
                        binaryPrefix = true;
                        standardUnit = true;
                        break;
                    }
                    case 'D': {
                        i++;
                        if (Character.isDigit(charArray[i])) {
                            depth = charArray[i] - '0';
                        } else {
                            depth = -1;
                        }
                        if (depth <= 0 || depth > 9) {
                            throw new IllegalArgumentException("Invalid depth " + depth);
                        }
                    }
                    case 'F': {
                        fixedLength = true;
                        break;
                    }
                    case '>': {
                        fixedLength = true;
                        alignRight = true;
                        break;
                    }
                    case '<': {
                        fixedLength = true;
                        alignRight = false;
                        break;
                    }
                    case '0': {
                        if (i == 0) {
                            leadingZeros = true;
                        } else if (i == charArray.length - 1) {
                            trailingZeros = true;
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
        if (this.high < this.low) {
            int t = this.low;
            this.low = this.high;
            this.high = t;
        }
    }

    private char evalCharByIndex(int c) {
        switch (c) {
            case 1: {
                return 'B';
            }
            case 2: {
                return 'K';
            }
            case 6: {
                return 'M';
            }
            case 9: {
                return 'G';
            }
            case 12: {
                return 'T';
            }
            case 15: {
                return 'P';
            }
            case 18: {
                return 'E';
            }
//            case 'Z': {
//                return binaryPrefix ? ZiBYTE : ZETTA;
//            }
//            case 'Y': {
//                return binaryPrefix ? YiBYTE : YOTTA;
//            }
        }
        throw new IllegalArgumentException("Unsupported");
    }

    private int evalIndex(char c) {
        switch (c) {
            case 'B': {
                return 1;
            }
            case 'K': {
                return 3;
            }
            case 'M': {
                return 6;
            }
            case 'G': {
                return 9;
            }
            case 'T': {
                return 12;
            }
            case 'P': {
                return 15;
            }
            case 'E': {
                return 18;
            }
//            case 'Z': {
//                return binaryPrefix ? ZiBYTE : ZETTA;
//            }
//            case 'Y': {
//                return binaryPrefix ? YiBYTE : YOTTA;
//            }
        }
        throw new IllegalArgumentException("Unsupported");
    }

    private long eval(char c) {
        switch (c) {
            case 'B': {
                return binaryPrefix ? Units.BYTE : Units.BYTE;
            }
            case 'K': {
                return binaryPrefix ? Units.KiBYTE : Units.KILO;
            }
            case 'M': {
                return binaryPrefix ? Units.MiBYTE : Units.MEGA;
            }
            case 'G': {
                return binaryPrefix ? Units.GiBYTE : Units.GIGA;
            }
            case 'T': {
                return binaryPrefix ? Units.TiBYTE : Units.TERA;
            }
            case 'P': {
                return binaryPrefix ? Units.PiBYTE : Units.PETA;
            }
            case 'E': {
                return binaryPrefix ? Units.EiBYTE : Units.EXA;
            }
//            case 'Z': {
//                return binaryPrefix ? ZiBYTE : ZETTA;
//            }
//            case 'Y': {
//                return binaryPrefix ? YiBYTE : YOTTA;
//            }
        }
        throw new IllegalArgumentException("Unsupported");
    }
    private long evalLongByIndex(int c) {
        switch (c) {
            case 1: {
                return binaryPrefix ? Units.BYTE : Units.BYTE;
            }
            case 3: {
                return binaryPrefix ? Units.KiBYTE : Units.KILO;
            }
            case 6: {
                return binaryPrefix ? Units.MiBYTE : Units.MEGA;
            }
            case 9: {
                return binaryPrefix ? Units.GiBYTE : Units.GIGA;
            }
            case 12: {
                return binaryPrefix ? Units.TiBYTE : Units.TERA;
            }
            case 15: {
                return binaryPrefix ? Units.PiBYTE : Units.PETA;
            }
            case 18: {
                return binaryPrefix ? Units.EiBYTE : Units.EXA;
            }
//            case 'Z': {
//                return binaryPrefix ? ZiBYTE : ZETTA;
//            }
//            case 'Y': {
//                return binaryPrefix ? YiBYTE : YOTTA;
//            }
        }
        throw new IllegalArgumentException("Unsupported");
    }

    private String formatNumber(Object number, int size) {
        if (fixedLength) {
            if (alignRight) {
                return _StringUtils.formatRight(number, size);
            } else {
                return _StringUtils.formatLeft(number, size);
            }
        } else {
            return String.valueOf(number);
        }
    }

    public String format(long bytes) {
        StringBuilder sb = new StringBuilder();
        boolean neg = bytes < 0;
        long v = bytes < 0 ? -bytes : bytes;
        long r = v;
        int currDepth = -1;
        boolean empty = true;
        boolean leading = true;
        long K = eval('K');
        long M = eval('M');
        long G = eval('G');
        long T = eval('T');
        long P = eval('P');
        long E = eval('E');

        int KK = 3;
        int MM = 6;
        int GG = 9;
        int TT = 12;
        int PP = 15;
        int EE = 18;
//        long Z = eval('Z');
//        long Y = eval('Y');
        if (low <= EE) {
//            if (high >= Y) {
//                r = v / T;
//                if ((leadingZeros && leading) || r > 0 || (!empty && intermediateZeros && ((v % Y)>0)) || (v==0 && trailingZeros)) {
//                    if (currDepth < 0) {
//                        currDepth = 1;
//                    } else {
//                        currDepth++;
//                    }
//                    if (currDepth <= depth) {
//                        if (sb.length() > 0) {
//                            sb.append(" ");
//                        }
//                        sb.append(formatLeft(r, 3)).append("Y");
//                        if(r!=0){
//                            leading=false;
//                        }
//                        v = v % Y;
//                        empty = false;
//                    }
//                }
//            }
//            if (high >= Z) {
//                r = v / T;
//                if ((leadingZeros && leading) || r > 0 || (!empty && intermediateZeros && ((v % Z)>0)) || (v==0 && trailingZeros)) {
//                    if (currDepth < 0) {
//                        currDepth = 1;
//                    } else {
//                        currDepth++;
//                    }
//                    if (currDepth <= depth) {
//                        if (sb.length() > 0) {
//                            sb.append(" ");
//                        }
//                        sb.append(formatLeft(r, 3)).append("Z");
//                        if(r!=0){
//                            leading=false;
//                        }
//                        v = v % Z;
//                        empty = false;
//                    }
//                }
//            }
            if (high >= EE) {
                r = v / T;
                if ((leadingZeros && leading) || r > 0 || (!empty && intermediateZeros && ((v % E) > 0)) || (v == 0 && trailingZeros)) {
                    if (currDepth < 0) {
                        currDepth = 1;
                    } else {
                        currDepth++;
                    }
                    if (currDepth <= depth) {
                        if (sb.length() > 0) {
                            sb.append(" ");
                        }
                        sb.append(formatNumber(r, 3)).append((binaryPrefix && !standardUnit) ? "Ei" : "E");
                        if (r != 0) {
                            leading = false;
                        }
                        v = v % E;
                        empty = false;
                    }
                }
            }
            if (high >= PP) {
                r = v / T;
                if ((leadingZeros && leading) || r > 0 || (!empty && intermediateZeros && ((v % P) > 0)) || (v == 0 && trailingZeros)) {
                    if (currDepth < 0) {
                        currDepth = 1;
                    } else {
                        currDepth++;
                    }
                    if (currDepth <= depth) {
                        if (sb.length() > 0) {
                            sb.append(" ");
                        }
                        sb.append(formatNumber(r, 3)).append((binaryPrefix && !standardUnit) ? "Pi" : "P");
                        if (r != 0) {
                            leading = false;
                        }
                        v = v % P;
                        empty = false;
                    }
                }
            }
            if (high >= TT) {
                r = v / T;
                if ((leadingZeros && leading) || r > 0 || (!empty && intermediateZeros && ((v % T) > 0)) || (v == 0 && trailingZeros)) {
                    if (currDepth < 0) {
                        currDepth = 1;
                    } else {
                        currDepth++;
                    }
                    if (currDepth <= depth) {
                        if (sb.length() > 0) {
                            sb.append(" ");
                        }
                        sb.append(formatNumber(r, 3)).append((binaryPrefix && !standardUnit) ? "Ti" : "T");
                        if (r != 0) {
                            leading = false;
                        }
                        v = v % T;
                        empty = false;
                    }
                }
            }
            if (low <= GG) {
                if (high >= GG) {
                    r = v / G;
                }
                if ((leadingZeros && leading) || r > 0 || (!empty && intermediateZeros && ((v % G) > 0)) || (v == 0 && trailingZeros)) {
                    if (currDepth < 0) {
                        currDepth = 1;
                    } else {
                        currDepth++;
                    }
                    if (currDepth <= depth) {
                        if (sb.length() > 0) {
                            sb.append(" ");
                        }
                        if (r != 0) {
                            leading = false;
                        }
                        sb.append(formatNumber(r, 3)).append((binaryPrefix && !standardUnit) ? "Gi" : "G");
                        v = v % G;
                        empty = false;
                    }
                }
                if (low <= MM) {
                    if (high >= MM) {
                        r = v / M;
                        if ((leadingZeros && leading) || r > 0 || (!empty && intermediateZeros && ((v % M) > 0)) || (v == 0 && trailingZeros)) {
                            if (currDepth < 0) {
                                currDepth = 1;
                            } else {
                                currDepth++;
                            }
                            if (currDepth <= depth) {
                                if (sb.length() > 0) {
                                    sb.append(" ");
                                }
                                if (r != 0) {
                                    leading = false;
                                }
                                sb.append(formatNumber(r, 3)).append((binaryPrefix && !standardUnit) ? "Mi" : "M");
                                v = v % M;
                                empty = false;
                            }
                        }
                    }
                    if (low <= KK) {
                        if (high >= KK) {
                            r = v / K;
                            if ((leadingZeros && leading) || r > 0 || (!empty && intermediateZeros && ((v % K) > 0)) || (v == 0 && trailingZeros)) {
                                if (currDepth < 0) {
                                    currDepth = 1;
                                } else {
                                    currDepth++;
                                }
                                if (currDepth <= depth) {
                                    if (sb.length() > 0) {
                                        sb.append(" ");
                                    }
                                    if (r != 0) {
                                        leading = false;
                                    }
                                    sb.append(formatNumber(r, 3)).append((binaryPrefix && !standardUnit) ? "Ki" : "K");
                                    v = v % K;
                                    empty = false;
                                }
                            }
                        }
                        if (low <= 1) {
                            if ((leadingZeros && leading) || v > 0 || sb.length() == 0 /*|| (!empty && intermediateZeros)*/) {
                                if (currDepth < 0) {
                                    currDepth = 1;
                                } else {
                                    currDepth++;
                                }
                                if (currDepth <= depth) {
                                    if (sb.length() > 0) {
                                        sb.append(" ");
                                    }
                                    if (r != 0) {
                                        leading = false;
                                    }
                                    sb.append(formatNumber(v, 3)).append((binaryPrefix && !standardUnit) ? "B " : "B");
                                    empty = false;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (sb.length() == 0) {
            if (neg) {
                sb.insert(0, "-");
            }
            if (low >= T) {
                sb.append(formatNumber(0, 3)).append((binaryPrefix && !standardUnit) ? "Ti" : "T");
            } else if (low >= G) {
                sb.append(formatNumber(0, 3)).append((binaryPrefix && !standardUnit) ? "Gi" : "G");
            } else if (low >= M) {
                sb.append(formatNumber(0, 3)).append((binaryPrefix && !standardUnit) ? "Mi" : "M");
            } else if (low >= K) {
                sb.append(formatNumber(0, 3)).append((binaryPrefix && !standardUnit) ? "Ki" : "K");
                sb.append(formatNumber(0, 3)).append((binaryPrefix && !standardUnit) ? "B " : "B");
            }
        } else {
            if (neg) {
                sb.insert(0, "-");
            }
        }
        return sb.toString();
    }


    public String toPattern(){
        StringBuilder sb=new StringBuilder();
        if(leadingZeros){
            sb.append('0');
        }
        sb.append(' ').append(evalCharByIndex(low));
        sb.append(' ').append(evalCharByIndex(high));
        if(depth>=0){
            sb.append('D').append(depth);
        }
        if(standardUnit){
            sb.append('i');
        }
        if(binaryPrefix){
            sb.append('I');
        }
        if(fixedLength){
            sb.append('F');
        }
        if(intermediateZeros){
            sb.append(' ');
            sb.append('0');
        }
        if(alignRight){
            sb.append('>');
        }else{
            sb.append('<');
        }
        if(trailingZeros){
            sb.append('0');
        }
        return sb.toString();
    }
}

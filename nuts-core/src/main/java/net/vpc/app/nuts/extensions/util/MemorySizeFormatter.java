package net.vpc.app.nuts.extensions.util;


/**
 * Created by vpc on 3/20/17.
 */
public class MemorySizeFormatter {
    boolean leadingZeros = false;
    boolean intermediateZeros = true;
    boolean trailingZeros = false;
    private boolean fixedLength = false;
    private boolean binaryPrefix = false;
    private long high = Units.TERA;
    private long low = Units.BYTE;
    private int depth = Integer.MAX_VALUE;

    public MemorySizeFormatter(boolean leadingZeros, boolean intermediateZeros, boolean fixedLength, boolean binaryPrefix, long high, long low,int depth) {
        this.leadingZeros = leadingZeros;
        this.intermediateZeros = intermediateZeros;
        this.fixedLength = fixedLength;
        this.binaryPrefix = binaryPrefix;
        this.high = high;
        this.low = low;
        this.depth = depth<=0?Integer.MAX_VALUE:depth;
    }

//    public static void main(String[] args) {
//        long value=Units.GiBYTE;
//        for (String s : new String[]{
//                "0I0BEF0","00BEF0","B0TD1F","B0TD2F","B0TD3F","B0TD1FI","B0TD2FI","B0TD3FI","0I0BEF0","I0BEF","IBEF","0IBTF"
//        }) {
//            System.out.println("");
//            System.out.println(">System.out.println(new MemorySizeFormatter(\""+s+"\").format("+value+"));");
//            System.out.println(new MemorySizeFormatter(s).format(value));
//        }
//    }

    public MemorySizeFormatter() {
        this("B0EF");
    }

    /**
     * Size format is a sequence of commands :
     * <ul>
     * <li>B,K,M,G,T : Show Bytes/Kilo/Mega/Giga/Tera, if this is the first multiplier it will be considered as the minimum multiplier otherwise it will be considered as the maximum multiplier</li>
     * <li>I : binary prefix (use 1024 multipliers)</li>
     * <li>D : multiplier maximum depth, should be suffixed with an integer (i.e BTD2 means that if number is in giga will not show further than kilo)</li>
     * <li>F : fixed length</li>
     * <li>Z : if used in the very first position (0) consider leadingZeros, if at the last position consider trailing zeros if anywhere else consider intermediateZeros</li>
     * </ul>
     * examples
     * <pre>
     *       >System.out.println(new MemorySizeFormatter("0I0BEF0").format(1073741824));
     *       0Ei   0Pi   0Ti   1Gi   0Mi   0Ki
     *
     *       >System.out.println(new MemorySizeFormatter("00BEF0").format(1073741824));
     *       0E   0P   0T   1G  73M 741K 824B
     *
     *       >System.out.println(new MemorySizeFormatter("B0TD1F").format(1073741824));
     *       1G
     *
     *       >System.out.println(new MemorySizeFormatter("B0TD2F").format(1073741824));
     *       1G  73M
     *
     *       >System.out.println(new MemorySizeFormatter("B0TD3F").format(1073741824));
     *       1G  73M 741K
     *
     *       >System.out.println(new MemorySizeFormatter("B0TD1FI").format(1073741824));
     *       1Gi
     *
     *       >System.out.println(new MemorySizeFormatter("B0TD2FI").format(1073741824));
     *       1Gi
     *
     *       >System.out.println(new MemorySizeFormatter("B0TD3FI").format(1073741824));
     *       1Gi
     *
     *       >System.out.println(new MemorySizeFormatter("0I0BEF0").format(1073741824));
     *       0Ei   0Pi   0Ti   1Gi   0Mi   0Ki
     *
     *       >System.out.println(new MemorySizeFormatter("I0BEF").format(1073741824));
     *       1Gi
     *
     *       >System.out.println(new MemorySizeFormatter("IBEF").format(1073741824));
     *       1Gi
     *
     *       >System.out.println(new MemorySizeFormatter("0IBTF").format(1073741824));
     *       0Ti   1Gi
     *
     *
     * </pre>
     *
     * @param format size format
     */
    public MemorySizeFormatter(String format) {
        leadingZeros = false;
        intermediateZeros = false;
        trailingZeros = false;
        char low = '\0';
        char high = '\0';
        if (format != null) {
            boolean startInterval = true;
            char[] charArray = format.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                char c = Character.toUpperCase(charArray[i]);
                switch (c) {
                    case 'B':
                    case 'K':
                    case 'M':
                    case 'G':
                    case 'T':
                    case 'P':
                    case 'E':
                    case 'Z':
                    case 'Y':
                        {
                        if (startInterval) {
                            startInterval = false;
                            low = c;
                        } else {
                            high = c;
                        }
                        break;
                    }
                    case 'I': {
                        binaryPrefix = true;
                        break;
                    }
                    case 'D': {
                        i++;
                        if(Character.isDigit(charArray[i])) {
                            depth = charArray[i] - '0';
                        }else{
                            depth=-1;
                        }
                        if(depth<=0 || depth>9){
                            throw new IllegalArgumentException("Invalid depth "+depth);
                        }
                    }
                    case 'F': {
                        fixedLength = true;
                        break;
                    }
                    case '0': {
                        if (i == 0) {
                            leadingZeros = true;
                        } else if(i==charArray.length-1){
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
        if (low == '\0') {
            low = 'B';
        }
        if (high == '\0') {
            high = 'T';
        }
        this.low = eval(low);
        this.high = eval(high);
        if (this.high < this.low) {
            long t = this.low;
            this.low = this.high;
            this.high = t;
        }
    }

    public static String sformatLeft(Object number, int size) {
        StringBuilder sb = new StringBuilder();
        sb.append(number);
        while (sb.length() < size) {
            sb.insert(0, ' ');
        }
        return sb.toString();
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

    private String formatLeft(Object number, int size) {
        if (fixedLength) {
            return sformatLeft(number, size);
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
//        long Z = eval('Z');
//        long Y = eval('Y');
        if (low <= E) {
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
            if (high >= E) {
                r = v / T;
                if ((leadingZeros && leading) || r > 0 || (!empty && intermediateZeros && ((v % E)>0)) || (v==0 && trailingZeros)) {
                    if (currDepth < 0) {
                        currDepth = 1;
                    } else {
                        currDepth++;
                    }
                    if (currDepth <= depth) {
                        if (sb.length() > 0) {
                            sb.append(" ");
                        }
                        sb.append(formatLeft(r, 3)).append("E").append(binaryPrefix?"i":"");
                        if(r!=0){
                            leading=false;
                        }
                        v = v % E;
                        empty = false;
                    }
                }
            }
            if (high >= P) {
                r = v / T;
                if ((leadingZeros && leading) || r > 0 || (!empty && intermediateZeros && ((v % P)>0)) || (v==0 && trailingZeros)) {
                    if (currDepth < 0) {
                        currDepth = 1;
                    } else {
                        currDepth++;
                    }
                    if (currDepth <= depth) {
                        if (sb.length() > 0) {
                            sb.append(" ");
                        }
                        sb.append(formatLeft(r, 3)).append("P").append(binaryPrefix?"i":"");
                        if(r!=0){
                            leading=false;
                        }
                        v = v % P;
                        empty = false;
                    }
                }
            }
            if (high >= T) {
                r = v / T;
                if ((leadingZeros && leading) || r > 0 || (!empty && intermediateZeros && ((v % T)>0)) || (v==0 && trailingZeros)) {
                    if (currDepth < 0) {
                        currDepth = 1;
                    } else {
                        currDepth++;
                    }
                    if (currDepth <= depth) {
                        if (sb.length() > 0) {
                            sb.append(" ");
                        }
                        sb.append(formatLeft(r, 3)).append("T").append(binaryPrefix?"i":"");
                        if(r!=0){
                            leading=false;
                        }
                        v = v % T;
                        empty = false;
                    }
                }
            }
            if (low <= G) {
                if (high >= G) {
                    r = v / G;
                }
                if ((leadingZeros && leading) || r > 0 || (!empty && intermediateZeros && ((v % G)>0)) || (v==0 && trailingZeros)) {
                    if (currDepth < 0) {
                        currDepth = 1;
                    } else {
                        currDepth++;
                    }
                    if (currDepth <= depth) {
                        if (sb.length() > 0) {
                            sb.append(" ");
                        }
                        if(r!=0){
                            leading=false;
                        }
                        sb.append(formatLeft(r, 3)).append("G").append(binaryPrefix?"i":"");
                        v = v % G;
                        empty = false;
                    }
                }
                if (low <= M) {
                    if (high >= M) {
                        r = v / M;
                        if ((leadingZeros && leading) || r > 0 || (!empty && intermediateZeros && ((v % M)>0)) || (v==0 && trailingZeros)) {
                            if(currDepth<0){
                                currDepth=1;
                            }else{
                                currDepth++;
                            }
                            if(currDepth<=depth) {
                                if (sb.length() > 0) {
                                    sb.append(" ");
                                }
                                if(r!=0){
                                    leading=false;
                                }
                                sb.append(formatLeft(r, 3)).append("M").append(binaryPrefix?"i":"");
                                v = v % M;
                                empty = false;
                            }
                        }
                    }
                    if (low <= K) {
                        if (high >= K) {
                            r = v / K;
                            if ((leadingZeros && leading) || r > 0 || (!empty && intermediateZeros && ((v % K)>0)) || (v==0 && trailingZeros)) {
                                if(currDepth<0){
                                    currDepth=1;
                                }else{
                                    currDepth++;
                                }
                                if(currDepth<=depth) {
                                    if (sb.length() > 0) {
                                        sb.append(" ");
                                    }
                                    if(r!=0){
                                        leading=false;
                                    }
                                    sb.append(formatLeft(r, 3)).append("K").append(binaryPrefix?"i":"");
                                    v = v % K;
                                    empty = false;
                                }
                            }
                        }
                        if (low <= 1) {
                            if ((leadingZeros && leading) || v > 0 || sb.length() == 0 /*|| (!empty && intermediateZeros)*/) {
                                if(currDepth<0){
                                    currDepth=1;
                                }else{
                                    currDepth++;
                                }
                                if(currDepth<=depth) {
                                    if (sb.length() > 0) {
                                        sb.append(" ");
                                    }
                                    if(r!=0){
                                        leading=false;
                                    }
                                    sb.append(formatLeft(v, 3)).append("B");
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
                sb.append(formatLeft(0, 3)).append("T");
            } else if (low >= G) {
                sb.append(formatLeft(0, 3)).append("G");
            } else if (low >= M) {
                sb.append(formatLeft(0, 3)).append("M");
            } else if (low >= K) {
                sb.append(formatLeft(0, 3)).append("K");
                sb.append(formatLeft(0, 3)).append("B");
            }
        } else {
            if (neg) {
                sb.insert(0, "-");
            }
        }
        return sb.toString();
    }


}

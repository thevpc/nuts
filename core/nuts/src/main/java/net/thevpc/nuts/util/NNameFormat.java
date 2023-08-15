package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NMapBy;

import java.util.ArrayList;
import java.util.List;

public class NNameFormat {
    public static final NNameFormat LOWER_CAMEL_CASE = new NNameFormat(NWordFormat.UNCAPITALIZED, NWordFormat.CAPITALIZED, null);
    public static final NNameFormat UPPER_CAMEL_CASE = new NNameFormat(NWordFormat.CAPITALIZED, NWordFormat.CAPITALIZED, null);
    public static final NNameFormat CAMEL_CASE = UPPER_CAMEL_CASE;
    public static final NNameFormat LOWER_KEBAB_CASE = new NNameFormat(NWordFormat.LOWERCASE, NWordFormat.LOWERCASE, "-");
    public static final NNameFormat UPPER_KEBAB_CASE = new NNameFormat(NWordFormat.UPPERCASE, NWordFormat.UPPERCASE, "-");
    public static final NNameFormat KEBAB_CASE = LOWER_KEBAB_CASE;
    public static final NNameFormat LOWER_SNAKE_CASE = new NNameFormat(NWordFormat.LOWERCASE, NWordFormat.LOWERCASE, "_");
    public static final NNameFormat UPPER_SNAKE_CASE = new NNameFormat(NWordFormat.UPPERCASE, NWordFormat.UPPERCASE, "_");
    public static final NNameFormat SNAKE_CASE = LOWER_SNAKE_CASE;

    public static final NNameFormat LOWER_SPACE_CASE = new NNameFormat(NWordFormat.LOWERCASE, NWordFormat.LOWERCASE, " ");
    public static final NNameFormat UPPER_SPACE_CASE = new NNameFormat(NWordFormat.UPPERCASE, NWordFormat.UPPERCASE, " ");
    public static final NNameFormat SPACE_CASE = LOWER_SPACE_CASE;

    public static final NNameFormat UPPER_TITLE_CASE = new NNameFormat(NWordFormat.CAPITALIZED, NWordFormat.CAPITALIZED, " ");
    public static final NNameFormat LOWER_TITLE_CASE = new NNameFormat(NWordFormat.CAPITALIZED, NWordFormat.UNCAPITALIZED, " ");
    public static final NNameFormat TITLE_CASE = UPPER_TITLE_CASE;

    public static final NNameFormat TITLE_NAME = TITLE_CASE;
    public static final NNameFormat ID_NAME = LOWER_KEBAB_CASE;
    public static final NNameFormat CONST_NAME = UPPER_SNAKE_CASE;
    public static final NNameFormat CLASS_NAME = UPPER_CAMEL_CASE;
    public static final NNameFormat VAR_NAME = LOWER_CAMEL_CASE;

    private NWordFormat leading;
    private NWordFormat next;
    private String sep;

    @NMapBy
    public NNameFormat(
            @NMapBy(name = "leading") NWordFormat leading,
            @NMapBy(name = "next") NWordFormat next,
            @NMapBy(name = "sep") String sep) {
        NAssert.requireNonNull(leading, "leading");
        NAssert.requireNonNull(next, "next");
        this.leading = leading;
        this.next = next;
        this.sep = sep;
    }

    /**
     * true if a and b have equivalent (cas ignored) parts.
     *
     * @param a first string
     * @param b second string
     * @return true if a and b have equivalent (case ignored) parts.
     */
    public static boolean equalsIgnoreFormat(String a, String b) {
        String[] aa = parse(NStringUtils.trim(a));
        String[] bb = parse(NStringUtils.trim(b));
        int length = aa.length;
        if (bb.length != length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            String o1 = aa[i];
            String o2 = bb[i];
            if (!(o1 == null ? o2 == null : o1.equalsIgnoreCase(o2)))
                return false;
        }
        return true;
    }

    public NWordFormat getLeading() {
        return leading;
    }

    public NWordFormat getNext() {
        return next;
    }

    public String getSep() {
        return sep;
    }


    public static String[] parse(CharSequence value) {
        return parse(value, false);
    }

    public static String[] parse(CharSequence value, boolean sep) {
        if (value == null) {
            return new String[]{""};
        }
        return parse(value.toString(), sep);
    }

    public static String[] parse(String value) {
        return parse(value, false);
    }

    public static boolean isSeparator(char c) {
        int t = Character.getType(c);
        return isSeparator(c,t);
    }
    private static boolean isSeparator(char c,int codeType) {
        switch (c) {
            case '-':
            case '_':
            case ' ':
            case '.':
            case ':':
            case '/':
            case '\\':
            case ',':
            case ';':
                return true;
            default:{
                if(c<=32){
                    return true;
                }
            }
        }
        switch (codeType) {
            case Character.DASH_PUNCTUATION:
            case Character.CONNECTOR_PUNCTUATION:
            case Character.START_PUNCTUATION:
            case Character.END_PUNCTUATION:
            case Character.FINAL_QUOTE_PUNCTUATION:
            case Character.INITIAL_QUOTE_PUNCTUATION:
            case Character.SPACE_SEPARATOR:
            case Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR:
            case Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR:
            case Character.DIRECTIONALITY_PARAGRAPH_SEPARATOR:
            case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
            case Character.CONTROL:
            case Character.OTHER_PUNCTUATION:{
                return true;
            }
        }
        return false;
    }

    public static String[] parse(String value, boolean sep) {
        if (NBlankable.isBlank(value)) {
            return new String[]{""};
        }
        String aValue = value.trim();
        List<String> all = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        final int wasSep = 1;
        final int wasUpper = 2;
        final int wasLower = 3;
        final int wasOther = 4;
        int was = wasSep;
        for (char c : aValue.toCharArray()) {
            int t = Character.getType(c);
            if (isSeparator(c,t)) {
                if (sb.length() > 0) {
                    all.add(sb.toString());
                    sb.setLength(0);
                }
                if (sep) {
                    all.add(String.valueOf(c));
                }
                was = wasSep;
            } else {
                switch (t) {
                    case Character.UPPERCASE_LETTER: {
                        if (was == wasLower) {
                            if (sb.length() > 0) {
                                all.add(sb.toString());
                                sb.setLength(0);
                            }
                        }
                        sb.append(c);
                        was = wasUpper;
                        break;
                    }
                    case Character.LOWERCASE_LETTER: {
                        sb.append(c);
                        was = wasLower;
                        break;
                    }
                    default: {
                        was = wasOther;
                        sb.append(c);
                    }
                }
            }
        }
        if (sb.length() > 0) {
            all.add(sb.toString());
            sb.setLength(0);
        }
        return all.toArray(new String[0]);
    }

    public boolean equals(String a, String b) {
        return format(a).equals(format(b));
    }

    public String format(String value) {
        return format(parse(value));
    }

    public String format(CharSequence value) {
        return format(parse(value));
    }

    public String format(String[] value) {
        StringBuilder sb = new StringBuilder();
        if (value.length > 0) {
            sb.append(leading.formatWord(value[0]));
            for (int i = 1; i < value.length; i++) {
                if (sep != null) {
                    sb.append(sep);
                }
                sb.append(next.formatWord(value[i]));
            }
        }
        return sb.toString();
    }
}

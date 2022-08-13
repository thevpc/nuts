package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.elem.NutsMapBy;

import java.util.ArrayList;
import java.util.List;

public class NutsNameFormat {
    public static final NutsNameFormat LOWER_CAMEL_CASE = new NutsNameFormat(NutsWordFormat.UNCAPITALIZED, NutsWordFormat.CAPITALIZED, null);
    public static final NutsNameFormat UPPER_CAMEL_CASE = new NutsNameFormat(NutsWordFormat.CAPITALIZED, NutsWordFormat.CAPITALIZED, null);
    public static final NutsNameFormat CAMEL_CASE = UPPER_CAMEL_CASE;
    public static final NutsNameFormat LOWER_KEBAB_CASE = new NutsNameFormat(NutsWordFormat.LOWERCASE, NutsWordFormat.LOWERCASE, "-");
    public static final NutsNameFormat UPPER_KEBAB_CASE = new NutsNameFormat(NutsWordFormat.UPPERCASE, NutsWordFormat.UPPERCASE, "-");
    public static final NutsNameFormat KEBAB_CASE = LOWER_KEBAB_CASE;
    public static final NutsNameFormat LOWER_SNAKE_CASE = new NutsNameFormat(NutsWordFormat.LOWERCASE, NutsWordFormat.LOWERCASE, "_");
    public static final NutsNameFormat UPPER_SNAKE_CASE = new NutsNameFormat(NutsWordFormat.UPPERCASE, NutsWordFormat.UPPERCASE, "_");
    public static final NutsNameFormat SNAKE_CASE = LOWER_SNAKE_CASE;

    public static final NutsNameFormat LOWER_SPACE_CASE = new NutsNameFormat(NutsWordFormat.LOWERCASE, NutsWordFormat.LOWERCASE, " ");
    public static final NutsNameFormat UPPER_SPACE_CASE = new NutsNameFormat(NutsWordFormat.UPPERCASE, NutsWordFormat.UPPERCASE, " ");
    public static final NutsNameFormat SPACE_CASE = LOWER_SPACE_CASE;

    public static final NutsNameFormat UPPER_TITLE_CASE = new NutsNameFormat(NutsWordFormat.CAPITALIZED, NutsWordFormat.CAPITALIZED, " ");
    public static final NutsNameFormat LOWER_TITLE_CASE = new NutsNameFormat(NutsWordFormat.CAPITALIZED, NutsWordFormat.UNCAPITALIZED, " ");
    public static final NutsNameFormat TITLE_CASE = UPPER_TITLE_CASE;

    public static final NutsNameFormat TITLE_NAME = TITLE_CASE;
    public static final NutsNameFormat ID_NAME = LOWER_KEBAB_CASE;
    public static final NutsNameFormat CONST_NAME = UPPER_SNAKE_CASE;
    public static final NutsNameFormat CLASS_NAME = UPPER_CAMEL_CASE;
    public static final NutsNameFormat VAR_NAME = LOWER_CAMEL_CASE;

    private NutsWordFormat leading;
    private NutsWordFormat next;
    private String sep;

    @NutsMapBy
    public NutsNameFormat(
            @NutsMapBy(name = "leading") NutsWordFormat leading,
            @NutsMapBy(name = "next") NutsWordFormat next,
            @NutsMapBy(name = "sep") String sep) {
        NutsUtils.requireNonNull(leading, "leading");
        NutsUtils.requireNonNull(next, "next");
        this.leading = leading;
        this.next = next;
        this.sep = sep;
    }

    public NutsWordFormat getLeading() {
        return leading;
    }

    public NutsWordFormat getNext() {
        return next;
    }

    public String getSep() {
        return sep;
    }


    public static String[] parse(String value) {
        if (NutsBlankable.isBlank(value)) {
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
            switch (c) {
                case '-':
                case '_':
                case ' ': {
                    if (sb.length() > 0) {
                        all.add(sb.toString());
                        sb.setLength(0);
                    }
                    was = wasSep;
                    break;
                }
                default: {
                    int t = Character.getType(c);
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
                        case Character.OTHER_PUNCTUATION: {
                            if (sb.length() > 0) {
                                all.add(sb.toString());
                                sb.setLength(0);
                            }
                            was = wasSep;
                            break;
                        }
                        default: {
                            was = wasOther;
                            sb.append(c);
                        }
                    }
                    break;
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

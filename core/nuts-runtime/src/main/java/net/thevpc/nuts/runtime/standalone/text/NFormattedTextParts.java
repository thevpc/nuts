package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.util.NLiteral;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UnknownFormatConversionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NFormattedTextParts {
    private static Pattern CFORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])");
    private List<NFormattedTextPart> parts;
    private List<String> formats;
    private String type;

    public NFormattedTextParts(String type, List<NFormattedTextPart> parts) {
        this.type = type;
        this.parts = new ArrayList<>(parts);
    }

    public NFormattedTextPart[] getParts() {
        return parts.toArray(new NFormattedTextPart[0]);
    }

    public static NFormattedTextParts parseJStyle(String msg) {
        if (msg == null) {
            return new NFormattedTextParts("jformat", Collections.emptyList());
        }
        List<NFormattedTextPart> al = new ArrayList<>();
        int length = msg.length();
        char[] chars = msg.toCharArray();
        boolean inText = true;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (inText) {
                if (chars[i] == '{') {
                    if (sb.length() > 0) {
                        al.add(new NFormattedTextPart(false, sb.toString()));
                        sb.setLength(0);
                    }
                    inText = false;
                } else if (chars[i] == '\\') {
                    i++;
                    sb.append(chars[i]);
                } else {
                    sb.append(chars[i]);
                }
            } else {
                if (chars[i] == '}') {
                    if (sb.length() > 0) {
                        al.add(new NFormattedTextPart(true, sb.toString()));
                        sb.setLength(0);
                    }
                    inText = true;
                } else if (chars[i] == '\\') {
                    i++;
                    sb.append(chars[i]);
                } else {
                    sb.append(chars[i]);
                }
            }
        }
        if (sb.length() > 0) {
            al.add(new NFormattedTextPart(!inText, sb.toString()));
            sb.setLength(0);
        }
        return new NFormattedTextParts("jformat", al);
    }

    public static NFormattedTextParts parseCFormat(String msg) {
        if (msg == null) {
            return new NFormattedTextParts("cformat", Collections.emptyList());
        }
        List<NFormattedTextPart> al = new ArrayList<>();
        Matcher m = CFORMAT_PATTERN.matcher(msg);
        int length = msg.length();
        for (int i = 0; i < length; ) {
            if (m.find(i)) {
                if (m.start() != i) {
                    checkCFormatText(msg, i, m.start());
                    al.add(new NFormattedTextPart(false, msg.substring(i, m.start())));
                }

                al.add(new NFormattedTextPart(true, m.group()));
                i = m.end();
            } else {
                checkCFormatText(msg, i, length);
                al.add(new NFormattedTextPart(false, msg.substring(i)));
                break;
            }
        }
        return new NFormattedTextParts("cformat", al);
    }

    private static void checkCFormatText(String s, int start, int end) {
        for (int i = start; i < end; i++) {
            // Any '%' found in the region starts an invalid format specifier.
            if (s.charAt(i) == '%') {
                char c = (i == end - 1) ? '%' : s.charAt(i + 1);
                throw new UnknownFormatConversionException(String.valueOf(c)+" in "+s);
            }
        }
    }

    public String getFormatAt(int index) {
        String[] formats1 = getFormats();
        if (index >= 0 && index < formats1.length) {
            return formats1[index];
        }
        return null;
    }

    public String getFormatFor(int index) {
        String[] formats1 = getFormats();
        switch (type) {
            case "jformat": {
                int i = 0;
                for (String s : formats1) {
                    if (s.isEmpty()) {
                        if (i == index) {
                            return s;
                        }
                        int c = s.indexOf(':');
                        String d = s;
                        if (c >= 0) {
                            d = s.substring(c + 1);
                        }
                        NLiteral dl = NLiteral.of(d);
                        if (dl.asInt().isPresent()) {
                            if (dl.asInt().get() == index) {
                                return s;
                            }
                        }
                    }
                    i++;
                }
                return null;
            }
            case "cformat": {
                return getFormatAt(index);
            }
        }
        return null;
    }

    public String[] getFormats() {
        if (formats == null) {
            formats = parts.stream().filter(NFormattedTextPart::isFormat).map(NFormattedTextPart::getValue).collect(Collectors.toList());
        }
        return formats.toArray(new String[0]);
    }


}

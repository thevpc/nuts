package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.parser;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements.*;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class TsonParserUtils {


    public static TsonElement parseDateTimeElem(String s) {
        return new TsonLocalDateTimeImpl(Instant.parse(s).atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    public static TsonElement parseDateElem(String s) {
        return new TsonLocalDateImpl(LocalDate.parse(s));
    }

    public static TsonElement parseTimeElem(String s) {
        return new TsonLocalTimeImpl(LocalTime.parse(s));
    }

    public static TsonElement parseRegexElem(String s) {
        final String p = s.substring(1, s.length() - 1);
        //should unescape
        return new TsonRegexImpl(Pattern.compile(p));
    }


    public static TsonElement parseNumber(String s) {
        return TsonNumberHelper.parse(s).toTson();
    }

    public static Instant parseDateTime(String s) {
        return Instant.parse(s);
    }

    public static LocalDate parseDate(String s) {
        return LocalDate.parse(s);
    }

    public static LocalTime parseTime(String s) {
        return LocalTime.parse(s);
    }

    public static Pattern parseRegex(String s) {
        return Pattern.compile(s.substring(1, s.length() - 1));
    }

    public static byte parseByte(String s) {
        if (s.endsWith("b") || s.endsWith("B")) {
            s = s.substring(0, s.length() - 1);
        }
        return Byte.parseByte(s);
    }

    public static short parseShort(String s) {
        if (s.endsWith("s") || s.endsWith("S")) {
            s = s.substring(0, s.length() - 1);
        }
        return Short.parseShort(s);
    }

    public static long parseLong(String s) {
        if (s.endsWith("l") || s.endsWith("L")) {
            s = s.substring(0, s.length() - 1);
        }
        return Long.parseLong(s);
    }

    public static int parseInt(String s) {
        if (s.endsWith("i") || s.endsWith("I")) {
            s = s.substring(0, s.length() - 1);
        }
        return Integer.parseInt(s);
    }

    public static float parseFloat(String s) {
        if (s.endsWith("f") || s.endsWith("F")) {
            s = s.substring(0, s.length() - 1);
        }
        return Float.parseFloat(s);
    }

    public static double parseDouble(String s) {
//        if(s.endsWith("f") || s.endsWith("F")){
//            s=s.substring(s.length()-1);
//        }
        return Double.parseDouble(s);
    }

    public static char parseChar(String s) {
        return parseRawString(s, TsonElementType.SINGLE_QUOTED_STRING).charAt(0);
    }

//    public static TsonElement parseCharElem(String s) {
//        if (s.length() != 1) {
//            return parseStringElem(s, TsonStringLayout.SINGLE_QUOTE);
//        }
//        return new TsonCharImpl(parseRawString(s, TsonStringLayout.SINGLE_QUOTE).charAt(0));
//    }
//
//    public static TsonElement parseStringElem(String s) {
//        return parseStringElem(s, TsonStringLayout.DOUBLE_QUOTE);
//    }

//    public static TsonElement parseStringElem(String s, TsonStringLayout layout) {
//        layout = layout == null ? TsonStringLayout.DOUBLE_QUOTE : layout;
//        return Tson.parseString(s, layout);
//    }

//    public static TsonElement parseAliasElem(String s) {
//        return new TsonAliasImpl(s.substring(1));
//    }

//    public static void main(String[] args) {
//        for (String string : new String[]{"\"Hello \\nWorld\"","\"Hello World\""}) {
//            System.out.println(string);
//            System.out.println(parseString(string));
//            System.out.println(parseString2(string));
//        }
//        final int count = 10000;
//
//        Chrono c2=Chrono.start();
//        for (int i = 0; i < count; i++) {
//            for (String string : new String[]{"\"Hello \\nWorld\""}) {
//                parseString(string);
//            }
//        }
//        c2.stop();
//        
//        Chrono c3=Chrono.start();
//        for (int i = 0; i < count; i++) {
//            for (String string : new String[]{"\"Hello \\nWorld\""}) {
//                parseString2(string);
//            }
//        }
//        c3.stop();
//        System.out.println(c2);
//        System.out.println(c3);
//    }


//    public static String extractRawString(String s, TsonStringLayout layout) {
//        char[] chars = s.toCharArray();
//        int len = chars.length;
//        int borderLen;
//        switch (layout) {
//            case DOUBLE_QUOTE:
//            case SINGLE_QUOTE:
//            case ANTI_QUOTE: {
//                borderLen = 1;
//                break;
//            }
//            case TRIPLE_ANTI_QUOTE:
//            case TRIPLE_DOUBLE_QUOTE:
//            case TRIPLE_SINGLE_QUOTE: {
//                borderLen = 3;
//                break;
//            }
//            default: {
//                throw new IllegalArgumentException("unsupported");
//            }
//        }
//        return s.substring(borderLen, len - borderLen);
//    }
//
//
    public static String parseRawString(String s, TsonElementType layout) {
        char[] chars = s.toCharArray();
        int len = chars.length;
        int prefixLen = 0;
        int suffixLen = 0;
        String sborder = "";
        String eborder = "";
        switch (layout) {
            case DOUBLE_QUOTED_STRING: {
                sborder = "\"";
                eborder = "\"";
                break;
            }
            case SINGLE_QUOTED_STRING: {
                sborder = "'";
                eborder = "'";
                break;
            }
            case ANTI_QUOTED_STRING: {
                sborder = "`";
                eborder = "`";
                break;
            }
            case TRIPLE_ANTI_QUOTED_STRING: {
                sborder = "```";
                eborder = "```";
                break;
            }
            case TRIPLE_DOUBLE_QUOTED_STRING: {
                sborder = "\"\"\"";
                eborder = "\"\"\"";
                break;
            }
            case TRIPLE_SINGLE_QUOTED_STRING: {
                sborder = "'''";
                eborder = "'''";
                break;
            }
            case LINE_STRING:{
                sborder = "¶";
                eborder = "";
            }
        }
        prefixLen = sborder.length();
        suffixLen = eborder.length();
        if (s.length() < prefixLen + suffixLen) {
            throw new IllegalArgumentException("unsupported: " + s);
        }
        if (
                !s.startsWith(sborder)
                        || !s.endsWith(eborder)
        ) {
            throw new IllegalArgumentException("unsupported: " + s);
        }
        switch (layout) {
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING: {
                final int beforeLen = len - suffixLen;
                StringBuilder sb = new StringBuilder();
                for (int i = suffixLen; i < beforeLen; i++) {
                    char c = s.charAt(i);
                    switch (c) {
                        case '\\': {
                            int ip = i + 1;
                            boolean processed = false;
                            if (ip < beforeLen) {
                                switch (s.charAt(ip)) {
                                    case 'n': {
                                        sb.append('\n');
                                        i++;
                                        processed = true;
                                        break;
                                    }
                                    case 't': {
                                        sb.append('\t');
                                        i++;
                                        processed = true;
                                        break;
                                    }
                                    case 'f': {
                                        sb.append('\f');
                                        i++;
                                        processed = true;
                                        break;
                                    }
                                    case 'b': {
                                        sb.append('\b');
                                        i++;
                                        processed = true;
                                        break;
                                    }
                                    case '\\': {
                                        sb.append('\\');
                                        i++;
                                        processed = true;
                                        break;
                                    }
                                }
                            }
                            if (!processed) {
                                sb.append(c);
                            }
                            break;
                        }
                        default: {
                            sb.append(c);
                        }
                    }
                }
                return sb.toString();
            }
            case TRIPLE_ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING: {
                final int beforeLen = len - prefixLen;
                StringBuilder sb = new StringBuilder(s.length());
                for (int i = prefixLen; i < beforeLen; i++) {
                    char c = s.charAt(i);
                    switch (c) {
                        case '\\': {
                            boolean processed = false;
                            if (i + 3 < len) {
                                String substring = s.substring(i + 1, i + 1 + suffixLen);
                                if (substring.equals(sborder)) {
                                    sb.append(substring);
                                    i += suffixLen;
                                    processed = true;
                                }
                            }
                            if (!processed) {
                                sb.append(c);
                            }
                            break;
                        }
                        default: {
                            sb.append(c);
                        }
                    }
                }
                return sb.toString();
            }
            case LINE_STRING:{
                return s.substring(1).trim();
            }
        }
        throw new IllegalArgumentException("unsupported: " + s);
    }

    public static TsonStringImpl parseRawString(String s) {
        char[] chars = s.toCharArray();
        int len = chars.length;
        int prefixLen = 1;
        int suffixLen = 1;
        if (len > 0 && chars[0] == '¶') {
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '\n' || chars[i] == '\r') {
                    throw new IllegalArgumentException("invalid raw line string " + s);
                }
            }
            return new TsonStringImpl(
                    TsonElementType.LINE_STRING,
                    new String(Arrays.copyOfRange(chars, 1, chars.length)).trim(),
                    new String(Arrays.copyOfRange(chars, 1, chars.length))
            );
        }
        if (len < 2) {
            throw new IllegalArgumentException("invalid raw string " + s);
        }
        if (chars[0] != chars[len - 1]) {
            throw new IllegalArgumentException("unbalanced string " + s);
        }

        String border = null;
        TsonElementType layout = null;
        switch (chars[0]) {
            case '\"': {
                border = "\"";
                layout = TsonElementType.DOUBLE_QUOTED_STRING;
                break;
            }
            case '\'': {
                border = "'";
                layout = TsonElementType.SINGLE_QUOTED_STRING;
                break;
            }
            case '`': {
                border = "`";
                layout = TsonElementType.ANTI_QUOTED_STRING;
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid string boundaries" + s);
            }
        }
        if (len >= 6 &&
                chars[0] == chars[1] && chars[1] == chars[2]
                && chars[len - 1] == chars[len - 2] && chars[len - 2] == chars[len - 3]
        ) {
            prefixLen = 3;
            suffixLen = 3;
            switch (layout) {
                case DOUBLE_QUOTED_STRING: {
                    border = "\"\"\"";
                    layout = TsonElementType.TRIPLE_DOUBLE_QUOTED_STRING;
                    break;
                }
                case SINGLE_QUOTED_STRING: {
                    border = "'''";
                    layout = TsonElementType.TRIPLE_SINGLE_QUOTED_STRING;
                    break;
                }
                case ANTI_QUOTED_STRING: {
                    border = "```";
                    layout = TsonElementType.TRIPLE_ANTI_QUOTED_STRING;
                    break;
                }
            }
        }
        switch (layout) {
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING: {
                final int beforeLen = len - suffixLen;
                StringBuilder sb = new StringBuilder();
                for (int i = suffixLen; i < beforeLen; i++) {
                    char c = s.charAt(i);
                    switch (c) {
                        case '\\': {
                            int ip = i + 1;
                            boolean processed = false;
                            if (ip < beforeLen) {
                                char c2 = s.charAt(ip);
                                switch (c2) {
                                    case '\\': {
                                        sb.append('\\');
                                        i++;
                                        processed = true;
                                        break;
                                    }
                                    case '\'': {
                                        if (layout == TsonElementType.SINGLE_QUOTED_STRING) {
                                            sb.append(c2);
                                            i++;
                                            processed = true;
                                        }
                                        break;
                                    }
                                    case '`': {
                                        if (layout == TsonElementType.ANTI_QUOTED_STRING) {
                                            sb.append(c2);
                                            i++;
                                            processed = true;
                                        }
                                        break;
                                    }
                                    case '\"': {
                                        if (layout == TsonElementType.DOUBLE_QUOTED_STRING) {
                                            sb.append(c2);
                                            i++;
                                            processed = true;
                                        }
                                        break;
                                    }
                                }
                            }
                            if (!processed) {
                                sb.append(c);
                            }
                            break;
                        }
                        case '\'': {
                            if (layout == TsonElementType.SINGLE_QUOTED_STRING) {
                                sb.append('\\');
                                sb.append(c);
                            } else {
                                sb.append(c);
                            }
                            break;
                        }
                        case '`': {
                            if (layout == TsonElementType.ANTI_QUOTED_STRING) {
                                sb.append('\\');
                                sb.append(c);
                                i++;
                            }
                            break;
                        }
                        case '\"': {
                            if (layout == TsonElementType.DOUBLE_QUOTED_STRING) {
                                sb.append('\\');
                                sb.append(c);
                            } else {
                                sb.append(c);
                            }
                            break;
                        }
                        default: {
                            sb.append(c);
                        }
                    }
                }
                return new TsonStringImpl(
                        layout,
                        sb.toString(),
                        s.substring(prefixLen, len - prefixLen)
                );
            }
            case TRIPLE_ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING: {
                final int beforeLen = len - prefixLen;
                StringBuilder sb = new StringBuilder(s.length());
                for (int i = prefixLen; i < beforeLen; i++) {
                    char c = s.charAt(i);
                    switch (c) {
                        case '\\': {
                            boolean processed = false;
                            if (i + 3 < len) {
                                String substring = s.substring(i + 1, i + 1 + suffixLen);
                                if (substring.equals(border)) {
                                    sb.append(substring);
                                    i += suffixLen;
                                    processed = true;
                                }
                            }
                            if (!processed) {
                                sb.append(c);
                            }
                            break;
                        }
                        default: {
                            sb.append(c);
                        }
                    }
                }
                return new TsonStringImpl(
                        layout,
                        sb.toString(),
                        s.substring(prefixLen, len - prefixLen)
                );
            }
        }
        throw new IllegalArgumentException("unsupported: " + s);
    }

    public static String parseStringOld(String s) {
        char[] chars = s.toCharArray();
        int len = chars.length;
        StringBuilder sb = new StringBuilder(len - 2);
        final int beforeLen = len - 1;
        for (int i = 1; i < beforeLen; i++) {
            switch (s.charAt(i)) {
                case '\\': {
                    i++;
                    switch (s.charAt(i)) {
                        case 'n': {
                            sb.append('\n');
                            break;
                        }
                        case 't': {
                            sb.append('\t');
                            break;
                        }
                        case 'f': {
                            sb.append('\f');
                            break;
                        }
                        case 'b': {
                            sb.append('\b');
                            break;
                        }
                        case '\\': {
                            sb.append('\\');
                            break;
                        }
                        default: {
                            sb.append(chars[i]);
                        }
                    }
                    break;
                }
                default: {
                    sb.append(chars[i]);
                }
            }
        }
        return sb.toString();
    }

    public static TsonDocument elementsToDocument(TsonElement[] roots) {
        TsonElement c = null;
        if (roots.length == 0) {
            return Tson.ofDocument().header(null).content(Tson.ofObjectBuilder().build()).build();
        } else if (roots.length == 1) {
            return elementToDocument(roots[0]);
        } else {
            List<TsonAnnotation> annotations = roots[0].annotations();
            if (annotations != null && annotations.size() > 0 && "tson".equals(annotations.get(0).name())) {
                // will remove it
                ArrayList<TsonAnnotation> newAnn = new ArrayList<>(annotations);
                newAnn.remove(0);
                List<TsonElement> newList = new ArrayList<>(Arrays.asList(roots));
                TsonElement c0 = roots[0].builder().setAnnotations(newAnn.toArray(new TsonAnnotation[0])).build();
                newList.set(0, c0);
                roots = newList.toArray(new TsonElement[0]);
            }
            return Tson.ofDocument().content(Tson.ofObjectBuilder(roots).build()).build();
        }
    }

    public static TsonDocument elementToDocument(TsonElement root) {
        List<TsonAnnotation> annotations = root.annotations();
        if (annotations != null && annotations.size() > 0 && "tson".equals(annotations.get(0).name())) {
            // will remove it
            ArrayList<TsonAnnotation> newAnn = new ArrayList<>(annotations);
            newAnn.remove(0);
            return Tson.ofDocument().header(Tson.ofDocumentHeader().addParams(annotations.get(0).params()).build())
                    .content(root.builder().setAnnotations(newAnn.toArray(new TsonAnnotation[0])).build()).build();
        }
        return Tson.ofDocument().header(null).content(root).build();
    }

    public static TsonComment parseComments(String c) {
        if (c == null) {
            return null;
        }
        if (c.startsWith("/*")) {
            return TsonComment.ofMultiLine(escapeMultiLineComments(c));
        }
        if (c.startsWith("//")) {
            return TsonComment.ofSingleLine(escapeSingleLineComments(c));
        }
        throw new IllegalArgumentException("unsupported comments " + c);
    }

    public static String escapeSingleLineComments(String c) {
        if (c == null) {
            return null;
        }
        if (c.startsWith("//")) {
            return c.substring(2);
        }
        throw new IllegalArgumentException("unsupported comments " + c);
    }

    public static String escapeMultiLineComments(String c) {
        if (c == null) {
            return null;
        }
        int line = 0;
        String[] lines = c.trim().split("\n");
        StringBuilder sb = new StringBuilder();
        for (String s : lines) {
            s = s.trim();
            if (line == 0) {
                if (s.startsWith("/*")) {
                    s = s.substring(2);
                }
            }
            if (line == lines.length - 1) {
                if (s.endsWith("*/")) {
                    s = s.substring(0, s.length() - 2);
                }
            }
            if (s.equals("*")) {
                s = s.substring(1);
            } else if (s.equals("**")) {
                s = s.substring(1);
            } else if (s.startsWith("*") && s.length() > 1 && Character.isWhitespace(s.charAt(1))) {
                s = s.substring(2).trim();
            } else if (s.startsWith("**") && s.length() > 2 && Character.isWhitespace(s.charAt(1))) {
                s = s.substring(2).trim();
            }
            if (s.length() > 1 && s.charAt(0) == '*' && s.charAt(1) == ' ') {
                s = s.substring(2);
            }
            s = s.trim();
            if (line == lines.length - 1) {
                if (s.length() > 0) {
                    if (line > 0) {
                        sb.append("\n");
                    }
                    sb.append(s.trim());
                }
            } else {
                if (line > 0) {
                    sb.append("\n");
                }
                sb.append(s.trim());
            }
            line++;
        }
        return sb.toString().trim();
    }


    private static int fastDecodeIntOctal(String nm) throws NumberFormatException {
        int index = 0;
        boolean negative = false;
        int result;
        char firstChar = nm.charAt(0);
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }
        //(nm.startsWith("0", index) && nm.length() > 1 + index)
        index++;
        try {
            result = Integer.valueOf(nm.substring(index), 8);
            if (negative) {
                return -result;
            }
            return result;
        } catch (NumberFormatException e) {
            String constant = negative ? ("-" + nm.substring(index))
                    : nm.substring(index);
            return Integer.parseInt(constant, 8);
        }
    }

    private static short fastDecodeShortOctal(String nm) throws NumberFormatException {
        int index = 0;
        boolean negative = false;
        short result;
        char firstChar = nm.charAt(0);
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }
        //(nm.startsWith("0", index) && nm.length() > 1 + index)
        index++;
        try {
            result = Short.parseShort(nm.substring(index, nm.length() - 1), 8);
            if (negative) {
                return (short) -result;
            }
            return result;
        } catch (NumberFormatException e) {
            String constant = negative ? ("-" + nm.substring(index, nm.length() - 1))
                    : nm.substring(index, nm.length() - 1);
            return Short.parseShort(constant, 8);
        }
    }

    private static long fastDecodeLongOctal(String nm) throws NumberFormatException {
        int index = 0;
        boolean negative = false;
        long result;
        char firstChar = nm.charAt(0);
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }
        //(nm.startsWith("0", index) && nm.length() > 1 + index)
        index++;
        try {
            result = Long.parseLong(nm.substring(index, nm.length() - 1), 8);
            if (negative) {
                return -result;
            }
            return result;
        } catch (NumberFormatException e) {
            String constant = negative ? ("-" + nm.substring(index, nm.length() - 1))
                    : nm.substring(index, nm.length() - 1);
            return Long.parseLong(constant, 8);
        }
    }

    private static BigInteger fastDecodeBigIntOctal(String nm) throws NumberFormatException {
        int index = 0;
        boolean negative = false;
        BigInteger result;
        char firstChar = nm.charAt(0);
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }
        //(nm.startsWith("0", index) && nm.length() > 1 + index)
        index++;
        try {
            result = new BigInteger(nm.substring(index, nm.length() - 1), 8);
            if (negative) {
                return result.negate();
            }
            return result;
        } catch (NumberFormatException e) {
            String constant = negative ? ("-" + nm.substring(index, nm.length() - 1))
                    : nm.substring(index, nm.length() - 1);
            return new BigInteger(constant, 8);
        }
    }


    private static int fastDecodeIntHex(String nm) throws NumberFormatException {
        int index = 0;
        boolean negative = false;
        int result;

        char firstChar = nm.charAt(0);
        // Handle sign, if present
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }

        //(nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
        index += 2;

        try {
            result = Integer.parseInt(nm.substring(index), 16);
            if (negative) {
                return -result;
            }
            return result;
        } catch (NumberFormatException e) {
            String constant = negative ? ("-" + nm.substring(index))
                    : nm.substring(index);
            result = Integer.parseInt(constant, 16);
        }
        return result;
    }

    private static int fastDecodeIntBin(String nm) throws NumberFormatException {
        int index = 0;
        boolean negative = false;
        int result;

        char firstChar = nm.charAt(0);
        // Handle sign, if present
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }

        //(nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
        index += 2;

        try {
            result = Integer.parseInt(nm.substring(index), 2);
            if (negative) {
                return -result;
            }
            return result;
        } catch (NumberFormatException e) {
            String constant = negative ? ("-" + nm.substring(index))
                    : nm.substring(index);
            result = Integer.parseInt(constant, 2);
        }
        return result;
    }

    private static short fastDecodeShortHex(String nm) throws NumberFormatException {
        int index = 0;
        boolean negative = false;
        short result;

        char firstChar = nm.charAt(0);
        // Handle sign, if present
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }

        //(nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
        index += 2;

        try {
            result = Short.parseShort(nm.substring(index), 16);
            if (negative) {
                return (short) -result;
            }
            return result;
        } catch (NumberFormatException e) {
            String constant = negative ? ("-" + nm.substring(index))
                    : nm.substring(index);
            result = Short.parseShort(constant, 16);
        }
        return result;
    }

    private static short fastDecodeShortBin(String nm) throws NumberFormatException {
        int index = 0;
        boolean negative = false;
        short result;

        char firstChar = nm.charAt(0);
        // Handle sign, if present
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }

        //(nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
        index += 2;

        try {
            result = Short.parseShort(nm.substring(index), 2);
            if (negative) {
                return (short) -result;
            }
            return result;
        } catch (NumberFormatException e) {
            String constant = negative ? ("-" + nm.substring(index))
                    : nm.substring(index);
            result = Short.parseShort(constant, 2);
        }
        return result;
    }

    private static long fastDecodeLongHex(String nm) throws NumberFormatException {
        int index = 0;
        boolean negative = false;
        long result;

        char firstChar = nm.charAt(0);
        // Handle sign, if present
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }

        //(nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
        index += 2;

        try {
            result = Long.parseLong(nm.substring(index), 16);
            if (negative) {
                return -result;
            }
            return result;
        } catch (NumberFormatException e) {
            String constant = negative ? ("-" + nm.substring(index))
                    : nm.substring(index);
            result = Long.parseLong(constant, 16);
        }
        return result;
    }

    private static BigInteger fastDecodeBigIntHex(String nm) throws NumberFormatException {
        int index = 0;
        boolean negative = false;
        BigInteger result;

        char firstChar = nm.charAt(0);
        // Handle sign, if present
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }

        //(nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
        index += 2;

        try {
            result = new BigInteger(nm.substring(index), 16);
            if (negative) {
                return result.negate();
            }
            return result;
        } catch (NumberFormatException e) {
            String constant = negative ? ("-" + nm.substring(index))
                    : nm.substring(index);
            result = new BigInteger(constant, 16);
        }
        return result;
    }

    private static long fastDecodeLongBin(String nm) throws NumberFormatException {
        int index = 0;
        boolean negative = false;
        long result;

        char firstChar = nm.charAt(0);
        // Handle sign, if present
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }

        //(nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
        index += 2;

        try {
            result = Long.parseLong(nm.substring(index), 2);
            if (negative) {
                return -result;
            }
            return result;
        } catch (NumberFormatException e) {
            String constant = negative ? ("-" + nm.substring(index))
                    : nm.substring(index);
            result = Long.parseLong(constant, 2);
        }
        return result;
    }

    private static BigInteger fastDecodeBigIntBin(String nm) throws NumberFormatException {
        int index = 0;
        boolean negative = false;
        BigInteger result;

        char firstChar = nm.charAt(0);
        // Handle sign, if present
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }

        //(nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
        index += 2;

        try {
            result = new BigInteger(nm.substring(index), 2);
            if (negative) {
                return result.negate();
            }
            return result;
        } catch (NumberFormatException e) {
            String constant = negative ? ("-" + nm.substring(index))
                    : nm.substring(index);
            result = new BigInteger(constant, 2);
        }
        return result;
    }

    public static TsonElement parseNaNElem(String s) {
        if (s == null) {
            return Tson.of(Double.NaN);
        }
        switch (s) {
            case "double":
                return Tson.of(Double.NaN);
            case "float":
                return Tson.of(Float.NaN);
        }
        throw new IllegalArgumentException("Unsupported NaN(" + s + ")");
    }

    public static TsonElement parsePosInfElem(String s) {
        if (s == null) {
            return Tson.of(Double.POSITIVE_INFINITY);
        }
        switch (s) {
            case "double":
                return Tson.of(Double.POSITIVE_INFINITY);
            case "float":
                return Tson.of(Float.POSITIVE_INFINITY);
        }
        throw new IllegalArgumentException("Unsupported +Bound(" + s + ")");
    }

    public static TsonElement parseNegInfElem(String s) {
        if (s == null) {
            return Tson.of(Double.NEGATIVE_INFINITY);
        }
        switch (s) {
            case "double":
                return Tson.of(Double.NEGATIVE_INFINITY);
            case "float":
                return Tson.of(Float.NEGATIVE_INFINITY);
        }
        throw new IllegalArgumentException("Unsupported -Bound(" + s + ")");
    }

    public static TsonElement parsePosBoundElem(String s) {
        if (s == null) {
            return Tson.of(Double.MAX_VALUE);
        }
        switch (s) {
            case "double":
                return Tson.of(Double.MAX_VALUE);
            case "float":
                return Tson.of(Float.MAX_VALUE);
            case "byte":
                return Tson.of(Byte.MAX_VALUE);
            case "short":
                return Tson.of(Short.MAX_VALUE);
            case "int":
                return Tson.of(Integer.MAX_VALUE);
            case "long":
                return Tson.of(Long.MAX_VALUE);
        }
        throw new IllegalArgumentException("Unsupported +Inf(" + s + ")");
    }

    public static TsonElement parseNegBoundElem(String s) {
        if (s == null) {
            return Tson.of(Double.MIN_VALUE);
        }
        switch (s) {
            case "double":
                return Tson.of(Double.MIN_VALUE);
            case "float":
                return Tson.of(Float.MIN_VALUE);
            case "byte":
                return Tson.of(Byte.MIN_VALUE);
            case "short":
                return Tson.of(Short.MIN_VALUE);
            case "int":
                return Tson.of(Integer.MIN_VALUE);
            case "long":
                return Tson.of(Long.MIN_VALUE);
        }
        throw new IllegalArgumentException("Unsupported +Inf(" + s + ")");
    }

}

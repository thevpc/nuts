/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.core.util;

import net.thevpc.nuts.*;

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Created by vpc on 5/16/17.
 */
public final class CoreStringUtils {

    private static final Pattern PATTERN_ALL = Pattern.compile(".*");



    public static String escapeQuoteStrings(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\':
                case '\'': {
                    sb.append("\\");
                    sb.append(c);
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public static String escapeReplacementStrings(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\':
                case '{':
                case '}':
                case '$': {
                    sb.append("\\");
                    sb.append(c);
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    /**
     * @param text text
     * @param compact if true, quotes will not be used unless necessary
     * @param entrySeparators entrySeparators
     * @return quotes
     */
    public static String simpleQuote(String text, boolean compact, String entrySeparators) {
        StringBuilder sb = new StringBuilder();
        boolean q = !compact;
        for (char c : text.toCharArray()) {
            switch (c) {
                case '\n': {
                    q = true;
                    sb.append("\\n");
                    break;
                }
                case '\f': {
                    q = true;
                    sb.append("\\f");
                    break;
                }
                case '\r': {
                    q = true;
                    sb.append("\\r");
                    break;
                }
                case '\'':
                case '\"': {
                    q = true;
                    sb.append("\\").append(c);
                    break;
                }
                default: {
                    if (entrySeparators != null && entrySeparators.indexOf(c) >= 0) {
                        q = true;
                        sb.append("\\").append(c);
                    } else {
                        sb.append(c);
                    }
                    break;
                }
            }
        }
        if (q) {
            sb.insert(0, '\'');
            sb.append('\'');
        }
        return sb.toString();
    }

    public static String dblQuote(String text) {
        return dblQuote(text, false, null);
    }

    /**
     * @param text text
     * @param compact if true, quotes will not be used unless necessary
     * @param entrySeparators entrySeparators
     * @return double quotes
     */
    public static String dblQuote(String text, boolean compact, String entrySeparators) {
        StringBuilder sb = new StringBuilder();
        boolean q = !compact;
        for (char c : text.toCharArray()) {
            switch (c) {
                case '\n': {
                    q = true;
                    sb.append("\\n");
                    break;
                }
                case '\f': {
                    q = true;
                    sb.append("\\f");
                    break;
                }
                case '\r': {
                    q = true;
                    sb.append("\\r");
                    break;
                }
                case '\"': {
                    q = true;
                    sb.append("\\").append(c);
                    break;
                }
                default: {
                    if (entrySeparators != null && entrySeparators.indexOf(c) >= 0) {
                        q = true;
                        sb.append("\\").append(c);
                    } else {
                        sb.append(c);
                    }
                    break;
                }
            }
        }
        if (q) {
            sb.insert(0, '\"');
            sb.append('\"');
        }
        return sb.toString();
    }

    public static boolean containsVars(String value) {
        return value != null && value.contains("${");
    }

    public static boolean containsTopWord(String word, String line) {
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(line));
        int last_ttype = -1;
        try {
            while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                switch (tokenizer.ttype) {
                    case StreamTokenizer.TT_WORD: {
                        if (word.equals(tokenizer.sval)) {
                            if (last_ttype != '.') {
                                return true;
                            }
                        }
                    }
                }
                last_ttype = tokenizer.ttype;
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return false;
    }

    public static char[] strToChr(String s) {
        if (s == null) {
            return null;
        }
        return s.toCharArray();
    }

    public static String chrToStr(char[] s) {
        if (s == null) {
            return null;
        }
        return new String(s);
    }


    //    /**
//     * copied from StringUtils (in order to remove dependency)
//     *
//     * @param s string
//     * @param converter converter
//     * @return replaced string
//     */
//    public static String replaceDollarPlaceHolders(String s, Map<String, String> converter) {
//        return StringPlaceHolderParser.replaceDollarPlaceHolders(s, new MapToFunction(converter));
//    }
    public static String enforceDoubleQuote(String s) {
        if (s.isEmpty() || s.contains(" ") || s.contains("\"")) {
            s = "\"" + s.replace("\"", "\\\"") + "\"";
        }
        return s;
    }

    public static String enforceDoubleQuote(String s, NutsSession session) {
        s = session.text().builder().append(s).toString();
        if (s.isEmpty() || s.contains(" ") || s.contains("\"") || s.contains("'")) {
            s = "\"" + s + "\"";
        }
        return s;
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param c builder
     * @return builder
     */
    public static StringBuilder clear(StringBuilder c) {
        return c.delete(0, c.length());
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param ex ex
     * @return String from exception
     */
    public static String exceptionToString(Throwable ex) {
        return exceptionToString(ex, false);
    }

    public static int firstIndexOf(String string, char[] chars) {
        char[] value = string.toCharArray();
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < chars.length; j++) {
                if (value[i] == chars[j]) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static String exceptionToString(Throwable ex, boolean inner) {
        String msg = null;
        if (ex instanceof NutsNotFoundException || ex instanceof UncheckedIOException) {
            if (ex.getCause() != null) {
                Throwable ex2 = ex.getCause();
                if (ex2 instanceof UncheckedIOException) {
                    ex2 = ex.getCause();
                }
                msg = exceptionToString(ex2, true);
            } else {
                msg = ex.getMessage();
            }
        } else {
            String msg2 = ex.toString();
            if (msg2.startsWith(ex.getClass().getName() + ":")) {
                if (inner) {
                    //this is  default toString for the exception
                    msg = msg2.substring((ex.getClass().getName()).length() + 1).trim();
                } else {
                    msg = ex.getClass().getSimpleName() + ": " + msg2.substring((ex.getClass().getName()).length() + 1).trim();
                }
            } else {
                for (Class aClass : new Class[]{
                        NullPointerException.class,
                        ArrayIndexOutOfBoundsException.class,
                        ClassCastException.class,
                        UnsupportedOperationException.class,
                        ReflectiveOperationException.class,
                        Error.class,}) {
                    if (aClass.isInstance(ex)) {
                        return ex.toString();
                    }
                }
                msg = ex.getMessage();
                if (msg == null) {
                    msg = ex.toString();
                }
            }
        }
        return msg;
    }

    public static NutsMessage exceptionToMessage(Throwable ex) {
        return exceptionToMessage(ex, false);
    }

    public static NutsMessage exceptionToMessage(Throwable ex, boolean inner) {
        NutsMessage msg = null;
        if (ex instanceof UncheckedIOException) {
            if (ex.getCause() != null) {
                Throwable ex2 = ex.getCause();
                if (ex2 instanceof UncheckedIOException) {
                    ex2 = ex.getCause();
                }
                msg = exceptionToMessage(ex2, true);
            } else {
                msg = NutsMessage.plain(ex.getMessage());
            }
        } else if (ex instanceof NutsNotFoundException) {
            if (ex.getCause() != null) {
                Throwable ex2 = ex.getCause();
                if (ex2 instanceof UncheckedIOException) {
                    ex2 = ex.getCause();
                }
                msg = exceptionToMessage(ex2, true);
            } else {
                msg = NutsMessage.formatted(((NutsNotFoundException) ex).getFormattedString().toString());
            }
        } else if (ex instanceof NutsException) {
            msg = NutsMessage.formatted(((NutsException) ex).getFormattedString().toString());
        } else {
            String msg2 = ex.toString();
            if (msg2.startsWith(ex.getClass().getName() + ":")) {
                if (inner) {
                    //this is  default toString for the exception
                    msg = NutsMessage.plain(msg2.substring((ex.getClass().getName()).length() + 1).trim());
                } else {
                    msg = NutsMessage.plain(ex.getClass().getSimpleName() + ": " + msg2.substring((ex.getClass().getName()).length() + 1).trim());
                }
            } else {
                for (Class aClass : new Class[]{
                        NullPointerException.class,
                        ArrayIndexOutOfBoundsException.class,
                        ClassCastException.class,
                        UnsupportedOperationException.class,
                        ReflectiveOperationException.class,
                        Error.class,}) {
                    if (aClass.isInstance(ex)) {
                        return NutsMessage.plain(ex.toString());
                    }
                }
                msg = ex.getMessage() == null ? null : NutsMessage.plain(ex.getMessage());
                if (msg == null) {
                    msg = NutsMessage.plain(ex.toString());
                }
            }
        }
        return msg;
    }

//    /**
//     * copied from StringUtils (in order to remove dependency)
//     *
//     * @param cmd string array
//     * @return the first non empty element of the array
//     */
//    public static String coalesce(String... cmd) {
//        for (String string : cmd) {
//            if (!NutsBlankable.isBlank(string)) {
//                return string;
//            }
//        }
//        return null;
//    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param x x
     * @param width width
     * @return string filled
     */
    public static String fillString(char x, int width) {
        if (width <= 0) {
            return "";
        }
        char[] cc = new char[width];
        Arrays.fill(cc, x);
        return new String(cc);
    }

    public static String fillString(String x, int width) {
        if (width <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        fillString(x, width, sb);
        return sb.toString();
    }

    public static void fillString(char x, int width, StringBuilder sb) {
        if (width <= 0) {
            return;
        }
        sb.ensureCapacity(sb.length() + width);
        for (int i = 0; i < width; i++) {
            sb.append(x);
        }
    }

    public static void fillString(char x, int width, NutsTextBuilder sb) {
        if (width <= 0) {
            return;
        }
//        sb.ensureCapacity(sb.length() + width);
        for (int i = 0; i < width; i++) {
            sb.append(x);
        }
    }

    public static void fillString(String x, int width, StringBuilder sb) {
        if (width <= 0) {
            return;
        }
        sb.ensureCapacity(sb.length() + (width * x.length()));
        for (int i = 0; i < width; i++) {
            sb.append(x);
        }
    }

    public static void fillString(String x, int width, NutsTextBuilder sb) {
        if (width <= 0) {
            return;
        }
        //sb.ensureCapacity(sb.length() + (width * x.length()));
        for (int i = 0; i < width; i++) {
            sb.append(x);
        }
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param s string
     * @param width width
     * @return aligned string
     */
    public static String alignLeft(String s, int width) {
        StringBuilder sb = new StringBuilder();
        if (s != null) {
            sb.append(s);
            int x = width - sb.length();
            if (x > 0) {
                sb.append(fillString(' ', x));
            }
        }
        return sb.toString();
    }

    public static String alignRight(String s, int width) {
        StringBuilder sb = new StringBuilder();
        if (s != null) {
            sb.append(s);
            int x = width - sb.length();
            if (x > 0) {
                sb.insert(0, fillString(' ', x));
            }
        }
        return sb.toString();
    }

    public static String indexToString(int x) {
        if (x < 0) {
            return "-" + indexToString(-x);
        }
        StringBuilder sb = new StringBuilder();
        while (x > 0) {
            int y = x % 10;
            if (y == 0) {
                sb.insert(0, '0');
            } else {
                sb.insert(0, ((char) ('A' + (y - 1))));
            }
            x = x / 10;
        }
        if (sb.length() == 0) {
            return "A";
        }
        return sb.toString();
    }

    public static String[] parseAndTrimToDistinctArray(String s){
        if(s==null){
            return  new String[0];
        }
        return Arrays.stream(s.split("[,;| \t]")).map(String::trim)
                .filter(x->x.length()>0)
                .distinct().toArray(String[]::new);
    }

    public static String joinAndTrimToNull(String[] args){
        return NutsUtilStrings.trimToNull(
                String.join(",",args)
        );
    }

    public static String prefixLinesPortableNL(String str,String prefix) {
        return prefixLines(str,prefix,"\n");
    }
    public static String prefixLinesOsNL(String str,String prefix) {
        return prefixLines(str,prefix,System.getProperty("line.separator"));
    }

    public static String prefixLines(String str,String prefix,String nl) {
        BufferedReader br=new BufferedReader(new StringReader(str==null?"":str));
        StringBuilder sb=new StringBuilder();
        String line;
        boolean first=true;
        if(nl==null) {
            nl = System.getProperty("line.separator");
            if(nl==null) {
                nl = "\n";
            }
        }
        while(true){
            try {
                if ((line = br.readLine()) == null) {
                    break;
                }
            } catch (IOException e) {
                break;
            }
            if(first){
                first=false;
            }else{
                sb.append(nl);
            }
            sb.append(prefix);
            sb.append(line);
        }
        return sb.toString();
    }

    public static String coalesce(String a, String b) {
        return a==null?b:a;
    }
}

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
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.util.NStringUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by vpc on 5/16/17.
 */
public final class CoreStringUtils {

    private static final Pattern PATTERN_ALL = Pattern.compile(".*");



    public static String generateIndexedName(String name, Predicate<String> exists) {
        int x=1;
        while(true){
            String a=name+(x==1?"":(" "+x));
            if(!exists.test(a)){
                return a;
            }
            x++;
        }
    }

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
            // not error expected here
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
        if (ex instanceof NNotFoundException || ex instanceof UncheckedIOException) {
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

    public static NMsg exceptionToMessage(Throwable ex) {
        return exceptionToMessage(ex, false);
    }

    public static NMsg exceptionToMessage(Throwable ex, boolean inner) {
        NMsg msg = null;
        if (ex instanceof UncheckedIOException) {
            if (ex.getCause() != null) {
                Throwable ex2 = ex.getCause();
                if (ex2 instanceof UncheckedIOException) {
                    ex2 = ex.getCause();
                }
                msg = exceptionToMessage(ex2, true);
            } else {
                msg = NMsg.ofPlain(ex.getMessage());
            }
        } else if (ex instanceof NNotFoundException) {
            if (ex.getCause() != null) {
                Throwable ex2 = ex.getCause();
                if (ex2 instanceof UncheckedIOException) {
                    ex2 = ex.getCause();
                }
                msg = exceptionToMessage(ex2, true);
            } else {
                msg = ((NNotFoundException) ex).getFormattedMessage();
            }
        } else if (ex instanceof NException) {
            msg = ((NException) ex).getFormattedMessage();
        } else {
            String msg2 = ex.toString();
            if (msg2.startsWith(ex.getClass().getName() + ":")) {
                if (inner) {
                    //this is  default toString for the exception
                    msg = NMsg.ofPlain(msg2.substring((ex.getClass().getName()).length() + 1).trim());
                } else {
                    msg = NMsg.ofPlain(ex.getClass().getSimpleName() + ": " + msg2.substring((ex.getClass().getName()).length() + 1).trim());
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
                        return NMsg.ofPlain(ex.toString());
                    }
                }
                msg = ex.getMessage() == null ? null : NMsg.ofPlain(ex.getMessage());
                if (msg == null) {
                    msg = NMsg.ofPlain(ex.toString());
                }
            }
        }
        return msg;
    }

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

    public static void fillString(char x, int width, NTextBuilder sb) {
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

    public static void fillString(String x, int width, NTextBuilder sb) {
        if (width <= 0) {
            return;
        }
        //sb.ensureCapacity(sb.length() + (width * x.length()));
        for (int i = 0; i < width; i++) {
            sb.append(x);
        }
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

    public static List<String> parseAndTrimToDistinctReadOnlyList(String s){
        return Collections.unmodifiableList(parseAndTrimToDistinctList(s));
    }

    public static List<String> parseAndTrimToDistinctList(String s){
        return Arrays.asList(parseAndTrimToDistinctArray(s));
    }

    public static String[] parseAndTrimToDistinctArray(String s){
        if(s==null){
            return  new String[0];
        }
        return StringTokenizerUtils.splitDefault(s).stream().map(String::trim)
                .filter(x->x.length()>0)
                .distinct().toArray(String[]::new);
    }

    public static String joinAndTrimToNull(List<String> args){
        return NStringUtils.trimToNull(
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


    public static List<String> splitOnNewlines(String line) {
        char[] text = line.toCharArray();
        StringBuilder sb = new StringBuilder();
        List<String> tb = new ArrayList<>();
        for (int i = 0; i < text.length; i++) {
            switch (text[i]) {
                case '\r': {
                    if (sb.length() > 0) {
                        tb.add(sb.toString());
                        sb.setLength(0);
                    }
                    if (i + 1 < text.length && text[i + 1] == '\n') {
                        tb.add("\r\n");
                        i++;
                    } else {
                        tb.add("\r");
                    }
                    break;
                }
                case '\n': {
                    if (sb.length() > 0) {
                        tb.add(sb.toString());
                        sb.setLength(0);
                    }
                    tb.add("\n");
                    break;
                }
                default: {
                    sb.append(text[i]);
                }
            }
        }
        if (sb.length() > 0) {
            tb.add(sb.toString());
            sb.setLength(0);
        }
        return tb;
    }

    public static String stringValue(Object o) {
        if (o == null) {
            return ("");
        }
        if (o.getClass().isEnum()) {
            return (CoreEnumUtils.getEnumString((Enum) o));
        }
        if (o instanceof Instant) {
            return (CoreNUtils.DEFAULT_DATE_TIME_FORMATTER.format(((Instant) o)));
        }
        if (o instanceof Date) {
            return (CoreNUtils.DEFAULT_DATE_TIME_FORMATTER.format(((Date) o).toInstant()));
        }
        if (o instanceof Collection) {
            Collection c = ((Collection) o);
            Object[] a = c.toArray();
            if (a.length == 0) {
                return ("");
            }
            if (a.length == 1) {
                return stringValue(a[0]);
            }
            return ("[" + String.join(", ", (List) c.stream().map(x -> stringValue(x)).collect(Collectors.toList())) + "]");
        }
        if (o.getClass().isArray()) {
            int len = Array.getLength(o);
            if (len == 0) {
                return ("");
            }
            if (len == 1) {
                return stringValue(Array.get(o, 0));
            }
            List<String> all = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                all.add(stringValue(Array.get(o, i)).toString());
            }
            return ("[" + String.join(", ", all) + "]");
        }
        return (o.toString());
    }



}

/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.StringMapper;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.vpc.app.nuts.NutsIOException;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsParseException;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreStringUtils {

    private static Pattern pattern = Pattern.compile("\\$\\{(?<key>[^}]*)\\}");

    public static String[] nonNullArray(String[] array1) {
        return array1 == null ? new String[0] : array1;
    }

    public static String[] concat(String[] array1, String[] array2) {
        String[] r = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, r, 0, array1.length);
        System.arraycopy(array2, 0, r, array1.length, array2.length);
        return r;
    }

    public static String[] removeFirst(String[] array) {
        String[] r = new String[array.length - 1];
        System.arraycopy(array, 1, r, 0, r.length);
        return r;
    }

    public static int parseInt(String v1, int defaultValue) {
        try {
            if (CoreStringUtils.isEmpty(v1)) {
                return defaultValue;
            }
            return Integer.parseInt(CoreStringUtils.trim(v1));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String checkNotEmpty(String str, String name) {
        str = CoreStringUtils.trim(str);
        if (CoreStringUtils.isEmpty(str)) {
            throw new NutsIllegalArgumentException("Empty string not allowed for " + name);
        }
        return str.trim();
    }

    /**
     * code from org.apache.tools.ant.types.Commandline copyrights goes to
     * Apache Ant Authors (Licensed to the Apache Software Foundation (ASF))
     * Crack a command line.
     *
     * @param line the command line to process.
     * @return the command line broken into strings. An empty or null toProcess
     * parameter results in a zero sized array.
     */
    public static String[] parseCommandline(String line) {
        if (line == null || line.length() == 0) {
            //no command? no string
            return new String[0];
        }
        // parse with a simple finite state machine

        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        final StringTokenizer tok = new StringTokenizer(line, "\"\' ", true);
        final ArrayList<String> result = new ArrayList<String>();
        final StringBuilder current = new StringBuilder();
        boolean lastTokenHasBeenQuoted = false;

        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            switch (state) {
                case inQuote:
                    if ("\'".equals(nextTok)) {
                        lastTokenHasBeenQuoted = true;
                        state = normal;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                case inDoubleQuote:
                    if ("\"".equals(nextTok)) {
                        lastTokenHasBeenQuoted = true;
                        state = normal;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                default:
                    switch (nextTok) {
                        case "\'":
                            state = inQuote;
                            break;
                        case "\"":
                            state = inDoubleQuote;
                            break;
                        case " ":
                            if (lastTokenHasBeenQuoted || current.length() != 0) {
                                result.add(current.toString());
                                current.setLength(0);
                            }
                            break;
                        default:
                            current.append(nextTok);
                            break;
                    }
                    lastTokenHasBeenQuoted = false;
                    break;
            }
        }
        if (lastTokenHasBeenQuoted || current.length() != 0) {
            result.add(current.toString());
        }
        if (state == inQuote || state == inDoubleQuote) {
            throw new NutsParseException("unbalanced quotes in " + line);
        }
        return result.toArray(new String[result.size()]);
    }

    public static Map<String, String> parseMap(String text, String entrySeparators) {
        return parseMap(text, "=", entrySeparators);
    }

    public static Map<String, String> parseMap(String text, String eqSeparators, String entrySeparators) {
        Map<String, String> m = new LinkedHashMap<>();
        if (text == null) {
            return m;
        }
        StringReader reader = new StringReader(text);
        while (true) {
            StringBuilder key = new StringBuilder();
            int r = 0;
            try {
                r = readToken(reader, eqSeparators + entrySeparators, key);
            } catch (IOException e) {
                throw new NutsIOException(e);
            }
            String t = key.toString();
            if (r == -1) {
                if (!t.isEmpty()) {
                    m.put(t, null);
                }
                break;
            } else {
                char c = (char) r;
                if (eqSeparators.indexOf(c) >= 0) {
                    StringBuilder value = new StringBuilder();
                    try {
                        r = readToken(reader, entrySeparators, value);
                    } catch (IOException e) {
                        throw new NutsIOException(e);
                    }
                    m.put(t, value.toString());
                    if (r == -1) {
                        break;
                    }
                }
            }
        }
        return m;
    }

    public static int readToken(Reader reader, String stopTokens, StringBuilder result) throws IOException {
        while (true) {
            int r = reader.read();
            if (r == -1) {
                return -1;
            }
            if (r == '\"' || r == '\'') {
                char s = (char) r;
                while (true) {
                    r = reader.read();
                    if (r == -1) {
                        throw new NutsParseException("Expected " + '\"');
                    }
                    if (r == s) {
                        break;
                    }
                    if (r == '\\') {
                        r = reader.read();
                        if (r == -1) {
                            throw new NutsParseException("Expected " + '\"');
                        }
                        switch ((char) r) {
                            case 'n': {
                                result.append('\n');
                                break;
                            }
                            case 'r': {
                                result.append('\r');
                                break;
                            }
                            case 'f': {
                                result.append('\f');
                                break;
                            }
                            default: {
                                result.append((char) r);
                            }
                        }
                    } else {
                        char cr = (char) r;
                        result.append(cr);
                    }
                }
            } else {
                char cr = (char) r;
                if (stopTokens != null && stopTokens.indexOf(cr) >= 0) {
                    return cr;
                }
                result.append(cr);
            }
        }
    }

    public static StringBuilder clear(StringBuilder c) {
        return c.delete(0, c.length());
    }

    public static int getStartingInt(String v1) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < v1.length(); i++) {
            char c = v1.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            return Integer.parseInt(sb.toString());
        }
        return -1;
    }

    public static boolean isInt(String v1) {
        try {
            if (v1.length() == 0) {
                return false;
            }
            Integer.parseInt(v1);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isLong(String v1) {
        try {
            if (v1.length() == 0) {
                return false;
            }
            Long.parseLong(v1);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String fillString(char x, int width) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width; i++) {
            sb.append(x);
        }
        return sb.toString();
    }

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

    public static String join(String sep, Collection<String> items) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> i = items.iterator();
        if (i.hasNext()) {
            sb.append(i.next());
        }
        while (i.hasNext()) {
            sb.append(sep);
            sb.append(i.next());
        }
        return sb.toString();
    }

    public static String exceptionToString(Throwable ex) {
        String message = ex.getMessage();
        if(message==null){
            message=ex.toString();
        }
        return message;
    }

    public static String simpexpToRegexp(String pattern) {
        return simpexpToRegexp(pattern, false);
    }

    /**
     * *
     * **
     *
     * @param pattern
     * @return
     */
    public static String simpexpToRegexp(String pattern, boolean contains) {
        if (pattern == null) {
            pattern = "*";
        }
        int i = 0;
        char[] cc = pattern.toCharArray();
        StringBuilder sb = new StringBuilder();
        while (i < cc.length) {
            char c = cc[i];
            switch (c) {
                case '.':
                case '!':
                case '$':
                case '[':
                case ']':
                case '(':
                case ')':
                case '?':
                case '^':
                case '|':
                case '\\': {
                    sb.append('\\').append(c);
                    break;
                }
                case '*': {
//                    if (i + 1 < cc.length && cc[i + 1] == '*') {
//                        i++;
//                        sb.append("[a-zA-Z_0-9_$.-]*");
//                    } else {
//                        sb.append("[a-zA-Z_0-9_$-]*");
//                    }
                    sb.append(".*");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
            i++;
        }
        if (!contains) {
            sb.insert(0, '^');
            sb.append('$');
        }
        return sb.toString();
    }

    public static String replaceVars(String format, StringMapper map) {
        return replaceVars(format, map, new HashSet());
    }

    private static String replaceVars(String format, StringMapper map, Set<String> visited) {
        StringBuffer sb = new StringBuffer();
        Matcher m = pattern.matcher(format);
        while (m.find()) {
            String key = m.group("key");
            if (visited.contains(key)) {
                m.appendReplacement(sb, key);
            } else {
                Set<String> visited2 = new HashSet<>(visited);
                visited2.add(key);
                String replacement = map.get(key);
                if (replacement != null) {//replace if founded key exists in map
                    replacement = replaceVars(replacement, map, visited2);
                    m.appendReplacement(sb, escapeReplacementStrings(replacement));
                } else {//do not replace, or to be precise replace with same value
                    m.appendReplacement(sb, escapeReplacementStrings(m.group()));
                }
            }
        }
        m.appendTail(sb);

        return sb.toString();
    }

    public static String escapeCoteStrings(String s) {
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
     * @param text
     * @param compact if true, quotes will not be used unless necessary
     * @param entrySeparators
     * @return
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
                    if (entrySeparators.indexOf(c) >= 0) {
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

    public static String trimToNull(String str) {
        if (str == null) {
            return null;
        }
        String trimmed = str.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static String trim(String str) {
        if (str == null) {
            return "";
        }
        return str.trim();
    }

    public static String repeat(char c, int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, c);
        return new String(chars);
    }

    public static List<String> split(String str, String separators) {
        if (str == null) {
            return Collections.EMPTY_LIST;
        }
        StringTokenizer st = new StringTokenizer(str, separators);
        List<String> result = new ArrayList<>();
        while (st.hasMoreElements()) {
            result.add(st.nextToken());
        }
        return result;
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
            throw new NutsIOException(ex);
        }
        return false;
    }

    public static int[] searchRabinKarp(String phrase, String[] words) {
        char[] cphrase = phrase.toCharArray();
        char[][] cwords = new char[words.length][];
        for (int i = 0; i < words.length; i++) {
            cwords[i] = words[i].toCharArray();
        }
        return searchRabinKarp(cphrase, cwords);
    }

    public static int[] searchRabinKarp(char[] phrase, char[][] words) {
        int[] wordhash = new int[words.length];
        int[] wordLengths = new int[words.length];
        for (int w = 0; w < wordhash.length; w++) {
            wordhash[w] = _rkhash(words[w], 0, words[w].length, 0);
            wordLengths[w] = words[w].length;
        }
        int[] shash = new int[words.length];
        for (int i = 0; i < phrase.length; i++) {
            for (int j = 0; j < words.length; j++) {
                char[] word = words[j];
                if (i <= phrase.length - word.length) {

                }
            }
            shash = _rkhash(phrase, i, wordLengths, shash);
            for (int w = 0; w < words.length; w++) {
                if (shash[w] == wordhash[w]) {
                    // compare actual characters to be sure
                    boolean ok = true;
                    int k2, j2;
                    for (k2 = i, j2 = 0; k2 < words[w].length && ok; k2++, j2++) {
                        if (phrase[i] != words[w][j2]) {
                            ok = false;
                            break;
                        }
                    }
                    if (ok) {
                        return new int[]{i, w};
                    }
                }
            }
        }
        return null;
    }

    public static int searchRabinKarp(char[] phrase, char[] word) {
        int wordhash = _rkhash(word, 0, word.length, 0);
        int shash = 0;
        for (int i = 0; i <= phrase.length - word.length; i++) {
            shash = _rkhash(phrase, i, word.length, shash);
            if (shash == wordhash) {
                // compare actual characters to be sure
                boolean ok = true;
                int k, j;
                for (k = i, j = 0; k < word.length && ok; k++, j++) {
                    if (phrase[i] != word[j]) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int _rkhash(char[] s, int i, int len, int hval) {
        if (i == 0) {// first run, compute full hash
            hval = 0;
            for (int k = 0; k < len; k++) {
                hval += s[k];
            }
            return hval;
        }
        if (hval != 0 && i != 0) {
            hval -= s[i - 1];
        }
        if (i + len <= s.length) {
            hval += s[i + len - 1];
        } else // out of bounds
        {
            return -1;
        }
        return hval;
    }

    private static int[] _rkhash(char[] s, int i, int[] len, int[] hval) {
        if (i == 0) {// first run, compute full hash
            for (int j = 0; j < hval.length; j++) {
                if (i <= s.length - len[j]) {
                    hval[j] = 0;
                    for (int k = 0; k < len[j]; k++) {
                        hval[j] += s[k];
                    }
                } else {
                    hval[j] = -1;
                }
            }
        }
        for (int j = 0; j < hval.length; j++) {
            if (hval[j] != 0 && i != 0) {
                hval[j] -= s[i - 1];
            }
            if (i + len[j] <= s.length) {
                hval[j] += s[i + len[j] - 1];
            } else // out of bounds
            {
                hval[j] = -1;
            }
        }
        return hval;
    }

    // %[argument_index$][flags][width][.precision][t]conversion
    private static final Pattern printfPattern = Pattern.compile("%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])");

    private static String format0(Locale locale, String format0, Object arg) {
        StringBuilder sb = new StringBuilder();
        new Formatter(sb, locale).format(format0, new Object[]{arg});
        return sb.toString();
    }

    public static String nescape(String str) {
        if (str == null) {
            str = "";
        }
        str = str.replace("`", "\\`");
        return "``" + str + "``";
    }

    public static String format(Locale locale, String format, Object... args) {

        StringBuilder sb = new StringBuilder();
        Matcher m = printfPattern.matcher(format);
        int x = 0;
        for (int i = 0, len = format.length(); i < len;) {
            if (m.find(i)) {
                // Anything between the start of the string and the beginning
                // of the format specifier is either fixed text or contains
                // an invalid format string.
                if (m.start() != i) {
                    //checkText(s, i, m.start());
                    sb.append(format.substring(i, m.start()));
                }
                sb.append(nescape(format0(locale, m.group(), args[x])));
                x++;
                i = m.end();
            } else {
                sb.append(format.substring(i));
                break;
            }
        }
        return sb.toString();

//        char[] chars = format.toCharArray();
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < chars.length - 2; i++) {
//            if (chars[i] == '{' && Character.isDigit(chars[i + 1])) {
//                int j = i + 1;
//                while (j < chars.length && Character.isDigit(chars[j])) {
//                    j++;
//                }
//                if (j < chars.length && chars[j] == '}') {
//                    int pos = Integer.parseInt(new String(chars, i + 1, j));
//                    sb.append("``");
//                    sb.append(args[pos]);
//                    sb.append("``");
//                    i = j;
//                } else {
//                    sb.append(chars[i]);
//
//                }
//            } else {
//                sb.append(chars[i]);
//            }
//        }
//        return sb.toString();
    }

}

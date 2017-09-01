package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.util.StringUtils;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreStringUtils {
    //    public static void main(String[] args) {
//        try {
//            String encrypted = SecurityUtils.httpEncrypt("hello hello hello hello hello hello hello hello".getBytes(), "mypwd");
//            System.out.println(encrypted);
//            byte[] decrypted = SecurityUtils.httpDecrypt(encrypted, "mypwd");
//            System.out.println(new String(decrypted ));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
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
            if (StringUtils.isEmpty(v1)) {
                return defaultValue;
            }
            return Integer.parseInt(StringUtils.trim(v1));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String checkNotEmpty(String str, String name) {
        str = StringUtils.trim(str);
        if (StringUtils.isEmpty(str)) {
            throw new RuntimeException("Empty string not allowed for " + name);
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
                    if ("\'".equals(nextTok)) {
                        state = inQuote;
                    } else if ("\"".equals(nextTok)) {
                        state = inDoubleQuote;
                    } else if (" ".equals(nextTok)) {
                        if (lastTokenHasBeenQuoted || current.length() != 0) {
                            result.add(current.toString());
                            current.setLength(0);
                        }
                    } else {
                        current.append(nextTok);
                    }
                    lastTokenHasBeenQuoted = false;
                    break;
            }
        }
        if (lastTokenHasBeenQuoted || current.length() != 0) {
            result.add(current.toString());
        }
        if (state == inQuote || state == inDoubleQuote) {
            throw new RuntimeException("unbalanced quotes in " + line);
        }
        return result.toArray(new String[result.size()]);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;

/**
 * @author thevpc
 */
public class ShellUtils {

//    public static String escapeString(String name) {
//        if (name == null) {
//            name = "";
//        }
//        StringBuilder sb = new StringBuilder();
//        sb.append("\"");
//        for (char c : name.toCharArray()) {
//            switch (c) {
//                case '\"':
//                case '\\': {
//                    sb.append('\\').append(c);
//                    break;
//                }
//                case '\t': {
//                    sb.append('\\').append('t');
//                    break;
//                }
//                case '\n': {
//                    sb.append('\\').append('n');
//                    break;
//                }
//                case '\r': {
//                    sb.append('\\').append('r');
//                    break;
//                }
//                case '\f': {
//                    sb.append('\\').append('f');
//                    break;
//                }
//                default: {
//                    sb.append(c);
//                }
//            }
//        }
//        sb.append("\"");
//        return sb.toString();
//    }

    public static String shellPatternToRegexp(String pattern) {
        String pathSeparator = "/";
        if (pattern == null) {
            pattern = "*";
        }
        int i = 0;
        char[] cc = pattern.toCharArray();
        StringBuilder sb = new StringBuilder("^");
        while (i < cc.length) {
            char c = cc[i];
            switch (c) {
                case '.':
                case '!':
                case '$':
                case '{':
                case '}':
                case '+': {
                    sb.append('\\').append(c);
                    break;
                }
                case '\\': {
                    sb.append(c);
                    i++;
                    sb.append(cc[i]);
                    break;
                }
                case '[': {
                    while (i < cc.length) {
                        sb.append(cc[i]);
                        if (cc[i] == ']') {
                            break;
                        }
                    }
                    break;
                }
                case '?': {
                    sb.append("[^").append(pathSeparator).append("]");
                    break;
                }
                case '*': {
                    if (i + 1 < cc.length && cc[i + 1] == '*') {
                        i++;
                        sb.append(".*");
                    } else {
                        sb.append("[^").append(pathSeparator).append("]*");
                    }
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
            i++;
        }
        sb.append('$');
        return sb.toString();
    }


    public static boolean isEmpty(String string) {
        return string == null || string.trim().isEmpty();
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

    public static String fillString(char x, int width) {
        char[] cc = new char[width];
        Arrays.fill(cc, x);
        return new String(cc);
    }

    public static String fillString(String pattern, int width) {
        if (pattern == null || pattern.length() == 0) {
            throw new IllegalArgumentException("Empty Pattern");
        }
        char[] cc = new char[width];
        int len = pattern.length();
        for (int i = 0; i < cc.length; i++) {
            cc[i] = pattern.charAt(i % len);
        }
        return new String(cc);
    }

    private static String repeat(char c, int count) {
        char[] a = new char[count];
        Arrays.fill(a, 0, count, c);
        return new String(a);
    }

    private static String repeatV(char c, int count) {
        char[] a = new char[2 * count - 1];
        for (int i = 0; i < count; i += 2) {
            a[i] = c;
            if (i + 1 < a.length) {
                a[i + 1] = '\n';
            }
        }
        return new String(a);
    }

    public static boolean isFilePath(String path) {
        return path != null && path.indexOf('/') >= 0 && !path.contains("://");
    }

//    public static String[] findFilePaths(String path, File cwd, FileFilter fileFilter, boolean error) {
//        File[] files = findFiles(path, cwd, fileFilter, error);
//        String[] strings = new String[files.length];
//        for (int i = 0; i < strings.length; i++) {
//            strings[i] = files[i].getPath();
//        }
//        return strings;
//    }

//    public static File[] findFiles(String path, File cwd, FileFilter fileFilter, boolean error) {
//        File[] all = findFiles(path, cwd, fileFilter);
//        if (all.length == 0) {
//            if (error) {
//                throw new IllegalArgumentException("No file found " + path);
//            } else {
//                return new File[]{new File(path)};
//            }
//        }
//        return all;
//    }

//    public static File[] findFiles(String path, File cwd, FileFilter fileFilter) {
//        File f = getAbsoluteFile(cwd, path);
//        if (f.isAbsolute()) {
//            File f0 = f;
//            while (f0.getParentFile() != null && f0.getParentFile().getParent() != null) {
//                f0 = f0.getParentFile();
//            }
//            if(f.getParent()==null){
//                //this is root
//                return new File[]{f};
//            }
//            return findFiles(f.getPath().substring(f0.getParent().length()), f0.getParent(), cwd, fileFilter);
//        } else {
//            return findFiles(path, ".", cwd, fileFilter);
//        }
//    }

//    public static File[] findFiles(String path, String base, File cwd, final FileFilter fileFilter) {
//        int x = path.indexOf('/');
//        if (x > 0) {
//            String parent = path.substring(0, x);
//            String child = path.substring(x + 1);
//            List<File> all = new ArrayList<>();
//            for (File file : findFiles(parent, base, cwd, fileFilter)) {
//                Collections.addAll(all, findFiles(child, file.getPath(), cwd, fileFilter));
//            }
//            return all.toArray(new File[all.size()]);
//        } else {
//            if (path.contains("*") || path.contains("?")) {
//                final Pattern s = Pattern.compile(simpexpToRegexp(path));
//                File[] files = getAbsoluteFile(cwd, base).listFiles(new FileFilter() {
//                    @Override
//                    public boolean accept(File pathname) {
//                        return (fileFilter == null || fileFilter.accept(pathname))
//                                && s.matcher(pathname.getName()).matches();
//                    }
//                });
//                if (files == null) {
//                    return new File[0];
//                }
//                return files;
//            } else {
//                File f = new File(getAbsolutePath(base), path);
//                if (f.exists()) {
//                    return new File[]{f};
//                }
//                return new File[0];
//            }
//        }
//    }

    public static String simpexpToRegexp(String pattern) {
        return simpexpToRegexp(pattern, false);
    }

    /**
     *
     * GLOB (shell wildcards) to regular expression pattern
     *
     * @param pattern pattern
     * @param contains contains
     * @return regexpr string
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

    public static int readQuotes(char[] chars,int i,StringBuilder v){
        Stack<Character> s=new Stack<Character>();
        s.push(chars[i]);
        int j=0;
        while (i+j < chars.length && !s.isEmpty()) {
            switch (chars[i+j]){
                case '\\':{
                    j++;
                    break;
                }
                case '\"':{
                    if(s.peek().equals('\"')){
                        s.pop();
                    }else {
                        s.push('\"');
                    }
                    break;
                }
                case '\'':{
                    if(s.peek().equals('\'')){
                        s.pop();
                    }else {
                        s.push('\'');
                    }
                    break;
                }
                case '`':{
                    if(s.peek().equals('`')){
                        s.pop();
                    }else {
                        s.push('`');
                    }
                    break;
                }
            }
            v.append(chars[i+j]);
            j++;
        }
        return i;
    }
}

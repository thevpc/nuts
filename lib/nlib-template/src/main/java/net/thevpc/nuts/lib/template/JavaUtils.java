/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.template;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author thevpc
 */
public class JavaUtils {

    public static final Set<String> JAVA_KEYWORDS = new HashSet<>(Arrays.asList(
            "abstract",
            "boolean",
            "byte",
            "case",
            "break",
            "catch",
            "char",
            "class",
            "continue",
            "default",
            "do",
            "double",
            "else",
            "enum",
            "extends",
            "final",
            "finally",
            "float",
            "for",
            "if",
            "implements",
            "import",
            "instanceof",
            "int",
            "interface",
            "long",
            "native",
            "new",
            "null",
            "package",
            "private",
            "protected",
            "public",
            "return",
            "short",
            "static",
            "strictfp",
            "super",
            "switch",
            "synchronized",
            "this",
            "throw",
            "throws",
            "transient",
            "try",
            "void",
            "volatile",
            "while",
            "loop"
    ));

    public static String path(String s) {
        return s.replace('.', '/');
    }

    public static String packageName(String s) {
        return pathToPackage(s);
    }

    public static String pathToPackage(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '-':
                    break;
                case '/':
                case '\\':
                    sb.append(".");
                    break;
                default:
                    sb.append(Character.toLowerCase(c));
                    break;
            }
        }
        StringTokenizer st = new StringTokenizer(sb.toString(), ".", true);
        sb.delete(0, sb.length());
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if (t.equals(".")) {
                sb.append(t);
            } else if (JAVA_KEYWORDS.contains(t)) {
                sb.append("_").append(t);
            } else {
                sb.append(t);
            }
        }
        return sb.toString();
    }

    public static ClassInfo detectedJavaClassInfo(String javaCode) {
        String pack = null;
        String cls = null;
        for (String line : javaCode.split("\n")) {
            line = line.trim();
//            System.out.println(line);
            if (!_StringUtils.isBlank(line)) {
                if (pack == null && _StringUtils.isStartsWithWord(line, "package")) {
                    pack = line.substring("package".length(), line.indexOf(';', "package".length())).trim();
                } else {
                    for (String prefix : new String[]{
                        "public class",
                        "public interface",}) {
                        String rest = null;
                        if ((rest = _StringUtils.consumeWords(line, prefix)) != null && cls == null) {
                            String name = _StringUtils.consumeWord(rest);
                            if (name != null) {
                                cls = name;
                            }
                        }
                    }
                }
            }
            if (pack != null && cls != null) {
                break;
            }
        }
        if (cls != null) {
            return new ClassInfo(cls, pack);
        }
        throw new IllegalArgumentException("Unable to resolve class name");
    }

    public static ClassInfo detectedScalaClassInfo(String javaCode) {
        String pack = null;
        String cls = null;
        for (String line : javaCode.split("\n")) {
            line = line.trim();
//            System.out.println(line);
            if (!_StringUtils.isBlank(line)) {
                if (pack == null && _StringUtils.isStartsWithWord(line, "package")) {
                    pack = line.substring("package".length(), line.length()).trim();
                    if (pack.indexOf(';') >= 0) {
                        pack = line.substring(line.indexOf(';')).trim();
                    }
                } else {
                    for (String prefix : new String[]{
                        "object",}) {
                        String rest = null;
                        if ((rest = _StringUtils.consumeWords(line, prefix)) != null && cls == null) {
                            String name = _StringUtils.consumeWord(rest);
                            if (name != null) {
                                cls = name;
                            }
                        }
                    }
                }
            }
            if (pack != null && cls != null) {
                break;
            }
        }
        if (cls != null) {
            return new ClassInfo(cls, pack);
        }
        throw new IllegalArgumentException("Unable to resolve class name");
    }

    public static String className(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '-') {
                i++;
                sb.append(Character.toUpperCase(s.charAt(i)));
            } else {
                sb.append(c);
            }
        }
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        if (JAVA_KEYWORDS.contains(sb.toString().toLowerCase())) {
            sb.append("_");
        }
        return sb.toString();
    }

    public static String varName(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '-') {
                i++;
                sb.append(Character.toUpperCase(s.charAt(i)));
            } else {
                sb.append(c);
            }
        }
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));

        return sb.toString();
    }

    public static String toIdFormat(String s) {
        StringBuilder sb = new StringBuilder();
        for (String s1 : s.split("[ -_.]")) {
            if (!s1.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("-");
                }
                sb.append(s1.toLowerCase());
            }
        }
        return sb.toString();
    }

    public static String toNameFormat(String s) {
        StringBuilder sb = new StringBuilder();
        for (String s1 : s.split("[ -_.]")) {
            if (!s1.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(Character.toUpperCase(s1.charAt(0)));
                sb.append(s1.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nlog.util;

/**
 *
 * @author vpc
 */
public class StringUtils {

    public static boolean isBlank(String a) {
        return trim(a).isEmpty();
    }

    public static String quote(String a) {
        if (a == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        for (char c : a.toCharArray()) {
            switch (c) {
                case '\n': {
                    sb.append("\\n").append(c);
                }
                case '\r': {
                    sb.append("\\r").append(c);
                }
                case '\f': {
                    sb.append("\\f").append(c);
                }
                case '\\':
                case '\"': {
                    sb.append("\\").append(c);
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    public static String trim(String a) {
        if (a == null) {
            return "";
        }
        return a.trim();
    }
}

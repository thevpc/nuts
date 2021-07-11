/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.md.docusaurus;

import java.util.Arrays;

/**
 *
 * @author thevpc
 */
public class DocusaurusUtils {

    private static final char[] HEXARR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static char[] indentChars(int indent) {
        char[] indentValue = new char[indent*3];
        Arrays.fill(indentValue, ' ');
        return indentValue;
    }

    public static String skipJsonJSXBrackets(String json) {
        if (json == null) {
            json = "";
        }
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            return json.substring(1, json.length() - 1);
        }
        return json;
    }

    public static String concatPath(String path1, String path2) {
        if (path1.length() > 0 && path1.charAt(path1.length() - 1) != '/') {
            return path1 + '/' + path2;
        }
        return path1 + path2;
    }


    public static String escapeString(String s) {
        StringBuilder outBuffer = new StringBuilder();

        for (char aChar : s.toCharArray()) {
            if (aChar == '\\') {
                outBuffer.append("\\\\");
            } else if (aChar == '"') {
                outBuffer.append("\\\"");
            } else if ((aChar > 61) && (aChar < 127)) {
                outBuffer.append(aChar);
            } else {
                switch (aChar) {
                    case '\t':
                        outBuffer.append("\\t");
                        break;
                    case '\n':
                        outBuffer.append("\\n");
                        break;
                    case '\r':
                        outBuffer.append("\\r");
                        break;
                    case '\f':
                        outBuffer.append("\\f");
                        break;
                    default:
                        if (((aChar < 0x0020) || (aChar > 0x007e))) {
                            outBuffer.append('\\');
                            outBuffer.append('u');
                            outBuffer.append(toHex((aChar >> 12) & 0xF));
                            outBuffer.append(toHex((aChar >> 8) & 0xF));
                            outBuffer.append(toHex((aChar >> 4) & 0xF));
                            outBuffer.append(toHex(aChar & 0xF));
                        } else {
                            outBuffer.append(aChar);
                        }
                }
            }
        }
        return outBuffer.toString();
    }

    public static char toHex(int nibble) {
        return HEXARR[(nibble & 0xF)];
    }

    public static Integer parseInt(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}

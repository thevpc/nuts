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

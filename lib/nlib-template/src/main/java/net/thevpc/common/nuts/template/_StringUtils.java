/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.common.nuts.template;

import java.util.Arrays;
import net.thevpc.common.strings.StringUtils;

/**
 *
 * @author thevpc
 */
public class _StringUtils {

//    public static boolean isStartsWithWords(String string, String portions) {
//        return isStartsWithWords(string.split(" "), portions);
//    }
    public static String sortLines(String string) {
        String[] all = string.split("\n");
        Arrays.sort(all);
        return StringUtils.join("\n", all);
    }
    public static boolean isBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    public static String consumeWord(String string) {
        string = string.trim();
        if (string.isEmpty()) {
            return null;
        }
        int i = 0;
        for (i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (i == 0) {
                if (!Character.isJavaIdentifierStart(c)) {
                    break;
                }
            } else {
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
            }
        }
        if (i == 0) {
            return null;
        }
        return string.substring(0, i);
    }

    public static String consumeWords(String string, String... portions) {
        String i = string;
        for (String portion : portions) {
            if (!isStartsWithWord(i, portion)) {
                return null;
            }
            i = i.substring(portion.length()).trim();
        }
        return i;
    }

    public static boolean isStartsWithWords(String string, String... portions) {
        String i = string;
        for (String portion : portions) {
            if (!isStartsWithWord(i, portion)) {
                return false;
            }
            i = i.substring(portion.length()).trim();
        }
        return true;
    }

    public static boolean isStartsWithWord(String string, String portion) {
        return isStartsWithWord(string, portion, 0);
    }

    public static boolean isStartsWithWord(String string, String portion, int pos) {
        if (string.startsWith(portion, pos)) {
            if (string.length() == portion.length()) {
                return true;
            }
            char c = string.charAt(portion.length() + pos);
            if (Character.isJavaIdentifierPart(c)) {
                return false;
            }
            return true;
        }
        return false;
    }

}

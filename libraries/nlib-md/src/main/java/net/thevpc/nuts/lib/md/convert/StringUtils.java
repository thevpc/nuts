/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.md.convert;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author thevpc
 */
public class StringUtils {

    public static String replace(String str, Function<String, String> fct) {
        Pattern p = Pattern.compile("\\$\\{(?<k>[^\\]]*)\\}");
        Matcher matcher = p.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String k = matcher.group("k");
            String v = fct.apply(k);
            if (v == null) {
                throw new IllegalArgumentException("Unknow variable " + k);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(v));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

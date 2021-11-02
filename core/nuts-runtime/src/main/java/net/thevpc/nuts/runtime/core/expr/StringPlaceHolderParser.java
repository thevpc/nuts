package net.thevpc.nuts.runtime.core.expr;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringPlaceHolderParser {

    public static final Pattern DOLLAR_PLACE_HOLDER_PATTERN = Pattern.compile("[$][{](?<name>([^}]+))[}]");

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param s string
     * @param converter converter
     * @return replaced string
     */
    public static String replaceDollarPlaceHolders(String s, Function<String, String> converter) {
        Matcher matcher = DOLLAR_PLACE_HOLDER_PATTERN.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group("name");
            String x = converter == null ? null : converter.apply(name);
            if (x == null) {
                x = "${" + name + "}";
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(x));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

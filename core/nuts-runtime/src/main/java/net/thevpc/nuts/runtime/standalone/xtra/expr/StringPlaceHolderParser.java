package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NutsSession;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringPlaceHolderParser {

    public static final Pattern DOLLAR_PLACE_HOLDER_PATTERN = Pattern.compile("(?<braces>[$][{](?<bname>([^}]+))[}])|(?<dollar>[$](?<dname>[a-zA-Z0-9_-]+))");

    public static <T> String replaceDollarPlaceHolders(String s, T context, NutsSession session, PlaceHolderProvider<T> provider) {
        return replaceDollarPlaceHolders(s, x -> provider.get(x, context, session));
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param s        string
     * @param provider provider
     * @return replaced string
     */
    public static String replaceDollarPlaceHolders(String s, Function<String, String> provider) {
        Matcher matcher = DOLLAR_PLACE_HOLDER_PATTERN.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String bname = matcher.group("bname");
            if (bname != null) {
                int colon = bname.indexOf(':');
                if (colon >= 0) {
                    String n = bname.substring(0, colon);
                    String x = provider == null ? null : provider.apply(n);
                    if (x == null) {
                        x = bname.substring(colon + 1);
                    }
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(x));
                } else {
                    String x = provider == null ? null : provider.apply(bname);
                    if (x == null) {
                        x = "${" + bname + "}";
                    }
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(x));
                }
            } else {
                String dname = matcher.group("dname");
                String x = provider == null ? null : provider.apply(dname);
                if (x == null) {
                    x = "$"+dname;
                }
                matcher.appendReplacement(sb, Matcher.quoteReplacement(x));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public interface PlaceHolderProvider<T> {
        String get(String key, T context, NutsSession session);
    }
}

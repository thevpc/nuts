package net.thevpc.nuts.runtime.standalone.xtra.web;

import net.thevpc.nuts.util.NStringMapFormat;
import net.thevpc.nuts.net.NHttpUrlEncoder;
import net.thevpc.nuts.net.NWebCookie;

import java.util.HashMap;
import java.util.Map;

public class DefaultNWebCookie implements NWebCookie {
    private static final NStringMapFormat COOKIES_PARSER = NStringMapFormat.HTTP_HEADER_FORMAT;
    private String name;
    private String value;
    private Map<String, String> properties = new HashMap<>();

    public DefaultNWebCookie(String expr) {
        int index = expr.indexOf('=');
        name = NHttpUrlEncoder.decode(expr.substring(0, index));
        value = NHttpUrlEncoder.decode(expr.substring(index + 1));
        int pv = value.indexOf(';');
        if (pv > 0) {
            properties = COOKIES_PARSER.parse(value.substring(pv + 1)).get();
            value = value.substring(0, pv);
        }
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public String value() {
        return value;
    }

    public Map<String, String> properties() {
        return properties;
    }

    @Override
    public String domain() {
        return properties.get("path");
    }

    public static String formatCookie(NWebCookie cookie) {
        return NHttpUrlEncoder.encode(cookie.name()) + "=" + NHttpUrlEncoder.encode(cookie.value());
    }
}

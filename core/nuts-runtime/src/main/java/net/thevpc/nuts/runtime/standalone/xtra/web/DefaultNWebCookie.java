package net.thevpc.nuts.runtime.standalone.xtra.web;

import net.thevpc.nuts.util.NStringMapFormat;
import net.thevpc.nuts.web.NHttpUrlEncoder;
import net.thevpc.nuts.web.NWebCookie;

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
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String getDomain() {
        return properties.get("path");
    }

    public static String formatCookie(NWebCookie cookie) {
        return NHttpUrlEncoder.encode(cookie.getName()) + "=" + NHttpUrlEncoder.encode(cookie.getValue());
    }
}

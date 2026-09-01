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
        if (expr != null) {
            int index = expr.indexOf('=');
            if (index >= 0) {
                name = NHttpUrlEncoder.decode(expr.substring(0, index).trim());
                String rest = expr.substring(index + 1);
                int pv = rest.indexOf(';');
                if (pv >= 0) {
                    value = NHttpUrlEncoder.decode(rest.substring(0, pv).trim());
                    properties = COOKIES_PARSER.parse(rest.substring(pv + 1)).get();
                    if (properties == null) {
                        properties = new HashMap<>();
                    }
                } else {
                    value = NHttpUrlEncoder.decode(rest.trim());
                }
            } else {
                name = NHttpUrlEncoder.decode(expr.trim());
                value = "";
            }
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
        if (properties != null) {
            String d = properties.get("domain");
            if (d == null) {
                d = properties.get("Domain");
            }
            return d;
        }
        return null;
    }

    public static String formatCookie(NWebCookie cookie) {
        if (cookie == null) {
            return "";
        }
        return NHttpUrlEncoder.encode(cookie.name() == null ? "" : cookie.name())
                + "="
                + NHttpUrlEncoder.encode(cookie.value() == null ? "" : cookie.value());
    }
}

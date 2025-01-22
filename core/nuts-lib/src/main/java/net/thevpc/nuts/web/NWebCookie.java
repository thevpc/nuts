package net.thevpc.nuts.web;

import java.util.Map;

public interface NWebCookie {
    String getName();
    String getValue();
    String getDomain();
    Map<String,String> getProperties();
}

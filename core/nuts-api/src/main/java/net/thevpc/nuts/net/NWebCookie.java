package net.thevpc.nuts.net;

import java.util.Map;

public interface NWebCookie {
    String getName();
    String getValue();
    String getDomain();
    Map<String,String> getProperties();
}

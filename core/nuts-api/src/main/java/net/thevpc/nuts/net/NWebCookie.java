package net.thevpc.nuts.net;

import java.util.Map;

public interface NWebCookie {
    String name();
    String value();
    String domain();
    Map<String,String> properties();
}

package net.thevpc.nuts.util;

import java.util.List;
import java.util.Map;

public interface NConnexionString extends NImmutable {

    String getUserName();

    String getPassword();


    String getHost();

    NConnexionString getRoot();

    NConnexionString getParent();

    String getPort();

    NOptional<Map<String, List<String>>> getQueryMap();

    String getPath();

    String getProtocol();

    String getQueryString();

    List<String> getNames();

    NConnexionString resolve(String child);

    NConnexionStringBuilder builder();
}

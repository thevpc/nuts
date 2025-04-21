package net.thevpc.nuts.util;

import java.util.List;
import java.util.Map;

public interface NConnexionStringBuilder {

    String getUserName();

    NConnexionStringBuilder setUserName(String userName);

    String getPassword();

    NConnexionStringBuilder setPassword(String password);

    String getHost();

    NConnexionStringBuilder setHost(String host);

    NConnexionStringBuilder getRoot();

    NConnexionStringBuilder getParent();

    String getPort();

    NConnexionStringBuilder setPort(String port);

    NConnexionStringBuilder setQueryMap(Map<String, List<String>> queryMap);

    NOptional<Map<String, List<String>>> getQueryMap();

    String getPath();

    NConnexionStringBuilder setPath(String path);

    String getProtocol();

    NConnexionStringBuilder setProtocol(String protocol);

    String getQueryString();

    NConnexionStringBuilder setQueryString(String queryString);

    NConnexionStringBuilder copy();

    List<String> getNames();

    NConnexionStringBuilder resolve(String child);

    NConnexionString build();
}

package net.thevpc.nuts.util;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.util.List;
import java.util.Map;

public interface NConnexionStringBuilder extends NComponent {
    static NOptional<NConnexionStringBuilder> get(String expression) {
        return NExtensions.of(NConnexionStringBuilderFactory.class)
                .create(expression);
    }

    static NConnexionStringBuilder of(String expression) {
        return get(expression).get();
    }

    static NConnexionStringBuilder of() {
        return NExtensions.of(NConnexionStringBuilderFactory.class)
                .create();
    }

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

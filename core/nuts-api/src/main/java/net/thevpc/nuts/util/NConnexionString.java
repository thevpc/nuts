package net.thevpc.nuts.util;

import net.thevpc.nuts.ext.NExtensions;

import java.util.List;
import java.util.Map;

public interface NConnexionString extends NImmutable {
    static NOptional<NConnexionString> get(String expression) {
        return NExtensions.of(NConnexionStringBuilderFactory.class)
                .create(expression).map(NConnexionStringBuilder::build);
    }

    static NConnexionString of(String expression) {
        return get(expression).get();
    }

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

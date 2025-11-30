package net.thevpc.nuts.net;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.util.NImmutable;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Map;

public interface NConnectionString extends NImmutable {
    static NOptional<NConnectionString> get(String expression) {
        return NExtensions.of(NConnectionStringBuilderFactory.class)
                .create(expression).map(NConnectionStringBuilder::build);
    }

    static NConnectionString of(String expression) {
        return get(expression).get();
    }

    String getUserName();

    String getPassword();

    String getHost();

    NConnectionString getRoot();

    NConnectionString getParent();

    String getPort();

    NOptional<Map<String, List<String>>> getQueryMap();

    String getPath();

    String getProtocol();

    String getQueryString();

    List<String> getNames();

    NConnectionString resolve(String child);

    NConnectionStringBuilder builder();

    NConnectionString withPath(String path);

    NConnectionString normalize();
}

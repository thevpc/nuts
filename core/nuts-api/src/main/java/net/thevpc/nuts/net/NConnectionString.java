package net.thevpc.nuts.net;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Map;

public interface NConnectionString  {
    static NOptional<NConnectionString> get(String expression) {
        return NExtensions.of(NConnectionStringBuilderFactory.class)
                .create(expression).map(NConnectionStringBuilder::build);
    }

    static NConnectionString of(String expression) {
        return get(expression).get();
    }

    @NGetter
    String userName();

    @NGetter
    String password();

    @NGetter
    String host();

    @NGetter
    NConnectionString root();

    @NGetter
    NConnectionString parent();

    @NGetter
    String port();

    @NGetter
    NOptional<Map<String, List<String>>> queryMap();

    @NGetter
    String path();

    @NGetter
    String protocol();

    @NGetter
    String queryString();

    @NGetter
    List<String> names();

    NConnectionString resolve(String child);

    NConnectionStringBuilder builder();

    NConnectionString withPath(String path);

    NConnectionString normalize();
}

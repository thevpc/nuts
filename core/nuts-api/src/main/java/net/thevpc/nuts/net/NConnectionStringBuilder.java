package net.thevpc.nuts.net;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSetter;

import java.util.List;
import java.util.Map;

public interface NConnectionStringBuilder extends NComponent {
    static NOptional<NConnectionStringBuilder> get(String expression) {
        return NExtensions.of(NConnectionStringBuilderFactory.class)
                .create(expression);
    }

    static NConnectionStringBuilder of(String expression) {
        return get(expression).get();
    }

    static NConnectionStringBuilder of() {
        return NExtensions.of(NConnectionStringBuilderFactory.class)
                .create();
    }

    String userName();

    @NSetter
    NConnectionStringBuilder userName(String userName);

    String password();

    @NSetter
    NConnectionStringBuilder password(String password);

    String host();

    @NSetter
    NConnectionStringBuilder host(String host);

    NConnectionStringBuilder root();

    NConnectionStringBuilder parent();

    String port();

    @NSetter
    NConnectionStringBuilder port(String port);

    @NSetter
    NConnectionStringBuilder queryMap(Map<String, List<String>> queryMap);

    NOptional<Map<String, List<String>>> queryMap();

    String path();

    @NSetter
    NConnectionStringBuilder path(String path);

    String protocol();

    @NSetter
    NConnectionStringBuilder protocol(String protocol);

    String queryString();

    @NSetter
    NConnectionStringBuilder queryString(String queryString);

    /**
     * @since 0.8.9
     */
    NConnectionStringBuilder setQueryParam(String param, String value);

    /**
     * @since 0.8.9
     */
    NConnectionStringBuilder addQueryParam(String param, String value);

    /**
     *
     * @since  0.8.9
     */
    NConnectionStringBuilder addUniqueQueryParam(String param, String value);

    /**
     * @since 0.8.9
     */
    NConnectionStringBuilder clearQueryParam(String param);

    /**
     *
     * @since  0.8.9
     */
    NOptional<String> getQueryParam(String param);

    /**
     *
     * @since  0.8.9
     */
    List<String> getQueryParams(String param);


    /**
     *
     * @since  0.8.9
     */
    @NSetter
    NConnectionStringBuilder normalized(boolean normalized);

    /**
     *
     * @since  0.8.9
     */
    boolean isNormalized();

    NConnectionStringBuilder copy();

    List<String> names();

    NConnectionStringBuilder resolve(String child);

    NConnectionString build();

}

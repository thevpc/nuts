package net.thevpc.nuts.net;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

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

    String getUserName();

    NConnectionStringBuilder setUserName(String userName);

    String getPassword();

    NConnectionStringBuilder setPassword(String password);

    String getHost();

    NConnectionStringBuilder setHost(String host);

    NConnectionStringBuilder getRoot();

    NConnectionStringBuilder getParent();

    String getPort();

    NConnectionStringBuilder setPort(String port);

    NConnectionStringBuilder setQueryMap(Map<String, List<String>> queryMap);

    NOptional<Map<String, List<String>>> getQueryMap();

    String getPath();

    NConnectionStringBuilder setPath(String path);

    String getProtocol();

    NConnectionStringBuilder setProtocol(String protocol);

    String getQueryString();

    NConnectionStringBuilder setQueryString(String queryString);

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
    NConnectionStringBuilder setNormalized(boolean normalized);

    /**
     *
     * @since  0.8.9
     */
    boolean isNormalized();

    NConnectionStringBuilder copy();

    List<String> getNames();

    NConnectionStringBuilder resolve(String child);

    NConnectionString build();

}

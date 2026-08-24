package net.thevpc.nuts.net;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSetter;

import java.util.List;
import java.util.Map;

/**
 * NConnectionStringBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NConnectionStringBuilder extends NComponent {
    /**
     * Returns the get.
     *
     * @param expression expression
     * @return get result
     */
    static NOptional<NConnectionStringBuilder> get(String expression) {
        return NExtensions.of(NConnectionStringBuilderFactory.class)
                .create(expression);
    }

    /**
     * Creates a new instance of of.
     *
     * @param expression expression
     * @return of result
     */
    static NConnectionStringBuilder of(String expression) {
        /**
         * Returns the get.
         *
         * @param expression).get( expression).get(
         * @return get result
         */
        return get(expression).get();
    }

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NConnectionStringBuilder of() {
        return NExtensions.of(NConnectionStringBuilderFactory.class)
                .create();
    }

    /**
     * User name.
     *
     * @return user name result
     */
    String userName();

    /**
     * User name.
     *
     * @param userName user name
     * @return user name result
     */
    @NSetter
    NConnectionStringBuilder userName(String userName);

    /**
     * Password.
     *
     * @return password result
     */
    String password();

    /**
     * Password.
     *
     * @param password password
     * @return password result
     */
    @NSetter
    NConnectionStringBuilder password(String password);

    /**
     * Host.
     *
     * @return host result
     */
    String host();

    /**
     * Host.
     *
     * @param host host
     * @return host result
     */
    @NSetter
    NConnectionStringBuilder host(String host);

    /**
     * Root.
     *
     * @return root result
     */
    NConnectionStringBuilder root();

    /**
     * Parent.
     *
     * @return parent result
     */
    NConnectionStringBuilder parent();

    /**
     * Port.
     *
     * @return port result
     */
    String port();

    /**
     * Port.
     *
     * @param port port
     * @return port result
     */
    @NSetter
    NConnectionStringBuilder port(String port);

    /**
     * Query map.
     *
     * @param queryMap query map
     * @return query map result
     */
    @NSetter
    NConnectionStringBuilder queryMap(Map<String, List<String>> queryMap);

    /**
     * Query map.
     *
     * @return query map result
     */
    NOptional<Map<String, List<String>>> queryMap();

    /**
     * Path.
     *
     * @return path result
     */
    String path();

    /**
     * Path.
     *
     * @param path path
     * @return path result
     */
    @NSetter
    NConnectionStringBuilder path(String path);

    /**
     * Protocol.
     *
     * @return protocol result
     */
    String protocol();

    /**
     * Protocol.
     *
     * @param protocol protocol
     * @return protocol result
     */
    @NSetter
    NConnectionStringBuilder protocol(String protocol);

    /**
     * Query string.
     *
     * @return query string result
     */
    String queryString();

    /**
     * Query string.
     *
     * @param queryString query string
     * @return query string result
     */
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

    /**
     * Copy.
     *
     * @return copy result
     */
    NConnectionStringBuilder copy();

    /**
     * Names.
     *
     * @return names result
     */
    List<String> names();

    /**
     * Resolve.
     *
     * @param child child
     * @return resolve result
     */
    NConnectionStringBuilder resolve(String child);

    /**
     * Build.
     *
     * @return build result
     */
    NConnectionString build();

}

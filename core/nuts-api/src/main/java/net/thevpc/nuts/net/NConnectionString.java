package net.thevpc.nuts.net;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Map;

/**
 * NConnectionString interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NConnectionString  {
    /**
     * Returns the get.
     *
     * @param expression expression
     * @return get result
     */
    static NOptional<NConnectionString> get(String expression) {
        return NExtensions.of(NConnectionStringBuilderFactory.class)
                .create(expression).map(NConnectionStringBuilder::build);
    }

    /**
     * Creates a new instance of of.
     *
     * @param expression expression
     * @return of result
     */
    static NConnectionString of(String expression) {
        /**
         * Returns the get.
         *
         * @param expression).get( expression).get(
         * @return get result
         */
        return get(expression).get();
    }

    /**
     * User name.
     *
     * @return user name result
     */
    @NGetter
    String userName();

    /**
     * Password.
     *
     * @return password result
     */
    @NGetter
    String password();

    /**
     * Host.
     *
     * @return host result
     */
    @NGetter
    String host();

    /**
     * Root.
     *
     * @return root result
     */
    @NGetter
    NConnectionString root();

    /**
     * Parent.
     *
     * @return parent result
     */
    @NGetter
    NConnectionString parent();

    /**
     * Port.
     *
     * @return port result
     */
    @NGetter
    String port();

    /**
     * Query map.
     *
     * @return query map result
     */
    @NGetter
    NOptional<Map<String, List<String>>> queryMap();

    /**
     * Path.
     *
     * @return path result
     */
    @NGetter
    String path();

    /**
     * Protocol.
     *
     * @return protocol result
     */
    @NGetter
    String protocol();

    /**
     * Query string.
     *
     * @return query string result
     */
    @NGetter
    String queryString();

    /**
     * Names.
     *
     * @return names result
     */
    @NGetter
    List<String> names();

    /**
     * Resolve.
     *
     * @param child child
     * @return resolve result
     */
    NConnectionString resolve(String child);

    /**
     * Builder.
     *
     * @return builder result
     */
    NConnectionStringBuilder builder();

    /**
     * With path.
     *
     * @param path path
     * @return with path result
     */
    NConnectionString withPath(String path);

    /**
     * Normalize.
     *
     * @return normalize result
     */
    NConnectionString normalize();
}

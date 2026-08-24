package net.thevpc.nuts.io;

import net.thevpc.nuts.collections.NMaps;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * NPathCredentialsOption class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NPathCredentialsOption implements NPathOption {
    private final String scheme; // e.g., "Basic", "Bearer", "OIDC", "APIKey"
    private final Map<String, String> attributes;

    /**
     * Creates a new instance of of.
     *
     * @param userName user name
     * @param secret secret
     * @return of result
     */
    public static NPathCredentialsOption of(String userName, String secret) {
        return new NPathCredentialsOption("DEFAULT", NMaps.of("userName", userName, "secret", secret));
    }

    /**
     * Creates a new instance of of http basic.
     *
     * @param userName user name
     * @param secret secret
     * @return of http basic result
     */
    public static NPathCredentialsOption ofHttpBasic(String userName, String secret) {
        return new NPathCredentialsOption("BASIC", NMaps.of("userName", userName, "secret", secret));
    }

    /**
     * Creates a new instance of of http bearer.
     *
     * @param secret secret
     * @return of http bearer result
     */
    public static NPathCredentialsOption ofHttpBearer(String secret) {
        return new NPathCredentialsOption("BEARER", NMaps.of("secret", secret));
    }

    /**
     * N path credentials option.
     *
     * @param scheme scheme
     * @param attributes attributes
     * @return n path credentials option result
     */
    public NPathCredentialsOption(String scheme, Map<String, String> attributes) {
        this.scheme = scheme==null?"":NStringUtils.strip(scheme.toUpperCase());
        LinkedHashMap<String, String> c = new LinkedHashMap<>();
        if (attributes != null) {
            for (Map.Entry<String, String> e : attributes.entrySet()) {
                String key = e.getKey();
                String value = e.getValue();
                if (key != null && value != null) {
                    c.put(NStringUtils.strip(key), value);
                }
            }
        }
        this.attributes = Collections.unmodifiableMap(c);
    }

    /**
     * Scheme.
     *
     * @return scheme result
     */
    public String scheme() {
        return scheme;
    }

    /**
     * Attributes.
     *
     * @return attributes result
     */
    public Map<String, String> attributes() {
        return attributes;
    }

    /**
     * Secret.
     *
     * @return secret result
     */
    public NOptional<String> secret() {
        return NOptional.ofNullable(attributes.get("secret"));
    }

    /**
     * User name.
     *
     * @return user name result
     */
    public NOptional<String> userName() {
        return NOptional.ofNullable(attributes.get("userName"));
    }

    @Override
    public String toString() {
        return "NPathCredentialsOption{" +
                "scheme='" + scheme + '\'' +
                ", attributes=[MASKED]}";
    }
}

package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class NPathCredentialsOption implements NPathOption {
    private final String scheme; // e.g., "Basic", "Bearer", "OIDC", "APIKey"
    private final Map<String, String> attributes;

    public static NPathCredentialsOption of(String userName, String secret) {
        return new NPathCredentialsOption("DEFAULT", NMaps.of("userName", userName, "secret", secret));
    }

    public static NPathCredentialsOption ofHttpBasic(String userName, String secret) {
        return new NPathCredentialsOption("BASIC", NMaps.of("userName", userName, "secret", secret));
    }

    public static NPathCredentialsOption ofHttpBearer(String secret) {
        return new NPathCredentialsOption("BEARER", NMaps.of("secret", secret));
    }

    public NPathCredentialsOption(String scheme, Map<String, String> attributes) {
        this.scheme = scheme==null?null:NStringUtils.trimToNull(scheme.toUpperCase());
        LinkedHashMap<String, String> c = new LinkedHashMap<>();
        if (attributes != null) {
            for (Map.Entry<String, String> e : attributes.entrySet()) {
                String key = e.getKey();
                String value = e.getValue();
                if (key != null && value != null) {
                    c.put(NStringUtils.trim(key), value);
                }
            }
        }
        this.attributes = Collections.unmodifiableMap(c);
    }

    public String scheme() {
        return scheme;
    }

    public Map<String, String> attributes() {
        return attributes;
    }

    public NOptional<String> secret() {
        return NOptional.ofNullable(attributes.get("secret"));
    }

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

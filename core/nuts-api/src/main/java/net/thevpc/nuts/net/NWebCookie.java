package net.thevpc.nuts.net;

import java.util.Map;

/**
 * NWebCookie interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NWebCookie {
    /**
     * Name.
     *
     * @return name result
     */
    String name();
    /**
     * Value.
     *
     * @return value result
     */
    String value();
    /**
     * Domain.
     *
     * @return domain result
     */
    String domain();
    /**
     * Properties.
     *
     * @return properties result
     */
    Map<String,String> properties();
}

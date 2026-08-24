package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

/**
 * NArgValue interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NArgValue<T> {
    /**
     * Key.
     *
     * @return key result
     */
    String key();
    /**
     * Value.
     *
     * @return value result
     */
    T value();
    /**
     * Boolean value.
     *
     * @return boolean value result
     */
    boolean booleanValue();
    /**
     * String value.
     *
     * @return string value result
     */
    String stringValue();
    /**
     * Optional value.
     *
     * @return optional value result
     */
    NOptional<T> optionalValue();
    /**
     * Literal.
     *
     * @return literal result
     */
    NLiteral literal();
}

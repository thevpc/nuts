package net.thevpc.nuts.elem;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * NNumberElement interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NNumberElement extends NPrimitiveElement {
    /**
     * Image.
     *
     * @return image result
     */
    String image();
    /**
     * Number value.
     *
     * @return number value result
     */
    Number numberValue();

    /**
     * Big decimal value.
     *
     * @return big decimal value result
     */
    BigDecimal bigDecimalValue();

    /**
     * Big int value.
     *
     * @return big int value result
     */
    BigInteger bigIntValue();

    /**
     * Number layout.
     *
     * @return number layout result
     */
    NNumberLayout numberLayout();

    /**
     * Number suffix.
     *
     * @return number suffix result
     */
    String numberSuffix();
}

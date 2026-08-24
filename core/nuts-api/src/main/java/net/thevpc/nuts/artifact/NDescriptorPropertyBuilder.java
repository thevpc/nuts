package net.thevpc.nuts.artifact;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NSetter;

/**
 * NDescriptorPropertyBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDescriptorPropertyBuilder extends NBlankable {
    /**
     * property name
     *
     * @return property name
     */
    @NGetter
    String name();

    /**
     * property value
     *
     * @return property value
     */
    @NGetter
    NLiteral value();

    /**
     * property condition
     *
     * @return property condition
     */
    @NGetter
    NEnvConditionBuilder condition();

    /**
     * Condition.
     *
     * @param condition condition
     * @return condition result
     */
    @NSetter
    NDescriptorPropertyBuilder condition(NEnvCondition condition);

    /**
     * Condition.
     *
     * @param condition condition
     * @return condition result
     */
    NDescriptorPropertyBuilder condition(NEnvConditionBuilder condition);

    /**
     * Name.
     *
     * @param name name
     * @return name result
     */
    @NSetter
    NDescriptorPropertyBuilder name(String name);

    /**
     * Value.
     *
     * @param value value
     * @return value result
     */
    @NSetter
    NDescriptorPropertyBuilder value(String value);

    /**
     * Copy from.
     *
     * @param value value
     * @return copy from result
     */
    NDescriptorPropertyBuilder copyFrom(NDescriptorProperty value);

    /**
     * Copy from.
     *
     * @param value value
     * @return copy from result
     */
    NDescriptorPropertyBuilder copyFrom(NDescriptorPropertyBuilder value);

    /**
     * Build.
     *
     * @return build result
     */
    NDescriptorProperty build();

    /**
     * Copy.
     *
     * @return copy result
     */
    NDescriptorPropertyBuilder copy();
}

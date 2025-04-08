package net.thevpc.nuts;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;

public interface NDescriptorPropertyBuilder extends NBlankable {
    /**
     * property name
     *
     * @return property name
     */
    String getName();

    /**
     * property value
     *
     * @return property value
     */
    NLiteral getValue();

    /**
     * property condition
     *
     * @return property condition
     */
    NEnvConditionBuilder getCondition();

    NDescriptorPropertyBuilder setCondition(NEnvCondition condition);

    NDescriptorPropertyBuilder setCondition(NEnvConditionBuilder condition);

    NDescriptorPropertyBuilder setName(String name);

    NDescriptorPropertyBuilder setValue(String value);

    NDescriptorPropertyBuilder copyFrom(NDescriptorProperty value);

    NDescriptorPropertyBuilder copyFrom(NDescriptorPropertyBuilder value);

    NDescriptorProperty build();

    NDescriptorPropertyBuilder copy();
}

package net.thevpc.nuts.artifact;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NSetter;

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

    @NSetter
    NDescriptorPropertyBuilder condition(NEnvCondition condition);

    NDescriptorPropertyBuilder condition(NEnvConditionBuilder condition);

    @NSetter
    NDescriptorPropertyBuilder name(String name);

    @NSetter
    NDescriptorPropertyBuilder value(String value);

    NDescriptorPropertyBuilder copyFrom(NDescriptorProperty value);

    NDescriptorPropertyBuilder copyFrom(NDescriptorPropertyBuilder value);

    NDescriptorProperty build();

    NDescriptorPropertyBuilder copy();
}

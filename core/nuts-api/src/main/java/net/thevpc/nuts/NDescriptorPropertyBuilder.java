package net.thevpc.nuts;

import net.thevpc.nuts.env.NEnvCondition;

public interface NDescriptorPropertyBuilder extends NDescriptorProperty {
    NDescriptorPropertyBuilder setCondition(NEnvCondition condition);

    NDescriptorPropertyBuilder setName(String name);

    NDescriptorPropertyBuilder setValue(String value);

    NDescriptorPropertyBuilder setAll(NDescriptorProperty value);

    NDescriptorProperty build();
    NDescriptorPropertyBuilder copy();
}

package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

public interface NutsDescriptorPropertyBuilder {
    static NutsDescriptorPropertyBuilder of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().descriptor().propertyBuilder();
    }

    String getName();

    NutsDescriptorPropertyBuilder setName(String name);

    String getValue();

    NutsDescriptorPropertyBuilder setValue(String value);

    NutsEnvConditionBuilder getCondition();

    NutsDescriptorPropertyBuilder setCondition(NutsEnvConditionBuilder condition);

    NutsDescriptorPropertyBuilder setCondition(NutsEnvCondition condition);

    NutsDescriptorPropertyBuilder setAll(NutsDescriptorPropertyBuilder value);

    NutsDescriptorPropertyBuilder setAll(NutsDescriptorProperty value);

    NutsDescriptorProperty build();

}

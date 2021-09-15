package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

public interface NutsDescriptorPropertyBuilder {
    static NutsDescriptorPropertyBuilder of(NutsSession session){
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().descriptor().propertyBuilder();
    }

    String getName();

    String getValue();

    NutsDescriptorPropertyBuilder setCondition(NutsEnvConditionBuilder condition);

    NutsDescriptorPropertyBuilder setCondition(NutsEnvCondition condition);

    NutsEnvConditionBuilder getCondition();

    NutsDescriptorPropertyBuilder setName(String name);

    NutsDescriptorPropertyBuilder setValue(String value);

    NutsDescriptorPropertyBuilder setAll(NutsDescriptorPropertyBuilder value);

    NutsDescriptorPropertyBuilder setAll(NutsDescriptorProperty value);

    NutsDescriptorProperty build();

}

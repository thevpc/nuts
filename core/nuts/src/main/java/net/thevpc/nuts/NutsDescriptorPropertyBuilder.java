package net.thevpc.nuts;

public interface NutsDescriptorPropertyBuilder {
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

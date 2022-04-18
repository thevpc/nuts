package net.thevpc.nuts;

public interface NutsDescriptorPropertyBuilder extends NutsDescriptorProperty {
    NutsDescriptorPropertyBuilder setCondition(NutsEnvCondition condition);

    NutsDescriptorPropertyBuilder setName(String name);

    NutsDescriptorPropertyBuilder setValue(String value);

    NutsDescriptorPropertyBuilder setAll(NutsDescriptorProperty value);

    NutsDescriptorProperty build();
    NutsDescriptorPropertyBuilder copy();
}

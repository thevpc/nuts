package net.thevpc.nuts;

public interface NutsDescriptorProperty {
    String getName();

    String getValue();

    NutsEnvCondition getCondition();

    NutsDescriptorPropertyBuilder builder();
}

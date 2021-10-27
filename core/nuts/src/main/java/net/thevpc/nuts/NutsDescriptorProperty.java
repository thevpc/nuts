package net.thevpc.nuts;

public interface NutsDescriptorProperty extends NutsBlankable{
    String getName();

    String getValue();

    NutsEnvCondition getCondition();

    NutsDescriptorPropertyBuilder builder();
}

package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;

public interface NutsDescriptorPropertyBuilder extends NutsComponent {

    static NutsDescriptorPropertyBuilder of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsDescriptorPropertyBuilder.class,true,null);
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

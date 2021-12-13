package net.thevpc.nuts.runtime.standalone.descriptor.util;

import net.thevpc.nuts.NutsDescriptorProperty;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;

import java.util.LinkedHashMap;
import java.util.Map;

public class NutsDescriptorUtils {
    public static Map<String, String> getPropertiesMap(NutsDescriptorProperty[] list, NutsSession session) {
        Map<String, String> m = new LinkedHashMap<>();
        if (list != null) {
            for (NutsDescriptorProperty property : list) {
                if (property.getCondition() == null || property.getCondition().isBlank()) {
                    m.put(property.getName(), property.getValue());
                } else {
                    throw new NutsIllegalArgumentException(session, NutsMessage.plain("unexpected properties with conditions. probably a bug"));
                }
            }
        }
        return m;
    }
}

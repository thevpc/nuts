package net.vpc.app.nuts.core.util;

import net.vpc.app.nuts.NutsObjectConverter;
import net.vpc.common.strings.StringConverter;

public class NutsObjectConverterUtilAdapter implements NutsObjectConverter<String, String> {
    private final StringConverter map;

    public NutsObjectConverterUtilAdapter(StringConverter map) {
        this.map = map;
    }

    @Override
    public String convert(String from) {
        return map.convert(from);
    }
}

package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import java.util.HashMap;
import java.util.Map;

class NanoDBSerializerForEnumByOrdinal extends NanoDBNonNullSerializer<Enum> {
    Map<Class, Object[]> constants = new HashMap<>();

    public NanoDBSerializerForEnumByOrdinal() {
        super(Enum.class);
    }

    @Override
    public void write(Enum obj, NanoDBOutputStream out) {
        out.writeInt(obj.ordinal());
    }

    @Override
    public Enum read(NanoDBInputStream in, Class expectedType) {
        int o = in.readInt();
        Object[] enumConstants = constants.get(expectedType);
        if (enumConstants == null) {
            enumConstants = expectedType.getEnumConstants();
            constants.put(expectedType, enumConstants);
        }
        if (o >= 0 && o <= enumConstants.length) {
            return (Enum) enumConstants[o];
        }
        return null;
    }
}

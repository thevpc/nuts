package net.thevpc.nuts.runtime.standalone.format.desc;

import net.thevpc.nuts.elem.*;

public class NEDescHelper {

    public static NElement addProperty(NElement e, String name, boolean value) {
        return addProperty(e, name, NElements.of().ofBoolean(value));
    }

    public static NElement addProperty(NElement e, String name, String value) {
        return addProperty(e, name, NElements.of().ofString(value));
    }

    public static NElement addProperty(NElement e, String name, int value) {
        return addProperty(e, name, NElements.of().ofInt(value));
    }

    public static NElement addProperty(NElement e, String name, NElement value) {
        if (e == null) {
            return NElements.of().ofObjectBuilder().set(name, value).build();
        } else {
            NObjectElementBuilder ee = e.toObject().get().builder();
            ee.add(name, value);
            return ee.build();
        }
    }
}

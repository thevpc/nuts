package net.thevpc.nuts.runtime.standalone.format.desc;

import net.thevpc.nuts.elem.*;

public class NEDescHelper {

    public static NElement addProperty(NElement e, String name, boolean value) {
        return addProperty(e, name, NElement.ofBoolean(value));
    }

    public static NElement addProperty(NElement e, String name, String value) {
        return addProperty(e, name, NElement.ofString(value));
    }

    public static NElement addProperty(NElement e, String name, int value) {
        return addProperty(e, name, NElement.ofInt(value));
    }

    public static NElement addProperty(NElement e, String name, NElement value) {
        if (e == null) {
            return NElement.ofObjectBuilder().set(name, value).build();
        } else {
            NObjectElementBuilder ee = e.toObject().get().builder();
            ee.add(name, value);
            return ee.build();
        }
    }
}

package net.thevpc.nuts.runtime.standalone.io.ask;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.io.NAsk;
import net.thevpc.nuts.io.NAskFormat;
import net.thevpc.nuts.util.NUnsupportedArgumentException;

import java.util.Arrays;
import java.util.List;

public class DefaultNAskFormat<T> implements NAskFormat<T> {


    public DefaultNAskFormat() {
    }

    @Override
    public List<Object> getDefaultValues(Class type, NAsk<T> question) {
        if (type.isEnum()) {
            return Arrays.asList(type.getEnumConstants());
        }
        switch (type.getName()) {
            case "java.lang.String": {
                return null;
            }
            case "int":
            case "java.lang.Integer": {
                return null;
            }
            case "long":
            case "java.lang.Long": {
                return null;
            }
            case "float":
            case "java.lang.Float": {
                return null;
            }
            case "double":
            case "java.lang.Double": {
                return null;
            }
            case "boolean":
            case "java.lang.Boolean": {
                return Arrays.asList(true, false);
            }
            case "[C": {
                return null;
            }
            default: {
                throw new NUnsupportedArgumentException(NMsg.ofC("unsupported type %s", type.getName()));
            }
        }
    }

    @Override
    public String format(Object value, NAsk<T> question) {
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "y" : "n";
        }
        return String.valueOf(value);
    }

}

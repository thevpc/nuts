package net.thevpc.nuts.runtime.standalone.io.ask;

import net.thevpc.nuts.*;

import java.util.Arrays;
import java.util.List;

public class DefaultNutsQuestionFormat<T> implements NutsQuestionFormat<T> {

    private NutsSession session;

    public DefaultNutsQuestionFormat(NutsSession session) {
        this.session = session;
    }

    @Override
    public List<Object> getDefaultValues(Class type, NutsQuestion<T> question) {
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
            default: {
                throw new NutsUnsupportedArgumentException(session, NutsMessage.cstyle("unsupported type %s", type.getName()));
            }
        }
    }

    @Override
    public String format(Object value, NutsQuestion<T> question) {
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "y" : "n";
        }
        return String.valueOf(value);
    }

}

package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsQuestion;
import net.thevpc.nuts.NutsQuestionFormat;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsUnsupportedArgumentException;

public class DefaultNutsQuestionFormat<T> implements NutsQuestionFormat<T> {

    private NutsSession session;

    public DefaultNutsQuestionFormat(NutsSession session) {
        this.session = session;
    }

    @Override
    public Object[] getDefaultValues(Class type, NutsQuestion<T> question) {
        if (type.isEnum()) {
            return type.getEnumConstants();
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
                return new Object[]{true, false};
            }
            default: {
                throw new NutsUnsupportedArgumentException(session, "Unsupported type " + type.getName());
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

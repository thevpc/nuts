package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

public class DefaultNutsQuestionFormat<T> implements NutsQuestionFormat<T> {

    private NutsWorkspace ws;

    public DefaultNutsQuestionFormat(NutsWorkspace ws) {
        this.ws = ws;
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
                throw new NutsUnsupportedArgumentException(ws, "Unsupported type " + type.getName());
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

package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.app.DefaultNutsArgument;

public class DefaultNutsResponseParser implements NutsResponseParser {

//    public static final NutsResponseParser INSTANCE = new DefaultNutsResponseParser();
    private NutsWorkspace ws;

    public DefaultNutsResponseParser(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public Object parse(Object response, Class type) {
        if (response == null) {
            return null;
        }
        if (type.isInstance(response)) {
            return response;
        }
        if (type.isEnum()) {
            String s = String.valueOf(response).trim();
            if (s.isEmpty()) {
                return null;
            }
            try {
                return Enum.valueOf(type, s);
            } catch (Exception ex) {
                for (Object enumConstant : type.getEnumConstants()) {
                    if (enumConstant.toString().equalsIgnoreCase(s.replace("-", "_"))) {
                        return enumConstant;
                    }
                }
                throw ex;
            }
        }
        switch (type.getName()) {
            case "java.lang.String": {
                return String.valueOf(response);
            }
            case "int":
            case "java.lang.Integer": {
                return Integer.parseInt(String.valueOf(response));
            }
            case "long":
            case "java.lang.Long": {
                return Long.parseLong(String.valueOf(response));
            }
            case "float":
            case "java.lang.Float": {
                return Float.parseFloat(String.valueOf(response));
            }
            case "double":
            case "java.lang.Double": {
                return Double.parseDouble(String.valueOf(response));
            }
            case "boolean":
            case "java.lang.Boolean": {
                if (!(response instanceof String)) {
                    response = String.valueOf(response);
                }
                String sReponse = response.toString();
                NutsArgument a = new DefaultNutsArgument(sReponse, '=');
                if (!a.isBoolean()) {
                    throw new NutsIllegalArgumentException(ws, "Invalid response " + sReponse);
                }
                return a.getBoolean();
            }

            default: {
                throw new NutsUnsupportedArgumentException(ws, "Unsupported type " + type.getName());
            }
        }
    }

    @Override
    public Object[] getDefaultAcceptedValues(Class type) {
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
    public String format(Object value) {
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "y" : "n";
        }
        return String.valueOf(value);
    }

}

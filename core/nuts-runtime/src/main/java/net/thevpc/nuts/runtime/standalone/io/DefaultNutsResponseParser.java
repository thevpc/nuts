package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.app.DefaultNutsArgument;

public class DefaultNutsResponseParser<T> implements NutsQuestionParser<T> {

//    public static final NutsResponseFormat INSTANCE = new DefaultNutsResponseFormat();
    private final NutsWorkspace ws;
    private final Class<T> type;

    public DefaultNutsResponseParser(NutsWorkspace ws, Class<T> type) {
        this.ws = ws;
        this.type = type;
    }

    @Override
    public T parse(Object response, T defaultValue, NutsQuestion<T> question) {
        if (response == null || ((response instanceof String) && response.toString().length() == 0)) {
            response = defaultValue;
        }
        if ("cancel!".equals(response)) {
            throw new NutsUserCancelException(ws);
        }
        if (response == null) {
            return null;
        }
        if (type.isInstance(response)) {
            return (T) response;
        }
        if (type.isEnum()) {
            String s = String.valueOf(response).trim();
            if (s.isEmpty()) {
                return null;
            }
            try {
                return (T) Enum.valueOf((Class) type, s);
            } catch (Exception ex) {
                for (Object enumConstant : type.getEnumConstants()) {
                    if (enumConstant.toString().equalsIgnoreCase(s.replace("-", "_"))) {
                        return (T) enumConstant;
                    }
                }
                throw ex;
            }
        }
        switch (type.getName()) {
            case "java.lang.String": {
                return (T) (Object) String.valueOf(response);
            }
            case "int":
            case "java.lang.Integer": {
                return (T) (Object) Integer.parseInt(String.valueOf(response));
            }
            case "long":
            case "java.lang.Long": {
                return (T) (Object) Long.parseLong(String.valueOf(response));
            }
            case "float":
            case "java.lang.Float": {
                return (T) (Object) Float.parseFloat(String.valueOf(response));
            }
            case "double":
            case "java.lang.Double": {
                return (T) (Object) Double.parseDouble(String.valueOf(response));
            }
            case "boolean":
            case "java.lang.Boolean": {
                if (!(response instanceof String)) {
                    response = String.valueOf(response);
                }
                String sReponse = response.toString();
                NutsArgument a = new DefaultNutsArgument(sReponse);
                if (!a.isBoolean()) {
                    throw new NutsIllegalArgumentException(ws, "invalid response " + sReponse);
                }
                return (T) (Object) a.getBoolean();
            }

            default: {
                throw new NutsUnsupportedArgumentException(ws, "unsupported type " + type.getName());
            }
        }
    }
}

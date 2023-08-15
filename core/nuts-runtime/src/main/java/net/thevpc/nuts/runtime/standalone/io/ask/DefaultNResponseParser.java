package net.thevpc.nuts.runtime.standalone.io.ask;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.DefaultNArg;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.runtime.standalone.util.CoreEnumUtils;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NQuestion;
import net.thevpc.nuts.util.NQuestionParser;

public class DefaultNResponseParser<T> implements NQuestionParser<T> {

    private final NSession session;
    private final Class<T> type;

    public DefaultNResponseParser(NSession session, Class<T> type) {
        this.session = session;
        this.type = type;
    }

    @Override
    public T parse(Object response, T defaultValue, NQuestion<T> question) {
        if (response == null || ((response instanceof String) && response.toString().length() == 0)) {
            response = defaultValue;
        }
        if ("cancel!".equals(response)) {
            throw new NCancelException(session);
        }
        if (response == null) {
            return null;
        }
        if (type.isInstance(response)) {
            return (T) response;
        }
        if (type.isEnum()) {
            String s = String.valueOf(response).trim();
            return (T) CoreEnumUtils.parseEnumString(s,(Class)type,false);
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
                NArg a = new DefaultNArg(sReponse);
                if (!a.isBoolean()) {
                    throw new NIllegalArgumentException(session, NMsg.ofC("invalid response %s", sReponse));
                }
                return (T) (Object) a.asBoolean().get(session);
            }

            default: {
                throw new NUnsupportedArgumentException(session, NMsg.ofC("unsupported type %s", type.getName()));
            }
        }
    }
}

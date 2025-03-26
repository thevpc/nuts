package net.thevpc.nuts.runtime.standalone.io.ask;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.DefaultNArg;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.runtime.standalone.util.CoreEnumUtils;
import net.thevpc.nuts.util.NAskParseContext;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NAsk;
import net.thevpc.nuts.util.NAskParser;

public class DefaultNResponseParser<T> implements NAskParser<T> {

    private final NSession session;
    private final Class<T> type;

    public DefaultNResponseParser(NSession session, Class<T> type) {
        this.session = session;
        this.type = type;
    }

    @Override
    public T parse(NAskParseContext<T> context) {
        Object response=context.response();
        NAsk<T> question=context.question();
        T defaultValue=question.getDefaultValue();
        if (response == null || ((response instanceof String) && response.toString().length() == 0)) {
            response = defaultValue;
        }
        if ("cancel!".equals(response)) {
            throw new NCancelException();
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
                    throw new NIllegalArgumentException(NMsg.ofC("invalid response %s", sReponse));
                }
                return (T) (Object) a.asBooleanValue().get();
            }

            default: {
                throw new NUnsupportedArgumentException(NMsg.ofC("unsupported type %s", type.getName()));
            }
        }
    }
}

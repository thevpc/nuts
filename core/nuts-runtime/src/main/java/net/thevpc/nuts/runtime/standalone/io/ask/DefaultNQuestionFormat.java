package net.thevpc.nuts.runtime.standalone.io.ask;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NQuestion;
import net.thevpc.nuts.util.NQuestionFormat;

import java.util.Arrays;
import java.util.List;

public class DefaultNQuestionFormat<T> implements NQuestionFormat<T> {

    private NSession session;

    public DefaultNQuestionFormat(NSession session) {
        this.session = session;
    }

    @Override
    public List<Object> getDefaultValues(Class type, NQuestion<T> question) {
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
                throw new NUnsupportedArgumentException(session, NMsg.ofCstyle("unsupported type %s", type.getName()));
            }
        }
    }

    @Override
    public String format(Object value, NQuestion<T> question) {
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "y" : "n";
        }
        return String.valueOf(value);
    }

}

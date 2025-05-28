package net.thevpc.nuts;

import net.thevpc.nuts.util.NMsg;

public class NCmdLineException extends NIllegalArgumentException {

    public NCmdLineException(NMsg formattedMessage, Throwable cause) {
        super(formattedMessage, cause);
    }

}

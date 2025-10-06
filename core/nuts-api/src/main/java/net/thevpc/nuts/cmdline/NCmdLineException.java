package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.util.NMsg;

public class NCmdLineException extends NIllegalArgumentException {

    public NCmdLineException(NMsg formattedMessage, Throwable cause) {
        super(formattedMessage, cause);
    }

    public NCmdLineException(NMsg formattedMessage) {
        super(formattedMessage);
    }

}

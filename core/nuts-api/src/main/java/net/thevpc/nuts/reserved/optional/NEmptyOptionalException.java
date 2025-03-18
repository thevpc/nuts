package net.thevpc.nuts.reserved.optional;

import net.thevpc.nuts.NException;
import net.thevpc.nuts.util.NMsg;

public class NEmptyOptionalException extends NException {

    public NEmptyOptionalException(NMsg formattedMessage) {
        super(formattedMessage);
    }

}

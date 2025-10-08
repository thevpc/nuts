package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

public class NEmptyOptionalException extends NException {

    public NEmptyOptionalException(NMsg formattedMessage) {
        super(formattedMessage);
    }

}

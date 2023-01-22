package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NMsgTemplate;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.util.NProgressHandlerEvent;
import net.thevpc.nuts.util.NProgressHandler;

import java.util.logging.Level;

public class NPrintStreamProgressHandler implements NProgressHandler {
    private NMsgTemplate messageFormat;
    private NPrintStream printStream;


    /**
     * %value%
     * %date%
     *
     * @param messageFormat
     */
    public NPrintStreamProgressHandler(NMsgTemplate messageFormat, NPrintStream printStream, NSession session) {
        this.messageFormat = JLogProgressHandler.resolveFormat(messageFormat);
        if (printStream == null) {
            printStream = session.out();
        }
        this.printStream = printStream;

    }

    @Override
    public void onEvent(NProgressHandlerEvent event) {
        NMsg message = event.getModel().getMessage();
        Object msg = JLogProgressHandler.formatMessage(messageFormat, event.getModel());
        printStream.print(message.getLevel() == null ? Level.INFO : message.getLevel() + " ");
        printStream.println(msg);
    }

}

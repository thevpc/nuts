package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.util.NProgressHandlerEvent;
import net.thevpc.nuts.util.NProgressHandler;

import java.util.logging.Level;

public class NPrintStreamProgressHandler implements NProgressHandler {
    private String messageFormat;
    private NOutputStream printStream;


    /**
     * %value%
     * %date%
     *
     * @param messageFormat
     */
    public NPrintStreamProgressHandler(String messageFormat, NOutputStream printStream, NSession session) {
        this.messageFormat = JLogProgressHandler.resolveFormat(messageFormat);
        if (printStream == null) {
            printStream = session.out();
        }
        this.printStream = printStream;

    }

    @Override
    public void onEvent(NProgressHandlerEvent event) {
        NMsg message = event.getModel().getMessage();
        String msg = JLogProgressHandler.formatMessage(messageFormat, event.getModel());
        printStream.print(message.getLevel() == null ? Level.INFO : message.getLevel() + " ");
        printStream.println(msg);
    }

}

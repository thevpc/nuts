package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NMsgTemplate;
import net.thevpc.nuts.util.NProgressHandlerEvent;
import net.thevpc.nuts.util.NProgressHandler;

import java.io.PrintStream;
import java.util.logging.Level;

public class PrintStreamProgressHandler implements NProgressHandler {
    private NMsgTemplate messageFormat;
    private PrintStream printStream;


    /**
     * %value%
     * %date%
     *
     * @param messageFormat
     */
    public PrintStreamProgressHandler(NMsgTemplate messageFormat, PrintStream printStream) {
        this.messageFormat = JLogProgressHandler.resolveFormat(messageFormat);
        if (printStream == null) {
            printStream = System.out;
        }
        this.printStream = printStream;

    }

    @Override
    public void onEvent(NProgressHandlerEvent event) {
        NMsg message = event.getModel().getMessage();
        NMsg msg = JLogProgressHandler.formatMessage(messageFormat, event.getModel());
        Level level = (message == null || message.getLevel() == null) ? Level.INFO : message.getLevel();
        printStream.print(level + " ");
        printStream.println(msg);
    }

}

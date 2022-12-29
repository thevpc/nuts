package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.util.NProgressHandlerEvent;
import net.thevpc.nuts.util.NProgressHandler;

import java.io.PrintStream;
import java.util.logging.Level;

public class PrintStreamProgressHandler implements NProgressHandler {
    private String messageFormat;
    private PrintStream printStream;


    /**
     * %value%
     * %date%
     *
     * @param messageFormat
     */
    public PrintStreamProgressHandler(String messageFormat, PrintStream printStream) {
        this.messageFormat = JLogProgressHandler.resolveFormat(messageFormat);
        if (printStream == null) {
            printStream = System.out;
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

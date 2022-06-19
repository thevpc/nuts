package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.util.NutsProgressEventType;
import net.thevpc.nuts.util.NutsProgressHandlerEvent;
import net.thevpc.nuts.util.NutsProgressMonitorModel;
import net.thevpc.nuts.util.NutsProgressHandler;

import java.io.PrintStream;
import java.util.logging.Level;

public class PrintStreamProgressHandler implements NutsProgressHandler {
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
    public void onEvent(NutsProgressHandlerEvent event) {
        NutsMessage message = event.getModel().getMessage();
        String msg = JLogProgressHandler.formatMessage(messageFormat, event.getModel());
        printStream.print(message.getLevel() == null ? Level.INFO : message.getLevel() + " ");
        printStream.println(msg);
    }

}

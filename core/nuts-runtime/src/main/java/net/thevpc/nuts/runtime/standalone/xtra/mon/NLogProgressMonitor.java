package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.util.NMemorySizeFormat;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.time.NProgressHandler;
import net.thevpc.nuts.time.NProgressHandlerEvent;

import java.text.DecimalFormat;
import java.util.logging.Level;

public class NLogProgressMonitor implements NProgressHandler {
    private static NMemorySizeFormat MF = NMemorySizeFormat.FIXED;
    public static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#00.00%");
    private NLog logger;

    public NLogProgressMonitor(NLog logger) {
        if (logger == null) {
            logger = NLog.of(NLogProgressMonitor.class);
        }
        this.logger = logger;
    }

    @Override
    public void onEvent(NProgressHandlerEvent event) {
        NMsg message = event.getModel().getMessage();
        logger.log(message.withLevel(message.getLevel() == null ? Level.INFO : message.getLevel()));
    }

}

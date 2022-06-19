package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.util.*;

import java.text.DecimalFormat;
import java.util.logging.Level;

public class NLogProgressMonitor implements NutsProgressHandler {
    private static NutsMemorySizeFormat MF = NutsMemorySizeFormat.FIXED;
    public static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#00.00%");
    private NutsLogger logger;

    public NLogProgressMonitor(NutsLogger logger,NutsSession session) {
        if (logger == null) {
            logger = NutsLogger.of(NLogProgressMonitor.class,session);
        }
        this.logger = logger;
    }

    @Override
    public void onEvent(NutsProgressHandlerEvent event) {
        NutsMessage message = event.getModel().getMessage();
        NutsLoggerOp w = logger.with().level(message.getLevel() == null ? Level.INFO : message.getLevel());
        w.log(message);
    }

}

package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.runtime.standalone.util.MemoryUtils;
import net.thevpc.nuts.util.*;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JLogProgressHandler implements NutsProgressHandler {
    private static NutsMemorySizeFormat MF = NutsMemorySizeFormat.FIXED;
    public static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#00.00%");
    private static Logger defaultLog = Logger.getLogger(JLogProgressHandler.class.getName());

    static {
        defaultLog.setUseParentHandlers(false);
    }

    private String messageFormat;
    private Logger logger;

    /**
     * %value%
     * %date%
     *
     * @param messageFormat
     */
    public JLogProgressHandler(String messageFormat, Logger logger) {
        this.messageFormat = resolveFormat(messageFormat);
        if (logger == null) {
            logger = defaultLog;
        }
        this.logger = logger;
    }

    @Override
    public void onEvent(NutsProgressHandlerEvent event) {
        NutsMessage message = event.getModel().getMessage();
        String msg = formatMessage(messageFormat, event.getModel());
        logger.log(message.getLevel() == null ? Level.INFO : message.getLevel(), msg);
    }

    public static String formatMessage(String messageFormat, NutsProgressMonitorModel model) {
        long newd = System.currentTimeMillis();
        NutsMessage message = model.getMessage();
        return messageFormat
                .replace("%date%", new Date(newd).toString())
                .replace("%value%", Double.isNaN(model.getProgress()) ? "   ?%" : PERCENT_FORMAT.format(model.getProgress()))
                .replace("%inuse-mem%", MF.format(MemoryUtils.inUseMemory()))
                .replace("%free-mem%", MF.format(MemoryUtils.maxFreeMemory()))
                .replace("%message%", message.toString());
    }

    public static String resolveFormat(String messageFormat) {
        if (messageFormat == null || messageFormat.isEmpty()) {
            messageFormat = "%inuse-mem% | %free-mem% | %progress% : %message%";
        }
        if (!messageFormat.contains("%message%")) {
            if (!messageFormat.endsWith(" ")) {
                messageFormat += " ";
            }
            messageFormat += "%message%";
        }
        return messageFormat;
    }

}

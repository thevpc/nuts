package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NMsgParam;
import net.thevpc.nuts.util.NMsgTemplate;
import net.thevpc.nuts.runtime.standalone.util.MemoryUtils;
import net.thevpc.nuts.time.NProgressHandler;
import net.thevpc.nuts.time.NProgressHandlerEvent;
import net.thevpc.nuts.time.NProgressMonitorModel;
import net.thevpc.nuts.util.*;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JLogProgressHandler implements NProgressHandler {
    private static NMemorySizeFormat MF = NMemorySizeFormat.FIXED;
    public static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#00.00%");
    private static Logger defaultLog = Logger.getLogger(JLogProgressHandler.class.getName());

    static {
        defaultLog.setUseParentHandlers(false);
    }

    private NMsgTemplate messageFormat;
    private Logger logger;

    /**
     * %value%
     * %date%
     *
     * @param messageFormat
     */
    public JLogProgressHandler(NMsgTemplate messageFormat, Logger logger) {
        this.messageFormat = resolveFormat(messageFormat);
        if (logger == null) {
            logger = defaultLog;
        }
        this.logger = logger;
    }

    @Override
    public void onEvent(NProgressHandlerEvent event) {
        NMsg message = event.getModel().getMessage();
        NMsg msg = formatMessage(messageFormat, event.getModel());
        logger.log(message.getLevel() == null ? Level.INFO : message.getLevel(), msg.toString());
    }

    public static NMsg formatMessage(NMsgTemplate messageFormat, NProgressMonitorModel model) {
        long newd = System.currentTimeMillis();
        NMsg message = model.getMessage();
        return messageFormat.build(
                NMsgParam.of("message",()-> message==null?"":message),
                NMsgParam.of("date",()-> new Date(newd)),
                NMsgParam.of("progress",()-> Double.isNaN(model.getProgress()) ? "   ?%" : PERCENT_FORMAT.format(model.getProgress())),
                NMsgParam.of("inuse",()->MF.format(MemoryUtils.inUseMemory())),
                NMsgParam.of("free",()-> MF.format(MemoryUtils.maxFreeMemory()))
        );
    }

    public static NMsgTemplate resolveFormat(NMsgTemplate messageFormat) {
        if (messageFormat == null) {
            messageFormat = NMsgTemplate.ofV("$inuse | $free | $progress : $message");
        }
        if (messageFormat.getParamNames().length==0) {
            switch (messageFormat.getFormat()){
                case VFORMAT:{
                    String message = messageFormat.getMessage();
                    if (!message.endsWith(" ")) {
                        message += " ";
                    }
                    message += "$message";
                    messageFormat=NMsgTemplate.ofV(message);
                    break;
                }
                case CFORMAT:{
                    String message = messageFormat.getMessage();
                    if (!message.endsWith(" ")) {
                        message += " ";
                    }
                    message += "%s";
                    messageFormat=NMsgTemplate.ofC(message);
                    break;
                }
                case JFORMAT:{
                    String message = messageFormat.getMessage();
                    if (!message.endsWith(" ")) {
                        message += " ";
                    }
                    message += "{0}";
                    messageFormat=NMsgTemplate.ofJ(message);
                    break;
                }
            }
        }
        return messageFormat;
    }

}

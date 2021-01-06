package net.thevpc.nuts.runtime.standalone.log;


import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class NutsLogRichFormatter extends Formatter {
    private long lastMillis = -1;
    public static final NutsLogRichFormatter RICH = new NutsLogRichFormatter();

    public NutsLogRichFormatter() {
    }

    public static LogRecord compile(LogRecord record) {
        LogRecord h=null;
        if (record instanceof NutsLogRecord) {
            NutsLogRecord wRecord = (NutsLogRecord) record;
            Object[] p = wRecord.getParameters();
            NutsWorkspace ws = wRecord.getWorkspace();
            h = new NutsLogRecord(
                    ws,
                    wRecord.getSession(),
                    wRecord.getLevel(),
                    wRecord.getVerb(),
                    ws.formats().text().toString(
                            new NutsMessage(
                                wRecord.getFormatStyle(),
                                    NutsString.of(wRecord.getMessage()),
                                p
                            ),
                            wRecord.getSession()
                    ).toString()
                    ,
                    null,
                    wRecord.isFormatted(),
                    wRecord.getTime(),
                    wRecord.getFormatStyle()
            );
        }else{
            return record;
        }
        h.setResourceBundle(record.getResourceBundle());
        h.setResourceBundleName(record.getResourceBundleName());
        h.setSequenceNumber(record.getSequenceNumber());
        h.setMillis(record.getMillis());
        h.setSourceClassName(record.getSourceClassName());
        h.setLoggerName(record.getLoggerName());
        h.setSourceMethodName(record.getSourceMethodName());
        h.setThreadID(record.getThreadID());
        h.setThrown(record.getThrown());
        return h;
    }
    @Override
    public String format(LogRecord record) {
        if (record instanceof NutsLogRecord) {
            NutsLogRecord wRecord = (NutsLogRecord) record;
            NutsTextFormatStyle style = wRecord.getFormatStyle();
            NutsTextFormatManager tf = wRecord.getWorkspace().formats().text();

            NutsTextNodeBuilder sb = tf.builder();
            NutsTextNodeFactory ff = tf.factory();
            String date = CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(wRecord.getMillis()));

            sb.append(ff.styled(date,NutsTextNodeStyle.pale()));
            boolean verboseLog = false;//read from session or workspace;
            if (verboseLog) {
                sb.append(" ");
                int len = date.length() + 5;
                StringBuilder sb2=new StringBuilder(5);
                if (lastMillis > 0) {
                    sb2.append(String.valueOf(wRecord.getMillis() - lastMillis));
                }
                while(sb2.length()<5){
                    sb2.append(' ');
                }
                sb.append(sb.toString());
            }
            sb.append(" ");
            switch (wRecord.getLevel().intValue()) {
                case 1000: {//Level.SEVERE
                    sb.append(NutsLogFormatHelper.logLevel(wRecord.getLevel()),NutsTextNodeStyle.error());
                    break;
                }
                case 900: {//Level.WARNING
                    sb.append(NutsLogFormatHelper.logLevel(wRecord.getLevel()),NutsTextNodeStyle.warn());
                    break;
                }
                case 800: {//Level.INFO
                    sb.append(NutsLogFormatHelper.logLevel(wRecord.getLevel()),NutsTextNodeStyle.info());
                    break;
                }
                case 700: {//Level.CONFIG
                    sb.append(NutsLogFormatHelper.logLevel(wRecord.getLevel()),NutsTextNodeStyle.config());
                    break;
                }
                case 500: {//Level.FINE
                    sb.append(NutsLogFormatHelper.logLevel(wRecord.getLevel()),NutsTextNodeStyle.primary(4));
                    break;
                }
                case 400: {//Level.FINER
//                    sb.append("[[");
                    sb.append(NutsLogFormatHelper.logLevel(wRecord.getLevel()),NutsTextNodeStyle.pale());
//                    sb.append("]]");
                    break;
                }
                case 300: {//Level.FINEST
                    sb.append(NutsLogFormatHelper.logLevel(wRecord.getLevel()),NutsTextNodeStyle.pale());
                    break;
                }
                default: {
                    sb.append(NutsLogFormatHelper.logLevel(wRecord.getLevel()));
                    break;
                }
            }

            sb.append(" ");
            switch (CoreStringUtils.trim(wRecord.getVerb()).toUpperCase()) {
                case NutsLogVerb.FAIL:
                    {//Level.SEVERE
                    sb.append(NutsLogFormatHelper.logVerb(wRecord.getVerb()),NutsTextNodeStyle.error());
                    break;
                }
                case NutsLogVerb.WARNING:
                {//Level.WARNING
                    sb.append(NutsLogFormatHelper.logVerb(wRecord.getVerb()),NutsTextNodeStyle.warn());
                    break;
                }
                case NutsLogVerb.UPDATE:
                case NutsLogVerb.START:
                    {//Level.WARNING
                        sb.append(NutsLogFormatHelper.logVerb(wRecord.getVerb()),NutsTextNodeStyle.info());
                    break;
                }
                case NutsLogVerb.SUCCESS:
                    {//Level.INFO
                        sb.append(NutsLogFormatHelper.logVerb(wRecord.getVerb()),NutsTextNodeStyle.success());
                    break;
                }
//                case NutsLogVerb.BIND:
//                    {//Level.CONFIG
//                    sb.append("^^");
//                    sb.append(tf.escapeText(NutsLogFormatHelper.logVerb(wRecord.getVerb())));
//                    sb.append("^^");
//                    break;
//                }
                case NutsLogVerb.INFO:
                case NutsLogVerb.READ:
                    {//Level.FINE
                        sb.append(NutsLogFormatHelper.logVerb(wRecord.getVerb()),NutsTextNodeStyle.option());
                    break;
                }
                case NutsLogVerb.CACHE:
                case NutsLogVerb.DEBUG:
                    {//Level.FINE
                        sb.append(NutsLogFormatHelper.logVerb(wRecord.getVerb()),NutsTextNodeStyle.pale());
                    break;
                }
//                case NutsLogVerb.INIT: {//Level.FINER
//                    sb.append("[[");
//                    sb.append(tf.escapeText(NutsLogFormatHelper.logVerb(wRecord.getVerb())));
//                    sb.append("]]");
//                    break;
//                }
//                case NutsLogVerb.START: {//Level.FINEST
//                    sb.append("<<");
//                    sb.append(tf.escapeText(NutsLogFormatHelper.logLevel(wRecord.getLevel())));
//                    sb.append(">>");
//                    break;
//                }
                default: {
                    sb.append(NutsLogFormatHelper.logVerb(wRecord.getVerb()));
                    break;
                }
            }

            sb.append(" "
                    +NutsLogFormatHelper.formatClassName(wRecord.getSourceClassName())
                    +": ");
            Object[] parameters2 = wRecord.getParameters();
            if(parameters2==null){
                parameters2=new Object[0];
            }
            String message = wRecord.getMessage();
            // \\{[0-9]+\\}
//            message=message.replaceAll("\\\\\\{([0-9]+)\\\\}","{$1}");
            NutsFormatManager formats = wRecord.getWorkspace().formats();
            NutsTextFormatManager text = formats.text();
            if(!wRecord.isFormatted()){
                parameters2= Arrays.copyOf(parameters2,parameters2.length);
                for (int i = 0; i < parameters2.length; i++) {
                    if (parameters2[i] instanceof NutsString) {
                        parameters2[i] = text.filterText(parameters2[i].toString());
                    } else if (parameters2[i] instanceof NutsFormattable) {
                        parameters2[i] = text.filterText(
                                formats.of((NutsFormattable) parameters2[i]).format()
                        );
                    }
                }
            }
            NutsString msgStr =
                    wRecord.getWorkspace().formats().text().toString(
                    new NutsMessage(style,
                            NutsString.of(message),
                            parameters2
                    )
                    ,wRecord.getSession()
            );
//                    formatMessage(wRecord);
            if(wRecord.isFormatted()) {
                sb.append(msgStr);
            }else{
                sb.append(msgStr.toString());
            }
            if(wRecord.getTime()>0){
                sb.append(" (");
                sb.append(CoreCommonUtils.formatPeriodMilli(wRecord.getTime()),NutsTextNodeStyle.error());
                sb.append(")");
            }
            sb.append(NutsLogFormatHelper.LINE_SEPARATOR);
            lastMillis = wRecord.getMillis();
            if (wRecord.getThrown() != null) {
                sb.append(NutsLogFormatHelper.stacktrace(wRecord.getThrown()));
            }
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            String date = Instant.ofEpochMilli(record.getMillis()).toString().replace(":", "");

            sb.append(date);
            for (int i = 22 - date.length() - 1; i >= 0; i--) {
                sb.append(' ');
            }
            boolean verboseLog = false;//read from session or workspace;
            if (verboseLog) {
                sb.append(" ");
                int len = sb.length() + 4;
                if (lastMillis > 0) {
                    sb.append(record.getMillis() - lastMillis);
                }
                NutsLogFormatHelper.ensureSize(sb, len);
            }
            sb
                    .append(" ")
                    .append(NutsLogFormatHelper.logLevel(record.getLevel()))
                    .append(" ")
                    .append(NutsLogFormatHelper.logVerb(""))
                    .append(" ")
                    .append(NutsLogFormatHelper.formatClassName(record.getSourceClassName()))
                    .append(": ")
                    .append(formatMessage(record))
                    .append(NutsLogFormatHelper.LINE_SEPARATOR);
            lastMillis = record.getMillis();
            if (record.getThrown() != null) {
                sb.append(NutsLogFormatHelper.stacktrace(record.getThrown()));
            }
            return sb.toString();
        }
    }
}

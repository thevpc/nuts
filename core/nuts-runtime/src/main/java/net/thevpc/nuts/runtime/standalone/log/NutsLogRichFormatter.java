package net.thevpc.nuts.runtime.standalone.log;


import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.common.CoreCommonUtils;

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
            String msgStr = ws.formats().text().formatText(
                    wRecord.getSession(),
                    wRecord.getFormatStyle(), wRecord.getMessage(),
                    p
            );
            h = new NutsLogRecord(
                    ws,
                    wRecord.getSession(),
                    wRecord.getLevel(),
                    wRecord.getVerb(),
                    msgStr,
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

            sb.appendStyled(ff.styled(date,NutsTextNodeStyle.PALE1));
            boolean verboseLog = false;//read from session or workspace;
            if (verboseLog) {
                sb.appendPlain(" ");
                int len = date.length() + 5;
                StringBuilder sb2=new StringBuilder(5);
                if (lastMillis > 0) {
                    sb2.append(String.valueOf(wRecord.getMillis() - lastMillis));
                }
                while(sb2.length()<5){
                    sb2.append(' ');
                }
                sb.appendPlain(sb.toString());
            }
            sb.appendPlain(" ");
            switch (wRecord.getLevel().intValue()) {
                case 1000: {//Level.SEVERE
                    sb.appendStyled(NutsLogFormatHelper.logLevel(wRecord.getLevel()),NutsTextNodeStyle.ERROR1);
                    break;
                }
                case 900: {//Level.WARNING
                    sb.appendStyled(NutsLogFormatHelper.logLevel(wRecord.getLevel()),NutsTextNodeStyle.WARN1);
                    break;
                }
                case 800: {//Level.INFO
                    sb.appendStyled(NutsLogFormatHelper.logLevel(wRecord.getLevel()),NutsTextNodeStyle.INFO1);
                    break;
                }
                case 700: {//Level.CONFIG
                    sb.appendStyled(NutsLogFormatHelper.logLevel(wRecord.getLevel()),NutsTextNodeStyle.CONFIG1);
                    break;
                }
                case 500: {//Level.FINE
                    sb.appendStyled(NutsLogFormatHelper.logLevel(wRecord.getLevel()),NutsTextNodeStyle.PRIMARY4);
                    break;
                }
                case 400: {//Level.FINER
//                    sb.append("[[");
                    sb.appendStyled(NutsLogFormatHelper.logLevel(wRecord.getLevel()),NutsTextNodeStyle.PALE1);
//                    sb.append("]]");
                    break;
                }
                case 300: {//Level.FINEST
                    sb.appendStyled(NutsLogFormatHelper.logLevel(wRecord.getLevel()),NutsTextNodeStyle.PALE1);
                    break;
                }
                default: {
                    sb.appendPlain(NutsLogFormatHelper.logLevel(wRecord.getLevel()));
                    break;
                }
            }

            sb.appendPlain(" ");
            switch (CoreStringUtils.trim(wRecord.getVerb()).toUpperCase()) {
                case NutsLogVerb.FAIL:
                    {//Level.SEVERE
                    sb.appendStyled(NutsLogFormatHelper.logVerb(wRecord.getVerb()),NutsTextNodeStyle.ERROR1);
                    break;
                }
                case NutsLogVerb.WARNING:
                {//Level.WARNING
                    sb.appendStyled(NutsLogFormatHelper.logVerb(wRecord.getVerb()),NutsTextNodeStyle.WARN1);
                    break;
                }
                case NutsLogVerb.UPDATE:
                case NutsLogVerb.START:
                    {//Level.WARNING
                        sb.appendStyled(NutsLogFormatHelper.logVerb(wRecord.getVerb()),NutsTextNodeStyle.INFO1);
                    break;
                }
                case NutsLogVerb.SUCCESS:
                    {//Level.INFO
                        sb.appendStyled(NutsLogFormatHelper.logVerb(wRecord.getVerb()),NutsTextNodeStyle.SUCCESS1);
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
                        sb.appendStyled(NutsLogFormatHelper.logVerb(wRecord.getVerb()),NutsTextNodeStyle.OPTION1);
                    break;
                }
                case NutsLogVerb.CACHE:
                case NutsLogVerb.DEBUG:
                    {//Level.FINE
                        sb.appendStyled(NutsLogFormatHelper.logVerb(wRecord.getVerb()),NutsTextNodeStyle.PALE1);
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
                    sb.appendPlain(NutsLogFormatHelper.logVerb(wRecord.getVerb()));
                    break;
                }
            }

            sb.appendPlain(" "
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
            String msgStr =wRecord.getWorkspace().formats().text().formatText(
                    wRecord.getSession(),
                    style, message,
                    parameters2
            );
//                    formatMessage(wRecord);
            if(wRecord.isFormatted()) {
                sb.append(NutsString.of(msgStr));
            }else{
                sb.appendPlain(msgStr);
            }
            if(wRecord.getTime()>0){
                sb.appendPlain(" (");
                sb.appendStyled(CoreCommonUtils.formatPeriodMilli(wRecord.getTime()),NutsTextNodeStyle.ERROR1);
                sb.appendPlain(")");
            }
            sb.appendPlain(NutsLogFormatHelper.LINE_SEPARATOR);
            lastMillis = wRecord.getMillis();
            if (wRecord.getThrown() != null) {
                sb.appendPlain(NutsLogFormatHelper.stacktrace(wRecord.getThrown()));
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

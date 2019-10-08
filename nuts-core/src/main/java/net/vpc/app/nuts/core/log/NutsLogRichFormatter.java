package net.vpc.app.nuts.core.log;


import net.vpc.app.nuts.NutsTerminalFormat;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

import java.time.Instant;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class NutsLogRichFormatter extends Formatter {
    private long lastMillis = -1;
    public static final NutsLogRichFormatter RICH = new NutsLogRichFormatter();

    public NutsLogRichFormatter() {
    }

    @Override
    public String format(LogRecord record) {
        if (record instanceof NutsLogRecord) {
            NutsLogRecord wRecord = (NutsLogRecord) record;
            NutsTerminalFormat tf = wRecord.getWorkspace().io().terminalFormat();
            StringBuilder sb = new StringBuilder();
            String date = CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(wRecord.getMillis()));
            sb.append("<<").append(tf.escapeText(date)).append(">>");
            boolean verboseLog = false;//read from session or workspace;
            if (verboseLog) {
                sb.append(" ");
                int len = sb.length() + 4;
                if (lastMillis > 0) {
                    sb.append(wRecord.getMillis() - lastMillis);
                }
                NutsLogFormatHelper.ensureSize(sb, len);
            }
            sb.append(" ");
            switch (wRecord.getLevel().intValue()) {
                case 1000: {//Level.SEVERE
                    sb.append("@@");
                    sb.append(tf.escapeText(NutsLogFormatHelper.logLevel(wRecord.getLevel())));
                    sb.append("@@");
                    break;
                }
                case 900: {//Level.WARNING
                    sb.append("{{");
                    sb.append(tf.escapeText(NutsLogFormatHelper.logLevel(wRecord.getLevel())));
                    sb.append("}}");
                    break;
                }
                case 800: {//Level.INFO
                    sb.append("##");
                    sb.append(tf.escapeText(NutsLogFormatHelper.logLevel(wRecord.getLevel())));
                    sb.append("##");
                    break;
                }
                case 700: {//Level.CONFIG
                    sb.append("^^");
                    sb.append(tf.escapeText(NutsLogFormatHelper.logLevel(wRecord.getLevel())));
                    sb.append("^^");
                    break;
                }
                case 500: {//Level.FINE
                    sb.append("**");
                    sb.append(tf.escapeText(NutsLogFormatHelper.logLevel(wRecord.getLevel())));
                    sb.append("**");
                    break;
                }
                case 400: {//Level.FINER
                    sb.append("[[");
                    sb.append(tf.escapeText(NutsLogFormatHelper.logLevel(wRecord.getLevel())));
                    sb.append("]]");
                    break;
                }
                case 300: {//Level.FINEST
                    sb.append("<<");
                    sb.append(tf.escapeText(NutsLogFormatHelper.logLevel(wRecord.getLevel())));
                    sb.append(">>");
                    break;
                }
                default: {
                    sb.append(tf.escapeText(NutsLogFormatHelper.logLevel(wRecord.getLevel())));
                    break;
                }
            }

            sb.append(" ");
            switch (CoreStringUtils.trim(wRecord.getVerb()).toUpperCase()) {
                case NutsLogVerb.FAIL: {//Level.SEVERE
                    sb.append("@@");
                    sb.append(tf.escapeText(NutsLogFormatHelper.logVerb(wRecord.getVerb())));
                    sb.append("@@");
                    break;
                }
                case NutsLogVerb.UPDATE:
                case NutsLogVerb.WARNING:
                case NutsLogVerb.START:
                    {//Level.WARNING
                    sb.append("{{");
                    sb.append(tf.escapeText(NutsLogFormatHelper.logVerb(wRecord.getVerb())));
                    sb.append("}}");
                    break;
                }
                case NutsLogVerb.SUCCESS:
                    {//Level.INFO
                    sb.append("##");
                    sb.append(tf.escapeText(NutsLogFormatHelper.logVerb(wRecord.getVerb())));
                    sb.append("##");
                    break;
                }
//                case NutsLogVerb.BIND:
//                    {//Level.CONFIG
//                    sb.append("^^");
//                    sb.append(tf.escapeText(NutsLogFormatHelper.logVerb(wRecord.getVerb())));
//                    sb.append("^^");
//                    break;
//                }
                case NutsLogVerb.READ:
                    {//Level.FINE
                    sb.append("**");
                    sb.append(tf.escapeText(NutsLogFormatHelper.logVerb(wRecord.getVerb())));
                    sb.append("**");
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
                    sb.append(tf.escapeText(NutsLogFormatHelper.logVerb(wRecord.getVerb())));
                    break;
                }
            }

            sb.append(" ")
                    .append(tf.escapeText(NutsLogFormatHelper.formatClassName(wRecord.getSourceClassName())))
                    .append(": ");
            String msgStr = formatMessage(wRecord);
            if(wRecord.isFormatted()) {
                sb.append(msgStr);
            }else{
                sb.append(tf.escapeText(msgStr));
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

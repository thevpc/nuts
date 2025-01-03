package net.thevpc.nuts.runtime.standalone.log;


import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreTimeUtils;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.log.NLogRecord;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.time.Instant;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class NLogRichFormatter extends Formatter {
    private long lastMillis = -1;
    private NWorkspace workspace;
    private boolean filtered;
//    public static final NutsLogRichFormatter RICH = new NutsLogRichFormatter();

    public NLogRichFormatter(NWorkspace workspace, boolean filtered) {
        this.workspace = workspace;
        this.filtered = filtered;
    }

    @Override
    public String format(LogRecord record) {
        NLogRecord wRecord = NLogUtils.toNutsLogRecord(record, workspace.currentSession());
        NTexts tf = NTexts.of();

        NTextBuilder sb = tf.ofBuilder();
        String date = CoreNUtils.DEFAULT_DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(wRecord.getMillis()));

        sb.append(tf.ofStyled(date, NTextStyle.pale()));
        boolean verboseLog = false;//read from session or workspace;
        if (verboseLog) {
            sb.append(" ");
            int len = date.length() + 5;
            StringBuilder sb2 = new StringBuilder(5);
            if (lastMillis > 0) {
                sb2.append(String.valueOf(wRecord.getMillis() - lastMillis));
            }
            while (sb2.length() < 5) {
                sb2.append(' ');
            }
            sb.append(sb.toString());
        }
        sb.append(" ");
        switch (wRecord.getLevel().intValue()) {
            case 1000: {//Level.SEVERE
                sb.append(NLogUtils.logLevel(wRecord.getLevel()), NTextStyle.error());
                break;
            }
            case 900: {//Level.WARNING
                sb.append(NLogUtils.logLevel(wRecord.getLevel()), NTextStyle.warn());
                break;
            }
            case 800: {//Level.INFO
                sb.append(NLogUtils.logLevel(wRecord.getLevel()), NTextStyle.info());
                break;
            }
            case 700: {//Level.CONFIG
                sb.append(NLogUtils.logLevel(wRecord.getLevel()), NTextStyle.config());
                break;
            }
            case 500: {//Level.FINE
                sb.append(NLogUtils.logLevel(wRecord.getLevel()), NTextStyle.primary4());
                break;
            }
            case 400: {//Level.FINER
//                    sb.append("[[");
                sb.append(NLogUtils.logLevel(wRecord.getLevel()), NTextStyle.pale());
//                    sb.append("]]");
                break;
            }
            case 300: {//Level.FINEST
                sb.append(NLogUtils.logLevel(wRecord.getLevel()), NTextStyle.pale());
                break;
            }
            default: {
                sb.append(NLogUtils.logLevel(wRecord.getLevel()));
                break;
            }
        }

        sb.append(" ");
        switch (wRecord.getVerb() == null ? "" : wRecord.getVerb().name()) {
            case "FAIL": {//Level.SEVERE
                sb.append(NLogUtils.logVerb(wRecord.getVerb().name()), NTextStyle.error());
                break;
            }
            case "WARNING": {//Level.WARNING
                sb.append(NLogUtils.logVerb(wRecord.getVerb().name()), NTextStyle.warn());
                break;
            }
            case "UPDATE":
            case "START": {//Level.WARNING
                sb.append(NLogUtils.logVerb(wRecord.getVerb().name()), NTextStyle.info());
                break;
            }
            case "SUCCESS": {//Level.INFO
                sb.append(NLogUtils.logVerb(wRecord.getVerb().name()), NTextStyle.success());
                break;
            }
//                case NutsLogVerb.BIND:
//                    {//Level.CONFIG
//                    sb.append("^^");
//                    sb.append(tf.escapeText(NutsLogFormatHelper.logVerb(wRecord.getVerb())));
//                    sb.append("^^");
//                    break;
//                }
            case "INFO":
            case "READ": {//Level.FINE
                sb.append(NLogUtils.logVerb(wRecord.getVerb().name()), NTextStyle.option());
                break;
            }
            case "CACHE":
            case "DEBUG": {//Level.FINE
                sb.append(NLogUtils.logVerb(wRecord.getVerb().name()), NTextStyle.pale());
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
                sb.append(NLogUtils.logVerb(wRecord.getVerb() == null ? null : wRecord.getVerb().name()));
                break;
            }
        }

        sb.append(" "
                + NLogUtils.formatClassName(wRecord.getSourceClassName())
                + ": ");

        NMsg message = wRecord.getFormattedMessage();
        NText msgStr =
                NTexts.of()
                        .of(message);
        sb.append(msgStr);
        if (wRecord.getTime() > 0) {
            sb.append(" (");
            sb.append(CoreTimeUtils.formatPeriodMilli(wRecord.getTime()), NTextStyle.config());
            sb.append(")");
        }
        sb.append(NLogUtils.LINE_SEPARATOR);
        lastMillis = wRecord.getMillis();
        if (wRecord.getThrown() != null) {
            sb.append(
                    NText.ofPlain(
                            NStringUtils.stacktrace(wRecord.getThrown())
                    ).toString()
            );
        }
        if (filtered) {
            return sb.filteredText();
        }
        return sb.toString();

    }
}

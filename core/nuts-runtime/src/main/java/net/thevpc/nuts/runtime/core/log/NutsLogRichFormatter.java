package net.thevpc.nuts.runtime.core.log;


import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreTimeUtils;

import java.time.Instant;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class NutsLogRichFormatter extends Formatter {
    private long lastMillis = -1;
    private NutsSession session;
    private boolean filtered;
//    public static final NutsLogRichFormatter RICH = new NutsLogRichFormatter();

    public NutsLogRichFormatter(NutsSession session, boolean filtered) {
        this.session = session;
        this.filtered = filtered;
    }

    @Override
    public String format(LogRecord record) {
        NutsLogRecord wRecord = NutsLogUtils.toNutsLogRecord(record, session);
        NutsTextManager tf = wRecord.getWorkspace().text().setSession(wRecord.getSession());

        NutsTextBuilder sb = tf.builder();
        String date = CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(wRecord.getMillis()));

        sb.append(tf.ofStyled(date, NutsTextStyle.pale()));
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
                sb.append(NutsLogUtils.logLevel(wRecord.getLevel()), NutsTextStyle.error());
                break;
            }
            case 900: {//Level.WARNING
                sb.append(NutsLogUtils.logLevel(wRecord.getLevel()), NutsTextStyle.warn());
                break;
            }
            case 800: {//Level.INFO
                sb.append(NutsLogUtils.logLevel(wRecord.getLevel()), NutsTextStyle.info());
                break;
            }
            case 700: {//Level.CONFIG
                sb.append(NutsLogUtils.logLevel(wRecord.getLevel()), NutsTextStyle.config());
                break;
            }
            case 500: {//Level.FINE
                sb.append(NutsLogUtils.logLevel(wRecord.getLevel()), NutsTextStyle.primary4());
                break;
            }
            case 400: {//Level.FINER
//                    sb.append("[[");
                sb.append(NutsLogUtils.logLevel(wRecord.getLevel()), NutsTextStyle.pale());
//                    sb.append("]]");
                break;
            }
            case 300: {//Level.FINEST
                sb.append(NutsLogUtils.logLevel(wRecord.getLevel()), NutsTextStyle.pale());
                break;
            }
            default: {
                sb.append(NutsLogUtils.logLevel(wRecord.getLevel()));
                break;
            }
        }

        sb.append(" ");
        switch (wRecord.getVerb() == null ? "" : wRecord.getVerb().name()) {
            case "FAIL": {//Level.SEVERE
                sb.append(NutsLogUtils.logVerb(wRecord.getVerb().name()), NutsTextStyle.error());
                break;
            }
            case "WARNING": {//Level.WARNING
                sb.append(NutsLogUtils.logVerb(wRecord.getVerb().name()), NutsTextStyle.warn());
                break;
            }
            case "UPDATE":
            case "START": {//Level.WARNING
                sb.append(NutsLogUtils.logVerb(wRecord.getVerb().name()), NutsTextStyle.info());
                break;
            }
            case "SUCCESS": {//Level.INFO
                sb.append(NutsLogUtils.logVerb(wRecord.getVerb().name()), NutsTextStyle.success());
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
                sb.append(NutsLogUtils.logVerb(wRecord.getVerb().name()), NutsTextStyle.option());
                break;
            }
            case "CACHE":
            case "DEBUG": {//Level.FINE
                sb.append(NutsLogUtils.logVerb(wRecord.getVerb().name()), NutsTextStyle.pale());
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
                sb.append(NutsLogUtils.logVerb(wRecord.getVerb() == null ? null : wRecord.getVerb().name()));
                break;
            }
        }

        sb.append(" "
                + NutsLogUtils.formatClassName(wRecord.getSourceClassName())
                + ": ");

        NutsMessage message = wRecord.getNutsMessage();
        NutsString msgStr =
                wRecord.getWorkspace().text()
                        .setSession(wRecord.getSession())
                        .toText(message);
        sb.append(msgStr);
        if (wRecord.getTime() > 0) {
            sb.append(" (");
            sb.append(CoreTimeUtils.formatPeriodMilli(wRecord.getTime()), NutsTextStyle.config());
            sb.append(")");
        }
        sb.append(NutsLogUtils.LINE_SEPARATOR);
        lastMillis = wRecord.getMillis();
        if (wRecord.getThrown() != null) {
            sb.append(
                    wRecord.getSession().text().ofPlain(
                            NutsLogUtils.stacktrace(wRecord.getThrown())
                    ).toString()
            );
        }
        if (filtered) {
            return sb.filteredText();
        }
        return sb.toString();

    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.util.BytesSizeFormat;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NLog;
import net.thevpc.nuts.util.NLogVerb;
import net.thevpc.nuts.util.NProgressEvent;
import net.thevpc.nuts.util.NProgressListener;

import java.text.DecimalFormat;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class TraceNProgressListener implements NProgressListener/*, NutsOutputStreamTransparentAdapter*/ {
    private static DecimalFormat df = new DecimalFormat("##0.00");

    private NPrintStream out;
    private int minLength;
    private CProgressBar bar;
    private boolean optionsProcessed = false;
    private ProgressOptions options;
    private NLog logger;

    public TraceNProgressListener() {
//        this.session = session;
    }

    @Override
    public boolean onProgress(NProgressEvent event) {
        switch (event.getState()) {
            case START: {
                bar = CProgressBar.of(event.getSession());
                this.out = event.getSession().getTerminal().err();
                this.logger= NLog.of(TraceNProgressListener.class,event.getSession());
                if (event.getSession().isPlainOut()) {
                    onProgress0(event, false);
                }
                return true;
            }
            case COMPLETE: {
                if (event.getSession().isPlainOut()) {
                    boolean b = onProgress0(event, true);
                    out.resetLine();
                    return b;
                    //out.println();
                }
                return false;
            }
            default: {
                if (event.getSession().isPlainOut()) {
                    return onProgress0(event, false);
                }
                return false;
            }
        }
    }

    public boolean onProgress0(NProgressEvent event, boolean end) {
        if (!optionsProcessed) {
            optionsProcessed = true;
            options = ProgressOptions.of(event.getSession());
        }
        double partialSeconds = event.getPartialDuration().getTimeAsDoubleSeconds();
        if (event.getCurrentCount() == 0 || partialSeconds > 0.5 || event.getCurrentCount() == event.getMaxValue()) {
            NTexts text = NTexts.of(event.getSession());
            double globalSeconds = event.getDuration().getTimeAsDoubleSeconds();
            long globalSpeed = globalSeconds == 0 ? 0 : (long) (event.getCurrentCount() / globalSeconds);
            long partialSpeed = partialSeconds == 0 ? 0 : (long) (event.getPartialCount() / partialSeconds);
            double percent = event.getProgress();

            NTextBuilder formattedLine = text.ofBuilder();
            NText p = bar.progress(event.isIndeterminate() ? -1 : (int) (event.getProgress()));
            if (p == null || p.isEmpty()) {
                return false;
            }
            formattedLine.append(p);
            BytesSizeFormat mf = new BytesSizeFormat("BTD1F", event.getSession());

            if(Double.isNaN(percent)){
                formattedLine.append(" ").append(text.ofStyled(String.format("%6s", ""), NTextStyle.config())).append("  ");
            }else {
                formattedLine.append(" ").append(text.ofStyled(String.format("%6s", df.format(percent)), NTextStyle.config())).append("% ");
            }
            formattedLine.append(" ").append(text.ofStyled(String.format("%6s", mf.format(partialSpeed)), NTextStyle.config())).append("/s");
            if (event.getMaxValue() < 0) {
                if (globalSpeed == 0) {
                    formattedLine.append(" ( -- )");
                } else {
                    formattedLine.append(" (").append(text.ofStyled(mf.format(globalSpeed), NTextStyle.info())).append(")");
                }
            } else {
                formattedLine.append(" (").append(text.ofStyled(mf.format(event.getMaxValue()), NTextStyle.warn())).append(")");
            }
            if (event.getError() != null) {
                formattedLine.append(" ").append(text.ofStyled("ERROR", NTextStyle.error())).append(" ");
            }
            formattedLine.append(" ").append(event.getMessage()).append(" ");
            String ff = formattedLine.toString();
            int length = text.ofBuilder().append(ff).textLength();
            if (length < minLength) {
                CoreStringUtils.fillString(' ', minLength - length, formattedLine);
            } else {
                minLength = length;
            }
            bar.printProgress2(formattedLine.toText(),out);
            return true;
        }
        return false;
    }

}

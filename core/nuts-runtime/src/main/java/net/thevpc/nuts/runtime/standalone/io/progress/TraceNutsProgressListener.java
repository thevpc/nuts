/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.util.BytesSizeFormat;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.text.NutsText;
import net.thevpc.nuts.text.NutsTextBuilder;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsLoggerVerb;
import net.thevpc.nuts.util.NutsProgressEvent;
import net.thevpc.nuts.util.NutsProgressListener;

import java.text.DecimalFormat;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class TraceNutsProgressListener implements NutsProgressListener/*, NutsOutputStreamTransparentAdapter*/ {
    private static DecimalFormat df = new DecimalFormat("##0.00");

    private NutsPrintStream out;
    private int minLength;
    private CProgressBar bar;
    private boolean optionsProcessed = false;
    private ProgressOptions options;
    private NutsLogger logger;

    public TraceNutsProgressListener() {
//        this.session = session;
    }

    @Override
    public boolean onProgress(NutsProgressEvent event) {
        switch (event.getState()) {
            case START: {
                bar = CProgressBar.of(event.getSession());
                this.out = event.getSession().getTerminal().err();
                this.logger=NutsLogger.of(TraceNutsProgressListener.class,event.getSession());
                if (event.getSession().isPlainOut()) {
                    onProgress0(event, false);
                }
                return true;
            }
            case COMPLETE: {
                if (event.getSession().isPlainOut()) {
                    return onProgress0(event, true);
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

    public boolean onProgress0(NutsProgressEvent event, boolean end) {
        if (!optionsProcessed) {
            optionsProcessed = true;
            options = ProgressOptions.of(event.getSession());
        }
        double partialSeconds = event.getPartialDuration().getTimeAsDoubleSeconds();
        if (event.getCurrentCount() == 0 || partialSeconds > 0.5 || event.getCurrentCount() == event.getMaxValue()) {
            NutsTexts text = NutsTexts.of(event.getSession());
            Level armedLogLevel = options.getArmedLogLevel();
            if (options.isArmedNewline()) {
                out.print("\n");
            }else if (armedLogLevel!=null) {
                //
            }else{
                out.resetLine();
            }
            double globalSeconds = event.getDuration().getTimeAsDoubleSeconds();
            long globalSpeed = globalSeconds == 0 ? 0 : (long) (event.getCurrentCount() / globalSeconds);
            long partialSpeed = partialSeconds == 0 ? 0 : (long) (event.getPartialCount() / partialSeconds);
            double percent = event.getProgress();

            NutsTextBuilder formattedLine = text.ofBuilder();
            NutsText p = bar.progress(event.isIndeterminate() ? -1 : (int) (event.getProgress()));
            if (p == null || p.isEmpty()) {
                return false;
            }
            formattedLine.append(p);
            BytesSizeFormat mf = new BytesSizeFormat("BTD1F", event.getSession());

            formattedLine.append(" ").append(text.ofStyled(String.format("%6s", df.format(percent)), NutsTextStyle.config())).append("% ");
            formattedLine.append(" ").append(text.ofStyled(String.format("%6s", mf.format(partialSpeed)), NutsTextStyle.config())).append("/s");
            if (event.getMaxValue() < 0) {
                if (globalSpeed == 0) {
                    formattedLine.append(" ( -- )");
                } else {
                    formattedLine.append(" (").append(text.ofStyled(mf.format(globalSpeed), NutsTextStyle.info())).append(")");
                }
            } else {
                formattedLine.append(" (").append(text.ofStyled(mf.format(event.getMaxValue()), NutsTextStyle.warn())).append(")");
            }
            if (event.getError() != null) {
                formattedLine.append(" ").append(text.ofStyled("ERROR", NutsTextStyle.error())).append(" ");
            }
            formattedLine.append(" ").append(event.getMessage()).append(" ");
            String ff = formattedLine.toString();
            int length = text.ofBuilder().append(ff).textLength();
            if (length < minLength) {
                CoreStringUtils.fillString(' ', minLength - length, formattedLine);
            } else {
                minLength = length;
            }
            if (armedLogLevel!=null && logger!=null) {
                this.logger.with().level(armedLogLevel)
                        .verb(NutsLoggerVerb.PROGRESS)
                        .log(NutsMessage.ofNtf(formattedLine.toString()));
            }else {
                out.print(ff);
                out.flush();
            }
            return true;
        }
        return false;
    }

}

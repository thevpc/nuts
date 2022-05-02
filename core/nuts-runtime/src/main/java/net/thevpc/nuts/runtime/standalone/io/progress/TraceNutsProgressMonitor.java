/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io.progress;

import java.text.DecimalFormat;

import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.io.terminal.CoreTerminalUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.BytesSizeFormat;
import net.thevpc.nuts.text.NutsTextBuilder;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsProgressEvent;
import net.thevpc.nuts.util.NutsProgressMonitor;

/**
 * @author thevpc
 */
public class TraceNutsProgressMonitor implements NutsProgressMonitor/*, NutsOutputStreamTransparentAdapter*/ {
    private static DecimalFormat df = new DecimalFormat("##0.00");

    private NutsPrintStream out;
    private int minLength;
    private CProgressBar bar;
    private boolean optionsProcessed=false;
    private boolean optionNewline=false;

    public TraceNutsProgressMonitor() {
//        this.session = session;
    }

//    @Override
//    public OutputStream baseOutputStream() {
//        return out;
//    }

    @Override
    public void onStart(NutsProgressEvent event) {
        bar= CoreTerminalUtils.resolveProgressBar(event.getSession());
        this.out = event.getSession().getTerminal().err();
        if (event.getSession().isPlainOut()) {
            onProgress0(event, false);
        }
    }

    @Override
    public void onComplete(NutsProgressEvent event) {
        if (event.getSession().isPlainOut()) {
            onProgress0(event, true);
            //out.println();
        }
    }

    @Override
    public boolean onProgress(NutsProgressEvent event) {
        if (event.getSession().isPlainOut()) {
            return onProgress0(event, false);
        }
        return true;
    }

//    private String escapeText(NutsTexts text , String str) {
//        return text.builder().append(str).toString();
//    }

    public boolean onProgress0(NutsProgressEvent event, boolean end) {
        if(!optionsProcessed) {
            optionsProcessed=true;
            optionNewline= NutsProgressUtils.parseProgressOptions(event.getSession()).isArmedNewline();
        }
        double partialSeconds = event.getPartialMillis() / 1000.0;
        if (event.getCurrentValue() == 0 || partialSeconds > 0.5 || event.getCurrentValue() == event.getMaxValue()) {
            NutsTexts text = NutsTexts.of(event.getSession());
            if(!optionNewline) {
                out.resetLine();
//                out.run(NutsTerminalCommand.MOVE_LINE_START);
            }else{
                out.print("\n");
            }
            double globalSeconds = event.getTimeMillis() / 1000.0;
            long globalSpeed = globalSeconds == 0 ? 0 : (long) (event.getCurrentValue() / globalSeconds);
            long partialSpeed = partialSeconds == 0 ? 0 : (long) (event.getPartialValue() / partialSeconds);
            double percent = event.getPercent();

            NutsTextBuilder formattedLine = text.builder();
            String p = bar.progress(event.isIndeterminate() ? -1 : (int) (event.getPercent()));
            if(p==null|| p.isEmpty()){
                return false;
            }
            formattedLine.append(text.parse(p));
            BytesSizeFormat mf = new BytesSizeFormat("BTD1F", event.getSession());

            formattedLine.append(" ").append(text.ofStyled(String.format("%6s", df.format(percent)), NutsTextStyle.config())).append("% ");
            formattedLine.append(" ").append(text.ofStyled(String.format("%6s", mf.format(partialSpeed)),NutsTextStyle.config())).append("/s");
            if (event.getMaxValue() < 0) {
                if (globalSpeed == 0) {
                    formattedLine.append(" ( -- )");
                } else {
                    formattedLine.append(" (").append(text.ofStyled(mf.format(globalSpeed),NutsTextStyle.info())).append(")");
                }
            } else {
                formattedLine.append(" (").append(text.ofStyled(mf.format(event.getMaxValue()),NutsTextStyle.warn())).append(")");
            }
            if (event.getError() != null) {
                formattedLine.append(" ").append(text.ofStyled("ERROR",NutsTextStyle.error())).append(" ");
            }
            formattedLine.append(" ").append(event.getMessage()).append(" ");
            String ff = formattedLine.toString();
            int length = text.builder().append(ff).textLength();
            if (length < minLength) {
                CoreStringUtils.fillString(' ', minLength - length, formattedLine);
            } else {
                minLength = length;
            }
            out.print(ff);
            out.flush();
            return true;
        }
        return false;
    }

}

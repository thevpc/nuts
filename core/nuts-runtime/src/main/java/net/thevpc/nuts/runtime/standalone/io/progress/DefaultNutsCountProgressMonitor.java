/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.common.BytesSizeFormat;
import net.thevpc.nuts.runtime.core.terminals.CoreTerminalUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.core.format.text.FPrintCommands;

import java.io.PrintStream;
import java.text.DecimalFormat;

/**
 * @author thevpc
 */
public class DefaultNutsCountProgressMonitor implements NutsProgressMonitor/*, NutsOutputStreamTransparentAdapter*/ {

    private NutsPrintStream out;
    private int minLength;
    private NutsWorkspace ws;

    public DefaultNutsCountProgressMonitor() {
    }

//    @Override
//    public OutputStream baseOutputStream() {
//        return out;
//    }

    public DecimalFormat df(NutsProgressEvent event) {
        return new DecimalFormat("##0.00");
    }

    public BytesSizeFormat mf(NutsProgressEvent event) {
        return new BytesSizeFormat("BTD1F",event.getSession());
    }

    @Override
    public void onStart(NutsProgressEvent event) {
        this.out = event.getSession().getTerminal().out();
        if (event.getSession().isPlainOut()) {
            onProgress0(event, false);
        }
    }

    @Override
    public void onComplete(NutsProgressEvent event) {
        if (event.getSession().isPlainOut()) {
            onProgress0(event, true);
//            out.println();
        }
    }

    @Override
    public boolean onProgress(NutsProgressEvent event) {
        if (event.getSession().isPlainOut()) {
            return onProgress0(event, false);
        }
        return true;
    }
//
//    private String escapeText(NutsTextManager text , String str) {
//        return text.builder().append(str).toString();
//    }

    public boolean onProgress0(NutsProgressEvent event, boolean end) {
        double partialSeconds = event.getPartialMillis() / 1000.0;
        if (event.getCurrentValue() == 0 || partialSeconds > 0.5 || event.getCurrentValue() == event.getMaxValue()) {
            NutsTextManager text = event.getSession().getWorkspace().text();
            out.resetLine();
            double globalSeconds = event.getTimeMillis() / 1000.0;
            long globalSpeed = globalSeconds == 0 ? 0 : (long) (event.getCurrentValue() / globalSeconds);
            long partialSpeed = partialSeconds == 0 ? 0 : (long) (event.getPartialValue() / partialSeconds);
            double percent = event.getPercent();
            if (event.isIndeterminate()) {
                percent = end ? 100 : 0;
            }
//            int x = (int) (20.0 / 100.0 * percent);

            NutsTextBuilder formattedLine = text.builder();
            CProgressBar cp= CoreTerminalUtils.resolveProgressBar(event.getSession());

            formattedLine.append(text.parse(cp.progress((int)percent)));
//            if (x > 0) {
//                formattedLine.append(text.forStyled(
//                        CoreStringUtils.fillString("*", x),
//                        NutsTextStyle.primary(1)
//                ));
//            }
//            CoreStringUtils.fillString(' ', 20 - x, formattedLine);
//            formattedLine.append("]");
            BytesSizeFormat mf = mf(event);
            DecimalFormat df = df(event);
            formattedLine.append(" ").append(text.forStyled(String.format("%6s", df.format(percent)),NutsTextStyle.config())).append("% ");
            formattedLine.append(" ").append(text.forStyled(mf.format(partialSpeed),NutsTextStyle.config())).append("/s");
            if (event.getMaxValue() < 0) {
                if (globalSpeed == 0) {
                    formattedLine.append(" ( -- )");
                } else {
                    formattedLine.append(" (").append(text.forStyled(mf.format(globalSpeed),NutsTextStyle.info())).append(")");
                }
            } else {
                formattedLine.append(" (").append(text.forStyled(mf.format(event.getMaxValue()),NutsTextStyle.warn())).append(")");
            }
            if (event.getError() != null) {
                formattedLine.append(" ").append(text.forStyled("ERROR",NutsTextStyle.error())).append(" ");
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

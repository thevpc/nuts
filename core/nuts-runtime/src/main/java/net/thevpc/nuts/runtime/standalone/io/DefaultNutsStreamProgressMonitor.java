/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io;

import java.io.PrintStream;
import java.text.DecimalFormat;

import net.thevpc.nuts.*;

import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.bundles.common.BytesSizeFormat;
import net.thevpc.nuts.runtime.standalone.util.console.CProgressBar;
import net.thevpc.nuts.runtime.core.format.text.FPrintCommands;

/**
 * @author thevpc
 */
public class DefaultNutsStreamProgressMonitor implements NutsProgressMonitor/*, NutsOutputStreamTransparentAdapter*/ {
    private static DecimalFormat df = new DecimalFormat("##0.00");

    private NutsPrintStream out;
    private int minLength;
    private CProgressBar bar;
    private boolean optionsProcessed=false;
    private boolean optionNewline=false;

    public DefaultNutsStreamProgressMonitor() {
//        this.session = session;
    }

//    @Override
//    public OutputStream baseOutputStream() {
//        return out;
//    }

    @Override
    public void onStart(NutsProgressEvent event) {
        bar=new CProgressBar(event.getSession());
        this.out = event.getSession().getTerminal().out();
        if (event.getSession().isPlainOut()) {
            onProgress0(event, false);
        }
    }

    @Override
    public void onComplete(NutsProgressEvent event) {
        if (event.getSession().isPlainOut()) {
            onProgress0(event, true);
            out.println();
        }
    }

    @Override
    public boolean onProgress(NutsProgressEvent event) {
        if (event.getSession().isPlainOut()) {
            return onProgress0(event, false);
        }
        return true;
    }

    private String escapeText(NutsTextManager text , String str) {
        return text.builder().append(str).toString();
    }

    public boolean onProgress0(NutsProgressEvent event, boolean end) {
        if(!optionsProcessed) {
            optionsProcessed=true;
            optionNewline= NutsWorkspaceUtils.parseProgressOptions(event.getSession()).contains("newline");
        }
        double partialSeconds = event.getPartialMillis() / 1000.0;
        if (event.getCurrentValue() == 0 || partialSeconds > 0.5 || event.getCurrentValue() == event.getMaxValue()) {
            NutsTextManager text = event.getSession().getWorkspace().text();
            if(!optionNewline) {
                out.run(NutsTerminalCommand.MOVE_LINE_START);
            }else{
                out.print("\n");
            }
            double globalSeconds = event.getTimeMillis() / 1000.0;
            long globalSpeed = globalSeconds == 0 ? 0 : (long) (event.getCurrentValue() / globalSeconds);
            long partialSpeed = partialSeconds == 0 ? 0 : (long) (event.getPartialValue() / partialSeconds);
            double percent = event.getPercent();

            StringBuilder formattedLine = new StringBuilder();
            String p = bar.progress(event.isIndeterminate() ? -1 : (int) (event.getPercent()));
            if(p==null|| p.isEmpty()){
                return false;
            }
            formattedLine.append(p);
            BytesSizeFormat mf = new BytesSizeFormat("BTD1F", event.getSession());

            formattedLine.append(" ").append(escapeText(text,String.format("%6s", df.format(percent)))).append("% ");
            formattedLine.append(" ##:config:").append(escapeText(text,mf.format(partialSpeed))).append("/s##");
            if (event.getMaxValue() < 0) {
                if (globalSpeed == 0) {
                    formattedLine.append(escapeText(text," ( -- )"));
                } else {
                    formattedLine.append(" (##:info:").append(escapeText(text,mf.format(globalSpeed))).append("##)");
                }
            } else {
                formattedLine.append(" (##:warn:").append(escapeText(text,mf.format(event.getMaxValue()))).append("##)");
            }
            if (event.getError() != null) {
                formattedLine.append(" ```error ERROR``` ");
            }
            formattedLine.append(" ").append(escapeText(text,event.getMessage())).append(" ");
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

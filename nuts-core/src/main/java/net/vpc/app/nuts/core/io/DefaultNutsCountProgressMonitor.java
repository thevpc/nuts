/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.io;

import net.vpc.app.nuts.NutsProgressEvent;
import net.vpc.app.nuts.NutsProgressMonitor;
import net.vpc.app.nuts.NutsTerminalFormat;
import net.vpc.app.nuts.core.util.common.BytesSizeFormat;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.fprint.FPrintCommands;

import java.io.PrintStream;
import java.text.DecimalFormat;

/**
 * @author vpc
 */
public class DefaultNutsCountProgressMonitor implements NutsProgressMonitor/*, NutsOutputStreamTransparentAdapter*/ {

    private static DecimalFormat df = new DecimalFormat("##0.00");
    private static BytesSizeFormat mf = new BytesSizeFormat("BTD1F");
    private PrintStream out;
    private int minLength;

    public DefaultNutsCountProgressMonitor() {
//        this.session = session;
    }

//    @Override
//    public OutputStream baseOutputStream() {
//        return out;
//    }

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

    public boolean onProgress0(NutsProgressEvent event, boolean end) {
        double partialSeconds = event.getPartialMillis() / 1000.0;
        if (event.getCurrentValue() == 0 || partialSeconds > 0.5 || event.getCurrentValue() == event.getMaxValue()) {
            NutsTerminalFormat terminalFormat = event.getSession().getWorkspace().io().getTerminalFormat();
            out.print("`" + FPrintCommands.MOVE_LINE_START + "`");
            double globalSeconds = event.getTimeMillis() / 1000.0;
            long globalSpeed = globalSeconds == 0 ? 0 : (long) (event.getCurrentValue() / globalSeconds);
            long partialSpeed = partialSeconds == 0 ? 0 : (long) (event.getPartialValue() / partialSeconds);
            double percent = event.getPercent();
            if (event.isIndeterminate()) {
                percent = end ? 100 : 0;
            }
            int x = (int) (20.0 / 100.0 * percent);

            StringBuilder formattedLine = new StringBuilder();
            formattedLine.append("\\[");
            if (x > 0) {
                formattedLine.append("##");
                CoreStringUtils.fillString("\\*", x, formattedLine);
                formattedLine.append("##");
            }
            CoreStringUtils.fillString(' ', 20 - x, formattedLine);
            formattedLine.append("\\]");
            formattedLine.append(" ").append(terminalFormat.escapeText(String.format("%6s", df.format(percent)))).append("\\% ");
            formattedLine.append(" [[").append(terminalFormat.escapeText(mf.format(partialSpeed))).append("/s]]");
            if (event.getMaxValue() < 0) {
                if (globalSpeed == 0) {
                    formattedLine.append(terminalFormat.escapeText(" ( -- )"));
                } else {
                    formattedLine.append(" ([[").append(terminalFormat.escapeText(mf.format(globalSpeed))).append("]])");
                }
            } else {
                formattedLine.append(" ([[").append(terminalFormat.escapeText(mf.format(event.getMaxValue()))).append("]])");
            }
            if (event.getError() != null) {
                formattedLine.append(" @@ERROR@@ ");
            }
            formattedLine.append(" ").append(terminalFormat.escapeText(event.getMessage())).append(" ");
            String ff = formattedLine.toString();
            int length = terminalFormat.filterText(ff).length();
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

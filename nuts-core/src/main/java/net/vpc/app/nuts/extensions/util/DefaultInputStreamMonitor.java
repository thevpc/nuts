/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.OutputStreamTransparentAdapter;
import net.vpc.common.io.InputStreamEvent;
import net.vpc.common.io.InputStreamMonitor;
import net.vpc.common.util.BytesSizeFormat;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;

/**
 * @author vpc
 */
public class DefaultInputStreamMonitor implements InputStreamMonitor, OutputStreamTransparentAdapter {

    private static DecimalFormat df = new DecimalFormat("##0.00");
    private static BytesSizeFormat mf = new BytesSizeFormat("BTD1F");
    private PrintStream out;
    private int minLength;
    private NutsWorkspace ws;

    public DefaultInputStreamMonitor(NutsWorkspace ws,PrintStream out) {
        this.out = out;
        this.ws = ws;
    }

    @Override
    public OutputStream baseOutputStream() {
        return out;
    }

    @Override
    public void onStart(InputStreamEvent event) {
        onProgress0(event, true);
    }

    @Override
    public void onComplete(InputStreamEvent event) {
        onProgress0(event, false);
        out.println();
    }

    @Override
    public boolean onProgress(InputStreamEvent event) {
        return onProgress0(event, false);
    }

    public boolean onProgress0(InputStreamEvent event, boolean first) {
        double partialSeconds = event.getPartialMillis() / 1000.0;
        if (event.getGlobalCount() == 0 || partialSeconds > 0.5 || event.getGlobalCount() == event.getLength()) {
            //if (!first) {
                //if (event.getGlobalCount() != event.getLength() && event.getException() == null) {
                    //print command to move cursor to last line!
                    out.print("`move-line-start`");
//                    out.print("`move-line-start;move-up`");
                //}
            //}
            double globalSeconds = event.getGlobalMillis() / 1000.0;
            long globalSpeed = globalSeconds == 0 ? 0 : (long) (event.getGlobalCount() / globalSeconds);
            long partialSpeed = partialSeconds == 0 ? 0 : (long) (event.getPartialCount() / partialSeconds);
            double percent = 0;
            if (event.getLength() > 0) {
                percent = (double) (event.getGlobalCount() * 100.0 / event.getLength());
            } else {
                percent = 0;
            }
            int x = (int) (20.0 / 100.0 * percent);

            StringBuilder formattedLine = new StringBuilder();
            formattedLine.append("\\[");
            if (x > 0) {
                formattedLine.append("##");
                for (int i = 0; i < x; i++) {
                    formattedLine.append("\\*");
//                    formattedLine.append("\u2588");
                }
                formattedLine.append("##");
            }
            for (int i = x; i < 20; i++) {
                formattedLine.append(" ");
            }
            formattedLine.append("\\]");
            formattedLine.append(" ").append(String.format("%6s", df.format(percent))).append("\\% ");
            formattedLine.append(" [[").append(mf.format(partialSpeed)).append("/s]]");
//            formattedLine.append(" ([[").append(mf.format(globalSpeed)).append("/s]])");
            if (event.getLength() < 0) {
                if (globalSpeed == 0) {
                    formattedLine.append(" ( -- )");
                } else {
                    formattedLine.append(" ([[").append(mf.format(globalSpeed)).append("]])");
                }
            } else {
                formattedLine.append(" ([[").append(mf.format(event.getLength())).append("]])");
            }
            if (event.getException() != null) {
                formattedLine.append(" @@ERROR@@ ");
            }
            formattedLine.append(" ``").append(event.getSourceName()).append("`` ");
//            while (formattedLine.length() < 80) {
//                formattedLine.append(' ');
//            }
//            if (line.length() > 80) {
//                line.delete(80, line.length());
//            }

            String ff = formattedLine.toString();
            int length = ws.getParseManager().filterText(ff).length();
            if(length<minLength){
                while(length<minLength){
                    length++;
                    formattedLine.append(' ');
                }
            }else{
                minLength=length;
            }
            out.print(ff);
            return true;
        }
        return false;
    }

}

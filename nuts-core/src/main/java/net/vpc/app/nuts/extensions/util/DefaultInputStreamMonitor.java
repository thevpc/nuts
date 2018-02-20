/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.util;

import java.text.DecimalFormat;
import net.vpc.app.nuts.NutsPrintStream;

/**
 *
 * @author vpc
 */
public class DefaultInputStreamMonitor implements InputStreamMonitor {

    private static DecimalFormat df = new DecimalFormat("##0.00");
    private static BytesSizeFormatter mf = new BytesSizeFormatter("BTD1F");
    private NutsPrintStream out;
    public static void main(String[] args) {
        System.out.println(String.format("%6s", df.format(12.3)));
        System.out.println(String.format("%6s", df.format(122.3)));
    }

    public DefaultInputStreamMonitor(NutsPrintStream out) {
        this.out = out;
    }

    @Override
    public boolean onProgress(InputStreamEvent event) {
        double partialSeconds = event.getPartialNanos() / 1000000000.0;
        if (partialSeconds > 1 || event.getGlobalCount() == event.getLength()) {
            double globalSeconds = event.getGlobalNanos() / 1000000000.0;
            long globalSpeed = (long) (event.getGlobalCount() / globalSeconds);
            long partialSpeed = (long) (event.getPartialCount() / partialSeconds);
            double percent = 0;
            if (event.getLength() > 0) {
                percent = (double) (event.getGlobalCount() * 100.0 / event.getLength());
            } else {
                percent = 0;
            }
            int x = (int) (20.0 / 100.0 * percent);

            StringBuilder line = new StringBuilder();
                line.append("\\[");
            if (x > 0) {
                line.append("##");
                for (int i = 0; i < x; i++) {
                    line.append("\\*");
                }
                line.append("##");
            }
            for (int i = x; i < 20; i++) {
                line.append(" ");
            }
            line.append("\\]");
            line.append(" " + String.format("%6s", df.format(percent)) + "\\% ");
            line.append(" [[" + (mf.format(partialSpeed)) + "/s]] ([[" + (mf.format(globalSpeed)) + "/s]])");
            line.append(" [[" + event.getSourceName() + "]] ");
            while (line.length() < 80) {
                line.append(' ');
            }
//            if (line.length() > 80) {
//                line.delete(80, line.length());
//            }
            out.println(line.toString());
            if (event.getGlobalCount() != event.getLength()) {
                out.print("`move-line-start;move-up`");
            }
            return true;
        }
        return false;
    }

}

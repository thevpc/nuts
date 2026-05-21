/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.xtra.time;

import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.util.BytesSizeFormat;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.time.NProgressEvent;
import net.thevpc.nuts.time.NProgressListener;

import java.text.DecimalFormat;

/**
 * @author thevpc
 */
public class DefaultNCountProgressListener implements NProgressListener/*, NutsOutputStreamTransparentAdapter*/ {

    private NPrintStream out;
    private int minLength;

    public DefaultNCountProgressListener() {
    }

//    @Override
//    public OutputStream baseOutputStream() {
//        return out;
//    }

    public DecimalFormat df(NProgressEvent event) {
        return new DecimalFormat("##0.00");
    }

    public BytesSizeFormat mf(NProgressEvent event) {
        return new BytesSizeFormat("BTD1F");
    }

    @Override
    public boolean onProgress(NProgressEvent event) {
        switch (event.state()){
            case START:{
                this.out = event.session().terminal().err();
                if (event.session().isPlainOut()) {
                    onProgress0(event, false);
                }
                break;
            }
            case COMPLETE:{
                if (event.session().isPlainOut()) {
                    onProgress0(event, true);
//            out.println();
                }
                break;
            }
            case PROGRESS:{
                if (event.session().isPlainOut()) {
                    return onProgress0(event, false);
                }
                break;
            }
        }
        return true;
    }

    public boolean onProgress0(NProgressEvent event, boolean end) {
        double partialSeconds = event.partialDuration().timeAsDoubleSeconds();
        if (event.currentCount() == 0 || partialSeconds > 0.5 || event.currentCount() == event.maxValue()) {
            NTexts text = NTexts.of();
            out.resetLine();
            double globalSeconds = event.duration().timeAsDoubleSeconds();
            long globalSpeed = globalSeconds == 0 ? 0 : (long) (event.currentCount() / globalSeconds);
            long partialSpeed = partialSeconds == 0 ? 0 : (long) (event.partialCount() / partialSeconds);
            double percent = event.progress();
            if (event.isIndeterminate()) {
                percent = end ? 100 : 0;
            }
//            int x = (int) (20.0 / 100.0 * percent);

            NTextBuilder formattedLine = text.ofBuilder();
            CProgressBar cp= CProgressBar.of();

            formattedLine.append(cp.progress((int)percent));
//            if (x > 0) {
//                formattedLine.append(text.forStyled(
//                        CoreStringUtils.fillString("*", x),
//                        NutsTextStyle.primary1()
//                ));
//            }
//            CoreStringUtils.fillString(' ', 20 - x, formattedLine);
//            formattedLine.append("]");
            BytesSizeFormat mf = mf(event);
            DecimalFormat df = df(event);
            formattedLine.append(" ").append(text.ofStyled(String.format("%6s", df.format(percent)), NTextStyle.config())).append("% ");
            formattedLine.append(" ").append(text.ofStyled(mf.formatString(partialSpeed), NTextStyle.config())).append("/s");
            if (event.maxValue() < 0) {
                if (globalSpeed == 0) {
                    formattedLine.append(" ( -- )");
                } else {
                    formattedLine.append(" (").append(text.ofStyled(mf.formatString(globalSpeed), NTextStyle.info())).append(")");
                }
            } else {
                formattedLine.append(" (").append(text.ofStyled(mf.formatString(event.maxValue()), NTextStyle.warn())).append(")");
            }
            if (event.error() != null) {
                formattedLine.append(" ").append(text.ofStyled("ERROR", NTextStyle.error())).append(" ");
            }
            formattedLine.append(" ").append(event.message()).append(" ");
            String ff = formattedLine.toString();
            int length = text.ofBuilder().append(ff).length();
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

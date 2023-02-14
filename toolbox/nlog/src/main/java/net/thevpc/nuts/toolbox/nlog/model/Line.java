/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nlog.model;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.text.NTextStyle;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vpc
 */
public class Line {
    private static DecimalFormat DF = new DecimalFormat("00000");

    private long num;
    private String text;
    private NMsg marker;

    public Line(long num, String text) {
        this.num = num;
        this.text = text;
    }

    public long getNum() {
        return num;
    }

    public String getText() {
        return text;
    }

    public NMsg getMarker() {
        return marker;
    }

    public void setMarker(NMsg marker) {
        this.marker = marker;
    }

    @Override
    public String toString() {
        return ((marker == null ? "" : (marker + " ")) + DF.format(getNum())
                + " " + getText());
    }

    public NMsg toMsg(LineFormat lineFormat) {
        List<Object> params = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        NMsg marker = getMarker();
        if (marker != null) {
            sb.append("%s");
            params.add(marker);
        }
        if (lineFormat.isLineNumber()) {
            sb.append("[%s]");
            params.add(NMsg.ofStyled(DF.format(getNum()), NTextStyle.number()));
        }
        if (sb.length() > 0) {
            sb.append(" ");
        }
        sb.append("%s");
        params.add(getText());
        return NMsg.ofC(sb.toString(), params.toArray());
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nlog.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vpc
 */
public class LineBuffer {

    private long rowNum;
    private List<Line> lines = new ArrayList<>();
    private int size;

    public LineBuffer(int size) {
        this.size = size;
    }

    public Line append(String line) {
        rowNum++;
        Line ll = new Line(rowNum, line);
        lines.add(ll);
        while (lines.size() > size) {
            lines.remove(0);
        }
        return ll;
    }

    public List<Line> getLast(int i) {
        ArrayList<Line> list = new ArrayList<>();
        if (i <= 0) {
            return list;
        }
        int x = lines.size() - i;
        if (x >= 0) {
            for (int j = x; j < lines.size(); j++) {
                list.add(lines.get(j));
            }
        }
        return list;
    }

    public Line getPrevious(int i) {
        int x = lines.size() - i;
        if (x >= 0) {
            return lines.get(x);
        }
        return null;
    }
}

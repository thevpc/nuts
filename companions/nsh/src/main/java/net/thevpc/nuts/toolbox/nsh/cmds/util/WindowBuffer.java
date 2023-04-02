/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nsh.cmds.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vpc
 */
public class WindowBuffer<T> {

    private List<T> lines = new ArrayList<>();
    private int size;

    public WindowBuffer(int size) {
        this.size = size;
    }

    public T append(T line) {
        lines.add(line);
        while (lines.size() > size) {
            lines.remove(0);
        }
        return line;
    }

    public List<T> getLast(int i) {
        ArrayList<T> list = new ArrayList<>();
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

    public T getPrevious(int i) {
        int x = lines.size() - i;
        if (x >= 0) {
            return lines.get(x);
        }
        return null;
    }
}

package net.thevpc.nuts.toolbox.nsh.cmds.util;

import java.util.ArrayList;
import java.util.List;

public class WindowObject<T> {
    private List<T> items;
    private int pivotIndex;

    public WindowObject(List<T> items, int pivotIndex) {
        this.items = items;
        this.pivotIndex = pivotIndex;
    }

    public List<T> getItems() {
        return items;
    }

    public int getPivotIndex() {
        return pivotIndex;
    }
}

package net.thevpc.nuts.toolbox.nsh.cmds.util;

import net.thevpc.nuts.toolbox.nsh.cmds.util.filter.AndWindowFilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WindowFilterIterator<T> implements Iterator<WindowObject<T>> {
    T pushedBack = null;
    Iterator<T> base = null;
    WindowBuffer<T> buffer;
    int windowMin;
    int windowMax;
    int windowMaxBase;
    int windowMinBase;
    WindowFilter<T> filter;
    WindowObject<T> ret;

    public WindowFilterIterator(Iterator<T> base, WindowFilter<T> filter, Integer windowMin, Integer windowMax) {
        this.base = base;
        if (windowMin == null) {
            windowMin = 0;
        }
        if (windowMax == null) {
            windowMax = 0;
        }
        this.filter = filter;
        if (this.filter == null) {
            this.filter = new AndWindowFilter<>();
        }
        this.windowMinBase = Math.max(0, windowMin);
        int hardMin = Math.max(0, Math.max(windowMinBase, this.filter.getPreviousWindowSize()));
        buffer = new WindowBuffer<>(hardMin + 10);
        this.windowMax = windowMax;
        int windowMaxBase = Math.max(0, windowMax);
        this.windowMax = Math.max(0, Math.max(windowMaxBase, this.filter.getNextWindowSize()));
    }

    @Override
    public boolean hasNext() {
        while (true) {
            T ll;
            if (pushedBack != null) {
                ll = pushedBack;
                pushedBack = null;
            } else {
                pushedBack = null;
                T line;
                line = nextFromBase();
                if (line == null) {
                    return false;
                }
                ll = buffer.append(line);
            }
            if (filter.accept(ll)) {
                List<T> all = new ArrayList<>();
                int win = windowMinBase + 1 + 1;
                while (win < windowMin) {
                    T line2 = buffer.getPrevious(win);
                    if (filter.acceptPrevious(line2, ll, 0, null)) {
                        win++;
                    } else {
                        break;
                    }
                }
                all.addAll(buffer.getLast(win + 1));
                int pivotIndex = all.size() - 1;
                int wm = windowMaxBase;
                while (wm > 0) {
                    T line = nextFromBase();
                    if (line == null) {
                        break;
                    }
                    ll = buffer.append(line);
                    all.add(ll);
                    wm--;
                }
                wm = windowMax - windowMaxBase;
                while (wm > 0) {
                    T line = nextFromBase();
                    if (line == null) {
                        break;
                    }
                    T ll2 = buffer.append(line);
                    if (filter.acceptNext(ll2, ll, 0, null)) {
                        all.add(ll2);
                        wm--;
                    } else {
                        pushedBack = ll2;
                        break;
                    }
                }
                filter.prepare(all, pivotIndex);
                if (!all.isEmpty()) {
                    ret = new WindowObject<>(all, pivotIndex);
                    return true;
                }
            }
        }
    }

    private T nextFromBase() {
        if (base.hasNext()) {
            return base.next();
        } else {
            return null;
        }
    }

    @Override
    public WindowObject<T> next() {
        return ret;
    }
}

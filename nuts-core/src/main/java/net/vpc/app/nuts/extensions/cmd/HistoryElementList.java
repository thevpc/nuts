package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.HistoryElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HistoryElementList {
    private List<HistoryElement> history = new ArrayList<>();

    public void add(String e) {
        add(new DefaultHistoryElement(e));
    }

    public void add(HistoryElement e) {
        for (Iterator<HistoryElement> iterator = history.iterator(); iterator.hasNext(); ) {
            HistoryElement historyElement = iterator.next();
            if (historyElement.getCommand().equals(e.getCommand())) {
                iterator.remove();
            }
        }
        history.add(e);
        //append file ??
    }

    public List<HistoryElement> getElements(int maxElements) {
        List<HistoryElement> all = new ArrayList<>();
        if (maxElements <= 0 || maxElements > history.size()) {
            maxElements = history.size();
        }
        for (int i = 0; i < maxElements; i++) {
            all.add(history.get(history.size() - maxElements + i));
        }
        return all;
    }

    public void save() {

    }

    public void load() {

    }
}

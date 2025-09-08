package net.thevpc.nuts.runtime.standalone.text.art.table;

import java.util.*;

public class Bounds {

    List<Integer> columnSize = new ArrayList<>();
    List<Integer> rowSize = new ArrayList<>();
    Map<Interval, Integer> columnIntervalSize = new HashMap<Interval, Integer>();
    Map<Interval, Integer> rowIntervalSize = new HashMap<Interval, Integer>();

    Set<Pos> reserved = new HashSet<>();

    public void discardRow(int row) {
        for (Iterator<Pos> iterator = reserved.iterator(); iterator.hasNext(); ) {
            Pos pos = iterator.next();
            if (pos.row == row) {
                iterator.remove();
            }
        }
    }

    public boolean isReserved(int col, int row) {
        return reserved.contains(new Pos(col, row));
    }

    public void addReservation(int col, int row) {
        reserved.add(new Pos(col, row));
    }

    public void setColumnMinSize(int index, int size) {
        while (columnSize.size() < index + 1) {
            columnSize.add(0);
        }
        columnSize.set(index, Math.max(size, columnSize.get(index)));
    }

    public void setRowMinSize(int index, int size) {
        while (rowSize.size() < index + 1) {
            rowSize.add(0);
        }
        rowSize.set(index, Math.max(size, rowSize.get(index)));
    }

    public void setColumnSize(int index, int size) {
        while (columnSize.size() < index + 1) {
            columnSize.add(0);
        }
        columnSize.set(index, size);
    }

    public void setRowSize(int index, int size) {
        while (rowSize.size() < index + 1) {
            rowSize.add(0);
        }
        columnSize.set(index, size);
    }

    public void setColumnIntervalSize(int from, int to, int size) {
        if (from + 1 == to) {
            setColumnSize(from, size);
            return;
        }
        columnIntervalSize.put(new Interval(from, to), size);
    }

    public void setRowIntervalSize(int from, int to, int size) {
        if (from + 1 == to) {
            setRowSize(from, size);
            return;
        }
        rowIntervalSize.put(new Interval(from, to), size);
    }

    public void setColumnIntervalMinSize(int from, int to, int size) {
        if (from + 1 == to) {
            setColumnMinSize(from, size);
            return;
        }
        Interval key = new Interval(from, to);

        columnIntervalSize.put(key, Math.max(
                (columnIntervalSize.containsKey(key) ? columnIntervalSize.get(key) : 0),
                size));
    }

    public void setRowIntervalMinSize(int from, int to, int size) {
        if (from + 1 == to) {
            setRowMinSize(from, size);
            return;
        }
        Interval key = new Interval(from, to);
        rowIntervalSize.put(key, Math.max(
                (rowIntervalSize.containsKey(key) ? rowIntervalSize.get(key) : 0),
                size));
    }

    public int evalCharLineWidth(int col, int colspan) {
        if (colspan <= 0) {
            return 0;
        }
        if (colspan == 1) {
            return columnSize.get(col);
        }

        int best = 0;
        for (Map.Entry<Interval, Integer> e : columnIntervalSize.entrySet()) {
            Interval interval = e.getKey();
            if (interval.from >= col && interval.to <= col + colspan) {
                int v = evalCharLineWidth(col, interval.from - col) + e.getValue() + evalCharLineWidth(interval.to, col + colspan - interval.to);
                if (v > best) {
                    best = v;
                }
            }
        }
        int v = evalCharLineWidth(col, 1) + evalCharLineWidth(col + 1, colspan - 1);
        if (v > best) {
            best = v;
        }
        return best;
    }

    public int evalCharLineHeight(int row, int rowspan) {
        if (rowspan <= 0) {
            return 0;
        }
        if (rowspan == 1) {
            return rowSize.get(row);
        }

        int best = 0;
        for (Map.Entry<Interval, Integer> e : rowIntervalSize.entrySet()) {
            Interval interval = e.getKey();
            if (interval.from >= row && interval.to <= row + rowspan) {
                int v = evalCharLineHeight(row, interval.from - row) + e.getValue() + evalCharLineHeight(interval.to, row + rowspan - interval.to);
                if (v > best) {
                    best = v;
                }
            }
        }
        int v = evalCharLineHeight(row, 1) + evalCharLineHeight(row + 1, rowspan - 1);
        if (v > best) {
            best = v;
        }
        return best;
    }

}

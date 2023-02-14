package net.thevpc.nuts.toolbox.nlog.filter;

import net.thevpc.nuts.toolbox.nlog.model.Line;
import net.thevpc.nuts.toolbox.nlog.model.LineFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vpc
 */
public class OrLineFilter implements LineFilter {

    private final List<LineFilter> all = new ArrayList<>();

    public OrLineFilter() {
    }

    public OrLineFilter(OrLineFilter other) {
        if (other != null) {
            for (LineFilter lineFilter : other.all) {
                or(lineFilter.copy());
            }
        }
    }

    public OrLineFilter or(LineFilter line) {
        if (line != null) {
            all.add(line);
        }
        return this;
    }

    @Override
    public boolean accept(Line line) {
        if (all.isEmpty()) {
            return true;
        }
        for (LineFilter lineFilter : all) {
            if (lineFilter.accept(line)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public LineFilter copy() {
        return new OrLineFilter(this);
    }

    @Override
    public String toString() {
        if (all.isEmpty()) {
            return "any";
        }
        if (all.size() == 1) {
            return all.get(0).toString();
        }
        return all.stream().map(x -> x.toString()).collect(Collectors.joining(" or "));
    }

    @Override
    public boolean acceptPrevious(Line line, Line pivot, int pivotIndex, List<Line> all) {
        for (LineFilter lineFilter : this.all) {
            if (lineFilter.acceptPrevious(line, pivot, pivotIndex, all)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean acceptNext(Line line, Line pivot, int pivotIndex, List<Line> all) {
        for (LineFilter lineFilter : this.all) {
            if (lineFilter.acceptNext(line, pivot, pivotIndex, all)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getPreviousWindowSize() {
        int max = 0;
        for (LineFilter lineFilter : this.all) {
            max = Math.max(max, lineFilter.getPreviousWindowSize());
        }
        return max;
    }

    @Override
    public int getNextWindowSize() {
        int max = 0;
        for (LineFilter lineFilter : this.all) {
            max = Math.max(max, lineFilter.getNextWindowSize());
        }
        return max;
    }

    @Override
    public LineFilter acceptanceResponsible(Line line) {
        for (LineFilter lineFilter : this.all) {
            if (lineFilter.accept(line)) {
                return lineFilter;
            }
        }
        return this;
    }
}

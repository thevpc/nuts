/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nlog.filter;

import net.thevpc.nuts.toolbox.nlog.model.Line;
import net.thevpc.nuts.toolbox.nlog.model.LineFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author vpc
 */
public class AndLineFilter implements LineFilter {

    private final List<LineFilter> all = new ArrayList<>();

    public AndLineFilter(AndLineFilter other) {
        if (other != null) {
            for (LineFilter lineFilter : other.all) {
                and(lineFilter.copy());
            }
        }
    }

    public AndLineFilter() {
    }

    public AndLineFilter and(LineFilter line) {
        if (line != null) {
            all.add(line);
        }
        return this;
    }

    @Override
    public boolean accept(Line line) {
        for (LineFilter lineFilter : all) {
            if (!lineFilter.accept(line)) {
                return false;
            }
        }
        return true;
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
    public LineFilter copy() {
        return new AndLineFilter(this);
    }

    public boolean isEmpty() {
        return all.isEmpty();
    }

    @Override
    public String toString() {
        if (all.isEmpty()) {
            return "any";
        }
        if (all.size() == 1) {
            return all.get(0).toString();
        }
        return all.stream().map(x -> x.toString()).collect(Collectors.joining(" and "));
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

}

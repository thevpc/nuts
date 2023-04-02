/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nsh.cmds.util.filter;

import net.thevpc.nuts.toolbox.nsh.cmds.util.WindowFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author vpc
 */
public class AndWindowFilter<T> implements WindowFilter<T> {

    private final List<WindowFilter<T>> all = new ArrayList<>();

    public AndWindowFilter(AndWindowFilter<T> other) {
        if (other != null) {
            for (WindowFilter<T> windowFilter : other.all) {
                and(windowFilter.copy());
            }
        }
    }

    public AndWindowFilter() {
    }

    public AndWindowFilter<T> and(WindowFilter<T> line) {
        if (line != null) {
            all.add(line);
        }
        return this;
    }

    @Override
    public boolean accept(T line) {
        for (WindowFilter<T> windowFilter : all) {
            if (!windowFilter.accept(line)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean acceptPrevious(T line, T pivot, int pivotIndex, List<T> all) {
        for (WindowFilter<T> windowFilter : this.all) {
            if (windowFilter.acceptPrevious(line, pivot, pivotIndex, all)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean acceptNext(T line, T pivot, int pivotIndex, List<T> all) {
        for (WindowFilter<T> windowFilter : this.all) {
            if (windowFilter.acceptNext(line, pivot, pivotIndex, all)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public WindowFilter<T> copy() {
        return new AndWindowFilter<T>(this);
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
        for (WindowFilter<T> windowFilter : this.all) {
            max = Math.max(max, windowFilter.getPreviousWindowSize());
        }
        return max;
    }

    @Override
    public int getNextWindowSize() {
        int max = 0;
        for (WindowFilter<T> windowFilter : this.all) {
            max = Math.max(max, windowFilter.getNextWindowSize());
        }
        return max;
    }

}

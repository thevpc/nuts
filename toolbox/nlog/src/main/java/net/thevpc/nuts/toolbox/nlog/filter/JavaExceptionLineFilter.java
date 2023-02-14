/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nlog.filter;

import net.thevpc.nuts.toolbox.nlog.model.LineFilter;
import net.thevpc.nuts.toolbox.nlog.model.Line;

import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author vpc
 */
public class JavaExceptionLineFilter implements LineFilter {

    private final Pattern atPattern = Pattern.compile("[ \t]at [a-z]+[.].*");
    private final Pattern javaPattern = Pattern.compile("[.]java:[0-9]+[)]");

    public JavaExceptionLineFilter() {
    }

    @Override
    public boolean accept(Line line) {
        String text = line.getText();
        if (atPattern.matcher(text).matches()) {
            return true;
        }
        if (javaPattern.matcher(text).matches()) {
            return true;
        }
        return false;
    }

    @Override
    public LineFilter copy() {
        //immutable
        return this;
    }

    @Override
    public String toString() {
        return "JavaException";
    }

    @Override
    public boolean acceptPrevious(Line line, Line pivot, int pivotIndex, List<Line> all) {
        if (pivot.getNum() == pivot.getNum() - 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean acceptNext(Line line, Line pivot, int pivotIndex, List<Line> all) {
        return accept(line);
    }

    @Override
    public int getPreviousWindowSize() {
        return 1;
    }

    @Override
    public int getNextWindowSize() {
        return 1000;
    }

}

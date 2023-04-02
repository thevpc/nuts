/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nsh.cmds.util.filter;

import net.thevpc.nuts.toolbox.nsh.cmds.util.WindowFilter;
import net.thevpc.nuts.toolbox.nsh.cmds.util.NNumberedObject;

import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author vpc
 */
public class JavaExceptionWindowFilter implements WindowFilter<NNumberedObject<String>> {

    private final Pattern atPattern = Pattern.compile("[ \t]at [a-z]+[.].*");
    private final Pattern javaPattern = Pattern.compile("[.]java:[0-9]+[)]");

    public JavaExceptionWindowFilter() {
    }

    @Override
    public boolean accept(NNumberedObject<String> line) {
        String text = line.getObject();
        if (atPattern.matcher(text).matches()) {
            return true;
        }
        if (javaPattern.matcher(text).matches()) {
            return true;
        }
        return false;
    }

    @Override
    public WindowFilter<NNumberedObject<String>> copy() {
        //immutable
        return this;
    }

    @Override
    public String toString() {
        return "JavaException";
    }

    @Override
    public boolean acceptPrevious(NNumberedObject<String> line, NNumberedObject<String> pivot, int pivotIndex, List<NNumberedObject<String>> all) {
        if (pivot.getNumber() == pivot.getNumber() - 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean acceptNext(NNumberedObject<String> line, NNumberedObject<String> pivot, int pivotIndex, List<NNumberedObject<String>> all) {
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

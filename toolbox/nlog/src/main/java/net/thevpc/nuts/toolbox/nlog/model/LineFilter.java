/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nlog.model;

import java.util.List;

/**
 * @author vpc
 */
public interface LineFilter {

    default int getPreviousWindowSize() {
        return 0;
    }

    default int getNextWindowSize() {
        return 0;
    }

    default boolean acceptPrevious(Line line, Line pivot, int pivotIndex, List<Line> all) {
        return false;
    }

    default boolean acceptNext(Line line, Line pivot, int pivotIndex, List<Line> all) {
        return false;
    }

    default LineFilter acceptanceResponsible(Line line) {
        if (accept(line)) {
            return this;
        }
        return null;
    }


    boolean accept(Line line);

    LineFilter copy();
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nsh.cmds.util;

import java.util.List;

/**
 * @author vpc
 */
public interface WindowFilter<T> {

    default int getPreviousWindowSize() {
        return 0;
    }

    default int getNextWindowSize() {
        return 0;
    }

    default boolean acceptPrevious(T line, T pivot, int pivotIndex, List<T> all) {
        return false;
    }

    default boolean acceptNext(T line, T pivot, int pivotIndex, List<T> all) {
        return false;
    }

    default WindowFilter<T> acceptanceResponsible(T line) {
        if (accept(line)) {
            return this;
        }
        return null;
    }


    boolean accept(T line);

    WindowFilter<T> copy();

    default void prepare(List<T> all, int pivotIndex) {

    }
}

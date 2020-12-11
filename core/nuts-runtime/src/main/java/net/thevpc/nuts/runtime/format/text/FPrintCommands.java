/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.format.text;

import java.io.PrintStream;

/**
 * @author vpc
 */
public class FPrintCommands {

    public static final String LATER_RESET_LINE = "later-reset-line";
    public static final String MOVE_LINE_START = "move-line-start";
    public static final String MOVE_UP = "move-up";

    public static void runLaterResetLine(PrintStream out) {
        runCommand(out, LATER_RESET_LINE);
    }

    public static void runMoveLineStart(PrintStream out) {
        runCommand(out, MOVE_LINE_START);
    }

    public static void runMoveUp(PrintStream out) {
        runCommand(out, MOVE_UP);
    }

    public static void runCommand(PrintStream out, String cmd) {
        out.print("ø```!" + cmd + "```ø");
    }

    public static void runLaterResetLine(StringBuilder out) {
        runCommand(out, LATER_RESET_LINE);
    }

    public static void runMoveLineStart(StringBuilder out) {
        runCommand(out, MOVE_LINE_START);
    }

    public static void runMoveUp(StringBuilder out) {
        runCommand(out, MOVE_UP);
    }

    public static void runCommand(StringBuilder out, String cmd) {
        out.append("ø```!" + cmd + "```ø");
    }
}

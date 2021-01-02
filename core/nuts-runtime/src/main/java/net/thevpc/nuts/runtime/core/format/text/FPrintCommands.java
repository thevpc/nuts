/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsTerminalManager;

import java.io.PrintStream;

/**
 * @author thevpc
 */
public class FPrintCommands {

    public static void runLaterResetLine(PrintStream out) {
        runCommand(out, NutsTerminalManager.CMD_LATER_RESET_LINE);
    }

    public static void runMoveLineStart(PrintStream out) {
        runCommand(out, NutsTerminalManager.CMD_MOVE_LINE_START);
    }

    public static void runMoveUp(PrintStream out) {
        runCommand(out, NutsTerminalManager.CMD_MOVE_UP);
    }

    public static void runCommand(PrintStream out, String cmd) {
        out.print("ø```!" + cmd + "```ø");
    }

    public static void runLaterResetLine(StringBuilder out) {
        runCommand(out, NutsTerminalManager.CMD_LATER_RESET_LINE);
    }

    public static void runMoveLineStart(StringBuilder out) {
        runCommand(out, NutsTerminalManager.CMD_MOVE_LINE_START);
    }

    public static void runMoveUp(StringBuilder out) {
        runCommand(out, NutsTerminalManager.CMD_MOVE_UP);
    }

    public static void runCommand(StringBuilder out, String cmd) {
        out.append("ø```!" + cmd + "```ø");
    }
}

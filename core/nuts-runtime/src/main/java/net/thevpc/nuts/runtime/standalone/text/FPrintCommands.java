///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.thevpc.nuts.runtime.standalone.text;
//
//import java.io.PrintStream;
//
//import net.thevpc.nuts.NutsBlankable;
//import net.thevpc.nuts.NutsConstants;
//import net.thevpc.nuts.NutsTerminalCommand;
//
///**
// * @author thevpc
// */
//public class FPrintCommands {
//
//
//    public static void runLaterResetLine(StringBuilder out) {
//        runCommand(out, NutsTerminalCommand.LATER_RESET_LINE);
//    }
//
////    public static void runMoveLineStart(StringBuilder out) {
////        runCommand(out, NutsTerminalCommand.MOVE_LINE_START);
////    }
//
//    public static void runMoveUp(StringBuilder out) {
//        runCommand(out, NutsTerminalCommand.MOVE_UP);
//    }
//
//    public static void runCommand(StringBuilder out, NutsTerminalCommand cmd) {
//        out.append(NutsConstants.Ntf.SILENT);
//        out.append("```!").append(cmd.getName());
//        if (!NutsBlankable.isBlank(cmd.getArgs())) {
//            out.append(" ");
//            out.append(cmd.getArgs());
//        }
//        out.append("```");
//        out.append(NutsConstants.Ntf.SILENT);
//    }
//}

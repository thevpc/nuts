///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.thevpc.nuts.toolbox.ntemplate.filetemplate.util;
//
//import java.io.*;
//
///**
// *
// * @author thevpc
// */
//public class ProcessUtils {
//
//    public static Object execProcess(String cmdLine, String dir) {
//        return execProcess(CommandlineUtils.parseCmdLine(commandline), dir);
//    }
//
//    public static Object execProcess(String[] cmdLine, String dir) {
//        ProcessBuilder pb = new ProcessBuilder(cmdLine);
//        pb.redirectErrorStream(true);
//        pb.directory(new File(dir));
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        Process p;
//        try {
//            p = pb.start();
//        } catch (IOException ex) {
//            throw new UncheckedIOException(ex);
//        }
//        try (InputStream in = p.getInputStream()) {
//            byte[] buffer = new byte[2048];
//            int r;
//            while ((r = in.read(buffer)) > 0) {
//                out.write(buffer, 0, r);
//            }
//        } catch (IOException ex) {
//            throw new UncheckedIOException(ex);
//        }
//        return out.toString();
//    }
//}

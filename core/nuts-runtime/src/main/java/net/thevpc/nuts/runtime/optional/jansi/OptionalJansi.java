///**
// * ====================================================================
// *            Nuts : Network Updatable Things Service
// *                  (universal package manager)
// * <br>
// * is a new Open Source Package Manager to help install packages and libraries
// * for runtime execution. Nuts is the ultimate companion for maven (and other
// * build managers) as it helps installing all package dependencies at runtime.
// * Nuts is not tied to java and is a good choice to share shell scripts and
// * other 'things' . Its based on an extensible architecture to help supporting a
// * large range of sub managers / repositories.
// * <br>
// *
// * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
// * or agreed to in writing, software distributed under the License is
// * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied. See the License for the specific language
// * governing permissions and limitations under the License.
// * <br> ====================================================================
// */
//package net.thevpc.nuts.runtime.optional.jansi;
//
//import java.io.IOException;
//import java.io.OutputStream;
//import java.io.PrintStream;
//import java.util.Locale;
//import net.thevpc.nuts.runtime.core.io.BaseTransparentFilterOutputStream;
//
///**
// *
// * @author vpc
// */
//public class OptionalJansi {
//
//    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
//
//    public static final boolean IS_CYGWIN = IS_WINDOWS
//            && System.getenv("PWD") != null
//            && System.getenv("PWD").startsWith("/")
//            && !"cygwin".equals(System.getenv("TERM"));
//
//    public static final boolean IS_MINGW_XTERM = IS_WINDOWS
//            && System.getenv("MSYSTEM") != null
//            && System.getenv("MSYSTEM").startsWith("MINGW")
//            && "xterm".equals(System.getenv("TERM"));
//
//    public static boolean isAvailable() {
//        try {
//            Class.forName("org.fusesource.jansi.AnsiOutputStream");
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    public static OutputStream preparestream(OutputStream base) {
//        if (isAvailable()) {
//            if (IS_WINDOWS && !IS_CYGWIN && !IS_MINGW_XTERM) {
//                // On windows we know the console does not interpret ANSI codes..
//                try {
//                    return new org.fusesource.jansi.WindowsAnsiPrintStream((base instanceof PrintStream) ? ((PrintStream) base) : new PrintStream(base));
//                } catch (Throwable ignore) {
//                    return new org.fusesource.jansi.AnsiOutputStream(base);
//                }
//            } else {
//                return new ResetOnCloseOutputStream(base);
//            }
//        }
//        return null;
//    }
//
//    private static class ResetOnCloseOutputStream extends BaseTransparentFilterOutputStream {
//
//        public ResetOnCloseOutputStream(OutputStream base) {
//            super(base);
//        }
//
//        @Override
//        public void close() throws IOException {
//            write(org.fusesource.jansi.AnsiOutputStream.RESET_CODE);
//            flush();
//            super.close();
//        }
//    }
//}

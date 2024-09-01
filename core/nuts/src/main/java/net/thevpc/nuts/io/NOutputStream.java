/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
//package net.thevpc.nuts.io;
//
//import net.thevpc.nuts.NSession;
//
//import java.io.OutputStream;
//
//public interface NOutputStream extends NOutputTarget{
//
////    static NOutputStream ofNull(NSession session) {
////        return NIO.of(session).ofNullOutputStream();
////    }
//
//    /**
//     * return new in-memory NutsPrintStream implementation.
//     * this is equivalent to {@code NutsMemoryPrintStream.of(session)}
//     *
//     * @param session session
//     * @return new in-memory NutsPrintStream implementation
//     */
//    static NMemoryPrintStream ofInMemory(NSession session) {
//        return NIO.of(session).ofInMemoryPrintStream();
//    }
//
//    static NMemoryPrintStream ofInMemory(NTerminalMode mode, NSession session) {
//        return NIO.of(session).ofInMemoryPrintStream(mode);
//    }
//
//    //static NOutputStream of(OutputStream out, NSession session) {
//    //    return NIO.of(session).ofOutputStream(out,null);
//    //}
//
////    /**
////     * create print stream that supports the given {@code mode}. If the given
////     * {@code out} is a PrintStream that supports {@code mode}, it should be
////     * returned without modification.
////     *
////     * @param out      stream to wrap
////     * @param mode     mode to support
////     * @param terminal terminal
////     * @param session  session
////     * @return {@code mode} supporting PrintStream
////     */
////    static NOutputStream of(OutputStream out, NTerminalMode mode, NSession session) {
////        return NIO.of(session).ofOutputStream(out, mode, terminal);
////    }
////
////    static NOutputStream of(Writer out, NSession session) {
////        return NIO.of(session).ofOutputStream(out);
////    }
//    NOutputStream flush(NSession session);
//
//    NOutputStream close(NSession session);
//
//    NOutputStream write(byte[] b,NSession session);
//
//    NOutputStream write(int b,NSession session);
//
//    NOutputStream writeRaw(byte[] buf, int off, int len,NSession session);
//
//    NOutputStream write(byte[] buf, int off, int len,NSession session);
//
//    NOutputStream write(char[] buf, int off, int len,NSession session);
//
//    NTerminalMode getTerminalMode();
//
//    boolean isAutoFlash();
//
//    /**
//     * update mode and return a new instance
//     *
//     * @param other new mode
//     * @return a new instance of NutsPrintStream (if the mode changes)
//     */
//    NOutputStream setTerminalMode(NTerminalMode other);
//
//    OutputStream asOutputStream(NSession session);
//
//    boolean isNtf();
//}

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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.optional.jansi;

import net.thevpc.nuts.NWorkspaceTerminalOptions;
import net.thevpc.nuts.util.NOsFamily;
import net.thevpc.nuts.NSession;

import java.util.List;

/**
 *
 * @author thevpc
 */
public class OptionalJansi {

    public static boolean isAvailable() {
        if (NOsFamily.getCurrent() == NOsFamily.WINDOWS) {
            try {
                Class.forName("org.fusesource.jansi.io.AnsiOutputStream");
                return true;
            } catch (Exception e) {
                //
            }
        }
        return false;
    }

    public static NWorkspaceTerminalOptions resolveStdFd(NSession session, List<String> flags) {
        boolean tty=flags.contains("tty");
        if(isAvailable()) {
            flags.add("jansi");
            if(System.console()!=null) {
                org.fusesource.jansi.AnsiConsole.systemInstall();
                flags.add("ansi");
                return new NWorkspaceTerminalOptions(System.in,System.out, System.err, flags.toArray(new String[0]));
            }else{
                if(tty){
                    flags.add("ansi");
                }else{
                    flags.add("raw");
                }
                return new NWorkspaceTerminalOptions(System.in,System.out, System.err, flags.toArray(new String[0]));
            }
        }
        return null;
    }

//
//    private static class ResetOnCloseOutputStream extends BaseTransparentFilterOutputStream {
//
//        public ResetOnCloseOutputStream(OutputStream base) {
//            super(base);
//        }
//
//        @Override
//        public void close() throws IOException {
//            write(org.fusesource.jansi.io.AnsiOutputStream.RESET_CODE);
//            flush();
//            super.close();
//        }
//    }
}

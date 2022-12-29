/**
 * ====================================================================
 *             Doovos (Distributed Object Oriented Operating System)
 *
 * Doovos is a new Open Source Distributed Object Oriented Operating System
 * Design and implementation based on the Java Platform.
 * Actually, it is a try for designing a distributed operation system in
 * top of existing centralized/network OS.
 * Designed OS will follow the object oriented architecture for redefining
 * all OS resources (memory,process,file system,device,...etc.) in a highly
 * distributed context.
 * Doovos is also a distributed Java virtual machine that implements JVM
 * specification on top the distributed resources context.
 *
 * Doovos BIN is a standard implementation for Doovos boot sequence, shell and
 * common application tools. These applications are running onDoovos guest JVM
 * (distributed jvm).
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.toolbox.nsh.jshell;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;

/**
 * @author thevpc (taha.bensalah@gmail.com)
 * %lastmodified 13 nov. 2004 Time: 21:17:13
 */
public class JShellCmdSyntaxError extends JShellException {

    public JShellCmdSyntaxError(NSession session, int result, String[] args, String cmd, String desc, String message) {
        super(session, buildMessage(args, cmd, desc, message,session),result);
    }

    private static NMsg buildMessage(String[] args, String cmd, String desc, String message, NSession session) {
        StringBuilder s = new StringBuilder();
        if (cmd != null) {
            s.append(cmd);
            s.append(" : ");
            s.append("Syntax Error.\n");
        }
        s.append(message);
        if (args != null) {
            s.append("\nPlease check command parameters.");
            s.append("\nArguments where : ");
            for (int i = 0; i < args.length; i++) {
                String a = args[i];
                if (i > 0) {
                    s.append(", ");
                }
                s.append("\"");
                s.append(a);
                s.append("\"");
            }
        }
        if (desc != null) {
            s.append("\n");
            s.append(desc);
        }
        return NMsg.ofPlain(s.toString());
    }
}

/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.NExecCommand;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellContext;
import net.thevpc.nuts.NExecutableInformation;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellCommandType;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellCommandTypeResolver;

/**
 *
 * @author thevpc
 */
public class NCommandTypeResolver implements JShellCommandTypeResolver {

    @Override
    public JShellCommandType type(String item, JShellContext context) {
        String a = context.aliases().get(item);
        if (a != null) {
            return new JShellCommandType(item, "shell alias", a, item + " is aliased to " + a);
        }
        String path = item;
        if (!item.startsWith("/")) {
            path = context.getCwd() + "/" + item;
        }
        NSession session = context.getSession();
        final NExecutableInformation w = NExecCommand.of(session).addCommand(item).which();
        if (w != null) {
            return new JShellCommandType(item, "nuts " + w.getType().toString().toLowerCase(), w.getValue(), w.getDescription());
        }
        if (NPath.of(path, session).exists()) {
            return new JShellCommandType(item, "path", path, item + " is " + path);
        }
        return null;
    }

}

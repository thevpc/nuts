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
package net.thevpc.nuts.toolbox.nsh.cmdresolver;

import net.thevpc.nuts.NExecCmd;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;
import net.thevpc.nuts.NExecutableInformation;

/**
 *
 * @author thevpc
 */
public class NCommandTypeResolver implements NShellCommandTypeResolver {

    @Override
    public NShellCommandResolution type(String item, NShellContext context) {
        String a = context.aliases().get(item);
        if (a != null) {
            return new NShellCommandResolution(item, "shell alias", a, item + " is aliased to " + a);
        }
        String path = item;
        if (!item.startsWith("/")) {
            path = context.getDirectory() + "/" + item;
        }
        NSession session = context.getSession();
        try(NExecutableInformation w = NExecCmd.of().addCommand(item).which()) {
            if (w != null) {
                return new NShellCommandResolution(item, "nuts " + w.getType().toString().toLowerCase(), w.getValue(), w.getDescription());
            }
            if (NPath.of(path).exists()) {
                return new NShellCommandResolution(item, "path", path, item + " is " + path);
            }
            return null;
        }
    }

}

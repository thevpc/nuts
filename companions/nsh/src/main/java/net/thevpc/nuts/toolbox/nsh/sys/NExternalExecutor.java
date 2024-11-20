/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
package net.thevpc.nuts.toolbox.nsh.sys;

import net.thevpc.nuts.NExecCmd;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class NExternalExecutor implements NShellExternalExecutor {

    @Override
    public int execExternalCommand(String[] command, NShellContext context) {
        if(command.length>1 && command[0].equals("nuts")){
            //just remove it
            List<String> list = new ArrayList<>(Arrays.asList(command));
            list.remove(0);
            command=list.toArray(new String[0]);
        }
        return NExecCmd.of().addCommand(command).failFast()
                .setExecutionType(context.getSession().getExecutionType().orDefault())
                .setDirectory(NPath.of(context.getDirectory()))
                .run().getResultCode();
    }

}

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
package net.thevpc.nuts.toolbox.nsh.options;

import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.NutsArgumentCandidate;
import net.thevpc.nuts.NutsArgumentName;
import net.thevpc.nuts.NutsCommandAutoComplete;
import net.thevpc.nuts.NutsCommandLineManager;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellContext;

/**
 *
 * @author thevpc
 */
public class CommandNonOption implements NutsArgumentName {

    private JShellContext context;
    private String name;

    public CommandNonOption(String name, JShellContext context) {
        this.name = name;
        this.context = context;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<NutsArgumentCandidate> getCandidates(NutsCommandAutoComplete context) {
        List<NutsArgumentCandidate> all = new ArrayList<>();
        NutsCommandLineManager c = this.context.getSession().commandLine();
        for (JShellBuiltin command : this.context.builtins().getAll()) {
            all.add(c.createCandidate(command.getName()).build());
        }
        return all;
    }

}

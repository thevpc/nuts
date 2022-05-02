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
package net.thevpc.nuts.runtime.standalone.app.cmdline.option;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.DefaultNutsArgumentCandidate;
import net.thevpc.nuts.cmdline.NutsArgumentCandidate;
import net.thevpc.nuts.cmdline.NutsCommandAutoComplete;

import java.util.*;

/**
 *
 * @author thevpc
 */
public class PermissionNonOption extends DefaultNonOption {
    private final boolean existing;
    private final String user;

//    public RightNonOption(String name, NutsConsoleContext context) {
//        super(name);
//        this.workspace = context.getWorkspace();
//    }
    public PermissionNonOption(String name, String user, boolean existing) {
        super(name);
        this.existing = existing;
        this.user = user;
    }

    @Override
    public List<NutsArgumentCandidate> getCandidates(NutsCommandAutoComplete context) {
        List<NutsArgumentCandidate> all = new ArrayList<>();
        for (String r : NutsConstants.Permissions.ALL) {
            all.add(new DefaultNutsArgumentCandidate(r));
        }
        Iterator<NutsArgumentCandidate> i = all.iterator();
        NutsRepository repository=context.get(NutsRepository.class);
        NutsUser info = repository != null ? repository.security()
                .setSession(context.getSession())
                .getEffectiveUser(user) : 
                context.getSession().security().setSession(context.getSession()).findUser(user);
        Set<String> rights = new HashSet<>(info == null ? Collections.emptyList() : (info.getPermissions()));
        while (i.hasNext()) {
            NutsArgumentCandidate right = i.next();
            if (existing) {
                if (!rights.contains(right.getValue())) {
                    i.remove();
                }
            } else {
                if (rights.contains(right.getValue())) {
                    i.remove();
                }
            }
        }
        return all;
    }
}

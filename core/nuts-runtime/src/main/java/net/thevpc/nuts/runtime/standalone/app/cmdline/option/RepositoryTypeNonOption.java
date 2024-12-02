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
package net.thevpc.nuts.runtime.standalone.app.cmdline.option;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.DefaultNArgCandidate;
import net.thevpc.nuts.cmdline.NArgCandidate;
import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;

import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.util.NBlankable;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author thevpc
 */
public class RepositoryTypeNonOption extends DefaultNonOption {

    public RepositoryTypeNonOption(String name) {
        super(name);
    }

    @Override
    public List<NArgCandidate> getCandidates(NCmdLineAutoComplete context) {
        TreeSet<String> allValid = new TreeSet<>();
        allValid.add(NConstants.RepoTypes.NUTS);
        allValid.add(NConstants.RepoTypes.MAVEN);
        for (NAddRepositoryOptions repo : NWorkspace.of().getDefaultRepositories()) {
            if(repo.getConfig()!=null) {
                String t = NRepositoryUtils.getRepoType(repo.getConfig());
                if(!NBlankable.isBlank(t)){
                    allValid.add(t.trim());
                }
            }
        }
        List<NArgCandidate> all = new ArrayList<>();
        for (String v : allValid) {
            all.add(new DefaultNArgCandidate(v));
        }
        return all;
    }
}

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

import net.thevpc.nuts.cmdline.DefaultNArgCompleteCandidate;
import net.thevpc.nuts.cmdline.NArgCompleteCandidate;
import net.thevpc.nuts.cmdline.NArgCompleteResult;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.core.NRepository;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class RepositoryNonOption extends DefaultNonOption {


    public RepositoryNonOption(String name) {
        super(name);
    }


    @Override
    public NArgCompleteResult resolveCandidates() {
        List<NArgCompleteCandidate> all = new ArrayList<>();
        NRepository repository=null;
        if(repository!=null){
            if (repository.config().isSupportedMirroring()) {
                for (NRepository repo : repository.config().mirrors()) {
                    all.add(NArgCompleteCandidate.of(repo.name()));
                }
            }
        }else{
            for (NRepository repo : NWorkspace.of().repositories()) {
                all.add(NArgCompleteCandidate.of(repo.name()));
            }
        }
        return NArgCompleteResult.ofCandidates(all);
    }

}

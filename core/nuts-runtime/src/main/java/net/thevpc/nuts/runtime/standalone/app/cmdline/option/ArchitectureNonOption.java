/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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

import net.thevpc.nuts.cmdline.DefaultNArgCandidate;
import net.thevpc.nuts.cmdline.NArgCandidate;
import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class ArchitectureNonOption extends DefaultNonOption {

    public ArchitectureNonOption(String name) {
        super(name);
    }

    @Override
    public List<NArgCandidate> getCandidates(NCmdLineAutoComplete context) {
        List<NArgCandidate> all = new ArrayList<>();
        all.add(new DefaultNArgCandidate("\"linux x86\""));
        all.add(new DefaultNArgCandidate("\"linux x64\""));
        all.add(new DefaultNArgCandidate("\"win x86\""));
        all.add(new DefaultNArgCandidate("\"win x64\""));
        return all;
    }

}

/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.NInstallStatusFilter;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.reserved.util.InstallStatusFilterWithDescription;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NSimplifiable;

/**
 *
 * @author thevpc
 */
public abstract class AbstractInstallStatusFilter extends AbstractNFilter implements NInstallStatusFilter, NSimplifiable<NInstallStatusFilter> {

    public AbstractInstallStatusFilter(NWorkspace workspace, NFilterOp op) {
        super(workspace, op);
    }

    @Override
    public NInstallStatusFilter or(NInstallStatusFilter other) {
        return or((NFilter) other).to(NInstallStatusFilter.class);
    }

    @Override
    public NInstallStatusFilter and(NInstallStatusFilter other) {
        return and((NFilter) other).to(NInstallStatusFilter.class);
    }

    @Override
    public NInstallStatusFilter neg() {
        return super.neg().to(NInstallStatusFilter.class);
    }

    @Override
    public NFilter withDesc(NEDesc description) {
        if(description==null){
            return this;
        }
        return new InstallStatusFilterWithDescription(workspace, this,description);
    }
}
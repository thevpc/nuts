/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
package net.thevpc.nuts;


import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NMsg;

/**
 * Created by vpc on 1/15/17.
 *
 * @app.category Exceptions
 * @since 0.5.4
 */
public class NReadOnlyException extends NException {

    /**
     * Constructs a new NutsReadOnlyException exception
     *
     * @param location location
     */
    public NReadOnlyException(String location) {
        super(NMsg.ofC("cannot update readonly workspace %s", NText.ofStyled(location, NTextStyle.path())));
    }

    /**
     * Constructs a new NutsReadOnlyException exception
     */
    public NReadOnlyException() {
        this(NSession.of().isNotPresent()? null : String.valueOf(NWorkspace.get().getWorkspaceLocation()));
    }
}

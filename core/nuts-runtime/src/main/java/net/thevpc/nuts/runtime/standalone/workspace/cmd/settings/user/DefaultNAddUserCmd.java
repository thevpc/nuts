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
 *
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NConfigsExt;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

/**
 *
 * @author thevpc
 * @since 0.5.4
 */
public class DefaultNAddUserCmd extends AbstractNAddUserCmd {

    public DefaultNAddUserCmd(NSession session) {
        super(session);
    }

    @Override
    public NAddUserCmd run() {
        if (NBlankable.isBlank(getUsername())) {
            throw new NIllegalArgumentException(getSession(), NMsg.ofPlain("invalid user"));
        }
        checkSession();
        if (repository != null) {
            NRepositorySecurityManager sec = repository.security().setSession(session);
            NUserConfig security = new NUserConfig(getUsername(),
                    CoreStringUtils.chrToStr(sec
                            .createCredentials(getCredentials(), false, null)),
                    getGroups(), getPermissions());
            security.setRemoteIdentity(getRemoteIdentity());
            security.setRemoteCredentials(CoreStringUtils.chrToStr(sec.createCredentials(getRemoteCredentials(), true, null)));
            NRepositoryConfigManagerExt.of(repository.config())
                    .getModel()
                    .setUser(security, getSession());
        } else {
            checkSession();
            NSession session = getSession();
            NWorkspaceSecurityManager sec = NWorkspaceSecurityManager.of(session);
            NUserConfig security = new NUserConfig(getUsername(),
                    CoreStringUtils.chrToStr(sec.createCredentials(getCredentials(), false, null)),
                    getGroups(), getPermissions());
            security.setRemoteIdentity(getRemoteIdentity());
            security.setRemoteCredentials(CoreStringUtils.chrToStr(sec.createCredentials(getRemoteCredentials(), true, null)));
            NConfigsExt.of(NConfigs.of(session)).getModel()
                    .setUser(security, session);
        }
        return this;
    }

}

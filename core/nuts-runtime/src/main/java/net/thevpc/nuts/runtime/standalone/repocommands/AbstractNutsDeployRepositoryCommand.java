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
package net.thevpc.nuts.runtime.standalone.repocommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.spi.NutsDeployRepositoryCommand;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 *
 * @author thevpc
 * %category SPI Base
 */
public abstract class AbstractNutsDeployRepositoryCommand extends NutsRepositoryCommandBase<NutsDeployRepositoryCommand> implements NutsDeployRepositoryCommand {

    private NutsId id;
    private Object content;
    private NutsDescriptor descriptor;

    public AbstractNutsDeployRepositoryCommand(NutsRepository repo) {
        super(repo, "deploy");
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        if (super.configureFirst(cmd)) {
            return true;
        }
        return false;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsDeployRepositoryCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public Object getContent() {
        return content;
    }

    @Override
    public NutsDeployRepositoryCommand setContent(Path content) {
        this.content = content;
        return this;
    }

    @Override
    public NutsDeployRepositoryCommand setContent(URL content) {
        this.content = content;
        return this;
    }

    @Override
    public NutsDeployRepositoryCommand setContent(File content) {
        this.content = content;
        return this;
    }

    @Override
    public NutsDeployRepositoryCommand setContent(InputStream content) {
        this.content = content;
        return this;
    }

    @Override
    public NutsDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public NutsDeployRepositoryCommand setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    protected void checkParameters() {
        checkSession();
        NutsSession session = getSession();
        getRepo().security().setSession(session).checkAllowed(NutsConstants.Permissions.DEPLOY, "deploy");
        if (this.getId() == null) {
            throw new NutsIllegalArgumentException(session, "missing Id");
        }
        if (this.getContent() == null) {
            throw new NutsIllegalArgumentException(session, "missing Content");
        }
        if (this.getDescriptor() == null) {
            throw new NutsIllegalArgumentException(session, "missing Descriptor");
        }
        if (CoreStringUtils.isBlank(this.getId().getGroupId())) {
            throw new NutsIllegalArgumentException(session, "empty group");
        }
        if (CoreStringUtils.isBlank(this.getId().getArtifactId())) {
            throw new NutsIllegalArgumentException(session, "empty name");
        }
        if ((this.getId().getVersion().isBlank())) {
            throw new NutsIllegalArgumentException(session, "empty version");
        }
        if ("RELEASE".equals(this.getId().getVersion().getValue())
                || NutsConstants.Versions.LATEST.equals(this.getId().getVersion().getValue())) {
            throw new NutsIllegalArgumentException(session, "invalid version " + this.getId().getVersion());
        }
    }


}

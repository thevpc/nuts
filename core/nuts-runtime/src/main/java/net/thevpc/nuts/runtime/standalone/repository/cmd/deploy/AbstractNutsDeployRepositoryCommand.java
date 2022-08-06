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
package net.thevpc.nuts.runtime.standalone.repository.cmd.deploy;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsIO;
import net.thevpc.nuts.io.NutsInputSource;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.id.util.NutsIdUtils;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NutsRepositoryCommandBase;
import net.thevpc.nuts.spi.NutsDeployRepositoryCommand;
import net.thevpc.nuts.util.NutsUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * @author thevpc %category SPI Base
 */
public abstract class AbstractNutsDeployRepositoryCommand extends NutsRepositoryCommandBase<NutsDeployRepositoryCommand> implements NutsDeployRepositoryCommand {

    private NutsId id;
    private NutsInputSource content;
    private NutsDescriptor descriptor;

    public AbstractNutsDeployRepositoryCommand(NutsRepository repo) {
        super(repo, "deploy");
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        return super.configureFirst(cmd);
    }

    @Override
    public NutsInputSource getContent() {
        return content;
    }

    @Override
    public NutsDeployRepositoryCommand setContent(NutsInputSource content) {
        this.content = content;
        return this;
    }

    @Override
    public NutsDeployRepositoryCommand setContent(NutsPath content) {
        checkSession();
        this.content = content;
        return this;
    }

    @Override
    public NutsDeployRepositoryCommand setContent(Path content) {
        checkSession();
        this.content = content == null ? null : NutsPath.of(content, getSession());
        return this;
    }

    @Override
    public NutsDeployRepositoryCommand setContent(URL content) {
        checkSession();
        this.content = content == null ? null : NutsPath.of(content, getSession());
        return this;
    }

    @Override
    public NutsDeployRepositoryCommand setContent(File content) {
        checkSession();
        this.content = content == null ? null : NutsPath.of(content, getSession());
        return this;
    }

    @Override
    public NutsDeployRepositoryCommand setContent(InputStream content) {
        checkSession();
        this.content = content == null ? null : NutsIO.of(getSession()).createInputSource(content);
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

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsDeployRepositoryCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

    protected void checkParameters() {
        checkSession();
        NutsSession session = getSession();
        getRepo().security().setSession(session).checkAllowed(NutsConstants.Permissions.DEPLOY, "deploy");
        NutsIdUtils.checkLongId(getId(), session);
        NutsUtils.requireNonNull(this.getContent(), "content", getSession());
        NutsUtils.requireNonNull(this.getDescriptor(), "descriptor", getSession());
        if (this.getId().getVersion().isReleaseVersion()
                || this.getId().getVersion().isLatestVersion()
        ) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("invalid version %s", this.getId().getVersion()));
        }
    }

}

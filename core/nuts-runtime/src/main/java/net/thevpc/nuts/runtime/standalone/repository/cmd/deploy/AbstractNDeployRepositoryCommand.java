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
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.id.util.NIdUtils;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositoryCommandBase;
import net.thevpc.nuts.spi.NDeployRepositoryCommand;
import net.thevpc.nuts.util.NAssert;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * @author thevpc %category SPI Base
 */
public abstract class AbstractNDeployRepositoryCommand extends NRepositoryCommandBase<NDeployRepositoryCommand> implements NDeployRepositoryCommand {

    private NId id;
    private NInputSource content;
    private NDescriptor descriptor;

    public AbstractNDeployRepositoryCommand(NRepository repo) {
        super(repo, "deploy");
    }

    @Override
    public boolean configureFirst(NCommandLine cmd) {
        return super.configureFirst(cmd);
    }

    @Override
    public NInputSource getContent() {
        return content;
    }

    @Override
    public NDeployRepositoryCommand setContent(NInputSource content) {
        this.content = content;
        return this;
    }

    @Override
    public NDeployRepositoryCommand setContent(NPath content) {
        checkSession();
        this.content = content;
        return this;
    }

    @Override
    public NDeployRepositoryCommand setContent(Path content) {
        checkSession();
        this.content = content == null ? null : NPath.of(content, getSession());
        return this;
    }

    @Override
    public NDeployRepositoryCommand setContent(URL content) {
        checkSession();
        this.content = content == null ? null : NPath.of(content, getSession());
        return this;
    }

    @Override
    public NDeployRepositoryCommand setContent(File content) {
        checkSession();
        this.content = content == null ? null : NPath.of(content, getSession());
        return this;
    }

    @Override
    public NDeployRepositoryCommand setContent(InputStream content) {
        checkSession();
        this.content = content == null ? null : NIO.of(getSession()).createInputSource(content);
        return this;
    }

    @Override
    public NDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public NDeployRepositoryCommand setDescriptor(NDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    @Override
    public NId getId() {
        return id;
    }

    @Override
    public NDeployRepositoryCommand setId(NId id) {
        this.id = id;
        return this;
    }

    protected void checkParameters() {
        checkSession();
        NSession session = getSession();
        getRepo().security().setSession(session).checkAllowed(NConstants.Permissions.DEPLOY, "deploy");
        NIdUtils.checkLongId(getId(), session);
        NAssert.requireNonNull(this.getContent(), "content", getSession());
        NAssert.requireNonNull(this.getDescriptor(), "descriptor", getSession());
        if (this.getId().getVersion().isReleaseVersion()
                || this.getId().getVersion().isLatestVersion()
        ) {
            throw new NIllegalArgumentException(session, NMsg.ofCstyle("invalid version %s", this.getId().getVersion()));
        }
    }

}

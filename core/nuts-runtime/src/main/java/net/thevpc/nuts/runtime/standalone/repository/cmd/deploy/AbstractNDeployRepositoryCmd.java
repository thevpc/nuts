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
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.deploy;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositoryCmdBase;
import net.thevpc.nuts.spi.NDeployRepositoryCmd;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * @author thevpc %category SPI Base
 */
public abstract class AbstractNDeployRepositoryCmd extends NRepositoryCmdBase<NDeployRepositoryCmd> implements NDeployRepositoryCmd {

    private NId id;
    private NInputSource content;
    private NDescriptor descriptor;

    public AbstractNDeployRepositoryCmd(NRepository repo) {
        super(repo, "deploy");
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        return super.configureFirst(cmdLine);
    }

    @Override
    public NInputSource getContent() {
        return content;
    }

    @Override
    public NDeployRepositoryCmd setContent(NInputSource content) {
        this.content = content;
        return this;
    }

    @Override
    public NDeployRepositoryCmd setContent(NPath content) {
        this.content = content;
        return this;
    }

    @Override
    public NDeployRepositoryCmd setContent(Path content) {
        this.content = content == null ? null : NPath.of(content);
        return this;
    }

    @Override
    public NDeployRepositoryCmd setContent(URL content) {
        this.content = content == null ? null : NPath.of(content);
        return this;
    }

    @Override
    public NDeployRepositoryCmd setContent(File content) {
        this.content = content == null ? null : NPath.of(content);
        return this;
    }

    @Override
    public NDeployRepositoryCmd setContent(InputStream content) {
        this.content = content == null ? null : NInputSource.of(content);
        return this;
    }

    @Override
    public NDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public NDeployRepositoryCmd setDescriptor(NDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    @Override
    public NId getId() {
        return id;
    }

    @Override
    public NDeployRepositoryCmd setId(NId id) {
        this.id = id;
        return this;
    }

    protected void checkParameters() {
        getRepo().security().checkAllowed(NConstants.Permissions.DEPLOY, "deploy");
        CoreNIdUtils.checkLongId(getId());
        NAssert.requireNonNull(this.getContent(), "content");
        NAssert.requireNonNull(this.getDescriptor(), "descriptor");
        if (this.getId().getVersion().isReleaseVersion()
                || this.getId().getVersion().isLatestVersion()
        ) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid version %s", this.getId().getVersion()));
        }
    }

}

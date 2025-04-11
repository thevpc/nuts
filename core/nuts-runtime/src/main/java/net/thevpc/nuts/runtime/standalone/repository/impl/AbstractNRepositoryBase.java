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
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.*;

import net.thevpc.nuts.NIndexStore;

import net.thevpc.nuts.NSpeedQualifier;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.log.NLogUtils;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
import net.thevpc.nuts.runtime.standalone.repository.cmd.deploy.DefaultNDeployRepositoryCmd;
import net.thevpc.nuts.runtime.standalone.repository.cmd.fetch.DefaultNFetchContentRepositoryCmd;
import net.thevpc.nuts.runtime.standalone.repository.cmd.fetch.DefaultNFetchDescriptorRepositoryCmd;
import net.thevpc.nuts.runtime.standalone.repository.cmd.push.DefaultNPushRepositoryCmd;
import net.thevpc.nuts.runtime.standalone.repository.cmd.search.DefaultNSearchRepositoryCmd;
import net.thevpc.nuts.runtime.standalone.repository.cmd.search.DefaultNSearchVersionsRepositoryCmd;
import net.thevpc.nuts.runtime.standalone.repository.cmd.undeploy.DefaultNRepositoryUndeployCmd;
import net.thevpc.nuts.runtime.standalone.repository.cmd.updatestats.AbstractNUpdateRepositoryStatsCmd;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNRepositoryConfigModel;
import net.thevpc.nuts.spi.*;

import java.util.*;
import java.util.logging.Level;
import net.thevpc.nuts.runtime.standalone.xtra.glob.GlobUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

/**
 * Created by vpc on 1/18/17.
 */
public abstract class AbstractNRepositoryBase extends AbstractNRepository implements NRepositoryExt {

    private static final long serialVersionUID = 1L;
    protected NIndexStore nIndexStore;


    public AbstractNRepositoryBase(NAddRepositoryOptions options, NRepository parentRepository, NSpeedQualifier speed, boolean supportedMirroring, String repositoryType, boolean supportsDeploy) {
        super();
        this.supportsDeploy=supportsDeploy;
        init(options, parentRepository, speed, supportedMirroring, repositoryType);
    }

    @Override
    public NIndexStore getIndexStore() {
        return nIndexStore;
    }

    protected void init(NAddRepositoryOptions options, NRepository parent, NSpeedQualifier speed, boolean supportedMirroring, String repositoryType) {
        this.parentRepository = parent;
        this.configModel = new DefaultNRepositoryConfigModel(this, options, workspace,speed, supportedMirroring, repositoryType);
        this.nIndexStore = NWorkspace.of().getIndexStoreClientFactory().createIndexStore(this);
//        setEnabled(options.isEnabled(), initSession);
    }

    @Override
    public boolean acceptAction(NId id, NRepositorySupportedAction supportedAction, NFetchMode mode) {
        String groups = config().getGroups();
        if (NBlankable.isBlank(groups)) {
            return true;
        }
        return GlobUtils.ofExact(groups).matcher(id.getGroupId()).matches();
    }

    @Override
    public String toString() {
        NRepositoryConfigManagerExt c = NRepositoryConfigManagerExt.of(config());
        String name = getName();
        String storePath = null;
        NRepositoryLocation loc = c.getModel().getLocation();
        String impl = getClass().getSimpleName();
        if (c != null) {
            storePath = c.getModel().getStoreLocation().toAbsolute().toString();
        }
        LinkedHashMap<String, String> a = new LinkedHashMap<>();
        if (name != null) {
            a.put("name", name);
        }
        a.put("impl", impl);
        if (storePath != null) {
            a.put("store", storePath);
        }
        if (loc != null) {
            a.put("location", loc.toString());
        }
        return a.toString();
    }

    @Override
    public void checkAllowedFetch(NId id) {
    }

    @Override
    public NFetchDescriptorRepositoryCmd fetchDescriptor() {
        return new DefaultNFetchDescriptorRepositoryCmd(this);
    }

    @Override
    public NId searchLatestVersion(NId id, NDefinitionFilter filter, NFetchMode fetchMode) {
        Iterator<NId> allVersions = searchVersions().setId(id).setFilter(filter)
                .setFetchMode(fetchMode)
                .getResult();
        NId a = null;
        while (allVersions.hasNext()) {
            NId next = allVersions.next();
            if (a == null || next.getVersion().compareTo(a.getVersion()) > 0) {
                a = next;
            }
        }
        return a;
    }

    protected void traceMessage(NFetchMode fetchMode, Level lvl, NId id, NLogVerb tracePhase, String title, long startTime, NMsg extraMessage) {
        NLogUtils.traceMessage(NLog.of(AbstractNRepositoryBase.class), lvl, getName(), fetchMode, id, tracePhase, title, startTime, extraMessage);
    }

    @Override
    public NDeployRepositoryCmd deploy() {
        return new DefaultNDeployRepositoryCmd(this);
    }

    @Override
    public NPushRepositoryCmd push() {
        return new DefaultNPushRepositoryCmd(this);
    }

    @Override
    public NSearchRepositoryCmd search() {
        return new DefaultNSearchRepositoryCmd(this);
    }

    @Override
    public NFetchContentRepositoryCmd fetchContent() {
        return new DefaultNFetchContentRepositoryCmd(this);
    }

    @Override
    public NSearchVersionsRepositoryCmd searchVersions() {
        return new DefaultNSearchVersionsRepositoryCmd(this);
    }

    @Override
    public NRepositoryUndeployCmd undeploy() {
        return new DefaultNRepositoryUndeployCmd(this);
    }

    protected String getIdComponentExtension(String packaging) {
        return NWorkspace.of().getDefaultIdContentExtension(packaging);
    }

    protected String getIdExtension(NId id) {
        return NWorkspace.of().getDefaultIdExtension(id);
    }

    @Override
    public NPath getIdBasedir(NId id) {
        return NWorkspace.of().getDefaultIdBasedir(id);
    }

    public NPath getIdRemotePath(NId id) {
        return config().getLocationPath().resolve(getIdRelativePath(id));
    }

    protected NPath getIdRelativePath(NId id) {
        return getIdBasedir(id).resolve(getIdFilename(id));
    }

    @Override
    public NUpdateRepositoryStatsCmd updateStatistics() {
        return new AbstractNUpdateRepositoryStatsCmd(this) {
            @Override
            public NUpdateRepositoryStatsCmd run() {
                return this;
            }
        };
    }

}

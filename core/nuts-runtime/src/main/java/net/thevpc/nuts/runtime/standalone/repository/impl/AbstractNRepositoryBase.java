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
import net.thevpc.nuts.io.NPath;
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
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

/**
 * Created by vpc on 1/18/17.
 */
public abstract class AbstractNRepositoryBase extends AbstractNRepository implements NRepositoryExt {

    private static final long serialVersionUID = 1L;
    protected NIndexStore nIndexStore;

    private final NLog LOG;

    public AbstractNRepositoryBase(NAddRepositoryOptions options, NSession session, NRepository parentRepository, NSpeedQualifier speed, boolean supportedMirroring, String repositoryType, boolean supportsDeploy) {
        this.initSession=session;
        this.supportsDeploy=supportsDeploy;
        LOG = NLog.of(AbstractNRepositoryBase.class,session);
        init(options, session, parentRepository, speed, supportedMirroring, repositoryType);
    }

    @Override
    public NIndexStore getIndexStore() {
        return nIndexStore;
    }

    protected void init(NAddRepositoryOptions options, NSession initSession, NRepository parent, NSpeedQualifier speed, boolean supportedMirroring, String repositoryType) {
        this.workspace = initSession.getWorkspace();
        this.parentRepository = parent;
        this.configModel = new DefaultNRepositoryConfigModel(this, options, initSession,speed, supportedMirroring, repositoryType);
        this.nIndexStore = NConfigs.of(initSession).getIndexStoreClientFactory().createIndexStore(this);
//        setEnabled(options.isEnabled(), initSession);
    }

    @Override
    public boolean acceptAction(NId id, NRepositorySupportedAction supportedAction, NFetchMode mode, NSession session) {
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
    public void checkAllowedFetch(NId id, NSession session) {
    }

    @Override
    public NFetchDescriptorRepositoryCmd fetchDescriptor() {
        return new DefaultNFetchDescriptorRepositoryCmd(this);
    }

    @Override
    public NId searchLatestVersion(NId id, NIdFilter filter, NFetchMode fetchMode, NSession session) {
        Iterator<NId> allVersions = searchVersions().setSession(session).setId(id).setFilter(filter)
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

    protected void traceMessage(NSession session, NFetchMode fetchMode, Level lvl, NId id, NLogVerb tracePhase, String title, long startTime, NMsg extraMessage) {
        NLogUtils.traceMessage(LOG, lvl, getName(), session, fetchMode, id, tracePhase, title, startTime, extraMessage);
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

    protected String getIdComponentExtension(String packaging, NSession session) {
        return NLocations.of(session).getDefaultIdContentExtension(packaging);
    }

    protected String getIdExtension(NId id, NSession session) {
        return NLocations.of(session).getDefaultIdExtension(id);
    }

    @Override
    public NPath getIdBasedir(NId id, NSession session) {
        return NLocations.of(session).getDefaultIdBasedir(id);
    }

    public NPath getIdRemotePath(NId id, NSession session) {
        return config().setSession(session).getLocationPath().resolve(getIdRelativePath(id, session));
    }

    protected NPath getIdRelativePath(NId id, NSession session) {
        return getIdBasedir(id, session).resolve(getIdFilename(id, session));
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

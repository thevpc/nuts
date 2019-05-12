/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.core.filters.DefaultNutsIdMultiFilter;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.TraceResult;
import net.vpc.app.nuts.NutsFetchDescriptorRepositoryCommand;

/**
 *
 * @author vpc
 */
public class DefaultNutsFetchDescriptorRepositoryCommand extends NutsRepositoryCommandBase<NutsFetchDescriptorRepositoryCommand> implements NutsFetchDescriptorRepositoryCommand {

    private static final Logger LOG = Logger.getLogger(DefaultNutsFetchDescriptorRepositoryCommand.class.getName());

    private NutsId id;
    private NutsDescriptor result;

    public DefaultNutsFetchDescriptorRepositoryCommand(NutsRepository repo) {
        super(repo);
    }

    @Override
    public NutsFetchDescriptorRepositoryCommand run() {
        CoreNutsUtils.checkSession(getSession());
        getRepo().security().checkAllowed(NutsConstants.Rights.FETCH_DESC, "fetch-descriptor");
        Map<String, String> queryMap = id.getQueryMap();
        queryMap.remove(NutsConstants.QueryKeys.OPTIONAL);
        queryMap.remove(NutsConstants.QueryKeys.SCOPE);
        queryMap.put(NutsConstants.QueryKeys.FACE, NutsConstants.QueryFaces.DESCRIPTOR);
        id = id.setQuery(queryMap);
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        xrepo.checkAllowedFetch(id, getSession());
        long startTime = System.currentTimeMillis();
        try {
            String versionString = id.getVersion().getValue();
            NutsDescriptor d = null;
            if (DefaultNutsVersion.isBlank(versionString)) {
                NutsId a = xrepo.findLatestVersion(id.setVersion(""), null, getSession());
                if (a == null) {
                    throw new NutsNotFoundException(id);
                }
                a = a.setFaceDescriptor();
                d = xrepo.fetchDescriptorImpl(a, getSession());
            } else if (DefaultNutsVersion.isStaticVersionPattern(versionString)) {
                id = id.setFaceDescriptor();
                d = xrepo.fetchDescriptorImpl(id, getSession());
            } else {
                NutsIdFilter filter = new DefaultNutsIdMultiFilter(id.getQueryMap(), new NutsPatternIdFilter(id), null, getRepo(), getSession()).simplify();
                NutsId a = xrepo.findLatestVersion(id.setVersion(""), filter, getSession());
                if (a == null) {
                    throw new NutsNotFoundException(id);
                }
                a = a.setFaceDescriptor();
                d = xrepo.fetchDescriptorImpl(a, getSession());
            }
            if (d == null) {
                throw new NutsNotFoundException(id);
            }
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(LOG, getRepo().config().name(), getSession(), id, TraceResult.SUCCESS, "Fetch descriptor", startTime);
            }
            result = d;
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(LOG, getRepo().config().name(), getSession(), id, TraceResult.ERROR, "Fetch descriptor", startTime);
            }
            throw ex;
        }
        return this;
    }

    @Override
    public NutsDescriptor getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

    @Override
    public NutsFetchDescriptorRepositoryCommand id(NutsId id) {
        return setId(id);
    }

    @Override
    public NutsFetchDescriptorRepositoryCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsId getId() {
        return id;
    }

}

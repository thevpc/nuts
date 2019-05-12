/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsContent;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsFetchContentRepositoryCommand;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.TraceResult;

/**
 *
 * @author vpc
 */
public class DefaultNutsFetchContentRepositoryCommand extends NutsRepositoryCommandBase<NutsFetchContentRepositoryCommand> implements NutsFetchContentRepositoryCommand {

    private static final Logger LOG = Logger.getLogger(DefaultNutsFetchContentRepositoryCommand.class.getName());

    private NutsId id;
    private NutsContent result;
    private NutsDescriptor descriptor;
    private Path localPath;
    
    public DefaultNutsFetchContentRepositoryCommand(NutsRepository repo) {
        super(repo);
    }

    @Override
    public NutsDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public NutsFetchContentRepositoryCommand setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }
    @Override
    public NutsFetchContentRepositoryCommand descriptor(NutsDescriptor descriptor) {
        return setDescriptor(descriptor);
    }

    
    @Override
    public Path getLocalPath() {
        return localPath;
    }

    @Override
    public NutsFetchContentRepositoryCommand localPath(Path localPath) {
        return setLocalPath(localPath);
    }
    
    @Override
    public NutsFetchContentRepositoryCommand setLocalPath(Path localPath) {
        this.localPath = localPath;
        return this;
    }

    @Override
    public NutsFetchContentRepositoryCommand run() {
        CoreNutsUtils.checkSession(getSession());
        NutsDescriptor descriptor0=descriptor;
        if (descriptor0 == null) {
            descriptor0 = getRepo().fetchDescriptor().id(id).session(getSession()).getResult();
        }
        id = id.setFaceComponent();
        getRepo().security().checkAllowed(NutsConstants.Rights.FETCH_CONTENT, "fetch-content");
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        xrepo.checkAllowedFetch(id, getSession());
        long startTime = System.currentTimeMillis();
        try {
            NutsContent f = xrepo.fetchContentImpl(id, descriptor0, localPath, getSession());
            if (f == null) {
                throw new NutsNotFoundException(id);
            }
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(LOG, getRepo().config().name(),getSession(), id, TraceResult.SUCCESS, "Fetch component", startTime);
            }
            result= f;
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(LOG, getRepo().config().name(),getSession(), id, TraceResult.ERROR, "Fetch component", startTime);
            }
            throw ex;
        }
        return this;
    }

    @Override
    public NutsContent getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

    @Override
    public NutsFetchContentRepositoryCommand id(NutsId id) {
        return setId(id);
    }

    @Override
    public NutsFetchContentRepositoryCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsId getId() {
        return id;
    }

}

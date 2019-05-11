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
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.NutsDeployRepositoryCommand;

/**
 *
 * @author vpc
 */
public class DefaultNutsDeployRepositoryCommand extends NutsRepositoryCommandBase<NutsDeployRepositoryCommand> implements NutsDeployRepositoryCommand {
    private static final Logger LOG = Logger.getLogger(DefaultNutsDeployRepositoryCommand.class.getName());

    private NutsId id;
    private Path content;
    private NutsDescriptor descriptor;
    private String repository;
    private boolean offline = false;
    private boolean transitive = true;

    public DefaultNutsDeployRepositoryCommand(NutsRepository repo) {
        super(repo);
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
    public Path getContent() {
        return content;
    }

    @Override
    public NutsDeployRepositoryCommand setContent(Path content) {
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

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NutsDeployRepositoryCommand setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NutsDeployRepositoryCommand setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public NutsDeployRepositoryCommand setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public NutsDeployRepositoryCommand id(NutsId id) {
        return setId(id);
    }

    @Override
    public NutsDeployRepositoryCommand repository(String repository) {
        return setRepository(repository);
    }

    @Override
    public NutsDeployRepositoryCommand offline() {
        return offline(true);
    }

    @Override
    public NutsDeployRepositoryCommand offline(boolean offline) {
        return setOffline(offline);
    }

    @Override
    public NutsDeployRepositoryCommand transitive(boolean transitive) {
        return setTransitive(transitive);
    }

//    @Override
//    public NutsRepositoryDeploymentOptions copy() {
//        return new DefaultNutsRepositoryDeploymentOptions()
//                .setContent(content)
//                .setDescriptor(descriptor)
//                .setId(id)
//                .setOffline(offline)
//                .setRepository(repository)
//                .setTransitive(transitive);
//
//    }

    @Override
    public NutsDeployRepositoryCommand run() {
        getRepo().security().checkAllowed(NutsConstants.Rights.DEPLOY, "deploy");
        if (this.getId() == null) {
            throw new NutsIllegalArgumentException("Missing Id");
        }
        if (this.getContent() == null) {
            throw new NutsIllegalArgumentException("Missing Content");
        }
        if (this.getDescriptor() == null) {
            throw new NutsIllegalArgumentException("Missing Descriptor");
        }
        if (CoreStringUtils.isBlank(this.getId().getGroup())) {
            throw new NutsIllegalArgumentException("Empty group");
        }
        if (CoreStringUtils.isBlank(this.getId().getName())) {
            throw new NutsIllegalArgumentException("Empty name");
        }
        if ((this.getId().getVersion().isBlank())) {
            throw new NutsIllegalArgumentException("Empty version");
        }
        if ("RELEASE".equals(this.getId().getVersion().getValue())
                || NutsConstants.Versions.LATEST.equals(this.getId().getVersion().getValue())) {
            throw new NutsIllegalArgumentException("Invalid version " + this.getId().getVersion());
        }
//        if (descriptor.getArch().length > 0 || descriptor.getOs().length > 0 || descriptor.getOsdist().length > 0 || descriptor.getPlatform().length > 0) {
//            if (CoreStringUtils.isEmpty(descriptor.getFace())) {
//                throw new NutsIllegalArgumentException("face property '" + NutsConstants.QUERY_FACE + "' could not be null if env {arch,os,osdist,platform} is specified");
//            }
//        }
        try {
            NutsRepositoryExt xrepo = NutsRepositoryExt.of(repo);
            xrepo.deployImpl(this);
            if (getSession().isIndexed() && xrepo.getIndexStoreClient() != null && xrepo.getIndexStoreClient().isEnabled()) {
                try {
                    xrepo.getIndexStoreClient().revalidate(this.getId());
                } catch (NutsException ex) {
                    LOG.log(Level.FINEST, "[ERROR  ] Error revalidating Indexer for {0} : {1}", new Object[]{getRepo().config().getName(), ex});
                }
            }
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "[SUCCESS] {0} Deploy {1}", new Object[]{CoreStringUtils.alignLeft(getRepo().config().getName(), 20), this.getId()});
            }
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "[ERROR  ] {0} Deploy {1}", new Object[]{CoreStringUtils.alignLeft(getRepo().config().getName(), 20), this.getId()});
            }
            throw ex;
        }
        return this;
    }
    

}

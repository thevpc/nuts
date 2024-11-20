package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
import net.thevpc.nuts.util.NIterator;

public class NSimpleRepositoryWrapper extends NCachedRepository {
    private NRepositoryModel base;
    private int mode;

    public NSimpleRepositoryWrapper(NAddRepositoryOptions options, NWorkspace workspace, NRepository parent, NRepositoryModel base) {
        super(options, workspace, parent,
                base.getSpeed(),
                (base.getMode() & NRepositoryModel.MIRRORING) != 0,
                base.getRepositoryType(),true
        );
        this.mode = base.getMode();
        lib.setReadEnabled((this.mode & NRepositoryModel.LIB_READ) != 0);
        lib.setWriteEnabled((this.mode & NRepositoryModel.LIB_WRITE) != 0);
        cache.setReadEnabled((this.mode & NRepositoryModel.CACHE_READ) != 0);
        cache.setWriteEnabled((this.mode & NRepositoryModel.CACHE_WRITE) != 0);
        this.base = base;
    }
    
    public NIterator<NId> searchVersionsCore(NId id, NIdFilter idFilter, NFetchMode fetchMode) {
        return base.searchVersions(id, idFilter, fetchMode, this);
    }

    public NId searchLatestVersionCore(NId id, NIdFilter filter, NFetchMode fetchMode) {
        return base.searchLatestVersion(id, filter, fetchMode, this);
    }

    public NDescriptor fetchDescriptorCore(NId id, NFetchMode fetchMode) {
        return base.fetchDescriptor(id, fetchMode, this);
    }

    public NPath fetchContentCore(NId id, NDescriptor descriptor, NFetchMode fetchMode) {
        return base.fetchContent(id, descriptor, fetchMode, this);
    }

    public NIterator<NId> searchCore(final NIdFilter filter, NPath[] basePaths, NId[] baseIds, NFetchMode fetchMode) {
        return base.search(filter, basePaths, fetchMode, this);
    }

    public void updateStatisticsImpl() {
        base.updateStatistics(this);
    }

    protected boolean isAllowedOverrideArtifact(NId id) {
        return ((this.mode & NRepositoryModel.LIB_OVERRIDE) != 0);
    }

    @Override
    public boolean acceptAction(NId id, NRepositorySupportedAction supportedAction, NFetchMode mode) {
        if(!super.acceptAction(id, supportedAction, mode)){
            return false;
        }
        switch (supportedAction){
            case DEPLOY: return base.acceptDeploy(id, mode, this);
            case SEARCH: return base.acceptFetch(id, mode, this);
        }
        return false;
    }

    @Override
    public boolean isAcceptFetchMode(NFetchMode mode) {
        return base.isAcceptFetchMode(mode);
    }

    @Override
    public boolean isRemote() {
        return base.isRemote();
    }
}

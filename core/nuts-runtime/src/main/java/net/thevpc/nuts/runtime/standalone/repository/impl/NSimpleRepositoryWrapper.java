package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
import net.thevpc.nuts.util.NIterator;

public class NSimpleRepositoryWrapper extends NCachedRepository {
    private NRepositoryModel base;
    private int mode;

    public NSimpleRepositoryWrapper(NAddRepositoryOptions options, NSession session, NRepository parent, NRepositoryModel base) {
        super(options, session, parent,
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
    
    public NIterator<NId> searchVersionsCore(NId id, NIdFilter idFilter, NFetchMode fetchMode, NSession session) {
        return base.searchVersions(id, idFilter, fetchMode, this, session);
    }

    public NId searchLatestVersionCore(NId id, NIdFilter filter, NFetchMode fetchMode, NSession session) {
        return base.searchLatestVersion(id, filter, fetchMode, this, session);
    }

    public NDescriptor fetchDescriptorCore(NId id, NFetchMode fetchMode, NSession session) {
        return base.fetchDescriptor(id, fetchMode, this, session);
    }

    public NPath fetchContentCore(NId id, NDescriptor descriptor, NFetchMode fetchMode, NSession session) {
        return base.fetchContent(id, descriptor, fetchMode, this, session);
    }

    public NIterator<NId> searchCore(final NIdFilter filter, NPath[] basePaths, NId[] baseIds, NFetchMode fetchMode, NSession session) {
        return base.search(filter, basePaths, fetchMode, this, session);
    }

    public void updateStatisticsImpl(NSession session) {
        base.updateStatistics(this, session);
    }

    protected boolean isAllowedOverrideArtifact(NId id) {
        return ((this.mode & NRepositoryModel.LIB_OVERRIDE) != 0);
    }

    @Override
    public boolean acceptAction(NId id, NRepositorySupportedAction supportedAction, NFetchMode mode, NSession session) {
        if(!super.acceptAction(id, supportedAction, mode, session)){
            return false;
        }
        switch (supportedAction){
            case DEPLOY: return base.acceptDeploy(id, mode, this, session);
            case SEARCH: return base.acceptFetch(id, mode, this, session);
        }
        return false;
    }

    @Override
    public boolean isAcceptFetchMode(NFetchMode mode, NSession session) {
        return base.isAcceptFetchMode(mode);
    }

    @Override
    public boolean isRemote() {
        return base.isRemote();
    }
}

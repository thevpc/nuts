package net.vpc.app.nuts.main.repos;

import net.vpc.app.nuts.*;

import java.nio.file.Path;
import java.util.Iterator;

public class NutsSimpleRepositoryWrapper extends NutsCachedRepository {
    private NutsRepositoryModel base;
    private int mode;

    public NutsSimpleRepositoryWrapper(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parent, NutsRepositoryModel base) {
        super(options, workspace, parent,
                base.getSpeed(),
                (base.getMode() & NutsRepositoryModel.MIRRORING) != 0,
                base.getRepositoryType()
        );
        this.mode = base.getMode();
        lib.setReadEnabled((this.mode & NutsRepositoryModel.LIB_READ) != 0);
        lib.setWriteEnabled((this.mode & NutsRepositoryModel.LIB_WRITE) != 0);
        cache.setReadEnabled((this.mode & NutsRepositoryModel.CACHE_READ) != 0);
        cache.setWriteEnabled((this.mode & NutsRepositoryModel.CACHE_WRITE) != 0);
        this.base = base;
    }

    public Iterator<NutsId> searchVersionsCore(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {
        return base.searchVersions(id, idFilter, session);
    }

    public NutsId searchLatestVersionCore(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        return base.searchLatestVersion(id, filter, session);
    }

    public NutsDescriptor fetchDescriptorCore(NutsId id, NutsRepositorySession session) {
        return base.fetchDescriptor(id, session);
    }

    public NutsContent fetchContentCore(NutsId id, NutsDescriptor descriptor, Path localPath, NutsRepositorySession session) {
        return base.fetchContent(id, descriptor, localPath, session);
    }

    public Iterator<NutsId> searchCore(final NutsIdFilter filter, String[] roots, NutsRepositorySession session) {
        return base.search(filter, roots, session);
    }

    public void updateStatistics2() {
        base.updateStatistics();
    }

    protected boolean isAllowedOverrideNut(NutsId id) {
        return ((this.mode & NutsRepositoryModel.LIB_OVERRIDE) != 0);
    }

    @Override
    public boolean acceptAction(NutsId id, NutsRepositorySupportedAction supportedAction, NutsFetchMode mode) {
        if(!super.acceptAction(id, supportedAction, mode)){
            return false;
        }
        return base.acceptAction(id, supportedAction, mode);
    }
}

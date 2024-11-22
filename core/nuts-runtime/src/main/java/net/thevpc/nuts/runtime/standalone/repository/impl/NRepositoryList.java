package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NIteratorBuilder;
import net.thevpc.nuts.util.NIteratorUtils;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.util.NIterator;

import java.util.ArrayList;
import java.util.List;

public class NRepositoryList extends NCachedRepository {
    protected NRepository[] repoItems;

    public NRepositoryList(NAddRepositoryOptions options, NRepository[] repoItems, NWorkspace workspace, NRepository parentRepository,
                           NSpeedQualifier speed, boolean supportedMirroring, String repositoryType, boolean supportsDeploy) {
        super(options, workspace, parentRepository, speed, supportedMirroring, repositoryType, supportsDeploy);
        this.repoItems = repoItems;
    }

    @Override
    public boolean isRemote() {
        for (NRepository repoItem : repoItems) {
            if (repoItem.isRemote()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAcceptFetchMode(NFetchMode mode) {
        for (NRepository repoItem : repoItems) {
            if (((NRepositorySPI) repoItem).isAcceptFetchMode(mode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isAvailableImpl() {
        for (NRepository repoItem : repoItems) {
            if (repoItem.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NIterator<NId> searchVersionsCore(final NId id, NIdFilter idFilter, NFetchMode fetchMode) {
        List<NIterator<? extends NId>> all = new ArrayList<>();
        for (NRepository repoItem : repoItems) {
            all.add(
                    ((NRepositorySPI) repoItem).searchVersions()
                            .setId(id)
                            .setFilter(idFilter)
                            .setFetchMode(fetchMode)
                            .getResult()
            );
        }
        NSession session = workspace.currentSession();
        return NIteratorBuilder.ofCoalesce(all).build();
    }


    @Override
    public NPath fetchContentCore(NId id, NDescriptor descriptor, NFetchMode fetchMode) {
        for (NRepository repoItem : repoItems) {
            try {
                NPath result = ((NRepositorySPI) repoItem).fetchContent()
                        .setId(id)
                        .setDescriptor(descriptor)
                        .setFetchMode(fetchMode)
                        .getResult();
                if (result != null) {
                    return result;
                }
            } catch (NNotFoundException e) {
                //ignore
            }
        }
        return null;
    }

    @Override
    public NIterator<NId> searchCore(final NIdFilter filter, NPath[] basePaths, NId[] baseIds, NFetchMode fetchMode) {
        List<NIterator<? extends NId>> list = new ArrayList<>();
        for (NRepository repoItem : repoItems) {
            list.add(
                    ((NRepositorySPI) repoItem).search()
                            .setFilter(filter)
                            .setFetchMode(fetchMode)
                            .getResult()
            );
        }
        return NIteratorUtils.concat(list);
    }

    @Override
    public void updateStatisticsImpl() {
        for (NRepository repoItem : repoItems) {
            ((NRepositorySPI) repoItem).updateStatistics().run();
        }
    }

    public NDescriptor fetchDescriptorCore(NId id, NFetchMode fetchMode) {
        for (NRepository repoItem : repoItems) {
            try {
                NDescriptor result = ((NRepositorySPI) repoItem).fetchDescriptor()
                        .setId(id)
                        .setFetchMode(fetchMode)
                        .getResult();
                if (result != null) {
                    return result;
                }
            } catch (NNotFoundException e) {
                //ignore
            }
        }
        return null;
    }


    public NId searchLatestVersionCore(NId id, NIdFilter filter, NFetchMode fetchMode) {
        for (NRepository repoItem : repoItems) {
            try {
                NId nId = ((NRepositoryExt) repoItem).searchLatestVersion(id, filter, fetchMode);
                if (nId != null) {
                    return nId;
                }
            } catch (NNotFoundException e) {
                //ignore
            }
        }
        return null;
    }

}

package net.thevpc.nuts.runtime.standalone.repository.toolbox;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.toolbox.helpers.NetbeansRepoHelper;
import net.thevpc.nuts.runtime.standalone.repository.toolbox.helpers.TomcatRepoHelper;
import net.thevpc.nuts.util.*;

import java.util.*;

public class ToolboxRepositoryModel implements NRepositoryModel {
    private Map<String, ToolboxRepoHelper> mapByShortId = new HashMap<>();
    private List<ToolboxRepoHelper> map = new ArrayList<>();

    public ToolboxRepositoryModel() {
        register(new TomcatRepoHelper());
        register(new NetbeansRepoHelper());
    }

    private void register(ToolboxRepoHelper t) {
        map.add(t);
    }


    @Override
    public String getName() {
        return "toolbox";
    }

    @Override
    public String getUuid() {
        return UUID.nameUUIDFromBytes(getName().getBytes()).toString();
    }

    private ToolboxRepoHelper findByShortId(NId id) {
        NId shortId = id.getShortId();
        ToolboxRepoHelper old = mapByShortId.get(id.getShortName());
        if (old != null) {
            return old;
        }
        for (ToolboxRepoHelper h : map) {
            if (h.acceptId(shortId)) {
                mapByShortId.put(id.getShortName(), h);
                return h;
            }
        }
        return null;
    }

    @Override
    public NIterator<NId> searchVersions(NId id, NIdFilter filter, NFetchMode fetchMode, NRepository repository) {
        if (fetchMode != NFetchMode.REMOTE) {
            return null;
        }
        ToolboxRepoHelper old = findByShortId(id);
        if (old != null) {
            return old.searchVersions(id, filter, repository);
        }
        return null;
    }

    @Override
    public NDescriptor fetchDescriptor(NId id, NFetchMode fetchMode, NRepository repository) {
        if (fetchMode != NFetchMode.REMOTE) {
            return null;
        }
        ToolboxRepoHelper old = findByShortId(id);
        if (old != null) {
            return old.fetchDescriptor(id, repository);
        }
        return null;
    }

    @Override
    public NPath fetchContent(NId id, NDescriptor descriptor, NFetchMode fetchMode, NRepository repository) {
        if (fetchMode != NFetchMode.REMOTE) {
            return null;
        }
        ToolboxRepoHelper old = findByShortId(id);
        if (old != null) {
            return old.fetchContent(id, descriptor, repository);
        }
        return null;
    }

    @Override
    public NIterator<NId> search(NIdFilter filter, NPath[] basePaths, NFetchMode fetchMode, NRepository repository) {
        if (fetchMode != NFetchMode.REMOTE) {
            return NIterator.ofEmpty();
        }
        NIteratorBuilder<NId> b = NIteratorBuilder.emptyBuilder();
        for (ToolboxRepoHelper h : map) {
            b.concat(h.search(filter, basePaths, repository));
        }
        return b.build();
    }

    public static String getIdLocalFile(NId id, NRepository repository) {
        NWorkspace workspace = NWorkspace.of();
        return repository.config().getStoreLocation()
                .resolve(workspace.getDefaultIdBasedir(id))
                .resolve(workspace.getDefaultIdFilename(id))
                .toString();
    }

}

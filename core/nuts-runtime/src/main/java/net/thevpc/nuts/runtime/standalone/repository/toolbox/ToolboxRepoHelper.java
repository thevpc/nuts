package net.thevpc.nuts.runtime.standalone.repository.toolbox;

import net.thevpc.nuts.NDescriptor;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.NIdFilter;
import net.thevpc.nuts.NRepository;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NIterator;

public interface ToolboxRepoHelper {
    NIterator<NId> searchVersions(NId id, NIdFilter filter, NRepository repository);

    boolean acceptId(NId id);

    NDescriptor fetchDescriptor(NId id, NRepository repository);

    NIterator<NId> search(NIdFilter filter, NPath[] basePaths, NRepository repository);

    NPath fetchContent(NId id, NDescriptor descriptor, NRepository repository);
}

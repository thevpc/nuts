package net.thevpc.nuts.runtime.standalone.repository.toolbox;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NIterator;

public interface ToolboxRepoHelper {
    NIterator<NId> searchVersions(NId id, NDefinitionFilter filter, NRepository repository);

    boolean acceptId(NId id);

    NDescriptor fetchDescriptor(NId id, NRepository repository);

    NIterator<NId> search(NDefinitionFilter filter, NPath[] basePaths, NRepository repository);

    NPath fetchContent(NId id, NDescriptor descriptor, NRepository repository);
}

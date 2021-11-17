package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;

import java.io.IOException;
import java.io.InputStream;

public interface NutsIdPathIteratorModel {

    void undeploy(NutsId id, NutsSession session) throws NutsExecutionException;

    boolean isDescFile(NutsPath pathname);

    NutsDescriptor parseDescriptor(NutsPath pathname, InputStream in, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session, NutsPath rootURL) throws IOException;

    NutsId parseId(NutsPath pathname, NutsPath rootPath, NutsIdFilter filter, NutsRepository repository, NutsSession session) throws IOException;
}

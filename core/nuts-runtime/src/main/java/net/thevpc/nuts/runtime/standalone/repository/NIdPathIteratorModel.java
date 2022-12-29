package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;

import java.io.IOException;
import java.io.InputStream;

public interface NIdPathIteratorModel {

    void undeploy(NId id, NSession session) throws NExecutionException;

    boolean isDescFile(NPath pathname);

    NDescriptor parseDescriptor(NPath pathname, InputStream in, NFetchMode fetchMode, NRepository repository, NSession session, NPath rootURL) throws IOException;

    NId parseId(NPath pathname, NPath rootPath, NIdFilter filter, NRepository repository, NSession session) throws IOException;
}

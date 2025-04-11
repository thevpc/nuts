package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;

import java.io.InputStream;

public interface NIdPathIteratorModel {

    void undeploy(NId id) throws NExecutionException;

    boolean isDescFile(NPath pathname);

    NDescriptor parseDescriptor(NPath pathname, InputStream in, NFetchMode fetchMode, NRepository repository, NPath rootURL) ;

    NId parseId(NPath pathname, NPath rootPath, NDefinitionFilter filter, NRepository repository) ;
}

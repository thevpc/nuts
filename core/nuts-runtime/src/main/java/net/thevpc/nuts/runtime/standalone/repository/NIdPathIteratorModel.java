package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.command.NFetchMode;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.core.NRepository;

import java.io.InputStream;

public interface NIdPathIteratorModel {

    void undeploy(NId id) throws NExecutionException;

    boolean isDescFile(NPath pathname);

    NDescriptor parseDescriptor(NPath pathname, InputStream in, NFetchMode fetchMode, NRepository repository, NPath rootURL) ;

    NId parseId(NPath pathname, NPath rootPath, NDefinitionFilter filter, NRepository repository) ;
}

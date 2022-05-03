package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsInputStreamMonitor;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsSearchIdByDescriptor;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsSearchIdById;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.util.NutsLoggerOp;

import java.io.IOException;
import java.util.logging.Level;

public abstract class NutsIdPathIteratorBase implements NutsIdPathIteratorModel {

    public NutsId validate(NutsId id, NutsDescriptor t, NutsPath pathname, NutsPath rootPath, NutsIdFilter filter, NutsRepository repository, NutsSession session) throws IOException {
        if (t != null) {
            if (!CoreNutsUtils.isEffectiveId(t.getId())) {
                NutsDescriptor nutsDescriptor = null;
                try {
                    nutsDescriptor = NutsWorkspaceExt.of(session.getWorkspace()).resolveEffectiveDescriptor(t, session);
                } catch (Exception ex) {
                    NutsLoggerOp.of(NutsIdPathIteratorBase.class,session).level(Level.FINE).error(ex).log(
                            NutsMessage.ofJstyle("error resolving effective descriptor for {0} in url {1} : {2}", t.getId(),
                                    pathname,
                                    ex));//e.printStackTrace();
                }
                t = nutsDescriptor;
            }
            if ((filter == null || filter.acceptSearchId(new NutsSearchIdByDescriptor(t), session))) {
                NutsId nutsId = t.getId().builder().setRepository(repository.getName()).build();
//                        nutsId = nutsId.setAlternative(t.getAlternative());
                return nutsId;
            }
        }
        if (id != null) {
            if ((filter == null || filter.acceptSearchId(new NutsSearchIdById(id), session))) {
                return id;
            }
        }
        return null;
    }

    @Override
    public NutsId parseId(NutsPath pathname, NutsPath rootPath, NutsIdFilter filter, NutsRepository repository, NutsSession session) throws IOException {
        NutsDescriptor t = null;
        try {
            t = parseDescriptor(pathname, NutsInputStreamMonitor.of(session).setSource(pathname).create(),
                    NutsFetchMode.LOCAL, repository, session, rootPath);
        } catch (Exception ex) {
            NutsLoggerOp.of(NutsIdPathIteratorBase.class,session).level(Level.FINE).error(ex)
                    .log(NutsMessage.ofJstyle("error parsing url : {0} : {1}", pathname, toString()));//e.printStackTrace();
        }
        if (t != null) {
            return validate(null, t, pathname, rootPath, filter, repository, session);
        }
        return null;
    }

}

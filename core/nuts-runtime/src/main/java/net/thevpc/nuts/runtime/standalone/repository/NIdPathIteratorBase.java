package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NInputStreamMonitor;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.id.filter.NSearchIdByDescriptor;
import net.thevpc.nuts.runtime.standalone.id.filter.NSearchIdById;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.util.NMsg;

import java.io.IOException;
import java.util.logging.Level;

public abstract class NIdPathIteratorBase implements NIdPathIteratorModel {

    public NId validate(NId id, NDescriptor t, NPath pathname, NPath rootPath, NIdFilter filter, NRepository repository, NSession session) throws IOException {
        if (t != null) {
            if (!CoreNUtils.isEffectiveId(t.getId())) {
                NDescriptor nutsDescriptor = null;
                try {
                    nutsDescriptor = NWorkspaceExt.of(session.getWorkspace()).resolveEffectiveDescriptor(t, session);
                } catch (Exception ex) {
                    NLogOp.of(NIdPathIteratorBase.class,session).level(Level.FINE).error(ex).log(
                            NMsg.ofJ("error resolving effective descriptor for {0} in url {1} : {2}", t.getId(),
                                    pathname,
                                    ex));//e.printStackTrace();
                }
                t = nutsDescriptor;
            }
            if ((filter == null || filter.acceptSearchId(new NSearchIdByDescriptor(t), session))) {
                NId nutsId = t.getId().builder().setRepository(repository.getName()).build();
//                        nutsId = nutsId.setAlternative(t.getAlternative());
                return nutsId;
            }
        }
        if (id != null) {
            if ((filter == null || filter.acceptSearchId(new NSearchIdById(id), session))) {
                return id;
            }
        }
        return null;
    }

    @Override
    public NId parseId(NPath pathname, NPath rootPath, NIdFilter filter, NRepository repository, NSession session) throws IOException {
        NDescriptor t = null;
        try {
            t = parseDescriptor(pathname, NInputStreamMonitor.of(session).setSource(pathname).create(),
                    NFetchMode.LOCAL, repository, session, rootPath);
        } catch (Exception ex) {
            NLogOp.of(NIdPathIteratorBase.class,session).level(Level.FINE).error(ex)
                    .log(NMsg.ofJ("error parsing url : {0} : {1}", pathname, toString()));//e.printStackTrace();
        }
        if (t != null) {
            return validate(null, t, pathname, rootPath, filter, repository, session);
        }
        return null;
    }

}

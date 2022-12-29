/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.id.filter;

import java.util.Objects;
import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenRepositoryFolderHelper;
import net.thevpc.nuts.util.NLogger;
import net.thevpc.nuts.util.NLoggerVerb;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.util.Simplifiable;

/**
 *
 * @author thevpc
 */
public class NDescriptorIdFilter extends AbstractIdFilter implements NIdFilter, Simplifiable<NIdFilter> {

    private NLogger LOG;
    private final NDescriptorFilter filter;

    public NDescriptorIdFilter(NDescriptorFilter filter, NSession session) {
        super(session, NFilterOp.CONVERT);
        this.filter = filter;
    }

    @Override
    public boolean acceptSearchId(NSearchId sid, NSession session) {
        return filter == null ? true : filter.acceptSearchId(sid, session);
    }

    @Override
    public boolean acceptId(NId id, NSession session) {
        if (filter == null) {
            return true;
        }
        if(LOG==null){
            LOG= NLogger.of(MavenRepositoryFolderHelper.class,session);
        }
        NDescriptor descriptor = null;
        try {
//                descriptor = repository.fetchDescriptor().setId(id).setSession(session).getResult();
            descriptor = session.fetch().setId(id).setSession(session).getResultDescriptor();
            if (!CoreNUtils.isEffectiveId(descriptor.getId())) {
                NDescriptor nutsDescriptor = null;
                try {
                    //NutsWorkspace ws = repository.getWorkspace();
                    nutsDescriptor = NWorkspaceExt.of(session.getWorkspace()).resolveEffectiveDescriptor(descriptor, session);
                } catch (Exception ex) {
                    LOG.with().session(session).level(Level.FINE).error(ex)
                            .log( NMsg.ofJstyle("failed to resolve effective desc {0} for {1}", descriptor.getId(),id));
                    //throw new NutsException(e);
                }
                descriptor = nutsDescriptor;
            }
        } catch (Exception ex) {
            //suppose we cannot retrieve descriptor
            if (LOG.isLoggable(Level.FINER)) {
                LOG.with().session(session).level(Level.FINER).verb(NLoggerVerb.FAIL)
                        .log(
                                NMsg.ofJstyle("unable to fetch descriptor for {0} : {1}",
                        id,ex)
                        );
            }
            return false;
        }
        if (!filter.acceptDescriptor(descriptor, session)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.filter);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NDescriptorIdFilter other = (NDescriptorIdFilter) obj;
        if (!Objects.equals(this.filter, other.filter)) {
            return false;
        }
        return true;
    }

    @Override
    public NIdFilter simplify() {
        NDescriptorFilter f2 = CoreFilterUtils.simplify(filter);
        if (f2 == null) {
            return null;
        }
        if (f2 == filter) {
            return this;
        }
        return new NDescriptorIdFilter(f2,getSession());
    }

    @Override
    public String toString() {
        return String.valueOf(filter);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.definition.NDefinitionDelegate;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenRepositoryFolderHelper;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.spi.base.AbstractIdFilter;
import net.thevpc.nuts.util.*;

import java.util.Objects;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class NDefinitionIdFilter extends AbstractIdFilter implements NIdFilter, NSimplifiable<NIdFilter> {

    private final NDefinitionFilter filter;

    public NDefinitionIdFilter(NDefinitionFilter filter) {
        super(NFilterOp.CONVERT);
        this.filter = filter;
    }

    @Override
    public boolean acceptSearchId(NSearchId sid) {
        return acceptId(sid.getId());
    }

    @Override
    public boolean acceptId(NId id) {
        if (filter == null) {
            return true;
        }
        NDefinition d = new MyNDefinition(id);
        if (!filter.acceptDefinition(d)) {
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
        final NDefinitionIdFilter other = (NDefinitionIdFilter) obj;
        if (!Objects.equals(this.filter, other.filter)) {
            return false;
        }
        return true;
    }

    @Override
    public NIdFilter simplify() {
        NDefinitionFilter f2 = CoreFilterUtils.simplify(filter);
        if (f2 == null) {
            return null;
        }
        if (f2 == filter) {
            return this;
        }
        return new NDefinitionIdFilter(f2);
    }

    @Override
    public String toString() {
        return String.valueOf(filter);
    }

    private static class MyNDefinition extends NDefinitionDelegate {
        NLog LOG = NLog.of(MavenRepositoryFolderHelper.class);
        NDefinition definition;
        private final NId id;
        boolean loaded;
        RuntimeException replayException;

        public MyNDefinition(NId id) {
            this.id = id;
        }

        @Override
        public NId getId() {
            return id;
        }

        protected NDefinition getBase() {
            if (!loaded) {
                if (LOG == null) {
                    LOG = NLog.of(MavenRepositoryFolderHelper.class);
                }
                loaded = true;
                try {
//                descriptor = repository.fetchDescriptor().setId(id).setSession(session).getResult();
                    definition = NFetchCmd.of(id).getResultDefinition();

                } catch (Exception ex) {
                    //suppose we cannot retrieve descriptor
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.with().level(Level.FINER).verb(NLogVerb.FAIL)
                                .log(
                                        NMsg.ofC("unable to fetch descriptor for %s : %s",
                                                id, ex)
                                );
                    }
                    if (!(ex instanceof RuntimeException)) {
                        ex = new UncheckedException(ex);
                    }
                    replayException = (RuntimeException) ex;
                }
            }
            if (replayException != null) {
                throw replayException;
            }
            return definition;
        }
    }
}

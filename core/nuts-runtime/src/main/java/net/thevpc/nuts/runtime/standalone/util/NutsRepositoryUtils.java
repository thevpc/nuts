package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import net.thevpc.nuts.NutsLogVerb;

import java.util.Map;
import java.util.logging.Level;

public class NutsRepositoryUtils {
    private final NutsLogger LOG;

    private NutsRepository repo;

    public static NutsRepositoryUtils of(NutsRepository repo) {
        Map<String, Object> up = repo.getUserProperties();
        NutsRepositoryUtils wp = (NutsRepositoryUtils) up.get(NutsRepositoryUtils.class.getName());
        if (wp == null) {
            wp = new NutsRepositoryUtils(repo);
            up.put(NutsRepositoryUtils.class.getName(), wp);
        }
        return wp;
    }

    private NutsRepositoryUtils(NutsRepository repo) {
        this.repo = repo;
        LOG=repo.getWorkspace().log().of(NutsRepositoryUtils.class);
    }

    public Events events(){
        return new Events(this);
    }

    public static class Events{
        private NutsRepositoryUtils u;

        public Events(NutsRepositoryUtils u) {
            this.u = u;
        }

        public void fireOnUndeploy(NutsContentEvent evt) {
            for (NutsRepositoryListener listener : u.repo.getRepositoryListeners()) {
                listener.onUndeploy(evt);
            }
            for (NutsRepositoryListener listener : u.repo.getWorkspace().events().getRepositoryListeners()) {
                listener.onUndeploy(evt);
            }
        }

        public void fireOnDeploy(NutsContentEvent file) {
            for (NutsRepositoryListener listener : u.repo.getRepositoryListeners()) {
                listener.onDeploy(file);
            }
            for (NutsRepositoryListener listener : u.repo.getWorkspace().events().getRepositoryListeners()) {
                listener.onDeploy(file);
            }
        }

        public void fireOnPush(NutsContentEvent event) {
            for (NutsRepositoryListener listener : u.repo.getRepositoryListeners()) {
                listener.onPush(event);
            }
            for (NutsRepositoryListener listener : u.repo.getWorkspace().events().getRepositoryListeners()) {
                listener.onPush(event);
            }
            for (NutsRepositoryListener listener : event.getSession().getListeners(NutsRepositoryListener.class)) {
                listener.onPush(event);
            }
        }

        public void fireOnAddRepository(NutsRepositoryEvent event) {
            if (u.LOG.isLoggable(Level.FINEST)) {
                u.LOG.with().session(event.getSession()).level(Level.FINEST).verb(NutsLogVerb.UPDATE).log( "{0} add    repo {1}", CoreStringUtils.alignLeft(u.repo.getName(), 20), event
                        .getRepository().getName());
            }
            for (NutsRepositoryListener listener : u.repo.getRepositoryListeners()) {
                listener.onAddRepository(event);
            }
            for (NutsRepositoryListener listener : u.repo.getWorkspace().events().getRepositoryListeners()) {
                listener.onAddRepository(event);
            }
            for (NutsRepositoryListener listener : event.getSession().getListeners(NutsRepositoryListener.class)) {
                listener.onAddRepository(event);
            }
        }

        public void fireOnRemoveRepository(NutsRepositoryEvent event) {
            if (u.LOG.isLoggable(Level.FINEST)) {
                u.LOG.with().session(event.getSession()).level(Level.FINEST).verb(NutsLogVerb.UPDATE).log("{0} remove repo {1}", new Object[]{CoreStringUtils.alignLeft(u.repo.getName(), 20), event
                        .getRepository().getName()});
            }
            for (NutsRepositoryListener listener : u.repo.getRepositoryListeners()) {
//            if (event == null) {
//                event = new DefaultNutsRepositoryEvent(getWorkspace(), this, event, "mirror", event, null);
//            }
                listener.onRemoveRepository(event);
            }
            for (NutsRepositoryListener listener : u.repo.getWorkspace().events().getRepositoryListeners()) {
//            if (event == null) {
//                event = new DefaultNutsRepositoryEvent(getWorkspace(), this, event, "mirror", event, null);
//            }
                listener.onRemoveRepository(event);
            }
            for (NutsRepositoryListener listener : event.getSession().getListeners(NutsRepositoryListener.class)) {
                listener.onRemoveRepository(event);
            }
        }

    }
}

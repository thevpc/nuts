package net.vpc.app.nuts.core.util;

import net.vpc.app.nuts.NutsContentEvent;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryEvent;
import net.vpc.app.nuts.NutsRepositoryListener;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class NutsRepositoryUtils {
    private static final Logger LOG = Logger.getLogger(NutsRepositoryUtils.class.getName());
    public static class Events{
        public static void fireOnUndeploy(NutsRepository repo,NutsContentEvent evt) {
            for (NutsRepositoryListener listener : repo.getRepositoryListeners()) {
                listener.onUndeploy(evt);
            }
            for (NutsRepositoryListener listener : repo.getWorkspace().getRepositoryListeners()) {
                listener.onUndeploy(evt);
            }
        }

        public static void fireOnDeploy(NutsRepository repo,NutsContentEvent file) {
            for (NutsRepositoryListener listener : repo.getRepositoryListeners()) {
                listener.onDeploy(file);
            }
            for (NutsRepositoryListener listener : repo.getWorkspace().getRepositoryListeners()) {
                listener.onDeploy(file);
            }
        }

        public static void fireOnPush(NutsRepository repo,NutsContentEvent event) {
            for (NutsRepositoryListener listener : repo.getRepositoryListeners()) {
                listener.onPush(event);
            }
            for (NutsRepositoryListener listener : repo.getWorkspace().getRepositoryListeners()) {
                listener.onPush(event);
            }
            for (NutsRepositoryListener listener : event.getSession().getListeners(NutsRepositoryListener.class)) {
                listener.onPush(event);
            }
        }

        public static void fireOnAddRepository(NutsRepository repo, NutsRepositoryEvent event) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "{0} add    repo {1}", new Object[]{CoreStringUtils.alignLeft(repo.config().getName(), 20), event
                        .getRepository().config().name()});
            }
            for (NutsRepositoryListener listener : repo.getRepositoryListeners()) {
                listener.onAddRepository(event);
            }
            for (NutsRepositoryListener listener : repo.getWorkspace().getRepositoryListeners()) {
                listener.onAddRepository(event);
            }
            for (NutsRepositoryListener listener : event.getSession().getListeners(NutsRepositoryListener.class)) {
                listener.onAddRepository(event);
            }
        }

        public static void fireOnRemoveRepository(NutsRepository repo,NutsRepositoryEvent event) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "{0} remove repo {1}", new Object[]{CoreStringUtils.alignLeft(repo.config().getName(), 20), event
                        .getRepository().config().name()});
            }
            for (NutsRepositoryListener listener : repo.getRepositoryListeners()) {
//            if (event == null) {
//                event = new DefaultNutsRepositoryEvent(getWorkspace(), this, event, "mirror", event, null);
//            }
                listener.onRemoveRepository(event);
            }
            for (NutsRepositoryListener listener : repo.getWorkspace().getRepositoryListeners()) {
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

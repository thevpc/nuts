package net.thevpc.nuts.runtime.core.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.NutsLogVerb;

import java.util.Map;
import java.util.logging.Level;
import net.thevpc.nuts.runtime.core.commands.repo.NutsRepositorySupportedAction;

public class NutsRepositoryUtils {
    private NutsLogger LOG;

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
//        LOG=repo.getWorkspace().log().of(.class);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = this.repo.getWorkspace().log().setSession(session).of(NutsRepositoryUtils.class);
        }
        return LOG;
    }

    public static int getSupportSpeedLevel(NutsRepository repository, NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode mode, boolean transitive, NutsSession session) {
        if (repository instanceof NutsInstalledRepository) {
            return 0;
        }
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(repository);
        double result = 0;
        if (xrepo.acceptAction(id, supportedAction, mode, session)) {
            int r = repository.config().getSpeed();
            if (r > 0) {
                result += 1.0 / r;
            }
        }
        if (transitive) {
            for (NutsRepository remote : repository.config()
                    .setSession(session)
                    .getMirrors()) {
                int r = getSupportSpeedLevel(remote, supportedAction, id, mode, transitive, session);
                if (r > 0) {
                    result += 1.0 / r;
                }
            }
        }
        int intResult = 0;
        if (result != 0) {
            intResult = (int) (1.0 / result);
            if (intResult < 0) {
                intResult = Integer.MAX_VALUE;
            }
        }
        return intResult;
    }
    public static int getSupportDeployLevel(NutsRepository repository, NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode mode, boolean transitive, NutsSession session) {
        if (repository instanceof NutsInstalledRepository) {
            return 0;
        }
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(repository);
        int result = 0;
        if (xrepo.acceptAction(id, supportedAction, mode, session)) {
            int r = repository.config().getDeployOrder();
            if (r > 0) {
                result += r;
            }
        }
        if (transitive) {
            for (NutsRepository remote : repository.config()
                    .setSession(session)
                    .getMirrors()) {
                int r = getSupportSpeedLevel(remote, supportedAction, id, mode, transitive, session);
                if (r > 0) {
                    result += r;
                }
            }
        }
        return result;
    }



    public Events events() {
        return new Events(this);
    }

    public static class Events {

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
            if (u._LOG(event.getSession()).isLoggable(Level.FINEST)) {
                u._LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLogVerb.UPDATE)
                        .log(NutsMessage.jstyle("{0} add    repo {1}", CoreStringUtils.alignLeft(u.repo.getName(), 20), event
                        .getRepository().getName())
                        );
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
            if (u._LOG(event.getSession()).isLoggable(Level.FINEST)) {
                u._LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLogVerb.UPDATE).log(
                        NutsMessage.jstyle("{0} remove repo {1}", CoreStringUtils.alignLeft(u.repo.getName(), 20), event
                    .getRepository().getName()));
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

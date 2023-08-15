package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
import net.thevpc.nuts.runtime.standalone.util.NSpeedQualifiers;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

public class NRepositoryHelper {
    private NLog LOG;

    private final NRepository repo;

    private NRepositoryHelper(NRepository repo) {
        this.repo = repo;
//        LOG=repo.getWorkspace().log().of(.class);
    }

    public static NRepositoryHelper of(NRepository repo) {
        Map<String, Object> up = repo.getUserProperties();
        NRepositoryHelper wp = (NRepositoryHelper) up.get(NRepositoryHelper.class.getName());
        if (wp == null) {
            wp = new NRepositoryHelper(repo);
            up.put(NRepositoryHelper.class.getName(), wp);
        }
        return wp;
    }

    public static NSpeedQualifier getSupportSpeedLevel(NRepository repository, NRepositorySupportedAction supportedAction, NId id, NFetchMode mode, boolean transitive, NSession session) {
        if (repository instanceof NInstalledRepository) {
            return NSpeedQualifier.UNAVAILABLE;
        }
        NRepositoryExt xrepo = NRepositoryExt.of(repository);
        List<NSpeedQualifier> speeds = new ArrayList<>();
        if (xrepo.acceptAction(id, supportedAction, mode, session)) {
            NSpeedQualifier r = repository.config().getSpeed();
            if (r != NSpeedQualifier.UNAVAILABLE) {
                speeds.add(r);
            }
        }
        if (transitive) {
            for (NRepository remote : repository.config()
                    .setSession(session)
                    .getMirrors()) {
                NSpeedQualifier r = getSupportSpeedLevel(remote, supportedAction, id, mode, transitive, session);
                if (r != NSpeedQualifier.UNAVAILABLE) {
                    speeds.add(r);
                }
            }
        }
        if (speeds.size() == 0) {
            return NSpeedQualifier.UNAVAILABLE;
        }
        return NSpeedQualifiers.max(speeds.toArray(new NSpeedQualifier[0]));
    }

    public static int getSupportDeployLevel(NRepository repository, NRepositorySupportedAction supportedAction, NId id, NFetchMode mode, boolean transitive, NSession session) {
        if (repository instanceof NInstalledRepository) {
            return 0;
        }
        NRepositoryExt xrepo = NRepositoryExt.of(repository);
        int result = 0;
        if (xrepo.acceptAction(id, supportedAction, mode, session)) {
            int r = repository.config().getDeployWeight();
            if (r > 0 && r > result) {
                result = r;
            }
        }
        if (transitive) {
            for (NRepository remote : repository.config()
                    .setSession(session)
                    .getMirrors()) {
                int r = getSupportDeployLevel(remote, supportedAction, id, mode, transitive, session);
                if (r > 0 && r > result) {
                    result = r;
                }
            }
        }
        return result;
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(NRepositoryHelper.class,session);
        }
        return LOG;
    }

    public Events events() {
        return new Events(this);
    }

    public static class Events {

        private final NRepositoryHelper u;

        public Events(NRepositoryHelper u) {
            this.u = u;
        }

        public void fireOnUndeploy(NContentEvent evt) {
            for (NRepositoryListener listener : u.repo.getRepositoryListeners()) {
                listener.onUndeploy(evt);
            }
            for (NRepositoryListener listener : NEvents.of(evt.getSession()).getRepositoryListeners()) {
                listener.onUndeploy(evt);
            }
        }

        public void fireOnDeploy(NContentEvent event) {
            for (NRepositoryListener listener : u.repo.getRepositoryListeners()) {
                listener.onDeploy(event);
            }
            for (NRepositoryListener listener : NEvents.of(event.getSession()).getRepositoryListeners()) {
                listener.onDeploy(event);
            }
        }

        public void fireOnPush(NContentEvent event) {
            for (NRepositoryListener listener : u.repo.getRepositoryListeners()) {
                listener.onPush(event);
            }
            for (NRepositoryListener listener : NEvents.of(event.getSession()).getRepositoryListeners()) {
                listener.onPush(event);
            }
            for (NRepositoryListener listener : event.getSession().getListeners(NRepositoryListener.class)) {
                listener.onPush(event);
            }
        }

        public void fireOnAddRepository(NRepositoryEvent event) {
            if (u._LOG(event.getSession()).isLoggable(Level.FINEST)) {
                u._LOGOP(event.getSession()).level(Level.FINEST).verb(NLogVerb.ADD)
                        .log(NMsg.ofJ("{0} add    repo {1}", NStringUtils.formatAlign(u.repo.getName(), 20, NPositionType.FIRST), event
                                .getRepository().getName())
                        );
            }
            for (NRepositoryListener listener : u.repo.getRepositoryListeners()) {
                listener.onAddRepository(event);
            }
            for (NRepositoryListener listener : NEvents.of(event.getSession()).getRepositoryListeners()) {
                listener.onAddRepository(event);
            }
            for (NRepositoryListener listener : event.getSession().getListeners(NRepositoryListener.class)) {
                listener.onAddRepository(event);
            }
        }

        public void fireOnRemoveRepository(NRepositoryEvent event) {
            if (u._LOG(event.getSession()).isLoggable(Level.FINEST)) {
                u._LOGOP(event.getSession()).level(Level.FINEST).verb(NLogVerb.REMOVE).log(
                        NMsg.ofJ("{0} remove repo {1}", NStringUtils.formatAlign(u.repo.getName(), 20, NPositionType.FIRST), event
                                .getRepository().getName()));
            }
            for (NRepositoryListener listener : u.repo.getRepositoryListeners()) {
//            if (event == null) {
//                event = new DefaultNRepositoryEvent(getWorkspace(), this, event, "mirror", event, null);
//            }
                listener.onRemoveRepository(event);
            }
            for (NRepositoryListener listener : NEvents.of(event.getSession()).getRepositoryListeners()) {
//            if (event == null) {
//                event = new DefaultNRepositoryEvent(getWorkspace(), this, event, "mirror", event, null);
//            }
                listener.onRemoveRepository(event);
            }
            for (NRepositoryListener listener : event.getSession().getListeners(NRepositoryListener.class)) {
                listener.onRemoveRepository(event);
            }
        }

    }
}

package net.thevpc.nuts.runtime.standalone.repository.impl.maven.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NutsLogger;

import java.util.logging.Level;

public class MvnClient {
    private final NutsLogger LOG;
    public static final String NET_VPC_APP_NUTS_MVN = "net.thevpc.nuts.toolbox:mvn";
    private NutsSession session;
    private Status status = Status.INIT;

    public enum Status {
        INIT,
        DIRTY,
        SUCCESS,
        FAIL,
    }

    public MvnClient(NutsSession session) {
        this.session = session;
        LOG= NutsLogger.of(MvnClient.class,session);
    }

    public boolean get(NutsId id, String repoURL, NutsSession session) {
        if (id.getShortName().equals(NET_VPC_APP_NUTS_MVN)) {
            return false;
        }
        switch (status) {
            case INIT: {
                status = Status.DIRTY;
                try {
                    NutsDefinition ff = session.search()
                            .addId(NET_VPC_APP_NUTS_MVN)
                            .setSession(session.copy().setFetchStrategy(NutsFetchStrategy.ONLINE))
                            .setOptional(false)
                            .setInlineDependencies(true).setLatest(true).getResultDefinitions().required();
                    for (NutsId nutsId : this.session.search().addId(ff.getId()).setInlineDependencies(true).getResultIds()) {
                        this.session.fetch().setId(nutsId).setSession(session.copy().setFetchStrategy(NutsFetchStrategy.ONLINE))
                                .setOptional(false)
                                .setDependencies(true).getResultDefinition();
                    }
                    status = Status.SUCCESS;
                } catch (Exception ex) {
                    LOG.with().session(session).level(Level.SEVERE).error(ex)
                            .log(NutsMessage.ofJstyle("failed to load {0} : {1}", NET_VPC_APP_NUTS_MVN, ex));
                    ex.printStackTrace();
                    status = Status.FAIL;
                    return false;
                }
                break;
            }
            case FAIL: {
                return false;
            }
            case SUCCESS: {
                //OK
                break;
            }
            case DIRTY: {
                return false;
            }
        }
        try {
            NutsExecCommand b = this.session
                    .exec()
                    .setFailFast(true)
                    .addCommand(
                            NET_VPC_APP_NUTS_MVN,
                            "--json",
                            "get",
                            id.toString(),
                            repoURL == null ? "" : repoURL
                    ).setSession(session).run();
            return (b.getResult() == 0);
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.SEVERE).error(ex)
                    .log(NutsMessage.ofJstyle("failed to invoke {0} : {1}", NET_VPC_APP_NUTS_MVN, ex));
            return false;
        }
    }
}

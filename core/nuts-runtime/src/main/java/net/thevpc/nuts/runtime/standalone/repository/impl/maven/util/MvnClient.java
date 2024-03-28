package net.thevpc.nuts.runtime.standalone.repository.impl.maven.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NMsg;

import java.util.logging.Level;

public class MvnClient {
    private final NLog LOG;
    public static final String NET_VPC_APP_NUTS_MVN = "net.thevpc.nuts.toolbox:mvn";
    private NSession session;
    private Status status = Status.INIT;

    public enum Status {
        INIT,
        DIRTY,
        SUCCESS,
        FAIL,
    }

    public MvnClient(NSession session) {
        this.session = session;
        LOG= NLog.of(MvnClient.class,session);
    }

    public boolean get(NId id, String repoURL, NSession session) {
        if (id.getShortName().equals(NET_VPC_APP_NUTS_MVN)) {
            return false;
        }
        switch (status) {
            case INIT: {
                status = Status.DIRTY;
                try {
                    NDefinition ff = NSearchCmd.of(session.copy().setFetchStrategy(NFetchStrategy.ONLINE))
                            .addId(NET_VPC_APP_NUTS_MVN)
                            .setOptional(false)
                            .setInlineDependencies(true).setLatest(true).getResultDefinitions().findFirst().get();
                    for (NId nutsId : NSearchCmd.of(this.session).addId(ff.getId()).setInlineDependencies(true).getResultIds()) {
                        NFetchCmd.of(nutsId,session.copy().setFetchStrategy(NFetchStrategy.ONLINE))
                                .setOptional(false)
                                .setDependencies(true).getResultDefinition();
                    }
                    status = Status.SUCCESS;
                } catch (Exception ex) {
                    LOG.with().session(session).level(Level.SEVERE).error(ex)
                            .log(NMsg.ofJ("failed to load {0} : {1}", NET_VPC_APP_NUTS_MVN, ex));
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
            NExecCmd b = NExecCmd.of(session)
                    .failFast()
                    .addCommand(
                            NET_VPC_APP_NUTS_MVN,
                            "--json",
                            "get",
                            id.toString(),
                            repoURL == null ? "" : repoURL
                    ).run();
            return (b.getResultCode() == 0);
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.SEVERE).error(ex)
                    .log(NMsg.ofJ("failed to invoke {0} : {1}", NET_VPC_APP_NUTS_MVN, ex));
            return false;
        }
    }
}

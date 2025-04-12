package net.thevpc.nuts.runtime.standalone.repository.impl.maven.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NMsg;

import java.util.logging.Level;

public class MvnClient {
    public static final String NET_VPC_APP_NUTS_MVN = "net.thevpc.nmvn:nmvn";
    private Status status = Status.INIT;

    public enum Status {
        INIT,
        DIRTY,
        SUCCESS,
        FAIL,
    }

    public MvnClient() {
    }

    protected NLog LOG() {
        return NLog.of(MvnClient.class);
    }

    public boolean get(NId id, String repoURL) {
        if (id.getShortName().equals(NET_VPC_APP_NUTS_MVN)) {
            return false;
        }
        switch (status) {
            case INIT: {
                status = Status.DIRTY;
                try {
                    NDefinition ff = NSearchCmd.of()
                            .setFetchStrategy(NFetchStrategy.ONLINE)
                            .addId(NET_VPC_APP_NUTS_MVN)
                            .setDependencyFilter(NDependencyFilters.of().byRunnable(false))
                            .setInlineDependencies(true).setLatest(true).getResultDefinitions().findFirst().get();
                    for (NId nutsId : NSearchCmd.of().addId(ff.getId()).setInlineDependencies(true).getResultIds()) {
                        NFetchCmd.of(nutsId).setFetchStrategy(NFetchStrategy.ONLINE)
                                .setDependencyFilter(NDependencyFilters.of().byRunnable(false))
                                .setDependencies(true).getResultDefinition();
                    }
                    status = Status.SUCCESS;
                } catch (Exception ex) {
                    LOG().with().level(Level.SEVERE).error(ex)
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
            NExecCmd b = NExecCmd.of()
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
            LOG().with().level(Level.SEVERE).error(ex)
                    .log(NMsg.ofJ("failed to invoke {0} : {1}", NET_VPC_APP_NUTS_MVN, ex));
            return false;
        }
    }
}

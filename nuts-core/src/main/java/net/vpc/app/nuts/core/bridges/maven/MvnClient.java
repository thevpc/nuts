package net.vpc.app.nuts.core.bridges.maven;

import net.vpc.app.nuts.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MvnClient {
    private final NutsLogger LOG;
    public static final String NET_VPC_APP_NUTS_MVN = "net.vpc.app.nuts.toolbox:mvn";
    private NutsWorkspace ws;
    private Status status = Status.INIT;

    public enum Status {
        INIT,
        DIRTY,
        SUCCESS,
        FAIL,
    }

    public MvnClient(NutsWorkspace ws) {
        this.ws = ws;
        LOG=ws.log().of(MvnClient.class);
    }

    public boolean get(NutsId id, String repoURL, NutsSession session) {
        if (id.getShortName().equals(NET_VPC_APP_NUTS_MVN)) {
            return false;
        }
        NutsSession searchSession = session.copy().trace(false);
        switch (status) {
            case INIT: {
                status = Status.DIRTY;
                try {
                    NutsDefinition ff = ws.search()
                            .id(NET_VPC_APP_NUTS_MVN).session(searchSession)
                            .online()
                            .optional(false)
                            .inlineDependencies().latest().getResultDefinitions().required();
                    for (NutsId nutsId : ws.search().id(ff.getId()).inlineDependencies().getResultIds()) {
                        ws.fetch().id(nutsId).session(searchSession)
                                .online()
                                .optional(false)
                                .dependencies().getResultDefinition();
                    }
                    status = Status.SUCCESS;
                } catch (Exception ex) {
                    LOG.log(Level.FINE, "Failed to load " + NET_VPC_APP_NUTS_MVN,ex);
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
            NutsExecCommand b = ws
                    .exec()
                    .failFast()
                    .addCommand(
                            NET_VPC_APP_NUTS_MVN,
                            "--json",
                            "get",
                            id.toString(),
                            repoURL == null ? "" : repoURL
                    ).session(session).run();
            return (b.getResult() == 0);
        } catch (Exception ex) {
            LOG.log(Level.FINE, "Failed to invoke " + NET_VPC_APP_NUTS_MVN,ex);
            return false;
        }
    }
}

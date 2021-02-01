package net.thevpc.nuts.runtime.standalone.bridges.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.util.logging.Level;

public class MvnClient {
    private final NutsLogger LOG;
    public static final String NET_VPC_APP_NUTS_MVN = "net.thevpc.nuts.toolbox:mvn";
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
        NutsSession searchSession = CoreNutsUtils.silent(session);
        switch (status) {
            case INIT: {
                status = Status.DIRTY;
                try {
                    NutsDefinition ff = ws.search()
                            .addId(NET_VPC_APP_NUTS_MVN).setSession(searchSession)
                            .setOnline()
                            .setOptional(false)
                            .setInlineDependencies(true).setLatest(true).getResultDefinitions().required();
                    for (NutsId nutsId : ws.search().addId(ff.getId()).setInlineDependencies(true).getResultIds()) {
                        ws.fetch().setId(nutsId).setSession(searchSession)
                                .setOnline()
                                .setOptional(false)
                                .setDependencies(true).getResultDefinition();
                    }
                    status = Status.SUCCESS;
                } catch (Exception ex) {
                    LOG.with().session(session).level(Level.SEVERE).error(ex)
                            .log("failed to load {0} : {1}", NET_VPC_APP_NUTS_MVN, ex);
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
                    .log("failed to invoke {0} : {1}", NET_VPC_APP_NUTS_MVN, ex);
            return false;
        }
    }
}

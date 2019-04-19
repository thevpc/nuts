package net.vpc.app.nuts.core.bridges.maven;

import net.vpc.app.nuts.*;

public class MvnClient {
    public static final String NET_VPC_APP_NUTS_MVN = "net.vpc.app.nuts.toolbox:mvn";
    private NutsWorkspace ws;
    private Status status=Status.INIT;
    public enum Status{
        INIT,
        DIRTY,
        SUCCESS,
        FAIL,
    }


    public MvnClient(NutsWorkspace ws) {
        this.ws = ws;
    }

    public boolean get(NutsId id, String repoURL, NutsSession session){
        if(id.getSimpleName().equals(NET_VPC_APP_NUTS_MVN)){
            return false;
        }
        switch (status){
            case INIT:{
                status=Status.DIRTY;
                try {
                    NutsDefinition ff = ws.find().id(NET_VPC_APP_NUTS_MVN).setSession(session.copy())
                            .wired()
                            .setAcceptOptional(false)
                            .includeDependencies().latestVersions().getResultDefinitions().required();
                    for (NutsId nutsId : ws.find().id(ff.getId()).mainAndDependencies().getResultIds()) {
                        ws.fetch().id(nutsId).setSession(session)
                                .wired()
                                .setAcceptOptional(false)
                                .includeDependencies().getResultDefinition();
                    }
                    status=Status.SUCCESS;
                }catch (Exception ex){
                    ex.printStackTrace();
                    status=Status.FAIL;
                    return false;
                }
                break;
            }
            case FAIL:{
                return false;
            }
            case SUCCESS:{
                //OK
                break;
            }
            case DIRTY:{
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
                    ).setSession(session).run();
            return (b.getResult() == 0);
        }catch (Exception ex){
            return false;
        }
    }
}

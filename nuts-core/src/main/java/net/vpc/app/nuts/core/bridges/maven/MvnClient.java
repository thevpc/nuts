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
                    ws.fetch(NET_VPC_APP_NUTS_MVN).setSession(session.copy())
                            .wired()
                            .setAcceptOptional(false)
                            .includeDependencies().fetchDefinition();
                    for (NutsId nutsId : ws.createQuery().mainAndDependencies().find()) {
                        ws.fetch(nutsId).setSession(session)
                                .wired()
                                .setAcceptOptional(false)
                                .includeDependencies().fetchDefinition();
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
            NutsCommandExecBuilder b = ws
                    .createExecBuilder()
                    .setFailFast()
                    .addCommand(
                            NET_VPC_APP_NUTS_MVN,
                            "--json",
                            "get",
                            id.toString(),
                            repoURL == null ? "" : repoURL
                    ).setSession(session).exec();
            return (b.getResult() == 0);
        }catch (Exception ex){
            return false;
        }
    }
}

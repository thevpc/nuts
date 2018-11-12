package net.vpc.app.nuts.bridges.maven;

import net.vpc.app.nuts.*;

public class MvnClient {
    public static final String NET_VPC_APP_NUTS_MVN = "net.vpc.app.nuts.toolbox:mvn#1.0.0";
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
        switch (status){
            case INIT:{
                status=Status.DIRTY;
                try {
                    ws.fetchWithDependencies(NET_VPC_APP_NUTS_MVN, session.copy().setFetchMode(NutsFetchMode.ONLINE));
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

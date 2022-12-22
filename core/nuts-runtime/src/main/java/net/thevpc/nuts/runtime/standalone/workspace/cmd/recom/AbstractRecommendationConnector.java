package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.clinfo.NutsCliInfo;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public abstract class AbstractRecommendationConnector implements RecommendationConnector {

    private String localUserUUID;

    public AbstractRecommendationConnector() {
    }

    private String getLocalUserUUID(NutsSession session) {
        if (localUserUUID != null) {
            return localUserUUID;
        }
        localUserUUID = NutsCliInfo.loadCliId(session, true);
        return localUserUUID;
    }

    @Override
    public Map getRecommendations(RequestQueryInfo ri, NutsRecommendationPhase phase, boolean failure, NutsSession session) {
        validateRequest(ri, session);
        NutsId id = NutsId.of(ri.q.getId()).ifBlankEmpty().get(session);
        String name = phase.name().toLowerCase() + (failure ? "-failure" : "") + "-recommendations.json";
        String url = "/repo/" + id.getGroupId().replace('.', '/')
                + '/' + id.getArtifactId()
                + '/' + id.getVersion()
                + '/' + name;
        return post(url, ri, Map.class, session);
    }


    public abstract <T> T post(String url, RequestQueryInfo ri, Class<T> resultType, NutsSession session);

    public void validateRequest(RequestQueryInfo ri, NutsSession session) {
        ri.server = (ri.server == null || ri.server.trim().isEmpty()) ? "https://thevpc.net/nuts" : ri.server;
        NutsWorkspaceEnvManager env = session.env();
        RequestAgent agent = ri.q.getAgent();
        NutsWorkspace ws = session.getWorkspace();
        if (agent.getApiVersion() == null) {
            agent.setApiVersion(ws.getApiVersion().toString());
        }
        if (agent.getRuntimeId() == null) {
            agent.setRuntimeId(ws.getRuntimeId().toString());
        }
        if (agent.getArch() == null) {
            agent.setArch(env.getArch().toString());
        }
        if (agent.getOs() == null) {
            agent.setOs(env.getOs().toString());
        }
        if (agent.getOsDist() == null) {
            agent.setOsDist(env.getOsDist().toString());
        }
        if (agent.getDesktop() == null) {
            agent.setDesktop(env.getDesktopEnvironment().toString());
        }
        if (agent.getPlatform() == null) {
            agent.setPlatform(env.getPlatform().toString());
        }
        if (agent.getShell() == null) {
            agent.setShell(env.getShellFamily().toString());
        }
        if (agent.getUserDigest() == null) {
            agent.setUserDigest(getLocalUserUUID(session));
        }
        if (agent.getUserLocale() == null) {
            String loc = session.getLocale();
            if (loc == null) {
                loc = Locale.getDefault().toString();
            }
            agent.setUserLocale(loc);
        }
        if (agent.getUserTimeZone() == null) {
            agent.setUserTimeZone(TimeZone.getDefault().getDisplayName());
        }
    }

}

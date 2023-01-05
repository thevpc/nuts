package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.clinfo.NCliInfo;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public abstract class AbstractRecommendationConnector implements RecommendationConnector {

    private String localUserUUID;

    public AbstractRecommendationConnector() {
    }

    private String getLocalUserUUID(NSession session) {
        if (localUserUUID != null) {
            return localUserUUID;
        }
        localUserUUID = NCliInfo.loadCliId(session, true);
        return localUserUUID;
    }

    @Override
    public Map getRecommendations(RequestQueryInfo ri, NRecommendationPhase phase, boolean failure, NSession session) {
        validateRequest(ri, session);
        NId id = NId.of(ri.q.getId()).ifBlankEmpty().get(session);
        String name = phase.name().toLowerCase() + (failure ? "-failure" : "") + "-recommendations.json";
        String url = "/repo/" + id.getGroupId().replace('.', '/') + '/' + id.getArtifactId() + '/' + id.getVersion() + '/' + name;
        return post(url, ri, Map.class, session);
    }


    public abstract <T> T post(String url, RequestQueryInfo ri, Class<T> resultType, NSession session);

    public void validateRequest(RequestQueryInfo ri, NSession session) {
        NEnvs envs = NEnvs.of(session);
        NLiteral endPointURL = envs.getProperty("nuts-endpoint-url").orNull();
        if (NBlankable.isBlank(ri.server)) {
            if (endPointURL == null || endPointURL.isBlank()) {
                String defaultURL = resolveDefaultEndpointURL(session);
                envs.setProperty("nuts-endpoint-url", defaultURL);
            } else {
                ri.server = endPointURL.asString().get();
            }
        }
        NEnvs env = envs;
        RequestAgent agent = ri.q.getAgent();
        NWorkspace ws = session.getWorkspace();
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

    private String resolveDefaultEndpointURL(NSession session) {
        String p = NStringUtils.trim(System.getProperty("nuts-endpoint-url"));
        if (!p.isEmpty()) {
            if (p.equals("dev") || p.equals("debug")) {
                p = "http://127.0.0.1:8080/public/nuts";
            }else if (p.matches("localhost") || p.matches("127[.]0[.]0[.][0-9]+")) {
                p = "http://" + p + ":8080/public/nuts";
            } else if (p.matches("localhost(:[0-9]+)?") || p.matches("127[.]0[.]0[.][0-9]+(:[0-9]+)?")) {
                p = "http://" + p + "/public/nuts";
            } else if (p.matches("[a-zAzZ]+(:[0-9]+)?") || p.matches("[0-9]+[.][0-9]+[.][0-9]+[.][0-9]+(:[0-9]+)?")) {
                p = "https://" + p + "/public/nuts";
            } else {
                //
            }
        } else {
            try {
                String s = NStringUtils.trimToNull(NPath.of("https://raw.githubusercontent.com/thevpc/nuts/master/.endpoint", session).readString());
                if (s != null && s.startsWith("https://")) {
                    return s;
                }
            } catch (Exception ex) {
                //error;
            }
            return "https://nuts-pm.net/public/nuts";
        }
        return p;
    }

}

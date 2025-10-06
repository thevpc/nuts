package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.clinfo.NCliInfo;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.net.NWebCli;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public abstract class AbstractRecommendationConnector implements RecommendationConnector {

    private String localUserUUID;

    public AbstractRecommendationConnector() {
    }

    private String getLocalUserUUID() {
        if (localUserUUID != null) {
            return localUserUUID;
        }
        localUserUUID = NCliInfo.loadCliId(true);
        return localUserUUID;
    }

    @Override
    public Map getRecommendations(RequestQueryInfo ri, NRecommendationPhase phase, boolean failure) {
        validateRequest(ri);
        NId id = NId.get(ri.q.getId()).ifBlankEmpty().get();
        String name = phase.name().toLowerCase() + (failure ? "-failure" : "") + "-recommendations.json";
        String url = "/repo/" + id.getMavenFolder() + '/' + name;
        return post(url, ri, Map.class);
    }


    public abstract <T> T post(String url, RequestQueryInfo ri, Class<T> resultType);

    public void validateRequest(RequestQueryInfo ri) {
        NSession session = NSession.of();
        NWorkspace workspace = NWorkspace.of();
        String endPointURL = workspace.getProperty("nuts-endpoint-url").flatMap(x->x.asString()).orNull();
        if (NBlankable.isBlank(ri.server)) {
            if (NBlankable.isBlank(endPointURL)) {
                String defaultURL = resolveDefaultEndpointURL();
                if (!NBlankable.isBlank(defaultURL)) {
                    workspace.setProperty("nuts-endpoint-url", defaultURL);
                    ri.server = defaultURL;
                }
            } else {
                ri.server = endPointURL;
            }
        }
        RequestAgent agent = ri.q.getAgent();
        if (agent.getApiVersion() == null) {
            agent.setApiVersion(workspace.getApiVersion().toString());
        }
        if (agent.getRuntimeId() == null) {
            agent.setRuntimeId(workspace.getRuntimeId().toString());
        }
        if (agent.getArch() == null) {
            agent.setArch(workspace.getArch().toString());
        }
        if (agent.getOs() == null) {
            agent.setOs(workspace.getOs().toString());
        }
        if (agent.getOsDist() == null) {
            agent.setOsDist(workspace.getOsDist().toString());
        }
        if (agent.getDesktop() == null) {
            agent.setDesktop(workspace.getDesktopEnvironment().toString());
        }
        if (agent.getPlatform() == null) {
            agent.setPlatform(workspace.getPlatform().toString());
        }
        if (agent.getShell() == null) {
            agent.setShell(workspace.getShellFamily().toString());
        }
        if (agent.getUserDigest() == null) {
            agent.setUserDigest(getLocalUserUUID());
        }
        if (agent.getUserLocale() == null) {
            String loc = session.getLocale().orDefault();
            if (loc == null) {
                loc = Locale.getDefault().toString();
            }
            agent.setUserLocale(loc);
        }
        if (agent.getUserTimeZone() == null) {
            agent.setUserTimeZone(TimeZone.getDefault().getDisplayName());
        }
    }

    private String resolveDefaultEndpointURL() {
        String p = NStringUtils.trim(System.getProperty("nuts-endpoint-url"));
        if (!p.isEmpty()) {
            if (p.equals("dev") || p.equals("debug")) {
                p = "http://127.0.0.1:8080/public/nuts";
            } else if (p.matches("localhost") || p.matches("127[.]0[.]0[.][0-9]+")) {
                p = "http://" + p + ":8080/public/nuts";
            } else if (p.matches("localhost(:[0-9]+)?") || p.matches("127[.]0[.]0[.][0-9]+(:[0-9]+)?")) {
                p = "http://" + p + "/public/nuts";
            } else if (p.matches("[a-zAzZ]+(:[0-9]+)?") || p.matches("[0-9]+[.][0-9]+[.][0-9]+[.][0-9]+(:[0-9]+)?")) {
                p = "https://" + p + "/public/nuts";
            }
            return p;
        } else {
            String s = null;
            try {
                NWebCli cli = NWebCli.of();
                cli.setConnectTimeout(500);
                cli.setReadTimeout(500);
                s = NStringUtils.trim(cli.GET("https://raw.githubusercontent.com/thevpc/nuts/master/.endpoint")
                        .run().getContent().readString());
            } catch (Exception ex) {
                //error;
            }
            if (!NBlankable.isBlank(s) && s.startsWith("https://")) {
                return s;
            } else {
                return "https://nuts-pm.net/public/nuts";
            }
        }
    }

}

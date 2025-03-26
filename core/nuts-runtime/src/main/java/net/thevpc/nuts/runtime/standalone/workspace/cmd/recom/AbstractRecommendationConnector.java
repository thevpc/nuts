package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

import net.thevpc.nuts.*;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.clinfo.NCliInfo;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public abstract class AbstractRecommendationConnector implements RecommendationConnector {

    private String localUserUUID;
    private NWorkspace workspace;

    public AbstractRecommendationConnector(NWorkspace workspace) {
        this.workspace = workspace;
    }

    private String getLocalUserUUID() {
        if (localUserUUID != null) {
            return localUserUUID;
        }
        localUserUUID = NCliInfo.loadCliId(true);
        return localUserUUID;
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public Map getRecommendations(RequestQueryInfo ri, NRecommendationPhase phase, boolean failure) {
        validateRequest(ri);
        NId id = NId.get(ri.q.getId()).ifBlankEmpty().get();
        String name = phase.name().toLowerCase() + (failure ? "-failure" : "") + "-recommendations.json";
        String url = "/repo/" + ExtraApiUtils.resolveIdPath(id) + '/' + name;
        return post(url, ri, Map.class);
    }


    public abstract <T> T post(String url, RequestQueryInfo ri, Class<T> resultType);

    public void validateRequest(RequestQueryInfo ri) {
        NSession session= workspace.currentSession();
        NLiteral endPointURL = workspace.getProperty("nuts-endpoint-url").orNull();
        if (NBlankable.isBlank(ri.server)) {
            if (endPointURL == null || endPointURL.isBlank()) {
                String defaultURL = resolveDefaultEndpointURL();
                workspace.setProperty("nuts-endpoint-url", defaultURL);
            } else {
                ri.server = endPointURL.asStringValue().get();
            }
        }
        RequestAgent agent = ri.q.getAgent();
        NWorkspace ws = workspace;
        if (agent.getApiVersion() == null) {
            agent.setApiVersion(ws.getApiVersion().toString());
        }
        if (agent.getRuntimeId() == null) {
            agent.setRuntimeId(ws.getRuntimeId().toString());
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
            } else {
                //
            }
        } else {
            try {
                String s = NStringUtils.trimToNull(NPath.of("https://raw.githubusercontent.com/thevpc/nuts/master/.endpoint").readString());
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

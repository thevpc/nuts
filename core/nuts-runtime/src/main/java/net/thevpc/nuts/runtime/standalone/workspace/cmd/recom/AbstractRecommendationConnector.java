package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.util.NutsPlatformUtils;
import net.thevpc.nuts.util.NutsStringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public abstract class AbstractRecommendationConnector implements RecommendationConnector {

    private String localUserUUID;

    public AbstractRecommendationConnector() {
    }

    private String getLocalUserUUID(NutsSession session) {
        if (localUserUUID != null) {
            return localUserUUID;
        }
        Path userConfig = Paths.get(NutsPlatformUtils.getWorkspaceLocation(
                NutsOsFamily.getCurrent(),
                false,
                null
        )).getParent().resolve(".nuts-user-config");
        Map m = null;
        NutsElements elems = NutsElements.of(session);
        String _uuid = null;
        if (Files.exists(userConfig)) {
            try {
                m = elems.json().parse(userConfig, Map.class);
            } catch (Exception ex) {/*IGNORE*/
            }
            if (m != null) {
                _uuid = NutsStringUtils.trimToNull(m.get("user") == null ? null : String.valueOf(m.get("user")));
            }
        }
        if (_uuid != null) {
            localUserUUID = _uuid;
        } else {
            if (m == null) {
                m = new LinkedHashMap();
            }
            m.put("user", localUserUUID = UUID.randomUUID().toString());
            try {
                elems.json().setValue(m).print(userConfig);
            } catch (Exception ex) {
                //ignore
            }
        }
        return localUserUUID;
    }

    @Override
    public Map getRecommendations(RequestQueryInfo ri, NutsRecommendationPhase phase, boolean failure, NutsSession session) {
        validateRequest(ri, session);
        NutsId id = NutsId.of(ri.q.getId()).ifBlankNull().get(session);
        String name=phase.name().toLowerCase()+(failure?"-failure":"")+"-recommendations.json";
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

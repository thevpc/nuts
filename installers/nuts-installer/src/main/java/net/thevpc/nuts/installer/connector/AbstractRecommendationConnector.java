package net.thevpc.nuts.installer.connector;

import com.google.gson.Gson;
import net.thevpc.nuts.installer.util.NutsId;
import net.thevpc.nuts.installer.util.Utils;

import java.io.FileReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public abstract class AbstractRecommendationConnector implements RecommendationConnector {
    private String localUserUUID;

    public AbstractRecommendationConnector() {
    }

    private String getLocalUserUUID() {
        if (localUserUUID != null) {
            return localUserUUID;
        }
        Path userConfig = Paths.get(Utils.getWorkspaceLocation()).getParent().resolve(".nuts-user-config");
        Map m = null;
        String _uuid = null;
        if (Files.exists(userConfig)) {
            try (Reader rr=Files.newBufferedReader(userConfig)){
                m = new Gson().fromJson(rr, Map.class);
            } catch (Exception ex) {/*IGNORE*/}
            if (m != null) {
                _uuid = Utils.trim(m.get("user") == null ? null : String.valueOf(m.get("user")));
                if(_uuid.isEmpty()){
                    _uuid="";
                }
            }
        }
        if (_uuid != null) {
            localUserUUID = _uuid;
        } else {
            if (m == null) {
                m = new LinkedHashMap();
            }
            m.put("user", localUserUUID = UUID.randomUUID().toString());
            try (Writer w=Files.newBufferedWriter(userConfig)){
                w.write(new Gson().toJson(m));
            } catch (Exception ex) {
                //ignore
            }
        }
        return localUserUUID;
    }

    @Override
    public Map askInstallRecommendations(RequestQueryInfo ri) {
        validateRequest(ri);
        NutsId id = new NutsId(ri.q.getId());
        String url = "/repo/" + id.getGroupId().replace('.', '/')
                + '/' + id.getArtifactId()
                + '/' + id.getVersion()
                + "/install-recommendations.json";
        return post(url, ri, Map.class);
    }

    @Override
    public Map askUpdateRecommendations(RequestQueryInfo ri) {
        validateRequest(ri);
        NutsId id = new NutsId(ri.q.getId());
        String url = "/repo/" + id.getGroupId().replace('.', '/')
                + '/' + id.getArtifactId()
                + '/' + id.getVersion()
                + "/update-recommendations.json";
        return post(url, ri, Map.class);
    }

    public Map askDescriptor(RequestQueryInfo ri) {
        validateRequest(ri);
        NutsId id = new NutsId(ri.q.getId());
        if(id.getVersion().isEmpty()){
            String url = "/repo/" + id.getGroupId().replace('.', '/')
                    + '/' + id.getArtifactId()
                    + "/descriptor.json";
            return post(url, ri, Map.class);
        }else {
            String url = "/repo/" + id.getGroupId().replace('.', '/')
                    + '/' + id.getArtifactId()
                    + '/' + id.getVersion()
                    + "/descriptor.json";
            return post(url, ri, Map.class);
        }
    }

    @Override
    public Map askCompanionsRecommendations(RequestQueryInfo ri) {
        validateRequest(ri);
        NutsId id = new NutsId(ri.q.getId());
        String url = "/repo/" + id.getGroupId().replace('.', '/')
                + '/' + id.getArtifactId()
                + '/' + id.getVersion()
                + "/companion-recommendations.json";
        return post(url, ri, Map.class);
    }

    @Override
    public Map askInstallFailureRecommendations(RequestQueryInfo ri) {
        validateRequest(ri);
        NutsId id = new NutsId(ri.q.getId());
        String url = "/repo/" + id.getGroupId().replace('.', '/')
                + '/' + id.getArtifactId()
                + '/' + id.getVersion()
                + "/install-failure-recommendations.json";
        return post(url, ri, Map.class);
    }

    @Override
    public Map askUninstallFailureRecommendations(RequestQueryInfo ri) {
        validateRequest(ri);
        NutsId id = new NutsId(ri.q.getId());
        String url = "/repo/" + id.getGroupId().replace('.', '/')
                + '/' + id.getArtifactId()
                + '/' + id.getVersion()
                + "/uninstall-failure-recommendations.json";
        return post(url, ri, Map.class);
    }

    @Override
    public Map askBootstrapFailureRecommendations(RequestQueryInfo ri) {
        validateRequest(ri);
        NutsId id = new NutsId(ri.q.getId());
        String url = "/repo/" + id.getGroupId().replace('.', '/')
                + '/' + id.getArtifactId()
                + '/' + id.getVersion()
                + "/bootstrap-failure-recommendations.json";
        return post(url, ri, Map.class);
    }

    @Override
    public Map askUpdateFailureRecommendations(RequestQueryInfo ri) {
        validateRequest(ri);
        NutsId id = new NutsId(ri.q.getId());
        String url = "/repo/" + id.getGroupId().replace('.', '/')
                + '/' + id.getArtifactId()
                + '/' + id.getVersion()
                + "/update-failure-recommendations.json";
        return post(url, ri, Map.class);
    }

    public Map askUninstallRecommendations(RequestQueryInfo ri) {
        validateRequest(ri);
        NutsId id = new NutsId(ri.q.getId());
        String url = "/repo/" + id.getGroupId().replace('.', '/')
                + '/' + id.getArtifactId()
                + '/' + id.getVersion()
                + "/alternatives.json";
        return post(url, ri, Map.class);
    }

    public abstract <T> T post(String url, RequestQueryInfo ri, Class<T> resultType);

    public void validateRequest(RequestQueryInfo ri) {
        if (Utils.isBlank(ri.url)) {
            ri.url = "{protocol}://{host}:{port}/{context}";
        }
        if (Utils.isBlank(ri.host)) {
            ri.host = "thevpc.net";
        }
        if (Utils.isBlank(ri.protocol)) {
            ri.protocol = "https";
        }else if(!ri.protocol.equals("http") && !ri.protocol.equals("https")){
            throw new IllegalArgumentException("invalid protocol "+ri.protocol);
        }
        if (Utils.isBlank(ri.context)) {
            ri.context = "nuts";
        } else if (ri.context.startsWith("/")) {
            ri.context = ri.context.substring(1);
        }
        if (ri.port <= 0) {
            if(ri.protocol.equals("https")){
                ri.port = 443;
            }else{
                ri.port = 80;
            }
        }
        ri.url = ri.url
                .replace("{protocol}", ri.protocol)
                .replace("{host}", ri.host)
                .replace("{port}", String.valueOf(ri.port))
                .replace("{context}", ri.context)
        ;
        RequestAgent agent = ri.q.getAgent();
        if (agent.getArch() == null) {
            agent.setArch(System.getProperty("os.arch"));
        }
        if (agent.getOs() == null) {
            agent.setOs(System.getProperty("os.name")+"#"+System.getProperty("os.version"));
        }
        if (agent.getPlatform() == null) {
            agent.setPlatform("java#"+System.getProperty("java.version"));
        }
        if (agent.getUserDigest() == null) {
            agent.setUserDigest(getLocalUserUUID());
        }
        if (agent.getUserLocale() == null) {
            agent.setUserLocale(Locale.getDefault().toString());
        }
        if (agent.getUserTimeZone() == null) {
            agent.setUserTimeZone(TimeZone.getDefault().getDisplayName());
        }
    }

}

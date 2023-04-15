package net.thevpc.nuts.installer.connector;

import com.google.gson.Gson;
import net.thevpc.nuts.installer.model.NId;
import net.thevpc.nuts.installer.util.Utils;

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
        Path userConfig = Paths.get(Utils.getBaseNutsLocation()).resolve(".nuts-user-config");
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
    public Map getRecommendations(RequestQueryInfo ri) {
        validateRequest(ri);
        NId id = new NId(ri.q.getId());
        String name="installer-recommendations.json";
        String url = "/repo/" + id.getGroupId().replace('.', '/')
                + '/' + id.getArtifactId()
                + '/' + id.getVersion()
                + '/' + name;
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

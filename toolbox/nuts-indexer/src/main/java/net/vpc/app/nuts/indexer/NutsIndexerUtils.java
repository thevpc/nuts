package net.vpc.app.nuts.indexer;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.indexer.services.NutsRepositoryResource;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NutsIndexerUtils {

    public static Path getCacheDir(NutsWorkspace ws, String entity) {
        String k = "NutsIndexerUtils.CACHE." + entity;
        String m = (String) ws.getUserProperties().get(k);
        if(m==null){
            m=ws.getConfigManager()
                    .getStoreLocation(ws.resolveIdForClass(NutsRepositoryResource.class)
                                    .getSimpleNameId()
                                    .setVersion("LATEST"),
                            NutsStoreFolder.CACHE)+File.separator+ new File(ws.getConfigManager().getWorkspaceLocation()).getName()+"-"+ws.getUuid() + "/" + entity;
            ws.getUserProperties().put(k,m);
        }
        return new File(m).toPath();
    }

    public static Map<String, String> nutsRepositoryToMap(NutsRepository repository, int level) {
        if (repository == null) {
            return new HashMap<>();
        }
        Map<String, String> entity = new HashMap<>();
        entity.put("name", repository.getName());
        entity.put("type", repository.getRepositoryType());
        entity.put("location", repository.getConfigManager().getLocation());
        entity.put("enabled", String.valueOf(repository.isEnabled()));
        entity.put("speed", String.valueOf(repository.getSpeed()));
        NutsWorkspace ws = repository.getWorkspace();
        if (level == 0) {
            entity.put("mirrors", Arrays.toString(
                    Arrays.stream(repository.getMirrors())
                            .map(nutsRepository -> mapToJson(nutsRepositoryToMap(nutsRepository, level + 1), ws))
                            .toArray()));
            entity.put("parents", mapToJson(nutsRepositoryToMap(repository.getParentRepository(), level + 1), ws));
        }
        return entity;
    }

    public static String mapToJson(Map<String, String> map, NutsWorkspace ws) {
        StringWriter s = new StringWriter();
        ws.getIOManager().writeJson(map, s, true);
        return s.toString();
    }

    public static Map<String, String> nutsRepositoryToMap(NutsRepository repository) {
        return nutsRepositoryToMap(repository, 0);
    }

    public static Map<String, String> nutsIdToMap(NutsId id) {
        Map<String, String> entity = new HashMap<>();
        _condPut(entity,"name", id.getName());
        _condPut(entity,"namespace", id.getNamespace());
        _condPut(entity,"group", id.getGroup());
        _condPut(entity,"version", id.getVersion().getValue());
//        _condPut(entity,"face", id.getFace());
        _condPut(entity,"os", id.getOs());
        _condPut(entity,"osdist", id.getOsdist());
        _condPut(entity,"scope", StringUtils.isEmpty(id.getScope())?"compile":id.getScope());
        _condPut(entity,"arch", id.getArch());
        _condPut(entity,"classifier", id.getClassifier());
        _condPut(entity,"alternative", id.getAlternative());
        return entity;
    }

    public static Map<String, String> nutsDependencyToMap(NutsDependency dependency) {
        Map<String, String> entity = new HashMap<>();
        _condPut(entity,"name", dependency.getName());
        _condPut(entity,"namespace", dependency.getNamespace());
        _condPut(entity,"group", dependency.getGroup());
        _condPut(entity,"version", dependency.getVersion().getValue());
//        _condPut(entity,"face", dependency.getId().getFace());
        _condPut(entity,"os", dependency.getId().getOs());
        _condPut(entity,"osdist", dependency.getId().getOsdist());
        _condPut(entity,"scope", dependency.getScope());
        _condPut(entity,"arch", dependency.getId().getArch());
        _condPut(entity,"classifier", dependency.getId().getClassifier());
        _condPut(entity,"alternative", dependency.getId().getAlternative());
        return entity;
    }
    private static void _condPut(Map<String, String> m, String k, String v){
        if(!trim(v).isEmpty()){
            m.put(k,v);
        }
    }

    public static BooleanQuery nutsIdToQuery(
            String name,
            String namespace,
            String group,
            String version,
            String os,
            String osdist,
            String arch,
            String classifier,
            String scope,
            String alternative) {
        return new BooleanQuery.Builder()
                .add(new PhraseQuery.Builder().add(new Term("name", name)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("namespace", namespace)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("group", group)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("version", version)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("os", os)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("osdist", osdist)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("arch", arch)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("classifier", classifier)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("scope", scope)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("alternative", alternative)).build(), BooleanClause.Occur.MUST)
                .add(new BooleanClause(new MatchAllDocsQuery(), BooleanClause.Occur.SHOULD))
                .build();
    }

    public static NutsId mapToNutsId(Map<String, Object> map, NutsWorkspace ws) {
        return ws.createIdBuilder()
                .setName(trim((String) map.get("name")))
                .setNamespace(trim((String) map.get("namespace")))
                .setGroup(trim((String) map.get("group")))
                .setVersion(trim((String) map.get("version")))
                .setOs(trim((String) map.get("os")))
                .setOsdist(trim((String) map.get("osdist")))
                .setClassifier(trim((String) map.get("classifier")))
                .setScope(trim((String) map.get("scope")))
                .setArch(trim((String) map.get("arch")))
                .setAlternative(trim((String) map.get("alternative")))
                .build();
    }

    public static Query mapToQuery(Map<String, String> map) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.add(new PhraseQuery.Builder()
                            .add(new Term(entry.getKey(),
                                    trim(entry.getValue()))).build(),
                    BooleanClause.Occur.MUST);
        }
        builder.add(new BooleanClause(new MatchAllDocsQuery(), BooleanClause.Occur.SHOULD));
        return builder.build();
    }

    public static String trim(String s) {
        return s == null ? "" : s.trim();
    }

}

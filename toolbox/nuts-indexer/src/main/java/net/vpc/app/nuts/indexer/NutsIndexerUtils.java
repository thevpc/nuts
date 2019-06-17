package net.vpc.app.nuts.indexer;

import net.vpc.app.nuts.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class NutsIndexerUtils {

    public static Path getCacheDir(NutsWorkspace ws, String entity) {
        String k = "NutsIndexerUtils.CACHE." + entity;
        String m = (String) ws.getUserProperties().get(k);
        if (m == null) {
            m = ws.config()
                    .getStoreLocation(ws.resolveId(NutsIndexerUtils.class)
                            .getSimpleNameId(),
                            NutsStoreLocation.CACHE) + File.separator + entity;
            ws.getUserProperties().put(k, m);
        }
        return new File(m).toPath();
    }

    public static Map<String, String> nutsRepositoryToMap(NutsRepository repository, int level) {
        if (repository == null) {
            return new HashMap<>();
        }
        Map<String, String> entity = new HashMap<>();
        entity.put("name", repository.config().getName());
        entity.put("type", repository.getRepositoryType());
        entity.put("location", repository.config().getLocation(false));
        entity.put("enabled", String.valueOf(repository.config().isEnabled()));
        entity.put("speed", String.valueOf(repository.config().getSpeed()));
        NutsWorkspace ws = repository.getWorkspace();
        if (level == 0) {
            entity.put("mirrors", Arrays.toString(
                    Arrays.stream(repository.config().getMirrors())
                            .map(nutsRepository -> mapToJson(nutsRepositoryToMap(nutsRepository, level + 1), ws))
                            .toArray()));
            entity.put("parents", mapToJson(nutsRepositoryToMap(repository.getParentRepository(), level + 1), ws));
        }
        return entity;
    }

    public static String mapToJson(Map<String, String> map, NutsWorkspace ws) {
        StringWriter s = new StringWriter();
        ws.format().json().set(map).print(s);
        return s.toString();
    }

    public static Map<String, String> nutsRepositoryToMap(NutsRepository repository) {
        return nutsRepositoryToMap(repository, 0);
    }

    public static Map<String, String> nutsIdToMap(NutsId id) {
        Map<String, String> entity = new HashMap<>();
        id = id.setFace(StringUtils.isEmpty(id.getFace()) ? "default" : id.getFace())
                .setScope(StringUtils.isEmpty(id.getScope()) ? "compile" : id.getScope());
        _condPut(entity, "name", id.getName());
        _condPut(entity, "namespace", id.getNamespace());
        _condPut(entity, "group", id.getGroup());
        _condPut(entity, "version", id.getVersion().getValue());
        _condPut(entity, "face", id.getFace());
        _condPut(entity, "os", id.getOs());
        _condPut(entity, "osdist", id.getOsdist());
        _condPut(entity, "scope", id.getScope());
        _condPut(entity, "arch", id.getArch());
        _condPut(entity, "classifier", id.getClassifier());
        _condPut(entity, "alternative", id.getAlternative());
        _condPut(entity, "stringId", id.toString());
        return entity;
    }

    public static Map<String, String> nutsDependencyToMap(NutsDependency dependency) {
        Map<String, String> entity = new HashMap<>();
        _condPut(entity, "name", dependency.getName());
        _condPut(entity, "namespace", dependency.getNamespace());
        _condPut(entity, "group", dependency.getGroup());
        _condPut(entity, "version", dependency.getVersion().getValue());
        dependency.getId().setFace(StringUtils.isEmpty(dependency.getId().getFace()) ? "default" : dependency.getId().getFace());
        _condPut(entity, "face", dependency.getId().getFace());
        _condPut(entity, "os", dependency.getId().getOs());
        _condPut(entity, "osdist", dependency.getId().getOsdist());
        dependency.getId().setScope(StringUtils.isEmpty(dependency.getId().getScope()) ? "compile" : dependency.getId().getScope());
        _condPut(entity, "scope", dependency.getScope());
        _condPut(entity, "arch", dependency.getId().getArch());
        _condPut(entity, "classifier", dependency.getId().getClassifier());
        _condPut(entity, "alternative", dependency.getId().getAlternative());
        _condPut(entity, "stringId", dependency.getId().toString());
        return entity;
    }

    private static void _condPut(Map<String, String> m, String k, String v) {
        if (!trim(v).isEmpty()) {
            m.put(k, v);
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

    public static NutsId mapToNutsId(Map<String, String> map, NutsWorkspace ws) {
        return ws.idBuilder()
                .setName(trim(map.get("name")))
                .setNamespace(trim(map.get("namespace")))
                .setGroup(trim(map.get("group")))
                .setVersion(trim(map.get("version")))
                .setOs(trim(map.get("os")))
                .setOsdist(trim(map.get("osdist")))
                .setClassifier(trim(map.get("classifier")))
                .setScope(trim(map.get("scope")))
                .setArch(trim(map.get("arch")))
                .setAlternative(trim(map.get("alternative")))
                .build();
    }

    public static Query mapToQuery(Map<String, String> map, String... exclus) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        Set<String> set = Arrays.stream(exclus).collect(Collectors.toSet());
        if (set.size() > 0) {
            set.add("stringId");
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!set.contains(entry.getKey())) {
                builder.add(new PhraseQuery.Builder()
                        .add(new Term(entry.getKey(),
                                trim(entry.getValue()))).build(),
                        BooleanClause.Occur.MUST);
            }
        }
        builder.add(new BooleanClause(new MatchAllDocsQuery(), BooleanClause.Occur.SHOULD));
        return builder.build();
    }

    public static String trim(String s) {
        return s == null ? "" : s.trim();
    }

}

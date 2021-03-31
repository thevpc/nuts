package net.thevpc.nuts.indexer;

import net.thevpc.nuts.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class NutsIndexerUtils {

    public static Path getCacheDir(NutsSession session, String entity) {
        String k = "NutsIndexerUtils.CACHE." + entity;
        NutsWorkspace ws = session.getWorkspace();
        String m = (String) ws.env().getProperty(k);
        if (m == null) {
            m = ws.locations()
                    .getStoreLocation(ws.id().resolveId(NutsIndexerUtils.class, session),
                            NutsStoreLocation.CACHE) + File.separator + entity;
            ws.env().setProperty(k, m);
        }
        return new File(m).toPath();
    }

    public static Map<String, String> nutsRepositoryToMap(NutsRepository repository, int level, NutsSession session) {
        if (repository == null) {
            return new HashMap<>();
        }
        Map<String, String> entity = new HashMap<>();
        entity.put("name", repository.getName());
        entity.put("type", repository.getRepositoryType());
        entity.put("location", repository.config().getLocation(false));
        entity.put("enabled", String.valueOf(repository.config().isEnabled()));
        entity.put("speed", String.valueOf(repository.config().getSpeed()));
        NutsWorkspace ws = repository.getWorkspace();
        if (level == 0) {
            entity.put("mirrors", Arrays.toString(
                    Arrays.stream(repository.config().getMirrors(session))
                            .map(nutsRepository -> mapToJson(nutsRepositoryToMap(nutsRepository, level + 1, session), ws))
                            .toArray()));
            entity.put("parents", mapToJson(nutsRepositoryToMap(repository.getParentRepository(), level + 1, session), ws));
        }
        return entity;
    }

    public static String mapToJson(Map<String, String> map, NutsWorkspace ws) {
        StringWriter s = new StringWriter();
        ws.formats().element().setContentType(NutsContentType.JSON).setValue(map).print(s);
        return s.toString();
    }

    public static Map<String, String> nutsRepositoryToMap(NutsRepository repository, NutsSession session) {
        return nutsRepositoryToMap(repository, 0, session);
    }

    public static Map<String, String> nutsIdToMap(NutsId id) {
        Map<String, String> entity = new HashMap<>();
        id = id.builder().setFace(StringUtils.isEmpty(id.getFace()) ? "default" : id.getFace()).build();
        _condPut(entity, "name", id.getArtifactId());
        _condPut(entity, "namespace", id.getNamespace());
        _condPut(entity, "group", id.getGroupId());
        _condPut(entity, "version", id.getVersion().getValue());
        _condPut(entity, "face", id.getFace());
        _condPut(entity, "os", id.getOs());
        _condPut(entity, "osdist", id.getOsdist());
        _condPut(entity, "arch", id.getArch());
        _condPut(entity, NutsConstants.IdProperties.CLASSIFIER, id.getClassifier());
//        _condPut(entity, NutsConstants.IdProperties.ALTERNATIVE, id.getAlternative());
        _condPut(entity, "stringId", id.toString());
        return entity;
    }

    public static Map<String, String> nutsDependencyToMap(NutsDependency dependency) {
        Map<String, String> entity = new HashMap<>();
        _condPut(entity, "name", dependency.getArtifactId());
        _condPut(entity, "namespace", dependency.getNamespace());
        _condPut(entity, "group", dependency.getGroupId());
        _condPut(entity, "version", dependency.getVersion().getValue());
        NutsId id2 = dependency.toId().builder()
                .setFace(StringUtils.isEmpty(dependency.toId().getFace()) ? "default" : dependency.toId().getFace())
                .build();
        _condPut(entity, NutsConstants.IdProperties.FACE, id2.getFace());
        _condPut(entity, "os", id2.getOs());
        _condPut(entity, "osdist", id2.getOsdist());
        _condPut(entity, "arch", id2.getArch());
        _condPut(entity, NutsConstants.IdProperties.CLASSIFIER, id2.getClassifier());
//        _condPut(entity, NutsConstants.IdProperties.ALTERNATIVE, dependency.getId().getAlternative());
        _condPut(entity, "stringId", id2.toString());
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
            String classifier
//            ,String alternative
    ) {
        return new BooleanQuery.Builder()
                .add(new PhraseQuery.Builder().add(new Term("name", name)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("namespace", namespace)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("group", group)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("version", version)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("os", os)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("osdist", osdist)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("arch", arch)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term(NutsConstants.IdProperties.CLASSIFIER, classifier)).build(), BooleanClause.Occur.MUST)
//                .add(new PhraseQuery.Builder().add(new Term(NutsConstants.IdProperties.ALTERNATIVE, alternative)).build(), BooleanClause.Occur.MUST)
                .add(new BooleanClause(new MatchAllDocsQuery(), BooleanClause.Occur.SHOULD))
                .build();
    }

    public static NutsId mapToNutsId(Map<String, String> map, NutsWorkspace ws) {
        return ws.id().builder()
                .setArtifactId(trim(map.get("name")))
                .setNamespace(trim(map.get("namespace")))
                .setGroupId(trim(map.get("group")))
                .setVersion(trim(map.get("version")))
                .setOs(trim(map.get("os")))
                .setOsdist(trim(map.get("osdist")))
                .setClassifier(trim(map.get(NutsConstants.IdProperties.CLASSIFIER)))
                .setArch(trim(map.get("arch")))
//                .setAlternative(trim(map.get(NutsConstants.IdProperties.ALTERNATIVE)))
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

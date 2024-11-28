package net.thevpc.nuts.indexer;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NStringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NIndexerUtils {

    public static Path getCacheDir(String entity) {
        String k = "NutsIndexerUtils.CACHE." + entity;
        String m = NEnvs.of().getProperty(k).flatMap(NLiteral::asString).orNull();
        if (m == null) {
            m = NLocations.of()
                    .getStoreLocation(NId.ofClass(NIndexerUtils.class).get(),
                            NStoreType.CACHE) + File.separator + entity;
            NEnvs.of().setProperty(k, m);
        }
        return new File(m).toPath();
    }

    public static Map<String, String> nutsRepositoryToMap(NRepository repository, int level) {
        if (repository == null) {
            return new HashMap<>();
        }
        Map<String, String> entity = new HashMap<>();
        entity.put("name", repository.getName());
        entity.put("type", repository.getRepositoryType());
        entity.put("location", repository.config().getLocation().toString());
        entity.put("enabled", String.valueOf(repository.config().isEnabled()));
        entity.put("speed", String.valueOf(repository.config().getSpeed()));
        NWorkspace ws = repository.getWorkspace();
        if (level == 0) {
            entity.put("mirrors", Arrays.toString(
                    repository.config().getMirrors().stream()
                            .map(nutsRepository -> mapToJson(nutsRepositoryToMap(nutsRepository, level + 1)))
                            .toArray()));
            entity.put("parents", mapToJson(nutsRepositoryToMap(repository.getParentRepository(), level + 1)));
        }
        return entity;
    }

    public static String mapToJson(Map<String, String> map) {
        StringWriter s = new StringWriter();
        NElements.of().json().setValue(map)
                .setNtf(false).print(s);
        return s.toString();
    }

    public static Map<String, String> nutsRepositoryToMap(NRepository repository) {
        return nutsRepositoryToMap(repository, 0);
    }

    public static Map<String, String> nutsIdToMap(NId id) {
        Map<String, String> entity = new HashMap<>();
        id = id.builder().setFace(StringUtils.isEmpty(id.getFace()) ? "default" : id.getFace()).build();
        _condPut(entity, "name", id.getArtifactId());
        _condPut(entity, "namespace", id.getRepository());
        _condPut(entity, "group", id.getGroupId());
        _condPut(entity, "version", id.getVersion().getValue());
        _condPut(entity, "face", id.getFace());
        _condPut(entity, NConstants.IdProperties.OS, String.join(",",id.getCondition().getOs()));
        _condPut(entity, NConstants.IdProperties.OS_DIST, String.join(",",id.getCondition().getOsDist()));
        _condPut(entity, NConstants.IdProperties.ARCH, String.join(",",id.getCondition().getArch()));
        _condPut(entity, NConstants.IdProperties.PLATFORM, String.join(",",id.getCondition().getPlatform()));
        _condPut(entity, NConstants.IdProperties.PROFILE, String.join(",",id.getCondition().getProfiles()));
        _condPut(entity, NConstants.IdProperties.DESKTOP, String.join(",",id.getCondition().getDesktopEnvironment()));
        _condPut(entity, NConstants.IdProperties.CLASSIFIER, id.getClassifier());
//        _condPut(entity, NutsConstants.IdProperties.ALTERNATIVE, id.getAlternative());
        _condPut(entity, "stringId", id.toString());
        return entity;
    }

    public static Map<String, String> nutsDependencyToMap(NDependency dependency) {
        Map<String, String> entity = new HashMap<>();
        _condPut(entity, "name", dependency.getArtifactId());
        _condPut(entity, "namespace", dependency.getRepository());
        _condPut(entity, "group", dependency.getGroupId());
        _condPut(entity, "version", dependency.getVersion().getValue());
        NId id2 = dependency.toId().builder()
                .setFace(StringUtils.isEmpty(dependency.toId().getFace()) ? "default" : dependency.toId().getFace())
                .build();
        _condPut(entity, NConstants.IdProperties.FACE, id2.getFace());

        _condPut(entity, NConstants.IdProperties.OS, String.join(",",id2.getCondition().getOs()));
        _condPut(entity, NConstants.IdProperties.OS_DIST, String.join(",",id2.getCondition().getOsDist()));
        _condPut(entity, NConstants.IdProperties.ARCH, String.join(",",id2.getCondition().getArch()));
        _condPut(entity, NConstants.IdProperties.PLATFORM, String.join(",",id2.getCondition().getPlatform()));
        _condPut(entity, NConstants.IdProperties.PROFILE, String.join(",",id2.getCondition().getProfiles()));
        _condPut(entity, NConstants.IdProperties.DESKTOP, String.join(",",id2.getCondition().getDesktopEnvironment()));
        _condPut(entity, NConstants.IdProperties.CLASSIFIER, id2.getClassifier());

//        _condPut(entity, NutsConstants.IdProperties.ALTERNATIVE, dependency.getId().getAlternative());
        _condPut(entity, "stringId", id2.toString());
        return entity;
    }

    private static void _condPut(Map<String, String> m, String k, String v) {
        if (!NStringUtils.trim(v).isEmpty()) {
            m.put(k, v);
        }
    }

    public static BooleanQuery nutsIdToQuery(
            String name,
            String namespace,
            String group,
            String version,
            String os,
            String osDist,
            String arch,
            String platform,
            String desktopEnvironment,
            String classifier
//            ,String alternative
    ) {
        return new BooleanQuery.Builder()
                .add(new PhraseQuery.Builder().add(new Term("name", name)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("namespace", namespace)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("group", group)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term("version", version)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term(NConstants.IdProperties.OS, os)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term(NConstants.IdProperties.OS_DIST, osDist)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term(NConstants.IdProperties.ARCH, arch)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term(NConstants.IdProperties.PLATFORM, platform)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term(NConstants.IdProperties.DESKTOP, desktopEnvironment)).build(), BooleanClause.Occur.MUST)
                .add(new PhraseQuery.Builder().add(new Term(NConstants.IdProperties.CLASSIFIER, classifier)).build(), BooleanClause.Occur.MUST)
//                .add(new PhraseQuery.Builder().add(new Term(NutsConstants.IdProperties.ALTERNATIVE, alternative)).build(), BooleanClause.Occur.MUST)
                .add(new BooleanClause(new MatchAllDocsQuery(), BooleanClause.Occur.SHOULD))
                .build();
    }

    public static NId mapToNutsId(Map<String, String> map) {
        return NIdBuilder.of()
                .setArtifactId(NStringUtils.trim(map.get("name")))
                .setRepository(NStringUtils.trim(map.get("namespace")))
                .setGroupId(NStringUtils.trim(map.get("group")))
                .setVersion(NStringUtils.trim(map.get("version")))
                .setCondition(
                        NEnvConditionBuilder.of()
                                //TODO what if the result is ',' separated array?
                                .setArch(Arrays.asList(NStringUtils.trim(map.get(NConstants.IdProperties.ARCH))))
                                .setOs(Arrays.asList(NStringUtils.trim(map.get(NConstants.IdProperties.OS))))
                                .setOsDist(Arrays.asList(NStringUtils.trim(map.get(NConstants.IdProperties.OS_DIST))))
                                .setPlatform(Arrays.asList(NStringUtils.trim(map.get(NConstants.IdProperties.PLATFORM))))
                                .setDesktopEnvironment(Arrays.asList(NStringUtils.trim(map.get(NConstants.IdProperties.DESKTOP))))
                )
                .setClassifier(NStringUtils.trim(map.get(NConstants.IdProperties.CLASSIFIER)))
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
                                        NStringUtils.trim(entry.getValue()))).build(),
                        BooleanClause.Occur.MUST);
            }
        }
        builder.add(new BooleanClause(new MatchAllDocsQuery(), BooleanClause.Occur.SHOULD));
        return builder.build();
    }

}

package net.thevpc.nuts.clown;

import net.thevpc.nuts.NutsConstants;
import net.thevpc.nuts.NutsEnvCondition;
import net.thevpc.nuts.NutsId;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class NutsClownUtils {

    public static Map<String, String> nutsIdToMap(NutsId id) {
        Map<String, String> entity = new HashMap<>();
        id = id.builder().setFace(StringUtils.isEmpty(id.getFace()) ? "default" : id.getFace())
//            .scope(StringUtils.isEmpty(id.getScope()) ? NutsDependencyScope.API.id() : id.getScope())
        .build()
        ;
        NutsEnvCondition cond = id.getCondition();
        _condPut(entity, "name", id.getArtifactId());
        _condPut(entity, "group", id.getGroupId());
        _condPut(entity, "version", id.getVersion().getValue());
        _condPut(entity, "face", id.getFace());
        _condPut(entity, "os", cond.getOs().length == 0 ? null : cond.getOs()[0]);
        _condPut(entity, "osdist", cond.getOsDist().length == 0 ? null : cond.getOsDist()[0]);
//        _condPut(entity, "scope", id.getScope());
        _condPut(entity, "arch", cond.getArch().length == 0 ? null : cond.getArch()[0]);
        _condPut(entity, NutsConstants.IdProperties.CLASSIFIER, id.getClassifier());
//        _condPut(entity, NutsConstants.QueryKeys.ALTERNATIVE, id.getAlternative());
        _condPut(entity, "stringId", id.toString());
        return entity;
    }

    private static void _condPut(Map<String, String> m, String k, String v) {
        if (!trim(v).isEmpty()) {
            m.put(k, v);
        }
    }

    public static String trim(String s) {
        return s == null ? "" : s.trim();
    }

}

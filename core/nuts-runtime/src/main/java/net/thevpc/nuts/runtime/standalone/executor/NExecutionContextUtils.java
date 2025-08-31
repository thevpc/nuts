package net.thevpc.nuts.runtime.standalone.executor;

import net.thevpc.nuts.*;


import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;

import java.util.HashMap;
import java.util.Map;

public class NExecutionContextUtils {
    public static Map<String, String> defaultEnv(NDefinition definition) {
        Map<String, String> m = new HashMap<>();
        m.put("NUTS_ID", definition.getId().toString());
        m.put("NUTS_ARTIFACT_ID", definition.getId().getArtifactId());
        m.put("NUTS_GROUP_ID", definition.getId().getGroupId());
        m.put("NUTS_VERSION", definition.getId().getVersion().toString());
        for (NStoreType value : NStoreType.values()) {
            NPath v = NPath.ofIdStore(definition.getId(), value);
            m.put("NUTS_ID_" + value.name(), v.toString());
        }
        return m;
    }

    public static StringPlaceHolderParser.PlaceHolderProvider<NDefinition> DEFINITION_PLACEHOLDER = new StringPlaceHolderParser.PlaceHolderProvider<NDefinition>() {
        @Override
        public String get(String key, NDefinition definition) {
            switch (key) {
                case "NUTS_ID": {
                    return (definition.getId().toString());
                }
                case "NUTS_ID_BIN": {
                    NPath v = NPath.ofIdStore(definition.getId(), NStoreType.BIN);
                    return (v.toString());
                }
                case "NUTS_ID_CONF": {
                    NPath v = NPath.ofIdStore(definition.getId(), NStoreType.CONF);
                    return (v.toString());
                }
                case "NUTS_ID_LOG": {
                    NPath v = NPath.ofIdStore(definition.getId(), NStoreType.LOG);
                    return (v.toString());
                }
                case "NUTS_ID_CACHE": {
                    NPath v = NPath.ofIdStore(definition.getId(), NStoreType.CACHE);
                    return (v.toString());
                }
                case "NUTS_ID_LIB": {
                    NPath v = NPath.ofIdStore(definition.getId(), NStoreType.LIB);
                    return (v.toString());
                }
                case "NUTS_ID_RUN": {
                    NPath v = NPath.ofIdStore(definition.getId(), NStoreType.RUN);
                    return (v.toString());
                }
                case "NUTS_ID_TEMP": {
                    NPath v = NPath.ofIdStore(definition.getId(), NStoreType.TEMP);
                    return (v.toString());
                }
                case "NUTS_ID_VAR": {
                    NPath v = NPath.ofIdStore(definition.getId(), NStoreType.VAR);
                    return (v.toString());
                }
            }
            return null;
        }
    };

    public static StringPlaceHolderParser.PlaceHolderProvider<NExecutionContext> EXECUTION_CONTEXT_PLACEHOLDER = new StringPlaceHolderParser.PlaceHolderProvider<NExecutionContext>() {
        @Override
        public String get(String key, NExecutionContext executionContext) {
            return DEFINITION_PLACEHOLDER.get(key, executionContext.getDefinition());
        }
    };


}

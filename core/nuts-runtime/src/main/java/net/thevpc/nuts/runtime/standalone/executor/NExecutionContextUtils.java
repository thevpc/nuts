package net.thevpc.nuts.runtime.standalone.executor;


import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.command.NExecutionContext;
import net.thevpc.nuts.core.NStoreKey;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;

import java.util.HashMap;
import java.util.Map;

public class NExecutionContextUtils {
    public static Map<String, String> defaultEnv(NDefinition definition) {
        Map<String, String> m = new HashMap<>();
        m.put("NUTS_ID", definition.id().toString());
        m.put("NUTS_ARTIFACT_ID", definition.id().artifactId());
        m.put("NUTS_GROUP_ID", definition.id().groupId());
        m.put("NUTS_VERSION", definition.id().version().toString());
        for (NStoreType type : NStoreType.values()) {
            NPath v = NPath.of(NStoreKey.of(definition.id()).type(type));
            m.put("NUTS_ID_" + type.name(), v.toString());
        }
        return m;
    }

    public static StringPlaceHolderParser.PlaceHolderProvider<NDefinition> DEFINITION_PLACEHOLDER = new StringPlaceHolderParser.PlaceHolderProvider<NDefinition>() {
        @Override
        public String get(String key, NDefinition definition) {
            switch (key) {
                case "NUTS_ID": {
                    return (definition.id().toString());
                }
                case "NUTS_ID_BIN": {
                    NPath v = NPath.of(NStoreKey.ofBin(definition.id()));
                    return (v.toString());
                }
                case "NUTS_ID_CONF": {
                    NPath v = NPath.of(NStoreKey.ofConf(definition.id()));
                    return (v.toString());
                }
                case "NUTS_ID_LOG": {
                    NPath v = NPath.of(NStoreKey.ofLog(definition.id()));
                    return (v.toString());
                }
                case "NUTS_ID_CACHE": {
                    NPath v = NPath.of(NStoreKey.ofCache(definition.id()));
                    return (v.toString());
                }
                case "NUTS_ID_LIB": {
                    NPath v = NPath.of(NStoreKey.ofLib(definition.id()));
                    return (v.toString());
                }
                case "NUTS_ID_RUN": {
                    NPath v = NPath.of(NStoreKey.ofRun(definition.id()));
                    return (v.toString());
                }
                case "NUTS_ID_TEMP": {
                    NPath v = NPath.of(NStoreKey.ofTemp(definition.id()));
                    return (v.toString());
                }
                case "NUTS_ID_VAR": {
                    NPath v = NPath.of(NStoreKey.ofVar(definition.id()));
                    return (v.toString());
                }
            }
            return null;
        }
    };

    public static StringPlaceHolderParser.PlaceHolderProvider<NExecutionContext> EXECUTION_CONTEXT_PLACEHOLDER = new StringPlaceHolderParser.PlaceHolderProvider<NExecutionContext>() {
        @Override
        public String get(String key, NExecutionContext executionContext) {
            return DEFINITION_PLACEHOLDER.get(key, executionContext.definition());
        }
    };


}

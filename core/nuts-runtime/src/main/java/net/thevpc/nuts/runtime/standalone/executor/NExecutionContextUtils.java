package net.thevpc.nuts.runtime.standalone.executor;

import net.thevpc.nuts.*;


import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;

public class NExecutionContextUtils {
    public static StringPlaceHolderParser.PlaceHolderProvider<NDefinition> DEFINITION_PLACEHOLDER=new StringPlaceHolderParser.PlaceHolderProvider<NDefinition>() {
        @Override
        public String get(String key, NDefinition definition) {
            switch (key) {
                case "NUTS_ID": {
                    return (definition.getId().toString());
                }
                case "NUTS_ID_BIN": {
                    NPath v = NWorkspace.of().getStoreLocation(definition.getId(), NStoreType.BIN);
                    return (v.toString());
                }
                case "NUTS_ID_CONF": {
                    NPath v = NWorkspace.of().getStoreLocation(definition.getId(), NStoreType.CONF);
                    return (v.toString());
                }
                case "NUTS_ID_LOG": {
                    NPath v = NWorkspace.of().getStoreLocation(definition.getId(), NStoreType.LOG);
                    return (v.toString());
                }
                case "NUTS_ID_CACHE": {
                    NPath v = NWorkspace.of().getStoreLocation(definition.getId(), NStoreType.CACHE);
                    return (v.toString());
                }
                case "NUTS_ID_LIB": {
                    NPath v = NWorkspace.of().getStoreLocation(definition.getId(), NStoreType.LIB);
                    return (v.toString());
                }
                case "NUTS_ID_RUN": {
                    NPath v = NWorkspace.of().getStoreLocation(definition.getId(), NStoreType.RUN);
                    return (v.toString());
                }
                case "NUTS_ID_TEMP": {
                    NPath v = NWorkspace.of().getStoreLocation(definition.getId(), NStoreType.TEMP);
                    return (v.toString());
                }
                case "NUTS_ID_VAR": {
                    NPath v = NWorkspace.of().getStoreLocation(definition.getId(), NStoreType.VAR);
                    return (v.toString());
                }
            }
            return null;
        }
    };

    public static StringPlaceHolderParser.PlaceHolderProvider<NExecutionContext> EXECUTION_CONTEXT_PLACEHOLDER=new StringPlaceHolderParser.PlaceHolderProvider<NExecutionContext>() {
        @Override
        public String get(String key, NExecutionContext executionContext) {
            return DEFINITION_PLACEHOLDER.get(key, executionContext.getDefinition());
        }
    };


}

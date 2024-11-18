package net.thevpc.nuts.runtime.standalone.executor;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;

public class NExecutionContextUtils {
    public static StringPlaceHolderParser.PlaceHolderProvider<NDefinition> DEFINITION_PLACEHOLDER=new StringPlaceHolderParser.PlaceHolderProvider<NDefinition>() {
        @Override
        public String get(String key, NDefinition definition, NSession session) {
            switch (key) {
                case "NUTS_ID": {
                    return (definition.getId().toString());
                }
                case "NUTS_ID_BIN": {
                    NPath v = NLocations.of().getStoreLocation(definition.getId(), NStoreType.BIN);
                    return (v.toString());
                }
                case "NUTS_ID_CONF": {
                    NPath v = NLocations.of().getStoreLocation(definition.getId(), NStoreType.CONF);
                    return (v.toString());
                }
                case "NUTS_ID_LOG": {
                    NPath v = NLocations.of().getStoreLocation(definition.getId(), NStoreType.LOG);
                    return (v.toString());
                }
                case "NUTS_ID_CACHE": {
                    NPath v = NLocations.of().getStoreLocation(definition.getId(), NStoreType.CACHE);
                    return (v.toString());
                }
                case "NUTS_ID_LIB": {
                    NPath v = NLocations.of().getStoreLocation(definition.getId(), NStoreType.LIB);
                    return (v.toString());
                }
                case "NUTS_ID_RUN": {
                    NPath v = NLocations.of().getStoreLocation(definition.getId(), NStoreType.RUN);
                    return (v.toString());
                }
                case "NUTS_ID_TEMP": {
                    NPath v = NLocations.of().getStoreLocation(definition.getId(), NStoreType.TEMP);
                    return (v.toString());
                }
                case "NUTS_ID_VAR": {
                    NPath v = NLocations.of().getStoreLocation(definition.getId(), NStoreType.VAR);
                    return (v.toString());
                }
            }
            return null;
        }
    };

    public static StringPlaceHolderParser.PlaceHolderProvider<NExecutionContext> EXECUTION_CONTEXT_PLACEHOLDER=new StringPlaceHolderParser.PlaceHolderProvider<NExecutionContext>() {
        @Override
        public String get(String key, NExecutionContext executionContext, NSession session) {
            return DEFINITION_PLACEHOLDER.get(key, executionContext.getDefinition(), session);
        }
    };


}

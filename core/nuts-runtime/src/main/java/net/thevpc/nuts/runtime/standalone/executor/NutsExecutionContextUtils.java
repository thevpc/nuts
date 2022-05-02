package net.thevpc.nuts.runtime.standalone.executor;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;

public class NutsExecutionContextUtils {
    public static StringPlaceHolderParser.PlaceHolderProvider<NutsDefinition> DEFINITION_PLACEHOLDER=new StringPlaceHolderParser.PlaceHolderProvider<NutsDefinition>() {
        @Override
        public String get(String key, NutsDefinition definition,NutsSession session) {
            switch (key) {
                case "NUTS_ID": {
                    return (definition.getId().toString());
                }
                case "NUTS_ID_APPS": {
                    NutsPath v = session.locations().getStoreLocation(definition.getId(), NutsStoreLocation.APPS);
                    return (v.toString());
                }
                case "NUTS_ID_CONFIG": {
                    NutsPath v = session.locations().getStoreLocation(definition.getId(), NutsStoreLocation.CONFIG);
                    return (v.toString());
                }
                case "NUTS_ID_LOG": {
                    NutsPath v = session.locations().getStoreLocation(definition.getId(), NutsStoreLocation.LOG);
                    return (v.toString());
                }
                case "NUTS_ID_CACHE": {
                    NutsPath v = session.locations().getStoreLocation(definition.getId(), NutsStoreLocation.CACHE);
                    return (v.toString());
                }
                case "NUTS_ID_LIB": {
                    NutsPath v = session.locations().getStoreLocation(definition.getId(), NutsStoreLocation.LIB);
                    return (v.toString());
                }
                case "NUTS_ID_RUN": {
                    NutsPath v = session.locations().getStoreLocation(definition.getId(), NutsStoreLocation.RUN);
                    return (v.toString());
                }
                case "NUTS_ID_TEMP": {
                    NutsPath v = session.locations().getStoreLocation(definition.getId(), NutsStoreLocation.TEMP);
                    return (v.toString());
                }
                case "NUTS_ID_VAR": {
                    NutsPath v = session.locations().getStoreLocation(definition.getId(), NutsStoreLocation.VAR);
                    return (v.toString());
                }
            }
            return null;
        }
    };

    public static StringPlaceHolderParser.PlaceHolderProvider<NutsExecutionContext> EXECUTION_CONTEXT_PLACEHOLDER=new StringPlaceHolderParser.PlaceHolderProvider<NutsExecutionContext>() {
        @Override
        public String get(String key, NutsExecutionContext executionContext,NutsSession session) {
            return DEFINITION_PLACEHOLDER.get(key, executionContext.getDefinition(), session);
        }
    };


}

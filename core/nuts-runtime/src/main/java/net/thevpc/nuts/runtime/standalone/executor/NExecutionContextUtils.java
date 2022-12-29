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
                case "NUTS_ID_APPS": {
                    NPath v = session.locations().getStoreLocation(definition.getId(), NStoreLocation.APPS);
                    return (v.toString());
                }
                case "NUTS_ID_CONFIG": {
                    NPath v = session.locations().getStoreLocation(definition.getId(), NStoreLocation.CONFIG);
                    return (v.toString());
                }
                case "NUTS_ID_LOG": {
                    NPath v = session.locations().getStoreLocation(definition.getId(), NStoreLocation.LOG);
                    return (v.toString());
                }
                case "NUTS_ID_CACHE": {
                    NPath v = session.locations().getStoreLocation(definition.getId(), NStoreLocation.CACHE);
                    return (v.toString());
                }
                case "NUTS_ID_LIB": {
                    NPath v = session.locations().getStoreLocation(definition.getId(), NStoreLocation.LIB);
                    return (v.toString());
                }
                case "NUTS_ID_RUN": {
                    NPath v = session.locations().getStoreLocation(definition.getId(), NStoreLocation.RUN);
                    return (v.toString());
                }
                case "NUTS_ID_TEMP": {
                    NPath v = session.locations().getStoreLocation(definition.getId(), NStoreLocation.TEMP);
                    return (v.toString());
                }
                case "NUTS_ID_VAR": {
                    NPath v = session.locations().getStoreLocation(definition.getId(), NStoreLocation.VAR);
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

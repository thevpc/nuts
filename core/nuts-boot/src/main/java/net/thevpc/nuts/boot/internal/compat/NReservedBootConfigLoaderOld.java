package net.thevpc.nuts.boot.internal.compat;

import net.thevpc.nuts.boot.NBootException;
import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NBootHomeLocation;
import net.thevpc.nuts.boot.internal.util.NBootLog;
import net.thevpc.nuts.boot.internal.util.NBootMsg;
import net.thevpc.nuts.boot.internal.util.NBootBootConfigLoader;
import net.thevpc.nuts.boot.internal.util.NBootPlatformHome;
import net.thevpc.nuts.boot.internal.util.NBootUtils;

import java.util.*;
import java.util.logging.Level;

public class NReservedBootConfigLoaderOld {

    /**
     * best effort to load config object from jsonObject saved with nuts version
     * "0.5.6" and later.
     *
     * @param config     config object to fill
     * @param jsonObject config JSON object
     */
    @SuppressWarnings("unchecked")
    public static void loadConfigVersion507(NBootOptionsInfo config, Map<String, Object> jsonObject,
                                            NBootLog bLog) {
        bLog.with().level(Level.CONFIG).verbNotice().log(NBootMsg.ofPlain("config version compatibility : 0.5.7"));
        config.uuid((String) jsonObject.get("uuid"));
        config.name((String) jsonObject.get("name"));
        config.workspace((String) jsonObject.get("workspace"));
        config.javaCommand((String) jsonObject.get("javaCommand"));
        config.javaOptions((String) jsonObject.get("javaOptions"));
        config.homeLocations(NBootBootConfigLoader.asNutsHomeLocationMap((Map<Object, String>) jsonObject.get("homeLocations")));
        config.storeLocations(NBootBootConfigLoader.asNutsStoreLocationMap((Map<Object, String>) jsonObject.get("storeLocations")));
        config.storeStrategy(((String) jsonObject.get("storeLocationStrategy")));
        config.storeLayout(((String) jsonObject.get("storeLocationLayout")));
        config.repositoryStoreStrategy(((String) jsonObject.get("repositoryStoreLocationStrategy")));
        if(jsonObject.get("bootRepositories") ==null) {
            config.bootRepositories(null);
        }else if(jsonObject.get("bootRepositories") instanceof List) {
            config.bootRepositories((List<String>) jsonObject.get("bootRepositories"));
        }else if(jsonObject.get("bootRepositories") instanceof String){
            config.bootRepositories(new ArrayList<>(Arrays.asList((String)jsonObject.get("bootRepositories"))));
        }
    }

    /**
     * best effort to load config object from jsonObject saved with nuts version
     * "[0.5.6]" and later.
     *
     * @param config     config object to fill
     * @param jsonObject config JSON object
     */
    @SuppressWarnings("unchecked")
    public static void loadConfigVersion506(NBootOptionsInfo config, Map<String, Object> jsonObject,
                                            NBootLog bLog) {
        bLog.with().level(Level.CONFIG).verbNotice().log(NBootMsg.ofPlain("config version compatibility : 0.5.6"));
        config.uuid((String) jsonObject.get("uuid"));
        config.name((String) jsonObject.get("name"));
        config.workspace((String) jsonObject.get("workspace"));
        config.apiVersion(((String) jsonObject.get("apiVersion")));
        config.runtimeId((String) jsonObject.get("runtimeId"));
        config.javaCommand((String) jsonObject.get("javaCommand"));
        config.javaOptions((String) jsonObject.get("javaOptions"));
        config.storeLocations(NBootBootConfigLoader.asNutsStoreLocationMap((Map<Object, String>) jsonObject.get("storeLocations")));
        config.homeLocations(NBootBootConfigLoader.asNutsHomeLocationMap((Map<Object, String>) jsonObject.get("homeLocations")));
        String s = (String) jsonObject.get("storeLocationStrategy");
        if (s != null && s.length() > 0) {
            config.storeStrategy(s.toUpperCase());
        }
        s = (String) jsonObject.get("repositoryStoreLocationStrategy");
        if (s != null && s.length() > 0) {
            config.repositoryStoreStrategy(s.toUpperCase());
        }
        s = (String) jsonObject.get("storeLocationLayout");
        if (s != null && s.length() > 0) {
            config.storeLayout(s.toUpperCase());
        }
    }

    /**
     * best effort to load config object from jsonObject saved with nuts version
     * "[0.5.2,0.5.6[".
     *
     * @param config     config object to fill
     * @param jsonObject config JSON object
     */
    public static void loadConfigVersion502(NBootOptionsInfo config, Map<String, Object> jsonObject,
                                            NBootLog bLog) {
        bLog.with().level(Level.CONFIG).verbNotice().log(NBootMsg.ofPlain("config version compatibility : 0.5.2"));
        config.uuid((String) jsonObject.get("uuid"));
        config.name((String) jsonObject.get("name"));
        config.workspace((String) jsonObject.get("workspace"));
        config.apiVersion(((String) jsonObject.get("bootApiVersion")));
        config.runtimeId((String) jsonObject.get("bootRuntime"));
        config.javaCommand((String) jsonObject.get("bootJavaCommand"));
        config.javaOptions((String) jsonObject.get("bootJavaOptions"));
        Map<String, String> storeLocations = new LinkedHashMap<>();
        Map<NBootHomeLocation, String> homeLocations = new LinkedHashMap<>();
        for (String folder : NBootPlatformHome.storeTypes()) {
            String folderName502 = folder;
            if (NBootUtils.sameEnum(folder, "BIN")) {
                folderName502 = "programs";
            }
            String k = folderName502.toLowerCase() + "StoreLocation";
            String v = (String) jsonObject.get(k);
            storeLocations.put(folder, v);

            k = folderName502.toLowerCase() + "SystemHome";
            v = (String) jsonObject.get(k);
            homeLocations.put(NBootHomeLocation.of(null, folder), v);
            for (String osFamily : NBootPlatformHome.osFamilies()) {
                switch (osFamily) {
                    case "MACOS": {
                        k = folderName502.toLowerCase() + "MacOs" + "Home";
                        break;
                    }
                    case "LINUX":
                    case "UNIX":
                    case "WINDOWS":
                    case "UNKNOWN": {
                        k = folderName502.toLowerCase() + NBootUtils.enumTitle(osFamily) + "Home";
                        break;
                    }
                    default: {
                        throw new NBootException(NBootMsg.ofC("unsupported os-family %s", osFamily));
                    }
                }
                v = (String) jsonObject.get(k);
                homeLocations.put(NBootHomeLocation.of(osFamily, folder), v);
            }
        }
        config.homeLocations(homeLocations);
        config.storeLocations(storeLocations);
        String s = (String) jsonObject.get("storeLocationStrategy");
        if (s != null && s.length() > 0) {
            config.storeStrategy(s.toUpperCase());
        }
        s = (String) jsonObject.get("repositoryStoreLocationStrategy");
        if (s != null && s.length() > 0) {
            config.repositoryStoreStrategy(s.toUpperCase());
        }
        s = (String) jsonObject.get("storeLocationLayout");
        if (s != null && s.length() > 0) {
            config.storeLayout(s.toUpperCase());
        }
    }
}

package net.thevpc.nuts.boot.reserved.compat;

import net.thevpc.nuts.boot.NBootException;
import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NBootHomeLocation;
import net.thevpc.nuts.boot.reserved.util.NBootLog;
import net.thevpc.nuts.boot.reserved.util.NBootMsg;
import net.thevpc.nuts.boot.reserved.util.NBootBootConfigLoader;
import net.thevpc.nuts.boot.reserved.util.NBootPlatformHome;
import net.thevpc.nuts.boot.reserved.util.NBootUtils;

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
        bLog.with().level(Level.CONFIG).verbInfo().log(NBootMsg.ofPlain("config version compatibility : 0.5.7"));
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspace((String) jsonObject.get("workspace"));
        config.setJavaCommand((String) jsonObject.get("javaCommand"));
        config.setJavaOptions((String) jsonObject.get("javaOptions"));
        config.setHomeLocations(NBootBootConfigLoader.asNutsHomeLocationMap((Map<Object, String>) jsonObject.get("homeLocations")));
        config.setStoreLocations(NBootBootConfigLoader.asNutsStoreLocationMap((Map<Object, String>) jsonObject.get("storeLocations")));
        config.setStoreStrategy(((String) jsonObject.get("storeLocationStrategy")));
        config.setStoreLayout(((String) jsonObject.get("storeLocationLayout")));
        config.setRepositoryStoreStrategy(((String) jsonObject.get("repositoryStoreLocationStrategy")));
        if(jsonObject.get("bootRepositories") ==null) {
            config.setBootRepositories(null);
        }else if(jsonObject.get("bootRepositories") instanceof List) {
            config.setBootRepositories((List<String>) jsonObject.get("bootRepositories"));
        }else if(jsonObject.get("bootRepositories") instanceof String){
            config.setBootRepositories(new ArrayList<>(Arrays.asList((String)jsonObject.get("bootRepositories"))));
        }

        List<Map<String, Object>> extensions = (List<Map<String, Object>>) jsonObject.get("extensions");
        if (extensions != null) {
            LinkedHashSet<String> extSet = new LinkedHashSet<>();
            for (Map<String, Object> extension : extensions) {
                String eid = (String) extension.get("id");
                Boolean enabled = (Boolean) extension.get("enabled");
                if (enabled != null && enabled) {
                    extSet.add(eid);
                }
            }
            config.setExtensionsSet(extSet);
        } else {
            config.setExtensionsSet(new HashSet<>());
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
        bLog.with().level(Level.CONFIG).verbInfo().log(NBootMsg.ofPlain("config version compatibility : 0.5.6"));
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspace((String) jsonObject.get("workspace"));
        config.setApiVersion(((String) jsonObject.get("apiVersion")));
        config.setRuntimeId((String) jsonObject.get("runtimeId"));
        config.setJavaCommand((String) jsonObject.get("javaCommand"));
        config.setJavaOptions((String) jsonObject.get("javaOptions"));
        config.setStoreLocations(NBootBootConfigLoader.asNutsStoreLocationMap((Map<Object, String>) jsonObject.get("storeLocations")));
        config.setHomeLocations(NBootBootConfigLoader.asNutsHomeLocationMap((Map<Object, String>) jsonObject.get("homeLocations")));
        String s = (String) jsonObject.get("storeLocationStrategy");
        if (s != null && s.length() > 0) {
            config.setStoreStrategy(s.toUpperCase());
        }
        s = (String) jsonObject.get("repositoryStoreLocationStrategy");
        if (s != null && s.length() > 0) {
            config.setRepositoryStoreStrategy(s.toUpperCase());
        }
        s = (String) jsonObject.get("storeLocationLayout");
        if (s != null && s.length() > 0) {
            config.setStoreLayout(s.toUpperCase());
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
        bLog.with().level(Level.CONFIG).verbInfo().log(NBootMsg.ofPlain("config version compatibility : 0.5.2"));
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspace((String) jsonObject.get("workspace"));
        config.setApiVersion(((String) jsonObject.get("bootApiVersion")));
        config.setRuntimeId((String) jsonObject.get("bootRuntime"));
        config.setJavaCommand((String) jsonObject.get("bootJavaCommand"));
        config.setJavaOptions((String) jsonObject.get("bootJavaOptions"));
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
        config.setHomeLocations(homeLocations);
        config.setStoreLocations(storeLocations);
        String s = (String) jsonObject.get("storeLocationStrategy");
        if (s != null && s.length() > 0) {
            config.setStoreStrategy(s.toUpperCase());
        }
        s = (String) jsonObject.get("repositoryStoreLocationStrategy");
        if (s != null && s.length() > 0) {
            config.setRepositoryStoreStrategy(s.toUpperCase());
        }
        s = (String) jsonObject.get("storeLocationLayout");
        if (s != null && s.length() > 0) {
            config.setStoreLayout(s.toUpperCase());
        }
    }
}

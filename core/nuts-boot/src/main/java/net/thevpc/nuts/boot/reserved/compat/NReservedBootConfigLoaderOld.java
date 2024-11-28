package net.thevpc.nuts.boot.reserved.compat;

import net.thevpc.nuts.boot.NBootException;
import net.thevpc.nuts.boot.NBootOptionsBoot;
import net.thevpc.nuts.boot.NHomeLocationBoot;
import net.thevpc.nuts.boot.reserved.NLogBoot;
import net.thevpc.nuts.boot.reserved.NMsgBoot;
import net.thevpc.nuts.boot.reserved.NReservedBootConfigLoader;
import net.thevpc.nuts.boot.reserved.util.NNameFormatBoot;
import net.thevpc.nuts.boot.reserved.util.NPlatformHomeBoot;
import net.thevpc.nuts.boot.reserved.util.NUtilsBoot;

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
    public static void loadConfigVersion507(NBootOptionsBoot config, Map<String, Object> jsonObject,
                                            NLogBoot bLog) {
        bLog.with().level(Level.CONFIG).verbInfo().log(NMsgBoot.ofPlain("config version compatibility : 0.5.7"));
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspace((String) jsonObject.get("workspace"));
        config.setJavaCommand((String) jsonObject.get("javaCommand"));
        config.setJavaOptions((String) jsonObject.get("javaOptions"));
        config.setHomeLocations(NReservedBootConfigLoader.asNutsHomeLocationMap((Map<Object, String>) jsonObject.get("homeLocations")));
        config.setStoreLocations(NReservedBootConfigLoader.asNutsStoreLocationMap((Map<Object, String>) jsonObject.get("storeLocations")));
        config.setStoreStrategy(((String) jsonObject.get("storeLocationStrategy")));
        config.setStoreLayout(((String) jsonObject.get("storeLocationLayout")));
        config.setRepositoryStoreStrategy(((String) jsonObject.get("repositoryStoreLocationStrategy")));
        config.setBootRepositories((String) jsonObject.get("bootRepositories"));

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
    public static void loadConfigVersion506(NBootOptionsBoot config, Map<String, Object> jsonObject,
                                            NLogBoot bLog) {
        bLog.with().level(Level.CONFIG).verbInfo().log(NMsgBoot.ofPlain("config version compatibility : 0.5.6"));
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspace((String) jsonObject.get("workspace"));
        config.setApiVersion(((String) jsonObject.get("apiVersion")));
        config.setRuntimeId((String) jsonObject.get("runtimeId"));
        config.setJavaCommand((String) jsonObject.get("javaCommand"));
        config.setJavaOptions((String) jsonObject.get("javaOptions"));
        config.setStoreLocations(NReservedBootConfigLoader.asNutsStoreLocationMap((Map<Object, String>) jsonObject.get("storeLocations")));
        config.setHomeLocations(NReservedBootConfigLoader.asNutsHomeLocationMap((Map<Object, String>) jsonObject.get("homeLocations")));
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
    public static void loadConfigVersion502(NBootOptionsBoot config, Map<String, Object> jsonObject,
                                            NLogBoot bLog) {
        bLog.with().level(Level.CONFIG).verbInfo().log(NMsgBoot.ofPlain("config version compatibility : 0.5.2"));
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspace((String) jsonObject.get("workspace"));
        config.setApiVersion(((String) jsonObject.get("bootApiVersion")));
        config.setRuntimeId((String) jsonObject.get("bootRuntime"));
        config.setJavaCommand((String) jsonObject.get("bootJavaCommand"));
        config.setJavaOptions((String) jsonObject.get("bootJavaOptions"));
        Map<String, String> storeLocations = new LinkedHashMap<>();
        Map<NHomeLocationBoot, String> homeLocations = new LinkedHashMap<>();
        for (String folder : NPlatformHomeBoot.storeTypes()) {
            String folderName502 = folder;
            if (NUtilsBoot.sameEnum(folder, "BIN")) {
                folderName502 = "programs";
            }
            String k = folderName502.toLowerCase() + "StoreLocation";
            String v = (String) jsonObject.get(k);
            storeLocations.put(folder, v);

            k = folderName502.toLowerCase() + "SystemHome";
            v = (String) jsonObject.get(k);
            homeLocations.put(NHomeLocationBoot.of(null, folder), v);
            for (String osFamily : NPlatformHomeBoot.osFamilies()) {
                switch (osFamily) {
                    case "MACOS": {
                        k = folderName502.toLowerCase() + "MacOs" + "Home";
                        break;
                    }
                    case "LINUX":
                    case "UNIX":
                    case "WINDOWS":
                    case "UNKNOWN": {
                        k = folderName502.toLowerCase() + NNameFormatBoot.TITLE_CASE.format(osFamily) + "Home";
                        break;
                    }
                    default: {
                        throw new NBootException(NMsgBoot.ofC("unsupported os-family %s", osFamily));
                    }
                }
                v = (String) jsonObject.get(k);
                homeLocations.put(NHomeLocationBoot.of(osFamily, folder), v);
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

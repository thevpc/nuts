package net.thevpc.nuts.reserved.compat;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NBootOptionsBuilder;
import net.thevpc.nuts.reserved.boot.NReservedBootConfigLoader;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.env.NOsFamily;

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
    public static void loadConfigVersion507(NBootOptionsBuilder config, Map<String, Object> jsonObject,
                                            NLog bLog) {
        bLog.with().level(Level.CONFIG).verb(NLogVerb.INFO).log( NMsg.ofPlain("config version compatibility : 0.5.7"));
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspace((String) jsonObject.get("workspace"));
        config.setJavaCommand((String) jsonObject.get("javaCommand"));
        config.setJavaOptions((String) jsonObject.get("javaOptions"));
        config.setHomeLocations(NReservedBootConfigLoader.asNutsHomeLocationMap((Map<Object, String>) jsonObject.get("homeLocations")));
        config.setStoreLocations(NReservedBootConfigLoader.asNutsStoreLocationMap((Map<Object, String>) jsonObject.get("storeLocations")));
        config.setStoreStrategy(NStoreStrategy.parse((String) jsonObject.get("storeLocationStrategy")).orNull());
        config.setStoreLayout(NOsFamily.parse((String) jsonObject.get("storeLocationLayout")).orNull());
        config.setRepositoryStoreStrategy(NStoreStrategy.parse((String) jsonObject.get("repositoryStoreLocationStrategy")).orNull());
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
    public static void loadConfigVersion506(NBootOptionsBuilder config, Map<String, Object> jsonObject,
                                            NLog bLog) {
        bLog.with().level(Level.CONFIG).verb(NLogVerb.INFO).log( NMsg.ofPlain("config version compatibility : 0.5.6"));
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspace((String) jsonObject.get("workspace"));
        config.setApiVersion(NVersion.of((String) jsonObject.get("apiVersion")).orNull());
        NId runtimeId = NId.of((String) jsonObject.get("runtimeId")).orNull();
        config.setRuntimeId(runtimeId);
        config.setJavaCommand((String) jsonObject.get("javaCommand"));
        config.setJavaOptions((String) jsonObject.get("javaOptions"));
        config.setStoreLocations(NReservedBootConfigLoader.asNutsStoreLocationMap((Map<Object, String>) jsonObject.get("storeLocations")));
        config.setHomeLocations(NReservedBootConfigLoader.asNutsHomeLocationMap((Map<Object, String>) jsonObject.get("homeLocations")));
        String s = (String) jsonObject.get("storeLocationStrategy");
        if (s != null && s.length() > 0) {
            config.setStoreStrategy(NStoreStrategy.valueOf(s.toUpperCase()));
        }
        s = (String) jsonObject.get("repositoryStoreLocationStrategy");
        if (s != null && s.length() > 0) {
            config.setRepositoryStoreStrategy(NStoreStrategy.valueOf(s.toUpperCase()));
        }
        s = (String) jsonObject.get("storeLocationLayout");
        if (s != null && s.length() > 0) {
            config.setStoreLayout(NOsFamily.valueOf(s.toUpperCase()));
        }
    }

    /**
     * best effort to load config object from jsonObject saved with nuts version
     * "[0.5.2,0.5.6[".
     *
     * @param config     config object to fill
     * @param jsonObject config JSON object
     */
    public static void loadConfigVersion502(NBootOptionsBuilder config, Map<String, Object> jsonObject,
                                            NLog bLog) {
        bLog.with().level(Level.CONFIG).verb(NLogVerb.INFO).log( NMsg.ofPlain("config version compatibility : 0.5.2"));
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspace((String) jsonObject.get("workspace"));
        config.setApiVersion(NVersion.of((String) jsonObject.get("bootApiVersion")).orNull());
        NId runtimeId = NId.of((String) jsonObject.get("bootRuntime")).orNull();
        config.setRuntimeId(runtimeId);
        config.setJavaCommand((String) jsonObject.get("bootJavaCommand"));
        config.setJavaOptions((String) jsonObject.get("bootJavaOptions"));
        Map<NStoreType, String> storeLocations = new LinkedHashMap<>();
        Map<NHomeLocation, String> homeLocations = new LinkedHashMap<>();
        for (NStoreType folder : NStoreType.values()) {
            String folderName502 = folder.name();
            if (folder == NStoreType.BIN) {
                folderName502 = "programs";
            }
            String k = folderName502.toLowerCase() + "StoreLocation";
            String v = (String) jsonObject.get(k);
            storeLocations.put(folder, v);

            k = folderName502.toLowerCase() + "SystemHome";
            v = (String) jsonObject.get(k);
            homeLocations.put(NHomeLocation.of(null, folder), v);
            for (NOsFamily osFamily : NOsFamily.values()) {
                switch (osFamily) {
                    case LINUX: {
                        k = folderName502.toLowerCase() + "LinuxHome";
                        break;
                    }
                    case UNIX: {
                        k = folderName502.toLowerCase() + "UnixHome";
                        break;
                    }
                    case MACOS: {
                        k = folderName502.toLowerCase() + "MacOsHome";
                        break;
                    }
                    case WINDOWS: {
                        k = folderName502.toLowerCase() + "WindowsHome";
                        break;
                    }
                    case UNKNOWN: {
                        k = folderName502.toLowerCase() + "UnknownHome";
                        break;
                    }
                    default: {
                        throw new NBootException(NMsg.ofC("unsupported os-family %s", osFamily));
                    }
                }
                v = (String) jsonObject.get(k);
                homeLocations.put(NHomeLocation.of(osFamily, folder), v);
            }
        }
        config.setHomeLocations(homeLocations);
        config.setStoreLocations(storeLocations);
        String s = (String) jsonObject.get("storeLocationStrategy");
        if (s != null && s.length() > 0) {
            config.setStoreStrategy(NStoreStrategy.valueOf(s.toUpperCase()));
        }
        s = (String) jsonObject.get("repositoryStoreLocationStrategy");
        if (s != null && s.length() > 0) {
            config.setRepositoryStoreStrategy(NStoreStrategy.valueOf(s.toUpperCase()));
        }
        s = (String) jsonObject.get("storeLocationLayout");
        if (s != null && s.length() > 0) {
            config.setStoreLayout(NOsFamily.valueOf(s.toUpperCase()));
        }
    }
}

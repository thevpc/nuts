/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.DefaultNutsWorkspaceBootOptionsBuilder;
import net.thevpc.nuts.boot.NutsWorkspaceBootOptionsBuilder;
import net.thevpc.nuts.util.NutsLoggerVerb;

import java.io.File;
import java.io.StringReader;
import java.util.*;
import java.util.logging.Level;

/**
 * JSON Config Best Effort Loader
 *
 * @author thevpc
 * @app.category Internal
 * @since 0.5.6
 */
public final class NutsReservedBootConfigLoader {

    public static NutsWorkspaceBootOptionsBuilder loadBootConfig(String workspaceLocation, NutsReservedBootLog bLog) {
        File bootFile = new File(workspaceLocation, NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        try {
            if (bootFile.isFile()) {
                bLog.log(Level.CONFIG, NutsLoggerVerb.READ, NutsMessage.ofJstyle("load boot file : {0}", bootFile.getPath()));
                String json = NutsReservedIOUtils.readStringFromFile(bootFile).trim();
                if (json.length() > 0) {
                    return loadBootConfigJSON(json, bLog);
                }
            }
            if (bLog.isLoggable(Level.FINEST)) {
                bLog.log(Level.CONFIG, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("previous Workspace config not found at {0}", bootFile.getPath()));
            }
        } catch (Exception ex) {
            bLog.log(Level.CONFIG, NutsMessage.ofJstyle("unable to load nuts version file {0}.\n", bootFile), ex);
        }
        return null;
    }

    private static NutsWorkspaceBootOptionsBuilder loadBootConfigJSON(String json, NutsReservedBootLog bLog) {
        NutsReservedJsonParser parser = new NutsReservedJsonParser(new StringReader(json));
        Map<String, Object> jsonObject = parser.parseObject();
        NutsWorkspaceBootOptionsBuilder c = new DefaultNutsWorkspaceBootOptionsBuilder();
        NutsVersion configVersion = NutsVersion.of((String) jsonObject.get("configVersion")).ifBlankNull()
                .orElseUse(()->NutsVersion.of((String) jsonObject.get("createApiVersion")))
                .orElse(NutsVersion.BLANK);

        if (configVersion.isBlank()) {
            configVersion = Nuts.getVersion();
            bLog.log(Level.FINEST, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("unable to detect config version. Fallback to {0}", configVersion));
        }
        if (configVersion == null) {
        }
        int buildNumber = getApiVersionOrdinalNumber(configVersion);
        if (buildNumber <= 501) {
            //load nothing!
            bLog.log(Level.CONFIG, NutsLoggerVerb.READ, NutsMessage.ofJstyle("detect config version {0} ( considered as 0.5.1, very old config, ignored)", configVersion));
        } else if (buildNumber <= 505) {
            bLog.log(Level.CONFIG, NutsLoggerVerb.READ, NutsMessage.ofJstyle("detect config version {0} ( compatible with 0.5.2 config file )", configVersion));
            loadConfigVersion502(c, jsonObject, bLog);
        } else if (buildNumber <= 506) {
            bLog.log(Level.CONFIG, NutsLoggerVerb.READ, NutsMessage.ofJstyle("detect config version {0} ( compatible with 0.5.6 config file )", configVersion));
            loadConfigVersion506(c, jsonObject, bLog);
        } else {
            bLog.log(Level.CONFIG, NutsLoggerVerb.READ, NutsMessage.ofJstyle("detect config version {0} ( compatible with 0.5.7 config file )", configVersion));
            loadConfigVersion507(c, jsonObject, bLog);
        }
        return c;
    }

    private static int getApiVersionOrdinalNumber(NutsVersion s) {
        try {
            int a = 0;
            for (String part : s.toString().split("\\.")) {
                a = a * 100 + NutsValue.of(part).asInt().orElse(0);
            }
            return a;
        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * best effort to load config object from jsonObject saved with nuts version
     * "0.5.6" and later.
     *
     * @param config     config object to fill
     * @param jsonObject config JSON object
     */
    @SuppressWarnings("unchecked")
    private static void loadConfigVersion507(NutsWorkspaceBootOptionsBuilder config, Map<String, Object> jsonObject,
                                             NutsReservedBootLog bLog) {
        bLog.log(Level.CONFIG, NutsLoggerVerb.INFO, NutsMessage.ofPlain("config version compatibility : 0.5.7"));
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspace((String) jsonObject.get("workspace"));
        config.setJavaCommand((String) jsonObject.get("javaCommand"));
        config.setJavaOptions((String) jsonObject.get("javaOptions"));
        config.setHomeLocations(asNutsHomeLocationMap((Map<Object, String>) jsonObject.get("homeLocations")));
        config.setStoreLocations(asNutsStoreLocationMap((Map<Object, String>) jsonObject.get("storeLocations")));
        config.setStoreLocationStrategy(NutsStoreLocationStrategy.parse((String) jsonObject.get("storeLocationStrategy")).orNull());
        config.setStoreLocationLayout(NutsOsFamily.parse((String) jsonObject.get("storeLocationLayout")).orNull());
        config.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.parse((String) jsonObject.get("repositoryStoreLocationStrategy")).orNull());
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

    private static Map<NutsHomeLocation, String> asNutsHomeLocationMap(Map<Object,String> m) {
        Map<NutsHomeLocation, String> a = new LinkedHashMap<>();
        if (m != null) {
            for (Map.Entry<Object, String> e : m.entrySet()) {
                Object k = e.getKey();
                NutsHomeLocation kk;
                if (k instanceof NutsHomeLocation) {
                    kk = (NutsHomeLocation) k;
                } else if (k == null) {
                    kk = NutsHomeLocation.of(null, null);
                } else {
                    kk = NutsHomeLocation.parse((String) k).orElse(NutsHomeLocation.of(null, null));
                }
                a.put(kk, e.getValue());
            }
        }
        return a;
    }

    private static Map<NutsStoreLocation, String> asNutsStoreLocationMap(Map<Object,String> m) {
        Map<NutsStoreLocation, String> a = new LinkedHashMap<>();
        if (m != null) {
            for (Map.Entry<Object, String> e : m.entrySet()) {
                Object k = e.getKey();
                NutsStoreLocation kk;
                if (k instanceof NutsStoreLocation) {
                    kk = (NutsStoreLocation) k;
                } else if (k == null) {
                    kk = null;
                } else {
                    kk = NutsStoreLocation.parse((String) k).orNull();
                }
                if (kk != null) {
                    a.put(kk, e.getValue());
                }
            }
        }
        return a;
    }


    /**
     * best effort to load config object from jsonObject saved with nuts version
     * "[0.5.6]" and later.
     *
     * @param config     config object to fill
     * @param jsonObject config JSON object
     */
    @SuppressWarnings("unchecked")
    private static void loadConfigVersion506(NutsWorkspaceBootOptionsBuilder config, Map<String, Object> jsonObject,
                                             NutsReservedBootLog bLog) {
        bLog.log(Level.CONFIG, NutsLoggerVerb.INFO, NutsMessage.ofPlain("config version compatibility : 0.5.6"));
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspace((String) jsonObject.get("workspace"));
        config.setApiVersion(NutsVersion.of((String) jsonObject.get("apiVersion")).orNull());
        NutsId runtimeId = NutsId.of((String) jsonObject.get("runtimeId")).orNull();
        config.setRuntimeId(runtimeId);
        config.setJavaCommand((String) jsonObject.get("javaCommand"));
        config.setJavaOptions((String) jsonObject.get("javaOptions"));
        config.setStoreLocations(asNutsStoreLocationMap((Map<Object, String>) jsonObject.get("storeLocations")));
        config.setHomeLocations(asNutsHomeLocationMap((Map<Object, String>) jsonObject.get("homeLocations")));
        String s = (String) jsonObject.get("storeLocationStrategy");
        if (s != null && s.length() > 0) {
            config.setStoreLocationStrategy(NutsStoreLocationStrategy.valueOf(s.toUpperCase()));
        }
        s = (String) jsonObject.get("repositoryStoreLocationStrategy");
        if (s != null && s.length() > 0) {
            config.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.valueOf(s.toUpperCase()));
        }
        s = (String) jsonObject.get("storeLocationLayout");
        if (s != null && s.length() > 0) {
            config.setStoreLocationLayout(NutsOsFamily.valueOf(s.toUpperCase()));
        }
    }

    /**
     * best effort to load config object from jsonObject saved with nuts version
     * "[0.5.2,0.5.6[".
     *
     * @param config     config object to fill
     * @param jsonObject config JSON object
     */
    private static void loadConfigVersion502(NutsWorkspaceBootOptionsBuilder config, Map<String, Object> jsonObject,
                                             NutsReservedBootLog bLog) {
        bLog.log(Level.CONFIG, NutsLoggerVerb.INFO, NutsMessage.ofPlain("config version compatibility : 0.5.2"));
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspace((String) jsonObject.get("workspace"));
        config.setApiVersion(NutsVersion.of((String) jsonObject.get("bootApiVersion")).orNull());
        NutsId runtimeId = NutsId.of((String) jsonObject.get("bootRuntime")).orNull();
        config.setRuntimeId(runtimeId);
        config.setJavaCommand((String) jsonObject.get("bootJavaCommand"));
        config.setJavaOptions((String) jsonObject.get("bootJavaOptions"));
        Map<NutsStoreLocation, String> storeLocations = new LinkedHashMap<>();
        Map<NutsHomeLocation, String> homeLocations = new LinkedHashMap<>();
        for (NutsStoreLocation folder : NutsStoreLocation.values()) {
            String folderName502 = folder.name();
            if (folder == NutsStoreLocation.APPS) {
                folderName502 = "programs";
            }
            String k = folderName502.toLowerCase() + "StoreLocation";
            String v = (String) jsonObject.get(k);
            storeLocations.put(folder, v);

            k = folderName502.toLowerCase() + "SystemHome";
            v = (String) jsonObject.get(k);
            homeLocations.put(NutsHomeLocation.of(null, folder), v);
            for (NutsOsFamily osFamily : NutsOsFamily.values()) {
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
                        throw new NutsBootException(NutsMessage.ofCstyle("unsupported os-family %s", osFamily));
                    }
                }
                v = (String) jsonObject.get(k);
                homeLocations.put(NutsHomeLocation.of(osFamily, folder), v);
            }
        }
        config.setHomeLocations(homeLocations);
        config.setStoreLocations(storeLocations);
        String s = (String) jsonObject.get("storeLocationStrategy");
        if (s != null && s.length() > 0) {
            config.setStoreLocationStrategy(NutsStoreLocationStrategy.valueOf(s.toUpperCase()));
        }
        s = (String) jsonObject.get("repositoryStoreLocationStrategy");
        if (s != null && s.length() > 0) {
            config.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.valueOf(s.toUpperCase()));
        }
        s = (String) jsonObject.get("storeLocationLayout");
        if (s != null && s.length() > 0) {
            config.setStoreLocationLayout(NutsOsFamily.valueOf(s.toUpperCase()));
        }
    }

}

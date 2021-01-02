/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts;

import java.io.File;
import java.io.StringReader;
import java.util.*;
import java.util.logging.Level;

/**
 * JSON Config Best Effort Loader
 *
 * @author thevpc
 * @since 0.5.6
 * @category Internal
 */
final class PrivateNutsBootConfigLoader {

    static PrivateNutsWorkspaceInitInformation loadBootConfig(String workspaceLocation,PrivateNutsLog LOG) {
        File versionFile = new File(workspaceLocation, NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        try {
            if (versionFile.isFile()) {
                LOG.log(Level.CONFIG, NutsLogVerb.READ, "loading boot file : {0}", versionFile.getPath());
                String json = PrivateNutsUtils.readStringFromFile(versionFile).trim();
                if (json.length() > 0) {
                    return loadBootConfigJSON(json,LOG);
                }
            }
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.CONFIG,NutsLogVerb.FAIL, "previous Workspace config not found at {0}", versionFile.getPath());
            }
        } catch (Exception ex) {
            LOG.log(Level.CONFIG, "unable to load nuts version file " + versionFile + ".\n", ex);
        }
        return null;
    }

    private static PrivateNutsWorkspaceInitInformation loadBootConfigJSON(String json,PrivateNutsLog LOG) {
        PrivateNutsJsonParser parser = new PrivateNutsJsonParser(new StringReader(json));
        Map<String, Object> jsonObject = parser.parseObject();
        PrivateNutsWorkspaceInitInformation c = new PrivateNutsWorkspaceInitInformation();
        String configVersion = (String) jsonObject.get("configVersion");

        if (configVersion == null) {
            configVersion = (String) jsonObject.get("createApiVersion");
        }
        if (configVersion == null) {
            configVersion = Nuts.getVersion();
            LOG.log(Level.FINEST,NutsLogVerb.FAIL,"unable to detect config version. Fallback to "+configVersion);
        }
        int buildNumber = getApiVersionOrdinalNumber(configVersion);
        if (buildNumber <= 501) {
            //load nothing!
            LOG.log(Level.CONFIG, NutsLogVerb.READ,"detected config version "+configVersion+" ( considered as 0.5.1, very old config, ignored)");
        }else if (buildNumber <= 505) {
            LOG.log(Level.CONFIG, NutsLogVerb.READ,"detected config version "+configVersion+" ( best effort to load 0.5.2 config file )");
            loadConfigVersion502(c, jsonObject,LOG);
        }else if (buildNumber <= 506) {
            LOG.log(Level.CONFIG, NutsLogVerb.READ,"detected config version "+configVersion+" ( best effort to load 0.5.6 config file )");
            loadConfigVersion506(c, jsonObject,LOG);
        } else {
            LOG.log(Level.CONFIG, NutsLogVerb.READ,"detected config version "+configVersion+" ( best effort to load 0.5.7 config file )");
            loadConfigVersion507(c, jsonObject,LOG);
        }
        return c;
    }

    private static int getApiVersionOrdinalNumber(String s) {
        try {
            int a = 0;
            for (String part : s.split("\\.")) {
                a = a * 100 + Integer.parseInt(part);
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
     * @param config config object to fill
     * @param jsonObject config JSON object
     */
    private static void loadConfigVersion507(PrivateNutsWorkspaceInitInformation config, Map<String, Object> jsonObject,PrivateNutsLog LOG) {
        LOG.log(Level.CONFIG, NutsLogVerb.READ,"effective load config version : 0.5.7");
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspaceLocation((String) jsonObject.get("workspace"));
        config.setJavaCommand((String) jsonObject.get("javaCommand"));
        config.setJavaOptions((String) jsonObject.get("javaOptions"));
        config.setStoreLocations((Map<String, String>) jsonObject.get("storeLocations"));
        config.setHomeLocations((Map<String, String>) jsonObject.get("homeLocations"));
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
        List<Map<String,Object>> extensions = (List<Map<String,Object>>) jsonObject.get("extensions");
        if(extensions!=null){
            LinkedHashSet<String> extSet=new LinkedHashSet<>();
            for (Map<String, Object> extension : extensions) {
                String eid = (String)extension.get("id");
                Boolean enabled = (Boolean)extension.get("enabled");
                if(enabled!=null && enabled){
                    extSet.add(eid);
                }
            }
            config.setExtensionsSet(extSet);
        }else {
            config.setExtensionsSet(new HashSet<>());
        }
    }


    /**
     * best effort to load config object from jsonObject saved with nuts version
     * "[0.5.6]" and later.
     *
     * @param config config object to fill
     * @param jsonObject config JSON object
     */
    private static void loadConfigVersion506(PrivateNutsWorkspaceInitInformation config, Map<String, Object> jsonObject,PrivateNutsLog LOG) {
        LOG.log(Level.CONFIG, NutsLogVerb.READ,"effective load config version : 0.5.6");
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspaceLocation((String) jsonObject.get("workspace"));
        config.setApiVersion((String) jsonObject.get("apiVersion"));
        config.setRuntimeId((String) jsonObject.get("runtimeId"));
        config.setJavaCommand((String) jsonObject.get("javaCommand"));
        config.setJavaOptions((String) jsonObject.get("javaOptions"));
        config.setStoreLocations((Map<String, String>) jsonObject.get("storeLocations"));
        config.setHomeLocations((Map<String, String>) jsonObject.get("homeLocations"));
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
     * @param config config object to fill
     * @param jsonObject config JSON object
     */
    private static void loadConfigVersion502(PrivateNutsWorkspaceInitInformation config, Map<String, Object> jsonObject,PrivateNutsLog LOG) {
        LOG.log(Level.CONFIG, NutsLogVerb.READ,"effective load config version : 0.5.2");
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspaceLocation((String) jsonObject.get("workspace"));
        config.setApiVersion((String) jsonObject.get("bootApiVersion"));
        config.setRuntimeId((String) jsonObject.get("bootRuntime"));
        config.setJavaCommand((String) jsonObject.get("bootJavaCommand"));
        config.setJavaOptions((String) jsonObject.get("bootJavaOptions"));
        Map<String, String> storeLocations = new LinkedHashMap<>();
        Map<String, String> homeLocations = new LinkedHashMap<>();
        for (NutsStoreLocation folder : NutsStoreLocation.values()) {
            String folderName502 = folder.name();
            if (folder == NutsStoreLocation.APPS) {
                folderName502 = "programs";
            }
            String k = folderName502.toLowerCase() + "StoreLocation";
            String v = (String) jsonObject.get(k);
            storeLocations.put(folder.id(), v);

            k = folderName502.toLowerCase() + "SystemHome";
            v = (String) jsonObject.get(k);
            homeLocations.put(PrivateBootWorkspaceOptions.createHomeLocationKey(null, folder), v);
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
                        throw new NutsBootException("unsupported os-family " + osFamily);
                    }
                }
                v = (String) jsonObject.get(k);
                homeLocations.put(PrivateBootWorkspaceOptions.createHomeLocationKey(osFamily, folder), v);
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

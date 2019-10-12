/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2019 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.File;
import java.io.StringReader;
import java.util.*;
import java.util.logging.Level;

/**
 * JSON Config Best Effort Loader
 *
 * @author vpc
 * @since 0.5.6
 */
final class PrivateNutsBootConfigLoader {

    static PrivateNutsWorkspaceInitInformation loadBootConfig(String workspaceLocation,PrivateNutsLog LOG) {
        File versionFile = new File(workspaceLocation, NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        try {
            if (versionFile.isFile()) {
                LOG.log(Level.CONFIG, PrivateNutsLog.READ, "loading boot file : {0}", versionFile.getPath());
                String json = PrivateNutsUtils.readStringFromFile(versionFile).trim();
                if (json.length() > 0) {
                    return loadBootConfigJSON(json,LOG);
                }
            }
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.CONFIG,PrivateNutsLog.FAIL, "previous Workspace config not found at {0}", versionFile.getPath());
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
            LOG.log(Level.FINEST,PrivateNutsLog.FAIL,"unable to detect config version. Fallback to "+configVersion);
        }
        int buildNumber = getApiVersionOrdinalNumber(configVersion);
        if (buildNumber <= 501) {
            //load nothing!
            LOG.log(Level.CONFIG, PrivateNutsLog.READ,"detected config version "+configVersion+" ( considered as 0.5.1, very old config, ignored)");
        }else if (buildNumber <= 505) {
            LOG.log(Level.CONFIG, PrivateNutsLog.READ,"detected config version "+configVersion+" ( best effort to load 0.5.2 config file )");
            loadConfigVersion502(c, jsonObject,LOG);
        }else if (buildNumber <= 506) {
            LOG.log(Level.CONFIG, PrivateNutsLog.READ,"detected config version "+configVersion+" ( best effort to load 0.5.6 config file )");
            loadConfigVersion506(c, jsonObject,LOG);
        } else {
            LOG.log(Level.CONFIG, PrivateNutsLog.READ,"detected config version "+configVersion+" ( best effort to load 0.5.7 config file )");
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
        LOG.log(Level.CONFIG, PrivateNutsLog.READ,"effective load config version : 0.5.7");
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
        LOG.log(Level.CONFIG, PrivateNutsLog.READ,"effective load config version : 0.5.6");
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
        LOG.log(Level.CONFIG, PrivateNutsLog.READ,"effective load config version : 0.5.2");
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
            homeLocations.put(NutsDefaultWorkspaceOptions.createHomeLocationKey(null, folder), v);
            for (NutsOsFamily layout : NutsOsFamily.values()) {
                switch (layout) {
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
                        throw new IllegalArgumentException("Unsupported " + layout);
                    }
                }
                v = (String) jsonObject.get(k);
                homeLocations.put(NutsDefaultWorkspaceOptions.createHomeLocationKey(layout, folder), v);
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

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

import java.io.StringReader;
import java.util.List;
import java.util.Map;

/**
 * JSON Config Best Effort Loader
 *
 * @author vpc
 * @since 0.5.6
 */
class NutsBootConfigLoaderLimited {

    static NutsBootConfig loadBootConfigJSON(String json) {
        NutsJsonParserLimited parser = new NutsJsonParserLimited(new StringReader(json));
        Map<String, Object> jsonObject = parser.parseObject();
        NutsBootConfig c = new NutsBootConfig();
        String createApiVersion = (String) jsonObject.get("createApiVersion");
        if (createApiVersion == null) {
            createApiVersion = "0.5.6";
        }
        int buildNumber = getNutsApiVersionOrdinalNumber(createApiVersion);
        if (buildNumber < 506) {
            loadConfigVersion502(c, jsonObject);
        } else {
            loadConfigVersion506(c, jsonObject);
        }
        return c;
    }

    private static int getNutsApiVersionOrdinalNumber(String s) {
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
    private static void loadConfigVersion506(NutsBootConfig config, Map<String, Object> jsonObject) {
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspace((String) jsonObject.get("workspace"));
        config.setApiVersion((String) jsonObject.get("bootApiVersion"));
        config.setRuntimeId((String) jsonObject.get("bootRuntime"));
        config.setRepositories((String) jsonObject.get("bootRepositories"));
        config.setRuntimeDependencies((String) jsonObject.get("bootRuntimeDependencies"));
        config.setJavaCommand((String) jsonObject.get("bootJavaCommand"));
        config.setJavaOptions((String) jsonObject.get("bootJavaOptions"));
        List<String> sl = (List<String>) jsonObject.get("storeLocations");
        if (sl != null) {
            config.setStoreLocations(sl.toArray(new String[0]));
        }
        List<String> hl = (List<String>) jsonObject.get("homeLocations");
        if (hl != null) {
            config.setHomeLocations(hl.toArray(new String[0]));
        }
        List<String> dhl = (List<String>) jsonObject.get("defaultHomeLocations");
        if (dhl != null) {
            config.setDefaultHomeLocations(dhl.toArray(new String[0]));
        }
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
    private static void loadConfigVersion502(NutsBootConfig config, Map<String, Object> jsonObject) {
        config.setUuid((String) jsonObject.get("uuid"));
        config.setName((String) jsonObject.get("name"));
        config.setWorkspace((String) jsonObject.get("workspace"));
        config.setApiVersion((String) jsonObject.get("bootApiVersion"));
        config.setRuntimeId((String) jsonObject.get("bootRuntime"));
        config.setRepositories((String) jsonObject.get("bootRepositories"));
        config.setRuntimeDependencies((String) jsonObject.get("bootRuntimeDependencies"));
        config.setJavaCommand((String) jsonObject.get("bootJavaCommand"));
        config.setJavaOptions((String) jsonObject.get("bootJavaOptions"));
        for (NutsStoreLocation folder : NutsStoreLocation.values()) {
            String folderName502 = folder.name();
            if(folder==NutsStoreLocation.APPS){
                folderName502="programs";
            }
            String k = folderName502.toLowerCase() + "StoreLocation";
            String v = (String) jsonObject.get(k);
            config.setStoreLocation(folder, v);

            k = folderName502.toLowerCase() + "SystemHome";
            v = (String) jsonObject.get(k);
            config.setHomeLocation(null, folder, v);
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
                config.setHomeLocation(layout, folder, v);
            }
        }
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

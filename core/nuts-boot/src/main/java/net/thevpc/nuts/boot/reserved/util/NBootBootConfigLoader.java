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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot.reserved.util;

import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NBootWorkspace;
import net.thevpc.nuts.boot.reserved.compat.NReservedBootConfigLoaderOld;
import net.thevpc.nuts.boot.NBootHomeLocation;

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
public final class NBootBootConfigLoader {

    public static NBootOptionsInfo loadBootConfig(String workspaceLocation, NBootLog bLog) {
        File bootFile = new File(workspaceLocation, NBootConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        try {
            if (bootFile.isFile()) {
                bLog.with().level(Level.CONFIG).verbRead().log(NBootMsg.ofC("load boot file : %s", bootFile.getPath()));
                String json = NBootUtils.readStringFromFile(bootFile).trim();
                if (json.length() > 0) {
                    return loadBootConfigJSON(json, bLog);
                }
            }
            if (bLog.isLoggable(Level.FINEST)) {
                bLog.with().level(Level.CONFIG).verbInfo().log(NBootMsg.ofC("previous Workspace config not found at %s", bootFile.getPath()));
            }
        } catch (Exception ex) {
            bLog.with().level(Level.CONFIG).verbFail().error(ex).log(NBootMsg.ofC("unable to load nuts version file %s", bootFile));
        }
        return null;
    }

    private static NBootOptionsInfo loadBootConfigJSON(String json, NBootLog bLog) {
        NBootJsonParser parser = new NBootJsonParser(new StringReader(json));
        Map<String, Object> jsonObject = parser.parseObject();
        NBootOptionsInfo c = new NBootOptionsInfo();
        String configVersion = ((String) jsonObject.get("configVersion"));
        if (NBootUtils.isBlank(configVersion)) {
            configVersion = ((String) jsonObject.get("createApiVersion"));
        }
        if (NBootUtils.isBlank(configVersion)) {
            configVersion = "";
        }

        if (NBootUtils.isBlank(configVersion)) {
            configVersion = NBootWorkspace.NUTS_BOOT_VERSION;
            bLog.with().level(Level.FINEST).verbFail().log(NBootMsg.ofC("unable to detect config version. Fallback to %s", configVersion));
        }
        int buildNumber = getApiVersionOrdinalNumber(configVersion);
        if (buildNumber <= 501) {
            //load nothing!
            bLog.with().level(Level.CONFIG).verbRead().log(NBootMsg.ofC("detect config version %s ( considered as 0.5.1, very old config, ignored)", configVersion));
        } else if (buildNumber <= 505) {
            bLog.with().level(Level.CONFIG).verbRead().log(NBootMsg.ofC("detect config version %s ( compatible with 0.5.2 config file )", configVersion));
            NReservedBootConfigLoaderOld.loadConfigVersion502(c, jsonObject, bLog);
        } else if (buildNumber <= 506) {
            bLog.with().level(Level.CONFIG).verbRead().log(NBootMsg.ofC("detect config version %s ( compatible with 0.5.6 config file )", configVersion));
            NReservedBootConfigLoaderOld.loadConfigVersion506(c, jsonObject, bLog);
        } else {
            bLog.with().level(Level.CONFIG).verbRead().log(NBootMsg.ofC("detect config version %s ( compatible with 0.5.7 config file )", configVersion));
            NReservedBootConfigLoaderOld.loadConfigVersion507(c, jsonObject, bLog);
        }
        return c;
    }

    private static int getApiVersionOrdinalNumber(String s) {
        try {
            int a = 0;
            for (String part : s.split("\\.")) {
                a = a * 100 + NBootUtils.firstNonNull(NBootUtils.parseInt(part), 0);
            }
            return a;
        } catch (Exception ex) {
            return -1;
        }
    }

    public static Map<NBootHomeLocation, String> asNutsHomeLocationMap(Map<Object, String> m) {
        Map<NBootHomeLocation, String> a = new LinkedHashMap<>();
        if (m != null) {
            for (Map.Entry<Object, String> e : m.entrySet()) {
                Object k = e.getKey();
                NBootHomeLocation kk;
                if (k instanceof NBootHomeLocation) {
                    kk = (NBootHomeLocation) k;
                } else if (k == null) {
                    kk = NBootHomeLocation.of(null, null);
                } else {
                    kk = NBootUtils.firstNonNull(NBootHomeLocation.parse((String) k), NBootHomeLocation.of(null, null));
                }
                a.put(kk, e.getValue());
            }
        }
        return a;
    }

    public static Map<String, String> asNutsStoreLocationMap(Map<Object, String> m) {
        Map<String, String> a = new LinkedHashMap<>();
        if (m != null) {
            for (Map.Entry<Object, String> e : m.entrySet()) {
                Object k = e.getKey();
                String kk = NBootUtils.enumId((String) k);
                if (kk != null) {
                    a.put(kk, e.getValue());
                }
            }
        }
        return a;
    }


}

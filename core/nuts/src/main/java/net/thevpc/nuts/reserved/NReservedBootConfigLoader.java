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
import net.thevpc.nuts.boot.DefaultNBootOptionsBuilder;
import net.thevpc.nuts.boot.NBootOptionsBuilder;
import net.thevpc.nuts.reserved.compat.NReservedBootConfigLoaderOld;
import net.thevpc.nuts.util.NLog;
import net.thevpc.nuts.util.NLogVerb;

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
public final class NReservedBootConfigLoader {

    public static NBootOptionsBuilder loadBootConfig(String workspaceLocation, NLog bLog) {
        File bootFile = new File(workspaceLocation, NConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        try {
            if (bootFile.isFile()) {
                bLog.with().level(Level.CONFIG).verb(NLogVerb.READ).log( NMsg.ofJ("load boot file : {0}", bootFile.getPath()));
                String json = NReservedIOUtils.readStringFromFile(bootFile).trim();
                if (json.length() > 0) {
                    return loadBootConfigJSON(json, bLog);
                }
            }
            if (bLog.isLoggable(Level.FINEST)) {
                bLog.with().level(Level.CONFIG).verb(NLogVerb.FAIL).log(NMsg.ofJ("previous Workspace config not found at {0}", bootFile.getPath()));
            }
        } catch (Exception ex) {
            bLog.with().level(Level.CONFIG).verb(NLogVerb.FAIL).error(ex).log(NMsg.ofJ("unable to load nuts version file {0}.\n", bootFile));
        }
        return null;
    }

    private static NBootOptionsBuilder loadBootConfigJSON(String json, NLog bLog) {
        NReservedJsonParser parser = new NReservedJsonParser(new StringReader(json));
        Map<String, Object> jsonObject = parser.parseObject();
        NBootOptionsBuilder c = new DefaultNBootOptionsBuilder();
        NVersion configVersion = NVersion.of((String) jsonObject.get("configVersion")).ifBlankEmpty()
                .orElseUse(()-> NVersion.of((String) jsonObject.get("createApiVersion")))
                .orElse(NVersion.BLANK);

        if (configVersion.isBlank()) {
            configVersion = Nuts.getVersion();
            bLog.with().level(Level.FINEST).verb(NLogVerb.FAIL).log(NMsg.ofJ("unable to detect config version. Fallback to {0}", configVersion));
        }
        if (configVersion == null) {
        }
        int buildNumber = getApiVersionOrdinalNumber(configVersion);
        if (buildNumber <= 501) {
            //load nothing!
            bLog.with().level(Level.CONFIG).verb(NLogVerb.READ).log( NMsg.ofJ("detect config version {0} ( considered as 0.5.1, very old config, ignored)", configVersion));
        } else if (buildNumber <= 505) {
            bLog.with().level(Level.CONFIG).verb(NLogVerb.READ).log(NMsg.ofJ("detect config version {0} ( compatible with 0.5.2 config file )", configVersion));
            NReservedBootConfigLoaderOld.loadConfigVersion502(c, jsonObject, bLog);
        } else if (buildNumber <= 506) {
            bLog.with().level(Level.CONFIG).verb(NLogVerb.READ).log(NMsg.ofJ("detect config version {0} ( compatible with 0.5.6 config file )", configVersion));
            NReservedBootConfigLoaderOld.loadConfigVersion506(c, jsonObject, bLog);
        } else {
            bLog.with().level(Level.CONFIG).verb(NLogVerb.READ).log(NMsg.ofJ("detect config version {0} ( compatible with 0.5.7 config file )", configVersion));
            NReservedBootConfigLoaderOld.loadConfigVersion507(c, jsonObject, bLog);
        }
        return c;
    }

    private static int getApiVersionOrdinalNumber(NVersion s) {
        try {
            int a = 0;
            for (String part : s.toString().split("\\.")) {
                a = a * 100 + NLiteral.of(part).asInt().orElse(0);
            }
            return a;
        } catch (Exception ex) {
            return -1;
        }
    }

    public static Map<NHomeLocation, String> asNutsHomeLocationMap(Map<Object,String> m) {
        Map<NHomeLocation, String> a = new LinkedHashMap<>();
        if (m != null) {
            for (Map.Entry<Object, String> e : m.entrySet()) {
                Object k = e.getKey();
                NHomeLocation kk;
                if (k instanceof NHomeLocation) {
                    kk = (NHomeLocation) k;
                } else if (k == null) {
                    kk = NHomeLocation.of(null, null);
                } else {
                    kk = NHomeLocation.parse((String) k).orElse(NHomeLocation.of(null, null));
                }
                a.put(kk, e.getValue());
            }
        }
        return a;
    }

    public static Map<NStoreType, String> asNutsStoreLocationMap(Map<Object,String> m) {
        Map<NStoreType, String> a = new LinkedHashMap<>();
        if (m != null) {
            for (Map.Entry<Object, String> e : m.entrySet()) {
                Object k = e.getKey();
                NStoreType kk;
                if (k instanceof NStoreType) {
                    kk = (NStoreType) k;
                } else if (k == null) {
                    kk = null;
                } else {
                    kk = NStoreType.parse((String) k).orNull();
                }
                if (kk != null) {
                    a.put(kk, e.getValue());
                }
            }
        }
        return a;
    }


}

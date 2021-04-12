/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.config;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import net.thevpc.nuts.NutsArchFamily;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsOsFamily;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsWorkspaceInitInformation;
import net.thevpc.nuts.runtime.bundles.common.CorePlatformUtils;
import net.thevpc.nuts.runtime.core.app.DefaultNutsArgument;
import net.thevpc.nuts.runtime.core.common.DefaultObservableMap;
import net.thevpc.nuts.runtime.core.common.ObservableMap;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.core.model.DefaultNutsVersion;
import net.thevpc.nuts.runtime.core.parser.DefaultNutsIdParser;
import net.thevpc.nuts.runtime.core.util.CoreBooleanUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsJavaSdkUtils;

/**
 *
 * @author vpc
 */
public class DefaultWorkspaceEnvManagerModel {

    private NutsWorkspace workspace;
    private Map<String, String> options = new LinkedHashMap<>();
    protected ObservableMap<String, Object> userProperties;
    private NutsId platform;
    private NutsId os;
    private NutsOsFamily osFamily;
    private NutsId arch;
    private NutsId osdist;
    private NutsArchFamily archFamily = NutsArchFamily.getArchFamily();

    public DefaultWorkspaceEnvManagerModel(NutsWorkspace ws, NutsWorkspaceInitInformation info, NutsSession session) {
        this.workspace = ws;
        userProperties = new DefaultObservableMap<>();
        String[] properties = info.getOptions().getProperties();
        if (properties != null) {
            for (String property : properties) {
                if (property != null) {
                    DefaultNutsArgument a = new DefaultNutsArgument(property);
                    String key = a.getStringKey();
                    String value = a.getStringValue();
                    this.options.put(key, value == null ? "" : value);
                }
            }
        }
        DefaultNutsIdParser nip = new DefaultNutsIdParser(session);
        os = nip.parse(CorePlatformUtils.getPlatformOs(workspace));
        String platformOsDist = CorePlatformUtils.getPlatformOsDist(workspace);
        if (platformOsDist == null) {
            platformOsDist = "default";
        }
        osdist = nip.parse(platformOsDist);
        platform = NutsJavaSdkUtils.of(session).createJdkId(System.getProperty("java.version"), session);
        arch = session.getWorkspace().id().parser().parse(System.getProperty("os.arch"));

    }

    NutsWorkspaceConfigMain getStoreModelMain() {
        return ((DefaultNutsWorkspaceConfigManager) workspace.config())
                .getModel()
                .getStoreModelMain();
    }

    public Map<String, String> getEnvMap() {
        Map<String, String> p = new LinkedHashMap<>();
        if (getStoreModelMain().getEnv() != null) {
            p.putAll(getStoreModelMain().getEnv());
        }
        p.putAll(options);
        return p;
    }

    public String getOption(String property) {
        return getOption(property, null);
    }

    public String getEnv(String property) {
        return getEnv(property, null);
    }

    public Integer getEnvAsInt(String property, Integer defaultValue) {
        String t = getEnv(property);
        try {
            return Integer.parseInt(t);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public Integer getOptionAsInt(String property, Integer defaultValue) {
        String t = getOption(property);
        try {
            return Integer.parseInt(t);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public Boolean getEnvAsBoolean(String property, Boolean defaultValue) {
        String t = getEnv(property);
        if (t != null) {
            try {
                return CoreBooleanUtils.parseBoolean(t, false, false);
            } catch (Exception ex) {
                //
            }
        }
        return defaultValue;
    }

    public Boolean getOptionAsBoolean(String property, Boolean defaultValue) {
        String t = getOption(property);
        if (t != null) {
            try {
                return CoreBooleanUtils.parseBoolean(t, true, false);
            } catch (Exception ex) {
                //
            }
        }
        return defaultValue;
    }

    public String getOption(String property, String defaultValue) {
        if (options.containsKey(property)) {
            return options.get(property);
        }
        return defaultValue;
    }

    public String getEnv(String property, String defaultValue) {
        if (options.containsKey(property)) {
            return options.get(property);
        }
        Map<String, String> env = getStoreModelMain().getEnv();
        if (env == null) {
            return defaultValue;
        }
        String o = env.get(property);
        if (CoreStringUtils.isBlank(o)) {
            return defaultValue;
        }
        return o;
    }

    public void setEnv(String property, String value, NutsSession session) {
        Map<String, String> env = getStoreModelMain().getEnv();
//        session = CoreNutsUtils.validate(session, workspace);
        if (CoreStringUtils.isBlank(value)) {
            if (env != null && env.containsKey(property)) {
                env.remove(property);
                NutsWorkspaceConfigManagerExt.of(workspace.config())
                        .getModel()
                        .fireConfigurationChanged("env", session, ConfigEventType.MAIN);
            }
        } else {
            if (env == null) {
                env = new LinkedHashMap<>();
                getStoreModelMain().setEnv(env);
            }
            String old = env.get(property);
            if (!value.equals(old)) {
                env.put(property, value);
                NutsWorkspaceConfigManagerExt.of(workspace.config())
                        .getModel()
                        .fireConfigurationChanged("env", session, ConfigEventType.MAIN);
            }
        }
    }
//
//    private DefaultNutsWorkspaceCurrentConfig current() {
//        return NutsWorkspaceConfigManagerExt.of(workspace.config())
//                .getModel()
//                .current();
//    }

//    @Override
//    public NutsOsFamily getOsFamily() {
//        return current().getOsFamily();
//    }
//    @Override
//    public NutsId getPlatform() {
//        return current().getPlatform();
//    }
//
//    @Override
//    public NutsId getOs() {
//        return current().getOs();
//    }
//
//    @Override
//    public NutsId getOsDist() {
//        return current().getOsDist();
//    }
//    @Override
//    public NutsId getArch() {
//        return current().getArch();
//    }
    private static NutsOsFamily getPlatformOsFamily0() {
        String property = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (property.startsWith("linux")) {
            return NutsOsFamily.LINUX;
        }
        if (property.startsWith("win")) {
            return NutsOsFamily.WINDOWS;
        }
        if (property.startsWith("mac")) {
            return NutsOsFamily.MACOS;
        }
        if (property.startsWith("sunos")) {
            return NutsOsFamily.UNIX;
        }
        if (property.startsWith("freebsd")) {
            return NutsOsFamily.UNIX;
        }
        return NutsOsFamily.UNKNOWN;
    }

    public NutsId getArch() {
        return arch;
    }

    public NutsArchFamily getArchFamily() {
        return archFamily;
    }

    public NutsOsFamily getOsFamily() {
        if (osFamily == null) {
            osFamily = getPlatformOsFamily0();
        }
        return osFamily;
    }

    public NutsId getOs() {
        return os;
    }

    public NutsId getPlatform(NutsSession session) {
//        if (platform == null) {
//            platform = NutsWorkspaceConfigManagerExt.of(workspace.config())
//                    .getModel()
//                    .createSdkId("java", System.getProperty("java.version"), session);
//        }
        return platform;
    }

    public NutsId getOsDist() {
        return osdist;
    }

    public Map<String, Object> getProperties() {
        return userProperties;
    }

    public Object getProperty(String property, Object defaultValue) {
        Object v = userProperties.get(property);
        if (v != null) {
            return v;
        }
        return defaultValue;
    }

    public Integer getPropertyAsInt(String property, Integer defaultValue) {
        Object t = getProperty(property, null);
        try {
            if (t instanceof Number) {
                return ((Number) t).intValue();
            }
            if (t instanceof CharSequence) {
                return Integer.parseInt(t.toString());
            }
        } catch (Exception ex) {
            //
        }
        return defaultValue;
    }

    public String getPropertyAsString(String property, String defaultValue) {
        Object t = getProperty(property, null);
        if (t != null) {
            return t.toString();
        }
        return defaultValue;
    }

    public Boolean getPropertyAsBoolean(String property, Boolean defaultValue) {
        Object t = getProperty(property, null);
        try {
            if (t instanceof Boolean) {
                return ((Boolean) t).booleanValue();
            }
            if (t instanceof Number) {
                return ((Number) t).doubleValue() != 0;
            }
            if (t instanceof CharSequence) {
                return CoreBooleanUtils.parseBoolean(t.toString(), false, false);
            }
        } catch (Exception ex) {
            //
        }
        return defaultValue;
    }

    public Object getProperty(String property) {
        return getProperty(property, null);
    }

    public <T> T getOrCreateProperty(Class<T> property, Supplier<T> supplier) {
        return getOrCreateProperty(property.getName(), supplier);
    }

    public <T> T getOrCreateProperty(String property, Supplier<T> supplier) {
        T o = (T) getProperty(property);
        if (o != null) {
            return o;
        }
        o = supplier.get();
        setProperty(property, o);
        return o;
    }

    public void setProperty(String property, Object value) {
        if (value == null) {
            userProperties.remove(property);
        } else {
            userProperties.put(property, value);
        }
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

}

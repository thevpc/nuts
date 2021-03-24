package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.app.DefaultNutsArgument;
import net.thevpc.nuts.runtime.core.common.DefaultObservableMap;
import net.thevpc.nuts.runtime.core.common.ObservableMap;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.bundles.common.CorePlatformUtils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import net.thevpc.nuts.runtime.core.util.CoreBooleanUtils;

public class DefaultWorkspaceEnvManager implements NutsWorkspaceEnvManager {

    private NutsWorkspace ws;
    private Map<String, String> options = new LinkedHashMap<>();
    protected ObservableMap<String, Object> userProperties;
    private NutsId platform;
    private NutsId os;
    private NutsOsFamily osFamily;
    private NutsId arch;
    private NutsId osdist;
    private NutsArchFamily archFamily = NutsArchFamily.getArchFamily();

    public DefaultWorkspaceEnvManager(NutsWorkspace ws, NutsWorkspaceOptions options) {
        this.ws = ws;
        userProperties = new DefaultObservableMap<>();
        String[] properties = options.getProperties();
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
    }

    NutsWorkspaceConfigMain getStoreModelMain() {
        return ((DefaultNutsWorkspaceConfigManager) ws.config()).getStoreModelMain();
    }

    @Override
    public Map<String, String> getEnvMap() {
        Map<String, String> p = new LinkedHashMap<>();
        if (getStoreModelMain().getEnv() != null) {
            p.putAll(getStoreModelMain().getEnv());
        }
        p.putAll(options);
        return p;
    }

    @Override
    public String getOption(String property) {
        return getOption(property, null);
    }

    @Override
    public String getEnv(String property) {
        return getEnv(property, null);
    }

    @Override
    public Integer getEnvAsInt(String property, Integer defaultValue) {
        String t = getEnv(property);
        try {
            return Integer.parseInt(t);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public Integer getOptionAsInt(String property, Integer defaultValue) {
        String t = getOption(property);
        try {
            return Integer.parseInt(t);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
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

    @Override
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

    @Override
    public String getOption(String property, String defaultValue) {
        if (options.containsKey(property)) {
            return options.get(property);
        }
        return defaultValue;
    }

    @Override
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

    @Override
    public NutsWorkspaceEnvManager setEnv(String property, String value, NutsUpdateOptions options) {
        Map<String, String> env = getStoreModelMain().getEnv();
        options = CoreNutsUtils.validate(options, ws);
        if (CoreStringUtils.isBlank(value)) {
            if (env != null && env.containsKey(property)) {
                env.remove(property);
                NutsWorkspaceConfigManagerExt.of(ws.config()).fireConfigurationChanged("env", options.getSession(), ConfigEventType.MAIN);
            }
        } else {
            if (env == null) {
                env = new LinkedHashMap<>();
                getStoreModelMain().setEnv(env);
            }
            String old = env.get(property);
            if (!value.equals(old)) {
                env.put(property, value);
                NutsWorkspaceConfigManagerExt.of(ws.config()).fireConfigurationChanged("env", options.getSession(), ConfigEventType.MAIN);
            }
        }
        return this;
    }

    private DefaultNutsWorkspaceCurrentConfig current() {
        return NutsWorkspaceConfigManagerExt.of(ws.config()).current();
    }

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

    @Override
    public NutsId getArch() {
        if (arch == null) {
            arch = ws.id().parser().parse(System.getProperty("os.arch"));
        }
        return arch;
    }

    @Override
    public NutsArchFamily getArchFamily() {
        return archFamily;
    }

    @Override
    public NutsOsFamily getOsFamily() {
        if (osFamily == null) {
            osFamily = getPlatformOsFamily0();
        }
        return osFamily;
    }

    @Override
    public NutsId getOs() {
        if (os == null) {
            os = ws.id().parser().parse(CorePlatformUtils.getPlatformOs(ws));
        }
        return os;
    }

    public NutsId getPlatform() {
        if (platform == null) {
            platform = NutsWorkspaceConfigManagerExt.of(ws.config())
                    .createSdkId("java", System.getProperty("java.version"));
        }
        return platform;
    }

    public NutsId getOsDist() {
        if (osdist == null) {
            String platformOsDist = CorePlatformUtils.getPlatformOsDist(ws);
            if (platformOsDist == null) {
                platformOsDist = "default";
            }
            osdist = ws.id().parser().parse(platformOsDist);
        }
        return osdist;
    }

    @Override
    public Map<String, Object> getProperties() {
        return userProperties;
    }

    @Override
    public Object getProperty(String property, Object defaultValue) {
        Object v = userProperties.get(property);
        if (v != null) {
            return v;
        }
        return defaultValue;
    }

    @Override
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

    @Override
    public String getPropertyAsString(String property, String defaultValue) {
        Object t = getProperty(property, null);
        if (t != null) {
            return t.toString();
        }
        return defaultValue;
    }

    @Override
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

    @Override
    public Object getProperty(String property) {
        return getProperty(property, null);
    }

    @Override
    public <T> T getOrCreateProperty(Class<T> property, Supplier<T> supplier, Supplier<NutsUpdateOptions> options) {
        return getOrCreateProperty(property.getName(), supplier, options);
    }

    @Override
    public <T> T getOrCreateProperty(String property, Supplier<T> supplier, Supplier<NutsUpdateOptions> options) {
        T o = (T) getProperty(property);
        if (o != null) {
            return o;
        }
        o = supplier.get();
        setProperty(property, o, options == null ? null : options.get());
        return o;
    }

    @Override
    public NutsWorkspaceEnvManager setProperty(String property, Object value, NutsUpdateOptions options) {
        if (value == null) {
            userProperties.remove(property);
        } else {
            userProperties.put(property, value);
        }
        return this;
    }
}

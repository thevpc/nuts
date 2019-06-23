package net.vpc.app.nuts.core.format;

import java.io.PrintWriter;
import java.io.Writer;

import net.vpc.app.nuts.*;

import java.util.*;

import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.core.DefaultNutsVersion;
import net.vpc.app.nuts.core.filters.version.DefaultNutsVersionFilter;

/**
 *
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultVersionFormat extends DefaultFormatBase<NutsVersionFormat> implements NutsVersionFormat {

    private final Properties extraProperties = new Properties();
    private boolean all;
    private NutsVersion version;
    private NutsVersionInterval versionIntervall;

    public DefaultVersionFormat(NutsWorkspace ws) {
        super(ws, "version");
    }

    @Override
    public NutsVersionFilter parseFilter(String versionFilter) {
        return DefaultNutsVersionFilter.parse(versionFilter);
    }

    @Override
    public NutsVersion parse(String version) {
        return DefaultNutsVersion.valueOf(version);
    }

    @Override
    public NutsVersion getVersion() {
        return version;
    }

    @Override
    public NutsVersionFormat setVersion(NutsVersion version) {
        this.versionIntervall = null;
        this.version = version;
        return this;
    }

    @Override
    public NutsVersionInterval getVersionInterval() {
        return versionIntervall;
    }

    @Override
    public NutsVersionFormat setVersionInterval(NutsVersionInterval version) {
        this.versionIntervall = version;
        this.version = null;
        return this;
    }

    @Override
    public NutsVersionFormat setWorkspaceVersion() {
        version = null;
        versionIntervall = null;
        return this;
    }

    @Override
    public boolean isWorkspaceVersion() {
        return version == null && versionIntervall == null;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "--all": {
                this.all = cmdLine.nextBoolean().getBooleanValue();
                return true;
            }
            case "--add": {
                this.all = true;
                NutsArgument r = cmdLine.nextString().getArgumentValue();
                extraProperties.put(r.getStringKey(), r.getStringValue());
                return true;
            }
            default: {
                if (getValidSession().configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public NutsVersionFormat addProperty(String key, String value) {
        extraProperties.setProperty(key, value);
        return this;
    }

    @Override
    public NutsVersionFormat addProperties(Properties p) {
        if (p != null) {
            extraProperties.putAll(p);
        }
        return this;
    }

    @Override
    public void print(Writer out) {
        if (getValidSession().isPlainOut() && !all) {
            if (isWorkspaceVersion()) {
                PrintWriter pout = getValidPrintWriter(out);
                NutsBootContext rtcontext = ws.config().getContext(NutsBootContextType.RUNTIME);
                pout.printf("%s/%s", rtcontext.getApiId().getVersion(), rtcontext.getRuntimeId().getVersion());
            } else if (getVersion() != null) {
                PrintWriter pout = getValidPrintWriter(out);
                pout.printf("%s", getVersion());
            } else if (getVersionInterval() != null) {
                PrintWriter pout = getValidPrintWriter(out);
                pout.printf("%s", getVersionInterval());
            }
        } else {
            if (isWorkspaceVersion()) {
                ws.object().session(getValidSession()).set(buildProps()).print(out);
            } else if (getVersion() != null) {
                ws.object().session(getValidSession()).set(getVersion()).print(out);
            } else if (getVersionInterval() != null) {
                ws.object().session(getValidSession()).set(getVersionInterval()).print(out);
            }
        }
    }

    public Map<String, String> buildProps() {
        LinkedHashMap<String, String> props = new LinkedHashMap<>();
        NutsWorkspaceConfigManager configManager = ws.config();
        NutsBootContext rtcontext = configManager.getContext(NutsBootContextType.RUNTIME);
        Set<String> extraKeys = new TreeSet<>();
        if (extraProperties != null) {
            extraKeys = new TreeSet(extraProperties.keySet());
        }
        props.put("nuts-api-version", rtcontext.getApiId().getVersion().toString());
        props.put("nuts-runtime-version", rtcontext.getRuntimeId().getVersion().toString());
        if (all) {
            props.put("java-version", System.getProperty("java.version"));
            props.put("os-version", ws.config().getPlatformOs().getVersion().toString());
        }
        for (String extraKey : extraKeys) {
            props.put(extraKey, extraProperties.getProperty(extraKey));
        }
        return props;
    }
}

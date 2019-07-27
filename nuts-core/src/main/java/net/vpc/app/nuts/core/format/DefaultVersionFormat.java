package net.vpc.app.nuts.core.format;

import java.io.PrintWriter;
import java.io.Writer;

import net.vpc.app.nuts.*;

import java.util.*;

import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.core.DefaultNutsVersion;

/**
 *
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultVersionFormat extends DefaultFormatBase<NutsVersionFormat> implements NutsVersionFormat {

    private final Map<String,String> extraProperties = new LinkedHashMap<>();
    private boolean all;
    private NutsVersion version;

    public DefaultVersionFormat(NutsWorkspace ws) {
        super(ws, "version");
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
        this.version = version;
        return this;
    }

    @Override
    public boolean isWorkspaceVersion() {
        return version == null;
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
        if(value==null){
            extraProperties.remove(key);
        }else {
            extraProperties.put(key, value);
        }
        return this;
    }

    @Override
    public NutsVersionFormat addProperties(Map<String,String> p) {
        if (p != null) {
            for (Map.Entry<String, String> entry : p.entrySet()) {
                addProperty(entry.getKey(),entry.getValue());
            }
        }
        return this;
    }

    @Override
    public void print(Writer out) {
        if (getValidSession().isPlainOut() && !all) {
            if (isWorkspaceVersion()) {
                PrintWriter pout = getValidPrintWriter(out);
                NutsWorkspaceConfigManager rtcontext = ws.config();
                pout.printf("%s/%s", rtcontext.getApiVersion(), rtcontext.getRuntimeId().getVersion());
            } else {
                PrintWriter pout = getValidPrintWriter(out);
                pout.printf("%s", getVersion());
            }
        } else {
            if (isWorkspaceVersion()) {
                ws.object().session(getValidSession()).value(buildProps()).print(out);
            } else {
                ws.object().session(getValidSession()).value(getVersion()).print(out);
            }
        }
    }

    public Map<String, String> buildProps() {
        LinkedHashMap<String, String> props = new LinkedHashMap<>();
        NutsWorkspaceConfigManager configManager = ws.config();
        Set<String> extraKeys = new TreeSet<>();
        if (extraProperties != null) {
            extraKeys = new TreeSet(extraProperties.keySet());
        }
        props.put("nuts-api-version", configManager.getApiVersion());
        props.put("nuts-runtime-version", configManager.getRuntimeId().getVersion().toString());
        if (all) {
            props.put("java-version", System.getProperty("java.version"));
            props.put("os-version", ws.config().getPlatformOs().getVersion().toString());
        }
        for (String extraKey : extraKeys) {
            props.put(extraKey, extraProperties.get(extraKey));
        }
        return props;
    }
}

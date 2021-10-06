package net.thevpc.nuts.runtime.core.format;

import net.thevpc.nuts.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultVersionFormat extends DefaultFormatBase<NutsVersionFormat> implements NutsVersionFormat {

    private final Map<String, String> extraProperties = new LinkedHashMap<>();
    private boolean all;
    private NutsVersion version;

    public DefaultVersionFormat(NutsWorkspace ws) {
        super(ws, "version");
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        checkSession();
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isEnabled();
        switch (a.getKey().getString()) {
            case "-a":
            case "--all": {
                boolean val = cmdLine.nextBoolean().getValue().getBoolean();
                if (enabled) {
                    this.all = val;
                }
                return true;
            }
            case "--add": {
                NutsArgument aa = cmdLine.nextString();
                NutsArgument r = NutsArgument.of(aa.getValue().getString(),getSession());
                if (enabled) {
                    this.all = true;
                    extraProperties.put(r.getKey().getString(), r.getValue().getString());
                }
                return true;
            }
            default: {
                if (getSession().configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public NutsVersionFormat addProperty(String key, String value) {
        if (value == null) {
            extraProperties.remove(key);
        } else {
            extraProperties.put(key, value);
        }
        return this;
    }

    @Override
    public NutsVersionFormat addProperties(Map<String, String> p) {
        if (p != null) {
            for (Map.Entry<String, String> entry : p.entrySet()) {
                addProperty(entry.getKey(), entry.getValue());
            }
        }
        return this;
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
    public void print(NutsPrintStream out) {
        checkSession();
        if (!isNtf()) {
            out = out.convertMode(NutsTerminalMode.FILTERED);
        }
        if (getSession().isPlainOut()) {
            if (isWorkspaceVersion()) {
                out.printf("%s/%s", getSession().getWorkspace().getApiVersion(), getSession().getWorkspace().getRuntimeId().getVersion());
            } else {
                out.printf("%s", getSession().text().ofStyled(
                        getVersion().toString(), NutsTextStyle.version()
                ));
            }
        } else {
            if (isWorkspaceVersion()) {
                getSession().formats().object(buildProps()).print(out);
            } else {
                out.print(
                        getSession().text().ofStyled(
                                getVersion().toString(), NutsTextStyle.version()
                        )
                );
            }
        }
    }

    public Map<String, String> buildProps() {
        LinkedHashMap<String, String> props = new LinkedHashMap<>();
        Set<String> extraKeys = new TreeSet<>();
        if (extraProperties != null) {
            extraKeys = new TreeSet(extraProperties.keySet());
        }
        NutsSession ws = getSession();
        props.put("nuts-api-version", ws.getWorkspace().getApiVersion().toString());
        props.put("nuts-runtime-version", ws.getWorkspace().getRuntimeId().getVersion().toString());
        if (all) {
            props.put("java-version", System.getProperty("java.version"));
            props.put("os-version", ws.env().getOs().getVersion().toString());
        }
        for (String extraKey : extraKeys) {
            props.put(extraKey, extraProperties.get(extraKey));
        }
        return props;
    }
}

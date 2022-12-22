package net.thevpc.nuts.runtime.standalone.version.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsTerminalMode;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNutsVersionFormat extends DefaultFormatBase<NutsVersionFormat> implements NutsVersionFormat {

    private final Map<String, String> extraProperties = new LinkedHashMap<>();
    private boolean all;
    private NutsVersion version;

    public DefaultNutsVersionFormat(NutsSession session) {
        super(session, "version");
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        checkSession();
        NutsSession session = getSession();
        NutsArgument aa = commandLine.peek().get(session);
        if (aa == null) {
            return false;
        }
        boolean enabled = aa.isActive();
        switch (aa.key()) {
            case "-a":
            case "--all": {
                commandLine.withNextBoolean((v, a, s) -> this.all = v);
                return true;
            }
            case "--add": {
                NutsArgument aa2 = commandLine.nextString().get(session);
                NutsArgument r = NutsArgument.of(aa2.getStringValue().get(session));
                if (enabled) {
                    this.all = true;
                    this.extraProperties.put(r.key(), r.getStringValue().get(session));
                }
                return true;
            }
            default: {
                if (session.configureFirst(commandLine)) {
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
            out = out.setTerminalMode(NutsTerminalMode.FILTERED);
        }
        if (getSession().isPlainOut()) {
            if (isWorkspaceVersion()) {
                out.printf("%s/%s", getSession().getWorkspace().getApiVersion(), getSession().getWorkspace().getRuntimeId().getVersion());
            } else {
                out.printf("%s", NutsTexts.of(getSession()).ofStyled(
                        getVersion().toString(), NutsTextStyle.version()
                ));
            }
        } else {
            if (isWorkspaceVersion()) {
                out.printf(buildProps());
            } else {
                out.print(
                        NutsTexts.of(getSession()).ofStyled(
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

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}

package net.thevpc.nuts.runtime.standalone.version.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNVersionFormat extends DefaultFormatBase<NVersionFormat> implements NVersionFormat {

    private final Map<String, String> extraProperties = new LinkedHashMap<>();
    private boolean all;
    private NVersion version;

    public DefaultNVersionFormat(NSession session) {
        super(session, "version");
    }

    @Override
    public boolean configureFirst(NCommandLine commandLine) {
        checkSession();
        NSession session = getSession();
        NArgument aa = commandLine.peek().get(session);
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
                NArgument aa2 = commandLine.nextString().get(session);
                NArgument r = NArgument.of(aa2.getStringValue().get(session));
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
    public NVersionFormat addProperty(String key, String value) {
        if (value == null) {
            extraProperties.remove(key);
        } else {
            extraProperties.put(key, value);
        }
        return this;
    }

    @Override
    public NVersionFormat addProperties(Map<String, String> p) {
        if (p != null) {
            for (Map.Entry<String, String> entry : p.entrySet()) {
                addProperty(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public NVersion getVersion() {
        return version;
    }

    @Override
    public NVersionFormat setVersion(NVersion version) {
        this.version = version;
        return this;
    }

    @Override
    public boolean isWorkspaceVersion() {
        return version == null;
    }

    @Override
    public void print(NOutStream out) {
        checkSession();
        if (!isNtf()) {
            out = out.setTerminalMode(NTerminalMode.FILTERED);
        }
        if (getSession().isPlainOut()) {
            if (isWorkspaceVersion()) {
                out.printf("%s/%s", getSession().getWorkspace().getApiVersion(), getSession().getWorkspace().getRuntimeId().getVersion());
            } else {
                out.printf("%s", NTexts.of(getSession()).ofStyled(
                        getVersion().toString(), NTextStyle.version()
                ));
            }
        } else {
            if (isWorkspaceVersion()) {
                out.printf(buildProps());
            } else {
                out.print(
                        NTexts.of(getSession()).ofStyled(
                                getVersion().toString(), NTextStyle.version()
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
        NSession ws = getSession();
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
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}

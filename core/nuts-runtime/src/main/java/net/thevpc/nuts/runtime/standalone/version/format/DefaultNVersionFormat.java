package net.thevpc.nuts.runtime.standalone.version.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.env.NEnvs;
import net.thevpc.nuts.format.NVersionFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NMsg;

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

    public DefaultNVersionFormat(NWorkspace workspace) {
        super(workspace, "version");
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NSession session=workspace.currentSession();
        NArg aa = cmdLine.peek().get();
        if (aa == null) {
            return false;
        }
        boolean enabled = aa.isActive();
        switch (aa.key()) {
            case "-a":
            case "--all": {
                cmdLine.withNextFlag((v, a) -> this.all = v);
                return true;
            }
            case "--add": {
                NArg aa2 = cmdLine.nextEntry().get();
                NArg r = NArg.of(aa2.getStringValue().get());
                if (enabled) {
                    this.all = true;
                    this.extraProperties.put(r.key(), r.getStringValue().get());
                }
                return true;
            }
            default: {
                if (session.configureFirst(cmdLine)) {
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
    public void print(NPrintStream out) {
        NSession session=workspace.currentSession();
        if (!isNtf()) {
            out = out.setTerminalMode(NTerminalMode.FILTERED);
        }
        if (session.isPlainOut()) {
            if (isWorkspaceVersion()) {
                out.print((NMsg.ofC("%s/%s", session.getWorkspace().getApiVersion(), session.getWorkspace().getRuntimeId().getVersion())));
            } else {
                out.print(NText.ofStyled(
                        getVersion().toString(), NTextStyle.version()
                ));
            }
        } else {
            if (isWorkspaceVersion()) {
                out.print(buildProps());
            } else {
                out.print(
                        NText.ofStyled(
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
        NSession session=workspace.currentSession();
        props.put("nuts-api-version", session.getWorkspace().getApiVersion().toString());
        props.put("nuts-runtime-version", session.getWorkspace().getRuntimeId().getVersion().toString());
        if (all) {
            props.put("java-version", System.getProperty("java.version"));
            props.put("os-version", NEnvs.of().getOs().getVersion().toString());
        }
        for (String extraKey : extraKeys) {
            props.put(extraKey, extraProperties.get(extraKey));
        }
        return props;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}

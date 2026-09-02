package net.thevpc.nuts.runtime.standalone.version.format;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.runtime.standalone.format.DefaultObjectWriterBase;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.text.NVersionWriter;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * type: Command Class
 *
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNVersionWriter extends DefaultObjectWriterBase<NVersionWriter> implements NVersionWriter {

    private final Map<String, String> extraProperties = new LinkedHashMap<>();
    private boolean all;

    public DefaultNVersionWriter() {
        super("version");
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg aa = cmdLine.peek().get();
        if (aa == null) {
            return false;
        }
        boolean enabled = aa.isUncommented();
        switch (aa.key()) {
            case "-a":
            case "--all": {
                cmdLine.matcher().whenAny().asFlag((v) -> this.all = v.booleanValue()).anyMatch();
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
                if (NSession.of().configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public NVersionWriter addProperty(String key, String value) {
        if (value == null) {
            extraProperties.remove(key);
        } else {
            extraProperties.put(key, value);
        }
        return this;
    }

    @Override
    public NVersionWriter addProperties(Map<String, String> p) {
        if (p != null) {
            for (Map.Entry<String, String> entry : p.entrySet()) {
                addProperty(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public void print(Object aValue, NPrintStream out) {
        if (!(aValue instanceof NVersion)) {
            return;
        }
        NVersion version = (NVersion) aValue;
        boolean simple = !all && extraProperties.isEmpty();
        NContentType p = NSession.of().outputFormat().orElse(NContentType.PLAIN);
        if (p == NContentType.PLAIN && !simple) {
            p = NContentType.PROPS;
        }
        if (!isNtf()) {
            out = out.terminalMode(NTerminalMode.FILTERED);
        }
        NPrintStream finalOut = out;
        NSession.of()
                .copy()
                .outputFormat(p)
                .runWith(() -> {
                    if (simple && NSession.of().outputFormat().get() == NContentType.PLAIN) {
                        finalOut.print(
                                NText.ofStyled(
                                        version.toString(), NTextStyle.version()
                                )
                        );
                    } else {
                        finalOut.print(buildProps(version));
                    }
                });
    }

    public Map<String, String> buildProps(NVersion version) {
        LinkedHashMap<String, String> props = new LinkedHashMap<>();
        Set<String> extraKeys = new TreeSet<>();
        if (extraProperties != null) {
            extraKeys = new TreeSet(extraProperties.keySet());
        }
        NWorkspace workspace = NWorkspace.of();
        props.put("version", version.toString());
        props.put("nuts-api-version", workspace.apiVersion().toString());
        props.put("nuts-runtime-version", workspace.runtimeId().version().toString());
        if (all) {
            props.put("java-version", System.getProperty("java.version"));
            NEnv environment = NEnv.of();
            props.put("os-version", environment.os().version().toString());
        }
        for (String extraKey : extraKeys) {
            if (!props.containsKey(extraKey)) { // do not override
                props.put(extraKey, extraProperties.get(extraKey));
            }
        }
        return props;
    }

}

package net.thevpc.nuts.runtime.standalone.version.format;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.text.NVersionFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NMsg;

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
public class DefaultNVersionFormat extends DefaultFormatBase<NVersionFormat> implements NVersionFormat {

    private final Map<String, String> extraProperties = new LinkedHashMap<>();
    private boolean all;

    public DefaultNVersionFormat(NWorkspace workspace) {
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
                cmdLine.matcher().matchFlag((v) -> this.all = v.booleanValue()).anyMatch();
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
    public void print(Object aValue, NPrintStream out) {
        if(!(aValue instanceof NVersion)){
            return;
        }
        if (isNtf()) {
            out.print(
                    NText.ofStyled(
                            aValue.toString(), NTextStyle.version()
                    )
            );
        }else{
            out = out.setTerminalMode(NTerminalMode.FILTERED);
            out.print(aValue.toString());
        }
    }

    public Map<String, String> buildProps() {
        LinkedHashMap<String, String> props = new LinkedHashMap<>();
        Set<String> extraKeys = new TreeSet<>();
        if (extraProperties != null) {
            extraKeys = new TreeSet(extraProperties.keySet());
        }
        NWorkspace workspace = NWorkspace.of();
        props.put("nuts-api-version", workspace.getApiVersion().toString());
        props.put("nuts-runtime-version", workspace.getRuntimeId().getVersion().toString());
        if (all) {
            props.put("java-version", System.getProperty("java.version"));
            NEnv environment = NEnv.of();
            props.put("os-version", environment.getOs().getVersion().toString());
        }
        for (String extraKey : extraKeys) {
            props.put(extraKey, extraProperties.get(extraKey));
        }
        return props;
    }

}

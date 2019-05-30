package net.vpc.app.nuts.core.format;

import java.io.PrintWriter;
import java.io.Writer;

import net.vpc.app.nuts.*;

import java.util.*;

import net.vpc.app.nuts.NutsCommand;

/**
 *
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultVersionFormat extends DefaultFormatBase<NutsWorkspaceVersionFormat> implements NutsWorkspaceVersionFormat {

    private final Properties extraProperties = new Properties();
    private boolean minimal = false;
    private boolean compact;

    public DefaultVersionFormat(NutsWorkspace ws) {
        super(ws,"version");
    }

    @Override
    public boolean configureFirst(NutsCommand cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getKey().getString()) {
            case "--min": {
                this.setMinimal(cmdLine.nextBoolean().getValue().getBoolean());
                return true;
            }
            case "--compact": {
                this.setCompact(cmdLine.nextBoolean().getValue().getBoolean());
                return true;
            }
            case "--add": {
                NutsArgument r = cmdLine.nextString().getValue();
                extraProperties.put(r.getKey().getString(), r.getValue().getString());
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
    public NutsWorkspaceVersionFormat minimal() {
        return minimal(true);
    }

    @Override
    public NutsWorkspaceVersionFormat minimal(boolean minimal) {
        return setMinimal(minimal);
    }

    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public NutsWorkspaceVersionFormat compact() {
        return compact(true);
    }

    @Override
    public NutsWorkspaceVersionFormat compact(boolean compact) {
        return setCompact(compact);
    }

    @Override
    public NutsWorkspaceVersionFormat setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    @Override
    public boolean isMinimal() {
        return minimal;
    }

    @Override
    public NutsWorkspaceVersionFormat setMinimal(boolean minimal) {
        this.minimal = minimal;
        return this;
    }

    @Override
    public NutsWorkspaceVersionFormat addProperty(String key, String value) {
        extraProperties.setProperty(key, value);
        return this;
    }

    @Override
    public NutsWorkspaceVersionFormat addProperties(Properties p) {
        if (p != null) {
            extraProperties.putAll(p);
        }
        return this;
    }

    @Override
    public void print(Writer out) {
        NutsOutputFormat t = getValidSession().getOutputFormat();
        if ((t == null || t == NutsOutputFormat.PLAIN) && isMinimal()) {
            PrintWriter pout = getValidPrintWriter(out);
            NutsBootContext rtcontext = ws.config().getContext(NutsBootContextType.RUNTIME);
            pout.printf("%s/%s", rtcontext.getApiId().getVersion(), rtcontext.getRuntimeId().getVersion());
        } else {
            ws.formatter().createObjectFormat(getValidSession(), buildProps()).print(out);
        }
    }

    public Map<String, String> buildProps() {
        LinkedHashMap<String, String> props = new LinkedHashMap<>();
        NutsWorkspaceConfigManager configManager = ws.config();
        NutsBootContext rtcontext = configManager.getContext(NutsBootContextType.RUNTIME);
        if (isMinimal()) {
            props.put("nuts-api-version", rtcontext.getApiId().getVersion().toString());
            props.put("nuts-runtime-version", rtcontext.getRuntimeId().getVersion().toString());
            return props;
        }
        Set<String> extraKeys = new TreeSet<>();
        if (extraProperties != null) {
            extraKeys = new TreeSet(extraProperties.keySet());
        }
        props.put("nuts-api-version", rtcontext.getApiId().getVersion().toString());
        props.put("nuts-runtime-version", rtcontext.getRuntimeId().getVersion().toString());
        props.put("java-version", System.getProperty("java.version"));
        props.put("os-version", ws.config().getPlatformOs().getVersion().toString());
        for (String extraKey : extraKeys) {
            props.put(extraKey, extraProperties.getProperty(extraKey));
        }
        return props;
    }
}

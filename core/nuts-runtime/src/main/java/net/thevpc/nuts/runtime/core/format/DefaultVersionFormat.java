package net.thevpc.nuts.runtime.core.format;

import java.io.PrintStream;

import net.thevpc.nuts.*;

import java.util.*;

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
        boolean enabled = a.isEnabled();
        switch (a.getStringKey()) {
            case "-a":
            case "--all": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    this.all = val;
                }
                return true;
            }
            case "--add": {
                NutsArgument r = cmdLine.nextString().getArgumentValue();
                if(enabled) {
                    this.all = true;
                    extraProperties.put(r.getStringKey(), r.getStringValue());
                }
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
    public void print(PrintStream out) {
        if (isWorkspaceVersion()) {
            out.printf("%s/%s", getWorkspace().getApiVersion(), getWorkspace().getRuntimeId().getVersion());
        } else {
            out.printf("%s", getVersion());
        }
//        if (getValidSession().isPlainOut() && !all) {
//            if (isWorkspaceVersion()) {
//                PrintStream pout = getValidPrintStream(out);
//                pout.printf("%s/%s", getWorkspace().getApiVersion(), getWorkspace().getRuntimeId().getVersion());
//            } else {
//                PrintStream pout = getValidPrintStream(out);
//                pout.printf("%s", getVersion());
//            }
//        } else {
//            if (isWorkspaceVersion()) {
//                getValidSession().formatObject(buildProps()).print(out);
//            } else {
//                getValidSession().formatObject(getVersion()).print(out);
//            }
//        }
    }

    public Map<String, String> buildProps() {
        LinkedHashMap<String, String> props = new LinkedHashMap<>();
        Set<String> extraKeys = new TreeSet<>();
        if (extraProperties != null) {
            extraKeys = new TreeSet(extraProperties.keySet());
        }
        props.put("nuts-api-version", getWorkspace().getApiVersion());
        props.put("nuts-runtime-version", getWorkspace().getRuntimeId().getVersion().toString());
        if (all) {
            props.put("java-version", System.getProperty("java.version"));
            props.put("os-version", getWorkspace().env().getOs().getVersion().toString());
        }
        for (String extraKey : extraKeys) {
            props.put(extraKey, extraProperties.get(extraKey));
        }
        return props;
    }
}

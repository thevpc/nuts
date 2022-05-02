package net.thevpc.nuts.runtime.standalone.xtra.hashname;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsHashName;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.util.NutsPlatformUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DefaultNutsHashName implements NutsHashName {
    private NutsSession session;
    private Object source;
    private String sourceType;

    public DefaultNutsHashName(NutsSession session) {
        this.session = session;
    }

    public String getWorkspaceHashName(String path) {
        if (path == null) {
            path = "";
        }
        String n;
        String p;
        if (path.contains("\\") || path.contains("/") || path.equals(".") || path.equals("..")) {
            Path pp = Paths.get(path).toAbsolutePath().normalize();
            n = pp.getFileName().toString();
            p = pp.getParent() == null ? null : pp.getParent().toString();
        } else {
            n = path;
            p = "";
        }
        if (p == null) {
            return ("Root " + n).trim();
        } else {
            Path root = Paths.get(NutsPlatformUtils.getWorkspaceLocation(
                    null,
                    false,
                    null
            )).getParent().getParent();
            if (p.equals(root.toString())) {
                return n;
            }
            return (getHashName(p) + " " + n).trim();
        }
    }


    @Override
    public String getHashName(Object source) {
        return getHashName(source, null);
    }

    @Override
    public String getHashName(Object source, String sourceType) {
        if (source == null) {
            return "default";
        } else if (source instanceof String) {
            if ("workspace".equalsIgnoreCase(sourceType)) {
                return getWorkspaceHashName(source.toString());
            }
            if (source.toString().isEmpty()) {
                return "empty";
            }
            return getHashName(source.hashCode());
        } else if (source instanceof NutsPath) {
            if ("workspace".equalsIgnoreCase(sourceType)) {
                return getWorkspaceHashName(source.toString());
            }
            return getHashName(source.hashCode());
        } else if (source instanceof NutsWorkspace) {
            NutsPath location = ((NutsWorkspace) source).getLocation();
            return getWorkspaceHashName(location == null ? null : location.toString());
        } else if (source instanceof NutsSession) {
            NutsPath location = ((NutsSession) source).getWorkspace().getLocation();
            return getWorkspaceHashName(location == null ? null : location.toString());
        } else if (source instanceof Integer) {
            int i = (int) source;
            return CoreNutsUtils.COLOR_NAMES[Math.abs(i) % CoreNutsUtils.COLOR_NAMES.length];
        } else {
            return getHashName(source.hashCode());
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}

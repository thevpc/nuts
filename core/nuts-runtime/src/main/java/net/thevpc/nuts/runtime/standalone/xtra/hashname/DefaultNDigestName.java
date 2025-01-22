package net.thevpc.nuts.runtime.standalone.xtra.hashname;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.io.NDigestName;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.NPlatformHome;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DefaultNDigestName implements NDigestName {
    private NSession session;
    private Object source;
    private String sourceType;

    public DefaultNDigestName(NSession session) {
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
            Path root = Paths.get(NPlatformHome.USER.getWorkspaceLocation(null)).getParent().getParent();
            if (p.equals(root.toString())) {
                return n;
            }
            return (getDigestName(p) + " " + n).trim();
        }
    }


    @Override
    public String getDigestName(Object source) {
        return getDigestName(source, null);
    }

    @Override
    public String getDigestName(Object source, String sourceType) {
        if (source == null) {
            return "default";
        } else if (source instanceof String) {
            if ("workspace".equalsIgnoreCase(sourceType)) {
                return getWorkspaceHashName(source.toString());
            }
            if (source.toString().isEmpty()) {
                return "empty";
            }
            return getDigestName(source.hashCode());
        } else if (source instanceof NPath) {
            if ("workspace".equalsIgnoreCase(sourceType)) {
                return getWorkspaceHashName(source.toString());
            }
            return getDigestName(source.hashCode());
        } else if (source instanceof NWorkspace) {
            NPath location = ((NWorkspace) source).getLocation();
            return getWorkspaceHashName(location == null ? null : location.toString());
        } else if (source instanceof NSession) {
            NPath location = ((NSession) source).getWorkspace().getLocation();
            return getWorkspaceHashName(location == null ? null : location.toString());
        } else if (source instanceof Integer) {
            int i = (int) source;
            return CoreNUtils.COLOR_NAMES[Math.abs(i) % CoreNUtils.COLOR_NAMES.length];
        } else {
            return getDigestName(source.hashCode());
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}

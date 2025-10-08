package net.thevpc.nuts.runtime.standalone.xtra.hashname;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.NDigestName;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NScorableContext;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NNames;

public class DefaultNDigestName implements NDigestName {
    private Object source;
    private String sourceType;

    public DefaultNDigestName() {
    }

//    public String getWorkspaceHashName(String path) {
//        if (path == null) {
//            path = "";
//        }
//        String n;
//        String p;
//        if (path.contains("\\") || path.contains("/") || path.equals(".") || path.equals("..")) {
//            Path pp = Paths.get(path).toAbsolutePath().normalize();
//            n = pp.getFileName().toString();
//            p = pp.getParent() == null ? null : pp.getParent().toString();
//        } else {
//            n = path;
//            p = "";
//        }
//        if (p == null) {
//            return ("Root " + n).trim();
//        } else {
//            Path root = Paths.get(NPlatformHome.USER.getWorkspaceLocation(null)).getParent().getParent();
//            if (p.equals(root.toString())) {
//                return n;
//            }
//            return (getDigestName(p) + " " + n).trim();
//        }
//    }


    @Override
    public String getDigestName(Object source) {
        if (source == null) {
            return "default";
        } else if (source instanceof String) {
            return getDigestNameString((String) source);
        } else if (source instanceof NPath) {
            return getDigestNameString(source.toString());
        } else if (source instanceof NWorkspace) {
            NPath location = ((NWorkspace) source).getLocation();
            if(location==null){
                return getDigestNameString("default");
            }
            return getDigestNameString(location.toString());
        } else if (source instanceof NSession) {
            NPath location = ((NSession) source).getWorkspace().getLocation();
            if(location==null){
                return getDigestNameString("default");
            }
            return getDigestNameString(location.toString());
        } else if (source instanceof Integer) {
            return getDigestNameInt((int) source);
        } else {
            return getDigestNameInt(source.hashCode());
        }
    }

    private String getDigestNameInt(int source) {
        return NNames.pickName(source,2,3, NNameFormat.CLASS_NAME);
    }

    private String getDigestNameString(String source) {
        if (source == null || source.isEmpty()) {
            return "empty";
        }
        return getDigestNameInt(source.hashCode());
    }

    @Override
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }
}

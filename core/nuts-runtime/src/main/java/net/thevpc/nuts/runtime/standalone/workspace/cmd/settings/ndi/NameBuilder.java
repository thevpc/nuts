package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;

public class NameBuilder {
    private NId id;
    private NDescriptor descriptor;
    private String preferredName;
    private String defaultName;
    private NWorkspace workspace;
    private boolean preferId;

    public NameBuilder(NId id, String preferredName, String defaultName, NDescriptor descriptor, NWorkspace workspace, boolean preferId) {
        this.id = id;
        this.preferredName = preferredName;
        this.descriptor = descriptor;
        this.workspace = workspace;
        this.preferId = preferId;
        if (defaultName == null) {
            defaultName = "";
        }
        defaultName = defaultName.trim();
        if (defaultName.isEmpty()) {
            if (preferId) {
                defaultName = "%n%s%v%s%h";
            } else {
                defaultName = "%N%s%v%s%h";
            }
        }
        this.defaultName = defaultName;
    }

    public static NameBuilder id(NId id, String preferredName, String defaultName, NDescriptor descriptor, NWorkspace workspace) {
        return new NameBuilder(id, preferredName, defaultName, descriptor, workspace, true);
    }

    public static NameBuilder label(NId id, String preferredName, String defaultName, NDescriptor descriptor, NWorkspace workspace) {
        return new NameBuilder(id, preferredName, defaultName, descriptor, workspace, false);
    }

    public static String extractPathName(String s) {
        if (s == null) {
            return "";
        }
        int i = Math.max(s.lastIndexOf('/'), s.lastIndexOf('\\'));
        if (i >= 0) {
            return s.substring(i + 1).trim();
        }
        return s.trim();
    }

    private String toValidString(String s) {
        StringBuilder sb = new StringBuilder();
        if (s != null) {
            for (char c : s.toCharArray()) {
                sb.append(toValidChar(c));
            }
        }
        return sb.toString();
    }

    private String toValidChar(char c) {
        if (c == ' ') {
            if (preferId) {
                return "-";
            }
            return " ";
        }
        if (Character.isWhitespace(c)) {
            return "_";
        }
        switch (c) {
            case '/':
            case '\\':
            case ':':
            case '*':
            case '?':
            case '&':
            case '(':
            case ')':
            case '[':
            case ']':
            case '%': {
                return "-";
            }
        }
        return String.valueOf(c);
    }

    public String buildName() {
        String s = preferredName;
        if (s != null) {
            int i = Math.max(s.lastIndexOf('/'), s.lastIndexOf('\\'));
            if (i >= 0) {
                String p = s.substring(0, i + 1);
                String n = s.substring(i + 1);
                return p + buildName(n);
            }
        }
        return buildName(s);
    }

    private String buildName(String s) {
        if (s == null) {
            s = "";
        }
        s = s.trim();
        if (s.isEmpty()) {
            s = defaultName;
        }
        BuildAccumulator h = new BuildAccumulator();
        char[] charArray = s.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == '%' && i + 1 < charArray.length) {
                i++;
                char cc = charArray[i];
                switch (cc) {
                    case 'v': {
                        h.append(id.getVersion().toString());
                        break;
                    }
                    case 'g': {
                        h.append(id.getGroupId());
                        break;
                    }
                    case 'n': {
                        h.append(id.getArtifactId());
                        break;
                    }
                    case 'N': {
                        String str = descriptor.getName();
                        if (str == null) {
                            str = "";
                        }
                        str = str.trim();
                        if (str.isEmpty()) {
                            str = id.getArtifactId();
                        }
                        h.append(str);
                        break;
                    }
                    case 'h': {
                        if (!NWorkspaceUtils.isUserDefaultWorkspace()) {
                            h.append(workspace.getDigestName());
                        }
                        break;
                    }
                    case 'a': {
                        h.appendValid(CoreStringUtils.joinAndTrimToNull(id.getCondition().getArch()));
                        break;
                    }
                    case 's': {
                        h.sep();
                        break;
                    }
                    default: {
                        h.append(c);
                    }
                }
            } else if (c == '%') {
                //
            } else if (c == '/' || c == '\\') {
                h.append(c);
            } else {
                h.appendValid(c);
            }
        }
        if (h.isEmpty()) {
            h.appendValid(id.getArtifactId());
        }
        return h.toString();
    }

    class BuildAccumulator {
        StringBuilder sb = new StringBuilder();
        boolean wasSep = false;

        boolean isEmpty() {
            return sb.length()==0;
        }

        void sep() {
            wasSep=true;
        }
        void appendValid(char c) {
            append(toValidChar(c));
        }

        void append(char c) {
            wasSep=false;
            sb.append(c);
        }

        void appendValid(String str) {
            append(toValidString(str));
        }

        void append(String str) {
            if(str!=null) {
                if (!str.isEmpty()) {
                    if (wasSep) {
                        sb.append(toValidChar(' '));
                        wasSep = false;
                    }
                    sb.append(toValidString(str));
                }
            }
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }

}

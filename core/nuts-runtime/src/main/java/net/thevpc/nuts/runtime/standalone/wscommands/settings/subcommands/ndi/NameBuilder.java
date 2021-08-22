package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi;

import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;

public class NameBuilder {
    private NutsId id;
    private NutsDescriptor descriptor;
    private String preferredName;
    private String defaultName;
    private NutsSession session;
    private boolean preferId;

    public NameBuilder(NutsId id, String preferredName, String defaultName, NutsDescriptor descriptor, NutsSession session, boolean preferId) {
        this.id = id;
        this.preferredName = preferredName;
        this.descriptor = descriptor;
        this.session = session;
        this.preferId = preferId;
        if (defaultName == null) {
            defaultName = "";
        }
        defaultName = defaultName.trim();
        if (defaultName.isEmpty()) {
            if(preferId) {
                defaultName = "%n%s%v%s%h";
            }else{
                defaultName = "%N%s%v%s%h";
            }
        }
        this.defaultName = defaultName;
    }

    public static NameBuilder id(NutsId id, String preferredName, String defaultName, NutsDescriptor descriptor, NutsSession session) {
        return new NameBuilder(id, preferredName, defaultName, descriptor, session, true);
    }

    public static NameBuilder label(NutsId id, String preferredName, String defaultName, NutsDescriptor descriptor, NutsSession session) {
        return new NameBuilder(id, preferredName, defaultName, descriptor, session, false);
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
        StringBuilder sb = new StringBuilder();
        char[] charArray = s.toCharArray();
        boolean wasSep = false;
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == '%' && i + 1 < charArray.length) {
                i++;
                char cc = charArray[i];
                switch (cc) {
                    case 'v': {
                        String str = id.getVersion().toString();
                        if (wasSep) {
                            if (!str.isEmpty()) {
                                sb.append(toValidChar(' '));
                            }
                            wasSep = false;
                        }
                        sb.append(toValidString(str));
                        break;
                    }
                    case 'g': {
                        String str = id.getGroupId();
                        if (wasSep) {
                            if (!str.isEmpty()) {
                                sb.append(toValidChar(' '));
                            }
                            wasSep = false;
                        }
                        sb.append(toValidString(str));
                        break;
                    }
                    case 'n': {
                        String str = id.getArtifactId();
                        if (wasSep) {
                            if (!str.isEmpty()) {
                                sb.append(toValidChar(' '));
                            }
                            wasSep = false;
                        }
                        sb.append(toValidString(str));
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
                        if (wasSep) {
                            if (!str.isEmpty()) {
                                sb.append(toValidChar(' '));
                            }
                            wasSep = false;
                        }
                        sb.append(toValidString(str));
                        break;
                    }
                    case 'h': {
                        String str = session.getWorkspace().getHashName().trim();
                        if (str.equalsIgnoreCase("default")) {
                            str = "";
                        }
                        if (wasSep) {
                            if (!str.isEmpty()) {
                                sb.append(toValidChar(' '));
                            }
                            wasSep = false;
                        }
                        sb.append(toValidString(str));
                        break;
                    }
                    case 'a': {
                        String str = toValidString(id.getArch());
                        if (wasSep) {
                            if (!str.isEmpty()) {
                                sb.append(toValidChar(' '));
                            }
                            wasSep = false;
                        }
                        sb.append(str);
                        break;
                    }
                    case 's': {
                        wasSep = true;
                        break;
                    }
                    default: {
                        wasSep = false;
                        sb.append(c);
                    }
                }
            } else if (c == '%') {
                //
            } else if (c == '/' || c == '\\') {
                sb.append(c);
            } else {
                sb.append(toValidChar(c));
            }
        }
        String sbs = sb.toString();
        if (sbs.isEmpty()) {
            sb.append(toValidString(id.getArtifactId()));
        }
        return sb.toString();
    }
}

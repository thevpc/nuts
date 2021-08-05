package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base;

import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsId;

public class NameBuilder {
    private NutsId id;
    private NutsDescriptor descriptor;
    private String preferredName;

    public NameBuilder(NutsId id, String preferredName, NutsDescriptor descriptor) {
        this.id = id;
        this.preferredName = preferredName;
        this.descriptor = descriptor;
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
        if (s == null) {
            s = "";
        }
        s = s.trim();
        if (s.isEmpty()) {
            s = "%n-%v";
        }
        StringBuilder sb = new StringBuilder();
        char[] charArray = s.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == '%' && i + 1 < charArray.length) {
                i++;
                char cc = charArray[i];
                switch (cc) {
                    case 'v': {
                        sb.append(toValidString(id.getVersion().toString()));
                        break;
                    }
                    case 'g': {
                        sb.append(toValidString(id.getGroupId()));
                        break;
                    }
                    case 'n': {
                        sb.append(toValidString(id.getArtifactId()));
                        break;
                    }
                    case 'N': {
                        String n = descriptor.getName();
                        if (n == null) {
                            n = "";
                        }
                        n = n.trim();
                        if (n.isEmpty()) {
                            n = id.getArtifactId();
                        }
                        sb.append(toValidString(n));
                        break;
                    }
                    case 'a': {
                        sb.append(toValidString(id.getArch()));
                        break;
                    }
                    default: {
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

package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsOsFamily;
import net.thevpc.nuts.NutsUtilPlatforms;
import net.thevpc.nuts.NutsWorkspaceOptions;

import java.util.ArrayList;
import java.util.List;

class PrivateNutsBootIdUtils {
    static NutsBootId[] parseBootIdList(String s) {
        List<NutsBootId> boots = new ArrayList<>();
        StringBuilder q = null;
        boolean inBrackets = false;
        for (char c : s.toCharArray()) {
            if (q == null) {
                q = new StringBuilder();
                if (c == '[' || c == ']') {
                    inBrackets = true;
                    q.append(c);
                } else if (c == ',' || Character.isWhitespace(c)) {
                    //ignore
                } else {
                    q.append(c);
                }
            } else {
                if (c == ',' || c == ' ') {
                    if (inBrackets) {
                        q.append(c);
                    } else {
                        boots.add(NutsBootId.parse(q.toString()));
                        q = null;
                        inBrackets = false;
                    }
                } else if (c == '[' || c == ']') {
                    if (inBrackets) {
                        inBrackets = false;
                        q.append(c);
                    } else {
                        inBrackets = true;
                        q.append(c);
                    }
                } else {
                    q.append(c);
                }
            }
        }
        if (q != null) {
            boots.add(NutsBootId.parse(q.toString()));
        }
        return boots.toArray(new NutsBootId[0]);
    }

    static boolean isAcceptDependency(NutsBootId s, NutsWorkspaceOptions woptions) {
        boolean bootOptionals = NutsWorkspaceOptionsUtils.isBootOptional(woptions);

        //by default ignore optionals
        if (s.isOptional()) {
            if (!bootOptionals && !NutsWorkspaceOptionsUtils.isBootOptional(s.getArtifactId(),woptions)) {
                return false;
            }
        }
        String os = s.getOs();
        String arch = s.getArch();
        if (os.isEmpty() && arch.isEmpty()) {
            return true;
        }
        if (!os.isEmpty()) {
            NutsOsFamily eos = NutsUtilPlatforms.getPlatformOsFamily();
            boolean osOk = false;
            for (NutsBootId e : NutsBootId.parseAll(os)) {
                if (e.getShortName().equalsIgnoreCase(eos.id())) {
                    if (e.getVersion().accept(NutsBootVersion.parse(System.getProperty("os.version")))) {
                        osOk = true;
                    }
                    break;
                }
            }
            if (!osOk) {
                return false;
            }
        }
        if (!arch.isEmpty()) {
            String earch = System.getProperty("os.arch");
            if (earch != null) {
                boolean archOk = false;
                for (String e : arch.split("[,; ]")) {
                    if (!e.isEmpty()) {
                        if (e.equalsIgnoreCase(earch)) {
                            archOk = true;
                            break;
                        }
                    }
                }
                return archOk;
            }
        }
        return true;
    }
}

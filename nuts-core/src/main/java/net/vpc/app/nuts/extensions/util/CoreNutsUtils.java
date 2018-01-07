package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdInvalidFormatException;
import net.vpc.app.nuts.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreNutsUtils {
    public static NutsId finNutsIdByFullName(NutsId id, Collection<NutsId> all) {
        if (all != null) {
            for (NutsId nutsId : all) {
                if (nutsId != null) {
                    if (nutsId.isSameFullName(id)) {
                        return nutsId;
                    }
                }
            }
        }
        return null;
    }

    public static boolean isSameGroupAndName(NutsId a, NutsId b) {
        return a.setVersion("").unsetQuery().setNamespace("")
                .equals(b.setVersion("").unsetQuery().setNamespace(""));
    }

    public static void validateNutName(String name) {
        if (!name.matches("[a-zA-Z][.a-zA-Z0-9_-]*")) {
            throw new IllegalArgumentException("Invalid nuts name " + name);
        }
    }

    public static String formatImport(List<String> imports) throws IOException {
        LinkedHashSet<String> all = new LinkedHashSet<>();
        StringBuilder sb = new StringBuilder();
        for (String s : imports) {
            s = s.trim();
            if (s.length() > 0) {
                if (!all.contains(s)) {
                    all.add(s);
                    if (sb.length() > 0) {
                        sb.append(":");
                    }
                    sb.append(s);
                }
            }
        }
        return sb.toString();
    }

    public static List<String> parseImport(String imports) throws IOException {
        LinkedHashSet<String> all = new LinkedHashSet<>();
        if (imports != null) {
            String[] groupsArr = imports.split(":");
            for (String grp : groupsArr) {
                String grp2 = StringUtils.trim(grp);
                if (grp2.length() > 0) {
                    if (!all.contains(grp2)) {
                        all.add(grp2);
                    }
                }
            }
        }
        return new ArrayList<>(all);
    }

    public static File getNutsFolder(NutsId id, File root) {
        if (StringUtils.isEmpty(id.getGroup())) {
            throw new NutsIdInvalidFormatException("Missing group for " + id);
        }
        File groupFolder = new File(root, id.getGroup().replaceAll("\\.", File.separator));
        if (StringUtils.isEmpty(id.getName())) {
            throw new NutsIdInvalidFormatException("Missing name for " + id.toString());
        }
        File artifactFolder = new File(groupFolder, id.getName());
        if (id.getVersion().isEmpty()) {
            throw new NutsIdInvalidFormatException("Missing version for " + id.toString());
        }
        File versionFolder = new File(artifactFolder, id.getVersion().getValue());
        String face = id.getFace();
        if (StringUtils.isEmpty(face)) {
            face = NutsConstants.QUERY_FACE_DEFAULT_VALUE;
        }
        return new File(versionFolder, face);
    }

    public static String[][] splitEnvAndAppArgs(String[] args) {
        List<String> env = new ArrayList<>();
        List<String> app = new ArrayList<>();
        boolean expectEnv = true;
        for (String s : args) {
            if (expectEnv) {
                if (s.startsWith("--nuts-")) {
                    if (s.startsWith("--nuts-arg-")) {
                        app.add("--nuts-" + s.substring(0, "--nuts-arg-".length()));
                    } else {
                        env.add(s.substring("--nuts".length()));
                    }
                } else {
                    app.add(s);
                    expectEnv = false;
                }
            } else {
                app.add(s);
            }
        }
        return new String[][]{
                env.toArray(new String[env.size()]),
                app.toArray(new String[app.size()]),
        };
    }

}

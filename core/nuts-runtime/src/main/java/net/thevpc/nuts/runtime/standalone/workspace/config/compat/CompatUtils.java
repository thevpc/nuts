package net.thevpc.nuts.runtime.standalone.workspace.config.compat;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.io.NutsPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

public class CompatUtils {
    public static List<String> copyStringList(List<String> refs) {
        if (refs == null) {
            return null;
        }
        return new ArrayList<>(refs);
    }

    public static List<NutsRepositoryRef> copyNutsRepositoryRefList(List<NutsRepositoryRef> refs) {
        if (refs == null) {
            return null;
        }
        List<NutsRepositoryRef> list = new ArrayList<>();
        for (NutsRepositoryRef r : refs) {
            if (r == null) {
                list.add(null);
            } else {
                NutsRepositoryRef r2 = new NutsRepositoryRef();
                r2.setEnabled(r.isEnabled());
                r2.setDeployWeight(r.getDeployWeight());
                r2.setFailSafe(r.isFailSafe());
                r2.setLocation(r.getLocation());
                r2.setName(r.getName());
                list.add(r2);
            }
        }
        return list;
    }

    public static List<NutsUserConfig> copyNutsUserConfigList(List<NutsUserConfig> refs) {
        if (refs == null) {
            return null;
        }
        List<NutsUserConfig> list = new ArrayList<>();
        for (NutsUserConfig r : refs) {
            if (r == null) {
                list.add(null);
            } else {
                NutsUserConfig r2 = new NutsUserConfig();
                r2.setGroups(r.getGroups());
                r2.setPermissions(r.getPermissions());
                r2.setRemoteCredentials(r.getRemoteCredentials());
                r2.setRemoteIdentity(r.getRemoteIdentity());
                r2.setUser(r.getUser());
                list.add(r2);
            }
        }
        return list;
    }

    public static NutsUserConfig[] copyNutsUserConfigArray(NutsUserConfig[] refs) {
        if (refs == null) {
            return null;
        }
        List<NutsUserConfig> list = new ArrayList<>();
        for (NutsUserConfig r : refs) {
            if (r == null) {
                list.add(null);
            } else {
                NutsUserConfig r2 = new NutsUserConfig();
                r2.setGroups(r.getGroups());
                r2.setPermissions(r.getPermissions());
                r2.setRemoteCredentials(r.getRemoteCredentials());
                r2.setRemoteIdentity(r.getRemoteIdentity());
                r2.setUser(r.getUser());
                list.add(r2);
            }
        }
        return list.toArray(new NutsUserConfig[0]);
    }

    public static List<NutsCommandFactoryConfig> copyNutsCommandAliasFactoryConfigList(List<NutsCommandFactoryConfig> refs) {
        if (refs == null) {
            return null;
        }
        List<NutsCommandFactoryConfig> list = new ArrayList<>();
        for (NutsCommandFactoryConfig r : refs) {
            if (r == null) {
                list.add(null);
            } else {
                NutsCommandFactoryConfig r2 = new NutsCommandFactoryConfig();
                r2.setFactoryId(r.getFactoryId());
                r2.setFactoryType(r.getFactoryType());
                r2.setParameters(r.getParameters() == null ? null : new LinkedHashMap<>(r.getParameters()));
                r2.setPriority(r.getPriority());
                list.add(r2);
            }
        }
        return list;
    }

    public static List<NutsPlatformLocation> copyNutsSdkLocationList(List<NutsPlatformLocation> refs) {
        if (refs == null) {
            return null;
        }
        List<NutsPlatformLocation> list = new ArrayList<>();
        for (NutsPlatformLocation r : refs) {
            if (r == null) {
                list.add(null);
            } else {
                NutsPlatformLocation r2 = new NutsPlatformLocation(
                        r.getId(),
                        r.getProduct(),
                        r.getName(),
                        r.getPath(),
                        r.getVersion(),
                        r.getPackaging(),
                        r.getPriority()
                );
                r2.setConfigVersion(r.getConfigVersion());
                list.add(r2);
            }
        }
        return list;
    }

    public static Properties copyProperties(Properties p) {
        if (p == null) {
            return null;
        }
        Properties pp=new Properties();
        pp.putAll(p);
        return pp;
    }

    public static byte[] readAllBytes(Path file, NutsSession session) {
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }
    public static byte[] readAllBytes(NutsPath file, NutsSession session) {
        if (!file.isRegularFile()) {
            return null;
        }
        return file.readAllBytes();
    }

}

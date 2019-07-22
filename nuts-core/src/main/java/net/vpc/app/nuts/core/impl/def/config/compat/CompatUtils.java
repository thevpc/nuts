package net.vpc.app.nuts.core.impl.def.config.compat;

import net.vpc.app.nuts.NutsCommandAliasFactoryConfig;
import net.vpc.app.nuts.NutsRepositoryRef;
import net.vpc.app.nuts.NutsSdkLocation;
import net.vpc.app.nuts.NutsUserConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
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
                r2.setDeployOrder(r.getDeployOrder());
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

    public static List<NutsCommandAliasFactoryConfig> copyNutsCommandAliasFactoryConfigList(List<NutsCommandAliasFactoryConfig> refs) {
        if (refs == null) {
            return null;
        }
        List<NutsCommandAliasFactoryConfig> list = new ArrayList<>();
        for (NutsCommandAliasFactoryConfig r : refs) {
            if (r == null) {
                list.add(null);
            } else {
                NutsCommandAliasFactoryConfig r2 = new NutsCommandAliasFactoryConfig();
                r2.setFactoryId(r.getFactoryId());
                r2.setFactoryType(r.getFactoryType());
                r2.setParameters(r.getParameters() == null ? null : new LinkedHashMap<>(r.getParameters()));
                r2.setPriority(r.getPriority());
                list.add(r2);
            }
        }
        return list;
    }

    public static List<NutsSdkLocation> copyNutsSdkLocationList(List<NutsSdkLocation> refs) {
        if (refs == null) {
            return null;
        }
        List<NutsSdkLocation> list = new ArrayList<>();
        for (NutsSdkLocation r : refs) {
            if (r == null) {
                list.add(null);
            } else {
                NutsSdkLocation r2 = new NutsSdkLocation();
                r2.setName(r.getName());
                r2.setPath(r.getPath());
                r2.setType(r.getType());
                r2.setVersion(r.getVersion());
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

    public static byte[] readAllBytes(Path file) {
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}

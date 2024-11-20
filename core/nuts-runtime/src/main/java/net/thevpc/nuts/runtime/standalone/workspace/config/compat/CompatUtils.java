package net.thevpc.nuts.runtime.standalone.workspace.config.compat;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;

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

    public static List<NRepositoryRef> copyNutsRepositoryRefList(List<NRepositoryRef> refs) {
        if (refs == null) {
            return null;
        }
        List<NRepositoryRef> list = new ArrayList<>();
        for (NRepositoryRef r : refs) {
            if (r == null) {
                list.add(null);
            } else {
                NRepositoryRef r2 = new NRepositoryRef();
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

    public static List<NUserConfig> copyNutsUserConfigList(List<NUserConfig> refs) {
        if (refs == null) {
            return null;
        }
        List<NUserConfig> list = new ArrayList<>();
        for (NUserConfig r : refs) {
            if (r == null) {
                list.add(null);
            } else {
                NUserConfig r2 = new NUserConfig();
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

    public static NUserConfig[] copyNutsUserConfigArray(NUserConfig[] refs) {
        if (refs == null) {
            return null;
        }
        List<NUserConfig> list = new ArrayList<>();
        for (NUserConfig r : refs) {
            if (r == null) {
                list.add(null);
            } else {
                NUserConfig r2 = new NUserConfig();
                r2.setGroups(r.getGroups());
                r2.setPermissions(r.getPermissions());
                r2.setRemoteCredentials(r.getRemoteCredentials());
                r2.setRemoteIdentity(r.getRemoteIdentity());
                r2.setUser(r.getUser());
                list.add(r2);
            }
        }
        return list.toArray(new NUserConfig[0]);
    }

    public static List<NCommandFactoryConfig> copyNutsCommandAliasFactoryConfigList(List<NCommandFactoryConfig> refs) {
        if (refs == null) {
            return null;
        }
        List<NCommandFactoryConfig> list = new ArrayList<>();
        for (NCommandFactoryConfig r : refs) {
            if (r == null) {
                list.add(null);
            } else {
                NCommandFactoryConfig r2 = new NCommandFactoryConfig();
                r2.setFactoryId(r.getFactoryId());
                r2.setFactoryType(r.getFactoryType());
                r2.setParameters(r.getParameters() == null ? null : new LinkedHashMap<>(r.getParameters()));
                r2.setPriority(r.getPriority());
                list.add(r2);
            }
        }
        return list;
    }

    public static List<NPlatformLocation> copyNutsSdkLocationList(List<NPlatformLocation> refs) {
        if (refs == null) {
            return null;
        }
        List<NPlatformLocation> list = new ArrayList<>();
        for (NPlatformLocation r : refs) {
            if (r == null) {
                list.add(null);
            } else {
                NPlatformLocation r2 = new NPlatformLocation(
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

    public static byte[] readAllBytes(Path file, NSession session) {
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }
    public static byte[] readAllBytes(NPath file, NSession session) {
        if (!file.isRegularFile()) {
            return null;
        }
        return file.readBytes();
    }

}

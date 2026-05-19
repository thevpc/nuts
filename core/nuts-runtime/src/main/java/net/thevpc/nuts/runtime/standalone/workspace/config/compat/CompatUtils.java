package net.thevpc.nuts.runtime.standalone.workspace.config.compat;

import net.thevpc.nuts.command.NCommandFactoryConfig;
import net.thevpc.nuts.platform.NExecutionEngineLocation;
import net.thevpc.nuts.core.NRepositoryRef;
import net.thevpc.nuts.security.NUserConfig;
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
                r2.enabled(r.isEnabled());
                r2.deployWeight(r.deployWeight());
                r2.failSafe(r.isFailSafe());
                r2.location(r.location());
                r2.name(r.name());
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
//                r2.setRemoteCredentials(r.getRemoteCredentials());
//                r2.setRemoteIdentity(r.getRemoteIdentity());
                r2.setUserName(r.getUserName());
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
//                r2.setRemoteCredentials(r.getRemoteCredentials());
//                r2.setRemoteIdentity(r.getRemoteIdentity());
                r2.setUserName(r.getUserName());
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
                r2.factoryId(r.factoryId());
                r2.factoryType(r.factoryType());
                r2.parameters(r.parameters() == null ? null : new LinkedHashMap<>(r.parameters()));
                r2.priority(r.priority());
                list.add(r2);
            }
        }
        return list;
    }

    public static List<NExecutionEngineLocation> copyNutsSdkLocationList(List<NExecutionEngineLocation> refs) {
        if (refs == null) {
            return null;
        }
        List<NExecutionEngineLocation> list = new ArrayList<>();
        for (NExecutionEngineLocation r : refs) {
            if (r == null) {
                list.add(null);
            } else {
                NExecutionEngineLocation r2 = r.copy();
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

    public static byte[] readAllBytes(Path file) {
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }
    public static byte[] readAllBytes(NPath file) {
        if (!file.isRegularFile()) {
            return null;
        }
        return file.readBytes();
    }

}

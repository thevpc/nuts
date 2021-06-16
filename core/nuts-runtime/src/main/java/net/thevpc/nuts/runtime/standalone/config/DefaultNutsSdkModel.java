package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.util.NutsJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class DefaultNutsSdkModel {

    private final Map<String, List<NutsSdkLocation>> configSdks = new LinkedHashMap<>();
    private NutsWorkspace workspace;

    public DefaultNutsSdkModel(NutsWorkspace ws) {
        this.workspace = ws;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public String[] findSdkTypes() {
        Set<String> s = getSdk().keySet();
        return s.toArray(new String[0]);
    }

    public boolean add(NutsSdkLocation location, NutsSession session) {
        return add0(location, session, true);
    }

    public boolean add0(NutsSdkLocation location, NutsSession session, boolean notify) {
//        session = CoreNutsUtils.validate(session, workspace);
        if (location != null) {
            if (CoreStringUtils.isBlank(location.getProduct())) {
                throw new NutsIllegalArgumentException(session, "sdk type should not be null");
            }
            if (CoreStringUtils.isBlank(location.getName())) {
                throw new NutsIllegalArgumentException(session, "sdk name should not be null");
            }
            if (CoreStringUtils.isBlank(location.getVersion())) {
                throw new NutsIllegalArgumentException(session, "sdk version should not be null");
            }
            if (CoreStringUtils.isBlank(location.getPath())) {
                throw new NutsIllegalArgumentException(session, "sdk path should not be null");
            }
            List<NutsSdkLocation> list = getSdk().get(location.getProduct());
            if (list == null) {
                list = new ArrayList<>();
                configSdks.put(location.getProduct(), list);
            }
            NutsSdkLocation old = null;
            for (NutsSdkLocation nutsSdkLocation : list) {
                if (Objects.equals(nutsSdkLocation.getPackaging(), location.getPackaging())
                        && Objects.equals(nutsSdkLocation.getProduct(), location.getProduct())) {
                    if (nutsSdkLocation.getName().equals(location.getName())
                            || nutsSdkLocation.getPath().equals(location.getPath())) {
                        old = nutsSdkLocation;
                        break;
                    }
                }
            }
            if (old != null) {
                return false;
            }
            list.add(location);
            if (notify) {
                if (session.isPlainTrace()) {
                    session.out().resetLine().printf("install %s %s (%s) %s at %s%n",
                            location.getId().getShortName(),
                            location.getPackaging(),
                            location.getProduct(),
                            session.getWorkspace().text().builder().append(location.getVersion(), NutsTextStyle.version()),
                            session.getWorkspace().text().builder().append(location.getPath(), NutsTextStyle.path())
                    );
                }
                NutsWorkspaceConfigManagerExt.of(workspace.config())
                        .getModel()
                        .fireConfigurationChanged("sdk", session, ConfigEventType.MAIN);
            }
            return true;
        }
        return false;
    }

    public boolean update(NutsSdkLocation oldLocation, NutsSdkLocation newLocation, NutsSession session) {
        boolean updated = false;
        updated |= remove(oldLocation, session);
        updated |= remove(newLocation, session);
        updated |= add(newLocation, session);
        return updated;
    }

    public boolean remove(NutsSdkLocation location, NutsSession session) {
        if (location != null) {
            List<NutsSdkLocation> list = getSdk().get(location.getProduct());
            if (list != null) {
                if (list.remove(location)) {
                    NutsWorkspaceConfigManagerExt.of(session.getWorkspace().config())
                            .getModel()
                            .fireConfigurationChanged("sdk", session, ConfigEventType.MAIN);
                    return true;
                }
            }
        }
        return false;
    }

    public NutsSdkLocation findByName(String type, String locationName, NutsSession session) {
        return findOne(type, location -> location.getName().equals(locationName), session);
    }

    public NutsSdkLocation findByPath(String type, String path, NutsSession session) {
        return findOne(type, location -> location.getPath() != null && location.getPath().equals(path.toString()), session);
    }

    public NutsSdkLocation findByVersion(String type, String version, NutsSession session) {
        return findOne(type, location -> location.getVersion().equals(version), session);
    }

    //    public void setRepositoryEnabled(String repoName, boolean enabled) {
//        NutsRepositoryRef e = repositoryRegistryHelper.findRepositoryRef(repoName);
//        if (e != null && e.isEnabled() != enabled) {
//            e.setEnabled(enabled);
//            fireConfigurationChanged();
//        }
//    }
    public NutsSdkLocation find(NutsSdkLocation location, NutsSession session) {
        if (location == null) {
            return null;
        }
        String type = location.getId().getArtifactId();
        type = toValidSdkName(type);
        List<NutsSdkLocation> list = getSdk().get(type);
        if (list != null) {
            for (NutsSdkLocation location2 : list) {
                if (location2.equals(location)) {
                    return location2;
                }
            }
        }
        return null;
    }

    public NutsSdkLocation findByVersion(String type, NutsVersionFilter javaVersionFilter, final NutsSession session) {
        return findOne(type,
                location -> javaVersionFilter == null || javaVersionFilter.acceptVersion(session.getWorkspace().version().parser().parse(location.getVersion()), session),
                 session);
    }

    public NutsSdkLocation[] searchSystem(String sdkType, NutsSession session) {
        NutsWorkspaceUtils.checkSession(workspace, session);
        if ("java".equals(sdkType)) {
            try {
                NutsSdkLocation[] nutsSdkLocations = NutsJavaSdkUtils.of(session.getWorkspace()).searchJdkLocationsFuture(session).get();
                return nutsSdkLocations;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return new NutsSdkLocation[0];
    }

    public NutsSdkLocation[] searchSystem(String sdkType, String path, NutsSession session) {
        NutsWorkspaceUtils.checkSession(workspace, session);
        if ("java".equals(sdkType)) {
            return NutsJavaSdkUtils.of(session.getWorkspace()).searchJdkLocations(path, session);
        }
        return new NutsSdkLocation[0];
    }

    public NutsSdkLocation resolve(String sdkType, String path, String preferredName, NutsSession session) {
        NutsWorkspaceUtils.checkSession(workspace, session);
        if ("java".equals(sdkType)) {
            return NutsJavaSdkUtils.of(session.getWorkspace()).resolveJdkLocation(path, null, session);
        }
        return null;
    }

//    
    public void setSdks(NutsSdkLocation[] locations, NutsSession session) {
        configSdks.clear();
        for (NutsSdkLocation sdk : locations) {
            add0(sdk, session, false);
        }
    }

    public NutsSdkLocation findOne(String type, Predicate<NutsSdkLocation> filter, NutsSession session) {
        NutsSdkLocation[] a = find(type, filter, session);
        return a.length == 0 ? null : a[0];
    }

    public NutsSdkLocation[] find(String type, Predicate<NutsSdkLocation> filter, NutsSession session) {
        if (filter == null) {
            if (type == null) {
                List<NutsSdkLocation> all = new ArrayList<>();
                for (List<NutsSdkLocation> value : configSdks.values()) {
                    all.addAll(value);
                }
                return all.toArray(new NutsSdkLocation[0]);
            }
            type = toValidSdkName(type);
            List<NutsSdkLocation> list = getSdk().get(type);
            if (list == null) {
                return new NutsSdkLocation[0];
            }
            return list.toArray(new NutsSdkLocation[0]);
        }
        List<NutsSdkLocation> ret = new ArrayList<>();
        if (type == null) {
            for (List<NutsSdkLocation> found : getSdk().values()) {
                for (NutsSdkLocation location : found) {
                    if (filter.test(location)) {
                        ret.add(location);
                    }
                }
            }
        } else {
            type = toValidSdkName(type);
            List<NutsSdkLocation> found = getSdk().get(type);
            if (found != null) {
                for (NutsSdkLocation location : found) {
                    if (filter.test(location)) {
                        ret.add(location);
                    }
                }
            }
        }
        if (!ret.isEmpty()) {
            ret.sort(new NutsSdkLocationSelectComparator(session.getWorkspace()));
        }
        return ret.toArray(new NutsSdkLocation[0]);
    }

    private String toValidSdkName(String type) {
        if (CoreStringUtils.isBlank(type)) {
            type = "java";
        } else {
            type = CoreStringUtils.trim(type);
        }
        return type;
    }

    public Map<String, List<NutsSdkLocation>> getSdk() {
        return configSdks;
    }
}

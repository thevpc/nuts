package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.util.NutsJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class DefaultNutsPlatformModel {

    private final Map<String, List<NutsPlatformLocation>> configPlatforms = new LinkedHashMap<>();
    private NutsWorkspace workspace;

    public DefaultNutsPlatformModel(NutsWorkspace ws) {
        this.workspace = ws;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public String[] findPlatformTypes() {
        Set<String> s = getPlatforms().keySet();
        return s.toArray(new String[0]);
    }

    public boolean add(NutsPlatformLocation location, NutsSession session) {
        return add0(location, session, true);
    }

    public boolean add0(NutsPlatformLocation location, NutsSession session, boolean notify) {
//        session = CoreNutsUtils.validate(session, workspace);
        if (location != null) {
            if (NutsUtilStrings.isBlank(location.getProduct())) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("platform type should not be null"));
            }
            if (NutsUtilStrings.isBlank(location.getName())) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("platform name should not be null"));
            }
            if (NutsUtilStrings.isBlank(location.getVersion())) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("platform version should not be null"));
            }
            if (NutsUtilStrings.isBlank(location.getPath())) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("platform path should not be null"));
            }
            List<NutsPlatformLocation> list = getPlatforms().get(location.getProduct());
            if (list == null) {
                list = new ArrayList<>();
                configPlatforms.put(location.getProduct(), list);
            }
            NutsPlatformLocation old = null;
            for (NutsPlatformLocation nutsPlatformLocation : list) {
                if (Objects.equals(nutsPlatformLocation.getPackaging(), location.getPackaging())
                        && Objects.equals(nutsPlatformLocation.getProduct(), location.getProduct())) {
                    if (nutsPlatformLocation.getName().equals(location.getName())
                            || nutsPlatformLocation.getPath().equals(location.getPath())) {
                        old = nutsPlatformLocation;
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
                    session.out().resetLine().printf("%s %s %s (%s) %s at %s%n",
                            session.getWorkspace().text().forStyled("install",NutsTextStyles.of(NutsTextStyle.success())),
                            location.getId().getShortName(),
                            location.getPackaging(),
                            location.getProduct(),
                            session.getWorkspace().version().parser().parse(location.getVersion()),
                            session.getWorkspace().io().path(location.getPath())
                    );
                }
                NutsWorkspaceConfigManagerExt.of(workspace.config())
                        .getModel()
                        .fireConfigurationChanged("platform", session, ConfigEventType.MAIN);
            }
            return true;
        }
        return false;
    }

    public boolean update(NutsPlatformLocation oldLocation, NutsPlatformLocation newLocation, NutsSession session) {
        boolean updated = false;
        updated |= remove(oldLocation, session);
        updated |= remove(newLocation, session);
        updated |= add(newLocation, session);
        return updated;
    }

    public boolean remove(NutsPlatformLocation location, NutsSession session) {
        if (location != null) {
            List<NutsPlatformLocation> list = getPlatforms().get(location.getProduct());
            if (list != null) {
                if (list.remove(location)) {
                    NutsWorkspaceConfigManagerExt.of(session.getWorkspace().config())
                            .getModel()
                            .fireConfigurationChanged("platform", session, ConfigEventType.MAIN);
                    return true;
                }
            }
        }
        return false;
    }

    public NutsPlatformLocation findByName(String type, String locationName, NutsSession session) {
        return findOne(type, location -> location.getName().equals(locationName), session);
    }

    public NutsPlatformLocation findByPath(String type, String path, NutsSession session) {
        return findOne(type, location -> location.getPath() != null && location.getPath().equals(path.toString()), session);
    }

    public NutsPlatformLocation findByVersion(String type, String version, NutsSession session) {
        return findOne(type, location -> location.getVersion().equals(version), session);
    }

    //    public void setRepositoryEnabled(String repoName, boolean enabled) {
//        NutsRepositoryRef e = repositoryRegistryHelper.findRepositoryRef(repoName);
//        if (e != null && e.isEnabled() != enabled) {
//            e.setEnabled(enabled);
//            fireConfigurationChanged();
//        }
//    }
    public NutsPlatformLocation find(NutsPlatformLocation location, NutsSession session) {
        if (location == null) {
            return null;
        }
        String type = location.getId().getArtifactId();
        type = toValidPlatformName(type);
        List<NutsPlatformLocation> list = getPlatforms().get(type);
        if (list != null) {
            for (NutsPlatformLocation location2 : list) {
                if (location2.equals(location)) {
                    return location2;
                }
            }
        }
        return null;
    }

    public NutsPlatformLocation findByVersion(String type, NutsVersionFilter javaVersionFilter, final NutsSession session) {
        return findOne(type,
                location -> javaVersionFilter == null || javaVersionFilter.acceptVersion(session.getWorkspace().version().parser().parse(location.getVersion()), session),
                 session);
    }

    public NutsPlatformLocation[] searchSystem(String platformType, NutsSession session) {
        NutsWorkspaceUtils.checkSession(workspace, session);
        if ("java".equals(platformType)) {
            try {
                NutsPlatformLocation[] nutsPlatformLocations = NutsJavaSdkUtils.of(session.getWorkspace()).searchJdkLocationsFuture(session).get();
                return nutsPlatformLocations;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return new NutsPlatformLocation[0];
    }

    public NutsPlatformLocation[] searchSystem(String platformType, String path, NutsSession session) {
        NutsWorkspaceUtils.checkSession(workspace, session);
        if ("java".equals(platformType)) {
            return NutsJavaSdkUtils.of(session.getWorkspace()).searchJdkLocations(path, session);
        }
        return new NutsPlatformLocation[0];
    }

    public NutsPlatformLocation resolve(String platformType, String path, String preferredName, NutsSession session) {
        NutsWorkspaceUtils.checkSession(workspace, session);
        if ("java".equals(platformType)) {
            return NutsJavaSdkUtils.of(session.getWorkspace()).resolveJdkLocation(path, null, session);
        }
        return null;
    }

//    
    public void setPlatforms(NutsPlatformLocation[] locations, NutsSession session) {
        configPlatforms.clear();
        for (NutsPlatformLocation platform : locations) {
            add0(platform, session, false);
        }
    }

    public NutsPlatformLocation findOne(String type, Predicate<NutsPlatformLocation> filter, NutsSession session) {
        NutsPlatformLocation[] a = find(type, filter, session);
        return a.length == 0 ? null : a[0];
    }

    public NutsPlatformLocation[] find(String type, Predicate<NutsPlatformLocation> filter, NutsSession session) {
        if (filter == null) {
            if (type == null) {
                List<NutsPlatformLocation> all = new ArrayList<>();
                for (List<NutsPlatformLocation> value : configPlatforms.values()) {
                    all.addAll(value);
                }
                return all.toArray(new NutsPlatformLocation[0]);
            }
            type = toValidPlatformName(type);
            List<NutsPlatformLocation> list = getPlatforms().get(type);
            if (list == null) {
                return new NutsPlatformLocation[0];
            }
            return list.toArray(new NutsPlatformLocation[0]);
        }
        List<NutsPlatformLocation> ret = new ArrayList<>();
        if (type == null) {
            for (List<NutsPlatformLocation> found : getPlatforms().values()) {
                for (NutsPlatformLocation location : found) {
                    if (filter.test(location)) {
                        ret.add(location);
                    }
                }
            }
        } else {
            type = toValidPlatformName(type);
            List<NutsPlatformLocation> found = getPlatforms().get(type);
            if (found != null) {
                for (NutsPlatformLocation location : found) {
                    if (filter.test(location)) {
                        ret.add(location);
                    }
                }
            }
        }
        if (!ret.isEmpty()) {
            ret.sort(new NutsPlatformLocationSelectComparator(session.getWorkspace()));
        }
        return ret.toArray(new NutsPlatformLocation[0]);
    }

    private String toValidPlatformName(String type) {
        if (NutsUtilStrings.isBlank(type)) {
            type = "java";
        } else {
            type = NutsUtilStrings.trim(type);
        }
        return type;
    }

    public Map<String, List<NutsPlatformLocation>> getPlatforms() {
        return configPlatforms;
    }
}

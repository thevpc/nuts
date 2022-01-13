package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.jclass.NutsJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class DefaultNutsPlatformModel {

    private NutsWorkspace workspace;
    private DefaultNutsWorkspaceEnvManagerModel model;

    public DefaultNutsPlatformModel(DefaultNutsWorkspaceEnvManagerModel model) {
        this.workspace = model.getWorkspace();
        this.model = model;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }


    public boolean addPlatform(NutsPlatformLocation location, NutsSession session) {
        return add0(location, session, true);
    }

    public boolean add0(NutsPlatformLocation location, NutsSession session, boolean notify) {
//        session = CoreNutsUtils.validate(session, workspace);
        if (location != null) {
            if (NutsBlankable.isBlank(location.getProduct())) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("platform type should not be null"));
            }
            if (NutsBlankable.isBlank(location.getName())) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("platform name should not be null"));
            }
            if (NutsBlankable.isBlank(location.getVersion())) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("platform version should not be null"));
            }
            if (NutsBlankable.isBlank(location.getPath())) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("platform path should not be null"));
            }
            List<NutsPlatformLocation> list = getPlatforms().get(location.getPlatformType());
            if (list == null) {
                list = new ArrayList<>();
                model.getConfigPlatforms().put(location.getPlatformType(), list);
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
                            NutsTexts.of(session).ofStyled("install",NutsTextStyles.of(NutsTextStyle.success())),
                            location.getId().getShortName(),
                            location.getPackaging(),
                            location.getProduct(),
                            NutsVersion.of(location.getVersion(),session),
                            NutsPath.of(location.getPath(),session)
                    );
                }
                NutsWorkspaceConfigManagerExt.of(session.config())
                        .getModel()
                        .fireConfigurationChanged("platform", session, ConfigEventType.MAIN);
            }
            return true;
        }
        return false;
    }

    public boolean updatePlatform(NutsPlatformLocation oldLocation, NutsPlatformLocation newLocation, NutsSession session) {
        boolean updated = false;
        updated |= removePlatform(oldLocation, session);
        updated |= removePlatform(newLocation, session);
        updated |= addPlatform(newLocation, session);
        return updated;
    }

    public boolean removePlatform(NutsPlatformLocation location, NutsSession session) {
        if (location != null) {
            List<NutsPlatformLocation> list = getPlatforms().get(location.getPlatformType());
            if (list != null) {
                if (list.remove(location)) {
                    NutsWorkspaceConfigManagerExt.of(session.config())
                            .getModel()
                            .fireConfigurationChanged("platform", session, ConfigEventType.MAIN);
                    return true;
                }
            }
        }
        return false;
    }

    public NutsPlatformLocation findPlatformByName(NutsPlatformFamily type, String locationName, NutsSession session) {
        return findOnePlatform(type, location -> location.getName().equals(locationName), session);
    }

    public NutsPlatformLocation findPlatformByPath(NutsPlatformFamily type, String path, NutsSession session) {
        return findOnePlatform(type, location -> location.getPath() != null && location.getPath().equals(path.toString()), session);
    }

    public NutsPlatformLocation findPlatformByVersion(NutsPlatformFamily type, String version, NutsSession session) {
        return findOnePlatform(type, location -> location.getVersion().equals(version), session);
    }

    //    public void setRepositoryEnabled(String repoName, boolean enabled) {
//        NutsRepositoryRef e = repositoryRegistryHelper.findRepositoryRef(repoName);
//        if (e != null && e.isEnabled() != enabled) {
//            e.setEnabled(enabled);
//            fireConfigurationChanged();
//        }
//    }
    public NutsPlatformLocation findPlatform(NutsPlatformLocation location, NutsSession session) {
        if (location == null) {
            return null;
        }
        String type = location.getId().getArtifactId();
        NutsPlatformFamily ftype = NutsPlatformFamily.parseLenient(type, NutsPlatformFamily.JAVA, NutsPlatformFamily.UNKNOWN);
        List<NutsPlatformLocation> list = getPlatforms().get(ftype);
        if (list != null) {
            for (NutsPlatformLocation location2 : list) {
                if (location2.equals(location)) {
                    return location2;
                }
            }
        }
        return null;
    }

    public NutsPlatformLocation findPlatformByVersion(NutsPlatformFamily type, NutsVersionFilter javaVersionFilter, final NutsSession session) {
        return findOnePlatform(type,
                location -> javaVersionFilter == null || javaVersionFilter.acceptVersion(NutsVersion.of(location.getVersion(),session), session),
                 session);
    }

    public NutsPlatformLocation[] searchSystemPlatforms(NutsPlatformFamily platformType, NutsSession session) {
        NutsWorkspaceUtils.checkSession(workspace, session);
        if (platformType== NutsPlatformFamily.JAVA) {
            try {
                return NutsJavaSdkUtils.of(session.getWorkspace()).searchJdkLocationsFuture(session).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return new NutsPlatformLocation[0];
    }

    public NutsPlatformLocation[] searchSystemPlatforms(NutsPlatformFamily platformType, String path, NutsSession session) {
        NutsWorkspaceUtils.checkSession(workspace, session);
        if (platformType== NutsPlatformFamily.JAVA) {
            return NutsJavaSdkUtils.of(session.getWorkspace()).searchJdkLocations(path, session);
        }
        return new NutsPlatformLocation[0];
    }

    public NutsPlatformLocation resolvePlatform(NutsPlatformFamily platformType, String path, String preferredName, NutsSession session) {
        NutsWorkspaceUtils.checkSession(workspace, session);
        if (platformType== NutsPlatformFamily.JAVA) {
            return NutsJavaSdkUtils.of(session.getWorkspace()).resolveJdkLocation(path, null, session);
        }
        return null;
    }

//    
    public void setPlatforms(NutsPlatformLocation[] locations, NutsSession session) {
        model.getConfigPlatforms().clear();
        for (NutsPlatformLocation platform : locations) {
            add0(platform, session, false);
        }
    }

    public NutsPlatformLocation findOnePlatform(NutsPlatformFamily type, Predicate<NutsPlatformLocation> filter, NutsSession session) {
        NutsPlatformLocation[] a = findPlatforms(type, filter, session);
        if(a.length == 0){
            return null;
        }
        if(a.length == 1){
            return a[0];
        }
        //find the best minimum version that is applicable!
        NutsPlatformLocation best=a[0];
        for (int i = 1; i < a.length; i++) {
            NutsVersion v1 = NutsVersion.of(best.getVersion(), session);
            NutsVersion v2 = NutsVersion.of(a[i].getVersion(), session);
            if(type==NutsPlatformFamily.JAVA) {
                double d1 = Double.parseDouble(JavaClassUtils.sourceVersionToClassVersion(v1.getValue(), session));
                double d2 = Double.parseDouble(JavaClassUtils.sourceVersionToClassVersion(v2.getValue(), session));
                if (d1 == d2) {
                    //1.8u100 vs 1.8u101, select 1.8u101
                    if (v1.compareTo(v2) < 0) {
                        best = a[i];
                    }
                } else {
                    //1.8u100 vs 9u101, select 1.8u100
                    if (v1.compareTo(v2) > 0) {
                        best = a[i];
                    }
                }
            }else{
                if (v1.compareTo(v2) > 0) {
                    best = a[i];
                }
            }
        }
        return best;
    }

    public NutsPlatformLocation[] findPlatforms(NutsPlatformFamily type, Predicate<NutsPlatformLocation> filter, NutsSession session) {
        if (filter == null) {
            if (type == null) {
                List<NutsPlatformLocation> all = new ArrayList<>();
                for (List<NutsPlatformLocation> value : model.getConfigPlatforms().values()) {
                    all.addAll(value);
                }
                return all.toArray(new NutsPlatformLocation[0]);
            }
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
            ret.sort(new NutsPlatformLocationSelectComparator(session));
        }
        return ret.toArray(new NutsPlatformLocation[0]);
    }

//    private NutsPlatformFamily toValidPlatformName(NutsPlatformFamily type) {
//        if (NutsBlankable.isBlank(type)) {
//            type = "java";
//        } else {
//            type = NutsUtilStrings.trim(type);
//        }
//        return type;
//    }

    public Map<NutsPlatformFamily, List<NutsPlatformLocation>> getPlatforms() {
        return model.getConfigPlatforms();
    }
}

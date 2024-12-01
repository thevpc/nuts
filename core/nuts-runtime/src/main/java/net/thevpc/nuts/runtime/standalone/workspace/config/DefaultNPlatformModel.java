package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NPlatformFamily;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class DefaultNPlatformModel {

    private NWorkspace workspace;
    private DefaultNWorkspaceEnvManagerModel model;

    public DefaultNPlatformModel(DefaultNWorkspaceEnvManagerModel model) {
        this.workspace = model.getWorkspace();
        this.model = model;
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }


    public boolean addPlatform(NPlatformLocation location) {
        return add0(location, true);
    }

    public boolean add0(NPlatformLocation location, boolean notify) {
//        session = CoreNutsUtils.validate(session, workspace);
        NSession session=getWorkspace().currentSession();
        if (location != null) {
            NAssert.requireNonBlank(location.getProduct(), "platform location product");
            NAssert.requireNonBlank(location.getName(), "platform location product");
            NAssert.requireNonBlank(location.getVersion(), "platform location version");
            NAssert.requireNonBlank(location.getVersion(), "platform location path");
            List<NPlatformLocation> list = getPlatforms().get(location.getPlatformType());
            if (list == null) {
                list = new ArrayList<>();
                model.getConfigPlatforms().put(location.getPlatformType(), list);
            }
            NPlatformLocation old = null;
            for (NPlatformLocation nutsPlatformLocation : list) {
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
                    session.out().resetLine().println(NMsg.ofC("%s %s %s (%s) %s at %s",
                            NText.ofStyledSuccess("install"),
                            location.getId().getShortName(),
                            location.getPackaging(),
                            location.getProduct(),
                            NVersion.of(location.getVersion()).get(),
                            NPath.of(location.getPath())
                    ));
                }
                NWorkspaceExt.of(workspace)
                        .getConfigModel()
                        .fireConfigurationChanged("platform", ConfigEventType.MAIN);
            }
            return true;
        }
        return false;
    }

    public boolean updatePlatform(NPlatformLocation oldLocation, NPlatformLocation newLocation) {
        boolean updated = false;
        updated |= removePlatform(oldLocation);
        updated |= removePlatform(newLocation);
        updated |= addPlatform(newLocation);
        return updated;
    }

    public boolean removePlatform(NPlatformLocation location) {
        if (location != null) {
            List<NPlatformLocation> list = getPlatforms().get(location.getPlatformType());
            if (list != null) {
                if (list.remove(location)) {
                    NWorkspaceExt.of(workspace)
                            .getConfigModel()
                            .fireConfigurationChanged("platform", ConfigEventType.MAIN);
                    return true;
                }
            }
        }
        return false;
    }

    public NOptional<NPlatformLocation> findPlatformByName(NPlatformFamily type, String locationName) {
        return findOnePlatform(type, location -> location.getName().equals(locationName));
    }

    public NOptional<NPlatformLocation> findPlatformByPath(NPlatformFamily type, NPath path) {
        NAssert.requireNonNull(path,"path");
        return findOnePlatform(type, location -> location.getPath() != null && location.getPath().equals(path.toString()));
    }

    public NOptional<NPlatformLocation> findPlatformByVersion(NPlatformFamily type, String version) {
        return findOnePlatform(type, location -> location.getVersion().equals(version));
    }

    //    public void setRepositoryEnabled(String repoName, boolean enabled) {
//        NutsRepositoryRef e = repositoryRegistryHelper.findRepositoryRef(repoName);
//        if (e != null && e.isEnabled() != enabled) {
//            e.setEnabled(enabled);
//            fireConfigurationChanged();
//        }
//    }
    public NOptional<NPlatformLocation> findPlatform(NPlatformLocation location) {
        if (location == null) {
            return NOptional.ofNamedEmpty(NMsg.ofC("platform %s", location));
        }
        String type = location.getId().getArtifactId();
        NPlatformFamily ftype = NPlatformFamily.parse(type).orElse(NPlatformFamily.JAVA);
        List<NPlatformLocation> list = getPlatforms().get(ftype);
        if (list != null) {
            for (NPlatformLocation location2 : list) {
                if (location2.equals(location)) {
                    return NOptional.of(location2);
                }
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("platform %s", location));
    }

    public NOptional<NPlatformLocation> findPlatformByVersion(NPlatformFamily type, NVersionFilter versionFilter) {
        return findOnePlatform(type,
                location -> {

                    if (versionFilter == null) {
                        return true;
                    }
                    String sVersion = location.getVersion();
                    NVersion version = NVersion.of(sVersion).get();
                    if (versionFilter.acceptVersion(version)) {
                        return true;
                    }
                    // replace 1.6 by 6, and 1.8 by 8
                    if (type == NPlatformFamily.JAVA || location.getPlatformType() == NPlatformFamily.JAVA) {
                        int a = sVersion.indexOf('.');
                        if (a > 0) {
                            NLiteral p = NLiteral.of(sVersion.substring(0, a));
                            if (p.isInt() && p.asInt().get() == 1) {
                                String sVersion2 = sVersion.substring(a + 1);
                                NVersion version2 = NVersion.of(sVersion2).get();
                                if (versionFilter.acceptVersion(version2)) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
        );
    }

    public NStream<NPlatformLocation> searchSystemPlatforms(NPlatformFamily platformType) {
        if (platformType == NPlatformFamily.JAVA) {
            try {
                return NStream.of(NJavaSdkUtils.of(workspace).searchJdkLocationsFuture().get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return NStream.ofEmpty();
    }

    public NStream<NPlatformLocation> searchSystemPlatforms(NPlatformFamily platformType, NPath path) {
        if (platformType == NPlatformFamily.JAVA) {
            return NStream.of(NJavaSdkUtils.of(workspace).searchJdkLocations(path));
        }
        return NStream.ofEmpty();
    }

    public NOptional<NPlatformLocation> resolvePlatform(NPlatformFamily platformType, NPath path, String preferredName) {
        if (platformType == NPlatformFamily.JAVA) {
            NPlatformLocation z = NJavaSdkUtils.of(workspace).resolveJdkLocation(path, preferredName);
            if (z == null) {
                return NOptional.ofNamedEmpty(NMsg.ofC("%s platform at %s", platformType.id(), path));
            }
            return NOptional.of(z);
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("%s platform at %s", platformType.id(), path));
    }

    //
    public void setPlatforms(NPlatformLocation[] locations) {
        model.getConfigPlatforms().clear();
        for (NPlatformLocation platform : locations) {
            add0(platform, false);
        }
    }

    public NOptional<NPlatformLocation> findOnePlatform(NPlatformFamily type, Predicate<NPlatformLocation> filter) {
        NPlatformLocation[] a = findPlatforms(type, filter).toArray(NPlatformLocation[]::new);
        if (a.length == 0) {
            return NOptional.ofNamedEmpty(type.id() + " platform");
        }
        if (a.length == 1) {
            NPlatformLocation r = a[0];
            if (r == null) {
                return NOptional.ofNamedEmpty(type.id() + " platform");
            }
            return NOptional.of(r);
        }
        //find the best minimum version that is applicable!
        NPlatformLocation best = a[0];
        for (int i = 1; i < a.length; i++) {
            NVersion v1 = NVersion.of(best.getVersion()).get();
            NVersion v2 = NVersion.of(a[i].getVersion()).get();
            if (type == NPlatformFamily.JAVA) {
                double d1 = Double.parseDouble(JavaClassUtils.sourceVersionToClassVersion(v1.getValue()));
                double d2 = Double.parseDouble(JavaClassUtils.sourceVersionToClassVersion(v2.getValue()));
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
            } else {
                if (v1.compareTo(v2) > 0) {
                    best = a[i];
                }
            }
        }
        if (best == null) {
            return NOptional.ofNamedEmpty(type.id() + " platform");
        }
        return NOptional.of(best);
    }

    public NStream<NPlatformLocation> findPlatforms(NPlatformFamily type, Predicate<NPlatformLocation> filter) {
        NSession session=getWorkspace().currentSession();
        if (filter == null) {
            if (type == null) {
                List<NPlatformLocation> all = new ArrayList<>();
                for (List<NPlatformLocation> value : model.getConfigPlatforms().values()) {
                    all.addAll(value);
                }
                return NStream.of(all);
            }
            List<NPlatformLocation> list = getPlatforms().get(type);
            if (list == null) {
                return NStream.ofEmpty();
            }
            return NStream.of(list);
        }
        List<NPlatformLocation> ret = new ArrayList<>();
        if (type == null) {
            for (List<NPlatformLocation> found : getPlatforms().values()) {
                for (NPlatformLocation location : found) {
                    if (filter.test(location)) {
                        ret.add(location);
                    }
                }
            }
        } else {
            List<NPlatformLocation> found = getPlatforms().get(type);
            if (found != null) {
                for (NPlatformLocation location : found) {
                    if (filter.test(location)) {
                        ret.add(location);
                    }
                }
            }
        }
        if (!ret.isEmpty()) {
            ret.sort(new NPlatformLocationSelectComparator(session));
        }
        return NStream.of(ret);
    }

    public Map<NPlatformFamily, List<NPlatformLocation>> getPlatforms() {
        return model.getConfigPlatforms();
    }
}

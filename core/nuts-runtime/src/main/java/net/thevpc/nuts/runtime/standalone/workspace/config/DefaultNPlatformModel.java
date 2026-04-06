package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.artifact.NVersionFilter;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.platform.NExecutionEngineFamily;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.platform.NExecutionEngineLocation;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class DefaultNPlatformModel {

    private NWorkspaceModel wsModel;

    public DefaultNPlatformModel(NWorkspaceModel wsModel) {
        this.wsModel = wsModel;
    }


    public boolean addPlatform(NExecutionEngineLocation location) {
        return add0(location, true);
    }

    public boolean add0(NExecutionEngineLocation location, boolean notify) {
//        session = CoreNutsUtils.validate(session, workspace);
        if (location != null) {
            NAssert.requireNamedNonBlank(location.getProduct(), "platform location product");
            NAssert.requireNamedNonBlank(location.getName(), "platform location product");
            NAssert.requireNamedNonBlank(location.getVersion(), "platform location version");
            NAssert.requireNamedNonBlank(location.getVersion(), "platform location path");
            List<NExecutionEngineLocation> list = getPlatforms().get(location.getExecutionEngineFamily());
            if (list == null) {
                list = new ArrayList<>();
                wsModel.getConfigPlatforms().put(location.getExecutionEngineFamily(), list);
            }
            NExecutionEngineLocation old = null;
            for (NExecutionEngineLocation nutsPlatformLocation : list) {
                if (Objects.equals(nutsPlatformLocation.getProduct(), location.getProduct())
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
                if (NSession.of().isPlainTrace()) {
                    NOut.println(NMsg.ofC("%s %s %s %s (%s) %s at %s",
                            NText.ofStyledSuccess("install"),
                            location.getId().getShortName(),
                            location.getVendor(),
                            location.getProduct(),
                            location.getVariant(),
                            NVersion.get(location.getVersion()).get(),
                            NPath.of(location.getPath())
                    ));
                }
                NWorkspaceExt.of()
                        .getConfigModel()
                        .fireConfigurationChanged("platform", ConfigEventType.MAIN);
            }
            return true;
        }
        return false;
    }

    public boolean updatePlatform(NExecutionEngineLocation oldLocation, NExecutionEngineLocation newLocation) {
        boolean updated = false;
        updated |= removePlatform(oldLocation);
        updated |= removePlatform(newLocation);
        updated |= addPlatform(newLocation);
        return updated;
    }

    public boolean removePlatform(NExecutionEngineLocation location) {
        if (location != null) {
            List<NExecutionEngineLocation> list = getPlatforms().get(location.getExecutionEngineFamily());
            if (list != null) {
                if (list.remove(location)) {
                    NWorkspaceExt.of()
                            .getConfigModel()
                            .fireConfigurationChanged("platform", ConfigEventType.MAIN);
                    return true;
                }
            }
        }
        return false;
    }

    public NOptional<NExecutionEngineLocation> findPlatformByName(NExecutionEngineFamily type, String locationName) {
        return findOneExecutionEngine(type, location -> location.getName().equals(locationName));
    }

    public NOptional<NExecutionEngineLocation> findPlatformByPath(NExecutionEngineFamily type, NPath path) {
        NAssert.requireNamedNonNull(path, "path");
        return findOneExecutionEngine(type, location -> location.getPath() != null && location.getPath().equals(path.toString()));
    }

    public NOptional<NExecutionEngineLocation> findPlatformByVersion(NExecutionEngineFamily type, String version) {
        return findOneExecutionEngine(type, location -> location.getVersion().equals(version));
    }

    //    public void setRepositoryEnabled(String repoName, boolean enabled) {
//        NutsRepositoryRef e = repositoryRegistryHelper.findRepositoryRef(repoName);
//        if (e != null && e.isEnabled() != enabled) {
//            e.setEnabled(enabled);
//            fireConfigurationChanged();
//        }
//    }
    public NOptional<NExecutionEngineLocation> findPlatform(NExecutionEngineLocation location) {
        if (location == null) {
            return NOptional.ofNamedEmpty(NMsg.ofC("platform %s", location));
        }
        String type = location.getId().getArtifactId();
        NExecutionEngineFamily ftype = NExecutionEngineFamily.parse(type).orElse(NExecutionEngineFamily.JAVA);
        List<NExecutionEngineLocation> list = getPlatforms().get(ftype);
        if (list != null) {
            for (NExecutionEngineLocation location2 : list) {
                if (location2.equals(location)) {
                    return NOptional.of(location2);
                }
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("platform %s", location));
    }

    public NOptional<NExecutionEngineLocation> findPlatformByVersion(NExecutionEngineFamily executionEngineType, NVersionFilter versionFilter) {
        return findOneExecutionEngine(executionEngineType,
                location -> {

                    if (versionFilter == null) {
                        return true;
                    }
                    String sVersion = location.getVersion();
                    NVersion version = NVersion.get(sVersion).get();
                    if (versionFilter.acceptVersion(version)) {
                        return true;
                    }
                    // replace 1.6 by 6, and 1.8 by 8
                    if (executionEngineType == NExecutionEngineFamily.JAVA || location.getExecutionEngineFamily() == NExecutionEngineFamily.JAVA) {
                        int a = sVersion.indexOf('.');
                        if (a > 0) {
                            NLiteral p = NLiteral.of(sVersion.substring(0, a));
                            if (p.asInt().isPresent() && p.asInt().get() == 1) {
                                String sVersion2 = sVersion.substring(a + 1);
                                NVersion version2 = NVersion.get(sVersion2).get();
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

    public NStream<NExecutionEngineLocation> searchSystemExecutionEngines(NExecutionEngineFamily executionEngineType) {
        if (executionEngineType == NExecutionEngineFamily.JAVA) {
            try {
                return NStream.ofArray(NJavaSdkUtils.of().searchJdkLocationsFuture().get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return NStream.ofEmpty();
    }

    public NStream<NExecutionEngineLocation> searchSystemExecutionEngines(NExecutionEngineFamily executionEngineType, NPath path) {
        if (executionEngineType == NExecutionEngineFamily.JAVA) {
            return NStream.ofArray(NJavaSdkUtils.of().searchJdkLocations(path));
        }
        return NStream.ofEmpty();
    }

    public NOptional<NExecutionEngineLocation> resolveExecutionEngine(NExecutionEngineFamily executionEngineType, NPath path, String preferredName) {
        if (executionEngineType == NExecutionEngineFamily.JAVA) {
            NExecutionEngineLocation z = NJavaSdkUtils.of().resolveJdkLocation(path, preferredName);
            if (z == null) {
                return NOptional.ofNamedEmpty(NMsg.ofC("%s platform at %s", executionEngineType.id(), path));
            }
            return NOptional.of(z);
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("%s platform at %s", executionEngineType.id(), path));
    }

    //
    public void setExecutionEngines(NExecutionEngineLocation[] locations) {
        wsModel.getConfigPlatforms().clear();
        for (NExecutionEngineLocation platform : locations) {
            add0(platform, false);
        }
    }

    public NOptional<NExecutionEngineLocation> findOneExecutionEngine(NExecutionEngineFamily executionEngineType, Predicate<NExecutionEngineLocation> filter) {
        NExecutionEngineLocation[] a = findPlatforms(executionEngineType, filter).toArray(NExecutionEngineLocation[]::new);
        if (a.length == 0) {
            return NOptional.ofNamedEmpty(executionEngineType.id() + " platform");
        }
        if (a.length == 1) {
            NExecutionEngineLocation r = a[0];
            if (r == null) {
                return NOptional.ofNamedEmpty(executionEngineType.id() + " platform");
            }
            return NOptional.of(r);
        }
        //find the best minimum version that is applicable!
        NExecutionEngineLocation best = a[0];
        for (int i = 1; i < a.length; i++) {
            NVersion v1 = NVersion.get(best.getVersion()).get();
            NVersion v2 = NVersion.get(a[i].getVersion()).get();
            if (executionEngineType == NExecutionEngineFamily.JAVA) {
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
            return NOptional.ofNamedEmpty(executionEngineType.id() + " platform");
        }
        return NOptional.of(best);
    }

    public NStream<NExecutionEngineLocation> findPlatforms(NExecutionEngineFamily type, Predicate<NExecutionEngineLocation> filter) {
        NJavaSdkUtils nJavaSdkUtils = NJavaSdkUtils.of();
        NExecutionEngineLocation current = nJavaSdkUtils.getHostJvm();
        if (filter == null) {
            if (type == null) {
                List<NExecutionEngineLocation> list = new ArrayList<>();
                for (List<NExecutionEngineLocation> value : wsModel.getConfigPlatforms().values()) {
                    list.addAll(value);
                }
                if (!list.contains(current)) {
                    list.add(current);
                }
                return NStream.ofIterable(list);
            }
            List<NExecutionEngineLocation> list = getPlatforms().get(type);
            if (list == null) {
                list = new ArrayList<>();
                getPlatforms().put(type, list);
            }
            if (!list.contains(current)) {
                list.add(current);
            }
            return NStream.ofIterable(list);
        }
        List<NExecutionEngineLocation> ret = new ArrayList<>();
        if (type == null) {
            for (List<NExecutionEngineLocation> found : getPlatforms().values()) {
                for (NExecutionEngineLocation location : found) {
                    if (filter.test(location)) {
                        ret.add(location);
                    }
                }
            }
        } else {
            List<NExecutionEngineLocation> found = getPlatforms().get(type);
            if (found != null) {
                for (NExecutionEngineLocation location : found) {
                    if (filter.test(location)) {
                        ret.add(location);
                    }
                }
            }
        }
        if(filter.test(current)) {
            if (!ret.contains(current)) {
                ret.add(current);
            }
        }
        if (!ret.isEmpty()) {
            ret.sort(new NExecutionEngineLocationSelectComparator());
        }
        return NStream.ofIterable(ret);
    }

    public Map<NExecutionEngineFamily, List<NExecutionEngineLocation>> getPlatforms() {
        return wsModel.getConfigPlatforms();
    }
}

package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.env.NPlatformFamily;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.text.NTexts;
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


    public boolean addPlatform(NPlatformLocation location, NSession session) {
        return add0(location, session, true);
    }

    public boolean add0(NPlatformLocation location, NSession session, boolean notify) {
//        session = CoreNutsUtils.validate(session, workspace);
        if (location != null) {
            NAssert.requireNonBlank(location.getProduct(), "platform location product", session);
            NAssert.requireNonBlank(location.getName(), "platform location product", session);
            NAssert.requireNonBlank(location.getVersion(), "platform location version", session);
            NAssert.requireNonBlank(location.getVersion(), "platform location path", session);
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
                            NTexts.of(session).ofStyled("install", NTextStyles.of(NTextStyle.success())),
                            location.getId().getShortName(),
                            location.getPackaging(),
                            location.getProduct(),
                            NVersion.of(location.getVersion()).get(session),
                            NPath.of(location.getPath(), session)
                    ));
                }
                NConfigsExt.of(NConfigs.of(session))
                        .getModel()
                        .fireConfigurationChanged("platform", session, ConfigEventType.MAIN);
            }
            return true;
        }
        return false;
    }

    public boolean updatePlatform(NPlatformLocation oldLocation, NPlatformLocation newLocation, NSession session) {
        boolean updated = false;
        updated |= removePlatform(oldLocation, session);
        updated |= removePlatform(newLocation, session);
        updated |= addPlatform(newLocation, session);
        return updated;
    }

    public boolean removePlatform(NPlatformLocation location, NSession session) {
        if (location != null) {
            List<NPlatformLocation> list = getPlatforms().get(location.getPlatformType());
            if (list != null) {
                if (list.remove(location)) {
                    NConfigsExt.of(NConfigs.of(session))
                            .getModel()
                            .fireConfigurationChanged("platform", session, ConfigEventType.MAIN);
                    return true;
                }
            }
        }
        return false;
    }

    public NOptional<NPlatformLocation> findPlatformByName(NPlatformFamily type, String locationName, NSession session) {
        return findOnePlatform(type, location -> location.getName().equals(locationName), session);
    }

    public NOptional<NPlatformLocation> findPlatformByPath(NPlatformFamily type, String path, NSession session) {
        return findOnePlatform(type, location -> location.getPath() != null && location.getPath().equals(path.toString()), session);
    }

    public NOptional<NPlatformLocation> findPlatformByVersion(NPlatformFamily type, String version, NSession session) {
        return findOnePlatform(type, location -> location.getVersion().equals(version), session);
    }

    //    public void setRepositoryEnabled(String repoName, boolean enabled) {
//        NutsRepositoryRef e = repositoryRegistryHelper.findRepositoryRef(repoName);
//        if (e != null && e.isEnabled() != enabled) {
//            e.setEnabled(enabled);
//            fireConfigurationChanged();
//        }
//    }
    public NOptional<NPlatformLocation> findPlatform(NPlatformLocation location, NSession session) {
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

    public NOptional<NPlatformLocation> findPlatformByVersion(NPlatformFamily type, NVersionFilter versionFilter, final NSession session) {
        return findOnePlatform(type,
                location -> {
                    if (versionFilter == null) {
                        return true;
                    }
                    String sVersion = location.getVersion();
                    NVersion version = NVersion.of(sVersion).get(session);
                    if (versionFilter.acceptVersion(version, session)) {
                        return true;
                    }
                    // replace 1.6 by 6, and 1.8 by 8
                    if (type == NPlatformFamily.JAVA || location.getPlatformType() == NPlatformFamily.JAVA) {
                        int a = sVersion.indexOf('.');
                        if (a > 0) {
                            NLiteral p = NLiteral.of(sVersion.substring(0, a));
                            if (p.isInt() && p.asInt().get() == 1) {
                                String sVersion2 = sVersion.substring(a + 1);
                                NVersion version2 = NVersion.of(sVersion2).get(session);
                                if (versionFilter.acceptVersion(version2, session)) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                },
                session);
    }

    public NStream<NPlatformLocation> searchSystemPlatforms(NPlatformFamily platformType, NSession session) {
        NSessionUtils.checkSession(workspace, session);
        if (platformType == NPlatformFamily.JAVA) {
            try {
                return NStream.of(NJavaSdkUtils.of(session).searchJdkLocationsFuture(session).get(), session);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return NStream.ofEmpty(session);
    }

    public NStream<NPlatformLocation> searchSystemPlatforms(NPlatformFamily platformType, String path, NSession session) {
        NSessionUtils.checkSession(workspace, session);
        if (platformType == NPlatformFamily.JAVA) {
            return NStream.of(NJavaSdkUtils.of(session).searchJdkLocations(path, session), session);
        }
        return NStream.ofEmpty(session);
    }

    public NOptional<NPlatformLocation> resolvePlatform(NPlatformFamily platformType, String path, String preferredName, NSession session) {
        NSessionUtils.checkSession(workspace, session);
        if (platformType == NPlatformFamily.JAVA) {
            NPlatformLocation z = NJavaSdkUtils.of(session).resolveJdkLocation(path, preferredName, session);
            if (z == null) {
                return NOptional.ofNamedEmpty(NMsg.ofC("%s platform at %s", platformType.id(), path));
            }
            return NOptional.of(z);
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("%s platform at %s", platformType.id(), path));
    }

    //
    public void setPlatforms(NPlatformLocation[] locations, NSession session) {
        model.getConfigPlatforms().clear();
        for (NPlatformLocation platform : locations) {
            add0(platform, session, false);
        }
    }

    public NOptional<NPlatformLocation> findOnePlatform(NPlatformFamily type, Predicate<NPlatformLocation> filter, NSession session) {
        NPlatformLocation[] a = findPlatforms(type, filter, session).toArray(NPlatformLocation[]::new);
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
            NVersion v1 = NVersion.of(best.getVersion()).get(session);
            NVersion v2 = NVersion.of(a[i].getVersion()).get(session);
            if (type == NPlatformFamily.JAVA) {
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

    public NStream<NPlatformLocation> findPlatforms(NPlatformFamily type, Predicate<NPlatformLocation> filter, NSession session) {
        if (filter == null) {
            if (type == null) {
                List<NPlatformLocation> all = new ArrayList<>();
                for (List<NPlatformLocation> value : model.getConfigPlatforms().values()) {
                    all.addAll(value);
                }
                return NStream.of(all, session);
            }
            List<NPlatformLocation> list = getPlatforms().get(type);
            if (list == null) {
                return NStream.ofEmpty(session);
            }
            return NStream.of(list, session);
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
        return NStream.of(ret, session);
    }

    public Map<NPlatformFamily, List<NPlatformLocation>> getPlatforms() {
        return model.getConfigPlatforms();
    }
}

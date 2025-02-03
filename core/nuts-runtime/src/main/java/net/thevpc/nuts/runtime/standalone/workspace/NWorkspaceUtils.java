/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.reflect.NReflectConfigurationBuilder;
import net.thevpc.nuts.reflect.NReflectPropertyAccessStrategy;
import net.thevpc.nuts.reflect.NReflectPropertyDefaultValueStrategy;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.NPrintIterator;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryHelper;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.util.reflect.*;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NRepositoryAndFetchMode;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class NWorkspaceUtils {

    private final NWorkspace workspace;

    private NWorkspaceUtils(NWorkspace workspace) {
        this.workspace = workspace;
    }

    public static NWorkspaceUtils of() {
        return of(NWorkspace.of());
    }

    public static NWorkspaceUtils of(NWorkspace workspace) {
        return new NWorkspaceUtils(workspace);
    }

    public static NSession bindSession(NWorkspace ws, NSession session) {
        if (ws != null && session != null && !Objects.equals(session.getWorkspace().getUuid(), ws.getUuid())) {
            return ws.createSession().setAll(session);
        }
        return session;
    }

    public static boolean isUserDefaultWorkspace() {
        String defaultWorkspaceLocation = NPlatformHome.USER.getWorkspaceLocation(null);
        return defaultWorkspaceLocation.equals(NWorkspace.of().getLocation().toString());
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(NWorkspaceUtils.class);
    }

    public NRepositorySPI repoSPI(NRepository repo) {
        return NWorkspaceExt.of().getRepositoryModel().toRepositorySPI(repo);
    }

    public NReflectRepository getReflectRepository() {
        //do not call env.getProperty(...). It will end up with a stack overflow
        NReflectRepository o = (NReflectRepository) (workspace.getProperties().get(NReflectRepository.class.getName()));
        if (o == null) {
            o = new DefaultNReflectRepository(workspace,NReflectConfigurationBuilder.of()
                    .setPropertyAccessStrategy(NReflectPropertyAccessStrategy.FIELD)
                    .setPropertyDefaultValueStrategy(NReflectPropertyDefaultValueStrategy.PROPERTY_DEFAULT)
                    .build());
            workspace.setProperty(NReflectRepository.class.getName(), o);
        }
        return o;
    }

    public NId createSdkId(String type, String version) {
        NAssert.requireNonBlank(type, "sdk type");
        NAssert.requireNonBlank(version, "version");
        if ("java".equalsIgnoreCase(type)) {
            return NJavaSdkUtils.of(workspace).createJdkId(version);
        } else {
            return NIdBuilder.of().setArtifactId(type)
                    .setVersion(version)
                    .build();
        }
    }

    public void checkReadOnly() {
        if (NWorkspace.of().isReadOnly()) {
            throw new NReadOnlyException(NWorkspace.of().getWorkspaceLocation().toString());
        }
    }



    public NId configureFetchEnv(NId id) {
        Map<String, String> qm = id.getProperties();
        if (qm.get(NConstants.IdProperties.FACE) == null
                && qm.get(NConstants.IdProperties.ARCH) == null
                && qm.get(NConstants.IdProperties.OS) == null
                && qm.get(NConstants.IdProperties.OS_DIST) == null
                && qm.get(NConstants.IdProperties.PLATFORM) == null
                && qm.get(NConstants.IdProperties.DESKTOP) == null
        ) {
            qm.put(NConstants.IdProperties.ARCH, workspace.getArchFamily().id());
            qm.put(NConstants.IdProperties.OS, workspace.getOs().toString());
            if (workspace.getOsDist() != null) {
                qm.put(NConstants.IdProperties.OS_DIST, workspace.getOsDist().toString());
            }
            if (workspace.getPlatform() != null) {
                qm.put(NConstants.IdProperties.PLATFORM, workspace.getPlatform().toString());
            }
            if (workspace.getDesktopEnvironment() != null) {
                qm.put(NConstants.IdProperties.DESKTOP, workspace.getDesktopEnvironment().toString());
            }
            return id.builder().setProperties(qm).build();
        }
        return id;
    }

    public List<NRepository> filterRepositoriesDeploy(NId id, NRepositoryFilter repositoryFilter) {
        NRepositoryFilter f = NRepositoryFilters.of().installedRepo().neg().and(repositoryFilter);
        return filterRepositories(NRepositorySupportedAction.DEPLOY, id, f, NFetchMode.LOCAL);
    }

    public List<NRepositoryAndFetchMode> filterRepositoryAndFetchModes(
            NRepositorySupportedAction fmode, NId id, NRepositoryFilter repositoryFilter, NFetchStrategy fetchStrategy) {
        List<NRepositoryAndFetchMode> ok = new ArrayList<>();
        for (NFetchMode nutsFetchMode : fetchStrategy) {
            for (NRepository nRepositoryAndFetchMode : filterRepositories(fmode, id, repositoryFilter, nutsFetchMode)) {
                ok.add(new NRepositoryAndFetchMode(nRepositoryAndFetchMode, nutsFetchMode));
            }
        }
        return ok;
    }

    private List<NRepository> filterRepositories(NRepositorySupportedAction fmode, NId id, NRepositoryFilter repositoryFilter, NFetchMode mode) {
        return filterRepositories(fmode, id, repositoryFilter, true, null, mode);
    }

    private List<NRepository> filterRepositories(NRepositorySupportedAction fmode, NId id, NRepositoryFilter repositoryFilter, boolean sortByLevelDesc, final Comparator<NRepository> postComp, NFetchMode mode) {
        List<RepoAndLevel> repos2 = new ArrayList<>();
        //        List<Integer> reposLevels = new ArrayList<>();

        NSession session = workspace.currentSession();
        for (NRepository repository : workspace.getRepositories()) {
            /*repository.isAvailable()*/
            if (repository.isEnabled()
                    && (fmode == NRepositorySupportedAction.SEARCH || repository.isSupportedDeploy())
                    && repoSPI(repository).isAcceptFetchMode(mode)
                    && (repositoryFilter == null || repositoryFilter.acceptRepository(repository))) {
                int d = 0;
                if (fmode == NRepositorySupportedAction.DEPLOY) {
                    try {
                        d = NRepositoryHelper.getSupportDeployLevel(repository, fmode, id, mode, session.isTransitive());
                    } catch (Exception ex) {
                        _LOGOP().level(Level.FINE).error(ex)
                                .log(NMsg.ofJ("unable to resolve support deploy level for : {0}", repository.getName()));
                    }
                }
                NSpeedQualifier t = NSpeedQualifier.NORMAL;
                try {
                    t = NRepositoryHelper.getSupportSpeedLevel(repository, fmode, id, mode, session.isTransitive());
                } catch (Exception ex) {
                    _LOGOP().level(Level.FINE).error(ex)
                            .log(NMsg.ofJ("unable to resolve support speed level for : {0}", repository.getName()));
                }
                if (t != NSpeedQualifier.UNAVAILABLE) {
                    repos2.add(new RepoAndLevel(repository, d, t, postComp));
                }
            }
        }
        if (sortByLevelDesc || postComp != null) {
            Collections.sort(repos2);
        }

        List<NRepository> ret = new ArrayList<>();
        NInstalledRepository installedRepository = NWorkspaceExt.of().getInstalledRepository();
        if (mode == NFetchMode.LOCAL && fmode == NRepositorySupportedAction.SEARCH
                &&
                (repositoryFilter == null || repositoryFilter.acceptRepository(installedRepository))) {
            ret.add(installedRepository);
        }
        for (RepoAndLevel repoAndLevel : repos2) {
            ret.add(repoAndLevel.r);
        }
        return ret;
    }

    public void validateRepositoryName(String repositoryName, Set<String> registered) {
        if (!repositoryName.matches("[a-zA-Z][.a-zA-Z0-9_-]*")) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid repository id %s", repositoryName));
        }
        if (registered.contains(repositoryName)) {
            throw new NRepositoryAlreadyRegisteredException(repositoryName);
        }
    }

    public <T> NIterator<T> decoratePrint(NIterator<T> it, NFetchDisplayOptions displayOptions) {
        return new NPrintIterator<>(it, workspace, NOut.out(), displayOptions);
    }

    public Events events() {
        return new Events(this);
    }

    public void installAllJVM() {
        NSession session = workspace.currentSession();
        try {
            if (session.isPlainTrace()) {
                NOut.resetLine().println("looking for java installations in default locations...");
            }
            List<NPlatformLocation> found = workspace.searchSystemPlatforms(NPlatformFamily.JAVA).toList();
            int someAdded = 0;
            for (NPlatformLocation java : found) {
                if (workspace.addPlatform(java)) {
                    someAdded++;
                }
            }
            if (session.isPlainTrace()) {
                if (someAdded == 0) {
                    NOut.println(NMsg.ofC("%s java installation locations found...", NMsg.ofStyledError("no new")));
                } else if (someAdded == 1) {
                    NOut.println(NMsg.ofC("%s new java installation location added...", NMsg.ofStyledPrimary2("1")));
                } else {
                    NOut.println(NMsg.ofC("%s new java installation locations added...", NMsg.ofStyledPrimary2("" + someAdded)));
                }
                NOut.println("you can always add another installation manually using 'nuts settings add java' command.");
            }
            if (!workspace.isReadOnly()) {
                workspace.saveConfig();
            }
        } catch (Exception ex) {
            _LOG().with().level(Level.FINEST).verb(NLogVerb.WARNING).error(ex)
                    .log(NMsg.ofJ("unable to resolve default JRE/JDK locations : {0}", ex));
            if (session.isPlainTrace()) {
                NPrintStream out = session.out();
                out.resetLine();
                out.println(NMsg.ofC("%s :  %s",
                        NMsg.ofStyledError("unable to resolve default JRE/JDK locations"),
                        ex));
            }
        }
    }

    public void installCurrentJVM() {
        NSession session = workspace.currentSession();
        try {
            if (session.isPlainTrace()) {
                NOut.resetLine().println("configuring current JVM...");
            }
            NPlatformLocation found0 = workspace.resolvePlatform(NPlatformFamily.JAVA, NPath.of(System.getProperty("java.home")), null).orNull();
            NPlatformLocation[] found = found0 == null ? new NPlatformLocation[0] : new NPlatformLocation[]{found0};
            int someAdded = 0;
            for (NPlatformLocation java : found) {
                if (workspace.addPlatform(java)) {
                    someAdded++;
                }
            }
            if (session.isPlainTrace()) {
                if (someAdded == 0) {
                    NOut.println(NMsg.ofC("%s java installation locations found...", NMsg.ofStyledError("no new")));
                }
            }
            if (!workspace.isReadOnly()) {
                workspace.saveConfig();
            }
        } catch (Exception ex) {
            _LOG().with().level(Level.FINEST).verb(NLogVerb.WARNING).error(ex)
                    .log(NMsg.ofJ("unable to resolve default JRE/JDK locations : {0}", ex));
            if (session.isPlainTrace()) {
                NPrintStream out = session.out();
                out.resetLine().println(NMsg.ofC("Ms :  %s", NMsg.ofStyledError("unable to resolve default JRE/JDK locations"), ex));
            }
        }
    }

    public void installScriptsAndLaunchers(boolean includeGraphicalLaunchers) {
        NSession session = workspace.currentSession();
        try {
            workspace.addLauncher(
                    new NLauncherOptions()
                            .setId(session.getWorkspace().getApiId())
                            .setCreateScript(true)
                            .setSwitchWorkspace(
                                    NWorkspace.of().getBootOptions().getSwitchWorkspace().orNull()
                            )
                            .setCreateDesktopLauncher(includeGraphicalLaunchers ? NSupportMode.PREFERRED : NSupportMode.NEVER)
                            .setCreateMenuLauncher(includeGraphicalLaunchers ? NSupportMode.SUPPORTED : NSupportMode.NEVER)
            );
        } catch (Exception ex) {
            _LOG().with().level(Level.FINEST).verb(NLogVerb.WARNING).error(ex)
                    .log(NMsg.ofJ("unable to install desktop launchers : {0}", ex));
            if (session.isPlainTrace()) {
                NPrintStream out = session.out();
                out.resetLine().println(NMsg.ofC("%s :  %s",
                        NMsg.ofStyledError("unable to install desktop launchers"),
                        ex));
            }
        }
    }

    public void installCompanions() {
        NSession session = workspace.currentSession();
        NTexts text = NTexts.of();
        Set<NId> companionIds = NExtensions.of().getCompanionIds();
        if (companionIds.isEmpty()) {
            return;
        }
        if (session.isPlainTrace()) {
            NPrintStream out = session.out();
            out.resetLine().println(NMsg.ofC("looking for recommended companion tools to install... detected : %s",
                    text.ofBuilder().appendJoined(text.ofPlain(","),
                            companionIds
                    ))
            );
        }
        try {
            NInstallCmd.of().companions()
                    .run();
        } catch (Exception ex) {
            _LOG().with().level(Level.FINEST).verb(NLogVerb.WARNING).error(ex)
                    .log(NMsg.ofJ("unable to install companions : {0}", ex));
            if (session.isPlainTrace()) {
                NPrintStream out = session.out();
                out.resetLine().println(NMsg.ofC("%s :  %s "
                                + "this happens when none of the following repositories are able to locate them : %s\n",
                        NMsg.ofStyledError("unable to install companion tools"),
                        ex,
                        text.ofBuilder().appendJoined(text.ofPlain(", "),
                                workspace.getRepositories().stream().map(x
                                        -> text.ofBuilder().append(x.getName(), NTextStyle.primary3())
                                ).collect(Collectors.toList())
                        )
                ));
            }
        }
    }

    private static class RepoAndLevel implements Comparable<RepoAndLevel> {

        NRepository r;
        int deployWeight;
        NSpeedQualifier speed;
        Comparator<NRepository> postComp;

        public RepoAndLevel(NRepository r, int deployWeight, NSpeedQualifier speed, Comparator<NRepository> postComp) {
            super();
            this.r = r;
            this.deployWeight = deployWeight;
            this.speed = speed;
            this.postComp = postComp;
        }

        @Override
        public int compareTo(RepoAndLevel o2) {
            int x = Integer.compare(this.deployWeight, o2.deployWeight);
            if (x != 0) {
                return x;
            }
            x = Integer.compare(o2.speed.ordinal(), this.speed.ordinal());
            if (x != 0) {
                return x;
            }
            if (postComp != null) {
                x = postComp.compare(this.r, o2.r);
            }
            return x;
        }
    }

    public static class Events {

        private final NWorkspaceUtils u;

        public Events(NWorkspaceUtils u) {
            this.u = u;
        }

        public void fireOnInstall(NInstallEvent event) {
            u._LOGOP().level(Level.FINEST).verb(NLogVerb.ADD)
                    .log(NMsg.ofJ("installed {0}", event.getDefinition().getId()));
            for (NInstallListener listener : event.getWorkspace().getInstallListeners()) {
                listener.onInstall(event);
            }
            for (NInstallListener listener : event.getSession().getListeners(NInstallListener.class)) {
                listener.onInstall(event);
            }
        }

        public void fireOnRequire(NInstallEvent event) {
            u._LOGOP().level(Level.FINEST).verb(NLogVerb.ADD)
                    .log(NMsg.ofJ("required {0}", event.getDefinition().getId()));
            for (NInstallListener listener : event.getWorkspace().getInstallListeners()) {
                listener.onRequire(event);
            }
            for (NInstallListener listener : event.getSession().getListeners(NInstallListener.class)) {
                listener.onRequire(event);
            }
        }

        public void fireOnUpdate(NUpdateEvent event) {
            if (u._LOG().isLoggable(Level.FINEST)) {
                if (event.getOldValue() == null) {
                    u._LOGOP().level(Level.FINEST).verb(NLogVerb.UPDATE)
                            .log(NMsg.ofJ("updated {0}", event.getNewValue().getId()));
                } else {
                    u._LOGOP().level(Level.FINEST).verb(NLogVerb.UPDATE)
                            .log(NMsg.ofJ("updated {0} (old is {1})",
                                    event.getOldValue().getId().getLongId(),
                                    event.getNewValue().getId().getLongId()));
                }
            }
            for (NInstallListener listener : event.getWorkspace().getInstallListeners()) {
                listener.onUpdate(event);
            }
            for (NInstallListener listener : event.getSession().getListeners(NInstallListener.class)) {
                listener.onUpdate(event);
            }
        }

        public void fireOnUninstall(NInstallEvent event) {
            if (u._LOG().isLoggable(Level.FINEST)) {
                u._LOGOP().level(Level.FINEST).verb(NLogVerb.REMOVE)
                        .log(NMsg.ofJ("uninstalled {0}", event.getDefinition().getId()));
            }
            for (NInstallListener listener : event.getWorkspace().getInstallListeners()) {
                listener.onUninstall(event);
            }
            for (NInstallListener listener : event.getSession().getListeners(NInstallListener.class)) {
                listener.onUninstall(event);
            }
        }

        public void fireOnAddRepository(NWorkspaceEvent event) {
            if (u._LOG().isLoggable(Level.CONFIG)) {
                u._LOGOP().level(Level.CONFIG).verb(NLogVerb.ADD)
                        .log(NMsg.ofJ("loaded repo ##{0}##", event.getRepository().getName()));
            }

            for (NWorkspaceListener listener : event.getWorkspace().getWorkspaceListeners()) {
                listener.onAddRepository(event);
            }
            for (NWorkspaceListener listener : event.getSession().getListeners(NWorkspaceListener.class)) {
                listener.onAddRepository(event);
            }
        }

        public void fireOnRemoveRepository(NWorkspaceEvent event) {
            if (u._LOG().isLoggable(Level.FINEST)) {
                u._LOGOP().level(Level.FINEST).verb(NLogVerb.REMOVE)
                        .log(NMsg.ofJ("unloaded repo ##{0}##", event.getRepository().getName()));
            }
            for (NWorkspaceListener listener : event.getWorkspace().getWorkspaceListeners()) {
                listener.onRemoveRepository(event);
            }
            for (NWorkspaceListener listener : event.getSession().getListeners(NWorkspaceListener.class)) {
                listener.onRemoveRepository(event);
            }
        }

    }
}

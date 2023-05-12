/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.NPrintIterator;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryHelper;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNRepositories;
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

    private final NWorkspace ws;
    private final NSession session;
    private NLog LOG;

    private NWorkspaceUtils(NSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    public static NWorkspaceUtils of(NSession session) {
        return new NWorkspaceUtils(session);
    }

    public static NSession bindSession(NWorkspace ws, NSession session) {
        if (ws != null && session != null && !Objects.equals(session.getWorkspace().getUuid(), ws.getUuid())) {
            return ws.createSession().setAll(session);
        }
        return session;
    }

    public static boolean isUserDefaultWorkspace(NSession session) {
        String defaultWorkspaceLocation = NPlatformHome.USER.getWorkspaceLocation(null);
        return defaultWorkspaceLocation.equals(session.getWorkspace().getLocation().toString());
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(NWorkspaceUtils.class, session);
        }
        return LOG;
    }

    public NRepositorySPI repoSPI(NRepository repo) {
        DefaultNRepositories repos = (DefaultNRepositories) NRepositories.of(session).setSession(session);
        return repos.getModel().toRepositorySPI(repo);
    }

    public NReflectRepository getReflectRepository() {
        NEnvs env = NEnvs.of(session);
        //do not call env.getProperty(...). It will end up with a stack overflow
        NReflectRepository o = (NReflectRepository) (env.getProperties().get(NReflectRepository.class.getName()));
        if (o == null) {
            o = new DefaultNReflectRepository(NReflectConfigurationBuilder.of(session)
                    .setPropertyAccessStrategy(NReflectPropertyAccessStrategy.FIELD)
                    .setPropertyDefaultValueStrategy(NReflectPropertyDefaultValueStrategy.PROPERTY_DEFAULT)
                    .build());
            env.setProperty(NReflectRepository.class.getName(), o);
        }
        return o;
    }

    public NId createSdkId(String type, String version) {
        NAssert.requireNonBlank(type, "sdk type", session);
        NAssert.requireNonBlank(version, "version", session);
        if ("java".equalsIgnoreCase(type)) {
            return NJavaSdkUtils.of(ws).createJdkId(version, session);
        } else {
            return NIdBuilder.of().setArtifactId(type)
                    .setVersion(version)
                    .build();
        }
    }

    public void checkReadOnly() {
        if (NConfigs.of(session).isReadOnly()) {
            throw new NReadOnlyException(session, NLocations.of(session).getWorkspaceLocation().toString());
        }
    }

    public NSession validateSession(NSession session) {
        if (session == null) {
            session = ws.createSession();
        } else {
            if (session.getWorkspace() != ws) {
                throw new NIllegalArgumentException(session, NMsg.ofPlain("session was created with a different Workspace"));
            }
        }
        return session;
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
            NEnvs env = NEnvs.of(session);
            qm.put(NConstants.IdProperties.ARCH, env.getArchFamily().id());
            qm.put(NConstants.IdProperties.OS, env.getOs().toString());
            if (env.getOsDist() != null) {
                qm.put(NConstants.IdProperties.OS_DIST, env.getOsDist().toString());
            }
            if (env.getPlatform() != null) {
                qm.put(NConstants.IdProperties.PLATFORM, env.getPlatform().toString());
            }
            if (env.getDesktopEnvironment() != null) {
                qm.put(NConstants.IdProperties.DESKTOP, env.getDesktopEnvironment().toString());
            }
            return id.builder().setProperties(qm).build();
        }
        return id;
    }

    public List<NRepository> filterRepositoriesDeploy(NId id, NRepositoryFilter repositoryFilter) {
        NRepositoryFilter f = NRepositoryFilters.of(session).installedRepo().neg().and(repositoryFilter);
        return filterRepositories(NRepositorySupportedAction.DEPLOY, id, f, NFetchMode.LOCAL);
    }

    public List<NRepositoryAndFetchMode> filterRepositoryAndFetchModes(
            NRepositorySupportedAction fmode, NId id, NRepositoryFilter repositoryFilter, NFetchStrategy fetchStrategy,
            NSession session) {
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

        for (NRepository repository : NRepositories.of(session).setSession(session).getRepositories()) {
            /*repository.isAvailable()*/
            if (repository.isEnabled(session)
                    && (fmode == NRepositorySupportedAction.SEARCH || repository.isSupportedDeploy(session))
                    && repoSPI(repository).isAcceptFetchMode(mode, session)
                    && (repositoryFilter == null || repositoryFilter.acceptRepository(repository))) {
                int d = 0;
                if (fmode == NRepositorySupportedAction.DEPLOY) {
                    try {
                        d = NRepositoryHelper.getSupportDeployLevel(repository, fmode, id, mode, session.isTransitive(), session);
                    } catch (Exception ex) {
                        _LOGOP(session).level(Level.FINE).error(ex)
                                .log(NMsg.ofJ("unable to resolve support deploy level for : {0}", repository.getName()));
                    }
                }
                NSpeedQualifier t = NSpeedQualifier.NORMAL;
                try {
                    t = NRepositoryHelper.getSupportSpeedLevel(repository, fmode, id, mode, session.isTransitive(), session);
                } catch (Exception ex) {
                    _LOGOP(session).level(Level.FINE).error(ex)
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
        NInstalledRepository installedRepository = NWorkspaceExt.of(ws).getInstalledRepository();
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

    public void validateRepositoryName(String repositoryName, Set<String> registered, NSession session) {
        if (!repositoryName.matches("[a-zA-Z][.a-zA-Z0-9_-]*")) {
            throw new NIllegalArgumentException(session, NMsg.ofC("invalid repository id %s", repositoryName));
        }
        if (registered.contains(repositoryName)) {
            throw new NRepositoryAlreadyRegisteredException(session, repositoryName);
        }
    }

    public <T> NIterator<T> decoratePrint(NIterator<T> it, NSession session, NFetchDisplayOptions displayOptions) {
        final NPrintStream out = validateSession(session).getTerminal().getOut();
        return new NPrintIterator<>(it, ws, out, displayOptions, session);
    }

    public Events events() {
        return new Events(this);
    }

    public void installAllJVM() {
        NEnvs env = NEnvs.of(session);
        NPlatforms platforms = NPlatforms.of(session);
        NConfigs config = NConfigs.of(session);
        try {
            if (session.isPlainTrace()) {
                session.out().resetLine().println("looking for java installations in default locations...");
            }
            List<NPlatformLocation> found = platforms.searchSystemPlatforms(NPlatformFamily.JAVA).toList();
            int someAdded = 0;
            for (NPlatformLocation java : found) {
                if (platforms.addPlatform(java)) {
                    someAdded++;
                }
            }
            if (session.isPlainTrace()) {
                if (someAdded == 0) {
                    session.out().println(NMsg.ofC("%s java installation locations found...", NMsg.ofStyled("no new", NTextStyle.error())));
                } else if (someAdded == 1) {
                    session.out().println(NMsg.ofC("%s new java installation location added...", NMsg.ofStyled("1", NTextStyle.primary2())));
                } else {
                    session.out().println(NMsg.ofC("%s new java installation locations added...", NMsg.ofStyled("" + someAdded, NTextStyle.primary2())));
                }
                session.out().println("you can always add another installation manually using 'nuts settings add java' command.");
            }
            if (!config.isReadOnly()) {
                config.save();
            }
        } catch (Exception ex) {
            _LOG(session).with().session(session).level(Level.FINEST).verb(NLogVerb.WARNING).error(ex)
                    .log(NMsg.ofJ("unable to resolve default JRE/JDK locations : {0}", ex));
            if (session.isPlainTrace()) {
                NPrintStream out = session.out();
                out.resetLine();
                out.println(NMsg.ofC("%s :  %s",
                        NMsg.ofStyled("unable to resolve default JRE/JDK locations", NTextStyle.error()),
                        ex));
            }
        }
    }

    public void installCurrentJVM() {
//at least add current vm
        NEnvs env = NEnvs.of(session);
        NConfigs config = NConfigs.of(session);
        NPlatforms platforms = NPlatforms.of(session);
        try {
            if (session.isPlainTrace()) {
                session.out().resetLine().println("configuring current JVM...");
            }
            NPlatformLocation found0 = platforms.resolvePlatform(NPlatformFamily.JAVA, System.getProperty("java.home"), null).orNull();
            NPlatformLocation[] found = found0 == null ? new NPlatformLocation[0] : new NPlatformLocation[]{found0};
            int someAdded = 0;
            for (NPlatformLocation java : found) {
                if (platforms.addPlatform(java)) {
                    someAdded++;
                }
            }
            if (session.isPlainTrace()) {
                if (someAdded == 0) {
                    session.out().println(NMsg.ofC("%s java installation locations found...", NMsg.ofStyled("no new", NTextStyle.error())));
                }
            }
            if (!config.isReadOnly()) {
                config.save();
            }
        } catch (Exception ex) {
            _LOG(session).with().session(session).level(Level.FINEST).verb(NLogVerb.WARNING).error(ex)
                    .log(NMsg.ofJ("unable to resolve default JRE/JDK locations : {0}", ex));
            if (session.isPlainTrace()) {
                NPrintStream out = session.out();
                out.resetLine().println(NMsg.ofC("Ms :  %s", NMsg.ofStyled("unable to resolve default JRE/JDK locations", NTextStyle.error()), ex));
            }
        }
    }

    public void installScriptsAndLaunchers(boolean includeGraphicalLaunchers) {
        NEnvs env = NEnvs.of(session);
        try {
            env.addLauncher(
                    new NLauncherOptions()
                            .setId(session.getWorkspace().getApiId())
                            .setCreateScript(true)
                            .setSwitchWorkspace(
                                    NBootManager.of(session).getBootOptions().getSwitchWorkspace().orNull()
                            )
                            .setCreateDesktopLauncher(includeGraphicalLaunchers ? NSupportMode.PREFERRED : NSupportMode.NEVER)
                            .setCreateMenuLauncher(includeGraphicalLaunchers ? NSupportMode.SUPPORTED : NSupportMode.NEVER)
            );
        } catch (Exception ex) {
            _LOG(session).with().session(session).level(Level.FINEST).verb(NLogVerb.WARNING).error(ex)
                    .log(NMsg.ofJ("unable to install desktop launchers : {0}", ex));
            if (session.isPlainTrace()) {
                NPrintStream out = session.out();
                out.resetLine().println(NMsg.ofC("%s :  %s",
                        NMsg.ofStyled("unable to install desktop launchers", NTextStyle.error()),
                        ex));
            }
        }
    }

    public void installCompanions() {
        NTexts text = NTexts.of(session);
        Set<NId> companionIds = session.extensions().getCompanionIds();
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
            NInstallCommand.of(session.copy().setTrace(session.isTrace() && session.isPlainOut())).companions()
                    .run();
        } catch (Exception ex) {
            _LOG(session).with().session(session).level(Level.FINEST).verb(NLogVerb.WARNING).error(ex)
                    .log(NMsg.ofJ("unable to install companions : {0}", ex));
            if (session.isPlainTrace()) {
                NPrintStream out = session.out();
                out.resetLine().println(NMsg.ofC("%s :  %s "
                                + "this happens when none of the following repositories are able to locate them : %s\n",
                        NMsg.ofStyled("unable to install companion tools", NTextStyle.error()),
                        ex,
                        text.ofBuilder().appendJoined(text.ofPlain(", "),
                                NRepositories.of(session).getRepositories().stream().map(x
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
            u._LOGOP(event.getSession()).level(Level.FINEST).verb(NLogVerb.ADD)
                    .log(NMsg.ofJ("installed {0}", event.getDefinition().getId()));
            for (NInstallListener listener : NEvents.of(event.getSession()).getInstallListeners()) {
                listener.onInstall(event);
            }
            for (NInstallListener listener : event.getSession().getListeners(NInstallListener.class)) {
                listener.onInstall(event);
            }
        }

        public void fireOnRequire(NInstallEvent event) {
            u._LOGOP(event.getSession()).level(Level.FINEST).verb(NLogVerb.ADD)
                    .log(NMsg.ofJ("required {0}", event.getDefinition().getId()));
            for (NInstallListener listener : NEvents.of(event.getSession()).getInstallListeners()) {
                listener.onRequire(event);
            }
            for (NInstallListener listener : event.getSession().getListeners(NInstallListener.class)) {
                listener.onRequire(event);
            }
        }

        public void fireOnUpdate(NUpdateEvent event) {
            if (u._LOG(event.getSession()).isLoggable(Level.FINEST)) {
                if (event.getOldValue() == null) {
                    u._LOGOP(event.getSession()).level(Level.FINEST).verb(NLogVerb.UPDATE)
                            .log(NMsg.ofJ("updated {0}", event.getNewValue().getId()));
                } else {
                    u._LOGOP(event.getSession()).level(Level.FINEST).verb(NLogVerb.UPDATE)
                            .log(NMsg.ofJ("updated {0} (old is {1})",
                                    event.getOldValue().getId().getLongId(),
                                    event.getNewValue().getId().getLongId()));
                }
            }
            for (NInstallListener listener : NEvents.of(event.getSession()).getInstallListeners()) {
                listener.onUpdate(event);
            }
            for (NInstallListener listener : event.getSession().getListeners(NInstallListener.class)) {
                listener.onUpdate(event);
            }
        }

        public void fireOnUninstall(NInstallEvent event) {
            if (u._LOG(event.getSession()).isLoggable(Level.FINEST)) {
                u._LOGOP(event.getSession()).level(Level.FINEST).verb(NLogVerb.REMOVE)
                        .log(NMsg.ofJ("uninstalled {0}", event.getDefinition().getId()));
            }
            for (NInstallListener listener : NEvents.of(event.getSession()).getInstallListeners()) {
                listener.onUninstall(event);
            }
            for (NInstallListener listener : event.getSession().getListeners(NInstallListener.class)) {
                listener.onUninstall(event);
            }
        }

        public void fireOnAddRepository(NWorkspaceEvent event) {
            if (u._LOG(event.getSession()).isLoggable(Level.CONFIG)) {
                u._LOGOP(event.getSession()).level(Level.CONFIG).verb(NLogVerb.ADD)
                        .log(NMsg.ofJ("added repo ##{0}##", event.getRepository().getName()));
            }

            for (NWorkspaceListener listener : NEvents.of(event.getSession()).getWorkspaceListeners()) {
                listener.onAddRepository(event);
            }
            for (NWorkspaceListener listener : event.getSession().getListeners(NWorkspaceListener.class)) {
                listener.onAddRepository(event);
            }
        }

        public void fireOnRemoveRepository(NWorkspaceEvent event) {
            if (u._LOG(event.getSession()).isLoggable(Level.FINEST)) {
                u._LOGOP(event.getSession()).level(Level.FINEST).verb(NLogVerb.REMOVE)
                        .log(NMsg.ofJ("removed repo ##{0}##", event.getRepository().getName()));
            }
            for (NWorkspaceListener listener : NEvents.of(event.getSession()).getWorkspaceListeners()) {
                listener.onRemoveRepository(event);
            }
            for (NWorkspaceListener listener : event.getSession().getListeners(NWorkspaceListener.class)) {
                listener.onRemoveRepository(event);
            }
        }

    }
}

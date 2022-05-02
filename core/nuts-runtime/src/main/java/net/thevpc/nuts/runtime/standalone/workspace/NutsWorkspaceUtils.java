/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.NutsPrintIterator;
import net.thevpc.nuts.runtime.standalone.repository.NutsRepositoryHelper;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NutsRepositorySupportedAction;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNutsRepositoryManager;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.util.jclass.NutsJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.util.reflect.*;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NutsRepositoryAndFetchMode;
import net.thevpc.nuts.spi.NutsRepositorySPI;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class NutsWorkspaceUtils {

    private final NutsWorkspace ws;
    private final NutsSession session;
    private NutsLogger LOG;

    private NutsWorkspaceUtils(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    public static NutsWorkspaceUtils of(NutsSession session) {
        return new NutsWorkspaceUtils(session);
    }

    public static NutsSession bindSession(NutsWorkspace ws, NutsSession session) {
        if (ws != null && session != null && !Objects.equals(session.getWorkspace().getUuid(), ws.getUuid())) {
            return ws.createSession().copyFrom(session);
        }
        return session;
    }

    public static boolean isUserDefaultWorkspace(NutsSession session) {
        String defaultWorkspaceLocation = NutsUtilPlatforms.getWorkspaceLocation(null, false, null);
        return defaultWorkspaceLocation.equals(session.getWorkspace().getLocation().toString());
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(NutsWorkspaceUtils.class, session);
        }
        return LOG;
    }

    public NutsRepositorySPI repoSPI(NutsRepository repo) {
        DefaultNutsRepositoryManager repos = (DefaultNutsRepositoryManager) session.repos().setSession(session);
        return repos.getModel().toRepositorySPI(repo);
    }

    public ReflectRepository getReflectRepository() {
        NutsWorkspaceEnvManager env = session.env();
        //do not call env.getProperty(...). It will end up with a stack overflow
        ReflectRepository o = (ReflectRepository) (env.getProperties().get(ReflectRepository.class.getName()));
        if (o == null) {
            o = new DefaultReflectRepository(ReflectConfigurationBuilder.create()
                    .setPropertyAccessStrategy(ReflectPropertyAccessStrategy.FIELD)
                    .setPropertyDefaultValueStrategy(ReflectPropertyDefaultValueStrategy.PROPERTY_DEFAULT)
                    .build());
            env.setProperty(ReflectRepository.class.getName(), o);
        }
        return o;
    }

    public NutsId createSdkId(String type, String version) {
        if (NutsBlankable.isBlank(type)) {
            throw new NutsException(session, NutsMessage.formatted("missing sdk type"));
        }
        if (NutsBlankable.isBlank(version)) {
            throw new NutsException(session, NutsMessage.formatted("missing version"));
        }
        if ("java".equalsIgnoreCase(type)) {
            return NutsJavaSdkUtils.of(ws).createJdkId(version, session);
        } else {
            return NutsIdBuilder.of().setArtifactId(type)
                    .setVersion(version)
                    .build();
        }
    }

    public void checkReadOnly() {
        if (session.config().isReadOnly()) {
            throw new NutsReadOnlyException(session, session.locations().getWorkspaceLocation().toString());
        }
    }

    public NutsSession validateSession(NutsSession session) {
        if (session == null) {
            session = ws.createSession();
        } else {
            if (session.getWorkspace() != ws) {
                throw new NutsIllegalArgumentException(session, NutsMessage.plain("session was created with a different Workspace"));
            }
        }
        return session;
    }

    public NutsId configureFetchEnv(NutsId id) {
        Map<String, String> qm = id.getProperties();
        if (qm.get(NutsConstants.IdProperties.FACE) == null
                && qm.get(NutsConstants.IdProperties.ARCH) == null
                && qm.get(NutsConstants.IdProperties.OS) == null
                && qm.get(NutsConstants.IdProperties.OS_DIST) == null
                && qm.get(NutsConstants.IdProperties.PLATFORM) == null
                && qm.get(NutsConstants.IdProperties.DESKTOP) == null
        ) {
            NutsWorkspaceEnvManager env = session.env();
            qm.put(NutsConstants.IdProperties.ARCH, env.getArchFamily().id());
            qm.put(NutsConstants.IdProperties.OS, env.getOs().toString());
            if (env.getOsDist() != null) {
                qm.put(NutsConstants.IdProperties.OS_DIST, env.getOsDist().toString());
            }
            if (env.getPlatform() != null) {
                qm.put(NutsConstants.IdProperties.PLATFORM, env.getPlatform().toString());
            }
            if (env.getDesktopEnvironment() != null) {
                qm.put(NutsConstants.IdProperties.DESKTOP, env.getDesktopEnvironment().toString());
            }
            return id.builder().setProperties(qm).build();
        }
        return id;
    }

    public List<NutsRepository> filterRepositoriesDeploy(NutsId id, NutsRepositoryFilter repositoryFilter) {
        NutsRepositoryFilter f = NutsRepositoryFilters.of(session).installedRepo().neg().and(repositoryFilter);
        return filterRepositories(NutsRepositorySupportedAction.DEPLOY, id, f, NutsFetchMode.LOCAL);
    }

    public List<NutsRepositoryAndFetchMode> filterRepositoryAndFetchModes(
            NutsRepositorySupportedAction fmode, NutsId id, NutsRepositoryFilter repositoryFilter, NutsFetchStrategy fetchStrategy,
            NutsSession session) {
        List<NutsRepositoryAndFetchMode> ok = new ArrayList<>();
        for (NutsFetchMode nutsFetchMode : fetchStrategy) {
            for (NutsRepository nutsRepositoryAndFetchMode : filterRepositories(fmode, id, repositoryFilter, nutsFetchMode)) {
                ok.add(new NutsRepositoryAndFetchMode(nutsRepositoryAndFetchMode, nutsFetchMode));
            }
        }
        return ok;
    }

    private List<NutsRepository> filterRepositories(NutsRepositorySupportedAction fmode, NutsId id, NutsRepositoryFilter repositoryFilter, NutsFetchMode mode) {
        return filterRepositories(fmode, id, repositoryFilter, true, null, mode);
    }

    private List<NutsRepository> filterRepositories(NutsRepositorySupportedAction fmode, NutsId id, NutsRepositoryFilter repositoryFilter, boolean sortByLevelDesc, final Comparator<NutsRepository> postComp, NutsFetchMode mode) {
        List<RepoAndLevel> repos2 = new ArrayList<>();
        //        List<Integer> reposLevels = new ArrayList<>();

        for (NutsRepository repository : session.repos().setSession(session).getRepositories()) {
            /*repository.isAvailable()*/
            if (repository.isEnabled()
                    && (fmode == NutsRepositorySupportedAction.SEARCH || repository.isSupportedDeploy(session))
                    && repoSPI(repository).isAcceptFetchMode(mode, session)
                    && (repositoryFilter == null || repositoryFilter.acceptRepository(repository))) {
                int d = 0;
                if (fmode == NutsRepositorySupportedAction.DEPLOY) {
                    try {
                        d = NutsRepositoryHelper.getSupportDeployLevel(repository, fmode, id, mode, session.isTransitive(), session);
                    } catch (Exception ex) {
                        _LOGOP(session).level(Level.FINE).error(ex)
                                .log(NutsMessage.jstyle("unable to resolve support deploy level for : {0}", repository.getName()));
                    }
                }
                NutsSpeedQualifier t = NutsSpeedQualifier.NORMAL;
                try {
                    t = NutsRepositoryHelper.getSupportSpeedLevel(repository, fmode, id, mode, session.isTransitive(), session);
                } catch (Exception ex) {
                    _LOGOP(session).level(Level.FINE).error(ex)
                            .log(NutsMessage.jstyle("unable to resolve support speed level for : {0}", repository.getName()));
                }
                if (t != NutsSpeedQualifier.UNAVAILABLE) {
                    repos2.add(new RepoAndLevel(repository, d, t, postComp));
                }
            }
        }
        if (sortByLevelDesc || postComp != null) {
            Collections.sort(repos2);
        }

        List<NutsRepository> ret = new ArrayList<>();
        NutsInstalledRepository installedRepository = NutsWorkspaceExt.of(ws).getInstalledRepository();
        if (mode == NutsFetchMode.LOCAL && fmode == NutsRepositorySupportedAction.SEARCH
                &&
                (repositoryFilter == null || repositoryFilter.acceptRepository(installedRepository))) {
            ret.add(installedRepository);
        }
        for (RepoAndLevel repoAndLevel : repos2) {
            ret.add(repoAndLevel.r);
        }
        return ret;
    }

    public void validateRepositoryName(String repositoryName, Set<String> registered, NutsSession session) {
        if (!repositoryName.matches("[a-zA-Z][.a-zA-Z0-9_-]*")) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid repository id %s", repositoryName));
        }
        if (registered.contains(repositoryName)) {
            throw new NutsRepositoryAlreadyRegisteredException(session, repositoryName);
        }
    }

    public <T> NutsIterator<T> decoratePrint(NutsIterator<T> it, NutsSession session, NutsFetchDisplayOptions displayOptions) {
        final NutsPrintStream out = validateSession(session).getTerminal().getOut();
        return new NutsPrintIterator<>(it, ws, out, displayOptions, session);
    }

    public Events events() {
        return new Events(this);
    }

    public void installAllJVM() {
        NutsWorkspaceEnvManager env = session.env();
        NutsWorkspaceConfigManager config = session.config();
        try {
            if (session.isPlainTrace()) {
                session.out().resetLine().println("looking for java installations in default locations...");
            }
            List<NutsPlatformLocation> found = env.platforms()
                    .searchSystemPlatforms(NutsPlatformFamily.JAVA).toList();
            int someAdded = 0;
            for (NutsPlatformLocation java : found) {
                if (env.platforms().addPlatform(java)) {
                    someAdded++;
                }
            }
            NutsTexts factory = NutsTexts.of(session);
            if (session.isPlainTrace()) {
                if (someAdded == 0) {
                    session.out().print("```error no new``` java installation locations found...\n");
                } else if (someAdded == 1) {
                    session.out().printf("%s new java installation location added...\n", factory.ofStyled("1", NutsTextStyle.primary2()));
                } else {
                    session.out().printf("%s new java installation locations added...\n", factory.ofStyled("" + someAdded, NutsTextStyle.primary2()));
                }
                session.out().println("you can always add another installation manually using 'nuts settings add java' command.");
            }
            if (!config.isReadOnly()) {
                config.save();
            }
        } catch (Exception ex) {
            _LOG(session).with().session(session).level(Level.FINEST).verb(NutsLoggerVerb.WARNING).error(ex)
                    .log(NutsMessage.jstyle("unable to resolve default JRE/JDK locations : {0}", ex));
            if (session.isPlainTrace()) {
                NutsPrintStream out = session.out();
                out.resetLine();
                out.printf("```unable to resolve default JRE/JDK locations``` :  %s%n", ex);
            }
        }
    }

    public void installCurrentJVM() {
//at least add current vm
        NutsWorkspaceEnvManager env = session.env();
        NutsWorkspaceConfigManager config = session.config();
        try {
            if (session.isPlainTrace()) {
                session.out().resetLine().println("adding current JVM...");
            }
            NutsPlatformLocation found0 = env.platforms()
                    .resolvePlatform(NutsPlatformFamily.JAVA, System.getProperty("java.home"), null);
            NutsPlatformLocation[] found = found0 == null ? new NutsPlatformLocation[0] : new NutsPlatformLocation[]{found0};
            int someAdded = 0;
            for (NutsPlatformLocation java : found) {
                if (env.platforms().addPlatform(java)) {
                    someAdded++;
                }
            }
            NutsTexts factory = NutsTexts.of(session);
            if (session.isPlainTrace()) {
                if (someAdded == 0) {
                    session.out().print("```error no new``` java installation locations found...\n");
                }
            }
            if (!config.isReadOnly()) {
                config.save();
            }
        } catch (Exception ex) {
            _LOG(session).with().session(session).level(Level.FINEST).verb(NutsLoggerVerb.WARNING).error(ex)
                    .log(NutsMessage.jstyle("unable to resolve default JRE/JDK locations : {0}", ex));
            if (session.isPlainTrace()) {
                NutsPrintStream out = session.out();
                out.resetLine();
                out.printf("```unable to resolve default JRE/JDK locations``` :  %s%n", ex);
            }
        }
    }

    public void installScriptsAndLaunchers(boolean includeGraphicalLaunchers) {
        NutsWorkspaceEnvManager env = session.env();
        try {
            env.addLauncher(
                    new NutsLauncherOptions()
                            .setId(session.getWorkspace().getApiId())
                            .setCreateScript(true)
                            .setSwitchWorkspace(
                                    session.boot().getBootOptions().getSwitchWorkspace().orElse(false)
                            )
                            .setCreateDesktopLauncher(includeGraphicalLaunchers ? NutsSupportMode.PREFERRED : NutsSupportMode.NEVER)
                            .setCreateMenuLauncher(includeGraphicalLaunchers ? NutsSupportMode.SUPPORTED : NutsSupportMode.NEVER)
            );
        } catch (Exception ex) {
            _LOG(session).with().session(session).level(Level.FINEST).verb(NutsLoggerVerb.WARNING).error(ex)
                    .log(NutsMessage.jstyle("unable to install desktop launchers : {0}", ex));
            if (session.isPlainTrace()) {
                NutsPrintStream out = session.out();
                out.resetLine();
                out.printf("```error unable to install desktop launchers``` :  %s%n", ex);
            }
        }
    }

    public void installCompanions() {
        NutsTexts text = NutsTexts.of(session);
        Set<NutsId> companionIds = session.extensions().getCompanionIds();
        if (companionIds.isEmpty()) {
            return;
        }
        if (session.isPlainTrace()) {
            NutsPrintStream out = session.out();
            out.resetLine();
            out.printf("looking for recommended companion tools to install... detected : %s%n",
                    text.builder().appendJoined(text.ofPlain(","),
                            companionIds
                    )
            );
        }
        try {
            session.install().companions().setSession(session.copy().setTrace(session.isTrace() && session.isPlainOut()))
                    .run();
        } catch (Exception ex) {
            _LOG(session).with().session(session).level(Level.FINEST).verb(NutsLoggerVerb.WARNING).error(ex)
                    .log(NutsMessage.jstyle("unable to install companions : {0}", ex));
            if (session.isPlainTrace()) {
                NutsPrintStream out = session.out();
                out.resetLine();
                out.printf("```error unable to install companion tools``` :  %s \n"
                                + "this happens when none of the following repositories are able to locate them : %s\n",
                        ex,
                        text.builder().appendJoined(text.ofPlain(", "),
                                session.repos().getRepositories().stream().map(x
                                        -> text.builder().append(x.getName(), NutsTextStyle.primary3())
                                ).collect(Collectors.toList())
                        )
                );
            }
        }
    }

    private static class RepoAndLevel implements Comparable<RepoAndLevel> {

        NutsRepository r;
        int deployWeight;
        NutsSpeedQualifier speed;
        Comparator<NutsRepository> postComp;

        public RepoAndLevel(NutsRepository r, int deployWeight, NutsSpeedQualifier speed, Comparator<NutsRepository> postComp) {
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

        private final NutsWorkspaceUtils u;

        public Events(NutsWorkspaceUtils u) {
            this.u = u;
        }

        public void fireOnInstall(NutsInstallEvent event) {
            u._LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLoggerVerb.ADD)
                    .log(NutsMessage.jstyle("installed {0}", event.getDefinition().getId()));
            for (NutsInstallListener listener : event.getSession().events().getInstallListeners()) {
                listener.onInstall(event);
            }
            for (NutsInstallListener listener : event.getSession().getListeners(NutsInstallListener.class)) {
                listener.onInstall(event);
            }
        }

        public void fireOnRequire(NutsInstallEvent event) {
            u._LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLoggerVerb.ADD)
                    .log(NutsMessage.jstyle("required {0}", event.getDefinition().getId()));
            for (NutsInstallListener listener : event.getSession().events().getInstallListeners()) {
                listener.onRequire(event);
            }
            for (NutsInstallListener listener : event.getSession().getListeners(NutsInstallListener.class)) {
                listener.onRequire(event);
            }
        }

        public void fireOnUpdate(NutsUpdateEvent event) {
            if (u._LOG(event.getSession()).isLoggable(Level.FINEST)) {
                if (event.getOldValue() == null) {
                    u._LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLoggerVerb.UPDATE)
                            .log(NutsMessage.jstyle("updated {0}", event.getNewValue().getId()));
                } else {
                    u._LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLoggerVerb.UPDATE)
                            .log(NutsMessage.jstyle("updated {0} (old is {1})",
                                    event.getOldValue().getId().getLongId(),
                                    event.getNewValue().getId().getLongId()));
                }
            }
            for (NutsInstallListener listener : event.getSession().events().getInstallListeners()) {
                listener.onUpdate(event);
            }
            for (NutsInstallListener listener : event.getSession().getListeners(NutsInstallListener.class)) {
                listener.onUpdate(event);
            }
        }

        public void fireOnUninstall(NutsInstallEvent event) {
            if (u._LOG(event.getSession()).isLoggable(Level.FINEST)) {
                u._LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLoggerVerb.REMOVE)
                        .log(NutsMessage.jstyle("uninstalled {0}", event.getDefinition().getId()));
            }
            for (NutsInstallListener listener : event.getSession().events().getInstallListeners()) {
                listener.onUninstall(event);
            }
            for (NutsInstallListener listener : event.getSession().getListeners(NutsInstallListener.class)) {
                listener.onUninstall(event);
            }
        }

        public void fireOnAddRepository(NutsWorkspaceEvent event) {
            if (u._LOG(event.getSession()).isLoggable(Level.CONFIG)) {
                u._LOGOP(event.getSession()).level(Level.CONFIG).verb(NutsLoggerVerb.ADD)
                        .log(NutsMessage.jstyle("added repo ##{0}##", event.getRepository().getName()));
            }

            for (NutsWorkspaceListener listener : event.getSession().events().getWorkspaceListeners()) {
                listener.onAddRepository(event);
            }
            for (NutsWorkspaceListener listener : event.getSession().getListeners(NutsWorkspaceListener.class)) {
                listener.onAddRepository(event);
            }
        }

        public void fireOnRemoveRepository(NutsWorkspaceEvent event) {
            if (u._LOG(event.getSession()).isLoggable(Level.FINEST)) {
                u._LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLoggerVerb.REMOVE)
                        .log(NutsMessage.jstyle("removed repo ##{0}##", event.getRepository().getName()));
            }
            for (NutsWorkspaceListener listener : event.getSession().events().getWorkspaceListeners()) {
                listener.onRemoveRepository(event);
            }
            for (NutsWorkspaceListener listener : event.getSession().getListeners(NutsWorkspaceListener.class)) {
                listener.onRemoveRepository(event);
            }
        }

    }
}

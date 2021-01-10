/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.commands.repo.NutsRepositorySupportedAction;
import net.thevpc.nuts.runtime.core.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.core.format.NutsPrintIterator;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.main.DefaultNutsRepositoryManager;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.io.ProcessBuilder2;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsExecutionEntry;
import net.thevpc.nuts.runtime.core.format.plain.DefaultSearchFormatPlain;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;
import net.thevpc.nuts.runtime.standalone.util.common.CorePlatformUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.util.common.TraceResult;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.io.InputStreamVisitor;
import net.thevpc.nuts.runtime.standalone.util.io.ZipUtils;
import net.thevpc.nuts.spi.NutsRepositorySPI;

/**
 * @author thevpc
 */
public class NutsWorkspaceUtils {

    private final NutsLogger LOG;

    private NutsWorkspace ws;

    public static NutsWorkspaceUtils of(NutsWorkspace ws) {
        NutsWorkspaceUtils wp = (NutsWorkspaceUtils) ws.env().getProperty(NutsWorkspaceUtils.class.getName());
        if (wp == null) {
            wp = new NutsWorkspaceUtils(ws);
            ws.env().setProperty(NutsWorkspaceUtils.class.getName(), wp,new NutsUpdateOptions(ws.createSession()));
        }
        return wp;
    }

    private NutsWorkspaceUtils(NutsWorkspace ws) {
        this.ws = ws;
        LOG = ws.log().of(NutsWorkspaceUtils.class);
    }

    public NutsRepositorySPI repoSPI(NutsRepository repo) {
        DefaultNutsRepositoryManager repos = (DefaultNutsRepositoryManager) ws.repos();
        return repos.toRepositorySPI(repo);
    }

    public NutsId createSdkId(String type, String version) {
        if (CoreStringUtils.isBlank(type)) {
            throw new NutsException(ws, "missing sdk type");
        }
        if (CoreStringUtils.isBlank(version)) {
            throw new NutsException(ws, "missing version");
        }
        if ("java".equalsIgnoreCase(type)) {
            return NutsJavaSdkUtils.of(ws).createJdkId(version);
        } else {
            return ws.id().builder().setArtifactId(type)
                    .setVersion(version)
                    .build();
        }
    }

    public void checkReadOnly() {
        if (ws.config().isReadOnly()) {
            throw new NutsReadOnlyException(ws, ws.locations().getWorkspaceLocation().toString());
        }
    }

    public NutsFetchCommand validateSession(NutsFetchCommand fetch) {
        if (fetch.getSession() == null) {
            fetch = fetch.setSession(ws.createSession());
        }
        return fetch;
    }

    public NutsSession validateSession(NutsSession session) {
        if (session == null) {
            session = ws.createSession();
        } else {
            if (session.getWorkspace() != ws) {
                throw new IllegalArgumentException("session was created with a different Workspace");
            }
        }
        return session;
    }

    public NutsSession validateSilentSession(NutsSession session) {
        if (session == null) {
            session = ws.createSession().setTrace(false);
            return session;
        } else {
            return CoreNutsUtils.silent(session);
        }
    }

    public NutsId configureFetchEnv(NutsId id) {
        Map<String, String> qm = id.getProperties();
        if (qm.get(NutsConstants.IdProperties.FACE) == null && qm.get("arch") == null && qm.get("os") == null && qm.get("osdist") == null && qm.get("platform") == null) {
            qm.put("arch", ws.env().getArch().toString());
            qm.put("os", ws.env().getOs().toString());
            if (ws.env().getOsDist() != null) {
                qm.put("osdist", ws.env().getOsDist().toString());
            }
            return id.builder().setProperties(qm).build();
        }
        return id;
    }

    public List<NutsRepository> _getEnabledRepositories(NutsRepositoryFilter repositoryFilter, NutsSession session) {
        List<NutsRepository> repos = new ArrayList<>();
        List<NutsRepository> subrepos = new ArrayList<>();
        for (NutsRepository repository : ws.repos().getRepositories(session)) {
            boolean ok = false;
            if (repository.config().isEnabled()) {
                if (repositoryFilter == null || repositoryFilter.acceptRepository(repository)) {
                    repos.add(repository);
                    ok = true;
                }
                if (!ok) {
                    subrepos.add(repository);
                }
            }
        }
        for (NutsRepository subrepo : subrepos) {
            repos.addAll(NutsWorkspaceHelper._getEnabledRepositories(subrepo, repositoryFilter, session));
        }
        return repos;
    }

    public List<NutsRepository> filterRepositoriesDeploy(NutsId id, NutsRepositoryFilter repositoryFilter, NutsSession session) {
        return filterRepositories(NutsRepositorySupportedAction.DEPLOY, id, repositoryFilter, NutsFetchMode.LOCAL, session, new InstalledVsNonInstalledSearch(
                false,
                true
        ));
    }

    public List<NutsRepository> filterRepositories(NutsRepositorySupportedAction fmode, NutsId id, NutsRepositoryFilter repositoryFilter, NutsFetchMode mode, NutsSession session, InstalledVsNonInstalledSearch installedVsNonInstalledSearch) {
        return filterRepositories(fmode, id, repositoryFilter, true, null, mode, session, installedVsNonInstalledSearch);
    }

    public List<NutsRepository> filterRepositories(NutsRepositorySupportedAction fmode, NutsId id, NutsRepositoryFilter repositoryFilter, boolean sortByLevelDesc, final Comparator<NutsRepository> postComp, NutsFetchMode mode, NutsSession session, InstalledVsNonInstalledSearch installedVsNonInstalledSearch) {
        List<RepoAndLevel> repos2 = new ArrayList<>();
        //        List<Integer> reposLevels = new ArrayList<>();
        if (installedVsNonInstalledSearch.isSearchInOtherRepositories()) {
            for (NutsRepository repository : ws.repos().getRepositories(session)) {
                if (repository.isEnabled() && (repositoryFilter == null || repositoryFilter.acceptRepository(repository))) {
                    int t = 0;
                    int d = 0;
                    if (fmode == NutsRepositorySupportedAction.DEPLOY) {
                        try {
                            d = CoreNutsUtils.getSupportDeployLevel(repository, fmode, id, mode, session.isTransitive(), session);
                        } catch (Exception ex) {
                            LOG.with().session(session).level(Level.FINE).error(ex).log("unable to resolve support deploy level for : {0}", repository.getName());
                        }
                    }
                    try {
                        t = CoreNutsUtils.getSupportSpeedLevel(repository, fmode, id, mode, session.isTransitive(), session);
                    } catch (Exception ex) {
                        LOG.with().session(session).level(Level.FINE).error(ex).log("unable to resolve support speed level for : {0}", repository.getName());
                    }
                    if (t > 0) {
                        repos2.add(new RepoAndLevel(repository, d, t, postComp));
                    }
                }
            }
            if (sortByLevelDesc || postComp != null) {
                Collections.sort(repos2);
            }
        }
        List<NutsRepository> ret = new ArrayList<>();
        if (fmode == NutsRepositorySupportedAction.SEARCH && installedVsNonInstalledSearch.isSearchInInstalled()) {
            ret.add(NutsWorkspaceExt.of(ws).getInstalledRepository());
        }
        for (RepoAndLevel repoAndLevel : repos2) {
            ret.add(repoAndLevel.r);
        }
        return ret;
    }

    public void checkSimpleNameNutsId(NutsId id) {
        if (id == null) {
            throw new NutsIllegalArgumentException(ws, "missing id");
        }
        if (CoreStringUtils.isBlank(id.getGroupId())) {
            throw new NutsIllegalArgumentException(ws, "missing group for " + id);
        }
        if (CoreStringUtils.isBlank(id.getArtifactId())) {
            throw new NutsIllegalArgumentException(ws, "missing name for " + id);
        }
    }

    public void checkLongNameNutsId(NutsId id) {
        checkSimpleNameNutsId(id);
        if (CoreStringUtils.isBlank(id.getVersion().toString())) {
            throw new NutsIllegalArgumentException(ws, "missing version for " + id);
        }
    }

    public void validateRepositoryName(String repositoryName, Set<String> registered) {
        if (!repositoryName.matches("[a-zA-Z][.a-zA-Z0-9_-]*")) {
            throw new NutsIllegalArgumentException(ws, "Invalid repository id " + repositoryName);
        }
        if (registered.contains(repositoryName)) {
            throw new NutsRepositoryAlreadyRegisteredException(ws, repositoryName);
        }
    }

    public static Set<String> parseProgressOptions(NutsSession session) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String s : CoreStringUtils.split(session.getProgressOptions(), ",; ")) {
            Boolean n = CoreCommonUtils.parseBoolean(s, null);
            if (n == null) {
                set.add(s);
            } else {
                set.add(n.toString());
            }
        }
        return set;
    }

//    public static NutsId parseRequiredNutsId0(String nutFormat) {
//        NutsId id = CoreNutsUtils.parseNutsId(nutFormat);
//        if (id == null) {
//            throw new NutsParseException(null, "invalid Id format : " + nutFormat);
//        }
//        return id;
//    }

//    public NutsId parseRequiredNutsId(String nutFormat) {
//        NutsId id = CoreNutsUtils.parseNutsId(nutFormat);
//        if (id == null) {
//            throw new NutsParseException(ws, "invalid Id format : " + nutFormat);
//        }
//        return id;
//    }

//    public NutsId findNutsIdBySimpleNameInStrings(NutsId id, Collection<String> all) {
//        if (all != null) {
//            for (String nutsId : all) {
//                if (nutsId != null) {
//                    NutsId nutsId2 = parseRequiredNutsId(nutsId);
//                    if (nutsId2.equalsShortName(id)) {
//                        return nutsId2;
//                    }
//                }
//            }
//        }
//        return null;
//    }

    public void checkSession(NutsSession session) {
        if (session == null) {
            throw new NutsIllegalArgumentException(ws, "missing Session");
        }
    }

    private static class RepoAndLevel implements Comparable<RepoAndLevel> {

        NutsRepository r;
        int deployOrder;
        int speedOrder;
        Comparator<NutsRepository> postComp;

        public RepoAndLevel(NutsRepository r, int deployOrder, int speedOrder, Comparator<NutsRepository> postComp) {
            super();
            this.r = r;
            this.deployOrder = deployOrder;
            this.speedOrder = speedOrder;
            this.postComp = postComp;
        }

        @Override
        public int compareTo(RepoAndLevel o2) {
            int x = Integer.compare(this.deployOrder, o2.deployOrder);
            if (x != 0) {
                return x;
            }
            x = Integer.compare(o2.speedOrder, this.speedOrder);
            if (x != 0) {
                return x;
            }
            if (postComp != null) {
                x = postComp.compare(this.r, o2.r);
            }
            return x;
        }
    }

    public NutsIdFormat getIdFormat() {
        String k = DefaultSearchFormatPlain.class.getName() + "#NutsIdFormat";
        NutsIdFormat f = (NutsIdFormat) ws.env().getProperty(k);
        if (f == null) {
            f = ws.id().formatter();
            ws.env().setProperty(k, f,new NutsUpdateOptions(ws.createSession()));
        }
        return f;
    }

    public NutsDescriptorFormat getDescriptorFormat() {
        String k = DefaultSearchFormatPlain.class.getName() + "#NutsDescriptorFormat";
        NutsDescriptorFormat f = (NutsDescriptorFormat) ws.env().getProperty(k);
        if (f == null) {
            f = ws.descriptor().formatter();
            ws.env().setProperty(k, f,new NutsUpdateOptions(ws.createSession()));
        }
        return f;
    }

    public <T> Iterator<T> decoratePrint(Iterator<T> it, NutsSession session, NutsFetchDisplayOptions displayOptions) {
        final PrintStream out = validateSession(session).getTerminal().getOut();
        return new NutsPrintIterator<>(it, ws, out, displayOptions, session);
    }

    public NutsDescriptor getEffectiveDescriptor(NutsDefinition def) {
        final NutsDescriptor d = def.getEffectiveDescriptor();
        if (d == null) {
            return NutsWorkspaceExt.of(ws).resolveEffectiveDescriptor(def.getDescriptor(), null);
        }
        return d;
    }

    public static void checkNutsIdBase(NutsWorkspace ws, NutsId id) {
        if (id == null) {
            throw new NutsIllegalArgumentException(ws, "missing id");
        }
        if (CoreStringUtils.isBlank(id.getGroupId())) {
            throw new NutsIllegalArgumentException(ws, "missing group for " + id);
        }
        if (CoreStringUtils.isBlank(id.getArtifactId())) {
            throw new NutsIllegalArgumentException(ws, "missing name for " + id);
        }
    }

    public void checkNutsId(NutsId id) {
        checkNutsIdBase(ws, id);
        if (id.getVersion().isBlank()) {
            throw new NutsIllegalArgumentException(ws, "missing name for " + id);
        }
    }

    public Events events() {
        return new Events(this);
    }

    public static class Events {

        private NutsWorkspaceUtils u;

        public Events(NutsWorkspaceUtils u) {
            this.u = u;
        }

        public void fireOnInstall(NutsInstallEvent event) {
            if (u.LOG.isLoggable(Level.FINEST)) {
                u.LOG.with().session(event.getSession()).level(Level.FINEST).verb(NutsLogVerb.UPDATE).log("installed ##{0}##", event.getDefinition().getId());
            }
            for (NutsInstallListener listener : u.ws.events().getInstallListeners()) {
                listener.onInstall(event);
            }
            for (NutsInstallListener listener : event.getSession().getListeners(NutsInstallListener.class)) {
                listener.onInstall(event);
            }
        }

        public void fireOnRequire(NutsInstallEvent event) {
            if (u.LOG.isLoggable(Level.FINEST)) {
                u.LOG.with().session(event.getSession()).level(Level.FINEST).verb(NutsLogVerb.UPDATE).log("required ##{0}##", event.getDefinition().getId());
            }
            for (NutsInstallListener listener : u.ws.events().getInstallListeners()) {
                listener.onRequire(event);
            }
            for (NutsInstallListener listener : event.getSession().getListeners(NutsInstallListener.class)) {
                listener.onRequire(event);
            }
        }

        public void fireOnUpdate(NutsUpdateEvent event) {
            if (u.LOG.isLoggable(Level.FINEST)) {
                if (event.getOldValue() == null) {
                    u.LOG.with().session(event.getSession()).level(Level.FINEST).verb(NutsLogVerb.UPDATE).formatted()
                            .log("updated ##{0}##", event.getNewValue().getId());
                } else {
                    u.LOG.with().session(event.getSession()).level(Level.FINEST).verb(NutsLogVerb.UPDATE).formatted()
                            .log("updated ##{0}## (old is ```error {1}```)",
                                    event.getOldValue().getId().getLongNameId(),
                                    event.getNewValue().getId().getLongNameId());
                }
            }
            for (NutsInstallListener listener : u.ws.events().getInstallListeners()) {
                listener.onUpdate(event);
            }
            for (NutsInstallListener listener : event.getSession().getListeners(NutsInstallListener.class)) {
                listener.onUpdate(event);
            }
        }

        public void fireOnUninstall(NutsInstallEvent event) {
            if (u.LOG.isLoggable(Level.FINEST)) {
                u.LOG.with().session(event.getSession()).level(Level.FINEST).verb(NutsLogVerb.UPDATE).formatted()
                        .log("uninstalled ##{0}##", event.getDefinition().getId());
            }
            for (NutsInstallListener listener : u.ws.events().getInstallListeners()) {
                listener.onUninstall(event);
            }
            for (NutsInstallListener listener : event.getSession().getListeners(NutsInstallListener.class)) {
                listener.onUninstall(event);
            }
        }

        public void fireOnAddRepository(NutsWorkspaceEvent event) {
            if (u.LOG.isLoggable(Level.CONFIG)) {
                u.LOG.with().session(event.getSession()).level(Level.CONFIG).verb(NutsLogVerb.UPDATE).formatted()
                        .log("added Repo ##{0}##", event.getRepository().getName());
            }

            for (NutsWorkspaceListener listener : u.ws.events().getWorkspaceListeners()) {
                listener.onAddRepository(event);
            }
            for (NutsWorkspaceListener listener : event.getSession().getListeners(NutsWorkspaceListener.class)) {
                listener.onAddRepository(event);
            }
        }

        public void fireOnRemoveRepository(NutsWorkspaceEvent event) {
            if (u.LOG.isLoggable(Level.FINEST)) {
                u.LOG.with().session(event.getSession()).level(Level.FINEST).verb(NutsLogVerb.UPDATE).formatted()
                        .log("removed Repo ##{0}##", event.getRepository().getName());
            }
            for (NutsWorkspaceListener listener : u.ws.events().getWorkspaceListeners()) {
                listener.onRemoveRepository(event);
            }
            for (NutsWorkspaceListener listener : event.getSession().getListeners(NutsWorkspaceListener.class)) {
                listener.onRemoveRepository(event);
            }
        }

    }

    public void traceMessage(NutsFetchStrategy fetchMode, NutsId id, TraceResult tracePhase, String message, long startTime, NutsSession session) {
        if (LOG.isLoggable(Level.FINEST)) {

            long time = (startTime != 0) ? (System.currentTimeMillis() - startTime) : 0;
            String fetchString = "[" + CoreStringUtils.alignLeft(fetchMode.name(), 7) + "] ";
            LOG.with().session(session).level(Level.FINEST)
                    .verb(tracePhase.toString()).formatted().time(time)
                    .log("{0}{1} {2}",
                            fetchString,
                            id,
                            CoreStringUtils.alignLeft(message, 18)
                    );
        }
    }

    public CoreIOUtils.ProcessExecHelper execAndWait(String[] args, Map<String, String> env, Path directory, NutsSessionTerminal prepareTerminal,
                                                     NutsSessionTerminal execTerminal, boolean showCommand, boolean failFast, long sleep, NutsSession session) {
        PrintStream out = execTerminal.out();
        PrintStream err = execTerminal.err();
        InputStream in = execTerminal.in();
        if (ws.io().term().getSystemTerminal().isStandardOutputStream(out)) {
            out = null;
        }
        if (ws.io().term().getSystemTerminal().isStandardErrorStream(err)) {
            err = null;
        }
        if (ws.io().term().getSystemTerminal().isStandardInputStream(in)) {
            in = null;
        }
        CoreIOUtils.clearMonitor(out, ws);
        ProcessBuilder2 pb = new ProcessBuilder2(ws)
                .setCommand(args)
                .setEnv(env)
                .setIn(in)
                .setOutput(out)
                .setErr(err)
                .setDirectory(directory == null ? null : directory.toFile())
                .setSleepMillis(sleep)
                .setFailFast(failFast);
        if (out == null && err == null && in == null) {
            pb.inheritIO();
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.with().session(session).level(Level.FINE).verb(NutsLogVerb.START).formatted().log("[exec] {0}", NutsString.of(pb.getFormattedCommandString(ws)));
        }
        if (showCommand || CoreCommonUtils.getSysBoolNutsProperty("show-command", false)) {
            if (ws.io().term().isFormatted(prepareTerminal.out())) {
                prepareTerminal.out().printf("%s ",ws.formats().text().factory().styled("[exec]",NutsTextNodeStyle.primary(4)));
                prepareTerminal.out().println(pb.getFormattedCommandString(ws));
            } else {
                prepareTerminal.out().print("exec ");
                prepareTerminal.out().printf("%s%n", pb.getCommandString());
            }
        }
        return new CoreIOUtils.ProcessExecHelper(pb, ws, out == null ? execTerminal.out() : out);
    }

    public CoreIOUtils.ProcessExecHelper execAndWait(NutsDefinition nutMainFile, NutsSession prepareSession, NutsSession execSession, Map<String, String> execProperties, String[] args, Map<String, String> env, String directory, boolean showCommand, boolean failFast,long sleep) throws NutsExecutionException {
        NutsWorkspace workspace = execSession.getWorkspace();
        NutsId id = nutMainFile.getId();
        Path installerFile = nutMainFile.getPath();
        String storeFolder = nutMainFile.getInstallInformation().getInstallFolder();
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> envmap = new HashMap<>();
//        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
//            map.put((String) entry.getKey(), (String) entry.getValue());
//        }
        for (Map.Entry<String, String> entry : execProperties.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        Path nutsJarFile = workspace.fetch().setNutsApi().setSession(CoreNutsUtils.silent(prepareSession)).getResultPath();
        if (nutsJarFile != null) {
            map.put("nuts.jar", nutsJarFile.toAbsolutePath().normalize().toString());
        }
        map.put("nuts.id", id.getLongName());
        map.put("nuts.id.version", id.getVersion().getValue());
        map.put("nuts.id.name", id.getArtifactId());
        map.put("nuts.id.simpleName", id.getShortName());
        map.put("nuts.id.group", id.getGroupId());
        map.put("nuts.file", nutMainFile.getPath().toString());
        String defaultJavaCommand = NutsJavaSdkUtils.of(ws).resolveJavaCommandByVersion("", false, prepareSession);

        map.put("nuts.java", defaultJavaCommand);
        if (map.containsKey("nuts.jar")) {
            map.put("nuts.cmd", map.get("nuts.java") + " -jar " + map.get("nuts.jar"));
        }
        map.put("nuts.workspace", workspace.locations().getWorkspaceLocation().toString());
        map.put("nuts.version", id.getVersion().getValue());
        map.put("nuts.name", id.getArtifactId());
        map.put("nuts.group", id.getGroupId());
        map.put("nuts.face", id.getFace());
        map.put("nuts.namespace", id.getNamespace());
        map.put("nuts.id", id.toString());
        if (installerFile != null) {
            map.put("nuts.installer", installerFile.toString());
        }
        if (storeFolder == null && installerFile != null) {
            map.put("nuts.store", installerFile.getParent().toString());
        } else if (storeFolder != null) {
            map.put("nuts.store", storeFolder);
        }
        if (env != null) {
            map.putAll(env);
        }
        Function<String, String> mapper = new Function<String, String>() {
            @Override
            public String apply(String skey) {
                if (skey.equals("java") || skey.startsWith("java#")) {
                    String javaVer = skey.substring(4);
                    if (CoreStringUtils.isBlank(javaVer)) {
                        return defaultJavaCommand;
                    }
                    return NutsJavaSdkUtils.of(workspace).resolveJavaCommandByVersion(javaVer, false, prepareSession);
                } else if (skey.equals("javaw") || skey.startsWith("javaw#")) {
                    String javaVer = skey.substring(4);
                    if (CoreStringUtils.isBlank(javaVer)) {
                        return defaultJavaCommand;
                    }
                    return NutsJavaSdkUtils.of(workspace).resolveJavaCommandByVersion(javaVer, true, prepareSession);
                } else if (skey.equals("nuts")) {
                    NutsDefinition nutsDefinition;
                    nutsDefinition = workspace.fetch().setId(NutsConstants.Ids.NUTS_API)
                            .setSession(prepareSession).getResultDefinition();
                    if (nutsDefinition.getPath() != null) {
                        return ("<::expand::> " + apply("java") + " -jar " + nutsDefinition.getPath());
                    }
                    return null;
                }
                return map.get(skey);
            }
        };
        for (Map.Entry<String, String> e : map.entrySet()) {
            String k = e.getKey();
            if (!CoreStringUtils.isBlank(k)) {
                k = k.replace('.', '_');
                if (!CoreStringUtils.isBlank(e.getValue())) {
                    envmap.put(k, e.getValue());
                }
            }
        }
        List<String> args2 = new ArrayList<>();
        for (String arg : args) {
            String s = CoreStringUtils.trim(CoreStringUtils.replaceDollarPlaceHolders(arg, mapper));
            if (s.startsWith("<::expand::>")) {
                Collections.addAll(args2, workspace.commandLine().parse(s).toStringArray());
            } else {
                args2.add(s);
            }
        }
        args = args2.toArray(new String[0]);

        Path path = Paths.get(workspace.locations().getWorkspaceLocation()).resolve(args[0]).normalize();
        if (Files.exists(path)) {
            CoreIOUtils.setExecutable(path);
        }
        Path pdirectory = null;
        if (CoreStringUtils.isBlank(directory)) {
            pdirectory = Paths.get(workspace.locations().getWorkspaceLocation());
        } else {
            pdirectory = Paths.get(workspace.locations().getWorkspaceLocation()).resolve(directory);
        }
        return execAndWait(args, envmap, pdirectory, prepareSession.getTerminal(), execSession.getTerminal(), showCommand, failFast,
                sleep, prepareSession);
    }

    public NutsExecutionEntry parseClassExecutionEntry(InputStream classStream, String sourceName, NutsSession session) {
        CorePlatformUtils.MainClassType mainClass = null;
        try {
            mainClass = CorePlatformUtils.getMainClassType(classStream);
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.FINE).error(ex).log("invalid file format {0}", sourceName);
        }
        if (mainClass != null) {
            return new DefaultNutsExecutionEntry(
                    mainClass.getName(),
                    false,
                    mainClass.isApp() && mainClass.isMain()
            );
        }
        return null;
    }

    public NutsExecutionEntry[] parseJarExecutionEntries(InputStream jarStream, String sourceName, NutsSession session) {
        if (!(jarStream instanceof BufferedInputStream)) {
            jarStream = new BufferedInputStream(jarStream);
        }
        final List<NutsExecutionEntry> classes = new ArrayList<>();
        final List<String> manifestClass = new ArrayList<>();
        try {
            ZipUtils.visitZipStream(jarStream, new Predicate<String>() {
                @Override
                public boolean test(String path) {
                    return path.endsWith(".class")
                            || path.equals("META-INF/MANIFEST.MF");
                }
            }, new InputStreamVisitor() {
                @Override
                public boolean visit(String path, InputStream inputStream) throws IOException {
                    if (path.endsWith(".class")) {
                        NutsExecutionEntry mainClass = parseClassExecutionEntry(inputStream, path, session);
                        if (mainClass != null) {
                            classes.add(mainClass);
                        }
                    } else {
                        Manifest manifest = new Manifest(inputStream);
                        Attributes a = manifest.getMainAttributes();
                        if (a != null && a.containsKey("Main-Class")) {
                            String v = a.getValue("Main-Class");
                            if (!CoreStringUtils.isBlank(v)) {
                                manifestClass.add(v);
                            }
                        }
                    }
                    return true;
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        List<NutsExecutionEntry> entries = new ArrayList<>();
        String defaultEntry = null;
        if (manifestClass.size() > 0) {
            defaultEntry = manifestClass.get(0);
        }
        boolean defaultFound = false;
        for (NutsExecutionEntry entry : classes) {
            if (defaultEntry != null && defaultEntry.equals(entry.getName())) {
                entries.add(new DefaultNutsExecutionEntry(entry.getName(), true, entry.isApp()));
                defaultFound = true;
            } else {
                entries.add(entry);
            }
        }
        if (defaultEntry != null && !defaultFound) {
            LOG.with().session(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL).log("invalid default entry " + defaultEntry + " in " + sourceName);
//            entries.add(new DefaultNutsExecutionEntry(defaultEntry, true, false));
        }
        return entries.toArray(new NutsExecutionEntry[0]);
    }

    public static boolean setSession(Object o,NutsSession session) {
        if (o instanceof NutsSessionAware) {
            ((NutsSessionAware) o).setSession(session);
            return true;
        }
        return false;
    }

    public static boolean unsetWorkspace(Object o) {
        if (o instanceof NutsWorkspaceAware) {
            ((NutsWorkspaceAware) o).setWorkspace(null);
            return true;
        }
        return false;
    }
}

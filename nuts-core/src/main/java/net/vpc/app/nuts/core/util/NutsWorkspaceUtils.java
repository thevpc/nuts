/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsExecutionEntry;
import net.vpc.app.nuts.core.format.plain.DefaultSearchFormatPlain;
import net.vpc.app.nuts.core.log.NutsLogVerb;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;

import net.vpc.app.nuts.core.format.NutsTraceIterator;
import net.vpc.app.nuts.core.format.NutsFetchDisplayOptions;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.core.util.common.TraceResult;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.io.InputStreamVisitor;
import net.vpc.app.nuts.core.util.io.ProcessBuilder2;
import net.vpc.app.nuts.core.util.io.ZipUtils;

/**
 * @author vpc
 */
public class NutsWorkspaceUtils {
    private final NutsLogger LOG;

    private NutsWorkspace ws;

    public static NutsWorkspaceUtils of(NutsWorkspace ws) {
        Map<String, Object> up = ws.userProperties();
        NutsWorkspaceUtils wp = (NutsWorkspaceUtils) up.get(NutsWorkspaceUtils.class.getName());
        if (wp == null) {
            wp = new NutsWorkspaceUtils(ws);
            up.put(NutsWorkspaceUtils.class.getName(), wp);
        }
        return wp;
    }

    private NutsWorkspaceUtils(NutsWorkspace ws) {
        this.ws = ws;
        LOG = ws.log().of(NutsWorkspaceUtils.class);
    }

    public NutsId createSdkId(String type, String version) {
        if (CoreStringUtils.isBlank(type)) {
            throw new NutsException(ws, "Missing sdk type");
        }
        if (CoreStringUtils.isBlank(version)) {
            throw new NutsException(ws, "Missing version");
        }
        if ("java".equalsIgnoreCase(type)) {
            return NutsJavaSdkUtils.of(ws).createJdkId(version);
        } else {
            return ws.id().builder().artifactId(type)
                    .version(version)
                    .build();
        }
    }

    public void checkReadOnly() {
        if (ws.config().isReadOnly()) {
            throw new NutsReadOnlyException(ws, ws.config().getWorkspaceLocation().toString());
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
        }
        return session;
    }

    public NutsId configureFetchEnv(NutsId id) {
        Map<String, String> qm = id.getProperties();
        if (qm.get(NutsConstants.IdProperties.FACE) == null && qm.get("arch") == null && qm.get("os") == null && qm.get("osdist") == null && qm.get("platform") == null) {
            qm.put("arch", ws.config().getArch().toString());
            qm.put("os", ws.config().getOs().toString());
            if (ws.config().getOsDist() != null) {
                qm.put("osdist", ws.config().getOsDist().toString());
            }
            return id.builder().setProperties(qm).build();
        }
        return id;
    }

    public List<NutsRepository> _getEnabledRepositories(NutsRepositoryFilter repositoryFilter) {
        List<NutsRepository> repos = new ArrayList<>();
        List<NutsRepository> subrepos = new ArrayList<>();
        for (NutsRepository repository : ws.config().getRepositories()) {
            boolean ok = false;
            if (repository.config().isEnabled()) {
                if (repositoryFilter == null || repositoryFilter.accept(repository)) {
                    repos.add(repository);
                    ok = true;
                }
                if (!ok) {
                    subrepos.add(repository);
                }
            }
        }
        for (NutsRepository subrepo : subrepos) {
            repos.addAll(NutsWorkspaceHelper._getEnabledRepositories(subrepo, repositoryFilter));
        }
        return repos;
    }

    public List<NutsRepository> filterRepositories(NutsRepositorySupportedAction fmode, NutsId id, NutsRepositoryFilter repositoryFilter, NutsFetchMode mode, NutsFetchCommand options) {
        return filterRepositories(fmode, id, repositoryFilter, true, null, mode, options);
    }

    public List<NutsRepository> filterRepositories(NutsRepositorySupportedAction fmode, NutsId id, NutsRepositoryFilter repositoryFilter, boolean sortByLevelDesc, final Comparator<NutsRepository> postComp, NutsFetchMode mode, NutsFetchCommand options) {

        List<RepoAndLevel> repos2 = new ArrayList<>();
        //        List<Integer> reposLevels = new ArrayList<>();
        for (NutsRepository repository : ws.config().getRepositories()) {
            if (repository.config().isEnabled() && (repositoryFilter == null || repositoryFilter.accept(repository))) {
                int t = 0;
                try {
                    t = repository.config().getSupportLevel(fmode, id, mode, options.isTransitive());
                } catch (Exception e) {
                    LOG.log(Level.FINE, "Unable to resolve support level for : " + repository.config().name(), e);
                }
                if (t > 0) {
                    repos2.add(new RepoAndLevel(repository, t, postComp));
                }
            }
        }
        if (sortByLevelDesc || postComp != null) {
            Collections.sort(repos2);
        }
        List<NutsRepository> ret = new ArrayList<>();
        for (RepoAndLevel repoAndLevel : repos2) {
            ret.add(repoAndLevel.r);
        }
        return ret;
    }

    public void checkSimpleNameNutsId(NutsId id) {
        if (id == null) {
            throw new NutsIllegalArgumentException(ws, "Missing id");
        }
        if (CoreStringUtils.isBlank(id.getGroupId())) {
            throw new NutsIllegalArgumentException(ws, "Missing group for " + id);
        }
        if (CoreStringUtils.isBlank(id.getArtifactId())) {
            throw new NutsIllegalArgumentException(ws, "Missing name for " + id);
        }
    }

    public void checkLongNameNutsId(NutsId id) {
        checkSimpleNameNutsId(id);
        if (CoreStringUtils.isBlank(id.getVersion().toString())) {
            throw new NutsIllegalArgumentException(ws, "Missing version for " + id);
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

    public static NutsId parseRequiredNutsId0(String nutFormat) {
        NutsId id = CoreNutsUtils.parseNutsId(nutFormat);
        if (id == null) {
            throw new NutsParseException(null, "Invalid Id format : " + nutFormat);
        }
        return id;
    }

    public NutsId parseRequiredNutsId(String nutFormat) {
        NutsId id = CoreNutsUtils.parseNutsId(nutFormat);
        if (id == null) {
            throw new NutsParseException(ws, "Invalid Id format : " + nutFormat);
        }
        return id;
    }

    public NutsId findNutsIdBySimpleNameInStrings(NutsId id, Collection<String> all) {
        if (all != null) {
            for (String nutsId : all) {
                if (nutsId != null) {
                    NutsId nutsId2 = parseRequiredNutsId(nutsId);
                    if (nutsId2.equalsShortName(id)) {
                        return nutsId2;
                    }
                }
            }
        }
        return null;
    }

    public void checkSession(NutsRepositorySession session) {
        if (session == null) {
            throw new NutsIllegalArgumentException(ws, "Missing Session");
        }
    }

    private static class RepoAndLevel implements Comparable<RepoAndLevel> {

        NutsRepository r;
        int level;
        Comparator<NutsRepository> postComp;

        public RepoAndLevel(NutsRepository r, int level, Comparator<NutsRepository> postComp) {
            super();
            this.r = r;
            this.level = level;
            this.postComp = postComp;
        }

        @Override
        public int compareTo(RepoAndLevel o2) {
            int x = Integer.compare(o2.level, this.level);
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
        NutsIdFormat f = (NutsIdFormat) ws.userProperties().get(k);
        if (f == null) {
            f = ws.id();
            ws.userProperties().put(k, f);
        }
        return f;
    }

    public NutsDescriptorFormat getDescriptorFormat() {
        String k = DefaultSearchFormatPlain.class.getName() + "#NutsDescriptorFormat";
        NutsDescriptorFormat f = (NutsDescriptorFormat) ws.userProperties().get(k);
        if (f == null) {
            f = ws.descriptor();
            ws.userProperties().put(k, f);
        }
        return f;
    }

    public <T> Iterator<T> decorateTrace(Iterator<T> it, NutsSession session, NutsFetchDisplayOptions displayOptions) {
        final PrintStream out = validateSession(session).getTerminal().getOut();
        return new NutsTraceIterator<>(it, ws, out, displayOptions, session);
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
            throw new NutsIllegalArgumentException(ws, "Missing id");
        }
        if (CoreStringUtils.isBlank(id.getGroupId())) {
            throw new NutsIllegalArgumentException(ws, "Missing group for " + id);
        }
        if (CoreStringUtils.isBlank(id.getArtifactId())) {
            throw new NutsIllegalArgumentException(ws, "Missing name for " + id);
        }
    }

    public void checkNutsId(NutsId id) {
        checkNutsIdBase(ws, id);
        if (id.getVersion().isBlank()) {
            throw new NutsIllegalArgumentException(ws, "Missing name for " + id);
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

        public void fireOnInstall( NutsInstallEvent event) {
            if (u.LOG.isLoggable(Level.FINEST)) {
                u.LOG.log(Level.FINEST, NutsLogVerb.UPDATE, "Installed {0}", new Object[]{event.getDefinition().getId()});
            }
            for (NutsInstallListener listener : u.ws.getInstallListeners()) {
                listener.onInstall(event);
            }
            for (NutsInstallListener listener : event.getSession().getListeners(NutsInstallListener.class)) {
                listener.onInstall(event);
            }
        }

        public void fireOnUpdate(NutsInstallEvent event) {
            if (u.LOG.isLoggable(Level.FINEST)) {
                u.LOG.log(Level.FINEST, NutsLogVerb.UPDATE, "Updated {0}", new Object[]{event.getDefinition().getId()});
            }
            for (NutsInstallListener listener : u.ws.getInstallListeners()) {
                listener.onUpdate(event);
            }
            for (NutsInstallListener listener : event.getSession().getListeners(NutsInstallListener.class)) {
                listener.onUpdate(event);
            }
        }

        public void fireOnUninstall(NutsInstallEvent event) {
            if (u.LOG.isLoggable(Level.FINEST)) {
                u.LOG.log(Level.FINEST, NutsLogVerb.UPDATE, "Uninstalled {0}", new Object[]{event.getDefinition().getId()});
            }
            for (NutsInstallListener listener : u.ws.getInstallListeners()) {
                listener.onUninstall(event);
            }
            for (NutsInstallListener listener : event.getSession().getListeners(NutsInstallListener.class)) {
                listener.onUninstall(event);
            }
        }

        public void fireOnAddRepository(NutsWorkspaceEvent event) {
            if (u.LOG.isLoggable(Level.CONFIG)) {
                u.LOG.log(Level.CONFIG, NutsLogVerb.UPDATE, "Added Repo {0}", new Object[]{event.getRepository().config().name()});
            }

            for (NutsWorkspaceListener listener : u.ws.getWorkspaceListeners()) {
                listener.onAddRepository(event);
            }
            for (NutsWorkspaceListener listener : event.getSession().getListeners(NutsWorkspaceListener.class)) {
                listener.onAddRepository(event);
            }
        }

        public void fireOnRemoveRepository(NutsWorkspaceEvent event) {
            if (u.LOG.isLoggable(Level.FINEST)) {
                u.LOG.log(Level.FINEST, NutsLogVerb.UPDATE, "Removed Repo {0}", new Object[]{event.getRepository().config().name()});
            }
            for (NutsWorkspaceListener listener : u.ws.getWorkspaceListeners()) {
                listener.onRemoveRepository(event);
            }
            for (NutsWorkspaceListener listener : event.getSession().getListeners(NutsWorkspaceListener.class)) {
                listener.onRemoveRepository(event);
            }
        }

    }

    public void traceMessage(NutsFetchStrategy fetchMode, NutsId id, TraceResult tracePhase, String message, long startTime) {
        if(LOG.isLoggable(Level.FINEST)) {
            String timeMessage = "";
            if (startTime != 0) {
                long time = System.currentTimeMillis() - startTime;
                if (time > 0) {
                    timeMessage = " (" + time + "ms)";
                }
            }
            String fetchString = "[" + CoreStringUtils.alignLeft(fetchMode.name(), 7) + "] ";
            LOG.log(Level.FINEST, tracePhase.toString(), fetchString
                    + CoreStringUtils.alignLeft(message, 18) + " " + id + timeMessage);
        }
    }

    public CoreIOUtils.ProcessExecHelper execAndWait(String[] args, Map<String, String> env, Path directory, NutsSessionTerminal terminal, boolean showCommand, boolean failFast) {
        PrintStream out = terminal.out();
        PrintStream err = terminal.err();
        InputStream in = terminal.in();
        if (ws.io().getSystemTerminal().isStandardOutputStream(out)) {
            out = null;
        }
        if (ws.io().getSystemTerminal().isStandardErrorStream(err)) {
            err = null;
        }
        if (ws.io().getSystemTerminal().isStandardInputStream(in)) {
            in = null;
        }
        ProcessBuilder2 pb = new ProcessBuilder2(ws)
                .setCommand(args)
                .setEnv(env)
                .setIn(in)
                .setOutput(out)
                .setErr(err)
                .setDirectory(directory == null ? null : directory.toFile())
                .setFailFast(failFast);
        if (out == null && err == null && in == null) {
            pb.inheritIO();
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.withLevel(Level.FINE).withVerb(NutsLogVerb.START).formatted().log("[exec] "+ pb.getFormattedCommandString(ws));
        }
        if (showCommand || CoreCommonUtils.getSysBoolNutsProperty("show-command", false)) {
            if (ws.io().getTerminalFormat().isFormatted(terminal.out())) {
                terminal.out().print("==[exec]== ");
                terminal.out().println(pb.getFormattedCommandString(ws));
            } else {
                terminal.out().print("exec ");
                terminal.out().printf("%s%n", pb.getCommandString());
            }
        }
        return new CoreIOUtils.ProcessExecHelper(pb, ws, out == null ? terminal.out() : out);
    }

    public CoreIOUtils.ProcessExecHelper execAndWait(NutsDefinition nutMainFile, NutsSession session, Map<String, String> execProperties, String[] args, Map<String, String> env, String directory, boolean showCommand, boolean failFast) throws NutsExecutionException {
        NutsWorkspace workspace = session.getWorkspace();
        NutsId id = nutMainFile.getId();
        Path installerFile = nutMainFile.getPath();
        Path storeFolder = nutMainFile.getInstallInformation().getInstallFolder();
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> envmap = new HashMap<>();
//        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
//            map.put((String) entry.getKey(), (String) entry.getValue());
//        }
        for (Map.Entry<String, String> entry : execProperties.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        Path nutsJarFile = workspace.fetch().nutsApi().session(session.copy().trace(false)).getResultPath();
        if (nutsJarFile != null) {
            map.put("nuts.jar", nutsJarFile.toAbsolutePath().normalize().toString());
        }
        map.put("nuts.id", id.getLongName());
        map.put("nuts.id.version", id.getVersion().getValue());
        map.put("nuts.id.name", id.getArtifactId());
        map.put("nuts.id.simpleName", id.getShortName());
        map.put("nuts.id.group", id.getGroupId());
        map.put("nuts.file", nutMainFile.getPath().toString());
        String defaultJavaCommand = NutsJavaSdkUtils.of(ws).resolveJavaCommandByVersion("", false);

        map.put("nuts.java", defaultJavaCommand);
        if (map.containsKey("nuts.jar")) {
            map.put("nuts.cmd", map.get("nuts.java") + " -jar " + map.get("nuts.jar"));
        }
        map.put("nuts.workspace", workspace.config().getWorkspaceLocation().toString());
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
            map.put("nuts.store", storeFolder.toString());
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
                    return NutsJavaSdkUtils.of(workspace).resolveJavaCommandByVersion(javaVer, false);
                }else if (skey.equals("javaw") || skey.startsWith("javaw#")) {
                    String javaVer = skey.substring(4);
                    if (CoreStringUtils.isBlank(javaVer)) {
                        return defaultJavaCommand;
                    }
                    return NutsJavaSdkUtils.of(workspace).resolveJavaCommandByVersion(javaVer, true);
                } else if (skey.equals("nuts")) {
                    NutsDefinition nutsDefinition;
                    nutsDefinition = workspace.fetch().id(NutsConstants.Ids.NUTS_API).setSession(session).getResultDefinition();
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
                Collections.addAll(args2, workspace.commandLine().parse(s).toArray());
            } else {
                args2.add(s);
            }
        }
        args = args2.toArray(new String[0]);

        Path path = workspace.config().getWorkspaceLocation().resolve(args[0]).normalize();
        if (Files.exists(path)) {
            CoreIOUtils.setExecutable(path);
        }
        Path pdirectory = null;
        if (CoreStringUtils.isBlank(directory)) {
            pdirectory = workspace.config().getWorkspaceLocation();
        } else {
            pdirectory = workspace.config().getWorkspaceLocation().resolve(directory);
        }
        return execAndWait(args, envmap, pdirectory, session.getTerminal(), showCommand, failFast);
    }


    public NutsExecutionEntry parseClassExecutionEntry(InputStream classStream, String sourceName) {
        CorePlatformUtils.MainClassType mainClass = null;
        try {
            mainClass = CorePlatformUtils.getMainClassType(classStream);
        } catch (Exception ex) {
            LOG.log(Level.FINEST, "Invalid file format " + sourceName, ex);
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

    public NutsExecutionEntry[] parseJarExecutionEntries(InputStream jarStream, String sourceName) {
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
                        NutsExecutionEntry mainClass = parseClassExecutionEntry(inputStream, path);
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
            LOG.log(Level.SEVERE, NutsLogVerb.FAIL, "Invalid default entry " + defaultEntry + " in " + sourceName);
//            entries.add(new DefaultNutsExecutionEntry(defaultEntry, true, false));
        }
        return entries.toArray(new NutsExecutionEntry[0]);
    }

}

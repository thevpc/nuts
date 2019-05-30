/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.format.DefaultSearchFormatPlain;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.vpc.app.nuts.core.format.NutsTraceIterator;
import net.vpc.app.nuts.core.format.NutsFetchDisplayOptions;

/**
 *
 * @author vpc
 */
public class NutsWorkspaceUtils {

    public static NutsSdkLocation[] searchJdkLocations(NutsWorkspace ws, PrintStream out) {
        String[] conf = {};
        switch (ws.config().getPlatformOsFamily()) {
            case LINUX:
            case UNIX:
            case UNKNOWN: {
                conf = new String[]{"/usr/java", "/usr/lib64/jvm", "/usr/lib/jvm"};
                break;
            }
            case WINDOWS: {
                conf = new String[]{CoreStringUtils.coalesce(System.getenv("ProgramFiles"), "C:\\Program Files") + "\\Java", CoreStringUtils.coalesce(System.getenv("ProgramFiles(x86)"), "C:\\Program Files (x86)") + "\\Java"};
                break;
            }
            case MACOS: {
                conf = new String[]{"/Library/Java/JavaVirtualMachines", "/System/Library/Frameworks/JavaVM.framework"};
                break;
            }
        }
        List<NutsSdkLocation> all = new ArrayList<>();
        for (String s : conf) {
            all.addAll(Arrays.asList(searchJdkLocations(ws, ws.io().path(s), out)));
        }
        return all.toArray(new NutsSdkLocation[0]);
    }

    public static NutsSdkLocation[] searchJdkLocations(NutsWorkspace ws, Path s, PrintStream out) {
        List<NutsSdkLocation> all = new ArrayList<>();
        if (Files.isDirectory(s)) {
            try (final DirectoryStream<Path> it = Files.newDirectoryStream(s)) {
                for (Path d : it) {
                    NutsSdkLocation r = resolveJdkLocation(ws, d);
                    if (r != null) {
                        all.add(r);
                        if (out != null) {
                            out.printf("Detected SDK [[%s]] at ==%s==%n", r.getVersion(), r.getPath());
                        }
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return all.toArray(new NutsSdkLocation[0]);
    }

    public static NutsSdkLocation resolveJdkLocation(NutsWorkspace ws, Path path) {
        if (path == null) {
            return null;
        }
        if (!Files.isDirectory(path)) {
            return null;
        }
        Path javaExePath = path.resolve("bin").resolve("java");
        if (!Files.exists(javaExePath)) {
            return null;
        }
        String type = null;
        String jdkVersion = null;
        try {
            NutsExecCommand b = ws.exec().syscall().command(javaExePath.toString(), "-version").redirectErrorStream().grabOutputString().run();
            if (b.getResult() == 0) {
                String s = b.getOutputString();
                if (s.length() > 0) {
                    String prefix = "java version \"";
                    int i = s.indexOf(prefix);
                    if (i >= 0) {
                        i = i + prefix.length();
                        int j = s.indexOf("\"", i);
                        if (i >= 0) {
                            jdkVersion = s.substring(i, j);
                            type = "JDK";
                        }
                    }
                    if (jdkVersion == null) {
                        prefix = "openjdk version \"";
                        i = s.indexOf(prefix);
                        if (i >= 0) {
                            i = i + prefix.length();
                            int j = s.indexOf("\"", i);
                            if (i > 0) {
                                jdkVersion = s.substring(i, j);
                                type = "OpenJDK";
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(CorePlatformUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (jdkVersion == null) {
            return null;
        }
        NutsSdkLocation loc = new NutsSdkLocation();
        loc.setType("java");
        loc.setName(type + " " + jdkVersion);
        loc.setVersion(jdkVersion);
        loc.setPath(path.toString());
        return loc;
    }

    public static void checkReadOnly(NutsWorkspace ws) {
        if (ws.config().isReadOnly()) {
            throw new NutsReadOnlyException(ws,ws.config().getWorkspaceLocation().toString());
        }
    }

    public static NutsFetchCommand validateSession(NutsWorkspace ws, NutsFetchCommand fetch) {
        if (fetch.getSession() == null) {
            fetch = fetch.setSession(ws.createSession());
        }
        return fetch;
    }

    public static NutsSession validateSession(NutsWorkspace ws, NutsSession session) {
        if (session == null) {
            session = ws.createSession();
        }
        return session;
    }

    public static NutsId configureFetchEnv(NutsWorkspace ws, NutsId id) {
        Map<String, String> qm = id.getQueryMap();
        if (qm.get(NutsConstants.QueryKeys.FACE) == null && qm.get("arch") == null && qm.get("os") == null && qm.get("osdist") == null && qm.get("platform") == null) {
            qm.put("arch", ws.config().getPlatformArch().toString());
            qm.put("os", ws.config().getPlatformOs().toString());
            if (ws.config().getPlatformOsDist() != null) {
                qm.put("osdist", ws.config().getPlatformOsDist().toString());
            }
            return id.setQuery(qm);
        }
        return id;
    }

    public static List<NutsRepository> _getEnabledRepositories(NutsWorkspace parent, NutsRepositoryFilter repositoryFilter) {
        List<NutsRepository> repos = new ArrayList<>();
        List<NutsRepository> subrepos = new ArrayList<>();
        NutsWorkspace ws = (NutsWorkspace) parent;
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

    public static List<NutsRepository> filterRepositories(NutsWorkspace ws, NutsRepositorySupportedAction fmode, NutsId id, NutsRepositoryFilter repositoryFilter, NutsFetchMode mode, NutsFetchCommand options) {
        return filterRepositories(ws, fmode, id, repositoryFilter, true, null, mode, options);
    }

    public static List<NutsRepository> filterRepositories(NutsWorkspace ws, NutsRepositorySupportedAction fmode, NutsId id, NutsRepositoryFilter repositoryFilter, boolean sortByLevelDesc, final Comparator<NutsRepository> postComp, NutsFetchMode mode, NutsFetchCommand options) {

        List<RepoAndLevel> repos2 = new ArrayList<>();
        //        List<Integer> reposLevels = new ArrayList<>();
        for (NutsRepository repository : ws.config().getRepositories()) {
            if (repository.config().isEnabled() && (repositoryFilter == null || repositoryFilter.accept(repository))) {
                int t = 0;
                try {
                    t = repository.config().getSupportLevel(fmode, id, mode, options.isTransitive());
                } catch (Exception e) {
                    //ignore...
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

    public static void checkSimpleNameNutsId(NutsWorkspace workspace, NutsId id) {
        if (id == null) {
            throw new NutsIllegalArgumentException(workspace, "Missing id");
        }
        if (CoreStringUtils.isBlank(id.getGroup())) {
            throw new NutsIllegalArgumentException(workspace, "Missing group for " + id);
        }
        if (CoreStringUtils.isBlank(id.getName())) {
            throw new NutsIllegalArgumentException(workspace, "Missing name for " + id);
        }
    }
    public static void checkLongNameNutsId(NutsWorkspace workspace, NutsId id) {
        checkSimpleNameNutsId(workspace,id);
        if (CoreStringUtils.isBlank(id.getVersion().toString())) {
            throw new NutsIllegalArgumentException(workspace, "Missing version for " + id);
        }
    }

    public static void validateRepositoryName(NutsWorkspace ws, String repositoryName, Set<String> registered) {
        if (!repositoryName.matches("[a-zA-Z][.a-zA-Z0-9_-]*")) {
            throw new NutsIllegalArgumentException(ws, "Invalid repository id " + repositoryName);
        }
        if (registered.contains(repositoryName)) {
            throw new NutsRepositoryAlreadyRegisteredException(ws,repositoryName);
        }
    }

    public static NutsId parseRequiredNutsId(NutsWorkspace ws, String nutFormat) {
        NutsId id = CoreNutsUtils.parseNutsId(nutFormat);
        if (id == null) {
            throw new NutsParseException(ws,"Invalid Id format : " + nutFormat);
        }
        return id;
    }

    public static NutsId findNutsIdBySimpleNameInStrings(NutsWorkspace ws, NutsId id, Collection<String> all) {
        if (all != null) {
            for (String nutsId : all) {
                if (nutsId != null) {
                    NutsId nutsId2 = parseRequiredNutsId(ws, nutsId);
                    if (nutsId2.equalsSimpleName(id)) {
                        return nutsId2;
                    }
                }
            }
        }
        return null;
    }

    public static void checkSession(NutsWorkspace ws, NutsRepositorySession session) {
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

    public static NutsIdFormat getIdFormat(NutsWorkspace ws) {
        String k = DefaultSearchFormatPlain.class.getName() + "#NutsIdFormat";
        NutsIdFormat f = (NutsIdFormat) ws.getUserProperties().get(k);
        if (f == null) {
            f = ws.formatter().createIdFormat();
            ws.getUserProperties().put(k, f);
        }
        return f;
    }

    public static NutsDescriptorFormat getDescriptorFormat(NutsWorkspace ws) {
        String k = DefaultSearchFormatPlain.class.getName() + "#NutsDescriptorFormat";
        NutsDescriptorFormat f = (NutsDescriptorFormat) ws.getUserProperties().get(k);
        if (f == null) {
            f = ws.formatter().createDescriptorFormat();
            ws.getUserProperties().put(k, f);
        }
        return f;
    }

//    public static void traceJson(NutsWorkspace ws, Object o, PrintStream out) {
//        ws.io().writeJson(o, out, true);
//        out.println();
//    }
//
//    public static void traceProperties(NutsWorkspace ws, Object o, PrintStream out) {
//        ws.io().writeJson(o, out, true);
//        out.println();
//    }
    public static <T> Iterator<T> decorateTrace(NutsWorkspace ws, Iterator<T> it, NutsSession session, PrintStream out, NutsOutputFormat oformat, NutsIncrementalFormat format, NutsFetchDisplayOptions displayOptions) {
        return new NutsTraceIterator<>(it, ws, out, oformat, format, displayOptions, session);
    }

    public static <T> Iterator<T> decorateTrace(NutsWorkspace ws, Iterator<T> it, NutsSession session, NutsOutputFormat oformat, NutsIncrementalFormat format, NutsFetchDisplayOptions displayOptions) {
        final PrintStream out = NutsWorkspaceUtils.validateSession(ws, session).getTerminal().getOut();
        return new NutsTraceIterator<>(it, ws, out, oformat, format, displayOptions, session);
    }
}

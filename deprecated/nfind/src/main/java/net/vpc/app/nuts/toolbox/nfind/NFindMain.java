package net.vpc.app.nuts.toolbox.nfind;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsApplication;
import net.vpc.common.strings.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.*;
import net.vpc.common.util.Converter;
import net.vpc.common.util.IteratorBuilder;
import net.vpc.common.util.IteratorUtils;

/**
 * @deprecated since 0.5.5
 * @author vpc
 * @deprecated
 */
@Deprecated
public class NFindMain extends NutsApplication {

    public static void main(String[] args) {
        new NFindMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext context) {
        NutsCommandLine cmdLine = context.getCommandLine();
        int currentFindWhat = 0;
        List<FindWhat> findWhats = new ArrayList<>();
        FindContext findContext = new FindContext();
        findContext.context = context;
        findContext.out = context.out();
        findContext.err = context.err();
        findContext.search = context.getWorkspace().search();
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {
                //
            }else if (findContext.search.configureFirst(cmdLine)) {
                //
//            } else if (cmdLine.readAllOnce("-js", "--javascript")) {
//                if (currentFindWhat + 1 >= findWhats.size()) {
//                    findWhats.add(new FindWhat(this));
//                }
//                if (findWhats.get(currentFindWhat).nonjs.size() > 0) {
//                    if (!cmdLine.isExecMode()) {
//                        return;
//                    }
//                    throw new NutsExecutionException("find: Unsupported mixed and non js find expressions", 1);
//                }
//                findContext.jsflag = true;
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-x", "--expression")) {
//                findContext.jsflag = false;
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-l", "--long")) {
//                findContext.longFormat = true;
//            } else if (currentFindWhat == 0 && (a = cmdLine.readBooleanOption("--omit-group")) != null) {
//                findContext.omitGroup = a.getBooleanValue();
//            } else if (currentFindWhat == 0 && (a = cmdLine.readBooleanOption("--omit-imported")) != null) {
//                findContext.omitImportedGroup = a.getBooleanValue();
//            } else if (currentFindWhat == 0 && (a = cmdLine.readBooleanOption("--highlight-imported")) != null) {
//                findContext.highlightImportedGroup = a.getBooleanValue();
//            } else if (currentFindWhat == 0 && (a = cmdLine.readBooleanOption("--omit-namespace")) != null) {
//                findContext.omitNamespace = a.getBooleanValue();
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-f", "--file")) {
//                findContext.showFile = true;
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("--file-name-only")) {
//                findContext.fileNameOnly = true;
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("--file-path-only")) {
//                findContext.filePathOnly = true;
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-c", "--class")) {
//                findContext.showClass = true;
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-F", "--offline")) {
//                findContext.fetchMode = SearchMode.OFFLINE;
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-O", "--online")) {
//                findContext.fetchMode = SearchMode.ONLINE;
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-R", "--remote")) {
//                findContext.fetchMode = SearchMode.REMOTE;
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-M", "--commitable")) {
//                findContext.fetchMode = SearchMode.COMMIT;
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-U", "--updatable")) {
//                findContext.fetchMode = SearchMode.UPDATE;
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-T", "--status")) {
//                findContext.fetchMode = SearchMode.STATUS;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-e", "--exec")) {
                findContext.executable = true;
                findContext.library = false;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-!e", "--lib")) {
                findContext.executable = false;
                findContext.library = true;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-s", "--descriptor")) {
                findContext.desc = true;
                findContext.eff = false;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-g", "--installed-dependencies")) {
                findContext.installedDependencies = true;
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-d", "--dependencies")) {
//                findContext.display = "dependencies";
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-d", "--dependencies-tree")) {
//                findContext.display = "dependencies-tree";
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-i", "--installed")) {
                findContext.installed = true;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-!i", "--non-installed")) {
                findContext.installed = false;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-u", "--updatable")) {
                findContext.updatable = true;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-!u", "--non-updatable")) {
                findContext.updatable = false;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-b", "--effective-descriptor")) {
                findContext.desc = true;
                findContext.eff = true;
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-I", "--display-id")) {
//                findContext.display = "id";
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-N", "--display-name")) {
//                findContext.display = "name";
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-P", "--display-packaging")) {
//                findContext.display = "packaging";
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-A", "--display-arch")) {
//                findContext.display = "arch";
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-L", "--display-file")) {
//                findContext.display = "file";
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-C", "--display-class")) {
//                findContext.display = "class";
//            } else if (currentFindWhat == 0 && (a = cmdLine.readBooleanOption("--test")) != null) {
//                if (a.getBooleanValue()) {
//                    findContext.scopes = NutsDependencyScope.add(findContext.scopes, NutsDependencyScope.GROUP_TEST);
//                } else {
//                    findContext.scopes = NutsDependencyScope.remove(findContext.scopes, NutsDependencyScope.GROUP_TEST);
//                }
//            } else if (currentFindWhat == 0 && (a = cmdLine.readBooleanOption("--provided")) != null) {
//                if (a.getBooleanValue()) {
//                    findContext.scopes = NutsDependencyScope.add(findContext.scopes, NutsDependencyScope.PROVIDED);
//                } else {
//                    findContext.scopes = NutsDependencyScope.remove(findContext.scopes, NutsDependencyScope.PROVIDED);
//                }
//            } else if (currentFindWhat == 0 && (a = cmdLine.readBooleanOption("--compile")) != null) {
//                if (a.getBooleanValue()) {
//                    findContext.scopes = NutsDependencyScope.add(findContext.scopes, NutsDependencyScope.GROUP_COMPILE);
//                } else {
//                    findContext.scopes = NutsDependencyScope.remove(findContext.scopes, NutsDependencyScope.GROUP_COMPILE);
//                }
//            } else if (currentFindWhat == 0 && (a = cmdLine.readBooleanOption("--scope-other")) != null) {
//                if (a.getBooleanValue()) {
//                    findContext.scopes = NutsDependencyScope.add(findContext.scopes, NutsDependencyScope.OTHER);
//                } else {
//                    findContext.scopes = NutsDependencyScope.remove(findContext.scopes, NutsDependencyScope.GROUP_COMPILE);
//                }
//            } else if (currentFindWhat == 0 && (a = cmdLine.readOption("--scope-all")) != null) {
//                findContext.scopes = NutsDependencyScope.ALL.expand();
//            } else if (currentFindWhat == 0 && (a = cmdLine.readBooleanOption("--optional")) != null) {
//                findContext.acceptOptional = a.getBooleanValue() ? null : false;
//            } else if (currentFindWhat == 0 && (a = cmdLine.readOption("--optional-only")) != null) {
//                findContext.acceptOptional = true;
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-p", "--pkg")) {
//                findContext.pack.add(cmdLine.readRequiredNonOption(cmdLine.createNonOption("Packaging")).getString());
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-a", "--arch")) {
//                findContext.arch.add(cmdLine.readRequiredNonOption(cmdLine.createNonOption("Architecture")).getString());
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-r", "--repo")) {
//                findContext.repos.add(cmdLine.readRequiredNonOption(cmdLine.createNonOption("Repository")).getString());
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-V", "--last-version")) {
//                findContext.allVersions = false;
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-v", "--all-versions")) {
//                findContext.allVersions = true;
//            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-Y", "--summary")) {
//                findContext.showSummary = true;
//            } else {
//                NutsArgument val = cmdLine.readRequiredNonOption(cmdLine.createNonOption("Expression"));
//                if (currentFindWhat + 1 >= findWhats.size()) {
//                    findWhats.add(new FindWhat(this));
//                }
//                if (cmdLine.isExecMode()) {
//                    if (findContext.jsflag) {
//                        if (findWhats.get(currentFindWhat).jsCode == null) {
//                            findWhats.get(currentFindWhat).jsCode = val.getString();
//                        } else {
//                            throw new NutsExecutionException("find: Unsupported mixed and non js find expressions", 1);
//                        }
//                    } else {
//                        String arg = val.getString();
//                        if (findWhats.get(currentFindWhat).jsCode == null) {
//                            findWhats.get(currentFindWhat).nonjs.add(arg);
//                        } else {
//                            throw new NutsExecutionException("find: Unsupported mixed and non js find expressions", 1);
//                        }
//                    }
//                }
//                currentFindWhat++;
            }
        }
        if (!cmdLine.isExecMode()) {
            return;
        }

        if (findContext.installedDependencies == null) {
            findContext.installedDependencies = false;
        }
        if (findWhats.isEmpty()) {
            FindWhat w = new FindWhat(this);
            w.nonjs.add("*");
            findWhats.add(w);
        }
        if (findContext.fetchMode == SearchMode.STATUS) {
            findContext.fetchMode = SearchMode.COMMIT;
            for (FindWhat findWhat : findWhats) {
                display((find(findWhat, findContext)), findContext, "===Packages to COMMIT===:");
            }

            findContext.fetchMode = SearchMode.UPDATE;
            for (FindWhat findWhat : findWhats) {
                display((find(findWhat, findContext)), findContext, "===Packages to UPDATE===:");
            }
        } else {
            for (FindWhat findWhat : findWhats) {
                long from = System.nanoTime();
                Iterator<NutsIdExt> it = find(findWhat, findContext);
                long to = System.nanoTime();
                findContext.executionTimeNano = to - from;
                findContext.executionSearch = findWhat;
                display(it, findContext, null);
            }
        }
    }

    private Iterator<NutsIdExt> toext(Iterator<NutsId> list) {
        return IteratorBuilder.of(list).map(new Converter<NutsId, NutsIdExt>() {
            @Override
            public NutsIdExt convert(NutsId nutsId) {
                return new NutsIdExt(nutsId, null);
            }
        }).iterator();
    }

    private Iterator<NutsIdExt> find(FindWhat findWhat, final FindContext findContext) {
        if (findWhat.nonjs.isEmpty() && findWhat.jsCode == null) {
            findWhat.nonjs.add("*");
        }
        NutsSearchCommand query = findContext.context.getWorkspace().search()
                .scripts(findWhat.jsCode)
                .ids(findWhat.nonjs.toArray(new String[0]))
                .archs(findContext.arch)
                .packagings(findContext.pack)
                .repositories(findContext.repos)
                .sort(findContext.sort)
                .allVersions(findContext.allVersions)
                .duplicateVersions(findContext.duplicateVersions)
                .setSession(findContext.context.getSession())
                .transitive(findContext.transitive);

        NutsWorkspace ws = findContext.context.getWorkspace();
        switch (findContext.fetchMode) {
            case ONLINE: {
                return toext(searchOnline(findContext, query, ws));
            }
            case OFFLINE: {
                return toext(searchOffline(findContext, query, ws));
            }
            case REMOTE: {
                return toext(searchRemote(findContext, query, ws));
            }
            case COMMIT: {
                return searchCommit(findContext, query, ws);
            }
            case UPDATE: {
                return searchUpdate(findContext, query, ws);
            }
            case STATUS: {
                throw new NutsIllegalArgumentException("find: Unsupported 'status' fetch mode");
            }
        }
        return Collections.emptyIterator();
    }

    private Iterator<NutsIdExt> searchUpdate(FindContext findContext, NutsSearchCommand query, NutsWorkspace ws) {
        Map<String, NutsId> local = new LinkedHashMap<>();
        for (NutsId nutsId : query.setSession(findContext.context.getSession()).setTransitive(findContext.transitive)
                .offline().getResultIds()) {
            NutsId r = local.get(nutsId.getSimpleName());
            if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                local.put(nutsId.getSimpleName(), nutsId);
            }
        }
        Map<String, NutsId> remote = new LinkedHashMap<>();
        for (NutsId nutsId : query.setSession(findContext.context.getSession()).setTransitive(findContext.transitive)
                .remote()
                .getResultIds()) {
            NutsId r = remote.get(nutsId.getSimpleName());
            if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                remote.put(nutsId.getSimpleName(), nutsId);
            }
        }

        //force search of all local nutIds because some repositories could not make a wildcard search...
        for (NutsId localNutsId : local.values()) {
            for (NutsId nutsId : ws.search().addId(localNutsId.toString()).setSession(findContext.context.getSession())
                    .setTransitive(findContext.transitive).remote().getResultIds()) {
                NutsId r = remote.get(nutsId.getSimpleName());
                if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                    remote.put(nutsId.getSimpleName(), nutsId);
                }
            }
        }

        Map<String, NutsIdExt> ret = new LinkedHashMap<>();
        for (NutsId localNutsId : local.values()) {
            NutsId remoteNutsId = remote.get(localNutsId.getSimpleName());
            if (remoteNutsId != null && localNutsId.getVersion().compareTo(remoteNutsId.getVersion()) >= 0) {
                remote.remove(localNutsId.getSimpleName());
            } else if (remoteNutsId != null) {
                ret.put(localNutsId.getSimpleName(), new NutsIdExt(remoteNutsId, "(local: " + localNutsId.getVersion().toString() + ")"));
            }
        }
        return new ArrayList<NutsIdExt>(ret.values()).iterator();
    }

    private Iterator<NutsIdExt> searchCommit(FindContext findContext, NutsSearchCommand query, NutsWorkspace ws) {
        Map<String, NutsId> local = new LinkedHashMap<>();
        Map<String, NutsId> remote = new LinkedHashMap<>();
        for (NutsId nutsId : query.setSession(findContext.context.getSession()).setTransitive(findContext.transitive).offline().getResultIds()) {
            NutsId r = local.get(nutsId.getSimpleName());
            if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                local.put(nutsId.getSimpleName(), nutsId);
            }
        }
        for (NutsId nutsId : query.setSession(findContext.context.getSession()).setTransitive(findContext.transitive).wired().getResultIds()) {
            NutsId r = remote.get(nutsId.getSimpleName());
            if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                remote.put(nutsId.getSimpleName(), nutsId);
            }
        }

        //force search of all local nutIds because some repositories could not make a wildcard search...
        for (NutsId localNutsId : local.values()) {
            for (NutsId nutsId : ws.search().addId(localNutsId.toString()).setSession(findContext.context.getSession().copy()
            ).setTransitive(findContext.transitive).wired().getResultIds()) {
                NutsId r = remote.get(nutsId.getSimpleName());
                if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                    remote.put(nutsId.getSimpleName(), nutsId);
                }
            }
        }

        Map<String, NutsIdExt> ret = new LinkedHashMap<>();

        for (NutsId remoteNutsId : remote.values()) {
            NutsId localNutsId = local.get(remoteNutsId.getSimpleName());
            if (localNutsId != null && remoteNutsId.getVersion().compareTo(localNutsId.getVersion()) >= 0) {
//                local.remove(nutsId.getSimpleName());
            } else if (localNutsId != null) {
                ret.put(remoteNutsId.getSimpleName(), new NutsIdExt(localNutsId, "(remote: " + remoteNutsId.getVersion().toString() + ")"));
            }
        }
        return new ArrayList<NutsIdExt>(ret.values()).iterator();
    }

    private Iterator<NutsId> searchFetchType(FindContext findContext, NutsSearchCommand query, NutsFetchStrategy m) {
        return query.setFetchStratery(m).getResultIds().iterator();
    }

    private Iterator<NutsId> searchRemote(FindContext findContext, NutsSearchCommand query, NutsWorkspace ws) {
        return searchFetchType(findContext, query, NutsFetchStrategy.REMOTE);
    }

    private Iterator<NutsId> searchOffline(FindContext findContext, NutsSearchCommand query, NutsWorkspace ws) {
        return searchFetchType(findContext, query, NutsFetchStrategy.OFFLINE);
    }

    private Iterator<NutsId> searchOnline(FindContext findContext, NutsSearchCommand query, NutsWorkspace ws) {
        return searchFetchType(findContext, query, NutsFetchStrategy.ONLINE);
        //display(nutsIdIterator,findContext);
    }

    private void display(Iterator<NutsIdExt> nutsList, FindContext findContext, String header) {
        Set<String> visitedItems = new HashSet<>();
        NutsWorkspace ws = findContext.context.getWorkspace();
        Set<String> visitedPackaging = new HashSet<>();
        Set<String> visitedArchs = new HashSet<>();
        if (!findContext.longFormat) {
            //if not long flag, should remove namespace and duplicates
            nutsList = IteratorUtils.unique(nutsList, (nutsId) -> new NutsIdExt(
                    nutsId.id.builder().setNamespace(null).setFace(null).setQuery("").build(),
                    nutsId.extra
            ));
        }
        int count = 0;
        while (nutsList.hasNext()) {
            if (count == 0 && header != null) {
                findContext.out.println(header);
            }
            count++;
            NutsIdExt nutsId = nutsList.next();
            NutsInfo info = new NutsInfo(nutsId, findContext.context);
            if (findContext.installed != null) {
                if (findContext.installed != info.isInstalled(findContext.installedDependencies)) {
                    continue;
                }
            }
            if (findContext.updatable != null) {
                if (findContext.updatable != info.isUpdatable()) {
                    continue;
                }
            }
            if (!findContext.executable || !findContext.library) {
                try {
                    if (info.getDescriptor().isExecutable() != findContext.executable) {
                        continue;
                    }
                } catch (Exception ex) {
                    try {
                        info.getDescriptor();
                    } catch (Exception ex2) {
                        //
                    }
                    findContext.err.print("Error Resolving " + info.nuts + " :: " + ex.getMessage() + "\n");
                    continue;
                }
            }
            switch (findContext.display) {
                case "id":
                case "dependencies": {
                    Set<String> imports = new HashSet<String>(Arrays.asList(ws.config().getImports()));
                    findContext.out.print(toStringId(findContext, info));
                    findContext.out.println();
                    if (findContext.desc) {
                        NutsDescriptor descriptor = null;
                        String descriptorError = null;
                        try {
                            descriptor = info.getDescriptor();
                        } catch (Exception ex) {
                            descriptorError = ex.getMessage();
                        }
                        findContext.out.printf(descriptor == null ? ("Error Retrieving Descriptor : %s (%s)\n") : "%s (%s)\n", descriptor.toString(), descriptorError);
                        findContext.out.println("");
                    }
                    if ("dependencies".equals(findContext.display)) {
                        printDependencyList(findContext, ws, info, imports);
                    }
                    break;
                }
                case "dependencies-tree": {
                    printDependencyTree(findContext, info);
                    break;
                }
                case "name": {
                    String fullName = info.nuts.getSimpleName();
                    if (!visitedItems.contains(fullName)) {
                        visitedItems.add(fullName);
                        if (findContext.longFormat) {
                            NutsDescriptor d = null;
                            try {
                                d = info.getDescriptor();
                            } catch (NutsException ex) {
                                //
                            }
                            String status = (info.isInstalled(findContext.installedDependencies) ? "i"
                                    : info.isFetched() ? "f"
                                    : "r") + (info.isUpdatable() ? "u" : ".");
                            findContext.out.print(status);
                            findContext.out.print(" ");
                            findContext.out.print(d == null ? "?" : (d.getPackaging() == null ? "" : d.getPackaging()));
                            findContext.out.print(" ");
                            findContext.out.print(Collections.singletonList(d == null ? "?" : d.getArch()));
                            findContext.out.print(" ");
                            findContext.out.printf("%s\n", info.nuts.getSimpleName());
                        } else {
                            findContext.out.printf("%s\n", info.nuts.getSimpleName());
                        }
                    }
                    break;
                }
                case "file": {
                    Path fullName = info.getFile();
                    if (fullName != null) {
                        findContext.out.printf("%s\n", fullName.toString());
                    }
                    break;
                }
                case "class": {
                    for (NutsExecutionEntry entry : ws.parser().parseExecutionEntries(info.getFile())) {
                        if (!visitedItems.contains(entry.getName())) {
                            visitedItems.add(entry.getName());
                            findContext.out.printf("%s\n", entry.getName());
                        }
                    }
                    break;
                }
                case "packaging": {
                    NutsDescriptor d = null;
                    try {
                        d = info.getDescriptor();
                    } catch (NutsException ex) {
                        //
                    }
                    if (d != null) {
                        String p = d.getPackaging();
                        if (!StringUtils.isEmpty(p) && !visitedPackaging.contains(p)) {
                            visitedPackaging.add(p);
                            findContext.out.printf("%s\n", p);
                        }
                    }
                    break;
                }
                case "arch": {
                    NutsDescriptor d = null;
                    try {
                        d = info.getDescriptor();
                    } catch (NutsException ex) {
                        //
                    }
                    if (d != null) {
                        for (String p : d.getArch()) {
                            if (!StringUtils.isEmpty(p) && !visitedArchs.contains(p)) {
                                visitedArchs.add(p);
                                findContext.out.printf("%s\n", p);
                            }
                        }
                    }
                    break;
                }
            }
        }
        if (count == 0) {
            findContext.err.printf("Nuts not found : %s\n", findContext.executionSearch);
        } else {
            if (findContext.showSummary) {
                findContext.out.println();
                findContext.out.printf("===%s=== nuts found in ===%s===s\n",
                        count,
                        (findContext.executionTimeNano / 1000000 / 1000.0)
                );
            }
        }
    }

    private String toStringId(FindContext findContext, NutsInfo info) {
        if (findContext.longFormat) {
            return (toStringLongId(findContext, info));
        } else {
            return (toStringShortId(findContext, info));
        }
    }

    private String toStringLongId(FindContext findContext, NutsInfo info) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(b);
        printLongId(findContext, info, out);
        out.flush();
        return b.toString();
    }

    private void printLongId(FindContext findContext, NutsInfo info, PrintStream out) {
        NutsDescriptor descriptor = null;
        String descriptorError = null;
        try {
            descriptor = info.getDescriptor();
        } catch (Exception ex) {
            descriptorError = ex.getMessage();
        }
        String status = (info.isInstalled(findContext.installedDependencies) ? "i"
                : info.isFetched() ? "."
                : "r")
                + (info.isUpdatable() ? "u" : ".")
                + (descriptor == null ? "?"
                        : (descriptor.isNutsApplication() ? "X" : descriptor.isExecutable() ? "x" : "."));
        findContext.out.print("**" + status + "**");
        findContext.out.print(" ");
        findContext.out.print(descriptor == null ? "?  " : (StringUtils.isEmpty(descriptor.getPackaging()) ? "" : descriptor.getPackaging()));
        findContext.out.print(" ");
        findContext.out.print(Arrays.asList(descriptor == null ? new String[0] : descriptor.getArch()));
        findContext.out.print(" ");
        if (StringUtils.isEmpty(info.nuts.getNamespace())) {
            //findContext.out.print("");
        } else {
            findContext.out.print(info.nuts.getNamespace());
        }
        findContext.out.print(" ");
        printShortId(findContext, info);
        if (!StringUtils.isEmpty(descriptorError)) {
            findContext.out.print(" [[" + descriptorError + "]]");
        }
    }

    private List<NutsDependency> findImmediateDependencies(FindContext findContext, String id) {
        NutsWorkspace ws = findContext.context.getWorkspace();
        NutsSession session = findContext.context.getSession();
        List<NutsDependency> all = new ArrayList<>();
        NutsId nid = ws.parser().parseId(id);
        if (!nid.getVersion().isSingleValue()) {
            return new ArrayList<>();
        }
        for (NutsDependency dependency : ws.fetch().id(id).session(session.setProperty("monitor-allowed", false)).effective().getResultDescriptor()
                .getDependencies(findContext.equivalentDependencyFilter)) {
            all.add(dependency);
        }
        ArrayList<NutsDependency> a = new ArrayList<>(all);
//        Collections.sort(a, new Comparator<NutsDependency>() {
//            @Override
//            public int compare(NutsDependency o1, NutsDependency o2) {
//                if (o1 == null || o2 == null) {
//                    if (o1 == o2) {
//                        return 0;
//                    }
//                    if (o1 == null) {
//                        return -1;
//                    }
//                    return 1;
//                }
//                return o1.getId().toString().compareTo(o2.getId().toString());
//            }
//        });
        return a;
    }

    private NutsInfo prepareDependencyTreeChildren(FindContext findContext, NutsInfo info, Set<String> visited) {
        boolean found = visited.contains(info.nuts.getLongName());
        if (info.children == null) {
            List<NutsDependency> immediateSelfDependencies = new ArrayList<>();
            try {
                immediateSelfDependencies = findImmediateDependencies(findContext, info.nuts.toString());
            } catch (Exception ex) {
                info.error = true;
            }
            List<NutsInfo> t = new ArrayList<>();
            for (NutsDependency dd : immediateSelfDependencies) {
                try {
                    NutsInfo dinfo = new NutsInfo(new NutsIdExt(dd.getId(), null), findContext.context);
                    t.add(dinfo);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            info.children = t;
        }

        if (!found) {
            visited.add(info.nuts.getLongName());
        } else {
            info.continued = info.children.size() > 0;
            info.children = new ArrayList<>();
        }
        return info;
    }

    private void printDependencyTree(FindContext findContext, NutsInfo info) {

        NutsTreeFormat f = findContext.context.getWorkspace().formatter().createTreeFormat()
                .setModel(new net.vpc.app.nuts.NutsTreeModel() {
                    Set<String> visited = new HashSet<>();

                    @Override
                    public Object getRoot() {
                        return prepareDependencyTreeChildren(findContext, info, visited);
                    }

                    @Override
                    public List getChildren(Object oo) {
                        NutsInfo o = (NutsInfo) oo;
                        List<NutsInfo> c = o.children;
                        for (NutsInfo nutsInfo : c) {
                            prepareDependencyTreeChildren(findContext, nutsInfo, visited);
                        }
                        return c;
                    }
                }).setNodeFormat(new NutsTreeNodeFormat() {
            @Override
            public String format(Object o, int depth) {
                return toStringId(findContext, (NutsInfo) o);
            }
        });
        f.print(findContext.out);
    }

    private void printDependencyList(FindContext findContext, NutsWorkspace ws, NutsInfo info, Set<String> imports) {
        NutsFetchStrategy m = getNutsFetchMode(findContext.fetchMode);
        NutsSession session = findContext.context.getSession();
        List<NutsDefinition> depsFiles = ws.search().setSession(session).setFetchStratery(m)
                .setTransitive(findContext.transitive)
                .addId(info.nuts)
                .scopes(findContext.scopes)
                .setDependencyFilter(findContext.equivalentDependencyFilter)
                .setAllVersions(findContext.allVersions)
                .setAcceptOptional(findContext.acceptOptional)
                .dependenciesOnly()
                .sort()
                .getResultDefinitions().list();
        Set<String> immediateSelfDependencies = toDependencySet(ws.fetch().id(info.nuts).setSession(session).effective(false).getResultDescriptor().getDependencies());
        Set<String> immediateInheritedDependencies = toDependencySet(ws.fetch().id(info.nuts).setSession(session).effective(true).getResultDescriptor().getDependencies());
        int packagingSize = 3;
        int archSizeSize = 2;
        for (NutsDefinition dd : depsFiles) {
            packagingSize = Math.max(packagingSize, dd.getDescriptor().getPackaging() == null ? 0 : dd.getDescriptor().getPackaging().length());
            archSizeSize = Math.max(archSizeSize, Arrays.asList(dd.getDescriptor().getArch()).toString().length());
        }
        Collections.sort(depsFiles, new Comparator<NutsDefinition>() {
            @Override
            public int compare(NutsDefinition o1, NutsDefinition o2) {
                String s1 = format(findContext, o1.getId(), null, ws);
                String s2 = format(findContext, o2.getId(), null, ws);
                return s1.compareTo(s2);
            }
        });
        for (NutsDefinition dd : depsFiles) {
            NutsInfo dinfo = new NutsInfo(new NutsIdExt(dd.getId(), null), findContext.context);
            dinfo.descriptor = dd.getDescriptor();
            String format = "";
            if (findContext.filePathOnly) {
                if (dinfo.getFile() == null) {
                    format += ("@@FILE NOT FOUND@@ ");
                    format += (format(findContext, dinfo.nuts, dinfo.desc, ws));
                } else {
                    format += (dinfo.getFile().toString());
                }
            } else if (findContext.fileNameOnly) {
                if (dinfo.getFile() == null) {
                    format += ("@@FILE NOT FOUND@@ ");
                    format += (format(findContext, dinfo.nuts, dinfo.desc, ws));
                } else {
                    format += (dinfo.getFile().getFileName().toString());
                }
            } else {
                format += format(findContext, dinfo.nuts, dinfo.desc, ws);
            }
            if (findContext.longFormat) {
                String status = buildDependencyStatus(findContext, immediateSelfDependencies, immediateInheritedDependencies, dd, dinfo);
                findContext.out.print("\t");
                findContext.out.printf("**%s**", status);
                findContext.out.print(" ");
                findContext.out.printf("%s", StringUtils.alignLeft(dinfo.getDescriptor().getPackaging(), packagingSize));
                findContext.out.print(" ");
                findContext.out.printf("%s", StringUtils.alignLeft(Arrays.asList(dinfo.getDescriptor().getArch()).toString(), archSizeSize));
                findContext.out.print(" ");
                findContext.out.println(format);
            } else {
                findContext.out.print("\t");
                findContext.out.println(format);
            }
        }
    }

    private String buildDependencyStatus(FindContext findContext, Set<String> immediateSelfDependencies, Set<String> immediateInheritedDependencies, NutsDefinition dd, NutsInfo dinfo) {
        String status = (dinfo.isInstalled(findContext.installedDependencies) ? "i"
                : dinfo.isFetched() ? "."
                : "r");
        status += (dinfo.isUpdatable() ? "u" : ".");
        status += (dd.getDescriptor().isNutsApplication() ? "X" : dd.getDescriptor().isExecutable() ? "x" : ".");
        if (immediateSelfDependencies.contains(dd.getId().getLongName())) {
            status += ".";
        } else if (immediateInheritedDependencies.contains(dd.getId().getLongName())) {
            status += "p";
        } else {
            status += "d";
        }
        switch (StringUtils.trim(dd.getId().getScope()).toLowerCase()) {
            case "":
            case "compile": {
                status += ".";
                break;
            }
            case "runtime": {
                status += "m";
                break;
            }
            case "test": {
                status += "t";
                break;
            }
            case "system": {
                status += "s";
                break;
            }
            case "provided": {
                status += "p";
                break;
            }
            default: {
                status += "?";
                break;
            }
        }
        return status;
    }

    private String toStringShortId(FindContext findContext, NutsInfo info) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(b);
        printShortId(findContext, info, out);
        out.flush();
        return b.toString();
    }

    private void printShortId(FindContext findContext, NutsInfo info) {
        printShortId(findContext, info, findContext.out);
    }

    private void printShortId(FindContext findContext, NutsInfo info, PrintStream out) {
        NutsWorkspace ws = findContext.context.getWorkspace();
        if (findContext.filePathOnly) {
            if (info.getFile() == null) {
                out.print("@@FILE NOT FOUND@@ ");
                out.print(format(findContext, info.nuts, info.desc, ws));
            } else {
                out.print(info.getFile().toString());
            }
            return;
        }
        if (findContext.fileNameOnly) {
            if (info.getFile() == null) {
                out.print("@@FILE NOT FOUND@@ ");
                out.print(format(findContext, info.nuts, info.desc, ws));
            } else {
                out.print(info.getFile().getFileName().toString());
            }
            return;
        }
        out.print(format(findContext, info.nuts, info.desc, ws));
        if (findContext.showFile) {
            out.print(" ");
            if (info.getFile() == null) {
                out.print("?");
            } else {
                out.print(info.getFile().toString());
            }
        }
        if (findContext.showClass) {
            out.print(" ");
            if (info.getFile() == null) {
                out.print("?");
            } else {
                NutsExecutionEntry[] cls = ws.parser().parseExecutionEntries(info.getFile());
                if (cls.length == 0) {
                    out.print("?");
                } else if (cls.length == 1) {
                    out.print(cls[0].getName());
                } else {
                    out.print(Arrays.toString(cls));
                }
            }
        }
        if (info.error) {
            out.print(" @@MISSING@@");
        }
        if (info.continued) {
            out.print(" ...");
        }
    }

    private Set<String> toDependencySet(NutsDependency[] all) {
        Set<String> set = new HashSet<>();
        for (NutsDependency nutsDependency : all) {
            set.add(nutsDependency.getLongName());
        }
        return set;
    }

    private NutsFetchStrategy getNutsFetchMode(SearchMode fecthMode) {
        NutsFetchStrategy m = null;
        switch (fecthMode) {
            case ONLINE: {
                m = NutsFetchStrategy.ONLINE;
                break;
            }
            case OFFLINE: {
                m = NutsFetchStrategy.OFFLINE;
                break;
            }
            case REMOTE: {
                m = NutsFetchStrategy.REMOTE;
                break;
            }
            case COMMIT: {
                m = NutsFetchStrategy.ONLINE;
                break;
            }
            case UPDATE: {
                m = NutsFetchStrategy.ONLINE;
                break;
            }
        }
        return m;
    }

    private String format(FindContext findContext, NutsId id, String desc, NutsWorkspace ws) {
        StringBuilder sb = new StringBuilder();
        sb.append(ws.formatter().createIdFormat()
                .setHighlightImportedGroup(true)
                .setHighlightOptional(true)
                .setHighlightScope(true)
                .setOmitEnv(true)
                .setOmitFace(true)
                .setHighlightImportedGroup(findContext.highlightImportedGroup)
                .setOmitGroup(findContext.omitGroup)
                .setOmitImportedGroup(findContext.omitImportedGroup)
                .setOmitNamespace(findContext.omitNamespace)
                .toString(id)
        );
        if (!StringUtils.isEmpty(desc)) {
            sb.append(" **");
            sb.append(ws.io().getTerminalFormat().escapeText(desc));
            sb.append("**");
        }
        return sb.toString();
    }
}

package net.vpc.app.nuts.toolbox.nfind;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.app.nuts.app.options.ArchitectureNonOption;
import net.vpc.app.nuts.app.options.PackagingNonOption;
import net.vpc.app.nuts.app.options.RepositoryNonOption;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.DefaultNonOption;
import net.vpc.common.commandline.format.TreeFormatter;
import net.vpc.common.commandline.format.TreeModel;
import net.vpc.common.commandline.format.TreeNodeFormatter;
import net.vpc.common.strings.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.*;

public class NFindMain extends NutsApplication {
    public static void main(String[] args) {
        new NFindMain().launchAndExit(args);
    }

    @Override
    public int launch(NutsApplicationContext context) {
        CommandLine cmdLine = new CommandLine(context);
        int currentFindWhat = 0;
        List<FindWhat> findWhats = new ArrayList<>();
        FindContext findContext = new FindContext();
        findContext.context = context;
        findContext.out = context.out();
        findContext.err = context.err();
        Argument a;
        while (cmdLine.hasNext()) {
            if (context.configure(cmdLine)) {
                //
            } else if (cmdLine.readAllOnce("-js", "--javascript")) {
                if (currentFindWhat + 1 >= findWhats.size()) {
                    findWhats.add(new FindWhat());
                }
                if (findWhats.get(currentFindWhat).nonjs.size() > 0) {
                    if (!cmdLine.isExecMode()) {
                        return -1;
                    }
                    throw new NutsIllegalArgumentException("find: Unsupported mixed and non js find expressions");
                }
                findContext.jsflag = true;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-x", "--expression")) {
                findContext.jsflag = false;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-l", "--long")) {
                findContext.longflag = true;
            } else if (currentFindWhat == 0 && (a=cmdLine.readBooleanOption("--omit-group"))!=null) {
                findContext.omitGroup = a.getBooleanValue();
            } else if (currentFindWhat == 0 && (a=cmdLine.readBooleanOption("--omit-imported"))!=null) {
                findContext.omitImportedGroup = a.getBooleanValue();
            } else if (currentFindWhat == 0 && (a=cmdLine.readBooleanOption("--highlight-imported"))!=null) {
                findContext.highlightImportedGroup = a.getBooleanValue();
            } else if (currentFindWhat == 0 && (a=cmdLine.readBooleanOption("--omit-namespace"))!=null) {
                findContext.omitNamespace = a.getBooleanValue();
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-f", "--file")) {
                findContext.showFile = true;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("--file-name-only")) {
                findContext.fileNameOnly = true;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("--file-path-only")) {
                findContext.filePathOnly = true;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-c", "--class")) {
                findContext.showClass = true;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-F", "--offline")) {
                findContext.fetchMode = SearchMode.OFFLINE;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-O", "--online")) {
                findContext.fetchMode = SearchMode.ONLINE;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-R", "--remote")) {
                findContext.fetchMode = SearchMode.REMOTE;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-M", "--commitable")) {
                findContext.fetchMode = SearchMode.COMMIT;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-U", "--updatable")) {
                findContext.fetchMode = SearchMode.UPDATE;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-T", "--status")) {
                findContext.fetchMode = SearchMode.STATUS;
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
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-d", "--dependencies")) {
                findContext.display = "dependencies";
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-d", "--dependencies-tree")) {
                findContext.display = "dependencies-tree";
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
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-I", "--display-id")) {
                findContext.display = "id";
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-N", "--display-name")) {
                findContext.display = "name";
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-P", "--display-packaging")) {
                findContext.display = "packaging";
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-A", "--display-arch")) {
                findContext.display = "arch";
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-L", "--display-file")) {
                findContext.display = "file";
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-C", "--display-class")) {
                findContext.display = "class";
            } else if (currentFindWhat == 0 && (a = cmdLine.readBooleanOption("--test")) != null) {
                if (a.getBooleanValue()) {
                    findContext.scopes = NutsDependencyScope.add(findContext.scopes, NutsDependencyScope.GROUP_TEST);
                } else {
                    findContext.scopes = NutsDependencyScope.remove(findContext.scopes, NutsDependencyScope.GROUP_TEST);
                }
            } else if (currentFindWhat == 0 && (a = cmdLine.readBooleanOption("--provided")) != null) {
                if (a.getBooleanValue()) {
                    findContext.scopes = NutsDependencyScope.add(findContext.scopes, NutsDependencyScope.PROVIDED);
                } else {
                    findContext.scopes = NutsDependencyScope.remove(findContext.scopes, NutsDependencyScope.PROVIDED);
                }
            } else if (currentFindWhat == 0 && (a = cmdLine.readBooleanOption("--compile")) != null) {
                if (a.getBooleanValue()) {
                    findContext.scopes = NutsDependencyScope.add(findContext.scopes, NutsDependencyScope.GROUP_COMPILE);
                } else {
                    findContext.scopes = NutsDependencyScope.remove(findContext.scopes, NutsDependencyScope.GROUP_COMPILE);
                }
            } else if (currentFindWhat == 0 && (a = cmdLine.readBooleanOption("--scope-other")) != null) {
                if (a.getBooleanValue()) {
                    findContext.scopes = NutsDependencyScope.add(findContext.scopes, NutsDependencyScope.OTHER);
                } else {
                    findContext.scopes = NutsDependencyScope.remove(findContext.scopes, NutsDependencyScope.GROUP_COMPILE);
                }
            } else if (currentFindWhat == 0 && (a = cmdLine.readOption("--scope-all")) != null) {
                findContext.scopes = NutsDependencyScope.ALL.expand();
            } else if (currentFindWhat == 0 && (a = cmdLine.readBooleanOption("--optional")) != null) {
                findContext.acceptOptional = a.getBooleanValue() ? null : false;
            } else if (currentFindWhat == 0 && (a = cmdLine.readOption("--optional-only")) != null) {
                findContext.acceptOptional = true;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-p", "--pkg")) {
                findContext.pack.add(cmdLine.readRequiredNonOption(new PackagingNonOption("Packaging", context.getWorkspace())).getStringExpression());
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-a", "--arch")) {
                findContext.arch.add(cmdLine.readRequiredNonOption(new ArchitectureNonOption("Architecture", context.getWorkspace())).getStringExpression());
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-r", "--repo")) {
                findContext.repos.add(cmdLine.readRequiredNonOption(new RepositoryNonOption("Repository", context.getWorkspace())).getStringExpression());
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-V", "--last-version")) {
                findContext.latestVersions = true;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-v", "--all-versions")) {
                findContext.latestVersions = false;
            } else if (currentFindWhat == 0 && cmdLine.readAllOnce("-Y", "--summary")) {
                findContext.showSummary = true;
            } else {
                Argument val = cmdLine.readRequiredNonOption(new DefaultNonOption("Expression"));
                if (currentFindWhat + 1 >= findWhats.size()) {
                    findWhats.add(new FindWhat());
                }
                if (cmdLine.isExecMode()) {
                    if (findContext.jsflag) {
                        if (findWhats.get(currentFindWhat).jsCode == null) {
                            findWhats.get(currentFindWhat).jsCode = val.getStringExpression();
                        } else {
                            throw new NutsIllegalArgumentException("find: Unsupported mixed and non js find expressions");
                        }
                    } else {
                        String arg = val.getStringExpression();
                        if (findWhats.get(currentFindWhat).jsCode == null) {
                            findWhats.get(currentFindWhat).nonjs.add(arg);
                        } else {
                            throw new NutsIllegalArgumentException("find: Unsupported mixed and non js find expressions");
                        }
                    }
                }
                currentFindWhat++;
            }
        }
        if (!cmdLine.isExecMode()) {
            return -1;
        }

        if (findContext.installedDependencies == null) {
            findContext.installedDependencies = false;
        }
        if (findWhats.isEmpty()) {
            FindWhat w = new FindWhat();
            w.nonjs.add("*");
            findWhats.add(w);
        }
        if (findContext.fetchMode == SearchMode.STATUS) {
            findContext.fetchMode = SearchMode.COMMIT;
            boolean first = true;
            for (FindWhat findWhat : findWhats) {
                List<NutsIdExt> it = (find(findWhat, findContext));
                if (!it.isEmpty()) {
                    if (first) {
                        first = false;
                        findContext.out.println("===Packages to COMMIT===:");
                    }
                    display(it, findContext);
                }
            }

            first = true;
            findContext.fetchMode = SearchMode.UPDATE;
            for (FindWhat findWhat : findWhats) {
                List<NutsIdExt> it = (find(findWhat, findContext));
                if (!it.isEmpty()) {
                    if (first) {
                        first = false;
                        findContext.out.println("===Packages to UPDATE===:");
                    }
                    display(it, findContext);
                }
            }
//          // there is no need to see only remote nuts for status
//            first=true;
//            findContext.fetchMode=SearchMode.REMOTE;
//            for (FindWhat findWhat : findWhats) {
//                List<NutsId> it = (find(findWhat, findContext));
//                if(!it.isEmpty()) {
//                    if (first) {
//                        first = false;
//                        findContext.out.drawln("===Packages versions on Remote servers===:");
//                    }
//                    display(it, findContext);
//                }
//            }
        } else {
            for (FindWhat findWhat : findWhats) {
                long from = System.nanoTime();
                List<NutsIdExt> it = find(findWhat, findContext);
                long to = System.nanoTime();
                findContext.executionTimeNano = to - from;
                findContext.executionSearch = findWhat;
                display(it, findContext);
            }
        }
        return 0;
    }

    private List<NutsIdExt> toext(List<NutsId> list) {
        List<NutsIdExt> e = new ArrayList<>();
        for (NutsId nutsId : list) {
            e.add(new NutsIdExt(nutsId, null));
        }
        return e;
    }

    private List<NutsIdExt> find(FindWhat findWhat, final FindContext findContext)  {
        if (findWhat.nonjs.isEmpty() && findWhat.jsCode == null) {
            findWhat.nonjs.add("*");
        }
        NutsQuery query = findContext.context.getWorkspace().createQuery().addJs(findWhat.jsCode)
                .addId(findWhat.nonjs)
                .addArch(findContext.arch)
                .addPackaging(findContext.pack)
                .addRepository(findContext.repos)
                .setSort(true)
                .setLatestVersions(findContext.latestVersions);

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
        return Collections.emptyList();
    }

    private List<NutsIdExt> searchUpdate(FindContext findContext, NutsQuery query, NutsWorkspace ws) {
        Map<String, NutsId> local = new LinkedHashMap<>();
        for (NutsId nutsId : query.setSession(findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.OFFLINE)).find()) {
            NutsId r = local.get(nutsId.getSimpleName());
            if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                local.put(nutsId.getSimpleName(), nutsId);
            }
        }
        Map<String, NutsId> remote = new LinkedHashMap<>();
        for (NutsId nutsId : query.setSession(findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.REMOTE)).find()) {
            NutsId r = remote.get(nutsId.getSimpleName());
            if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                remote.put(nutsId.getSimpleName(), nutsId);
            }
        }

        //force search of all local nutIds because some repositories could not make a wildcard search...
        for (NutsId localNutsId : local.values()) {
            for (NutsId nutsId : ws.createQuery().addId(localNutsId.toString()).setSession(findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.REMOTE)).find()) {
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
        return new ArrayList<NutsIdExt>(ret.values());
    }

    private List<NutsIdExt> searchCommit(FindContext findContext, NutsQuery query, NutsWorkspace ws) {
        Map<String, NutsId> local = new LinkedHashMap<>();
        Map<String, NutsId> remote = new LinkedHashMap<>();
        for (NutsId nutsId : query.setSession(findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.OFFLINE)).find()) {
            NutsId r = local.get(nutsId.getSimpleName());
            if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                local.put(nutsId.getSimpleName(), nutsId);
            }
        }
        for (NutsId nutsId : query.setSession(findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.REMOTE)).find()) {
            NutsId r = remote.get(nutsId.getSimpleName());
            if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                remote.put(nutsId.getSimpleName(), nutsId);
            }
        }

        //force search of all local nutIds because some repositories could not make a wildcard search...
        for (NutsId localNutsId : local.values()) {
            for (NutsId nutsId : ws.createQuery().addId(localNutsId.toString()).setSession(findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.REMOTE)).find()) {
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
        return new ArrayList<NutsIdExt>(ret.values());
    }

    private List<NutsId> searchFetchType(FindContext findContext, NutsQuery query, NutsFetchMode m) {
        return query.setSession(findContext.context.getSession().copy().setTransitive(true).setFetchMode(m)).find();
    }

    private List<NutsId> searchRemote(FindContext findContext, NutsQuery query, NutsWorkspace ws) {
        return searchFetchType(findContext, query, NutsFetchMode.REMOTE);
    }

    private List<NutsId> searchOffline(FindContext findContext, NutsQuery query, NutsWorkspace ws) {
        return searchFetchType(findContext, query, NutsFetchMode.OFFLINE);
    }

    private List<NutsId> searchOnline(FindContext findContext, NutsQuery query, NutsWorkspace ws) {
        return searchFetchType(findContext, query, NutsFetchMode.ONLINE);
        //display(nutsIdIterator,findContext);
    }

    private void display(List<NutsIdExt> nutsList, FindContext findContext) {
        if (nutsList.isEmpty()) {
            findContext.err.printf("Nuts not found : %s\n", findContext.executionSearch);
            return;
        }
        Set<String> visitedItems = new HashSet<>();
        NutsWorkspace ws = findContext.context.getWorkspace();
        Set<String> visitedPackaging = new HashSet<>();
        Set<String> visitedArchs = new HashSet<>();
        if (!findContext.longflag) {
            //if not long flag, should remove namespace and duplicates
            Set<NutsIdExt> mm = new HashSet<>();
            for (NutsIdExt nutsId : nutsList) {
                mm.add(new NutsIdExt(
                        nutsId.id.builder().setNamespace(null).setFace(null).setQuery("").build(),
                        nutsId.extra
                ));
            }
            nutsList = new ArrayList<>(mm);
        }
//        if (findContext.latestVersions) {
//            Map<String, NutsIdExt> mm = new HashMap<>();
//            for (NutsIdExt nutsId : nutsList) {
//                String fullName = nutsId.id.getSimpleName();
//                NutsIdExt old = mm.get(fullName);
//                if (old == null || old.id.getVersion().compareTo(nutsId.id.getVersion()) < 0) {
//                    mm.put(fullName, nutsId);
//                }
//            }
//            nutsList = new ArrayList<>(mm.values());
//        }
//        Collections.sort(nutsList/*, CoreNutsUtils.NUTS_ID_COMPARATOR*/);

        for (NutsIdExt nutsId : nutsList) {
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
                    Set<String> imports = new HashSet<String>(Arrays.asList(ws.getConfigManager().getImports()));
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
                        if (findContext.longflag) {
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
                            findContext.out.print(d == null ? "?" : (d.getPackaging()==null?"":d.getPackaging()));
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
                    File fullName = info.getFile();
                    if (fullName != null) {
                        findContext.out.printf("%s\n", fullName.getPath());
                    }
                    break;
                }
                case "class": {
                    for (NutsExecutionEntry entry : ws.getParseManager().parseExecutionEntries(info.getFile())) {
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
        if (findContext.showSummary) {
            findContext.out.println();
            findContext.out.printf("===%s=== nuts found in ===%s===s\n",
                    nutsList.size(),
                    (findContext.executionTimeNano / 1000000 / 1000.0)
            );
        }
    }

    private String toStringId(FindContext findContext, NutsInfo info) {
        if (findContext.longflag) {
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
                + (descriptor == null ? "?" :
                (descriptor.isNutsApplication() ? "X" : descriptor.isExecutable() ? "x" : ".")
        );
        findContext.out.print("**" + status + "**");
        findContext.out.print(" ");
        findContext.out.print(descriptor == null ? "?  " : (StringUtils.isEmpty(descriptor.getPackaging())?"":descriptor.getPackaging()));
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
        NutsId nid = ws.getParseManager().parseId(id);
        if(!nid.getVersion().isSingleValue()){
            return new ArrayList<>();
        }
        for (NutsDependency dependency : ws.fetchDescriptor(id, true, session
                .setProperty("monitor-allowed", false)
        ).getDependencies(findContext.equivalentDependencyFilter)) {
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

    private NutsInfo prepareChildren(FindContext findContext, NutsInfo info, Set<String> visited) {
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

        TreeFormatter<NutsInfo> f = new TreeFormatter<>(new TreeModel<NutsInfo>() {
            Set<String> visited = new HashSet<>();

            @Override
            public NutsInfo getRoot() {
                return prepareChildren(findContext, info, visited);
            }

            @Override
            public List<NutsInfo> getChildren(NutsInfo o) {
                List<NutsInfo> c = o.children;
                for (NutsInfo nutsInfo : c) {
                    prepareChildren(findContext, nutsInfo, visited);
                }
                return c;
            }
        }, new TreeNodeFormatter<NutsInfo>() {
            @Override
            public String format(NutsInfo o) {
                return toStringId(findContext, o);
            }
        }, null);
        f.print(findContext.out);
    }

    private void printDependencyList(FindContext findContext, NutsWorkspace ws, NutsInfo info, Set<String> imports) {
        NutsFetchMode m = getNutsFetchMode(findContext.fetchMode);
        NutsSession session = findContext.context.getSession().copy().setTransitive(true).setFetchMode(m);
        List<NutsDefinition> depsFiles = ws.createQuery().setSession(session)
                .addId(info.nuts)
                .setScope(findContext.scopes)
                .setDependencyFilter(findContext.equivalentDependencyFilter)
                .setLatestVersions(findContext.latestVersions)
                .setAcceptOptional(findContext.acceptOptional)
                .dependenciesOnly()
                .setSort(true)
                .fetch();
        Set<String> immediateSelfDependencies = toDependencySet(ws.fetchDescriptor(info.nuts.toString(), false, session).getDependencies());
        Set<String> immediateInheritedDependencies = toDependencySet(ws.fetchDescriptor(info.nuts.toString(), true, session).getDependencies());
        int packagingSize = 3;
        int archSizeSize = 2;
        for (NutsDefinition dd : depsFiles) {
            packagingSize = Math.max(packagingSize, dd.getDescriptor().getPackaging()==null?0:dd.getDescriptor().getPackaging().length());
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
            if(findContext.filePathOnly){
                if (dinfo.getFile() == null) {
                    format+=("@@FILE NOT FOUND@@ ");
                    format+=(format(findContext, dinfo.nuts, dinfo.desc, ws));
                } else {
                    format+=(dinfo.getFile().getPath());
                }
            }else if(findContext.fileNameOnly){
                if (dinfo.getFile() == null) {
                    format+=("@@FILE NOT FOUND@@ ");
                    format+=(format(findContext, dinfo.nuts, dinfo.desc, ws));
                } else {
                    format+=(dinfo.getFile().getName());
                }
            }else {
                format+=format(findContext, dinfo.nuts, dinfo.desc, ws);
            }
            if (findContext.longflag) {
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
        switch (StringUtils.trim(dd.getScope()).toLowerCase()) {
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
        if(findContext.filePathOnly){
            if (info.getFile() == null) {
                out.print("@@FILE NOT FOUND@@ ");
                out.print(format(findContext, info.nuts, info.desc, ws));
            } else {
                out.print(info.getFile().getPath());
            }
            return;
        }
        if(findContext.fileNameOnly){
            if (info.getFile() == null) {
                out.print("@@FILE NOT FOUND@@ ");
                out.print(format(findContext, info.nuts, info.desc, ws));
            } else {
                out.print(info.getFile().getName());
            }
            return;
        }
        out.print(format(findContext, info.nuts, info.desc, ws));
        if (findContext.showFile) {
            out.print(" ");
            if (info.getFile() == null) {
                out.print("?");
            } else {
                out.print(info.getFile().getPath());
            }
        }
        if (findContext.showClass) {
            out.print(" ");
            if (info.getFile() == null) {
                out.print("?");
            } else {
                NutsExecutionEntry[] cls = ws.getParseManager().parseExecutionEntries(info.getFile());
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

    private NutsFetchMode getNutsFetchMode(SearchMode fecthMode) {
        NutsFetchMode m = null;
        switch (fecthMode) {
            case ONLINE: {
                m = NutsFetchMode.ONLINE;
                break;
            }
            case OFFLINE: {
                m = NutsFetchMode.OFFLINE;
                break;
            }
            case REMOTE: {
                m = NutsFetchMode.REMOTE;
                break;
            }
            case COMMIT: {
                m = NutsFetchMode.ONLINE;
                break;
            }
            case UPDATE: {
                m = NutsFetchMode.ONLINE;
                break;
            }
        }
        return m;
    }

    private String format(FindContext findContext, NutsId id, String desc, NutsWorkspace ws) {
        StringBuilder sb = new StringBuilder();
        sb.append(ws.getFormatManager().createIdFormat()
                .setHighlightImportedGroup(true)
                .setHighlightOptional(true)
                .setHighlightImportedGroup(findContext.highlightImportedGroup)
                .setHighlightScope(true)
                .setOmitEnv(true)
                .setOmitFace(true)
                .setOmitGroup(findContext.omitGroup)
                .setOmitImportedGroup(findContext.omitImportedGroup)
                .setOmitNamespace(findContext.omitNamespace)
                .format(id)
        );
        if (!StringUtils.isEmpty(desc)) {
            sb.append(" **");
            sb.append(ws.getParseManager().escapeText(desc));
            sb.append("**");
        }
        return sb.toString();
    }

    enum SearchMode {
        OFFLINE,
        ONLINE,
        REMOTE,
        COMMIT,
        UPDATE,
        STATUS,
    }

    private static class NutsInfo {

        NutsId nuts;
        String desc;
        Boolean fetched;
        Boolean is_installed;
        Boolean is_updatable;
        NutsApplicationContext context;
        NutsWorkspace ws;
        NutsSession session;
        NutsDescriptor descriptor;
        NutsDefinition _fetchedFile;
        List<NutsInfo> children;
        boolean continued;
        boolean error;

        public NutsInfo(NutsIdExt nuts, NutsApplicationContext context) {
            this.nuts = nuts.id;
            this.desc = nuts.extra;
            this.context = context;
            ws = context.getWorkspace();
            session = context.getSession();
        }

        public boolean isFetched() {
            if (this.fetched == null) {
                this.fetched = ws.isFetched(nuts.toString(), session);
            }
            return this.fetched;
        }

        public boolean isInstalled(boolean checkDependencies) {
            if (this.is_installed == null) {
                this.is_installed = isFetched() && ws.isInstalled(nuts.toString(), checkDependencies, session);
            }
            return this.is_installed;
        }

        public boolean isUpdatable() {
            if (this.is_updatable == null) {
                this.is_updatable = false;
                if (this.isFetched()) {
                    NutsId nut2 = null;
                    try {
                        nut2 = ws.resolveId(nuts.setVersion(null).toString(), session.copy()
                                .setProperty("monitor-allowed", false)
                                .setTransitive(true).setFetchMode(NutsFetchMode.REMOTE));
                    } catch (Exception ex) {
                        //ignore
                    }
                    if (nut2 != null && nut2.getVersion().compareTo(nuts.getVersion()) > 0) {
                        this.is_updatable = true;
                    }
                }
            }
            return this.is_updatable;
        }

        public File getFile() {
            if (_fetchedFile == null) {
                try {
                    _fetchedFile = ws.fetch(nuts, session.copy().setTransitive(true).setFetchMode(NutsFetchMode.OFFLINE));
                } catch (Exception ex) {
                    _fetchedFile = new NutsDefinition(null, null, null, false, false, null, null);
                }
            }
            if (_fetchedFile.getFile() == null) {
                return null;
            }
            return new File(_fetchedFile.getFile());
        }

        public NutsDescriptor getDescriptor() {
            if (descriptor == null) {
//                    NutsDescriptor dd = ws.fetchDescriptor(nuts.toString(), true, session.copy().setTransitive(true).setFetchMode(NutsFetchMode.ONLINE));
//                    if(dd.isExecutable()){
//                        System.out.println("");
//                    }
                try {
                    descriptor = ws.fetchDescriptor(nuts.toString(), true, session.copy().setTransitive(true).setFetchMode(NutsFetchMode.ONLINE));
                } catch (Exception ex) {
                    descriptor = ws.fetchDescriptor(nuts.toString(), false, session.copy().setTransitive(true).setFetchMode(NutsFetchMode.ONLINE));
                }
            }
            return descriptor;
        }

    }

    class FindWhat {

        String jsCode = null;
        HashSet<String> nonjs = new HashSet<String>();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (!StringUtils.isEmpty(jsCode)) {
                sb.append("js::'").append(jsCode).append('\'');
            }
            for (String v : nonjs) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append("'");
                sb.append(v);
                sb.append("'");
            }
            return sb.toString();
        }
    }

    static class FindContext {
        HashSet<String> arch = new HashSet<String>();
        HashSet<String> pack = new HashSet<String>();
        HashSet<String> repos = new HashSet<String>();
        EnumSet<NutsDependencyScope> scopes = EnumSet.of(NutsDependencyScope.PROFILE_RUN);
        boolean longflag = false;
        boolean omitGroup = false;
        boolean omitNamespace = true;
        boolean omitImportedGroup = false;
        boolean highlightImportedGroup = true;
        boolean filePathOnly = false;
        boolean fileNameOnly = false;
        boolean showFile = false;
        boolean showClass = false;
        boolean jsflag = false;
        SearchMode fetchMode = SearchMode.ONLINE;
        boolean desc = false;
        boolean eff = false;
        boolean executable = true;
        boolean library = true;
        Boolean installed = null;
        Boolean installedDependencies = null;
        Boolean updatable = null;
        boolean latestVersions = true;
        PrintStream out;
        PrintStream err;
        String display = "id";
        boolean showSummary = false;
        NutsApplicationContext context;
        long executionTimeNano;
        FindWhat executionSearch;
        Boolean acceptOptional = false;
        NutsDependencyFilter dependencyFilter = null;
        NutsDependencyFilter equivalentDependencyFilter = new NutsDependencyFilter() {
            @Override
            public boolean accept(NutsId from, NutsDependency dependency) {
                if (scopes != null && scopes.size() > 0 && !NutsDependencyScope.expand(scopes).contains(NutsDependencyScope.lenientParse(dependency.getScope()))) {
                    return false;
                }
                if (acceptOptional != null) {
                    if (acceptOptional.booleanValue() != dependency.isOptional()) {
                        return false;
                    }
                }
                if (dependencyFilter != null) {
                    if (dependencyFilter.accept(from, dependency)) {
                        return false;
                    }
                }
                return true;
            }
        };


    }

    public static class NutsIdExt implements Comparable<NutsIdExt>{
        public NutsId id;
        public String extra;

        public NutsIdExt(NutsId id, String extra) {
            this.id = id;
            this.extra = extra;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NutsIdExt nutsIdExt = (NutsIdExt) o;
            return Objects.equals(id, nutsIdExt.id);
        }

        @Override
        public int hashCode() {

            return Objects.hash(id);
        }

        @Override
        public int compareTo(NutsIdExt o) {
            int x = this.id.getSimpleName().compareTo(o.id.getSimpleName());
            if(x!=0){
                return x;
            }
            x = - this.id.getVersion().compareTo(o.id.getVersion());
            return x;
        }
    }

}

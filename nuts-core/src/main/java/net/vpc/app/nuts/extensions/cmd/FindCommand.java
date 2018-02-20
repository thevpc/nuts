/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.cmd.cmdline.*;
import net.vpc.app.nuts.extensions.util.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
public class FindCommand extends AbstractNutsCommand {

    public FindCommand() {
        super("find", CORE_SUPPORT);
    }

    @Override
    public int exec(String[] args, NutsCommandContext context) throws Exception {
        NutsCommandAutoComplete autoComplete = context.getAutoComplete();
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        int currentFindWhat = 0;
        List<FindWhat> findWhats = new ArrayList<>();
        FindContext findContext = new FindContext();
        findContext.context = context;
        findContext.out = context.getTerminal().getOut();
        findContext.err = context.getTerminal().getErr();
        while (!cmdLine.isEmpty()) {
            if (cmdLine.readOnce("-js", "--javascript")) {
                if (currentFindWhat + 1 >= findWhats.size()) {
                    findWhats.add(new FindWhat());
                }
                if (findWhats.get(currentFindWhat).nonjs.size() > 0) {
                    if (!cmdLine.isExecMode()) {
                        return -1;
                    }
                    throw new NutsIllegalArgumentsException("Unsupported");
                }
                findContext.jsflag = true;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-x", "--expression")) {
                findContext.jsflag = false;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-l", "--long")) {
                findContext.longflag = true;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-f", "--file")) {
                findContext.showFile = true;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-c", "--class")) {
                findContext.showClass = true;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-F", "--offline")) {
                findContext.fecthMode = SearchMode.OFFLINE;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-O", "--online")) {
                findContext.fecthMode = SearchMode.ONLINE;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-R", "--remote")) {
                findContext.fecthMode = SearchMode.REMOTE;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-M", "--commitable")) {
                findContext.fecthMode = SearchMode.COMMIT;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-U", "--updatable")) {
                findContext.fecthMode = SearchMode.UPDATE;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-T", "--status")) {
                findContext.fecthMode = SearchMode.STATUS;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-e", "--exec")) {
                findContext.executable = true;
                findContext.library = false;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-!e", "--lib")) {
                findContext.executable = false;
                findContext.library = true;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-s", "--descriptor")) {
                findContext.desc = true;
                findContext.eff = false;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-g", "--installed-dependencies")) {
                findContext.installedDependencies = true;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-d", "--dependencies")) {
                findContext.display = "dependencies";
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-i", "--installed")) {
                findContext.installed = true;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-!i", "--non-installed")) {
                findContext.installed = false;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-u", "--updatable")) {
                findContext.updatable = true;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-!u", "--non-updatable")) {
                findContext.updatable = false;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-b", "--effective-descriptor")) {
                findContext.desc = true;
                findContext.eff = true;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-I", "--display-id")) {
                findContext.display = "id";
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-N", "--display-name")) {
                findContext.display = "name";
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-P", "--display-packaging")) {
                findContext.display = "packaging";
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-A", "--display-arch")) {
                findContext.display = "arch";
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-L", "--display-file")) {
                findContext.display = "file";
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-C", "--display-class")) {
                findContext.display = "class";
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-h", "-?", "--help")) {
                cmdLine.requireEmpty();
                if (cmdLine.isExecMode()) {
                    String help = getHelp();
                    findContext.out.println("Command " + this);
                    findContext.out.println(help);
                }
                return 0;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-p", "--pkg")) {
                findContext.pack.add(cmdLine.readNonOptionOrError(new PackagingNonOption("Packaging", context)).getString());
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-a", "--arch")) {
                findContext.arch.add(cmdLine.readNonOptionOrError(new ArchitectureNonOption("Architecture", context)).getString());
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-r", "--repo")) {
                findContext.repos.add(cmdLine.readNonOptionOrError(new RepositoryNonOption("Repository", context.getValidWorkspace())).getString());
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-v", "--last-version")) {
                findContext.bestVersion = true;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-Y", "--summary")) {
                findContext.showSummary = true;
            } else {
                CmdLine.Val val = cmdLine.readNonOptionOrError(new DefaultNonOption("Expression"));
                if (currentFindWhat + 1 >= findWhats.size()) {
                    findWhats.add(new FindWhat());
                }
                if (cmdLine.isExecMode()) {
                    if (findContext.jsflag) {
                        if (findWhats.get(currentFindWhat).jsCode == null) {
                            findWhats.get(currentFindWhat).jsCode = val.getString();
                        } else {
                            throw new NutsIllegalArgumentsException("Unsupported");
                        }
                    } else {
                        String arg = val.getString();
                        if (findWhats.get(currentFindWhat).jsCode == null) {
//                            if (!arg.startsWith("*")) {
//                                arg = "*" + arg;
//                            }
//                            if (arg.startsWith("*")) {
//                                arg = arg + "*";
//                            }
                            findWhats.get(currentFindWhat).nonjs.add(arg);
                        } else {
                            throw new NutsIllegalArgumentsException("Unsupported");
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
        if (findContext.fecthMode == SearchMode.STATUS) {
            findContext.fecthMode = SearchMode.COMMIT;
            boolean first = true;
            for (FindWhat findWhat : findWhats) {
                List<NutsId> it = (find(findWhat, findContext));
                if (!it.isEmpty()) {
                    if (first) {
                        first = false;
                        findContext.out.println("===Packages to COMMIT===:");
                    }
                    display(it, findContext);
                }
            }

            first = true;
            findContext.fecthMode = SearchMode.UPDATE;
            for (FindWhat findWhat : findWhats) {
                List<NutsId> it = (find(findWhat, findContext));
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
//            findContext.fecthMode=SearchMode.REMOTE;
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
                List<NutsId> it = find(findWhat, findContext);
                long to = System.nanoTime();
                findContext.executionTimeNano = to - from;
                findContext.executionSearch = findWhat;
                display(it, findContext);
            }
        }
        return 0;
    }

    private List<NutsId> find(FindWhat findWhat, final FindContext findContext) throws IOException {
        if (findWhat.nonjs.isEmpty() && findWhat.jsCode == null) {
            findWhat.nonjs.add("*");
        }
        NutsSearch search = new NutsSearchBuilder().addJs(findWhat.jsCode)
                .addId(findWhat.nonjs)
                .addArch(findContext.arch)
                .addPackaging(findContext.pack)
                .addRepository(findContext.repos)
                .build();

        NutsWorkspace ws = findContext.context.getValidWorkspace();
        switch (findContext.fecthMode) {
            case ONLINE: {
                return searchOnline(findContext, search, ws);
            }
            case OFFLINE: {
                return searchOffline(findContext, search, ws);
            }
            case REMOTE: {
                return searchRemote(findContext, search, ws);
            }
            case COMMIT: {
                return searchCommit(findContext, search, ws);
            }
            case UPDATE: {
                return searchUpdate(findContext, search, ws);
            }
            case STATUS: {
                throw new NutsIllegalArgumentsException("Unsupported");
            }
        }
        return Collections.emptyList();
    }

    private List<NutsId> searchUpdate(FindContext findContext, NutsSearch search, NutsWorkspace ws) throws IOException {
        Map<String, NutsId> local = new LinkedHashMap<>();
        for (NutsId nutsId : (ws.find(search, findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.OFFLINE)))) {
            NutsId r = local.get(nutsId.getFullName());
            if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                local.put(nutsId.getFullName(), nutsId);
            }
        }
        Map<String, NutsId> remote = new LinkedHashMap<>();
        for (NutsId nutsId : (ws.find(search, findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.REMOTE)))) {
            NutsId r = remote.get(nutsId.getFullName());
            if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                remote.put(nutsId.getFullName(), nutsId);
            }
        }
        for (NutsId localNutsId : local.values()) {
            NutsId remoteNutsId = remote.get(localNutsId.getFullName());
            if (remoteNutsId != null && localNutsId.getVersion().compareTo(remoteNutsId.getVersion()) >= 0) {
                remote.remove(localNutsId.getFullName());
            }
        }
        return new ArrayList<NutsId>(remote.values());
    }

    private List<NutsId> searchCommit(FindContext findContext, NutsSearch search, NutsWorkspace ws) throws IOException {
        Map<String, NutsId> local = new LinkedHashMap<>();
        for (NutsId nutsId : (ws.find(search, findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.OFFLINE)))) {
            NutsId r = local.get(nutsId.getFullName());
            if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                local.put(nutsId.getFullName(), nutsId);
            }
        }
        Map<String, NutsId> remote = new LinkedHashMap<>();
        for (NutsId nutsId : (ws.find(search, findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.REMOTE)))) {
            NutsId r = remote.get(nutsId.getFullName());
            if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                remote.put(nutsId.getFullName(), nutsId);
            }
        }
        for (NutsId nutsId : remote.values()) {
            NutsId r = local.get(nutsId.getFullName());
            if (r != null && nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                local.remove(nutsId.getFullName());
            }
        }
        return new ArrayList<NutsId>(local.values());
    }

    private List<NutsId> searchRemote(FindContext findContext, NutsSearch search, NutsWorkspace ws) throws IOException {
        return ws.find(search, findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.REMOTE));
    }

    private List<NutsId> searchOffline(FindContext findContext, NutsSearch search, NutsWorkspace ws) {
        return ws.find(search, findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.OFFLINE));
    }

    private List<NutsId> searchOnline(FindContext findContext, NutsSearch search, NutsWorkspace ws) {
        return ws.find(search, findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.ONLINE));
        //display(nutsIdIterator,findContext);
    }

    private void display(List<NutsId> nutsList, FindContext findContext) throws IOException {
        if (nutsList.isEmpty()) {
            findContext.err.println("Nuts not found " + findContext.executionSearch);
            return;
        }
        Set<String> visitedItems = new HashSet<>();
        NutsWorkspace ws = findContext.context.getValidWorkspace();
        Set<String> visitedPackaging = new HashSet<>();
        Set<String> visitedArchs = new HashSet<>();
        if (!findContext.longflag) {
            //if not long flag, should remove namespace and duplicates
            Set<NutsId> mm = new HashSet<>();
            for (NutsId nutsId : nutsList) {
                mm.add(nutsId.setNamespace(null).setFace(null).setQuery(""));
            }
            nutsList = new ArrayList<>(mm);
        }
        if (findContext.bestVersion) {
            Map<String, NutsId> mm = new HashMap<>();
            for (NutsId nutsId : nutsList) {
                String fullName = nutsId.getFullName();
                NutsId old = mm.get(fullName);
                if (old == null || old.getVersion().compareTo(nutsId.getVersion()) < 0) {
                    mm.put(fullName, nutsId);
                }
            }
            nutsList = new ArrayList<>(mm.values());
        }
        Collections.sort(nutsList, CoreNutsUtils.NUTS_ID_COMPARATOR);

        for (NutsId nutsId : nutsList) {
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
                if (info.getDescriptor().isExecutable() != findContext.executable) {
                    continue;
                }
            }
            switch (findContext.display) {
                case "id":
                case "dependencies":
                    Set<String> imports = new HashSet<String>(Arrays.asList(ws.getImports()));

                    if (findContext.longflag) {
                        NutsDescriptor descriptor = null;
                        String descriptorError = null;
                        try {
                            descriptor = info.getDescriptor();
                        } catch (Exception ex) {
                            descriptorError = ex.getMessage();
                        }
                        String status = (info.isInstalled(findContext.installedDependencies) ? "i"
                                : info.isFetched() ? "f"
                                : "r")
                                + (info.isUpdatable() ? "u" : ".")
                                + (descriptor == null ? "?" : (descriptor.isExecutable() ? "x" : "."));
                        findContext.out.print(status);
                        findContext.out.print(" ");
                        findContext.out.print(descriptor == null ? "?  " : descriptor.getPackaging());
                        findContext.out.print(" ");
                        findContext.out.print(Arrays.asList(descriptor == null ? new String[0] : descriptor.getArch()));
                        findContext.out.print(" ");
                        if (CoreStringUtils.isEmpty(info.nuts.getNamespace())) {
                            findContext.out.print("?");
                        } else {
                            findContext.out.print(info.nuts.getNamespace());
                        }
                        findContext.out.print(" ");
                        findContext.out.print(format(info.nuts, imports));
                        if (findContext.showFile) {
                            findContext.out.print(" ");
                            if (info.getFile() == null) {
                                findContext.out.print("?");
                            } else {
                                findContext.out.print(info.getFile().getPath());
                            }
                        }
                        if (findContext.showClass) {
                            findContext.out.print(" ");
                            if (info.getFile() == null) {
                                findContext.out.print("?");
                            } else {
                                String cls = CorePlatformUtils.getMainClass(info.getFile());
                                if (cls == null) {
                                    findContext.out.print("?");
                                } else {
                                    findContext.out.print(cls);
                                }
                            }
                        }
                        if (!CoreStringUtils.isEmpty(descriptorError)) {
                            findContext.out.print(" [[" + descriptorError + "]]");
                        }
                    } else {
                        findContext.out.print(format(info.nuts, imports));
                        if (findContext.showFile) {
                            findContext.out.print(" ");
                            if (info.getFile() == null) {
                                findContext.out.print("?");
                            } else {
                                findContext.out.print(info.getFile().getPath());
                            }
                        }
                        if (findContext.showClass) {
                            findContext.out.print(" ");
                            if (info.getFile() == null) {
                                findContext.out.print("?");
                            } else {
                                String cls = CorePlatformUtils.getMainClass(info.getFile());
                                if (cls == null) {
                                    findContext.out.print("?");
                                } else {
                                    findContext.out.print(cls);
                                }
                            }
                        }
                    }
                    findContext.out.println();
                    if (findContext.desc) {
                        NutsDescriptor descriptor = null;
                        String descriptorError = null;
                        try {
                            descriptor = info.getDescriptor();
                        } catch (Exception ex) {
                            descriptorError = ex.getMessage();
                        }
                        findContext.out.println(descriptor == null ? ("Error Retrieving Descriptor : " + descriptor) : descriptor.toString());
                        findContext.out.println("");
                    }
                    if ("dependencies".equals(findContext.display)) {
                        NutsFetchMode m = null;
                        switch (findContext.fecthMode) {
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
                        NutsFile[] depsFiles = ws.fetchDependencies(
                                new NutsDependencySearch(info.nuts)
                                        .setIncludeMain(false)
                                        .setScope(NutsDependencyScope.RUN),
                                findContext.context.getSession().copy().setTransitive(true)
                                        .setFetchMode(m)
                        );
                        Arrays.sort(depsFiles, CoreNutsUtils.NUTS_FILE_COMPARATOR);
                        for (NutsFile dd : depsFiles) {
                            NutsInfo dinfo = new NutsInfo(dd.getId(), findContext.context);
                            dinfo.descriptor = dd.getDescriptor();
                            if (findContext.longflag) {
                                String status = (dinfo.isInstalled(findContext.installedDependencies) ? "i"
                                        : dinfo.isFetched() ? "f"
                                        : "r") + (dinfo.isUpdatable() ? "u" : ".");
                                findContext.out.print("\t");
                                findContext.out.print(status);
                                findContext.out.print(" ");
                                findContext.out.print(dinfo.getDescriptor().getPackaging());
                                findContext.out.print(" ");
                                findContext.out.print(Arrays.asList(dinfo.getDescriptor().getArch()));
                                findContext.out.print(" ");
                                findContext.out.println(format(dinfo.nuts, imports));
                            } else {
                                findContext.out.print("\t");
                                findContext.out.println(format(dinfo.nuts, imports));
                            }
                        }
                    }
                    break;
                case "name": {
                    String fullName = info.nuts.getFullName();
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
                            findContext.out.print(d == null ? "?" : d.getPackaging());
                            findContext.out.print(" ");
                            findContext.out.print(Arrays.asList(d == null ? "?" : d.getArch()));
                            findContext.out.print(" ");
                            findContext.out.println(info.nuts.getFullName());
                        } else {
                            findContext.out.println(info.nuts.getFullName());
                        }
                    }
                    break;
                }
                case "file": {
                    File fullName = info.getFile();
                    if (fullName != null) {
                        findContext.out.println(fullName.getPath());
                    }
                    break;
                }
                case "class": {
                    String fullName = CorePlatformUtils.getMainClass(info.getFile());
                    if (fullName != null && !visitedItems.contains(fullName)) {
                        visitedItems.add(fullName);
                        findContext.out.println(fullName);
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
                        if (!CoreStringUtils.isEmpty(p) && !visitedPackaging.contains(p)) {
                            visitedPackaging.add(p);
                            findContext.out.println(p);
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
                            if (!CoreStringUtils.isEmpty(p) && !visitedArchs.contains(p)) {
                                visitedArchs.add(p);
                                findContext.out.println(p);
                            }
                        }
                    }
                    break;
                }
            }
        }
        if (findContext.showSummary) {
            findContext.out.println();
            findContext.out.println("===" + nutsList.size() + "=== nuts found in ===" + (findContext.executionTimeNano / 1000000 / 1000.0) + "===s");
        }
    }

    private String format(NutsId id, Set<String> imports) {
        id = id.setNamespace(null)
                .setQueryProperty(NutsConstants.QUERY_FACE, null)
                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
        StringBuilder sb = new StringBuilder();
        if (!CoreStringUtils.isEmpty(id.getNamespace())) {
            sb.append(id.getNamespace()).append("://");
        }
        if (!CoreStringUtils.isEmpty(id.getGroup())) {
            if (imports.contains(id.getGroup())) {
                sb.append("==");
                sb.append(id.getGroup());
                sb.append("==");
            } else {
                sb.append(id.getGroup());
            }
            sb.append(":");
        }
        sb.append("[[");
        sb.append(id.getName());
        sb.append("]]");
        if (!CoreStringUtils.isEmpty(id.getVersion().getValue())) {
            sb.append("#");
            sb.append(id.getVersion());
        }
        if (!CoreStringUtils.isEmpty(id.getQuery())) {
            sb.append("?");
            sb.append(id.getQuery());
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
        Boolean fetched;
        Boolean is_installed;
        Boolean is_updatable;
        NutsCommandContext context;
        NutsWorkspace ws;
        NutsSession session;
        NutsDescriptor descriptor;
        NutsFile _fetchedFile;

        public NutsInfo(NutsId nuts, NutsCommandContext context) throws IOException {
            this.nuts = nuts;
            this.context = context;
            ws = context.getValidWorkspace();
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
                        nut2 = ws.resolveId(nuts.setVersion(null).toString(), session.copy().setTransitive(true).setFetchMode(NutsFetchMode.REMOTE));
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
                    _fetchedFile = ws.fetch(nuts.toString(), session.copy().setTransitive(true).setFetchMode(NutsFetchMode.OFFLINE));
                } catch (Exception ex) {
                    _fetchedFile = new NutsFile(null, null, null, false, false, null);
                }
            }
            return _fetchedFile.getFile();
        }

        public NutsDescriptor getDescriptor() {
            if (descriptor == null) {
//                    NutsDescriptor dd = ws.fetchDescriptor(nuts.toString(), true, session.copy().setTransitive(true).setFetchMode(NutsFetchMode.ONLINE));
//                    if(dd.isExecutable()){
//                        System.out.println("");
//                    }
                descriptor = ws.fetchDescriptor(nuts.toString(), true, session.copy().setTransitive(true).setFetchMode(NutsFetchMode.ONLINE));
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
            if (!CoreStringUtils.isEmpty(jsCode)) {
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
        boolean longflag = false;
        boolean showFile = false;
        boolean showClass = false;
        boolean jsflag = false;
        SearchMode fecthMode = SearchMode.ONLINE;
        boolean desc = false;
        boolean eff = false;
        boolean executable = true;
        boolean library = true;
        Boolean installed = null;
        Boolean installedDependencies = null;
        Boolean updatable = null;
        boolean bestVersion = false;
        NutsPrintStream out;
        NutsPrintStream err;
        String display = "id";
        boolean showSummary = false;
        NutsCommandContext context;
        long executionTimeNano;
        FindWhat executionSearch;
    }
}

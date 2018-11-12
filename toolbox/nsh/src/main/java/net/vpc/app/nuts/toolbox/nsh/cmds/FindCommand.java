/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.*;
//import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
//import net.vpc.app.nuts.extensions.util.CorePlatformUtils;
//import net.vpc.app.nuts.extensions.util.CoreStringUtils;
//import net.vpc.app.nuts.extensions.util.NutsSearchBuilder;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.NutsIdExt;
import net.vpc.app.nuts.toolbox.nsh.options.ArchitectureNonOption;
import net.vpc.app.nuts.toolbox.nsh.options.PackagingNonOption;
import net.vpc.app.nuts.toolbox.nsh.options.RepositoryNonOption;
import net.vpc.common.commandline.ArgVal;
import net.vpc.common.commandline.DefaultNonOption;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
public class FindCommand extends AbstractNutsCommand {

    public FindCommand() {
        super("find", DEFAULT_SUPPORT);
    }

    @Override
    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        NutsWorkspace ws = context.getWorkspace();
        int currentFindWhat = 0;
        List<FindWhat> findWhats = new ArrayList<>();
        FindContext findContext = new FindContext();
        findContext.context = context;
        findContext.out = context.getTerminal().getFormattedOut();
        findContext.err = context.getTerminal().getFormattedErr();
        while (!cmdLine.isEmpty()) {
            if (cmdLine.readOnce("-js", "--javascript")) {
                if (currentFindWhat + 1 >= findWhats.size()) {
                    findWhats.add(new FindWhat());
                }
                if (findWhats.get(currentFindWhat).nonjs.size() > 0) {
                    if (!cmdLine.isExecMode()) {
                        return -1;
                    }
                    throw new NutsIllegalArgumentException("Unsupported");
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
                    findContext.out.printf("Command %s\n", this);
                    findContext.out.println(help);
                }
                return 0;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-p", "--pkg")) {
                findContext.pack.add(cmdLine.readNonOptionOrError(new PackagingNonOption("Packaging", context)).getString());
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-a", "--arch")) {
                findContext.arch.add(cmdLine.readNonOptionOrError(new ArchitectureNonOption("Architecture", context)).getString());
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-r", "--repo")) {
                findContext.repos.add(cmdLine.readNonOptionOrError(new RepositoryNonOption("Repository", context.getValidWorkspace())).getString());
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-V", "--last-version")) {
                findContext.latestVersions = true;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-v", "--all-versions")) {
                findContext.latestVersions = false;
            } else if (currentFindWhat == 0 && cmdLine.readOnce("-Y", "--summary")) {
                findContext.showSummary = true;
            } else {
                ArgVal val = cmdLine.readNonOptionOrError(new DefaultNonOption("Expression"));
                if (currentFindWhat + 1 >= findWhats.size()) {
                    findWhats.add(new FindWhat());
                }
                if (cmdLine.isExecMode()) {
                    if (findContext.jsflag) {
                        if (findWhats.get(currentFindWhat).jsCode == null) {
                            findWhats.get(currentFindWhat).jsCode = val.getString();
                        } else {
                            throw new NutsIllegalArgumentException("Unsupported");
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
                            throw new NutsIllegalArgumentException("Unsupported");
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
            findContext.fecthMode = SearchMode.UPDATE;
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

    private List<NutsIdExt> find(FindWhat findWhat, final FindContext findContext) throws IOException {
        if (findWhat.nonjs.isEmpty() && findWhat.jsCode == null) {
            findWhat.nonjs.add("*");
        }
        NutsSearch search = findContext.context.getValidWorkspace().createSearchBuilder().addJs(findWhat.jsCode)
                .addId(findWhat.nonjs)
                .addArch(findContext.arch)
                .addPackaging(findContext.pack)
                .addRepository(findContext.repos)
                .build()
                .setSort(true)
                .setLastestVersions(findContext.latestVersions);

        NutsWorkspace ws = findContext.context.getValidWorkspace();
        switch (findContext.fecthMode) {
            case ONLINE: {
                return toext(searchOnline(findContext, search, ws));
            }
            case OFFLINE: {
                return toext(searchOffline(findContext, search, ws));
            }
            case REMOTE: {
                return toext(searchRemote(findContext, search, ws));
            }
            case COMMIT: {
                return searchCommit(findContext, search, ws);
            }
            case UPDATE: {
                return searchUpdate(findContext, search, ws);
            }
            case STATUS: {
                throw new NutsIllegalArgumentException("Unsupported");
            }
        }
        return Collections.emptyList();
    }

    private List<NutsIdExt> searchUpdate(FindContext findContext, NutsSearch search, NutsWorkspace ws) throws IOException {
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

        //force search of all local nutIds because some repositories could not make a wildcard search...
        for (NutsId localNutsId : local.values()) {
            for (NutsId nutsId : (ws.find(new NutsSearch().addId(localNutsId.toString()), findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.REMOTE)))) {
                NutsId r = remote.get(nutsId.getFullName());
                if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                    remote.put(nutsId.getFullName(), nutsId);
                }
            }
        }

        Map<String, NutsIdExt> ret = new LinkedHashMap<>();
        for (NutsId localNutsId : local.values()) {
            NutsId remoteNutsId = remote.get(localNutsId.getFullName());
            if (remoteNutsId != null && localNutsId.getVersion().compareTo(remoteNutsId.getVersion()) >= 0) {
                remote.remove(localNutsId.getFullName());
            } else if (remoteNutsId != null) {
                ret.put(localNutsId.getFullName(), new NutsIdExt(remoteNutsId, "(local: " + localNutsId.getVersion().toString() + ")"));
            }
        }
        return new ArrayList<NutsIdExt>(ret.values());
    }

    private List<NutsIdExt> searchCommit(FindContext findContext, NutsSearch search, NutsWorkspace ws) throws IOException {
        Map<String, NutsId> local = new LinkedHashMap<>();
        Map<String, NutsId> remote = new LinkedHashMap<>();
        for (NutsId nutsId : (ws.find(search, findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.OFFLINE)))) {
            NutsId r = local.get(nutsId.getFullName());
            if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                local.put(nutsId.getFullName(), nutsId);
            }
        }
        for (NutsId nutsId : (ws.find(search, findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.REMOTE)))) {
            NutsId r = remote.get(nutsId.getFullName());
            if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                remote.put(nutsId.getFullName(), nutsId);
            }
        }

        //force search of all local nutIds because some repositories could not make a wildcard search...
        for (NutsId localNutsId : local.values()) {
            for (NutsId nutsId : (ws.find(new NutsSearch().addId(localNutsId.toString()), findContext.context.getSession().copy().setTransitive(true).setFetchMode(NutsFetchMode.REMOTE)))) {
                NutsId r = remote.get(nutsId.getFullName());
                if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                    remote.put(nutsId.getFullName(), nutsId);
                }
            }
        }

        Map<String, NutsIdExt> ret = new LinkedHashMap<>();

        for (NutsId remoteNutsId : remote.values()) {
            NutsId localNutsId = local.get(remoteNutsId.getFullName());
            if (localNutsId != null && remoteNutsId.getVersion().compareTo(localNutsId.getVersion()) >= 0) {
//                local.remove(nutsId.getFullName());
            } else if (localNutsId != null) {
                ret.put(remoteNutsId.getFullName(), new NutsIdExt(localNutsId, "(remote: " + remoteNutsId.getVersion().toString() + ")"));
            }
        }
        return new ArrayList<NutsIdExt>(ret.values());
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

    private void display(List<NutsIdExt> nutsList, FindContext findContext) throws IOException {
        if (nutsList.isEmpty()) {
            findContext.err.printf("Nuts not found : %s\n", findContext.executionSearch);
            return;
        }
        Set<String> visitedItems = new HashSet<>();
        NutsWorkspace ws = findContext.context.getValidWorkspace();
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
//                String fullName = nutsId.id.getFullName();
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
                }catch (Exception ex){
                    try {
                        info.getDescriptor();
                    }catch (Exception ex2){

                    }
                    findContext.err.print("Error Resolving "+info.nuts+" :: "+ex.getMessage()+"\n");
                    continue;
                }
            }
            switch (findContext.display) {
                case "id":
                case "dependencies":
                    Set<String> imports = new HashSet<String>(Arrays.asList(ws.getConfigManager().getImports()));

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
                        if (StringUtils.isEmpty(info.nuts.getNamespace())) {
                            findContext.out.print("?");
                        } else {
                            findContext.out.print(info.nuts.getNamespace());
                        }
                        findContext.out.print(" ");
                        findContext.out.print(format(info.nuts, info.desc, imports,ws));
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
                                String cls = ws.resolveJavaMainClass(info.getFile());
                                if (cls == null) {
                                    findContext.out.print("?");
                                } else {
                                    findContext.out.print(cls);
                                }
                            }
                        }
                        if (!StringUtils.isEmpty(descriptorError)) {
                            findContext.out.print(" [[" + descriptorError + "]]");
                        }
                    } else {
                        findContext.out.print(format(info.nuts, info.desc, imports,ws));
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
                                String cls = ws.resolveJavaMainClass(info.getFile());
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
                        findContext.out.printf(descriptor == null ? ("Error Retrieving Descriptor : %s (%s)\n") : "%s (%s)\n", descriptor.toString(), descriptorError);
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
                        Arrays.sort(depsFiles);
                        for (NutsFile dd : depsFiles) {
                            NutsInfo dinfo = new NutsInfo(new NutsIdExt(dd.getId(), null), findContext.context);
                            dinfo.descriptor = dd.getDescriptor();
                            if (findContext.longflag) {
                                String status = (dinfo.isInstalled(findContext.installedDependencies) ? "i"
                                        : dinfo.isFetched() ? "f"
                                        : "r") + (dinfo.isUpdatable() ? "u" : ".");
                                findContext.out.print("\t");
                                findContext.out.printf("%s", status);
                                findContext.out.print(" ");
                                findContext.out.printf("%s", dinfo.getDescriptor().getPackaging());
                                findContext.out.print(" ");
                                findContext.out.printf("%s", Arrays.asList(dinfo.getDescriptor().getArch()));
                                findContext.out.print(" ");
                                findContext.out.println(format(dinfo.nuts, info.desc, imports,ws));
                            } else {
                                findContext.out.print("\t");
                                findContext.out.println(format(dinfo.nuts, info.desc, imports,ws));
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
                            findContext.out.printf("%s\n", info.nuts.getFullName());
                        } else {
                            findContext.out.printf("%s\n", info.nuts.getFullName());
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
                    String fullName = ws.resolveJavaMainClass(info.getFile());
                    if (fullName != null && !visitedItems.contains(fullName)) {
                        visitedItems.add(fullName);
                        findContext.out.printf("%s\n", fullName);
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

    private String format(NutsId id, String desc, Set<String> imports,NutsWorkspace ws) {
        id = id.builder()
                .setNamespace(null)
                .setQueryProperty(NutsConstants.QUERY_FACE, null)
                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true)
        .build();
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(id.getNamespace())) {
            sb.append(id.getNamespace()).append("://");
        }
        if (!StringUtils.isEmpty(id.getGroup())) {
            if (imports.contains(id.getGroup())) {
                sb.append("==");
                sb.append(ws.escapeText(id.getGroup()));
                sb.append("==");
            } else {
                sb.append(ws.escapeText(id.getGroup()));
            }
            sb.append(":");
        }
        sb.append("[[");
        sb.append(ws.escapeText(id.getName()));
        sb.append("]]");
        if (!StringUtils.isEmpty(id.getVersion().getValue())) {
            sb.append("#");
            sb.append(ws.escapeText(id.getVersion().toString()));
        }
        if (!StringUtils.isEmpty(id.getQuery())) {
            sb.append("?");
            sb.append(ws.escapeText(id.getQuery()));
        }
        if (!StringUtils.isEmpty(desc)) {
            sb.append(" **");
            sb.append(ws.escapeText(desc));
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
        NutsCommandContext context;
        NutsWorkspace ws;
        NutsSession session;
        NutsDescriptor descriptor;
        NutsFile _fetchedFile;

        public NutsInfo(NutsIdExt nuts, NutsCommandContext context) throws IOException {
            this.nuts = nuts.id;
            this.desc = nuts.extra;
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
                }catch (Exception ex){
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
        boolean latestVersions = true;
        NutsPrintStream out;
        NutsPrintStream err;
        String display = "id";
        boolean showSummary = false;
        NutsCommandContext context;
        long executionTimeNano;
        FindWhat executionSearch;
    }
}

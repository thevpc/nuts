/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.cmd.cmdline.*;
import net.vpc.app.nuts.extensions.util.CorePlatformUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.app.nuts.extensions.util.NutsDescriptorJavascriptFilter;
import net.vpc.app.nuts.extensions.util.NutsIdPatternFilter;

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

    public int run(String[] args, NutsCommandContext context, NutsCommandAutoComplete autoComplete) throws Exception {
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        int currentFindWhat = 0;
        List<FindWhat> findWhats = new ArrayList<>();
        FindContext findContext = new FindContext();
        findContext.context = context;
        findContext.out = context.getTerminal().getOut();
        while (!cmdLine.isEmpty()) {
            if (cmdLine.acceptAndRemoveNoDuplicates("-js", "--javascript")) {
                if (currentFindWhat + 1 >= findWhats.size()) {
                    findWhats.add(new FindWhat());
                }
                if (findWhats.get(currentFindWhat).nonjs.size() > 0) {
                    if (!cmdLine.isExecMode()) {
                        return -1;
                    }
                    throw new IllegalArgumentException("Unsupported");
                }
                findContext.jsflag = true;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-x", "--expression")) {
                findContext.jsflag = false;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-l", "--long")) {
                findContext.longflag = true;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-f", "--file")) {
                findContext.showFile = true;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-c", "--class")) {
                findContext.showClass = true;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-off", "--offline")) {
                findContext.fecthMode = SearchMode.OFFLINE;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-on", "--online")) {
                findContext.fecthMode = SearchMode.ONLINE;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-R", "--remote")) {
                findContext.fecthMode = SearchMode.REMOTE;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-C", "--commitable")) {
                findContext.fecthMode = SearchMode.COMMIT;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-U", "--updatable")) {
                findContext.fecthMode = SearchMode.UPDATE;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-S", "--status")) {
                findContext.fecthMode = SearchMode.STATUS;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-e", "--exec")) {
                findContext.executable = true;
                findContext.library = false;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-l", "--lib")) {
                findContext.executable = false;
                findContext.library = true;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-s", "--descriptor")) {
                findContext.desc = true;
                findContext.eff = false;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-D", "--installed-dependencies")) {
                findContext.installedDependencies = true;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-d", "--dependencies")) {
                findContext.display = "dependencies";
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-i", "--installed")) {
                findContext.installed = true;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-!i", "--non-installed")) {
                findContext.installed = false;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-u", "--updatable")) {
                findContext.updatable = true;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-!u", "--non-updatable")) {
                findContext.updatable = false;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-S", "--effective-descriptor")) {
                findContext.desc = true;
                findContext.eff = true;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-I", "--display-id")) {
                findContext.display = "id";
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-N", "--display-name")) {
                findContext.display = "name";
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-P", "--display-packaging")) {
                findContext.display = "packaging";
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-A", "--display-arch")) {
                findContext.display = "arch";
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-F", "--display-file")) {
                findContext.display = "file";
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-C", "--display-class")) {
                findContext.display = "class";
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-h", "--help")) {
                cmdLine.requireEmpty();
                if (cmdLine.isExecMode()) {
                    String help = getHelp();
                    findContext.out.println("Command " + this);
                    findContext.out.println(help);
                }
                return 0;
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-p", "--pkg")) {
                findContext.pack.add(cmdLine.readNonOptionOrError(new PackagingNonOption("Packaging", context)).getString());
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-a", "--arch")) {
                findContext.arch.add(cmdLine.readNonOptionOrError(new ArchitectureNonOption("Architecture", context)).getString());
            } else if (currentFindWhat == 0 && cmdLine.acceptAndRemoveNoDuplicates("-r", "--repo")) {
                findContext.repos.add(cmdLine.readNonOptionOrError(new RepositoryNonOption("Repository", context.getValidWorkspace())).getString());
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
                            throw new IllegalArgumentException("Unsupported");
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
                            throw new IllegalArgumentException("Unsupported");
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

        if (findContext.fecthMode == SearchMode.STATUS) {
            findContext.fecthMode = SearchMode.COMMIT;
            boolean first = true;
            for (FindWhat findWhat : findWhats) {
                List<NutsId> it = (find(findWhat, findContext));
                if (!it.isEmpty()) {
                    if (first) {
                        first = false;
                        findContext.out.drawln("===Packages to COMMIT===:");
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
                        findContext.out.drawln("===Packages to UPDATE===:");
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
                List<NutsId> it = find(findWhat, findContext);
                display(it, findContext);
            }
        }
        return 0;
    }

    private List<NutsId> find(FindWhat findWhat, final FindContext findContext) throws IOException {
        if (findWhat.nonjs.isEmpty() && findWhat.jsCode == null) {
            findWhat.nonjs.add("*");
        }
        NutsDescriptorFilter filter = null;
        if (findWhat.jsCode != null) {
            filter = new NutsDescriptorJavascriptFilter(findWhat.jsCode);
        } else {
            filter = new NutsIdPatternFilter(findWhat.nonjs.toArray(new String[findWhat.nonjs.size()]), findContext.pack.toArray(new String[findContext.pack.size()]), findContext.arch.toArray(new String[findContext.arch.size()]));
        }
        NutsWorkspace ws = findContext.context.getValidWorkspace();
        NutsRepositoryFilter repositoryFilter = new NutsRepositoryFilter() {
            @Override
            public boolean accept(NutsRepository repository) {
                return findContext.repos.isEmpty() || findContext.repos.contains(repository.getRepositoryId());
            }
        };
        switch (findContext.fecthMode) {
            case ONLINE: {
                return searchOnline(findContext, filter, ws, repositoryFilter);
            }
            case OFFLINE: {
                return searchOffline(findContext, filter, ws, repositoryFilter);
            }
            case REMOTE: {
                return searchRemote(findContext, filter, ws, repositoryFilter);
            }
            case COMMIT: {
                return searchCommit(findContext, filter, ws, repositoryFilter);
            }
            case UPDATE: {
                return searchUpdate(findContext, filter, ws, repositoryFilter);
            }
            case STATUS: {
                throw new IllegalArgumentException("Unsupported");
            }
        }
        return Collections.emptyList();
    }

    private List<NutsId> searchUpdate(FindContext findContext, NutsDescriptorFilter filter, NutsWorkspace ws, NutsRepositoryFilter repositoryFilter) throws IOException {
        Map<String, NutsId> local = new LinkedHashMap<>();
        for (NutsId nutsId : (ws.find(repositoryFilter, filter, findContext.context.getSession().copy().setTransitive(true).setFetchMode(FetchMode.OFFLINE)))) {
            NutsId r = local.get(nutsId.getFullName());
            if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                local.put(nutsId.getFullName(), nutsId);
            }
        }
        Map<String, NutsId> remote = new LinkedHashMap<>();
        for (NutsId nutsId : (ws.find(repositoryFilter, filter, findContext.context.getSession().copy().setTransitive(true).setFetchMode(FetchMode.REMOTE)))) {
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

    private List<NutsId> searchCommit(FindContext findContext, NutsDescriptorFilter filter, NutsWorkspace ws, NutsRepositoryFilter repositoryFilter) throws IOException {
        Map<String, NutsId> local = new LinkedHashMap<>();
        for (NutsId nutsId : (ws.find(repositoryFilter, filter, findContext.context.getSession().copy().setTransitive(true).setFetchMode(FetchMode.OFFLINE)))) {
            NutsId r = local.get(nutsId.getFullName());
            if (r == null || nutsId.getVersion().compareTo(r.getVersion()) >= 0) {
                local.put(nutsId.getFullName(), nutsId);
            }
        }
        Map<String, NutsId> remote = new LinkedHashMap<>();
        for (NutsId nutsId : (ws.find(repositoryFilter, filter, findContext.context.getSession().copy().setTransitive(true).setFetchMode(FetchMode.REMOTE)))) {
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

    private List<NutsId> searchRemote(FindContext findContext, NutsDescriptorFilter filter, NutsWorkspace ws, NutsRepositoryFilter repositoryFilter) throws IOException {
        return ws.find(repositoryFilter, filter, findContext.context.getSession().copy().setTransitive(true).setFetchMode(FetchMode.REMOTE));
    }

    private List<NutsId> searchOffline(FindContext findContext, NutsDescriptorFilter filter, NutsWorkspace ws, NutsRepositoryFilter repositoryFilter) throws IOException {
        return ws.find(repositoryFilter, filter, findContext.context.getSession().copy().setTransitive(true).setFetchMode(FetchMode.OFFLINE));
    }

    private List<NutsId> searchOnline(FindContext findContext, NutsDescriptorFilter filter, NutsWorkspace ws, NutsRepositoryFilter repositoryFilter) throws IOException {
        return ws.find(repositoryFilter, filter, findContext.context.getSession().copy().setTransitive(true).setFetchMode(FetchMode.ONLINE));
        //display(nutsIdIterator,findContext);
    }

    private void display(List<NutsId> nutsList, FindContext findContext) throws IOException {
        Set<String> visitedItems = new HashSet<>();
        NutsWorkspace ws = findContext.context.getValidWorkspace();
        Set<String> visitedPackaging = new HashSet<>();
        Set<String> visitedArchs = new HashSet<>();
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
                    Set<String> imports = new HashSet<String>(Arrays.asList(ws.getConfig().getImports()));

                    if (findContext.longflag) {
                        String status = (info.isInstalled(findContext.installedDependencies) ? "i"
                                : info.isFetched() ? "f"
                                : "r")
                                + (info.isUpdatable() ? "u" : ".")
                                + (info.getDescriptor().isExecutable() ? "x" : ".");
                        findContext.out.print(status);
                        findContext.out.print(" ");
                        findContext.out.print(info.getDescriptor().getPackaging());
                        findContext.out.print(" ");
                        findContext.out.print(Arrays.asList(info.getDescriptor().getArch()));
                        findContext.out.print(" ");
                        if (CoreStringUtils.isEmpty(info.nuts.getNamespace())) {
                            findContext.out.print("?");
                        } else {
                            findContext.out.print(info.nuts.getNamespace());
                        }
                        findContext.out.print(" ");
                        findContext.out.draw(format(info.nuts, imports));
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
                    } else {
                        findContext.out.draw(format(info.nuts, imports));
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
                        findContext.out.println(info.getDescriptor().toString());
                        findContext.out.println("");
                    }
                    if ("dependencies".equals(findContext.display)) {
                        FetchMode m = null;
                        switch (findContext.fecthMode) {
                            case ONLINE: {
                                m = FetchMode.ONLINE;
                                break;
                            }
                            case OFFLINE: {
                                m = FetchMode.OFFLINE;
                                break;
                            }
                            case REMOTE: {
                                m = FetchMode.REMOTE;
                                break;
                            }
                            case COMMIT: {
                                m = FetchMode.ONLINE;
                                break;
                            }
                            case UPDATE: {
                                m = FetchMode.ONLINE;
                                break;
                            }
                        }
                        List<NutsFile> depsFiles = ws.fetchWithDependencies(info.nuts.toString(), false, null, findContext.context.getSession().copy().setTransitive(true)
                                .setFetchMode(m)
                        );
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
                                findContext.out.drawln(format(dinfo.nuts, imports));
                            } else {
                                findContext.out.print("\t");
                                findContext.out.drawln(format(dinfo.nuts, imports));
                            }
                        }
                    }
                    break;
                case "name": {
                    String fullName = info.nuts.getFullName();
                    if (!visitedItems.contains(fullName)) {
                        visitedItems.add(fullName);
                        if (findContext.longflag) {
                            String status = (info.isInstalled(findContext.installedDependencies) ? "i"
                                    : info.isFetched() ? "f"
                                    : "r") + (info.isUpdatable() ? "u" : ".");
                            findContext.out.print(status);
                            findContext.out.print(" ");
                            findContext.out.print(info.getDescriptor().getPackaging());
                            findContext.out.print(" ");
                            findContext.out.print(Arrays.asList(info.getDescriptor().getArch()));
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
                    //if(fullName!=null && !visitedItems.contains(fullName.getPath())) {
                    //visitedItems.add(fullName.getPath());
                    findContext.out.println(fullName.getPath());
                    //}
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
                    NutsDescriptor d = info.getDescriptor();
                    String p = d.getPackaging();
                    if (!CoreStringUtils.isEmpty(p) && !visitedPackaging.contains(p)) {
                        visitedPackaging.add(p);
                        findContext.out.println(p);
                    }
                    break;
                }
                case "arch": {
                    NutsDescriptor d = info.getDescriptor();
                    for (String p : d.getArch()) {
                        if (!CoreStringUtils.isEmpty(p) && !visitedArchs.contains(p)) {
                            visitedArchs.add(p);
                            findContext.out.println(p);
                        }
                    }
                    break;
                }
            }
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
                try {
                    this.fetched = ws.isFetched(nuts.toString(), session);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return this.fetched;
        }

        public boolean isInstalled(boolean checkDependencies) {
            if (this.is_installed == null) {
                try {
                    this.is_installed = isFetched() && ws.isInstalled(nuts.toString(), checkDependencies, session);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return this.is_installed;
        }

        public boolean isUpdatable() {
            if (this.is_updatable == null) {
                try {
                    this.is_updatable = false;
                    if (this.isFetched()) {
                        NutsId nut2 = null;
                        try {
                            nut2 = ws.resolveId(nuts.setVersion(null).toString(), session.copy().setTransitive(true).setFetchMode(FetchMode.REMOTE));
                        } catch (Exception ex) {
                            //ignore
                        }
                        if (nut2 != null && nut2.getVersion().compareTo(nuts.getVersion()) > 0) {
                            this.is_updatable = true;
                        }
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
            return this.is_updatable;
        }


        public File getFile() {
            if (_fetchedFile == null) {
                try {
                    _fetchedFile = ws.fetch(nuts.toString(), session.copy().setTransitive(true).setFetchMode(FetchMode.OFFLINE));
                } catch (Exception ex) {
                    _fetchedFile = new NutsFile(null, null, null, false, false);
                }
            }
            return _fetchedFile.getFile();
        }

        public NutsDescriptor getDescriptor() {
            if (descriptor == null) {
                try {
//                    NutsDescriptor dd = ws.fetchDescriptor(nuts.toString(), true, session.copy().setTransitive(true).setFetchMode(FetchMode.ONLINE));
//                    if(dd.isExecutable()){
//                        System.out.println("");
//                    }
                    descriptor = ws.fetchDescriptor(nuts.toString(), true, session.copy().setTransitive(true).setFetchMode(FetchMode.ONLINE));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return descriptor;
        }

    }

    class FindWhat {
        String jsCode = null;
        HashSet<String> nonjs = new HashSet<String>();
    }

    class FindContext {
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
        NutsPrintStream out;
        String display = "id";
        NutsCommandContext context;
    }
}

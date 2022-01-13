package net.thevpc.nuts.toolbox.nwork;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.ndiff.jar.Diff;
import net.thevpc.nuts.toolbox.ndiff.jar.DiffItem;
import net.thevpc.nuts.toolbox.ndiff.jar.DiffResult;
import net.thevpc.nuts.toolbox.nwork.config.ProjectConfig;
import net.thevpc.nuts.toolbox.nwork.config.RepositoryAddress;
import net.thevpc.nuts.toolbox.nwork.config.WorkspaceConfig;
import net.thevpc.nuts.toolbox.nwork.filescanner.FileScanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class WorkspaceService {

    public static final String SCAN = "net.thevpc.nuts.toolbox.nwork.scan";
    private WorkspaceConfig config;
    private final NutsApplicationContext appContext;
    private final NutsPath sharedConfigFolder;

    public WorkspaceService(NutsApplicationContext appContext) {
        this.appContext = appContext;
        sharedConfigFolder = appContext.getVersionFolder(NutsStoreLocation.CONFIG, NWorkConfigVersions.CURRENT);
        NutsPath c = getConfigFile();
        if (c.isRegularFile()) {
            try {
                NutsSession session = appContext.getSession();
                config = NutsElements.of(session).json().parse(c, WorkspaceConfig.class);
            } catch (Exception ex) {
                //
            }
        }
        if (config == null) {
            config = new WorkspaceConfig();
        }
    }

    public static String wildcardToRegex(String pattern) {
        if (pattern == null) {
            pattern = "*";
        }
        int i = 0;
        char[] cc = pattern.toCharArray();
        StringBuilder sb = new StringBuilder("^");
        while (i < cc.length) {
            char c = cc[i];
            switch (c) {
                case '.':
                case '$':
                case '{':
                case '}':
                case '+': {
                    sb.append('\\').append(c);
                    break;
                }
                case '?': {
                    sb.append(".");
                    break;
                }
                case '*': {
                    sb.append(".*");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
            i++;
        }
        sb.append('$');
        return sb.toString();
    }

    public WorkspaceConfig getWorkspaceConfig() {

        RepositoryAddress v = config.getDefaultRepositoryAddress();
        if (v == null) {
            v = new RepositoryAddress();
            config.setDefaultRepositoryAddress(v);
        }

        return config;
    }

    public void setWorkspaceConfig(WorkspaceConfig c) {
        if (c == null) {
            c = new WorkspaceConfig();
        }
        config = c;
        NutsPath configFile = getConfigFile();
        configFile.mkParentDirs();
        NutsSession session = appContext.getSession();
        NutsElements.of(session).json().setValue(c).print(configFile);
    }

    private void updateBools(Boolean[] all, boolean ok) {
        boolean positive = false;
        boolean negative = false;
        for (Boolean anAll : all) {
            if (anAll != null) {
                if (anAll) {
                    positive = true;
                } else {
                    negative = true;
                }
            }
        }
        boolean fill = false;
        if (!positive && !negative) {
            fill = true;
        } else if (negative) {
            fill = true;
        } else if (positive) {
            fill = false;
        }
        for (int i = 0; i < all.length; i++) {
            if (all[i] == null) {
                all[i] = fill;
            }
        }
    }

    public void enableScan(NutsCommandLine commandLine, NutsApplicationContext context, boolean enable) {
        int count = 0;
        while (commandLine.hasNext()) {
            if (commandLine.peek().isNonOption()) {
                String expression = commandLine.next().getString();
                if (commandLine.isExecMode()) {
                    setScanEnabled(Paths.get(expression), enable);
                    count++;
                }
            } else {
                context.configureLast(commandLine);
            }
        }

        if (count == 0) {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("missing projects"), 1);
        }
    }

    public void list(NutsCommandLine cmd, NutsApplicationContext appContext) {
        NutsArgument a;
        List<String> filters = new ArrayList<>();
        cmd.setCommandName("nwork list");
        while (cmd.hasNext()) {
            if ((a = cmd.requireNonOption().next()) != null) {
                filters.add(a.getString());
            } else {
                appContext.configureLast(cmd);
            }
        }
        if (cmd.isExecMode()) {

            List<ProjectConfig> result = new ArrayList<>();
            for (ProjectService projectService : findProjectServices()) {
                if (matches(projectService.getConfig().getId(), filters)) {
                    ProjectConfig config = projectService.getConfig();
                    result.add(config);
                }
            }
            result.sort(Comparator.comparing(ProjectConfig::getId));
            if (appContext.getSession().isPlainOut()) {
                for (ProjectConfig p2 : result) {
                    appContext.getSession().out().println(
                            formatProjectConfig(appContext, p2)
                    );
                }
            } else {
                NutsObjectFormat.of(appContext.getSession())
                        .setValue(result).println();
            }
        }
    }

    private NutsTextBuilder formatProjectConfig(NutsApplicationContext appContext, ProjectConfig p2) {
        NutsTexts text = NutsTexts.of(appContext.getSession());
        return text.builder()
                .append(p2.getId(), NutsTextStyle.primary4())
                .append(" ")
                .appendJoined(
                        text.ofPlain(", "),
                        p2.getTechnologies().stream().map(
                                x -> text.ofStyled(x, NutsTextStyle.primary5())
                        ).collect(Collectors.toList())
                )
                .append(" : ")
                .append(p2.getPath(), NutsTextStyle.path());
    }

    public void scan(NutsCommandLine cmdLine, NutsApplicationContext context) {
        NutsSession session = context.getSession();
        boolean interactive = false;
        NutsArgument a;
        boolean run = false;
        boolean reset = false;
        List<File> toScan = new ArrayList<>();
        while (cmdLine.hasNext()) {
            if ((a = cmdLine.nextBoolean("-i", "--interactive")) != null) {
                interactive = a.getBooleanValue();
            } else if ((a = cmdLine.nextBoolean("-r", "--reset")) != null) {
                reset = a.getBooleanValue();
            } else if (cmdLine.peek().isNonOption()) {
                String folder = cmdLine.nextNonOption(NutsArgumentName.of("Folder", session)).getString();
                run = true;
                toScan.add(new File(folder));
            } else {
                context.configureLast(cmdLine);
            }
        }
        if (cmdLine.isExecMode()) {
            if (reset) {
                resetAllProjectServices();
            }
            if (!reset && toScan.isEmpty()) {
                toScan.add(new File("."));
            }

            int scanned = scan(toScan, interactive);
            if (appContext.getSession().isPlainOut()) {
                appContext.getSession().out().printf("##SUMMARY## : %s projects scanned%n", scanned);
            }
        }
    }

    public void find(NutsCommandLine cmdLine, NutsApplicationContext context) {
        NutsArgument a;
        NutsSession session = context.getSession();
        List<File> toScan = new ArrayList<>();
        String where = null;
        while (cmdLine.hasNext()) {
            if ((a = cmdLine.nextString("-w", "--where")) != null) {
                where = a.getValue().getString();
            } else if (cmdLine.peek().isNonOption()) {
                String folder = cmdLine.nextNonOption(NutsArgumentName.of("Folder", session)).getString();
                toScan.add(new File(folder));
            } else {
                context.configureLast(cmdLine);
            }
        }
        if (cmdLine.isExecMode()) {
            int scanned = find(toScan, where);
            if (appContext.getSession().isPlainOut()) {
                appContext.getSession().out().printf("##SUMMARY## : %s projects scanned%n", scanned);
            }
        }
    }

    public void status(NutsCommandLine cmd, NutsApplicationContext appContext) {
        boolean progress = true;
        boolean verbose = false;
        Boolean commitable = null;
        Boolean dirty = null;
        Boolean newP = null;
        Boolean uptodate = null;
        Boolean old = null;
        Boolean invalid = null;
//        NutsTableFormat tf = appContext.getWorkspace().format().table()
//                .addHeaderCells("Id", "Local", "Remote", "Status");
        List<String> filters = new ArrayList<>();
        NutsArgument a;
        while (cmd.hasNext()) {
            if (appContext.configureFirst(cmd)) {
                //consumed
//            } else if (tf.configureFirst(cmd)) {
                //consumed
            } else if ((a = cmd.nextBoolean("-c", "--commitable", "--changed")) != null) {
                commitable = a.getBooleanValue();
            } else if ((a = cmd.nextBoolean("-d", "--dirty")) != null) {
                dirty = a.getBooleanValue();
            } else if ((a = cmd.nextBoolean("-w", "--new")) != null) {
                newP = a.getBooleanValue();
            } else if ((a = cmd.nextBoolean("-o", "--old")) != null) {
                old = a.getBooleanValue();
            } else if ((a = cmd.nextBoolean("-0", "--ok", "--uptodate")) != null) {
                uptodate = a.getBooleanValue();
            } else if ((a = cmd.nextBoolean("-e", "--invalid", "--error")) != null) {
                invalid = a.getBooleanValue();
            } else if ((a = cmd.nextBoolean("-p", "--progress")) != null) {
                progress = a.getBooleanValue();
            } else if ((a = cmd.nextBoolean("-v", "--verbose")) != null) {
                verbose = a.getBooleanValue();
            } else if (cmd.peek().isOption()) {
                cmd.setCommandName("nwork check").unexpectedArgument();
            } else {
                filters.add(cmd.next().getString());
            }
        }

        Boolean[] b = new Boolean[]{commitable, newP, uptodate, old, invalid, dirty};
        updateBools(b, true);
        commitable = b[0];
        newP = b[1];
        uptodate = b[2];
        old = b[3];
        invalid = b[4];
        dirty = b[5];

        Map<String, NutsDescriptor> dependencies = new HashMap<>();

        List<DataRow> ddd = new ArrayList<>();

        List<ProjectService> all = findProjectServices();
        all.sort((x, y) -> x.getConfig().getId().compareTo(y.getConfig().getId()));
        NutsSession session = appContext.getSession();

        for (Iterator<ProjectService> iterator = all.iterator(); iterator.hasNext(); ) {
            ProjectService projectService = iterator.next();
            String id = projectService.getConfig().getId();
            NutsDescriptor pom = projectService.getPom();
            if (pom != null) {
                dependencies.put(NutsId.of(id, session).getShortName(), pom);
            }
        }

        for (Iterator<ProjectService> iterator = all.iterator(); iterator.hasNext(); ) {
            ProjectService projectService = iterator.next();
            if (!matches(projectService.getConfig().getId(), filters)) {
                iterator.remove();
            }
        }

        int maxSize = 1;
        NutsVersionParser parser = NutsVersionParser.of(session);
        for (int i = 0; i < all.size(); i++) {
            ProjectService projectService = all.get(i);
            DataRow d = new DataRow();
            d.id = projectService.getConfig().getId();
            NutsDescriptor pom = dependencies.get(NutsId.of(d.id, session).getShortName());
            if (pom != null) {
                for (NutsDependency dependency : pom.getDependencies()) {
                    String did = dependency.getGroupId() + ":" + dependency.getArtifactId();
                    NutsDescriptor expectedPom = dependencies.get(NutsId.of(did, session).getShortName());
                    if (expectedPom != null) {
                        String expectedVersion = expectedPom.getId().getVersion().toString();
                        String currentVersion = dependency.getVersion().toString();
                        currentVersion = currentVersion.trim();
                        if (currentVersion.contains("$")) {
                            for (NutsDescriptorProperty entry : pom.getProperties()) {
                                String k = "${" + entry.getName() + "}";
                                if (currentVersion.equals(k)) {
                                    currentVersion = entry.getValue();
                                    break;
                                }
                            }
                        }
                        if (!Objects.equals(expectedVersion, currentVersion)) {
                            d.dependencies.add(new DiffVersion(did, currentVersion, expectedVersion));
                        }
                    }
                }
            }
            if (progress && session.isPlainOut()) {
                maxSize = Math.max(maxSize, projectService.getConfig().getId().length());
                session.out().resetLine().printf("(%s / %s) %s", (i + 1), all.size(), _StringUtils.alignLeft(projectService.getConfig().getId(), maxSize));
            }
            d.local = projectService.detectLocalVersion();
            d.remote = d.local == null ? null : projectService.detectRemoteVersion();
            if (d.local == null) {
                d.status = "invalid";
                d.local = "";
                d.remote = "";
            } else if (d.remote == null) {
                d.remote = "";
                d.status = "new";
            } else {
                int t = parser.parse(d.local).compareTo(d.remote);
                if (t > 0) {
                    d.status = "commitable";
                } else if (t < 0) {
                    d.status = "old";
                } else {
                    File l = projectService.detectLocalVersionFile(d.id + "#" + d.local);
                    File r = projectService.detectRemoteVersionFile(d.id + "#" + d.remote);
                    if (l != null && r != null) {
                        DiffResult result = Diff.of(l, r).verbose(verbose).eval();
                        if (result.hasChanges()) {
                            d.status = "dirty";
                            if (verbose) {
                                d.details = result.all();
                            }
                        } else {
                            d.status = "uptodate";
                        }
                    } else {
                        //
                        d.status = "uptodate";
                    }
                }
            }
            ddd.add(d);
        }
        if (all.size() > 0) {
            session.out().println("");
        }

        Collections.sort(ddd);
        for (Iterator<DataRow> iterator = ddd.iterator(); iterator.hasNext(); ) {
            DataRow d = iterator.next();
            switch (d.status) {
                case "invalid": {
                    if (!invalid) {
                        iterator.remove();
                    }
                    break;
                }
                case "new": {
                    if (!newP) {
                        iterator.remove();
                    }
                    break;
                }
                case "commitable": {
                    if (!commitable) {
                        iterator.remove();
                    }
                    break;
                }
                case "dirty": {
                    if (!dirty) {
                        iterator.remove();
                    }
                    break;
                }
                case "old": {
                    if (!old) {
                        iterator.remove();
                    }
                    break;
                }
                case "uptodate": {
                    if (!uptodate) {
                        iterator.remove();
                    }
                    break;
                }
            }
            //"%s %s %s%n",projectService.getConfig().getId(),local,remote
//            tf.addRow(d.id, d.local, d.remote, d.status);
        }
        if (!ddd.isEmpty() || !session.isPlainOut()) {
            NutsTexts tfactory = NutsTexts.of(session);
            if (session.isPlainOut()) {
                for (DataRow p2 : ddd) {
                    String status = p2.status;
                    NutsTexts tf = NutsTexts.of(session);
                    int len = tf.parse(status).textLength();
                    while (len < 10) {
                        status += " ";
                        len++;
                    }
                    switch (tf.ofPlain(p2.status).filteredText()) {
                        case "new": {
                            session.out().printf("[%s] %s : %s",
                                    tfactory.ofStyled("new", NutsTextStyle.primary3()),
                                    p2.id,
                                    tfactory.ofStyled(p2.local, NutsTextStyle.primary2())
                            );
                            break;
                        }
                        case "commitable": {
                            session.out().printf("[%s] %s : %s - %s",
                                    tfactory.ofStyled("commitable", NutsTextStyle.primary4()),
                                    p2.id,
                                    tfactory.ofStyled(p2.local, NutsTextStyle.primary2()),
                                    p2.remote
                            );
                            break;
                        }
                        case "dirty": {
                            session.out().printf("[```error dirty```] %s : ```error %s``` - %s", p2.id, p2.local, p2.remote);
                            printDiffResults("  ", session.out(), p2.details);
                            break;
                        }
                        case "old": {
                            session.out().printf("[%s] %s : ```error %s``` - %s",
                                    tfactory.ofStyled("old", NutsTextStyle.primary2()),
                                    p2.id, p2.local, p2.remote);
                            break;
                        }
                        case "invalid": {
                            session.out().printf("[```error invalid```invalid ] %s : ```error %s``` - %s", p2.id, p2.local, p2.remote);
                            break;
                        }
                        case "uptodate": {
                            session.out().printf("[uptodate] %s : %s", p2.id, p2.local);
                            break;
                        }
                        default: {
                            session.out().printf("[%s] %s : %s - %s", status, p2.id, p2.local, p2.remote);
                            break;
                        }
                    }
                    if (p2.dependencies.size() > 0) {
                        session.out().printf(" ; bad-deps:");
                        for (DiffVersion dependency : p2.dependencies) {
                            session.out().printf(" %s : %s <> expected %s", dependency.id,
                                    parser.parse(dependency.current),
                                    parser.parse(dependency.expected)
                            );
                        }
                    }
                    session.out().println();
                }
            } else {
                NutsObjectFormat.of(session)
                        .setValue(ddd).println();
            }
        }
    }

    private void printDiffResults(String prefix, NutsPrintStream out, List<DiffItem> result) {
        if (result != null) {
            for (DiffItem diffItem : result) {
                out.printf("%s%s%n", prefix, diffItem);
                printDiffResults(prefix + "  ", out, diffItem.children());
            }
        }
    }

    private boolean matches(String id, List<String> filters) {
        boolean accept = filters.isEmpty();
        if (!accept) {
            NutsSession session = appContext.getSession();
            NutsId nid = NutsId.of(id, session);
            for (String filter : filters) {
                if (id.equals(filter)
                        || id.matches(wildcardToRegex(filter))
                        || nid.getArtifactId().equals(filter)) {
                    accept = true;
                    break;
                }
            }
        }
        return accept;
    }

    public NutsPath getConfigFile() {
        return sharedConfigFolder.resolve("workspace.projects");
    }

    public void resetAllProjectServices() {
        NutsPath storeLocation = sharedConfigFolder.resolve("projects");
        if (storeLocation.isDirectory()) {
            for (NutsPath file : storeLocation.list().toList()) {
                if (file.isRegularFile() && file.getName().endsWith(".config")) {
                    file.delete();
                }
            }
        }
    }

    public List<ProjectService> findProjectServices() {
        List<ProjectService> all = new ArrayList<>();
        NutsPath storeLocation = sharedConfigFolder.resolve("projects");

        if (storeLocation.isDirectory()) {
            for (NutsPath file : storeLocation.list().toList()) {
                if (file.isRegularFile() && file.getName().endsWith(".config")) {
                    try {
                        all.add(new ProjectService(appContext, config.getDefaultRepositoryAddress(), file));
                    } catch (IOException e) {
                        //ignore
                    }
                }
            }
        }
        return all;
    }

    public int find(List<File> folders, String where) {
        FileScanner fs = new FileScanner();
        if (where != null && where.trim().length() > 0) {
            fs.setPathFilter(FileScanner.parseExpr(where, appContext.getSession()));
        }
        fs.getSource().addAll(folders.stream().map(File::toPath).collect(Collectors.toSet()));
        fs.scan().forEach(x -> {
            appContext.getSession().out().printf("%s%n", x);
        });
        return 0;
    }

    public int scan(List<File> folders, boolean interactive) {
        Stack<File> stack = new Stack<>();
        List<ScanResult> result = new ArrayList<>();
        for (File folder : folders) {
            stack.push(folder);
        }
        int scanned = 0;
        NutsSession session = appContext.getSession();
        boolean structuredOutContentType = session.isTrace() && session.getOutputFormat() != NutsContentType.PLAIN;
        while (!stack.isEmpty()) {
            File folder = stack.pop();
            if (folder.isDirectory()) {
                if (isScanEnabled(folder)) {
                    NutsTexts text = NutsTexts.of(session);
                    ProjectConfig p2 = new ProjectService(appContext, config.getDefaultRepositoryAddress(), new ProjectConfig().setPath(folder.getPath())
                    ).rebuildProjectMetadata();
                    if (p2.getTechnologies().size() > 0) {
                        ProjectService projectService = new ProjectService(appContext, config.getDefaultRepositoryAddress(), p2);
                        boolean loaded = false;
                        try {
                            loaded = projectService.load();
                        } catch (Exception ex) {
                            //
                        }
                        if (loaded) {
                            ProjectConfig p3 = projectService.getConfig();
                            if (p3.equals(p2)) {
                                //no updates!
                                if (session.isPlainOut()) {
                                    session.out().printf("already registered project folder %s%n", formatProjectConfig(appContext, p2));
                                }
                                if (structuredOutContentType) {
                                    result.add(new ScanResult(folder.getPath(), "already-registered", NutsMessage.cstyle("already registered project folder %s", formatProjectConfig(appContext, p2)).toString()));
                                }
                            } else if (!p2.getPath().equals(p3.getPath())) {
                                if (session.isPlainOut()) {
                                    session.out().printf("```error [CONFLICT]``` multiple paths for the same id %s. "
                                                    + "please consider adding .nuts-info file with " + SCAN + "=false  :  %s -- %s%n",
                                            text.ofStyled(p2.getId(), NutsTextStyle.primary2()),
                                            text.ofStyled(p2.getPath(), NutsTextStyle.path()),
                                            text.ofStyled(p3.getPath(), NutsTextStyle.path())
                                    );
                                }
                                if (structuredOutContentType) {
                                    result.add(new ScanResult(folder.getPath(), "conflict",
                                            NutsMessage.cstyle(
                                                    "[CONFLICT] multiple paths for the same id %s. "
                                                            + "please consider adding .nuts-info file with " + SCAN + "=false  :  %s -- %s",
                                                    p2.getId(),
                                                    p2.getPath(),
                                                    p3.getPath()
                                            ).toString()
                                    ));
                                }
                            } else {
                                if (session.isPlainOut()) {
                                    session.out().printf("reloaded project folder %s%n", formatProjectConfig(appContext, p2));
                                }
                                if (structuredOutContentType) {
                                    result.add(new ScanResult(folder.getPath(), "reloaded",
                                            NutsMessage.cstyle(
                                                    "reloaded project folder %s", formatProjectConfig(appContext, p2).toString()
                                            ).toString()
                                    ));
                                }
//                String repo = term.readLine("Enter Repository ####%s####: ", ((p2.getAddress() == null || p2.getAddress().getNutsRepository() == null )? "" : ("(" + p2.getAddress().getNutsRepository() + ")")));
//                if (!StringUtils.isEmpty(repo)) {
//                    p2.setAddress(new );
//                }
                                ProjectService ps = new ProjectService(appContext, null, p2);
                                try {
                                    ps.save();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                scanned++;
                            }
                        } else {

                            if (session.isPlainOut()) {
                                session.out().printf("detected Project Folder %s%n", formatProjectConfig(appContext, p2));
                            }
                            if (interactive) {
                                String id = session.getTerminal().readLine("enter Id %s: ",
                                        (p2.getId() == null ? "" : ("(" + text.ofPlain(p2.getId()) + ")")));
                                if (!NutsBlankable.isBlank(id)) {
                                    p2.setId(id);
                                }
                            }
                            if (structuredOutContentType) {
                                result.add(new ScanResult(folder.getPath(), "detected",
                                        NutsMessage.cstyle("detected Project Folder").toString()
                                ));
                            }
//                String repo = term.readLine("Enter Repository ####%s####: ", ((p2.getAddress() == null || p2.getAddress().getNutsRepository() == null )? "" : ("(" + p2.getAddress().getNutsRepository() + ")")));
//                if (!StringUtils.isEmpty(repo)) {
//                    p2.setAddress(new );
//                }
                            ProjectService ps = new ProjectService(appContext, null, p2);
                            try {
                                ps.save();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            scanned++;
                        }
                    }
                    File[] aa = folder.listFiles();
                    for (File file : aa) {
                        if (file.isDirectory()) {
                            stack.push(file);
                        }
                    }
                }
            }
        }
        if (structuredOutContentType) {
            session.out().printlnf(result);
        }
        return scanned;
    }

    public void setScanEnabled(Path folder, boolean enable) {
        Path ni = folder.resolve(".nuts-info");
        Map p = null;
        boolean scan = true;
        NutsSession session = appContext.getSession();
        if (Files.isRegularFile(ni)) {
            try {
                p = NutsElements.of(session).json().parse(ni, Map.class);
                String v = p.get(SCAN) == null ? null : String.valueOf(p.get(SCAN));
                if (v == null || "false".equals(v.trim())) {
                    scan = false;
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException(ex);
            }
            //=true
        }
        if (scan != enable) {
            if (p == null) {
                p = new Properties();
            }
            p.put(SCAN, enable);
            NutsElements.of(session).json().setValue(p).print(ni);
        }
    }

    public boolean isScanEnabled(File folder) {
        boolean scan = true;
        File ni = new File(folder, ".nuts-info");
        Map p = null;
        if (ni.isFile()) {
            try {
                NutsSession session = appContext.getSession();
                p = NutsElements.of(session).json().parse(ni, Map.class);
                String v = p.get(SCAN) == null ? null : String.valueOf(p.get(SCAN));
                if (v == null || "false".equals(v.trim())) {
                    scan = false;
                }
            } catch (Exception ex) {
                //ignore
            }
            //=true
        }
        return scan;
    }

    public int setWorkspaceConfigParam(NutsCommandLine cmd, NutsApplicationContext appContext) {
        NutsArgument a;
        while (cmd.hasNext()) {
            if ((a = cmd.nextString("-r", "--repo")) != null) {
                WorkspaceConfig conf = getWorkspaceConfig();
                conf.getDefaultRepositoryAddress().setNutsRepository(a.getValue().getString());
                setWorkspaceConfig(conf);
            } else if ((a = cmd.nextString("-w", "--workspace")) != null) {
                WorkspaceConfig conf = getWorkspaceConfig();
                conf.getDefaultRepositoryAddress().setNutsWorkspace(a.getValue().getString());
                setWorkspaceConfig(conf);
            } else {
                cmd.setCommandName("nwork set").unexpectedArgument();
            }
        }
        return 0;
    }

    public static class ScanResult {

        String path;
        String status;
        String message;

        public ScanResult(String path, String status, String message) {
            this.path = path;
            this.message = message;
            this.status = status;
            System.out.println(this.path + " " + status/*+" "+message*/);
        }

    }

    static class DiffVersion {

        String id;
        String current;
        String expected;

        public DiffVersion(String id, String current, String expected) {
            this.id = id;
            this.current = current;
            this.expected = expected;
        }

    }

    static class DataRow implements Comparable<DataRow> {

        String id;
        String local;
        String remote;
        String status;
        List<DiffItem> details;
        List<DiffVersion> dependencies = new ArrayList<>();

        @Override
        public int compareTo(DataRow o) {
            int v = status.compareTo(o.status);
            if (v != 0) {
                return v;
            }
            v = id.compareTo(o.id);
            return v;
        }
    }
}

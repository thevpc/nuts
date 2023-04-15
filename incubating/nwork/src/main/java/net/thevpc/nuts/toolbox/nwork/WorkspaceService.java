package net.thevpc.nuts.toolbox.nwork;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NObjectFormat;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.ndiff.jar.Diff;
import net.thevpc.nuts.toolbox.ndiff.jar.DiffItem;
import net.thevpc.nuts.toolbox.ndiff.jar.DiffResult;
import net.thevpc.nuts.toolbox.nwork.config.ProjectConfig;
import net.thevpc.nuts.toolbox.nwork.config.RepositoryAddress;
import net.thevpc.nuts.toolbox.nwork.config.WorkspaceConfig;
import net.thevpc.nuts.toolbox.nwork.filescanner.FileScanner;
import net.thevpc.nuts.util.NRef;

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
    private final NSession session;
    private final NPath sharedConfigFolder;

    public WorkspaceService(NSession session) {
        this.session = session;
        sharedConfigFolder = session.getAppVersionFolder(NStoreLocation.CONFIG, NWorkConfigVersions.CURRENT);
        NPath c = getConfigFile();
        if (c.isRegularFile()) {
            try {
                config = NElements.of(session).json().parse(c, WorkspaceConfig.class);
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
        NPath configFile = getConfigFile();
        configFile.mkParentDirs();
        NElements.of(session).json().setValue(c).print(configFile);
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

    public void enableScan(NCmdLine cmdLine, NSession session, boolean enable) {
        int count = 0;
        while (cmdLine.hasNext()) {
            if (cmdLine.peek().get(session).isNonOption()) {
                String expression = cmdLine.next().flatMap(NLiteral::asString).get(session);
                if (cmdLine.isExecMode()) {
                    setScanEnabled(Paths.get(expression), enable);
                    count++;
                }
            } else {
                session.configureLast(cmdLine);
            }
        }

        if (count == 0) {
            throw new NExecutionException(session, NMsg.ofPlain("missing projects"), 1);
        }
    }

    public void list(NCmdLine cmd, NSession session) {
       NArg a;
        List<String> filters = new ArrayList<>();
        cmd.setCommandName("nwork list");
        while (cmd.hasNext()) {
            if ((a = cmd.nextNonOption().orNull()) != null) {
                filters.add(a.asString().get(session));
            } else {
                session.configureLast(cmd);
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
            if (session.isPlainOut()) {
                for (ProjectConfig p2 : result) {
                    session.out().println(
                            formatProjectConfig(session, p2)
                    );
                }
            } else {
                NObjectFormat.of(session)
                        .setValue(result).println();
            }
        }
    }

    private NTextBuilder formatProjectConfig(NSession session, ProjectConfig p2) {
        NTexts text = NTexts.of(session);
        return text.ofBuilder()
                .append(p2.getId(), NTextStyle.primary4())
                .append(" ")
                .appendJoined(
                        text.ofPlain(", "),
                        p2.getTechnologies().stream().map(
                                x -> text.ofStyled(x, NTextStyle.primary5())
                        ).collect(Collectors.toList())
                )
                .append(" : ")
                .append(p2.getPath(), NTextStyle.path());
    }

    public void scan(NCmdLine cmdLine, NSession session) {
        boolean interactive = false;
        NArg a;
        boolean run = false;
        boolean reset = false;
        List<File> toScan = new ArrayList<>();
        while (cmdLine.hasNext()) {
            if ((a = cmdLine.nextFlag("-i", "--interactive").orNull()) != null) {
                interactive = a.getBooleanValue().get(session);
            } else if ((a = cmdLine.nextFlag("-r", "--reset").orNull()) != null) {
                reset = a.getBooleanValue().get(session);
            } else if (cmdLine.peek().get(session).isNonOption()) {
                String folder = cmdLine.nextNonOption(NArgName.of("Folder", session))
                        .flatMap(NLiteral::asString).get(session);
                run = true;
                toScan.add(new File(folder));
            } else {
                session.configureLast(cmdLine);
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
            if (session.isPlainOut()) {
                session.out().println(NMsg.ofC("##SUMMARY## : %s projects scanned", scanned));
            }
        }
    }

    public void find(NCmdLine cmdLine, NSession session) {
        NArg a;
        List<File> toScan = new ArrayList<>();
        String where = null;
        while (cmdLine.hasNext()) {
            if ((a = cmdLine.nextEntry("-w", "--where").orNull()) != null) {
                where = a.getStringValue().get(session);
            } else if (cmdLine.peek().get(session).isNonOption()) {
                String folder = cmdLine.nextNonOption(NArgName.of("Folder", session))
                        .flatMap(NLiteral::asString).get(session);
                toScan.add(new File(folder));
            } else {
                session.configureLast(cmdLine);
            }
        }
        if (cmdLine.isExecMode()) {
            int scanned = find(toScan, where);
            if (session.isPlainOut()) {
                session.out().println(NMsg.ofC("##SUMMARY## : %s projects scanned", scanned));
            }
        }
    }

    public void push(NCmdLine cmdLine, NSession session) {
        cmdLine.setCommandName("nwork push");
        //rsync /home/me/.m2/repository/net/thevpc/nuts/nuts/0.8.4/*  vpc@thevpc.net:/home/me/.m2/repository/net/thevpc/nuts/nuts/0.8.4/
        List<NId> idsToPush = new ArrayList<>();
        NRef<String> remoteServer = NRef.ofNull(String.class);
        NRef<String> remoteUser = NRef.ofNull(String.class);
        while (cmdLine.hasNext()) {
            if (session.configureFirst(cmdLine)) {

            } else if (cmdLine.withNextEntry((v, a, s) -> remoteServer.set(v), "--remote-server", "--to-server", "--to", "-t")) {
            } else if (cmdLine.withNextEntry((v, a, s) -> remoteUser.set(v), "--remote-user")) {
            } else if (cmdLine.isNextNonOption()) {
                NArg a = cmdLine.next().get();
                idsToPush.add(NId.of(a.toString()).get());
            } else {
                session.configureLast(cmdLine);
            }
        }
        if (idsToPush.isEmpty()) {
            cmdLine.throwMissingArgument();
        }
        if (remoteUser.isBlank()) {
            remoteUser.set(System.getProperty("user.name"));
        }
        if (remoteServer.isBlank()) {
            cmdLine.throwMissingArgumentByName("--remote-server");
        }
        for (NId id : idsToPush) {
            String groupIdPath = String.join("/", id.getGroupId().split("[.]"));
            String p = groupIdPath + "/" + id.getArtifactId();
            if (id.getVersion() != null) {
                p += "/" + id.getVersion();
            }
            NExecCommand.of(session).addCommand(
                            "rsync")
                    .addCommand(NPath.ofUserHome(session).resolve(".m2/repository")
                            .resolve(p).stream().map(NPath::toString).toList())
                    .addCommand(remoteUser + "@" + remoteServer + ":/home/" + remoteUser + "/.m2/repository/" + p
                    ).setFailFast(true).run();
        }
    }

    public void status(NCmdLine cmd, NSession session) {
        boolean progress = true;
        boolean verbose = false;
        Boolean commitable = null;
        Boolean dirty = null;
        Boolean newP = null;
        Boolean uptodate = null;
        Boolean old = null;
        Boolean invalid = null;
//        NutsTableFormat tf = session.getWorkspace().format().table()
//                .addHeaderCells("Id", "Local", "Remote", "Status");
        List<String> filters = new ArrayList<>();
        NArg a;
        while (cmd.hasNext()) {
            if (session.configureFirst(cmd)) {
                //consumed
//            } else if (tf.configureFirst(cmd)) {
                //consumed
            } else if ((a = cmd.nextFlag("-c", "--commitable", "--changed").orNull()) != null) {
                commitable = a.getBooleanValue().get(session);
            } else if ((a = cmd.nextFlag("-d", "--dirty").orNull()) != null) {
                dirty = a.getBooleanValue().get(session);
            } else if ((a = cmd.nextFlag("-w", "--new").orNull()) != null) {
                newP = a.getBooleanValue().get(session);
            } else if ((a = cmd.nextFlag("-o", "--old").orNull()) != null) {
                old = a.getBooleanValue().get(session);
            } else if ((a = cmd.nextFlag("-0", "--ok", "--uptodate").orNull()) != null) {
                uptodate = a.getBooleanValue().get(session);
            } else if ((a = cmd.nextFlag("-e", "--invalid", "--error").orNull()) != null) {
                invalid = a.getBooleanValue().get(session);
            } else if ((a = cmd.nextFlag("-p", "--progress").orNull()) != null) {
                progress = a.getBooleanValue().get(session);
            } else if ((a = cmd.nextFlag("-v", "--verbose").orNull()) != null) {
                verbose = a.getBooleanValue().get(session);
            } else if (cmd.isNextOption()) {
                cmd.setCommandName("nwork check").throwUnexpectedArgument();
            } else {
                filters.add(cmd.next().flatMap(NLiteral::asString).get(session));
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

        Map<String, NDescriptor> dependencies = new HashMap<>();

        List<DataRow> ddd = new ArrayList<>();

        List<ProjectService> all = findProjectServices();
        all.sort((x, y) -> x.getConfig().getId().compareTo(y.getConfig().getId()));

        for (Iterator<ProjectService> iterator = all.iterator(); iterator.hasNext(); ) {
            ProjectService projectService = iterator.next();
            String id = projectService.getConfig().getId();
            NDescriptor pom = projectService.getPom();
            if (pom != null) {
                dependencies.put(NId.of(id).get(session).getShortName(), pom);
            }
        }

        for (Iterator<ProjectService> iterator = all.iterator(); iterator.hasNext(); ) {
            ProjectService projectService = iterator.next();
            if (!matches(projectService.getConfig().getId(), filters)) {
                iterator.remove();
            }
        }

        int maxSize = 1;
        for (int i = 0; i < all.size(); i++) {
            ProjectService projectService = all.get(i);
            DataRow d = new DataRow();
            d.id = projectService.getConfig().getId();
            NDescriptor pom = dependencies.get(NId.of(d.id).get(session).getShortName());
            if (pom != null) {
                for (NDependency dependency : pom.getDependencies()) {
                    String did = dependency.getGroupId() + ":" + dependency.getArtifactId();
                    NDescriptor expectedPom = dependencies.get(NId.of(did).get(session).getShortName());
                    if (expectedPom != null) {
                        String expectedVersion = expectedPom.getId().getVersion().toString();
                        String currentVersion = dependency.getVersion().toString();
                        currentVersion = currentVersion.trim();
                        if (currentVersion.contains("$")) {
                            for (NDescriptorProperty entry : pom.getProperties()) {
                                String k = "${" + entry.getName() + "}";
                                if (currentVersion.equals(k)) {
                                    currentVersion = entry.getValue().asString().get(session);
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
                session.out().resetLine().print(NMsg.ofC("(%s / %s) %s", (i + 1), all.size(), _StringUtils.alignLeft(projectService.getConfig().getId(), maxSize)));
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
                int t = NVersion.of(d.local).get(session).compareTo(d.remote);
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
            NTexts tfactory = NTexts.of(session);
            if (session.isPlainOut()) {
                for (DataRow p2 : ddd) {
                    String status = p2.status;
                    NTexts tf = NTexts.of(session);
                    int len = tf.parse(status).textLength();
                    while (len < 10) {
                        status += " ";
                        len++;
                    }
                    switch (tf.ofPlain(p2.status).filteredText()) {
                        case "new": {
                            session.out().print(NMsg.ofC("[%s] %s : %s",
                                    tfactory.ofStyled("new", NTextStyle.primary3()),
                                    p2.id,
                                    tfactory.ofStyled(p2.local, NTextStyle.primary2())
                            ));
                            break;
                        }
                        case "commitable": {
                            session.out().print(NMsg.ofC("[%s] %s : %s - %s",
                                    tfactory.ofStyled("commitable", NTextStyle.primary4()),
                                    p2.id,
                                    tfactory.ofStyled(p2.local, NTextStyle.primary2()),
                                    p2.remote
                            ));
                            break;
                        }
                        case "dirty": {
                            session.out().print(NMsg.ofC("[```error dirty```] %s : ```error %s``` - %s", p2.id, p2.local, p2.remote));
                            printDiffResults("  ", session.out(), p2.details);
                            break;
                        }
                        case "old": {
                            session.out().print(NMsg.ofC("[%s] %s : ```error %s``` - %s",
                                    tfactory.ofStyled("old", NTextStyle.primary2()),
                                    p2.id, p2.local, p2.remote));
                            break;
                        }
                        case "invalid": {
                            session.out().print(NMsg.ofC("[```error invalid```invalid ] %s : ```error %s``` - %s", p2.id, p2.local, p2.remote));
                            break;
                        }
                        case "uptodate": {
                            session.out().print(NMsg.ofC("[uptodate] %s : %s", p2.id, p2.local));
                            break;
                        }
                        default: {
                            session.out().print(NMsg.ofC("[%s] %s : %s - %s", status, p2.id, p2.local, p2.remote));
                            break;
                        }
                    }
                    if (p2.dependencies.size() > 0) {
                        session.out().print(NMsg.ofC(" ; bad-deps:"));
                        for (DiffVersion dependency : p2.dependencies) {
                            session.out().print(NMsg.ofC(" %s : %s <> expected %s", dependency.id,
                                    NVersion.of(dependency.current).get(session),
                                    NVersion.of(dependency.expected).get(session)
                            ));
                        }
                    }
                    session.out().println();
                }
            } else {
                NObjectFormat.of(session)
                        .setValue(ddd).println();
            }
        }
    }

    private void printDiffResults(String prefix, NPrintStream out, List<DiffItem> result) {
        if (result != null) {
            for (DiffItem diffItem : result) {
                out.println(NMsg.ofC("%s%s", prefix, diffItem));
                printDiffResults(prefix + "  ", out, diffItem.children());
            }
        }
    }

    private boolean matches(String id, List<String> filters) {
        boolean accept = filters.isEmpty();
        if (!accept) {
            NId nid = NId.of(id).get(session);
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

    public NPath getConfigFile() {
        return sharedConfigFolder.resolve("workspace.projects");
    }

    public void resetAllProjectServices() {
        NPath storeLocation = sharedConfigFolder.resolve("projects");
        if (storeLocation.isDirectory()) {
            for (NPath file : storeLocation.list()) {
                if (file.isRegularFile() && file.getName().endsWith(".config")) {
                    file.delete();
                }
            }
        }
    }

    public List<ProjectService> findProjectServices() {
        List<ProjectService> all = new ArrayList<>();
        NPath storeLocation = sharedConfigFolder.resolve("projects");

        if (storeLocation.isDirectory()) {
            for (NPath file : storeLocation.list()) {
                if (file.isRegularFile() && file.getName().endsWith(".config")) {
                    try {
                        all.add(new ProjectService(session, config.getDefaultRepositoryAddress(), file));
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
            fs.setPathFilter(FileScanner.parseExpr(where, session));
        }
        fs.getSource().addAll(folders.stream().map(File::toPath).collect(Collectors.toSet()));
        fs.scan().forEach(x -> {
            session.out().println(x);
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
        boolean structuredOutContentType = session.isTrace() && session.getOutputFormat() != NContentType.PLAIN;
        while (!stack.isEmpty()) {
            File folder = stack.pop();
            if (folder.isDirectory()) {
                if (isScanEnabled(folder)) {
                    NTexts text = NTexts.of(session);
                    ProjectConfig p2 = new ProjectService(session, config.getDefaultRepositoryAddress(), new ProjectConfig().setPath(folder.getPath())
                    ).rebuildProjectMetadata();
                    if (p2.getTechnologies().size() > 0) {
                        ProjectService projectService = new ProjectService(session, config.getDefaultRepositoryAddress(), p2);
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
                                    session.out().println(NMsg.ofC("already registered project folder %s", formatProjectConfig(session, p2)));
                                }
                                if (structuredOutContentType) {
                                    result.add(new ScanResult(folder.getPath(), "already-registered", NMsg.ofC("already registered project folder %s", formatProjectConfig(session, p2)).toString()));
                                }
                            } else if (!p2.getPath().equals(p3.getPath())) {
                                if (session.isPlainOut()) {
                                    session.out().println(NMsg.ofC("```error [CONFLICT]``` multiple paths for the same id %s. "
                                                    + "please consider adding .nuts-info file with " + SCAN + "=false  :  %s -- %s",
                                            text.ofStyled(p2.getId(), NTextStyle.primary2()),
                                            text.ofStyled(p2.getPath(), NTextStyle.path()),
                                            text.ofStyled(p3.getPath(), NTextStyle.path())
                                    ));
                                }
                                if (structuredOutContentType) {
                                    result.add(new ScanResult(folder.getPath(), "conflict",
                                            NMsg.ofC(
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
                                    session.out().println(NMsg.ofC("reloaded project folder %s", formatProjectConfig(session, p2)));
                                }
                                if (structuredOutContentType) {
                                    result.add(new ScanResult(folder.getPath(), "reloaded",
                                            NMsg.ofC(
                                                    "reloaded project folder %s", formatProjectConfig(session, p2).toString()
                                            ).toString()
                                    ));
                                }
//                String repo = term.readLine("Enter Repository ####%s####: ", ((p2.getAddress() == null || p2.getAddress().getNutsRepository() == null )? "" : ("(" + p2.getAddress().getNutsRepository() + ")")));
//                if (!StringUtils.isEmpty(repo)) {
//                    p2.setAddress(new );
//                }
                                ProjectService ps = new ProjectService(session, null, p2);
                                try {
                                    ps.save();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                scanned++;
                            }
                        } else {

                            if (session.isPlainOut()) {
                                session.out().println(NMsg.ofC("detected Project Folder %s", formatProjectConfig(session, p2)));
                            }
                            if (interactive) {
                                String id = session.getTerminal().readLine(NMsg.ofC("enter Id %s: ",
                                        (p2.getId() == null ? "" : ("(" + text.ofPlain(p2.getId()) + ")"))));
                                if (!NBlankable.isBlank(id)) {
                                    p2.setId(id);
                                }
                            }
                            if (structuredOutContentType) {
                                result.add(new ScanResult(folder.getPath(), "detected",
                                        NMsg.ofPlain("detected Project Folder").toString()
                                ));
                            }
//                String repo = term.readLine("Enter Repository ####%s####: ", ((p2.getAddress() == null || p2.getAddress().getNutsRepository() == null )? "" : ("(" + p2.getAddress().getNutsRepository() + ")")));
//                if (!StringUtils.isEmpty(repo)) {
//                    p2.setAddress(new );
//                }
                            ProjectService ps = new ProjectService(session, null, p2);
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
            session.out().println(result);
        }
        return scanned;
    }

    public void setScanEnabled(Path folder, boolean enable) {
        Path ni = folder.resolve(".nuts-info");
        Map p = null;
        boolean scan = true;
        if (Files.isRegularFile(ni)) {
            try {
                p = NElements.of(session).json().parse(ni, Map.class);
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
            NElements.of(session).json().setValue(p).print(ni);
        }
    }

    public boolean isScanEnabled(File folder) {
        boolean scan = true;
        File ni = new File(folder, ".nuts-info");
        Map p = null;
        if (ni.isFile()) {
            try {
                p = NElements.of(session).json().parse(ni, Map.class);
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

    public int setWorkspaceConfigParam(NCmdLine cmd, NSession session) {
        NArg a;
        while (cmd.hasNext()) {
            if ((a = cmd.nextEntry("-r", "--repo").orNull()) != null) {
                WorkspaceConfig conf = getWorkspaceConfig();
                conf.getDefaultRepositoryAddress().setNutsRepository(a.getStringValue().get(session));
                setWorkspaceConfig(conf);
            } else if ((a = cmd.nextEntry("-w", "--workspace").orNull()) != null) {
                WorkspaceConfig conf = getWorkspaceConfig();
                conf.getDefaultRepositoryAddress().setNutsWorkspace(a.getStringValue().get(session));
                setWorkspaceConfig(conf);
            } else {
                cmd.setCommandName("nwork set").throwUnexpectedArgument();
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

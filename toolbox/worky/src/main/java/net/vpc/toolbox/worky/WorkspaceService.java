package net.vpc.toolbox.worky;

import net.vpc.app.nuts.*;
import net.vpc.common.io.IOUtils;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.worky.config.ProjectConfig;
import net.vpc.toolbox.worky.config.RepositoryAddress;
import net.vpc.toolbox.worky.config.WorkspaceConfig;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import net.vpc.app.nuts.NutsCommandLine;

public class WorkspaceService {

    public static final String SCAN = "net.vpc.app.nuts.toolbox.worky.scan";
    private WorkspaceConfig config;
    private NutsApplicationContext appContext;

    public WorkspaceService(NutsApplicationContext appContext) {
        this.appContext = appContext;
        Path c = getConfigFile();
        if (Files.isRegularFile(c)) {
            try {
                config = appContext.getWorkspace().json().parse(c, WorkspaceConfig.class);
            } catch (Exception ex) {
                //
            }
        }
        if (config == null) {
            config = new WorkspaceConfig();
        }
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
        Path configFile = getConfigFile();
        try {
            Files.createDirectories(configFile.getParent());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        appContext.getWorkspace().json().value(c).print(configFile);
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

    public void enableScan(NutsCommandLine cmd, NutsApplicationContext context, boolean enable) {
        int count = 0;
        while (cmd.hasNext()) {
            if (context.configureFirst(cmd)) {
                //consumed
            } else {
                String expression = cmd.next().getString();
                if (cmd.isExecMode()) {
                    setScanEnabled(context.getWorkspace().io().path(expression), enable);
                    count++;
                }
            }
        }

        if (count == 0) {
            throw new NutsExecutionException(context.getWorkspace(), "Missing projects", 1);
        }
    }

    public void list(NutsCommandLine cmd, NutsApplicationContext appContext) {
        NutsArgument a;
        List<String> filters = new ArrayList<>();
        while (cmd.hasNext()) {
            if (appContext.configureFirst(cmd)) {
                //consumed
            } else if ((a = cmd.requireNonOption().next()) != null) {
                filters.add(a.getString());
            } else {
                cmd.setCommandName("worky list").unexpectedArgument();
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
            result.sort((x, y) -> x.getId().compareTo(y.getId()));
            if (appContext.session().isPlainOut()) {
                for (ProjectConfig p2 : result) {
                    appContext.session().out().printf("[[%s]] {{%s}}: ==%s==%n", p2.getId(), p2.getTechnologies(), p2.getPath());
                }
            } else {
                appContext.workspace().object()
                        .session(appContext.session())
                        .value(result).println();
            }
        }
    }

    public void scan(NutsCommandLine cmdLine, NutsApplicationContext context) {
        boolean interactive = false;
        NutsArgument a;
        boolean run = false;
        NutsCommandLineFormat commandLineFormat = context.workspace().commandLine();
        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {
                //consumed
            } else if ((a = cmdLine.nextBoolean("-i", "--interactive")) != null) {
                interactive = a.getBooleanValue();
            } else {
                String folder = cmdLine.nextNonOption(commandLineFormat.createName("Folder")).getString();
                run = true;
                if (cmdLine.isExecMode()) {
                    scan(new File(folder), interactive);
                }
            }
        }
        if (!run) {
            scan(new File("."), interactive);
        }
    }

    public void status(NutsCommandLine cmd, NutsApplicationContext appContext) {
        boolean progress = true;
        Boolean commitable = null;
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
            } else if (cmd.peek().isOption()) {
                cmd.setCommandName("worky check").unexpectedArgument();
            } else {
                filters.add(cmd.next().getString());
            }
        }

        Boolean[] b = new Boolean[]{commitable, newP, uptodate, old, invalid};
        updateBools(b, true);
        commitable = b[0];
        newP = b[1];
        uptodate = b[2];
        old = b[3];
        invalid = b[4];

        List<DataRow> ddd = new ArrayList<>();

        List<ProjectService> all = findProjectServices();
        all.sort((x, y) -> x.getConfig().getId().compareTo(y.getConfig().getId()));
        for (Iterator<ProjectService> iterator = all.iterator(); iterator.hasNext();) {
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
            if (progress && appContext.session().isPlainOut()) {
                maxSize = Math.max(maxSize, projectService.getConfig().getId().length());
                if (i > 0) {
                    appContext.session().out().printf("`move-line-start`");
                    appContext.session().out().printf("`move-up`");
                }
                appContext.session().out().printf("(%s / %s) %s%n", (i + 1), all.size(), StringUtils.alignLeft(projectService.getConfig().getId(), maxSize));
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
                int t = appContext.getWorkspace().version().parse(d.local).compareTo(d.remote);
                if (t > 0) {
                    d.status = "commitable";
                } else if (t < 0) {
                    d.status = "old";
                } else {
                    d.status = "uptodate";
                }
            }
            ddd.add(d);
        }

        Collections.sort(ddd);
        for (Iterator<DataRow> iterator = ddd.iterator(); iterator.hasNext();) {
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
                    d.status = "{{new}}";
                    d.local = "[[" + d.local + "]]";
                    break;
                }
                case "commitable": {
                    if (!commitable) {
                        iterator.remove();
                    }
                    d.status = "[[commitable]]";
                    d.local = "[[" + d.local + "]]";
                    break;
                }
                case "old": {
                    if (!old) {
                        iterator.remove();
                    }
                    d.status = "{{old}}";
                    d.local = "@@" + d.local + "@@";
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
        if (!ddd.isEmpty() || !appContext.session().isPlainOut()) {
            if (appContext.session().isPlainOut()) {
                for (DataRow p2 : ddd) {
                    String status = p2.status;
                    NutsTerminalFormat tf = appContext.workspace().io().terminalFormat();
                    int len = tf.textLength(status);
                    while (len < 10) {
                        status += " ";
                        len++;
                    }
                    switch (tf.filterText(p2.status)) {
                        case "new": {
                            appContext.session().out().printf("\\[%N\\] %s : %N%n", status, p2.id, p2.local);
                            break;
                        }
                        case "commitable": {
                            appContext.session().out().printf("\\[%N\\] %s : %N - %N%n", status, p2.id, p2.local, p2.remote);
                            break;
                        }
                        case "old": {
                            appContext.session().out().printf("\\[%N\\] %s : %N - %N%n", status, p2.id, p2.local, p2.remote);
                            break;
                        }
                        case "invalid": {
                            appContext.session().out().printf("\\[%N\\] %s : %N - %N%n", status, p2.id, p2.local, p2.remote);
                            break;
                        }
                        case "uptodate": {
                            appContext.session().out().printf("\\[%N\\] %s : %N%n", status, p2.id, p2.local);
                            break;
                        }
                        default: {
                            appContext.session().out().printf("\\[%N\\] %s : %N - %N%n", status, p2.id, p2.local, p2.remote);
                            break;
                        }
                    }
                }
            } else {
                appContext.workspace().object()
                        .session(appContext.session())
                        .value(ddd).println();
            }
        }
    }

    private boolean matches(String id, List<String> filters) {
        boolean accept = filters.isEmpty();
        if (!accept) {
            for (String filter : filters) {
                if (id.matches(wildcardToRegex(filter))) {
                    accept = true;
                    break;
                }
            }
        }
        return accept;
    }

    public Path getConfigFile() {
        return appContext.getSharedConfigFolder().resolve("workspace.projects");
    }

    public List<ProjectService> findProjectServices() {
        List<ProjectService> all = new ArrayList<>();
        Path storeLocation = appContext.getSharedConfigFolder().resolve("projects");

        if (Files.isDirectory(storeLocation)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(storeLocation)) {
                for (Path file : ds) {
                    if (Files.isRegularFile(file) && file.getFileName().toString().endsWith(".config")) {
                        try {
                            all.add(new ProjectService(appContext, config.getDefaultRepositoryAddress(), file));
                        } catch (IOException e) {
                            //ignore
                        }
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return all;
    }

    public void scan(File folder, boolean interactive) {
        if (folder.isDirectory()) {
            if (!isScanEnabled(folder)) {
                return;
            }
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
                        if (appContext.session().isPlainOut()) {
                            appContext.session().out().printf("Already registered Project Folder [[%s]] {{%s}}: ==%s==%n", p2.getId(), p2.getTechnologies(), p2.getPath());
                        }
                    } else if (!p2.getPath().equals(p3.getPath())) {
                        if (appContext.session().isPlainOut()) {
                            appContext.session().out().printf("@@[CONFLICT]@@ Multiple paths for the same id [[%s]]. Please consider adding .nuts-info file with " + SCAN + "=false  :  ==%s== -- ==%s==%n", p2.getId(), p2.getPath(), p3.getPath());
                        }
                    } else {
                        if (appContext.session().isPlainOut()) {
                            appContext.session().out().printf("Reloaded Project Folder [[%s]] {{%s}}: ==%s==%n", p2.getId(), p2.getTechnologies(), p2.getPath());
                        }
//                String repo = term.readLine("Enter Repository ==%s==: ", ((p2.getAddress() == null || p2.getAddress().getNutsRepository() == null )? "" : ("(" + p2.getAddress().getNutsRepository() + ")")));
//                if (!StringUtils.isEmpty(repo)) {
//                    p2.setAddress(new );
//                }
                        ProjectService ps = new ProjectService(appContext, null, p2);
                        try {
                            ps.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {

                    if (appContext.session().isPlainOut()) {
                        appContext.session().out().printf("Detected Project Folder [[%s]] {{%s}}: ==%s==%n", p2.getId(), p2.getTechnologies(), p2.getPath());
                    }
                    if (interactive) {
                        String id = appContext.session().terminal().readLine("Enter Id ==%s==: ", (p2.getId() == null ? "" : ("(" + p2.getId() + ")")));
                        if (!StringUtils.isBlank(id)) {
                            p2.setId(id);
                        }
                    }
//                String repo = term.readLine("Enter Repository ==%s==: ", ((p2.getAddress() == null || p2.getAddress().getNutsRepository() == null )? "" : ("(" + p2.getAddress().getNutsRepository() + ")")));
//                if (!StringUtils.isEmpty(repo)) {
//                    p2.setAddress(new );
//                }
                    ProjectService ps = new ProjectService(appContext, null, p2);
                    try {
                        ps.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            File[] aa = folder.listFiles();
            for (File file : aa) {
                if (file.isDirectory()) {
                    scan(file, interactive);
                }
            }
        }
    }

    public void setScanEnabled(Path folder, boolean enable) {
        Path ni = folder.resolve(".nuts-info");
        Properties p = null;
        boolean scan = true;
        if (Files.isRegularFile(ni)) {
            try {
                p = IOUtils.loadProperties(ni);
                String v = p.getProperty(SCAN);
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
            p.setProperty(SCAN, String.valueOf(enable));
            try {
                IOUtils.saveProperties(p, "", ni);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    public boolean isScanEnabled(File folder) {
        boolean scan = true;
        File ni = new File(folder, ".nuts-info");
        Properties p = null;
        if (ni.isFile()) {
            try {
                p = IOUtils.loadProperties(ni);
                String v = p.getProperty(SCAN);
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
                conf.getDefaultRepositoryAddress().setNutsRepository(a.getStringValue());
                setWorkspaceConfig(conf);
            } else if ((a = cmd.nextString("-w", "--workspace")) != null) {
                WorkspaceConfig conf = getWorkspaceConfig();
                conf.getDefaultRepositoryAddress().setNutsWorkspace(a.getStringValue());
                setWorkspaceConfig(conf);
            } else {
                cmd.setCommandName("worky set").unexpectedArgument();
            }
        }
        return 0;
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

    static class DataRow implements Comparable<DataRow> {

        String id;
        String local;
        String remote;
        String status;

        @Override
        public int compareTo(DataRow o) {
            int v = status.compareTo(o.status);
            if (v != 0) {
                return v;
            }
            v = id.compareTo(o.id);
            if (v != 0) {
                return v;
            }
            return 0;
        }
    }
}

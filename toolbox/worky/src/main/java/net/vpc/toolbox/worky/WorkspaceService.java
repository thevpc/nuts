package net.vpc.toolbox.worky;

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.app.ColoredCellFormatter;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.FolderNonOption;
import net.vpc.common.commandline.format.TableFormatter;
import net.vpc.common.io.FileUtils;
import net.vpc.common.io.IOUtils;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.worky.config.ProjectConfig;
import net.vpc.toolbox.worky.config.RepositoryAddress;
import net.vpc.toolbox.worky.config.WorkspaceConfig;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WorkspaceService {
    public static final String NOSCAN = "net.vpc.app.nuts.toolbox.worky.noscan";
    private WorkspaceConfig config;
    private NutsApplicationContext appContext;

    public WorkspaceService(NutsApplicationContext appContext) {
        this.appContext = appContext;
        File c = getConfigFile();
        if (c.isFile()) {
            try {
                config = appContext.getWorkspace().getJsonIO().read(c, WorkspaceConfig.class);
            } catch (Exception ex) {
                //
            }
        }
        if (config == null) {
            config = new WorkspaceConfig();
        }
    }

    public WorkspaceConfig getWorkspaceConfig() {

        RepositoryAddress v=config.getDefaultRepositoryAddress();
        if(v==null) {
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
        File configFile = getConfigFile();
        FileUtils.createParents(configFile);
        appContext.getWorkspace().getJsonIO().write(c, configFile, true);
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


    public int enableScan(CommandLine cmd, NutsApplicationContext appContext, boolean enable) {
        int count = 0;
        while (cmd.hasNext()) {
            if (appContext.configure(cmd)) {
                //consumed
            } else {
                String expression = cmd.read().getExpression();
                if (cmd.isExecMode()) {
                    setNoScan(new File(expression), enable);
                    count++;
                }
            }
        }

        if (count == 0) {
            throw new NutsExecutionException("Missing projects", 1);
        }
        return count > 0 ? 0 : 1;
    }

    public int list(CommandLine cmd, NutsApplicationContext appContext) {
        Argument a;
        TableFormatter tf = new TableFormatter(appContext.getTableCellFormatter()).addHeaderCells("==Id==", "==Path==", "==Technos==");
        List<String> filters=new ArrayList<>();
        while (cmd.hasNext()) {
            if (appContext.configure(cmd)) {
                //consumed
            } else if (tf.configure(cmd)) {
                //consumed
            } else if((a = cmd.readNonOption())!=null) {
                filters.add(a.getString());
            }else{
                cmd.unexpectedArgument("worky list");
            }
        }
        if (cmd.isExecMode()) {
            for (ProjectService projectService : findProjectServices()) {
                if(matches(projectService.getConfig().getId(), filters)) {
                    ProjectConfig config = projectService.getConfig();
                    tf.newRow()
                            .addCells(
                                    config.getId(),
                                    config.getPath(),
                                    config.getTechnologies()
                            )
                    ;
                }
            }

            appContext.out().printf(tf.toString());
        }
        return 0;
    }

    public int scan(CommandLine cmd, NutsApplicationContext appContext) {
        boolean interactive = false;
        Argument a;
        boolean run = false;
        while (cmd.hasNext()) {
            if (appContext.configure(cmd)) {
                //consumed
            } else if ((a = cmd.readBooleanOption("-i", "--interactive")) != null) {
                interactive = a.getBooleanValue();
            } else {
                String folder = cmd.readNonOption(new FolderNonOption("Folder")).getExpression();
                run = true;
                if (cmd.isExecMode()) {
                    scan(new File(folder), interactive);
                }
            }
        }
        if (!run) {
            throw new NutsExecutionException("Missing folders", 1);
        }
        return 0;
    }

    public int check(CommandLine cmd, NutsApplicationContext appContext) {
        boolean progress = true;
        Boolean commitable = null;
        Boolean newP = null;
        Boolean uptodate = null;
        Boolean old = null;
        Boolean invalid = null;
        TableFormatter tf = new TableFormatter(appContext.getTableCellFormatter()).addHeaderCells("==Id==", "==Local==", "==Remote==", "==Status==");
        List<String> filters=new ArrayList<>();
        Argument a;
        while (cmd.hasNext()) {
            if (appContext.configure(cmd)) {
                //consumed
            } else if (tf.configure(cmd)) {
                //consumed
            } else if (cmd.readAll("-c", "--commitable", "--changed")) {
                commitable = true;
            } else if (cmd.readAll("-!c", "--!commitable", "--!changed")) {
                commitable = false;
            } else if (cmd.readAll("-n", "--new")) {
                newP = true;
            } else if (cmd.readAll("-!n", "--!new")) {
                newP = false;
            } else if (cmd.readAll("-o", "--old")) {
                old = true;
            } else if (cmd.readAll("-!o", "--!old")) {
                old = false;
            } else if (cmd.readAll("-0", "--ok", "--uptodate")) {
                uptodate = true;
            } else if (cmd.readAll("-!0", "--!ok", "--!uptodate")) {
                uptodate = false;
            } else if (cmd.readAll("-e", "--invalid", "--error")) {
                invalid = true;
            } else if (cmd.readAll("-!e", "--!invalid", "--!error")) {
                invalid = false;
            } else if (cmd.readAll("-p", "--progress")) {
                progress = true;
            } else if (cmd.readAll("-!p", "--!progress")) {
                progress = false;
            } else if((a = cmd.readNonOption())!=null) {
                filters.add(a.getString());
            } else {
                cmd.unexpectedArgument("worky check");
            }
        }
        if (appContext.isRequiredExit()) {
            return appContext.getExitCode();
        }

        Boolean[] b = new Boolean[]{commitable, newP, uptodate, old, invalid};
        updateBools(b, true);
        commitable = b[0];
        newP = b[1];
        uptodate = b[2];
        old = b[3];
        invalid = b[4];

        class Data implements Comparable<Data> {
            String id;
            String loc;
            String rem;
            String status;

            @Override
            public int compareTo(Data o) {
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
        List<Data> ddd = new ArrayList<>();

        List<ProjectService> all=findProjectServices();
        for (Iterator<ProjectService> iterator = all.iterator(); iterator.hasNext(); ) {
            ProjectService projectService = iterator.next();
            if(!matches(projectService.getConfig().getId(), filters)){
                iterator.remove();
            }
        }

        int maxSize = 1;
        for (int i = 0; i < all.size(); i++) {
            ProjectService projectService = all.get(i);
            Data d = new Data();
            d.id = projectService.getConfig().getId();
            if (progress) {
                maxSize = Math.max(maxSize, projectService.getConfig().getId().length());
                if (i > 0) {
                    appContext.out().printf("`move-line-start;move-up`");
                }
                appContext.out().printf("(%s / %s) %s\n", (i + 1), all.size(), StringUtils.alignLeft(projectService.getConfig().getId(), maxSize));
            }
            d.loc = projectService.detectLocalVersion();
            d.rem = d.loc == null ? null : projectService.detectRemoteVersion();
            if (d.loc == null) {
                d.status = "invalid";
                d.loc = "";
                d.rem = "";
            } else if (d.rem == null) {
                d.rem = "";
                d.status = "new";
            } else {
                int t = appContext.getWorkspace().parseVersion(d.loc).compareTo(d.rem);
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
        for (Iterator<Data> iterator = ddd.iterator(); iterator.hasNext(); ) {
            Data d = iterator.next();
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
                    d.loc = "[[" + d.loc + "]]";
                    break;
                }
                case "commitable": {
                    if (!commitable) {
                        iterator.remove();
                    }
                    d.status = "[[commitable]]";
                    d.loc = "[[" + d.loc + "]]";
                    break;
                }
                case "old": {
                    if (!old) {
                        iterator.remove();
                    }
                    d.status = "{{old}}";
                    d.loc = "@@" + d.loc + "@@";
                    break;
                }
                case "uptodate": {
                    if (!uptodate) {
                        iterator.remove();
                    }
                    break;
                }
            }
            //"%s %s %s\n",projectService.getConfig().getId(),loc,rem
            tf.addRow(d.id, d.loc, d.rem, d.status);
        }
        appContext.out().printf(tf.toString());
        return 0;
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

    public File getConfigFile() {
        return new File(appContext.getConfigFolder(), "workspace.projects");
    }

    public List<ProjectService> findProjectServices() {
        List<ProjectService> all = new ArrayList<>();
        File storeRoot = new File(appContext.getConfigFolder(), "projects");
        File[] projects = storeRoot.listFiles();
        if (projects != null) {
            for (File file : projects) {
                if (file.isFile() && file.getName().endsWith(".config")) {
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

    public void scan(File folder, boolean interactive) {
        if (folder.isDirectory()) {
            if (isNoScan(folder)) {
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

                }
                if (loaded) {
                    ProjectConfig p3 = projectService.getConfig();
                    if (p3.equals(p2)) {
                        //no updates!
                        appContext.out().printf("Already registered Project Folder [[%s]] {{%s}}: ==%s==\n", p2.getId(), p2.getTechnologies(), p2.getPath());
                    } else if (!p2.getPath().equals(p3.getPath())) {
                        appContext.out().printf("@@[CONFLICT]@@ Multiple paths for the same id [[%s]]. Please consider adding .nuts-info file with " + NOSCAN + "=true  :  ==%s== -- ==%s==\n", p2.getId(), p2.getPath(), p3.getPath());
                    } else {
                        appContext.out().printf("Reloaded Project Folder [[%s]] {{%s}}: ==%s==\n", p2.getId(), p2.getTechnologies(), p2.getPath());
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

                    appContext.out().printf("Detected Project Folder [[%s]] {{%s}}: ==%s==\n", p2.getId(), p2.getTechnologies(), p2.getPath());
                    if (interactive) {
                        String id = appContext.getTerminal().readLine("Enter Id ==%s==: ", (p2.getId() == null ? "" : ("(" + p2.getId() + ")")));
                        if (!StringUtils.isEmpty(id)) {
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

    public void setNoScan(File folder, boolean enable) {
        File ni = new File(folder, ".nuts-info");
        Properties p = null;
        boolean noscan = false;
        if (ni.isFile()) {
            try {
                p = IOUtils.loadProperties(ni);
                String v = p.getProperty("net.vpc.app.nuts.toolbox.worky.noscan");
                if (v != null && "true".equals(v.trim())) {
                    noscan = true;
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException(ex);
            }
            //=true
        }
        if (noscan != enable) {
            if (p == null) {
                p = new Properties();
            }
            p.setProperty(NOSCAN, String.valueOf(enable));
            IOUtils.saveProperties(p, "", ni);
        }
    }

    public boolean isNoScan(File folder) {
        File ni = new File(folder, ".nuts-info");
        Properties p = null;
        if (ni.isFile()) {
            try {
                p = IOUtils.loadProperties(ni);
                String v = p.getProperty(NOSCAN);
                if (v != null && "true".equals(v.trim())) {
                    return true;
                }
            } catch (Exception ex) {
                //ignore
            }
            //=true
        }
        return false;
    }

    public int setWorkspaceConfigParam(CommandLine cmd, NutsApplicationContext appContext) {
        Argument a;
        while (cmd.hasNext()) {
            if((a=cmd.readStringOption("-r","--repo"))!=null) {
                WorkspaceConfig conf = getWorkspaceConfig();
                conf.getDefaultRepositoryAddress().setNutsRepository(a.getStringValue());
                setWorkspaceConfig(conf);
            }else if((a=cmd.readStringOption("-w","--workspace"))!=null){
                WorkspaceConfig conf = getWorkspaceConfig();
                conf.getDefaultRepositoryAddress().setNutsWorkspace(a.getStringValue());
                setWorkspaceConfig(conf);
            }else if((a=cmd.readStringOption("-h","--home"))!=null){
                WorkspaceConfig conf = getWorkspaceConfig();
                conf.getDefaultRepositoryAddress().setNutsHome(a.getStringValue());
                setWorkspaceConfig(conf);
            }else{
                cmd.unexpectedArgument("worky set");
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

}

package net.vpc.toolbox.worky;

import net.vpc.app.nuts.*;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.FileNonOption;
import net.vpc.common.commandline.FolderNonOption;
import net.vpc.common.commandline.format.TableFormatter;
import net.vpc.common.io.IOUtils;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.worky.config.ProjectConfig;
import net.vpc.toolbox.worky.config.RepositoryAddress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class WorkyMain extends NutsApplication {

    private RepositoryAddress defaultRepositoryAddress = new RepositoryAddress()
            .setNutsRepository("vpc-public-maven");
    private TableFormatter.CellFormatter formatter;

    public static void main(String[] args) {
        new WorkyMain().launchAndExit(args);
    }

    private NutsWorkspace ws;
    private NutsSession session;

    @Override
    public int launch(String[] args, NutsWorkspace ws) {
        this.ws = ws;
        formatter = new TableFormatter.CellFormatter() {
            @Override
            public int stringLength(String value) {
                return ws.filterText(value).length();
            }

            @Override
            public String format(int row, int col, Object value) {
                return String.valueOf(value);
            }
        };
        session = ws.createSession();
        CommandLine cmd = new CommandLine(args);
        while (!cmd.isEmpty()) {
            if (cmd.read("scan")) {
                while (!cmd.isEmpty()) {
                    scan(new File(cmd.readNonOption(new FolderNonOption("Folder")).getString()));
                }
            } else if (cmd.read("check")) {
                while (!cmd.isEmpty()) {
                    cmd.requireEmpty();
                }
                check(list());
            } else if (cmd.read("list")) {

                TableFormatter tf = new TableFormatter(formatter).addHeaderCells("==Id==", "==Path==", "==Technos==");

                for (ProjectService projectService : list()) {
                    ProjectConfig config = projectService.getConfig();
                    tf.newRow()
                            .addCells(
                                    config.getId(),
                                    config.getPath(),
                                    config.getTechnologies()
                            )
                    ;

                }

                NutsFormattedPrintStream out = session.getTerminal().getFormattedOut();
                out.printf(tf.toString());
            } else {
                cmd.requireEmpty();
            }
        }
        return 0;
    }

    public void check(List<ProjectService> all) {
        TableFormatter tf = new TableFormatter(formatter).addHeaderCells("==Id==", "==Local==", "==Remote==", "==Status==");
        NutsFormattedPrintStream out = session.getTerminal().getFormattedOut();
        class Data implements Comparable<Data>{
            String id;
            String loc;
            String rem;
            String status;

            @Override
            public int compareTo(Data o) {
                int v = status.compareTo(o.status);
                if(v!=0){
                    return v;
                }
                v = id.compareTo(o.id);
                if(v!=0){
                    return v;
                }
                return 0;
            }
        }
        List<Data> ddd=new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            ProjectService projectService = all.get(i);
            Data d = new Data();
            d.id = projectService.getConfig().getId();
            out.printf("`move-line-start;move-up` (%s / %s) %s...\n", (i+1),all.size(), projectService.getConfig().getId());
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
                int t = ws.createVersion(d.loc).compareTo(d.rem);
                if (t > 0) {
                    d.status = "commitable";
                } else if (t < 0) {
                    d.status = "old";
                }else{
                    d.status = "";
                }
            }
            ddd.add(d);
        }
        Collections.sort(ddd);
        for (Data d : ddd) {
            switch (d.status){
                case "invalid":{
                    break;
                }
                case "new":{
                    d.status= "{{new}}";
                    d.loc = "[[" + d.loc + "]]";
                    break;
                }
                case "commitable":{
                    d.status= "[[commitable]]";
                    d.loc = "[[" + d.loc + "]]";
                    break;
                }
                case "old":{
                    d.status="{{old}}";
                    d.loc = "@@" + d.loc + "@@";
                    break;
                }
            }
            //"%s %s %s\n",projectService.getConfig().getId(),loc,rem
            tf.addRow(d.id, d.loc, d.rem, d.status);
        }
        out.printf(tf.toString());
    }

    public List<ProjectService> list() {
        List<ProjectService> all = new ArrayList<>();
        String storeRoot = ws.getStoreRoot("net.vpc.app.nuts.toolbox:worky#CURRENT", RootFolderType.CONFIG);
        for (File file : new File(storeRoot).listFiles()) {
            if (file.isFile() && file.getName().endsWith(".config")) {
                try {
                    all.add(new ProjectService(ws, defaultRepositoryAddress, file));
                } catch (IOException e) {
                    //ignore
                }
            }
        }
        return all;
    }

    public void scan(File folder) {
        if (folder.isDirectory()) {
            File ni = new File(folder, ".nuts-info");
            Properties p=null;
            if (ni.isFile()) {
                try {
                    p = IOUtils.loadProperties(ni);
                    String v = p.getProperty("net.vpc.app.nuts.toolbox.worky.noscan");
                    if (v != null && "true".equals(v.trim())) {
                        return;
                    }
                } catch (Exception ex) {
                    //ignore
                }
                //=true
            }
            ProjectConfig p2 = new ProjectService(ws, defaultRepositoryAddress, new ProjectConfig().setPath(folder.getPath())
            ).rebuildProjectMetadata();
            if (p2.getTechnologies().size() > 0) {
                NutsTerminal term = session.getTerminal();
                NutsFormattedPrintStream out = term.getFormattedOut();
                ProjectService projectService = new ProjectService(ws, defaultRepositoryAddress, p2);
                boolean loaded = false;
                try {
                    loaded = projectService.load();
                } catch (Exception ex) {

                }
                if (loaded) {
                    ProjectConfig p3 = projectService.getConfig();
                    if (p3.equals(p2)) {
                        //no updates!
                        out.printf("Already registered Project Folder [[%s]] {{%s}}: ==%s==\n", p2.getId(), p2.getTechnologies(), p2.getPath());
                    } else if(!p2.getPath().equals(p3.getPath())){
                        out.printf("@@[CONFLICT]@@ Multiple paths for the same id [[%s]]. Please consider adding .nuts-info file with net.vpc.app.nuts.toolbox.worky.noscan=true  :  ==%s== -- ==%s==\n", p2.getId(), p2.getPath(), p3.getPath());
                    } else {
                        out.printf("Reloaded Project Folder [[%s]] {{%s}}: ==%s==\n", p2.getId(), p2.getTechnologies(), p2.getPath());
                        boolean interactive = false;
//                String repo = term.readLine("Enter Repository ==%s==: ", ((p2.getAddress() == null || p2.getAddress().getNutsRepository() == null )? "" : ("(" + p2.getAddress().getNutsRepository() + ")")));
//                if (!StringUtils.isEmpty(repo)) {
//                    p2.setAddress(new );
//                }
                        ProjectService ps = new ProjectService(ws, null, p2);
                        try {
                            ps.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {

                    out.printf("Detected Project Folder [[%s]] {{%s}}: ==%s==\n", p2.getId(), p2.getTechnologies(), p2.getPath());
                    boolean interactive = false;
                    if (interactive) {
                        String id = term.readLine("Enter Id ==%s==: ", (p2.getId() == null ? "" : ("(" + p2.getId() + ")")));
                        if (!StringUtils.isEmpty(id)) {
                            p2.setId(id);
                        }
                    }
//                String repo = term.readLine("Enter Repository ==%s==: ", ((p2.getAddress() == null || p2.getAddress().getNutsRepository() == null )? "" : ("(" + p2.getAddress().getNutsRepository() + ")")));
//                if (!StringUtils.isEmpty(repo)) {
//                    p2.setAddress(new );
//                }
                    ProjectService ps = new ProjectService(ws, null, p2);
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
                    scan(file);
                }
            }
        }
    }
}

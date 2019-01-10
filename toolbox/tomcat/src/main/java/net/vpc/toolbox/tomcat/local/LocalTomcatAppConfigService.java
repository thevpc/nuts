package net.vpc.toolbox.tomcat.local;

import net.vpc.common.io.IOUtils;
import net.vpc.common.io.RuntimeIOException;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.tomcat.local.config.LocalTomcatAppConfig;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.File;
import java.io.PrintStream;

public class LocalTomcatAppConfigService extends LocalTomcatServiceBase{
    private String name;
    private LocalTomcatAppConfig config;
    private LocalTomcatConfigService tomcat;
    private NutsApplicationContext context;

    public LocalTomcatAppConfigService(String name, LocalTomcatAppConfig config, LocalTomcatConfigService tomcat) {
        this.name = name;
        this.config = config;
        this.tomcat = tomcat;
        this.context = tomcat.getTomcatServer().getContext();
    }

    public LocalTomcatAppConfig getConfig() {
        return config;
    }

    public LocalTomcatConfigService getTomcat() {
        return tomcat;
    }

    public File getArchiveFile(String version) {
        String runningFolder = tomcat.getConfig().getArchiveFolder();
        if (runningFolder == null || runningFolder.trim().isEmpty()) {
            runningFolder = new File(context.getVarFolder(), "archive").getPath();
        }
        String packaging = "war";
        return new File(runningFolder + "/" + name + "-" + version + "." + packaging);
    }

    public File getRunningFile() {
        String s = getConfig().getSourceFilePath();
        if(!TomcatUtils.isEmpty(s)){
            return new File(s);
        }
        String runningFolder = tomcat.getConfig().getRunningFolder();
        if (runningFolder == null || runningFolder.trim().isEmpty()) {
            runningFolder = new File(context.getVarFolder(), "running").getPath();
        }
        String packaging = "war";
        return new File(runningFolder + "/" + name + "." + packaging);
    }

    public File getVersionFile() {
        return new File(new File(context.getConfigFolder()), name + ".version");
    }

    public String getCurrentVersion() {
        if (getVersionFile().exists()) {
            return IOUtils.loadString(getVersionFile());
        }
        return null;
    }

    public LocalTomcatAppConfigService setCurrentVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            context.out().printf("==[%s]== unset version.\n",getFullName());
            if(getVersionFile().delete()){
                context.out().printf("==[%s]== [LOG] delete version file [[%s]].\n",getFullName(),getVersionFile());
            }
            if(getRunningFile().delete()){
                context.out().printf("==[%s]== [LOG] delete running file [[%s]].\n",getFullName(),getRunningFile());
            }
        } else {
            context.out().printf("==[%s]== set version [[%s]].\n",getFullName(),version);
            context.out().printf("==[%s]== [LOG] updating version file [[%s]] to [[%s]].\n",getFullName(),StringUtils.coalesce(version,"<DEFAULT>"),getVersionFile());
            IOUtils.saveString(version, getVersionFile());
            context.out().printf("==[%s]== [LOG] updating archive file [[%s]] -> [[%s]].\n",getFullName(),getArchiveFile(version), getRunningFile());
            IOUtils.copy(getArchiveFile(version), getRunningFile());
        }
        return this;
    }

    public File getDeployFile() {
        LocalTomcatDomainConfigService d = tomcat.getDomainOrCreate(getConfig().getDomain());
        String deployName = getConfig().getDeployName();
        if (TomcatUtils.isEmpty(deployName)) {
            deployName = name + ".war";
        }
        if (!deployName.endsWith(".war")) {
            deployName += ".war";
        }
        return new File(d.getDomainDeployPath(), deployName);
    }

    public File getDeployFolder() {
        File f = getDeployFile();
        return new File(f.getParentFile(), f.getName().substring(0, f.getName().length() - ".war".length()));
    }

    public LocalTomcatAppConfigService resetDeployment() {
        File deployFile = getDeployFile();
        File deployFolder = getDeployFolder();
        context.out().printf("==[%s]== reset deployment (delete [[%s]] ).\n",getFullName(),deployFile);
        IOUtils.delete(deployFile);
        IOUtils.delete(deployFolder);
        return this;
    }

    public LocalTomcatAppConfigService deploy(String version) {
        if(TomcatUtils.isEmpty(version)){
            version=getCurrentVersion();
        }
        File runningFile = getRunningFile();
        File deployFile = getDeployFile();
        context.out().printf("==[%s]== deploy [[%s]] as file [[%s]] to [[%s]].\n",getFullName(),StringUtils.coalesce(version,"<DEFAULT>"),runningFile,deployFile);
        IOUtils.copy(runningFile, deployFile);
        return this;
    }

    public LocalTomcatAppConfigService install(String version, String file, boolean setVersion) {
        File f = new File(file);
        if (!f.isFile()) {
            throw new RuntimeIOException("File not found " + f.getPath());
        }
        if(StringUtils.isEmpty(version)){
            version=getCurrentVersion();
        }
        File domainDeployPath = getArchiveFile(version);
        domainDeployPath.getParentFile().mkdirs();
        context.out().printf("==[%s]== install version [[%s]] : [[%s]]->[[%s]].\n",getFullName(),version,f,domainDeployPath);
        IOUtils.copy(f, domainDeployPath);
        if (setVersion) {
            setCurrentVersion(version);
        }
        return this;
    }


//
//    public void deploy(String configName, String appName, String version, String file) {
//        LocalTomcatConfig c = loadConfig(configName);
//        if (c.getApps().containsKey(appName)) {
//            LocalTomcatAppConfig a = getTomcatApp(c, appName);
//            c.getRunningFolder()
//            String domainDeployPath = getDomainDeployPath(configName, a.getDomain());
//            Files.copy(ws.getStoreLocation())
//        }
//    }

    public LocalTomcatAppConfigService remove() {
        tomcat.getConfig().getApps().remove(name);
        context.out().printf("==[%s]== app removed.\n",getFullName());
        return this;
    }
    public String getFullName(){
        return tomcat.getName()+"/"+getName();
    }

    public String getName() {
        return name;
    }

    public LocalTomcatAppConfigService write(PrintStream out) {
        TomcatUtils.writeJson(out, getConfig(), context.getWorkspace());
        return this;
    }


}

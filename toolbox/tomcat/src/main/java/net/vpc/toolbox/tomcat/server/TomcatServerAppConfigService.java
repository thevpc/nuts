package net.vpc.toolbox.tomcat.server;

import net.vpc.common.io.IOUtils;
import net.vpc.common.io.RuntimeIOException;
import net.vpc.toolbox.tomcat.server.config.TomcatServerAppConfig;
import net.vpc.toolbox.tomcat.util.NutsContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.File;
import java.io.PrintStream;

public class TomcatServerAppConfigService {
    private String name;
    private TomcatServerAppConfig config;
    private TomcatServerConfigService tomcat;
    private NutsContext context;

    public TomcatServerAppConfigService(String name, TomcatServerAppConfig config, TomcatServerConfigService tomcat) {
        this.name = name;
        this.config = config;
        this.tomcat = tomcat;
        this.context = tomcat.app.context;
    }

    public TomcatServerAppConfig getConfig() {
        return config;
    }

    public TomcatServerConfigService getTomcat() {
        return tomcat;
    }

    public File getArchiveFile(String version) {
        String runningFolder = tomcat.getConfig().getArchiveFolder();
        if (runningFolder == null || runningFolder.trim().isEmpty()) {
            runningFolder = new File(context.varFolder, "archive").getPath();
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
            runningFolder = new File(context.varFolder, "running").getPath();
        }
        String packaging = "war";
        return new File(runningFolder + "/" + name + "." + packaging);
    }

    public File getVersionFile() {
        return new File(new File(context.configFolder), name + ".version");
    }

    public String getCurrentVersion() {
        if (getVersionFile().exists()) {
            return IOUtils.loadString(getVersionFile());
        }
        return null;
    }

    public TomcatServerAppConfigService setCurrentVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            getVersionFile().delete();
            getRunningFile().delete();
            context.out.printf("==[%s]== unset version.\n",getFullName());
        } else {
            IOUtils.saveString(version, getVersionFile());
            IOUtils.copy(getArchiveFile(version), getRunningFile());
            context.out.printf("==[%s]== set version [[%s]].\n",getFullName(),version);
        }
        return this;
    }

    public File getDeployFile() {
        TomcatServerDomainConfigService d = tomcat.getDomainOrCreate(getConfig().getDomain());
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

    public TomcatServerAppConfigService resetDeployment() {
        File deployFile = getDeployFile();
        File deployFolder = getDeployFolder();
        context.out.printf("==[%s]== reset deployment (delete [[%s]] ).\n",getFullName(),deployFile);
        IOUtils.delete(deployFile);
        IOUtils.delete(deployFolder);
        return this;
    }

    public TomcatServerAppConfigService deploy(String version) {
        if(TomcatUtils.isEmpty(version)){
            version=getCurrentVersion();
        }
        File runningFile = getRunningFile();
        File deployFile = getDeployFile();
        context.out.printf("==[%s]== deploy [[%s]] as file [[%s]] to [[%s]].\n",getFullName(),version,runningFile,deployFile);
        IOUtils.copy(runningFile, deployFile);
        return this;
    }

    public TomcatServerAppConfigService install(String version, String file, boolean setVersion) {
        File f = new File(file);
        if (!f.isFile()) {
            throw new RuntimeIOException("File not found " + f.getPath());
        }
        File domainDeployPath = getArchiveFile(version);
        domainDeployPath.getParentFile().mkdirs();
        context.out.printf("==[%s]== install version [[%s]] as [[%s]].\n",getFullName(),version,domainDeployPath);
        IOUtils.copy(f, domainDeployPath);
        if (setVersion) {
            setCurrentVersion(version);
        }
        return this;
    }


//
//    public void deploy(String configName, String appName, String version, String file) {
//        TomcatServerConfig c = loadConfig(configName);
//        if (c.getApps().containsKey(appName)) {
//            TomcatServerAppConfig a = getTomcatApp(c, appName);
//            c.getRunningFolder()
//            String domainDeployPath = getDomainDeployPath(configName, a.getDomain());
//            Files.copy(ws.getStoreRoot())
//        }
//    }

    public TomcatServerAppConfigService remove() {
        tomcat.getConfig().getApps().remove(name);
        context.out.printf("==[%s]== app removed.\n",getFullName());
        return this;
    }
    public String getFullName(){
        return tomcat.getName()+"/"+getName();
    }

    public String getName() {
        return name;
    }

    public TomcatServerAppConfigService write(PrintStream out) {
        TomcatUtils.writeJson(out, getConfig(), context.ws);
        return this;
    }


}

package net.vpc.toolbox.tomcat.local;

import net.vpc.common.io.FileUtils;
import net.vpc.toolbox.tomcat.local.config.LocalTomcatDomainConfig;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.File;
import java.io.PrintStream;

public class LocalTomcatDomainConfigService extends LocalTomcatServiceBase{
    private String name;
    private LocalTomcatDomainConfig config;
    private LocalTomcatConfigService tomcat;
    private NutsApplicationContext context;

    public LocalTomcatDomainConfigService(String name, LocalTomcatDomainConfig config, LocalTomcatConfigService tomcat) {
        this.config = config;
        this.tomcat = tomcat;
        this.name = name;
        this.context = tomcat.getTomcatServer().getContext();
    }

    public LocalTomcatDomainConfig getConfig() {
        return config;
    }

    public LocalTomcatConfigService getTomcat() {
        return tomcat;
    }

    public String getName() {
        return name;
    }

    public String getDomainDeployPath() {
        String b = tomcat.getCatalinaBase();
        if(TomcatUtils.isEmpty(b)){
            b=tomcat.getCatalinaHome();
        }
        String p = config.getDeployPath();
        if(TomcatUtils.isEmpty(p)){
            p=tomcat.getDefaulDeployFolder(name);
        }
        return FileUtils.getAbsolutePath(new File(b), p);
    }

    public LocalTomcatDomainConfigService remove() {
        tomcat.getConfig().getDomains().remove(name);
        for (LocalTomcatAppConfigService aa : tomcat.getApps()) {
            if (name.equals(aa.getConfig().getDomain())) {
                aa.remove();
            }
        }
        context.out().printf("==[%s]== domain removed.\n",name);
        return this;
    }

    public LocalTomcatDomainConfigService write(PrintStream out) {
        TomcatUtils.writeJson(out, getConfig(), context.getWorkspace());
        return this;
    }

}

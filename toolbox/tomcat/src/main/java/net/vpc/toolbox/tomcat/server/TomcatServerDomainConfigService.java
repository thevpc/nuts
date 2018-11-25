package net.vpc.toolbox.tomcat.server;

import net.vpc.common.io.FileUtils;
import net.vpc.toolbox.tomcat.server.config.TomcatServerDomainConfig;
import net.vpc.toolbox.tomcat.util.NutsContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.File;
import java.io.PrintStream;

public class TomcatServerDomainConfigService {
    private String name;
    private TomcatServerDomainConfig config;
    private TomcatServerConfigService tomcat;
    private NutsContext context;

    public TomcatServerDomainConfigService(String name, TomcatServerDomainConfig config, TomcatServerConfigService tomcat) {
        this.config = config;
        this.tomcat = tomcat;
        this.name = name;
        this.context = tomcat.getTomcatServer().getContext();
    }

    public TomcatServerDomainConfig getConfig() {
        return config;
    }

    public TomcatServerConfigService getTomcat() {
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

    public TomcatServerDomainConfigService remove() {
        tomcat.getConfig().getDomains().remove(name);
        context.out.printf("==[%s]== domain removed.\n",name);
        return this;
    }

    public TomcatServerDomainConfigService write(PrintStream out) {
        TomcatUtils.writeJson(out, getConfig(), context.ws);
        return this;
    }

}

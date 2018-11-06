package net.vpc.toolbox.tomcat.server;

import net.vpc.common.io.FileUtils;
import net.vpc.toolbox.tomcat.util.NutsContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;
import net.vpc.toolbox.tomcat.server.config.TomcatServerDomainConfig;

import java.io.File;

public class TomcatServerDomainConfigService {
    private String name;
    private TomcatServerDomainConfig config;
    private TomcatServerConfigService tomcat;
    private NutsContext context;

    public TomcatServerDomainConfigService(String name, TomcatServerDomainConfig config, TomcatServerConfigService tomcat) {
        this.config = config;
        this.tomcat = tomcat;
        this.name = name;
        this.context = tomcat.app.context;
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
            p="webapps";
            if(!name.equals("")){
                p+=("/"+name);
            }
        }
        return FileUtils.getAbsolutePath(new File(b), p);
    }

    public TomcatServerDomainConfigService remove() {
        tomcat.getConfig().getDomains().remove(name);
        context.out.printf("==[%s]== domain removed.\n",name);
        return this;
    }
}

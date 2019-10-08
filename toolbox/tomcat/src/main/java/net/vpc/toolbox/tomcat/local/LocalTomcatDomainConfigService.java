package net.vpc.toolbox.tomcat.local;

import net.vpc.toolbox.tomcat.local.config.LocalTomcatDomainConfig;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.vpc.app.nuts.NutsApplicationContext;

public class LocalTomcatDomainConfigService extends LocalTomcatServiceBase {

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

    public Path getDomainDeployPath() {
        Path b = tomcat.getCatalinaBase();
        if (b == null) {
            b = tomcat.getCatalinaHome();
        }
        Path p = config.getDeployPath()==null ?null:Paths.get(config.getDeployPath());
        if (p == null) {
            p = tomcat.getDefaulDeployFolder(name);
        }
        return b.resolve(b);
    }

    public LocalTomcatDomainConfigService remove() {
        tomcat.getConfig().getDomains().remove(name);
        for (LocalTomcatAppConfigService aa : tomcat.getApps()) {
            if (name.equals(aa.getConfig().getDomain())) {
                aa.remove();
            }
        }
        context.session().out().printf("==[%s]== domain removed.\n", name);
        return this;
    }

    public LocalTomcatDomainConfigService print(PrintStream out) {
        context.workspace().json().value(getConfig()).print(out);
        return this;
    }

}

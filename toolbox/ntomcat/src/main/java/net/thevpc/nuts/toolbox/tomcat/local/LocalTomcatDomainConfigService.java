package net.thevpc.nuts.toolbox.tomcat.local;

import net.thevpc.nuts.NutsContentType;
import net.thevpc.nuts.NutsString;
import net.thevpc.nuts.NutsTextStyle;
import net.thevpc.nuts.toolbox.tomcat.local.config.LocalTomcatDomainConfig;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.thevpc.nuts.NutsApplicationContext;

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
        context.getSession().out().printf("%s domain removed.\n", getBracketsPrefix(name));
        return this;
    }
    public NutsString getBracketsPrefix(String str) {
        return context.getWorkspace().formats().text().builder()
                .append("[")
                .append(str, NutsTextStyle.primary(5))
                .append("]");
    }

    public LocalTomcatDomainConfigService print(PrintStream out) {
        context.getWorkspace().formats().element().setContentType(NutsContentType.JSON).setValue(getConfig()).print(out);
        return this;
    }

}

package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.toolbox.ntomcat.local.config.LocalTomcatDomainConfig;

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

    public NutsPath getDomainDeployPath() {
        NutsPath b = tomcat.getCatalinaBase();
        if (b == null) {
            b = tomcat.getCatalinaHome();
        }
        NutsPath p = config.getDeployPath()==null ?null:NutsPath.of(config.getDeployPath(), getSession());
        if (p == null) {
            p = tomcat.getDefaulDeployFolder(name);
        }
        return b.resolve(b);
    }

    private NutsSession getSession() {
        return context.getSession();
    }

    public LocalTomcatDomainConfigService remove() {
        tomcat.getConfig().getDomains().remove(name);
        for (LocalTomcatAppConfigService aa : tomcat.getApps()) {
            if (name.equals(aa.getConfig().getDomain())) {
                aa.remove();
            }
        }
        getSession().out().printf("%s domain removed.\n", getBracketsPrefix(name));
        return this;
    }
    public NutsString getBracketsPrefix(String str) {
        return NutsTexts.of(getSession()).ofBuilder()
                .append("[")
                .append(str, NutsTextStyle.primary5())
                .append("]");
    }

    public LocalTomcatDomainConfigService print(NutsPrintStream out) {
        NutsSession session = getSession();
        NutsElements.of(session).json().setValue(getConfig()).print(out);
        return this;
    }

}

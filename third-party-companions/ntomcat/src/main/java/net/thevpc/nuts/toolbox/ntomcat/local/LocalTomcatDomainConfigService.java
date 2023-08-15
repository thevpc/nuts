package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.ntomcat.local.config.LocalTomcatDomainConfig;
import net.thevpc.nuts.util.NMsg;

public class LocalTomcatDomainConfigService extends LocalTomcatServiceBase {

    private String name;
    private LocalTomcatDomainConfig config;
    private LocalTomcatConfigService tomcat;
    private NSession session;

    public LocalTomcatDomainConfigService(String name, LocalTomcatDomainConfig config, LocalTomcatConfigService tomcat) {
        this.config = config;
        this.tomcat = tomcat;
        this.name = name;
        this.session = tomcat.getTomcatServer().getSession();
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

    public NPath getDomainDeployPath() {
        NPath b = tomcat.getCatalinaBase();
        if (b == null) {
            b = tomcat.getCatalinaHome();
        }
        NPath p = config.getDeployPath()==null ?null: NPath.of(config.getDeployPath(), getSession());
        if (p == null) {
            p = tomcat.getDefaulDeployFolder(name);
        }
        return b.resolve(b);
    }

    private NSession getSession() {
        return session;
    }

    public LocalTomcatDomainConfigService remove() {
        tomcat.getConfig().getDomains().remove(name);
        for (LocalTomcatAppConfigService aa : tomcat.getApps()) {
            if (name.equals(aa.getConfig().getDomain())) {
                aa.remove();
            }
        }
        getSession().out().println(NMsg.ofC("%s domain removed.", getBracketsPrefix(name)));
        return this;
    }
    public NString getBracketsPrefix(String str) {
        return NTexts.of(getSession()).ofBuilder()
                .append("[")
                .append(str, NTextStyle.primary5())
                .append("]");
    }

    public LocalTomcatDomainConfigService print(NPrintStream out) {
        NSession session = getSession();
        NElements.of(session).json().setValue(getConfig()).print(out);
        return this;
    }

}

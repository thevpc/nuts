package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NSessionProvider;

public class DefaultNCmdLineContext implements NCmdLineContext {
    private Object source;

    public DefaultNCmdLineContext(Object source) {
        this.source = source;
    }

    @Override
    public Object getSource() {
        return source;
    }

    public NApplicationContext getApplicationContext() {
        if (source instanceof NApplicationContext) {
            return (NApplicationContext) source;
        }
        if (source instanceof NSession) {
            return ((NSession) source).getApplicationContext();
        }
        if (source instanceof NSessionProvider) {
            return ((NSessionProvider) source).getSession().getApplicationContext();
        }
        return null;
    }

    public NSession getSession() {
        if (source instanceof NSessionProvider) {
            return ((NSessionProvider) source).getSession();
        }
        if (source instanceof NSession) {
            return ((NSession) source);
        }
        return null;
    }

    public NCmdLineConfigurable getConfigurable() {
        if (source instanceof NCmdLineConfigurable) {
            return ((NCmdLineConfigurable) source);
        }
        if (source instanceof NSessionProvider) {
            return ((NSessionProvider) source).getSession();
        }
        return null;
    }
}

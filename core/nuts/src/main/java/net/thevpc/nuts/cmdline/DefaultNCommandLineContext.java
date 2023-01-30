package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NSessionProvider;

public class DefaultNCommandLineContext implements NCommandLineContext {
    private Object source;

    public DefaultNCommandLineContext(Object source) {
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

    public NCommandLineConfigurable getConfigurable() {
        if (source instanceof NCommandLineConfigurable) {
            return ((NCommandLineConfigurable) source);
        }
        if (source instanceof NSessionProvider) {
            return ((NSessionProvider) source).getSession();
        }
        return null;
    }
}

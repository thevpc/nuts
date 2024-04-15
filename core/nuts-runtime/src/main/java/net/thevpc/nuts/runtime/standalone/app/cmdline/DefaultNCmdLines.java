package net.thevpc.nuts.runtime.standalone.app.cmdline;

import net.thevpc.nuts.*;

import net.thevpc.nuts.cmdline.DefaultNCmdLine;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLines;
import net.thevpc.nuts.runtime.standalone.app.cmdline.option.*;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.shell.NShellHelper;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class DefaultNCmdLines implements NCmdLines {

    private NWorkspace ws;
    private NSession session;
    private NShellFamily family = NShellFamily.getCurrent();

    public DefaultNCmdLines(NSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    public NSession getSession() {
        return session;
    }

    public NCmdLines setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    public NShellFamily getShellFamily() {
        return family;
    }

    public NCmdLines setShellFamily(NShellFamily family) {
        this.family = family == null ? NShellFamily.getCurrent() : family;
        return this;
    }

    public NWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public NCmdLine parseCmdLine(String line) {
        checkSession();
        return new DefaultNCmdLine(parseCmdLineArr(line)).setSession(getSession());
    }

    private String[] parseCmdLineArr(String line) {
        NShellFamily f = getShellFamily();
        if (f == null) {
            f = NEnvs.of(session).getShellFamily();
        }
        if (f == null) {
            f = NShellFamily.getCurrent();
        }
        return NShellHelper.of(f).parseCmdLineArr(line, session);
    }

    protected void checkSession() {
        NSessionUtils.checkSession(ws, session);
    }

    @Override
    public NArgName createName(String type, String label) {
        checkSession();
        return Factory.createName0(getSession(), type, label);
    }

    @Override
    public NArgName createName(String type) {
        checkSession();
        return createName(type, type);
    }

    public static class Factory {

        public static NArgName createName0(NSession session, String type, String label) {
            if (type == null) {
                type = "";
            }
            if (label == null) {
                label = type;
            }
            switch (type) {
                case "arch": {
                    return new ArchitectureNonOption(label);
                }
                case "packaging": {
                    return new PackagingNonOption(label);
                }
                case "extension": {
                    return new ExtensionNonOption(type, session);
                }
                case "file": {
                    return new FileNonOption(type);
                }
                case "boolean": {
                    return new ValueNonOption(type, "true", "false");
                }
                case "repository": {
                    return new RepositoryNonOption(label);
                }
                case "repository-type": {
                    return new RepositoryTypeNonOption(label);
                }
                case "right": {
                    return new PermissionNonOption(label, null, false);
                }
                case "user": {
                    return new UserNonOption(label);
                }
                case "group": {
                    return new GroupNonOption(label);
                }
                default: {
                    return new DefaultNonOption(label);
                }
            }
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}

package net.thevpc.nuts.runtime.standalone.app.cmdline;

import net.thevpc.nuts.*;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.DefaultNCmdLine;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLines;

import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.runtime.standalone.app.cmdline.option.*;
import net.thevpc.nuts.runtime.standalone.xtra.shell.NShellHelper;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class DefaultNCmdLines implements NCmdLines {

    private NShellFamily family = NShellFamily.getCurrent();

    public DefaultNCmdLines() {
    }

    public NShellFamily getShellFamily() {
        return family;
    }

    public NCmdLines setShellFamily(NShellFamily family) {
        this.family = family == null ? NShellFamily.getCurrent() : family;
        return this;
    }

    @Override
    public NCmdLine parseCmdLine(String line) {
        return new DefaultNCmdLine(parseCmdLineArr(line));
    }

    private String[] parseCmdLineArr(String line) {
        NShellFamily f = getShellFamily();
        if (f == null) {
            f = NWorkspace.of().getShellFamily();
        }
        if (f == null) {
            f = NShellFamily.getCurrent();
        }
        return NShellHelper.of(f).parseCmdLineArr(line);
    }

    @Override
    public NArgName createName(String type, String label) {
        return Factory.createName0(type, label);
    }

    @Override
    public NArgName createName(String type) {
        return createName(type, type);
    }

    public static class Factory {

        public static NArgName createName0(String type, String label) {
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
                    return new ExtensionNonOption(type);
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

package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.core.NExceptionWithExitCodeBase;
import net.thevpc.nuts.boot.core.NWorkspaceBase;
import net.thevpc.nuts.boot.internal.util.NBootUtils;

import java.util.Arrays;

public interface NBootWorkspace {
    String NUTS_BOOT_VERSION = "1.0.0";

    static NBootWorkspace of(String[] args) {
        return of(NBootArguments.ofFullArgs(args));
    }

    static NBootWorkspace of(NBootOptionsInfo options) {
        return new NBootWorkspaceImpl(options);
    }

    static NBootWorkspace of(NBootArguments userOptionsUnparsed) {
        if (userOptionsUnparsed == null) {
            userOptionsUnparsed = new NBootArguments();
        }
        if (userOptionsUnparsed.optionArgs() != null && userOptionsUnparsed.optionArgs().length > 0 && userOptionsUnparsed.optionArgs()[0].equals(NBootWorkspaceNativeExec.COMMAND_PREFIX)) {
            userOptionsUnparsed.optionArgs(Arrays.copyOfRange(userOptionsUnparsed.optionArgs(), 1, userOptionsUnparsed.optionArgs().length));
            return new NBootWorkspaceNativeExec(userOptionsUnparsed);
        }
        return new NBootWorkspaceImpl(userOptionsUnparsed);
    }

    static int exitOnError(Throwable th) {
        if (th != null) {
            NExceptionWithExitCodeBase ec = NBootUtils.findThrowable(th, NExceptionWithExitCodeBase.class, null);
            int c = ec == null ? 254 : ec.exitCode();
            if (c != 0) {
                System.exit(c);
            }
            return c;
        }
        return 0;
    }

    NBootArguments getBootArguments();

    NBootOptionsInfo getOptions();

    NWorkspaceBase getWorkspace();

    NBootWorkspace runWorkspace();

}

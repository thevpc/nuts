package net.thevpc.nuts.boot;

import net.thevpc.nuts.NWorkspaceBase;

import java.util.Arrays;

public interface NBootWorkspace {
    String NUTS_BOOT_VERSION = "0.8.5";

    static NBootWorkspace of(NBootArguments userOptionsUnparsed) {
        if (userOptionsUnparsed == null) {
            userOptionsUnparsed = new NBootArguments();
        }
        if (userOptionsUnparsed.getArgs() != null && userOptionsUnparsed.getArgs().length > 0 && userOptionsUnparsed.getArgs()[0].equals(NBootWorkspaceNativeExec.COMMAND_PREFIX)) {
            userOptionsUnparsed.setArgs(Arrays.copyOfRange(userOptionsUnparsed.getArgs(), 1, userOptionsUnparsed.getArgs().length));
            return new NBootWorkspaceNativeExec(userOptionsUnparsed);
        }
        return new NBootWorkspaceImpl(userOptionsUnparsed);
    }

    NWorkspaceBase openWorkspace();

    NWorkspaceBase runWorkspace();
}

package net.thevpc.nuts.boot;

import net.thevpc.nuts.NExceptionWithExitCodeBase;
import net.thevpc.nuts.NWorkspaceBase;
import net.thevpc.nuts.boot.reserved.util.NBootUtils;

import java.time.Instant;
import java.util.Arrays;

public interface NBootWorkspace {
    String NUTS_BOOT_VERSION = "0.8.5";

    static NBootWorkspace of(String[] args) {
        Instant startTime = Instant.now();
        NBootArguments options = new NBootArguments();
        options.setArgs(args);
        options.setStartTime(startTime);
        return of(options);
    }

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

    static int exitOnError(Throwable th) {
        if(th!=null){
            NExceptionWithExitCodeBase ec = NBootUtils.findThrowable(th, NExceptionWithExitCodeBase.class, null);
            int c = ec==null?254:ec.getExitCode();
            if(c!=0){
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

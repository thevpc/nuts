package net.thevpc.nuts;

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.env.NOsServiceType;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;

import java.util.Map;

public interface NInstallSvcCmd extends NCmdLineConfigurable, NComponent, NSessionProvider{
    static NInstallSvcCmd of(NSession session) {
        return NExtensions.of(session).createComponent(NInstallSvcCmd.class).get();
    }

    String[] getStartCommand();

    NInstallSvcCmd setStartCommand(String[] startCommand);

    String[] getStopCommand();

    NInstallSvcCmd setStopCommand(String[] stopCommand);

    String[] getStatusCommand();

    NInstallSvcCmd setStatusCommand(String[] statusCommand);

    boolean uninstall();

    boolean install();

    Map<String, String> getEnv();

    NInstallSvcCmd setEnv(Map<String, String> env);

    NInstallSvcCmd setServiceName(String serviceName);

    NInstallSvcCmd setControlCommand(String[] controlCommand);

    NInstallSvcCmd setServiceType(NOsServiceType nOsServiceType);

    NInstallSvcCmd setRootDirectory(NPath rootDirectory);

    NPath getWorkingDirectory();

    NInstallSvcCmd setWorkingDirectory(NPath dir);

    NOsServiceType getSystemServiceType();
}

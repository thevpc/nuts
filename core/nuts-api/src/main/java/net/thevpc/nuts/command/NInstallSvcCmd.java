package net.thevpc.nuts.command;

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.platform.NOsServiceType;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.util.Map;

public interface NInstallSvcCmd extends NCmdLineConfigurable, NComponent{
    static NInstallSvcCmd of() {
        return NExtensions.of(NInstallSvcCmd.class);
    }

    @NGetter
    String[] startCommand();

    @NSetter
    NInstallSvcCmd startCommand(String[] startCommand);

    @NGetter
    String[] stopCommand();

    @NSetter
    NInstallSvcCmd stopCommand(String[] stopCommand);

    @NGetter
    String[] statusCommand();

    @NSetter
    NInstallSvcCmd statusCommand(String[] statusCommand);

    @NGetter
    boolean uninstall();

    @NGetter
    boolean install();

    @NGetter
    Map<String, String> env();

    @NSetter
    NInstallSvcCmd env(Map<String, String> env);

    @NSetter
    NInstallSvcCmd serviceName(String serviceName);

    @NSetter
    NInstallSvcCmd controlCommand(String[] controlCommand);

    @NSetter
    NInstallSvcCmd serviceType(NOsServiceType nOsServiceType);

    @NSetter
    NInstallSvcCmd rootDirectory(NPath rootDirectory);

    @NGetter
    NPath workingDirectory();

    @NSetter
    NInstallSvcCmd workingDirectory(NPath dir);

    @NGetter
    NOsServiceType systemServiceType();
}

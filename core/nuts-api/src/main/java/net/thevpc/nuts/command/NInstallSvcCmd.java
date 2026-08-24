package net.thevpc.nuts.command;

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.platform.NOsServiceType;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.util.Map;

/**
 * NInstallSvcCmd interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NInstallSvcCmd extends NCmdLineConfigurable, NComponent{
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NInstallSvcCmd of() {
        return NExtensions.of(NInstallSvcCmd.class);
    }

    /**
     * Start command.
     *
     * @return start command result
     */
    @NGetter
    String[] startCommand();

    /**
     * Start command.
     *
     * @param startCommand start command
     * @return start command result
     */
    @NSetter
    NInstallSvcCmd startCommand(String[] startCommand);

    /**
     * Stop command.
     *
     * @return stop command result
     */
    @NGetter
    String[] stopCommand();

    /**
     * Stop command.
     *
     * @param stopCommand stop command
     * @return stop command result
     */
    @NSetter
    NInstallSvcCmd stopCommand(String[] stopCommand);

    /**
     * Status command.
     *
     * @return status command result
     */
    @NGetter
    String[] statusCommand();

    /**
     * Status command.
     *
     * @param statusCommand status command
     * @return status command result
     */
    @NSetter
    NInstallSvcCmd statusCommand(String[] statusCommand);

    /**
     * Uninstall.
     *
     * @return uninstall result
     */
    @NGetter
    boolean uninstall();

    /**
     * Install.
     *
     * @return install result
     */
    @NGetter
    boolean install();

    /**
     * Env.
     *
     * @return env result
     */
    @NGetter
    Map<String, String> env();

    /**
     * Env.
     *
     * @param env env
     * @return env result
     */
    @NSetter
    NInstallSvcCmd env(Map<String, String> env);

    /**
     * Service name.
     *
     * @param serviceName service name
     * @return service name result
     */
    @NSetter
    NInstallSvcCmd serviceName(String serviceName);

    /**
     * Control command.
     *
     * @param controlCommand control command
     * @return control command result
     */
    @NSetter
    NInstallSvcCmd controlCommand(String[] controlCommand);

    /**
     * Service type.
     *
     * @param nOsServiceType n os service type
     * @return service type result
     */
    @NSetter
    NInstallSvcCmd serviceType(NOsServiceType nOsServiceType);

    /**
     * Root directory.
     *
     * @param rootDirectory root directory
     * @return root directory result
     */
    @NSetter
    NInstallSvcCmd rootDirectory(NPath rootDirectory);

    /**
     * Working directory.
     *
     * @return working directory result
     */
    @NGetter
    NPath workingDirectory();

    /**
     * Working directory.
     *
     * @param dir dir
     * @return working directory result
     */
    @NSetter
    NInstallSvcCmd workingDirectory(NPath dir);

    /**
     * System service type.
     *
     * @return system service type result
     */
    @NGetter
    NOsServiceType systemServiceType();
}

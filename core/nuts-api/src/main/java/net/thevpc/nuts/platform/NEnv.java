package net.thevpc.nuts.platform;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSupportMode;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NEnv interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NEnv extends NComponent {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NEnv of() {
        return NExtensions.of(NEnv.class);
    }

    /**
     * Creates a new instance of of.
     *
     * @param connectionString connection string
     * @return of result
     */
    static NEnv of(NConnectionString connectionString) {
        if (NBlankable.isBlank(connectionString) || NBlankable.isBlank(connectionString.host())) {
            /**
             * Creates a new instance of of.
             *
             * @return of result
             */
            return of();
        }

        NConnectionStringBuilder connectionStringBuilder = connectionString.builder()
                //remove 'path' query param because target is independent of path
                .path(null);
        NConnectionString normalizedConnectionStringWithUse = connectionString.normalize();

        NConnectionString normalizedConnectionStringWithoutUse = connectionStringBuilder
                //remove 'use' query param because target is independent of transport
                .setQueryParam("use", null)
                .build();


        Map<NConnectionString, NEnv> cache = NWorkspace.of().getOrComputeProperty(NEnv.class + "::Cache", () -> (Map<NConnectionString, NEnv>) new ConcurrentHashMap<NConnectionString, NEnv>());
        return cache.computeIfAbsent(normalizedConnectionStringWithoutUse, x -> NExtensions.of().createSupported(NEnv.class, normalizedConnectionStringWithUse).get());
    }

    /**
     * Creates a new instance of of.
     *
     * @param connectionString connection string
     * @return of result
     */
    static NEnv of(String connectionString) {
        if (NBlankable.isBlank(connectionString)) {
            return NEnv.of();
        }
        /**
         * Creates a new instance of of.
         *
         * @param NConnectionString.of(connectionString) n connection string.of(connection string)
         * @return of result
         */
        return of(NConnectionString.of(connectionString));
    }

    /**
     * Returns the connection string representing the target host for execution.
     * When non-blank, this connection string will be used to connect to a remote host.
     *
     * @return the target host connection string
     * @since 0.8.4
     */
    NConnectionString connectionString();

    /**
     * Os family.
     *
     * @return os family result
     */
    NOsFamily osFamily();

    /**
     * Shell families.
     *
     * @return shell families result
     */
    Set<NShellFamily> shellFamilies();

    /**
     * Root user name.
     *
     * @return root user name result
     */
    String rootUserName();

    /**
     * User name.
     *
     * @return user name result
     */
    String userName();

    /**
     * User home.
     *
     * @return user home result
     */
    String userHome();

    /**
     * Shell family.
     *
     * @return shell family result
     */
    NShellFamily shellFamily();

    /**
     * Shell.
     *
     * @return shell result
     */
    NId shell();

    /**
     * Desktop environment.
     *
     * @return desktop environment result
     */
    NId desktopEnvironment();

    /**
     * Desktop environments.
     *
     * @return desktop environments result
     */
    Set<NId> desktopEnvironments();

    /**
     * Desktop environment family.
     *
     * @return desktop environment family result
     */
    NDesktopEnvironmentFamily desktopEnvironmentFamily();

    /**
     * Desktop environment families.
     *
     * @return desktop environment families result
     */
    Set<NDesktopEnvironmentFamily> desktopEnvironmentFamilies();

    /**
     * Checks if is native image.
     *
     * @return is native image result
     */
    boolean isNativeImage();

    /**
     * Java.
     *
     * @return java result
     */
    NId java();

    /**
     * Os.
     *
     * @return os result
     */
    NId os();

    /**
     * Os dist.
     *
     * @return os dist result
     */
    NId osDist();

    /**
     * Arch.
     *
     * @return arch result
     */
    NId arch();

    /**
     * Arch family.
     *
     * @return arch family result
     */
    NArchFamily archFamily();

    /**
     * Checks if is graphical desktop environment.
     *
     * @return is graphical desktop environment result
     */
    boolean isGraphicalDesktopEnvironment();

    /**
     * Returns the desktop integration support.
     *
     * @param target target
     * @return get desktop integration support result
     */
    NSupportMode getDesktopIntegrationSupport(NDesktopIntegrationItem target);

    /**
     * Desktop path.
     *
     * @return desktop path result
     */
    Path desktopPath();

    /**
     * Returns the env.
     *
     * @param name name
     * @return get env result
     */
    NOptional<String> getEnv(String name);

    /**
     * Env.
     *
     * @return env result
     */
    Map<String, String> env();

    /**
     * Network/DNS hostname (what other machines resolve)
     *
     * @return
     */
    String hostName();

    /**
     * OS-level friendly/computer name (what user sees in System Settings)
     *
     * @return
     */
    String machineName();

    /**
     * CPU RAM
     * @return CPU RAM
     * @since 1.0.0
     */
    NRam ram();

    /**
     * GPU RAMs
     * @return GPU RAMs
     * @since 1.0.0
     */
    List<NGpu> gpus();

    /**
     * Returns a fresh NEnv instance with current runtime values.
     *
     * <p>This performs a full re-initialization (may involve SSH commands,
     * process spawns, etc.) and is significantly more expensive than cached
     * property access. Use sparingly when environment state is known to have
     * changed (e.g., after hostname reconfiguration via DHCP).
     *
     * <p>Existing NEnv instances remain valid snapshots — this returns a NEW
     * instance. Replace your reference to use the refreshed values:
     * <pre>
     * env = env.refresh();  // ← Must reassign!
     * </pre>
     *
     * @return new NEnv instance reflecting current environment state
     * @since 0.8.9
     */
    NEnv refresh();

    /**
     * Pid.
     *
     * @return pid result
     */
    String pid();
}

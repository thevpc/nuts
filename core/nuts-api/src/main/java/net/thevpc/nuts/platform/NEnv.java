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

public interface NEnv extends NComponent {
    static NEnv of() {
        return NExtensions.of(NEnv.class);
    }

    static NEnv of(NConnectionString connectionString) {
        if (NBlankable.isBlank(connectionString) || NBlankable.isBlank(connectionString.getHost())) {
            return of();
        }

        NConnectionStringBuilder connectionStringBuilder = connectionString.builder()
                //remove 'path' query param because target is independent of path
                .setPath(null);
        NConnectionString normalizedConnectionStringWithUse = connectionString.normalize();

        NConnectionString normalizedConnectionStringWithoutUse = connectionStringBuilder
                //remove 'use' query param because target is independent of transport
                .setQueryParam("use", null)
                .build();


        Map<NConnectionString, NEnv> cache = NWorkspace.of().getOrComputeProperty(NEnv.class + "::Cache", () -> (Map<NConnectionString, NEnv>) new ConcurrentHashMap<NConnectionString, NEnv>());
        return cache.computeIfAbsent(normalizedConnectionStringWithoutUse, x -> NExtensions.of().createSupported(NEnv.class, normalizedConnectionStringWithUse).get());
    }

    static NEnv of(String connectionString) {
        if (NBlankable.isBlank(connectionString)) {
            return NEnv.of();
        }
        return of(NConnectionString.of(connectionString));
    }

    /**
     * Returns the connection string representing the target host for execution.
     * When non-blank, this connection string will be used to connect to a remote host.
     *
     * @return the target host connection string
     * @since 0.8.4
     */
    NConnectionString getConnectionString();

    NOsFamily getOsFamily();

    Set<NShellFamily> getShellFamilies();

    String getRootUserName();

    String getUserName();

    String getUserHome();

    NShellFamily getShellFamily();

    NId getShell();

    NId getDesktopEnvironment();

    Set<NId> getDesktopEnvironments();

    NDesktopEnvironmentFamily getDesktopEnvironmentFamily();

    Set<NDesktopEnvironmentFamily> getDesktopEnvironmentFamilies();

    boolean isNativeImage();

    NId getJava();

    NId getOs();

    NId getOsDist();

    NId getArch();

    NArchFamily getArchFamily();

    /**
     * GPU devices of this environment, sorted by pci address.
     * <p>
     * Devices describe hardware only, the compute runtime able to drive them is
     * reported by {@link NParallelProcessorFamily}. The list contains every
     * display adapter, integrated ones included, so callers interested in
     * compute should filter on {@link NGpuDevice#isComputeCapable()}.
     * <p>
     * Detection is currently implemented for the local linux environment only,
     * other environments report an empty list rather than guessing.
     *
     * @return gpu devices, empty when none is detected or detection is unsupported
     * @since 0.8.9
     */
    List<NGpuDevice> getGpuDevices();

    /**
     * The gpu device to use when a single one has to be picked.
     * <p>
     * A dedicated device wins over an integrated one, the largest memory being
     * the tie breaker, and only compute capable devices are eligible. The choice
     * is deterministic so that resolution stays reproducible in unattended runs.
     *
     * @return the primary gpu device, empty when none is eligible
     * @since 0.8.9
     */
    NOptional<NGpuDevice> getGpuDevice();

    /**
     * Reads the currently free memory of a gpu device.
     * <p>
     * This value is volatile and is deliberately kept out of {@link NGpuDevice},
     * which holds only stable properties. It is never cached and must not take
     * part in dependency resolution, otherwise resolution would stop being
     * reproducible.
     *
     * @param device device to query, as returned by {@link #getGpuDevices()}
     * @return free memory in bytes, negative when unknown or unsupported
     * @since 0.8.9
     */
    long queryGpuFreeMemoryBytes(NGpuDevice device);

    /**
     * Parallel processing runtimes available on this environment, each reporting
     * separately whether code targeting it can be executed and whether it can be
     * compiled.
     * <p>
     * This is the software axis, {@link #getGpuDevices()} being the hardware one.
     * A machine holding an NVIDIA device may well expose cuda as runnable but
     * not buildable, which are opposite answers when choosing between a prebuilt
     * artifact and sources.
     *
     * @return available runtimes, empty when none is detected
     * @since 0.8.9
     */
    List<NParallelProcessorRuntime> getParallelProcessorRuntimes();

    /**
     * The parallel processing runtime family to use when a single one has to be
     * picked, vendor native stacks winning over cross vendor layers.
     *
     * @return the family, {@link NParallelProcessorFamily#NONE} when probing
     * found none, {@link NParallelProcessorFamily#UNKNOWN} when it could not
     * conclude
     * @since 0.8.9
     */
    NParallelProcessorFamily getParallelProcessorFamily();

    boolean isGraphicalDesktopEnvironment();

    NSupportMode getDesktopIntegrationSupport(NDesktopIntegrationItem target);

    Path getDesktopPath();

    NOptional<String> getEnv(String name);

    Map<String, String> getEnv();

    /**
     * Network/DNS hostname (what other machines resolve)
     *
     * @return
     */
    String getHostName();

    /**
     * OS-level friendly/computer name (what user sees in System Settings)
     *
     * @return
     */
    String getMachineName();

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
}

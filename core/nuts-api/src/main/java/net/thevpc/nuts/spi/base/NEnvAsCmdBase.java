package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.spi.NEnvCmdSPI;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSupportMode;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NEnvAsCmdBase class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public abstract class NEnvAsCmdBase implements NEnv {
    private NConnectionString connectionString;
    private NEnv defEnv;
    private String protocol;

    /**
     * N env as cmd base.
     *
     * @param context context
     * @param protocol protocol
     * @return n env as cmd base result
     */
    public NEnvAsCmdBase(NScorableContext context,String protocol) {
        this.protocol=protocol;
      /**
       * Init.
       *
       * @param context.criteria() context.criteria()
       */
        init(context.criteria());
    }

    /**
     * N env as cmd base.
     *
     * @param connectionString connection string
     * @param protocol protocol
     * @return n env as cmd base result
     */
    public NEnvAsCmdBase(NConnectionString connectionString,String protocol) {
        this.protocol=protocol;
      /**
       * Init.
       *
       * @param connectionString connection string
       */
        init(connectionString);
    }

    @Override
    public boolean isNativeImage() {
        return false;
    }

    /**
     * Init.
     *
     * @param connectionString connection string
     * @return init result
     */
    private void init(NConnectionString connectionString){
        this.connectionString = connectionString;
        NEnvCmdSPI commander=new NEnvCmdSPI() {
            @Override
            public String exec(String cmd) {
                /**
                 * Run system command.
                 *
                 * @param cmd cmd
                 * @return run system command result
                 */
                return runSystemCommand(cmd);
            }

            @Override
            public NConnectionString targetConnectionString() {
                return connectionString;
            }
        };
        defEnv = NExtensions.of().createSupported(NEnv.class, commander).get();
    }

    @Override
    public String machineName() {
        return defEnv.machineName();
    }

    @Override
    public NConnectionString connectionString() {
        return connectionString;
    }

    @Override
    public NOsFamily osFamily() {
        return defEnv.osFamily();
    }

    @Override
    public Set<NShellFamily> shellFamilies() {
        return defEnv.shellFamilies();
    }

    @Override
    public NShellFamily shellFamily() {
        return defEnv.shellFamily();
    }

    @Override
    public NId desktopEnvironment() {
        return defEnv.desktopEnvironment();
    }

    @Override
    public Set<NId> desktopEnvironments() {
        return defEnv.desktopEnvironments();
    }

    @Override
    public NDesktopEnvironmentFamily desktopEnvironmentFamily() {
        return defEnv.desktopEnvironmentFamily();
    }

    @Override
    public Set<NDesktopEnvironmentFamily> desktopEnvironmentFamilies() {
        return defEnv.desktopEnvironmentFamilies();
    }

    @Override
    public NId java() {
        return defEnv.java();
    }

    @Override
    public NId os() {
        return defEnv.os();
    }

    @Override
    public NId osDist() {
        return defEnv.osDist();
    }

    @Override
    public NId arch() {
        return defEnv.arch();
    }

    @Override
    public NArchFamily archFamily() {
        return defEnv.archFamily();
    }

    @Override
    public boolean isGraphicalDesktopEnvironment() {
        return defEnv.isGraphicalDesktopEnvironment();
    }

    @Override
    public NSupportMode getDesktopIntegrationSupport(NDesktopIntegrationItem target) {
        return defEnv.getDesktopIntegrationSupport(target);
    }

    @Override
    public Path desktopPath() {
        return defEnv.desktopPath();
    }

    @Override
    public NOptional<String> getEnv(String name) {
        return defEnv.getEnv(name);
    }

    @Override
    public Map<String, String> env() {
        return defEnv.env();
    }

    @Override
    public String rootUserName() {
        return defEnv.rootUserName();
    }

    @Override
    public String userName() {
        return defEnv.userName();
    }

    @Override
    public String userHome() {
        return defEnv.userHome();
    }

    @Override
    public NId shell() {
        return defEnv.shell();
    }

    @Override
    public String hostName() {
        return defEnv.hostName();
    }

    /**
     * Checks if is supported protocol.
     *
     * @param protocol protocol
     * @return is supported protocol result
     */
    private boolean isSupportedProtocol(String protocol) {
      /**
       * Return.
       *
       * @param this.protocol.equals(protocol) this.protocol.equals(protocol)
       */
        return (this.protocol.equals(protocol));
    }

    @Override
    public NRam ram() {
        return defEnv.ram();
    }

    @Override
    public List<NGpuDevice> gpuDevices() {
        return defEnv.gpuDevices();
    }

    @Override
    public NOptional<NGpuDevice> gpuDevice() {
        return defEnv.gpuDevice();
    }

    @Override
    public long queryGpuFreeMemoryBytes(NGpuDevice device) {
        return defEnv.queryGpuFreeMemoryBytes(device);
    }

    @Override
    public List<NParallelProcessorRuntime> parallelProcessorRuntimes() {
        return defEnv.parallelProcessorRuntimes();
    }

    @Override
    public NParallelProcessorFamily parallelProcessorFamily() {
        return defEnv.parallelProcessorFamily();
    }

    /**
     * Run system command.
     *
     * @param cmd cmd
     * @return run system command result
     */
    protected abstract String runSystemCommand(String cmd);

    @Override
    public String pid() {
        return defEnv.pid();
    }
}

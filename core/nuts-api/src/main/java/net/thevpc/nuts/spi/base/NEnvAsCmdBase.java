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

public abstract class NEnvAsCmdBase implements NEnv {
    private NConnectionString connectionString;
    private NEnv defEnv;
    private String protocol;

    public NEnvAsCmdBase(NScorableContext context,String protocol) {
        this.protocol=protocol;
        init(context.criteria());
    }

    public NEnvAsCmdBase(NConnectionString connectionString,String protocol) {
        this.protocol=protocol;
        init(connectionString);
    }

    @Override
    public boolean isNativeImage() {
        return false;
    }

    private void init(NConnectionString connectionString){
        this.connectionString = connectionString;
        NEnvCmdSPI commander=new NEnvCmdSPI() {
            @Override
            public String exec(String cmd) {
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

    private boolean isSupportedProtocol(String protocol) {
        return (this.protocol.equals(protocol));
    }

    @Override
    public NRam ram() {
        return defEnv.ram();
    }

    @Override
    public List<NGpu> gpus() {
        return defEnv.gpus();
    }

    protected abstract String runSystemCommand(String cmd);

    @Override
    public String pid() {
        return defEnv.pid();
    }
}

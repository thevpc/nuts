package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.util.*;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class NEnvSshImpl implements NEnv {
    private NConnectionString connectionString;
    private NEnv defEnv;

    public NEnvSshImpl(NScorableContext context) {
        init(context.getCriteria());
    }

    public NEnvSshImpl(NConnectionString connectionString) {
        init(connectionString);
    }

    @Override
    public NEnv refresh() {
        return new NEnvSshImpl(connectionString);
    }

    @NScore
    public static int getScore(NScorableContext context) {
        Object c = context.getCriteria();
        if (c instanceof NConnectionString) {
            NConnectionString z = (NConnectionString) c;
            if (isSupportedProtocol(z.getProtocol())) {
                return NScorable.DEFAULT_SCORE;
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    private void init(NConnectionString connectionString){
        this.connectionString = connectionString;
        NEnvCmdSPI commander=new NEnvCmdSPI() {
            @Override
            public String exec(String cmd) {
                return runOnceSystemGrab(cmd);
            }

            @Override
            public NConnectionString getTargetConnectionString() {
                return connectionString;
            }
        };
        defEnv = NExtensions.of().createSupported(NEnv.class, commander).get();
    }

    @Override
    public String getMachineName() {
        return defEnv.getMachineName();
    }

    @Override
    public NConnectionString getConnectionString() {
        return connectionString;
    }

    @Override
    public NOsFamily getOsFamily() {
        return defEnv.getOsFamily();
    }

    @Override
    public Set<NShellFamily> getShellFamilies() {
        return defEnv.getShellFamilies();
    }

    @Override
    public NShellFamily getShellFamily() {
        return defEnv.getShellFamily();
    }

    @Override
    public NId getDesktopEnvironment() {
        return defEnv.getDesktopEnvironment();
    }

    @Override
    public Set<NId> getDesktopEnvironments() {
        return defEnv.getDesktopEnvironments();
    }

    @Override
    public NDesktopEnvironmentFamily getDesktopEnvironmentFamily() {
        return defEnv.getDesktopEnvironmentFamily();
    }

    @Override
    public Set<NDesktopEnvironmentFamily> getDesktopEnvironmentFamilies() {
        return defEnv.getDesktopEnvironmentFamilies();
    }

    @Override
    public NId getJava() {
        return defEnv.getJava();
    }

    @Override
    public NId getOs() {
        return defEnv.getOs();
    }

    @Override
    public NId getOsDist() {
        return defEnv.getOsDist();
    }

    @Override
    public NId getArch() {
        return defEnv.getArch();
    }

    @Override
    public NArchFamily getArchFamily() {
        return defEnv.getArchFamily();
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
    public Path getDesktopPath() {
        return defEnv.getDesktopPath();
    }

    @Override
    public NOptional<String> getEnv(String name) {
        return defEnv.getEnv(name);
    }

    @Override
    public Map<String, String> getEnv() {
        return defEnv.getEnv();
    }

    @Override
    public String getRootUserName() {
        return defEnv.getRootUserName();
    }

    @Override
    public String getUserName() {
        return defEnv.getUserName();
    }

    @Override
    public String getUserHome() {
        return defEnv.getUserHome();
    }

    @Override
    public NId getShell() {
        return defEnv.getShell();
    }

    @Override
    public String getHostName() {
        return defEnv.getHostName();
    }

    private static boolean isSupportedProtocol(String protocol) {
        return ("ssh".equals(protocol));
    }


    private String runOnceSystemGrab(String cmd) {
        try (SshConnection sshc = SshConnectionPool.of().acquire(connectionString)) {
            return sshc.execStringCommandGrabbed(cmd).outString();
        }
    }

}

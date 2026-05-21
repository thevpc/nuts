package net.thevpc.nuts.runtime.standalone.platform.rnsh;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NullInputStream;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.util.*;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class NEnvRnsh implements NEnv {
    private NConnectionString connectionString;
    private NEnv defEnv;

    public NEnvRnsh(NScorableContext context) {
        init(context.criteria());
    }
    public NEnvRnsh(NConnectionString connectionString) {
        init(connectionString);
    }

    @Override
    public NEnv refresh() {
        return new NEnvRnsh(connectionString);
    }

    @NScore
    public static int getScore(NScorableContext context) {
        Object c = context.criteria();
        if (c instanceof NConnectionString) {
            NConnectionString z = (NConnectionString) c;
            if (isSupportedProtocol(z.protocol())) {
                return NScorable.DEFAULT_SCORE;
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
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
                return runOnceSystemGrab(cmd);
            }

            @Override
            public NConnectionString targetConnectionString() {
                return connectionString;
            }
        };

        defEnv = NExtensions.of().createSupported(NEnv.class, commander).get();
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

    @Override
    public String machineName() {
        return defEnv.machineName();
    }

    private static boolean isSupportedProtocol(String protocol) {
        return ("rnsh".equals(protocol));
    }

    private String runOnceSystemGrab(String cmd) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int x = RnshPool.of().get(connectionString).exec(new String[]{cmd}, true, NullInputStream.INSTANCE, out,err);
        return out.toString();
    }

}

package net.thevpc.nuts.runtime.standalone.platform;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.runtime.standalone.app.gui.CoreNUtilGui;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.*;

import java.util.Map;

@NComponentScope(NScopeType.WORKSPACE)
public class NEnvLocal extends NEnvBase {


    protected boolean initialized;

    private void init() {
        if (!this.initialized) {
            this.os = NId.get(CorePlatformUtils.getPlatformOs()).get();
            NId platformOsDist = CorePlatformUtils.getPlatformOsDist();
            if (platformOsDist == null) {
                platformOsDist = NId.of("default");
            }
            this.osDist = platformOsDist;
            this.java = NJavaSdkUtils.of(NWorkspace.of()).createJdkId(System.getProperty("java.version"));
            this.arch = NId.get(System.getProperty("os.arch")).get();
            this.archFamily = NArchFamily.getCurrent();
            this.initialized = true;
            userHome=System.getProperty("user.home");
            userName=System.getProperty("user.name");
            switch (getOsFamily()) {
                case WINDOWS:{
                    rootUserName=resolveWindowAdminName(userName,rootUserName);
                    break;
                }
                default: {
                    rootUserName="root";
                }
            }
        }
    }

    @Override
    public String getUserName0() {
        return userName;
    }

    @Override
    public String getUserHome0() {
        return userHome;
    }

    @Override
    protected String getRootUserName0() {
        return rootUserName;
    }

    @Override
    public NId getShell0() {
        return null;
    }

    public NConnectionString getConnectionString() {
        return null;
    }

    @Override
    protected NOsFamily getOsFamily0() {
        return NOsFamily.getCurrent();
    }

    @Override
    public NShellFamily getShellFamily0() {
        return NShellFamily.getCurrent();
    }

    @Override
    public NId getJava0() {
        init();
        return java;
    }

    @Override
    public NId getOs0() {
        init();
        return os;
    }

    public NId getOsDist0() {
        init();
        return osDist;
    }

    @Override
    public NId getArch0() {
        init();
        return arch;
    }

    @Override
    public NArchFamily getArchFamily0() {
        init();
        return archFamily;
    }

    @Override
    public boolean isGraphicalDesktopEnvironment0() {
        return CoreNUtilGui.isGraphicalDesktopEnvironment();
    }


    @Override
    public NOptional<String> getEnv(String name) {
        return NOptional.of(getEnv().get(name));
    }

    @Override
    public Map<String, String> getEnv() {
        return NWorkspaceExt.of().getSysEnv();
    }

    @NScore(fixed = NScorable.DEFAULT_SCORE)
    public static int getScore(NScorableContext context) {
        Object criteria = context.getCriteria();
        if (NBlankable.isBlank(criteria)) {
            return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }
}

package net.thevpc.nuts.runtime.standalone.platform;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.util.NSupportMode;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class NEnvBase implements NEnv {
    protected Set<NId> desktopEnvironments;
    protected NId java;
    protected NId os;
    protected NOsFamily osFamily;
    protected NShellFamily shellFamily;
    protected NId arch;
    protected NId osDist;
    protected NArchFamily archFamily;
    protected String hostName;
    protected String machineName;
    protected NDesktopEnvironmentFamily osDesktopEnvironmentFamily;
    protected NId shell;

    protected String userName;
    protected String rootUserName;
    protected String userHome;
    protected Set<NDesktopEnvironmentFamily> osDesktopEnvironmentFamilies;
    protected Boolean gui;

    protected abstract NOsFamily getOsFamily0();

    protected abstract NId getShell0();

    protected abstract NId getOs0();

    protected abstract NId getJava0();

    protected abstract NId getOsDist0();

    protected abstract NId getArch0();

    protected abstract NArchFamily getArchFamily0();

    protected abstract NShellFamily getShellFamily0();

    protected abstract String getRootUserName0();

    protected abstract String getUserName0();

    protected abstract String getUserHome0();

    protected abstract boolean isGraphicalDesktopEnvironment0();

    @Override
    public boolean isGraphicalDesktopEnvironment() {
        if (gui == null) {
            gui = isGraphicalDesktopEnvironment0();
        }
        return gui;
    }

    @Override
    public NId getOsDist() {
        if (osDist == null) {
            osDist = getOsDist0();
        }
        return osDist;
    }

    @Override
    public final NId getArch() {
        if (arch == null) {
            arch = getArch0();
        }
        return arch;
    }

    @Override
    public final NArchFamily getArchFamily() {
        if (archFamily == null) {
            archFamily = getArchFamily0();
        }
        return archFamily;
    }

    @Override
    public final String getRootUserName() {
        if (rootUserName == null) {
            rootUserName = getRootUserName0();
        }
        return rootUserName;
    }

    @Override
    public final String getUserName() {
        if (userName == null) {
            userName = getUserName0();
        }
        return userName;
    }

    @Override
    public final NId getJava() {
        if (java == null) {
            java = getJava0();
        }
        return java;
    }

    @Override
    public final String getUserHome() {
        if (userHome == null) {
            userHome = getUserHome0();
        }
        return userHome;
    }

    @Override
    public final NId getOs() {
        if (os == null) {
            os = getOs0();
        }
        return os;
    }

    @Override
    public final NOsFamily getOsFamily() {
        if (osFamily == null) {
            osFamily = getOsFamily0();
        }
        return osFamily;
    }

    @Override
    /*fix this add field  like above*/
    public final Set<NShellFamily> getShellFamilies() {
        return getShellFamilies(true);
    }

    public final Set<NShellFamily> getShellFamilies(boolean allEvenNonInstalled) {
        return NEnvUtils.getShellFamilies(this, allEvenNonInstalled);
    }

    @Override
    public final NId getShell() {
        if (shell == null) {
            shell = getShell0();
        }
        return shell;
    }

    @Override
    public final NShellFamily getShellFamily() {
        if (shellFamily == null) {
            shellFamily = getShellFamily0();
        }
        return shellFamily;
    }

    @Override
    public final NId getDesktopEnvironment() {
        return getDesktopEnvironments().stream().findFirst().get();
    }

    @Override
    public final Set<NId> getDesktopEnvironments() {
        if (desktopEnvironments == null) {
            desktopEnvironments = NEnvUtils.getDesktopEnvironments0(this);
        }
        return desktopEnvironments;
    }

    @Override
    public final NDesktopEnvironmentFamily getDesktopEnvironmentFamily() {
        if (osDesktopEnvironmentFamily == null) {
            osDesktopEnvironmentFamily = getDesktopEnvironmentFamily0();
        }
        return osDesktopEnvironmentFamily;
    }

    @Override
    public final Set<NDesktopEnvironmentFamily> getDesktopEnvironmentFamilies() {
        Set<NId> desktopEnvironments = getDesktopEnvironments();
        LinkedHashSet<NDesktopEnvironmentFamily> all = new LinkedHashSet<>();
        for (NId desktopEnvironment : desktopEnvironments) {
            all.add(NDesktopEnvironmentFamily.parse(desktopEnvironment.getShortName()).orNull());
        }
        return new LinkedHashSet<>(all);
    }

    public NDesktopEnvironmentFamily getDesktopEnvironmentFamily0() {
        Set<NDesktopEnvironmentFamily> all = getDesktopEnvironmentFamilies();
        if (all.size() == 0) {
            return NDesktopEnvironmentFamily.UNKNOWN;
        }
        boolean unknown = false;
        boolean none = false;
        boolean headless = false;
        for (NDesktopEnvironmentFamily f : all) {
            switch (f) {
                case UNKNOWN: {
                    unknown = true;
                    break;
                }
                case HEADLESS: {
                    headless = true;
                    break;
                }
                case NONE: {
                    none = true;
                    break;
                }
                default: {
                    return f;
                }
            }
        }
        if (headless) {
            return NDesktopEnvironmentFamily.HEADLESS;
        }
        if (none) {
            return NDesktopEnvironmentFamily.NONE;
        }
        if (unknown) {
            return NDesktopEnvironmentFamily.UNKNOWN;
        }
        return NDesktopEnvironmentFamily.UNKNOWN;
    }

    public Path getDesktopPath() {
        return NEnvUtils.getDesktopPath(this);
    }


    public abstract String getMachineName0();

    public abstract String getHostName0();

    public final String getMachineName() {
        if (machineName == null) {
            machineName = getMachineName0();
        }
        return machineName;
    }

    public final String getHostName() {
        if (hostName == null) {
            hostName = getHostName0();
        }
        return hostName;
    }


    @Override
    public final NSupportMode getDesktopIntegrationSupport(NDesktopIntegrationItem item) {
        return NEnvUtils.getDesktopIntegrationSupport(this, item);
    }


    protected String resolveWindowAdminName(String currUserName, String defaultRootUserName) {
        switch (NStringUtils.trim(currUserName).toLowerCase()) {
            case "adminitrateur": {
                return "Administrateur";
            }
            case "administrador": {
                return "Administrador";
            }
            case "administratör": {
                return "Administratör";
            }
            case "järjestelmänvalvoja": {
                return "Järjestelmänvalvoja";
            }
            case "rendszergazda": {
                return "Rendszergazda";
            }
            case "администратор": {
                return "Администратор";
            }
            default: {
                return "Administrator";
            }
        }
    }
}

package net.thevpc.nuts.runtime.standalone.platform;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.util.NSupportMode;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
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
    protected List<NParallelProcessorRuntime> parallelProcessorRuntimes;

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

    /**
     * Detects the parallel processing runtimes of this environment.
     * Implementations unable to inspect their target return an empty list
     * rather than describing the local machine, which would report the wrong
     * capabilities for a remote target.
     *
     * @return detected runtimes, never null
     */
    protected abstract List<NParallelProcessorRuntime> getParallelProcessorRuntimes0();

    /**
     * Whether probing this environment for parallel processing runtimes is
     * possible at all.
     * <p>
     * An empty {@link #parallelProcessorRuntimes()} is ambiguous on its own : it
     * is reported both by a machine that has no runtime installed and by an
     * environment that could not be inspected. This hook separates the two, so
     * that the former answers {@link NParallelProcessorFamily#NONE} and the
     * latter {@link NParallelProcessorFamily#UNKNOWN}. Implementations unable to
     * inspect their target leave it false.
     *
     * @return true when an empty runtime list means "none is installed"
     */
    protected boolean isParallelProcessorDetectionSupported() {
        return false;
    }

    @Override
    public final List<NParallelProcessorRuntime> parallelProcessorRuntimes() {
        if (parallelProcessorRuntimes == null) {
            List<NParallelProcessorRuntime> r = getParallelProcessorRuntimes0();
            parallelProcessorRuntimes = r == null ? Collections.<NParallelProcessorRuntime>emptyList() : r;
        }
        return parallelProcessorRuntimes;
    }

    @Override
    public NParallelProcessorFamily parallelProcessorFamily() {
        List<NParallelProcessorRuntime> runtimes = parallelProcessorRuntimes();
        if (runtimes.isEmpty()) {
            return isParallelProcessorDetectionSupported()
                    ? NParallelProcessorFamily.NONE
                    : NParallelProcessorFamily.UNKNOWN;
        }
        // detectAvailable() already yields vendor native stacks before cross
        // vendor layers, so the first runnable entry is the most specific one
        for (NParallelProcessorRuntime r : runtimes) {
            if (r.isRuntimeAvailable() && !r.getFamily().isCrossVendor()) {
                return r.getFamily();
            }
        }
        for (NParallelProcessorRuntime r : runtimes) {
            if (r.isRuntimeAvailable()) {
                return r.getFamily();
            }
        }
        return runtimes.get(0).getFamily();
    }

    @Override
    public boolean isGraphicalDesktopEnvironment() {
        if (gui == null) {
            gui = isGraphicalDesktopEnvironment0();
        }
        return gui;
    }

    @Override
    public NId osDist() {
        if (osDist == null) {
            osDist = getOsDist0();
        }
        return osDist;
    }

    @Override
    public final NId arch() {
        if (arch == null) {
            arch = getArch0();
        }
        return arch;
    }

    @Override
    public final NArchFamily archFamily() {
        if (archFamily == null) {
            archFamily = getArchFamily0();
        }
        return archFamily;
    }

    @Override
    public final String rootUserName() {
        if (rootUserName == null) {
            rootUserName = getRootUserName0();
        }
        return rootUserName;
    }

    @Override
    public final String userName() {
        if (userName == null) {
            userName = getUserName0();
        }
        return userName;
    }

    @Override
    public final NId java() {
        if (java == null) {
            java = getJava0();
        }
        return java;
    }

    @Override
    public final String userHome() {
        if (userHome == null) {
            userHome = getUserHome0();
        }
        return userHome;
    }

    @Override
    public final NId os() {
        if (os == null) {
            os = getOs0();
        }
        return os;
    }

    @Override
    public final NOsFamily osFamily() {
        if (osFamily == null) {
            osFamily = getOsFamily0();
        }
        return osFamily;
    }

    @Override
    /*fix this add field  like above*/
    public final Set<NShellFamily> shellFamilies() {
        return getShellFamilies(true);
    }

    public final Set<NShellFamily> getShellFamilies(boolean allEvenNonInstalled) {
        return NEnvUtils.getShellFamilies(this, allEvenNonInstalled);
    }

    @Override
    public final NId shell() {
        if (shell == null) {
            shell = getShell0();
        }
        return shell;
    }

    @Override
    public final NShellFamily shellFamily() {
        if (shellFamily == null) {
            shellFamily = getShellFamily0();
        }
        return shellFamily;
    }

    @Override
    public final NId desktopEnvironment() {
        return desktopEnvironments().stream().findFirst().get();
    }

    @Override
    public final Set<NId> desktopEnvironments() {
        if (desktopEnvironments == null) {
            desktopEnvironments = NEnvUtils.getDesktopEnvironments0(this);
        }
        return desktopEnvironments;
    }

    @Override
    public final NDesktopEnvironmentFamily desktopEnvironmentFamily() {
        if (osDesktopEnvironmentFamily == null) {
            osDesktopEnvironmentFamily = getDesktopEnvironmentFamily0();
        }
        return osDesktopEnvironmentFamily;
    }

    @Override
    public final Set<NDesktopEnvironmentFamily> desktopEnvironmentFamilies() {
        Set<NId> desktopEnvironments = desktopEnvironments();
        LinkedHashSet<NDesktopEnvironmentFamily> all = new LinkedHashSet<>();
        for (NId desktopEnvironment : desktopEnvironments) {
            all.add(NDesktopEnvironmentFamily.parse(desktopEnvironment.shortName()).orNull());
        }
        return new LinkedHashSet<>(all);
    }

    public NDesktopEnvironmentFamily getDesktopEnvironmentFamily0() {
        Set<NDesktopEnvironmentFamily> all = desktopEnvironmentFamilies();
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

    public Path desktopPath() {
        return NEnvUtils.getDesktopPath(this);
    }


    public abstract String getMachineName0();

    public abstract String getHostName0();

    public final String machineName() {
        if (machineName == null) {
            machineName = getMachineName0();
        }
        return machineName;
    }

    public final String hostName() {
        if (hostName == null) {
            hostName = getHostName0();
        }
        return hostName;
    }


    @Override
    public final NSupportMode getDesktopIntegrationSupport(NDesktopIntegrationItem item) {
        return NEnvUtils.getDesktopIntegrationSupport(this, item);
    }


}

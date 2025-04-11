package net.thevpc.nuts;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSupportMode;

import java.util.Map;
import java.util.Set;

public interface NEnvContext {
    NOsFamily getOsFamily();

    Set<NShellFamily> getShellFamilies();

    NShellFamily getShellFamily();

    NId getDesktopEnvironment();

    Set<NId> getDesktopEnvironments();

    NDesktopEnvironmentFamily getDesktopEnvironmentFamily();

    Set<NDesktopEnvironmentFamily> getDesktopEnvironmentFamilies();

    NId getPlatform();

    NId getOs();

    NId getOsDist();

    NId getArch();

    NArchFamily getArchFamily();

    boolean isGraphicalDesktopEnvironment();

    NSupportMode getDesktopIntegrationSupport(NDesktopIntegrationItem target);

    NOptional<String> getSysEnv(String name);

    Map<String, String> getSysEnv();

}

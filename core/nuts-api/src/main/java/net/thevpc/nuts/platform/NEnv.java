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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface NEnv extends NComponent {
    static NEnv of() {
        return NExtensions.of(NEnv.class);
    }

    static NEnv of(NConnectionString connectionString) {
        if(NBlankable.isBlank(connectionString) || NBlankable.isBlank(connectionString.getHost())) {
            return  of();
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

    NId getJava();

    NId getOs();

    NId getOsDist();

    NId getArch();

    NArchFamily getArchFamily();

    boolean isGraphicalDesktopEnvironment();

    NSupportMode getDesktopIntegrationSupport(NDesktopIntegrationItem target);

    Path getDesktopPath();

    NOptional<String> getEnv(String name);

    Map<String, String> getEnv();

    String getHostName();
}

package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsUpdateResult;
import net.vpc.app.nuts.NutsWorkspaceUpdateResult;

import java.util.Arrays;

public class DefaultNutsWorkspaceUpdateResult implements NutsWorkspaceUpdateResult {
    private final NutsUpdateResult api;
    private final NutsUpdateResult runtime;
    private final NutsUpdateResult[] extensions;
    private final NutsUpdateResult[] components;

    public DefaultNutsWorkspaceUpdateResult(NutsUpdateResult api, NutsUpdateResult runtime, NutsUpdateResult[] extensions, NutsUpdateResult[] components) {
        this.api = api;
        this.runtime = runtime;
        this.extensions = extensions;
        this.components = components;
    }

    public NutsUpdateResult getApi() {
        return api;
    }

    public NutsUpdateResult getRuntime() {
        return runtime;
    }

    public NutsUpdateResult[] getExtensions() {
        return Arrays.copyOf(extensions, extensions.length);
    }

    public NutsUpdateResult[] getComponents() {
        return Arrays.copyOf(components, components.length);
    }

    public boolean isUpdatableApi() {
        return api != null;
    }

    public boolean isUpdatableRuntime() {
        return runtime != null;
    }

    public boolean isUpdatableExtensions() {
        return extensions.length > 0;
    }

    public boolean isUpdateAvailable() {
        return getUpdatesCount()>0;
    }

    public int getUpdatesCount() {
        int c = 0;
        if (api != null) {
            c++;
        }
        if (runtime != null) {
            c++;
        }
        return c + extensions.length+components.length;
    }
}

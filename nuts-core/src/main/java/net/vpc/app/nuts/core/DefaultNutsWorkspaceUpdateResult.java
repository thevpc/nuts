package net.vpc.app.nuts.core;

import java.util.ArrayList;
import net.vpc.app.nuts.NutsUpdateResult;
import net.vpc.app.nuts.NutsWorkspaceUpdateResult;

import java.util.Arrays;
import java.util.List;

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

    @Override
    public NutsUpdateResult getApi() {
        return api;
    }

    @Override
    public NutsUpdateResult getRuntime() {
        return runtime;
    }

    @Override
    public NutsUpdateResult[] getExtensions() {
        return Arrays.copyOf(extensions, extensions.length);
    }

    @Override
    public NutsUpdateResult[] getComponents() {
        return Arrays.copyOf(components, components.length);
    }

    @Override
    public boolean isUpdatableApi() {
        return api != null && api.isUpdateAvailable();
    }

    @Override
    public boolean isUpdatableRuntime() {
        return runtime != null && runtime.isUpdateAvailable();
    }

    @Override
    public boolean isUpdatableExtensions() {
        for (NutsUpdateResult r : extensions) {
            if (r.isUpdateAvailable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isUpdateAvailable() {
        return getUpdatesCount() > 0;
    }

    @Override
    public NutsUpdateResult[] getAllUpdates() {
        List<NutsUpdateResult> all = new ArrayList<>();
        if (api != null && api.isUpdateAvailable()) {
            all.add(api);
        }
        if (runtime != null && runtime.isUpdateAvailable()) {
            all.add(runtime);
        }
        for (NutsUpdateResult r : extensions) {
            if (r.isUpdateAvailable()) {
                all.add(r);
            }
        }
        for (NutsUpdateResult r : components) {
            if (r.isUpdateAvailable()) {
                all.add(r);
            }
        }
        return all.toArray(new NutsUpdateResult[0]);
    }

    @Override
    public NutsUpdateResult[] getAllResults() {
        List<NutsUpdateResult> all = new ArrayList<>();
        if (api != null) {
            all.add(api);
        }
        if (runtime != null) {
            all.add(runtime);
        }
        all.addAll(Arrays.asList(extensions));
        all.addAll(Arrays.asList(components));
        return all.toArray(new NutsUpdateResult[0]);
    }

    @Override
    public int getUpdatesCount() {
        int c = 0;
        if (api != null && api.isUpdateAvailable()) {
            c++;
        }
        if (runtime != null && runtime.isUpdateAvailable()) {
            c++;
        }
        for (NutsUpdateResult r : extensions) {
            if (r.isUpdateAvailable()) {
                c++;
            }
        }
        for (NutsUpdateResult r : components) {
            if (r.isUpdateAvailable()) {
                c++;
            }
        }
        return c;
    }
}

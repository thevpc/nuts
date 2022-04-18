package net.thevpc.nuts.runtime.standalone.workspace.cmd.update;

import net.thevpc.nuts.NutsUpdateResult;
import net.thevpc.nuts.NutsWorkspaceUpdateResult;

import java.util.ArrayList;
import java.util.List;

public class DefaultNutsWorkspaceUpdateResult implements NutsWorkspaceUpdateResult {

    private final NutsUpdateResult api;
    private final NutsUpdateResult runtime;
    private final List<NutsUpdateResult> extensions;
    private final List<NutsUpdateResult> artifacts;

    public DefaultNutsWorkspaceUpdateResult(NutsUpdateResult api, NutsUpdateResult runtime, List<NutsUpdateResult> extensions, List<NutsUpdateResult> components) {
        this.api = api;
        this.runtime = runtime;
        this.extensions = extensions;
        this.artifacts = components;
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
    public List<NutsUpdateResult> getExtensions() {
        return extensions;
    }

    @Override
    public List<NutsUpdateResult> getArtifacts() {
        return artifacts;
    }

    @Override
    public boolean isUpdatableApi() {
        return api != null && api.isUpdatable();
    }

    @Override
    public boolean isUpdatableRuntime() {
        return runtime != null && runtime.isUpdatable();
    }

    @Override
    public boolean isUpdatableExtensions() {
        for (NutsUpdateResult r : extensions) {
            if (r.isUpdatable()) {
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
    public List<NutsUpdateResult> getUpdatable() {
        List<NutsUpdateResult> all = new ArrayList<>();
        if (api != null && api.isUpdatable()) {
            all.add(api);
        }
        if (runtime != null && runtime.isUpdatable()) {
            all.add(runtime);
        }
        for (NutsUpdateResult r : extensions) {
            if (r.isUpdatable()) {
                all.add(r);
            }
        }
        for (NutsUpdateResult r : artifacts) {
            if (r.isUpdatable()) {
                all.add(r);
            }
        }
        return all;
    }

    @Override
    public List<NutsUpdateResult> getAllResults() {
        List<NutsUpdateResult> all = new ArrayList<>();
        if (api != null) {
            all.add(api);
        }
        if (runtime != null) {
            all.add(runtime);
        }
        all.addAll((extensions));
        all.addAll((artifacts));
        return all;
    }

    @Override
    public int getUpdatesCount() {
        int c = 0;
        if (api != null && api.isUpdatable()) {
            c++;
        }
        if (runtime != null && runtime.isUpdatable()) {
            c++;
        }
        for (NutsUpdateResult r : extensions) {
            if (r.isUpdatable()) {
                c++;
            }
        }
        for (NutsUpdateResult r : artifacts) {
            if (r.isUpdatable()) {
                c++;
            }
        }
        return c;
    }
}

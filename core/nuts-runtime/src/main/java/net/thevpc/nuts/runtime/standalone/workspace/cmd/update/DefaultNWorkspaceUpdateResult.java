package net.thevpc.nuts.runtime.standalone.workspace.cmd.update;

import net.thevpc.nuts.NUpdateResult;
import net.thevpc.nuts.NWorkspaceUpdateResult;

import java.util.ArrayList;
import java.util.List;

public class DefaultNWorkspaceUpdateResult implements NWorkspaceUpdateResult {

    private final NUpdateResult api;
    private final NUpdateResult runtime;
    private final List<NUpdateResult> extensions;
    private final List<NUpdateResult> artifacts;

    public DefaultNWorkspaceUpdateResult(NUpdateResult api, NUpdateResult runtime, List<NUpdateResult> extensions, List<NUpdateResult> components) {
        this.api = api;
        this.runtime = runtime;
        this.extensions = extensions;
        this.artifacts = components;
    }

    @Override
    public NUpdateResult getApi() {
        return api;
    }

    @Override
    public NUpdateResult getRuntime() {
        return runtime;
    }

    @Override
    public List<NUpdateResult> getExtensions() {
        return extensions;
    }

    @Override
    public List<NUpdateResult> getArtifacts() {
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
        for (NUpdateResult r : extensions) {
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
    public List<NUpdateResult> getUpdatable() {
        List<NUpdateResult> all = new ArrayList<>();
        if (api != null && api.isUpdatable()) {
            all.add(api);
        }
        if (runtime != null && runtime.isUpdatable()) {
            all.add(runtime);
        }
        for (NUpdateResult r : extensions) {
            if (r.isUpdatable()) {
                all.add(r);
            }
        }
        for (NUpdateResult r : artifacts) {
            if (r.isUpdatable()) {
                all.add(r);
            }
        }
        return all;
    }

    @Override
    public List<NUpdateResult> getAllResults() {
        List<NUpdateResult> all = new ArrayList<>();
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
        for (NUpdateResult r : extensions) {
            if (r.isUpdatable()) {
                c++;
            }
        }
        for (NUpdateResult r : artifacts) {
            if (r.isUpdatable()) {
                c++;
            }
        }
        return c;
    }
}

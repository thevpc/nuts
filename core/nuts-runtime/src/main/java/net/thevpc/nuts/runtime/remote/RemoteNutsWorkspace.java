package net.thevpc.nuts.runtime.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.AbstractNutsWorkspace;
import net.thevpc.nuts.runtime.bundles.ntalk.NTalkClient;

import java.util.Set;

public class RemoteNutsWorkspace extends AbstractNutsWorkspace {

    public RemoteNutsWorkspace(NutsWorkspaceInitInformation info) {
        super(info);
    }

    public NutsElement createCall(String commandName, NutsElement body) {
        try (NTalkClient cli = new NTalkClient()) {
            NutsElementFormat e = formats().element().setContentType(NutsContentType.JSON);
            NutsObjectElement q = e.builder().forObject()
                    .set("cmd", commandName)
                    .set("body", body).build();
            String json = e.setValue(q).format();
            String wsURL = config().options().getWorkspace();
            byte[] result = cli.request("nuts/ws:"+wsURL, json.getBytes());
            NutsObjectElement resultObject = e.parse(result, NutsObjectElement.class);
            boolean success = resultObject.get("success").primitive().getBoolean();
            if (success) {
                return resultObject.get("body");
            } else {
                //TODO mush deserialize exception
                throw new NutsException(this, "unable to call " + commandName);
            }
        }
    }

    public NutsElement createCall(String commandName, String callId, NutsElement body) {
        NutsElementBuilder e = formats().element().builder();
        return e.forObject()
                .set(
                        "cmd",
                        e.forString(commandName))
                .set("id", e.forString(callId))
                .set("body", body).build();
    }

    @Override
    public NutsSearchCommand search() {
        return new RemoteNutsSearchCommand(this);
    }

    @Override
    public NutsFetchCommand fetch() {
        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported fetch");
    }

    @Override
    public NutsDeployCommand deploy() {
        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported deploy");
    }

    @Override
    public NutsUndeployCommand undeploy() {
        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported undeploy");
    }

    @Override
    public NutsExecCommand exec() {
        return new RemoteNutsExecCommand(this);
    }

    @Override
    public NutsInstallCommand install() {
        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported install");
    }

    @Override
    public NutsUninstallCommand uninstall() {
        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported uninstall");
    }

    @Override
    public NutsUpdateCommand update() {
        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported update");
    }

    @Override
    public NutsPushCommand push() {
        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported push");
    }

    @Override
    public Set<NutsId> getCompanionIds() {
        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported companionIds");
    }

    @Override
    public NutsFilterManager filters() {
        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported filters");
    }

    @Override
    public NutsLogManager log() {
        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported log");
    }

    public <T> T remoteCall(NutsElement call, Class<T> expectedType) {
        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported remoteCall");
    }

}

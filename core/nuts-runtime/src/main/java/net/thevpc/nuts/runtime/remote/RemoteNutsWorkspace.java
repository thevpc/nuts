package net.thevpc.nuts.runtime.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.AbstractNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.xtra.ntalk.NTalkClient;


public abstract class RemoteNutsWorkspace extends AbstractNutsWorkspace {

    public RemoteNutsWorkspace() {
    }

    public NutsElement createCall(String commandName, NutsElement body,NutsSession session) {
        try (NTalkClient cli = new NTalkClient()) {
            NutsElements e = NutsElements.of(session).json();
            NutsObjectElement q = e.ofObject()
                    .set("cmd", commandName)
                    .set("body", body).build();
            NutsString json = e.setValue(q).format();
            String wsURL = session.boot().getBootOptions().getWorkspace().orNull();
            byte[] result = cli.request("nuts/ws:"+wsURL, json.toString().getBytes());
            NutsObjectElement resultObject = e.parse(result, NutsObjectElement.class);
            NutsElements prv = NutsElements.of(session);
            boolean success = resultObject.getBoolean("success").get(session);
            if (success) {
                return resultObject.get("body").orNull();
            } else {
                //TODO mush deserialize exception
                throw new NutsException(session, NutsMessage.cstyle("unable to call %s",
                        NutsTexts.of(session).ofStyled(commandName,NutsTextStyle.primary1())));
            }
        }
    }

    public NutsElement createCall(String commandName, String callId, NutsElement body,NutsSession session) {
        NutsElements e = NutsElements.of(session);
        return e.ofObject()
                .set(
                        "cmd",
                        e.ofString(commandName))
                .set("id", e.ofString(callId))
                .set("body", body).build();
    }

//    @Override
//    public NutsSearchCommand search() {
//        return new RemoteNutsSearchCommand(this);
//    }
//
//    @Override
//    public NutsFetchCommand fetch() {
//        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported fetch");
//    }
//
//    @Override
//    public NutsDeployCommand deploy() {
//        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported deploy");
//    }
//
//    @Override
//    public NutsUndeployCommand undeploy() {
//        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported undeploy");
//    }
//
//    @Override
//    public NutsExecCommand exec() {
//        return new RemoteNutsExecCommand(this);
//    }
//
//    @Override
//    public NutsInstallCommand install() {
//        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported install");
//    }
//
//    @Override
//    public NutsUninstallCommand uninstall() {
//        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported uninstall");
//    }
//
//    @Override
//    public NutsUpdateCommand update() {
//        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported update");
//    }
//
//    @Override
//    public NutsPushCommand push() {
//        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported push");
//    }
//
//    @Override
//    public Set<NutsId> getCompanionIds() {
//        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported companionIds");
//    }
//
//    @Override
//    public NutsFilters filters() {
//        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported filters");
//    }
//
//    @Override
//    public NutsLogManager log() {
//        throw new NutsUnsupportedOperationException(configManager.getWorkspace(), "not yet supported log");
//    }
//
    public <T> T remoteCall(NutsElement call, Class<T> expectedType) {
        throw new NutsUnsupportedOperationException(null, NutsMessage.cstyle("not yet supported remoteCall"));
    }

}

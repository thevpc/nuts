package net.thevpc.nuts.runtime.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.runtime.standalone.workspace.AbstractNWorkspace;
import net.thevpc.nuts.runtime.standalone.xtra.ntalk.NTalkClient;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;


public abstract class RemoteNWorkspace extends AbstractNWorkspace {

    public RemoteNWorkspace() {
    }

    public NElement createCall(String commandName, NElement body, NSession session) {
        try (NTalkClient cli = new NTalkClient()) {
            NElements e = NElements.of(session).json();
            NObjectElement q = e.ofObject()
                    .set("cmd", commandName)
                    .set("body", body).build();
            NString json = e.setValue(q).format();
            String wsURL = session.boot().getBootOptions().getWorkspace().orNull();
            byte[] result = cli.request("nuts/ws:"+wsURL, json.toString().getBytes());
            NObjectElement resultObject = e.parse(result, NObjectElement.class);
            NElements prv = NElements.of(session);
            boolean success = resultObject.getBoolean("success").get(session);
            if (success) {
                return resultObject.get("body").orNull();
            } else {
                //TODO mush deserialize exception
                throw new NException(session, NMsg.ofCstyle("unable to call %s",
                        NTexts.of(session).ofStyled(commandName, NTextStyle.primary1())));
            }
        }
    }

    public NElement createCall(String commandName, String callId, NElement body, NSession session) {
        NElements e = NElements.of(session);
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
    public <T> T remoteCall(NElement call, Class<T> expectedType) {
        throw new NUnsupportedOperationException(null, NMsg.ofPlain("not yet supported remoteCall"));
    }

}

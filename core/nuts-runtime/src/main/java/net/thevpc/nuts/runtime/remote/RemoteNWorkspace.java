package net.thevpc.nuts.runtime.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.elem.*;

import net.thevpc.nuts.runtime.standalone.workspace.AbstractNWorkspace;
import net.thevpc.nuts.runtime.standalone.xtra.ntalk.NTalkClient;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NMsg;


public abstract class RemoteNWorkspace extends AbstractNWorkspace {

    public RemoteNWorkspace(NBootOptionsInfo info) {
        super(info);
    }

    public NElement createCall(String commandName, NElement body) {
        try (NTalkClient cli = new NTalkClient()) {
            NObjectElement q = NElement.ofObjectBuilder()
                    .set("cmd", commandName)
                    .set("body", body).build();
            String json = NElementWriter.ofJson().toString(q);
            String wsURL = NWorkspace.of().getBootOptions().getWorkspace().orNull();
            byte[] result = cli.request("nuts/ws:"+wsURL, json.getBytes());
            NObjectElement resultObject = NElementParser.ofJson().parse(result, NObjectElement.class);
            NElements prv = NElements.of();
            boolean success = resultObject.getBooleanValue("success").get();
            if (success) {
                return resultObject.get("body").orNull();
            } else {
                //TODO mush deserialize exception
                throw new NException(NMsg.ofC("unable to call %s",
                        NText.ofStyled(commandName, NTextStyle.primary1())));
            }
        }
    }

    public NElement createCall(String commandName, String callId, NElement body) {
        return NElement.ofObjectBuilder()
                .set(
                        "cmd",
                        NElement.ofString(commandName))
                .set("id", NElement.ofString(callId))
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
        throw new NUnsupportedOperationException(NMsg.ofPlain("not yet supported remoteCall"));
    }

}

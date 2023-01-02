/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NOutMemoryStream;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNUninstallCommand extends AbstractNUninstallCommand {

    public DefaultNUninstallCommand(NSession ws) {
        super(ws);
    }

    @Override
    public NUninstallCommand run() {
        checkSession();
        NWorkspaceUtils.of(getSession()).checkReadOnly();
        checkSession();
        NSession session = getSession();
        NWorkspaceExt dws = NWorkspaceExt.of(session.getWorkspace());
        NWorkspaceSecurityManager.of(session).checkAllowed(NConstants.Permissions.UNINSTALL, "uninstall");
        List<NDefinition> defs = new ArrayList<>();
        List<NId> nutsIds = this.getIds();
        NAssert.requireNonBlank(nutsIds, "packages to uninstall", session);
        List<NId> installed = new ArrayList<>();
        for (NId id : nutsIds) {
            List<NDefinition> resultDefinitions = NSearchCommand.of(session).addId(id)
                    .setInstallStatus(NInstallStatusFilters.of(session).byInstalled(true))
                    .setSession(session.copy().setTransitive(false))
                    .setOptional(false).setEffective(true)
                    .setContent(true)//include content so that we can remove it by calling executor
                    .setDependencies(true)//include dependencies so that we can remove it by calling executor
                    .addScope(NDependencyScopePattern.RUN)
                    .getResultDefinitions().toList();
            resultDefinitions.removeIf(it -> !it.getInstallInformation().get(session).isInstalledOrRequired());
            if (resultDefinitions.isEmpty()) {
                throw new NIllegalArgumentException(getSession(), NMsg.ofCstyle("not installed : %s", id));
            }
            installed.addAll(resultDefinitions.stream().map(NDefinition::getId).collect(Collectors.toList()));
            defs.addAll(resultDefinitions);
        }
        NOutMemoryStream mout = NOutMemoryStream.of(session);
        printList(mout, "installed", "uninstalled", installed);
        mout.println("should we proceed uninstalling ?");
        NMsg cancelMessage = NMsg.ofCstyle("uninstall cancelled : %s", defs.stream()
                .map(NDefinition::getId)
                .map(NId::getFullName)
                .collect(Collectors.joining(", ")));
        if (!defs.isEmpty() && !getSession().getTerminal().ask()
                .resetLine()
                .setSession(session)
                .forBoolean(NMsg.ofNtf(mout.toString()))
                .setDefaultValue(true)
                .setCancelMessage(cancelMessage)
                .getBooleanValue()) {
            throw new NCancelException(getSession(), cancelMessage);
        }

        for (NDefinition def : defs) {
            NWorkspaceExt.of(ws).uninstallImpl(def, getArgs().toArray(new String[0]), true, true, isErase(),true, session);
        }
        return this;
    }

    private void printList(NOutStream out, String skind, String saction, List<NId> all) {
        if (all.size() > 0) {
            if (session.isPlainOut()) {
                NTexts text = NTexts.of(session);
                NText kind = text.ofStyled(skind, NTextStyle.primary2());
                NText action =
                        text.ofStyled(saction,
                                saction.equals("set as default") ? NTextStyle.primary3() :
                                        saction.equals("ignored") ? NTextStyle.pale() :
                                                NTextStyle.primary1()
                        );
                NSession session = getSession();
                NTextBuilder msg = NTexts.of(getSession()).ofBuilder();
                msg.append("the following ")
                        .append(kind).append(" ").append((all.size() > 1 ? "artifacts are" : "artifact is"))
                        .append(" going to be ").append(action).append(" : ")
                        .appendJoined(
                                NTexts.of(session).ofPlain(", "),
                                all.stream().map(x
                                                -> NTexts.of(session).ofText(
                                                x.builder().build()
                                        )
                                ).collect(Collectors.toList())
                        );
                out.resetLine().println(msg);
            } else {
                NElements elem = NElements.of(session);
                session.eout().add(elem.ofObject()
                        .set("command", "warning")
                        .set("artifact-kind", skind)
                        .set("action-warning", saction)
                        .set("artifacts", elem.ofArray().addAll(
                                all.stream().map(Object::toString).toArray(String[]::new)
                        ).build())
                        .build()
                );
            }
        }
    }
}

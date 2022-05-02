/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsMemoryPrintStream;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.text.NutsText;
import net.thevpc.nuts.text.NutsTextBuilder;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNutsUninstallCommand extends AbstractNutsUninstallCommand {

    public DefaultNutsUninstallCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsUninstallCommand run() {
        checkSession();
        NutsWorkspaceUtils.of(getSession()).checkReadOnly();
        checkSession();
        NutsSession session = getSession();
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(session.getWorkspace());
        session.security().setSession(getSession()).checkAllowed(NutsConstants.Permissions.UNINSTALL, "uninstall");
        List<NutsDefinition> defs = new ArrayList<>();
        List<NutsId> nutsIds = this.getIds();
        if (nutsIds.size() == 0) {
            throw new NutsExecutionException(getSession(), NutsMessage.cstyle("missing packages to uninstall"), 1);
        }
        List<NutsId> installed = new ArrayList<>();
        for (NutsId id : nutsIds) {
            List<NutsDefinition> resultDefinitions = session.search().addId(id)
                    .setInstallStatus(NutsInstallStatusFilters.of(session).byInstalled(true))
                    .setSession(session.copy().setTransitive(false))
                    .setOptional(false).setEffective(true)
                    .setContent(true)//include content so that we can remove it by calling executor
                    .setDependencies(true)//include dependencies so that we can remove it by calling executor
                    .addScope(NutsDependencyScopePattern.RUN)
                    .getResultDefinitions().toList();
            resultDefinitions.removeIf(it -> !it.getInstallInformation().get(session).isInstalledOrRequired());
            if (resultDefinitions.isEmpty()) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("not installed : %s", id));
            }
            installed.addAll(resultDefinitions.stream().map(NutsDefinition::getId).collect(Collectors.toList()));
            defs.addAll(resultDefinitions);
        }
        NutsMemoryPrintStream mout = NutsMemoryPrintStream.of(session);
        printList(mout, "installed", "uninstalled", installed);
        mout.println("should we proceed uninstalling ?");
        NutsMessage cancelMessage = NutsMessage.cstyle("uninstall cancelled : %s", defs.stream()
                .map(NutsDefinition::getId)
                .map(NutsId::getFullName)
                .collect(Collectors.joining(", ")));
        if (!defs.isEmpty() && !getSession().getTerminal().ask()
                .resetLine()
                .setSession(session)
                .forBoolean(mout.toString())
                .setDefaultValue(true)
                .setCancelMessage(cancelMessage)
                .getBooleanValue()) {
            throw new NutsUserCancelException(getSession(), cancelMessage);
        }

        for (NutsDefinition def : defs) {
            NutsWorkspaceExt.of(ws).uninstallImpl(def, getArgs().toArray(new String[0]), true, true, isErase(),true, session);
        }
        return this;
    }

    private void printList(NutsPrintStream out, String skind, String saction, List<NutsId> all) {
        if (all.size() > 0) {
            if (session.isPlainOut()) {
                NutsTexts text = NutsTexts.of(session);
                NutsText kind = text.ofStyled(skind, NutsTextStyle.primary2());
                NutsText action =
                        text.ofStyled(saction,
                                saction.equals("set as default") ? NutsTextStyle.primary3() :
                                        saction.equals("ignored") ? NutsTextStyle.pale() :
                                                NutsTextStyle.primary1()
                        );
                NutsSession session = getSession();
                NutsTextBuilder msg = NutsTexts.of(getSession()).builder();
                msg.append("the following ")
                        .append(kind).append(" ").append((all.size() > 1 ? "artifacts are" : "artifact is"))
                        .append(" going to be ").append(action).append(" : ")
                        .appendJoined(
                                NutsTexts.of(session).ofPlain(", "),
                                all.stream().map(x
                                                -> NutsTexts.of(session).toText(
                                                x.builder().build()
                                        )
                                ).collect(Collectors.toList())
                        );
                out.resetLine().println(msg);
            } else {
                NutsElements elem = NutsElements.of(session);
                session.eout().add(elem.ofObject()
                        .set("command", "warning")
                        .set("artifact-kind", skind)
                        .set("action-warning", saction)
                        .set("artifacts", elem.ofArray().addAll(
                                all.stream().map(x -> x.toString()).toArray(String[]::new)
                        ).build())
                        .build()
                );
            }
        }
    }
}

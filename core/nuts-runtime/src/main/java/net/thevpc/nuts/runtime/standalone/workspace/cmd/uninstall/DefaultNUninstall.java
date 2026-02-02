/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall;

import net.thevpc.nuts.command.NSearch;
import net.thevpc.nuts.command.NUninstall;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDefinitionFilters;
import net.thevpc.nuts.artifact.NDependencyFilters;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.install.*;
import net.thevpc.nuts.security.NSecurityManager;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * type: Command Class
 *
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNUninstall extends AbstractNUninstall {

    public DefaultNUninstall() {
        super();
    }

    @Override
    public NUninstall run() {
        NWorkspaceUtils.of().checkReadOnly();
        NSecurityManager.of().checkAllowed(NConstants.Permissions.UNINSTALL, "uninstall");
        InstallIdList list = new InstallIdList();
        List<NId> nutsIds = this.getIds();
        NAssert.requireNonBlank(nutsIds, "packages to uninstall");
        List<NId> installed = new ArrayList<>();
        List<InstallIdInfo> infos = new ArrayList<>();
        InstallHelper h = new InstallHelper((DefaultNWorkspace) NWorkspaceExt.of(), list, false, args, conditionalArguments);
        for (NId id : nutsIds) {
            List<NDefinition> resultDefinitions = NSearch.of()
                    .setTransitive(false)
                    .addId(id)
                    .setDefinitionFilter(NDefinitionFilters.of().byInstalled(true))
                    .setDependencyFilter(NDependencyFilters.of().byRunnable())
                    .getResultDefinitions()
                    .distinct()
                    .toList();
            resultDefinitions.removeIf(it -> !it.getInstallInformation().get().isInstalledOrRequired());
            if (resultDefinitions.isEmpty()) {
                throw new NIllegalArgumentException(NMsg.ofC("not installed : %s", id));
            }
            for (NDefinition resultDefinition : resultDefinitions) {
                InstallIdInfo uu = list.addAsUninstalled(resultDefinition.getId(), new InstallFlags());
                uu.cacheItem = h.getCache(resultDefinition.getId());
                uu.cacheItem.revalidate(resultDefinition);
                installed.add(resultDefinition.getId());
                infos.add(uu);
            }
        }
        NMemoryPrintStream mout = NMemoryPrintStream.of();
        printList(mout, "installed", "uninstalled", installed);
        mout.println("should we proceed uninstalling ?");
        NMsg cancelMessage = NMsg.ofC("uninstall cancelled : %s", installed.stream()
                .map(NId::getFullName)
                .collect(Collectors.joining(", ")));
        if (!installed.isEmpty() && !NIn.ask()
                .forBoolean(NMsg.ofNtf(mout.toString()))
                .setDefaultValue(true)
                .setCancelMessage(cancelMessage)
                .getBooleanValue()) {
            throw new NCancelException(cancelMessage);
        }
        for (InstallIdInfo def : infos) {
            h.uninstallImpl(def, true, true, isErase(), true);
        }
        return this;
    }

    private void printList(NPrintStream out, String skind, String saction, List<NId> all) {
        if (all.size() > 0) {
            NSession session = NSession.of();
            if (NOut.isPlain()) {
                NTexts text = NTexts.of();
                NText kind = text.ofStyled(skind, NTextStyle.primary2());
                NText action =
                        text.ofStyled(saction,
                                saction.equals("set as default") ? NTextStyle.primary3() :
                                        saction.equals("ignored") ? NTextStyle.pale() :
                                        NTextStyle.primary1()
                        );
                NTextBuilder msg = NTextBuilder.of();
                msg.append("the following ")
                        .append(kind).append(" ").append((all.size() > 1 ? "artifacts are" : "artifact is"))
                        .append(" going to be ").append(action).append(" : ")
                        .appendJoined(
                                NText.ofPlain(", "),
                                all.stream().map(x
                                                -> NText.of(
                                                x.builder().build()
                                        )
                                ).collect(Collectors.toList())
                        );
                out.println(msg);
            } else {
                session.eout().add(NElement.ofObjectBuilder()
                        .set("command", "warning")
                        .set("artifact-kind", skind)
                        .set("action-warning", saction)
                        .set("artifacts", NElement.ofArrayBuilder().addAll(
                                all.stream().map(Object::toString).toArray(String[]::new)
                        ).build())
                        .build()
                );
            }
        }
    }
}

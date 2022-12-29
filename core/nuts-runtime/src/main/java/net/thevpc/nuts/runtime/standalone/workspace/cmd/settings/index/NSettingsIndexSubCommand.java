/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.index;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thevpc
 */
public class NSettingsIndexSubCommand extends AbstractNSettingsSubCommand {

    @Override
    public boolean exec(NCommandLine cmdLine, Boolean autoSave, NSession session) {
        String name = "settings update stats";
        NArgument a;
        if (cmdLine.next("update stats").isPresent()) {
            List<String> repos = new ArrayList<>();
            while (cmdLine.hasNext()) {
                repos.add(cmdLine.next().flatMap(NValue::asString).get(session));
            }
            updateStatistics(session, repos.toArray(new String[0]));
            cmdLine.setCommandName(name).throwUnexpectedArgument();
            return true;
        } else {
            return false;
        }
    }

    private void updateStatistics(NSession session, String[] repos) {
        NWorkspaceConfigManager cfg = session.config();
        NUpdateStatisticsCommand cmd = NUpdateStatisticsCommand.of(session);
        for (String repo : repos) {
            cmd.add(repo);
        }
        cmd.run();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.index;

import net.thevpc.nuts.NConfigs;
import net.thevpc.nuts.NLiteral;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NUpdateStatisticsCommand;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thevpc
 */
public class NSettingsIndexSubCommand extends AbstractNSettingsSubCommand {

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave, NSession session) {
        String name = "settings update stats";
        if (cmdLine.next("update stats").isPresent()) {
            List<String> repos = new ArrayList<>();
            while (cmdLine.hasNext()) {
                repos.add(cmdLine.next().flatMap(NLiteral::asString).get(session));
            }
            updateStatistics(session, repos.toArray(new String[0]));
            cmdLine.setCommandName(name).throwUnexpectedArgument();
            return true;
        } else {
            return false;
        }
    }

    private void updateStatistics(NSession session, String[] repos) {
        NConfigs cfg = NConfigs.of(session);
        NUpdateStatisticsCommand cmd = NUpdateStatisticsCommand.of(session);
        for (String repo : repos) {
            cmd.add(repo);
        }
        cmd.run();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.index;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.NUpdateStatsCmd;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thevpc
 */
public class NSettingsIndexSubCommand extends AbstractNSettingsSubCommand {
    public NSettingsIndexSubCommand(NWorkspace workspace) {
        super(workspace);
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave) {
        String name = "settings update stats";
        if (cmdLine.next("update stats").isPresent()) {
            List<String> repos = new ArrayList<>();
            while (cmdLine.hasNext()) {
                repos.add(cmdLine.next().flatMap(NLiteral::asStringValue).get());
            }
            updateStatistics(repos.toArray(new String[0]));
            cmdLine.setCommandName(name).throwUnexpectedArgument();
            return true;
        } else {
            return false;
        }
    }

    private void updateStatistics(String[] repos) {
        NUpdateStatsCmd cmd = NUpdateStatsCmd.of();
        for (String repo : repos) {
            cmd.add(repo);
        }
        cmd.run();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.index;

import net.thevpc.nuts.command.NUpdateStats;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class NSettingsIndexSubCommand extends AbstractNSettingsSubCommand {
    public NSettingsIndexSubCommand() {
        super();
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave) {
        String name = "settings update stats";
        if (cmdLine.next("update stats").isPresent()) {
            List<String> repos = new ArrayList<>();
            while (cmdLine.hasNext()) {
                repos.add(cmdLine.next().get().image());
            }
            updateStatistics(repos.toArray(new String[0]));
            cmdLine.setCommandName(name).throwUnexpectedArgument();
            return true;
        } else {
            return false;
        }
    }

    private void updateStatistics(String[] repos) {
        NUpdateStats cmd = NUpdateStats.of();
        for (String repo : repos) {
            cmd.add(repo);
        }
        cmd.run();
    }
}

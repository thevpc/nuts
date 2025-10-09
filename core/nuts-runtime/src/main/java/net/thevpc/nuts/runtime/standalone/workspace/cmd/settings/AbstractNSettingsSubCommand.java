/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings;

import net.thevpc.nuts.util.NScorableContext;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNSettingsSubCommand implements NSettingsSubCommand {

    public AbstractNSettingsSubCommand() {
    }

    @Override
    public int getScore(NScorableContext criteria) {
        return DEFAULT_SCORE;
    }
}

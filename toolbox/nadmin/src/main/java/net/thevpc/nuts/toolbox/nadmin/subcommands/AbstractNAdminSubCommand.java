/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nadmin.subcommands;

import net.thevpc.nuts.toolbox.nadmin.NAdminSubCommand;
import net.thevpc.nuts.*;

import java.io.PrintStream;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNAdminSubCommand implements NAdminSubCommand {

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DEFAULT_SUPPORT;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands;

import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.NutsSettingsSubCommand;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNutsSettingsSubCommand implements NutsSettingsSubCommand {

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DEFAULT_SUPPORT;
    }
}

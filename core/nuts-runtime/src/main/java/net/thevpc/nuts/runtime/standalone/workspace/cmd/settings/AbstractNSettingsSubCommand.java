/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings;

import net.thevpc.nuts.NCallableSupport;
import net.thevpc.nuts.spi.NSupportLevelContext;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNSettingsSubCommand implements NSettingsSubCommand {

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        return NCallableSupport.DEFAULT_SUPPORT;
    }
}

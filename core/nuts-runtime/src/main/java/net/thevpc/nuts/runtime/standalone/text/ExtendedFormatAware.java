/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.runtime.standalone.io.terminal.NutsTerminalModeOp;

/**
 *
 * @author thevpc
 */
public interface ExtendedFormatAware {
    NutsTerminalModeOp getModeOp();
    ExtendedFormatAware convert(NutsTerminalModeOp other);
}

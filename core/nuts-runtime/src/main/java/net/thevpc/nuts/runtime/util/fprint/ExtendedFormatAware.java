/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.util.fprint;

import net.thevpc.nuts.runtime.io.NutsTerminalModeOp;

/**
 *
 * @author vpc
 */
public interface ExtendedFormatAware {
    NutsTerminalModeOp getModeOp();
    ExtendedFormatAware convert(NutsTerminalModeOp other);
}

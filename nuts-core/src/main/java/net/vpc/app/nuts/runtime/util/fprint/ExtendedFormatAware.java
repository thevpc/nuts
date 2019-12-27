/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.util.fprint;

import net.vpc.app.nuts.NutsTerminalMode;
import net.vpc.app.nuts.runtime.io.NutsTerminalModeOp;

import java.io.OutputStream;

/**
 *
 * @author vpc
 */
public interface ExtendedFormatAware {
    NutsTerminalModeOp getModeOp();
    ExtendedFormatAware convert(NutsTerminalModeOp other);
}

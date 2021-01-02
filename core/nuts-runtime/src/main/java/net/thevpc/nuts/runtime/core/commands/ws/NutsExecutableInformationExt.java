/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.commands.ws;

import net.thevpc.nuts.NutsExecutableInformation;

/**
 * @author thevpc
 */
public interface NutsExecutableInformationExt extends NutsExecutableInformation {

    void execute();

    void dryExecute();

}

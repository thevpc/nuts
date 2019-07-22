/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.spi;

import net.vpc.app.nuts.NutsExecutableInformation;

/**
 * @author vpc
 */
public interface NutsExecutableInformationExt extends NutsExecutableInformation {

    void execute();

    void dryExecute();

}

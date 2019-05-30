/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.spi;

import net.vpc.app.nuts.NutsExecutableInfo;

/**
 *
 * @author vpc
 */
public interface NutsExecutableInfoExt extends NutsExecutableInfo {

    void execute();
    
}

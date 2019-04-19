/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

/**
 *
 * @author vpc
 */
public enum NutsBootContextType {
    /**
     * Boot-Time requested configuration
     */
    BOOT,

    /**
     * Run-time used configuration
     */
    RUNTIME,
    
    /**
     * Run-time configuration (may be different from Boot time as configuration changes)
     */
    CONFIG,
}

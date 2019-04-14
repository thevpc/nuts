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
public enum NutsExecutableType {
    /**
     * internal command is one of : version, info, install, uninstall,check-updates,license, help, exec
     */
    INTERNAL, 
    /**
     * workspace configured command using {@link NutsWorkspaceConfigManager#}
     */
    ALIAS, 
    COMPONENT, 
    SYSTEM
}

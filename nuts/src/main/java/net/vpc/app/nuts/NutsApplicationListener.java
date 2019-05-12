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
public interface NutsApplicationListener {

    default void onRunApplication(NutsApplicationContext applicationContext){
        
    }

    default void onInstallApplication(NutsApplicationContext applicationContext){
        
    }

    default void onUpdateApplication(NutsApplicationContext applicationContext){
        
    }

    default void onUninstallApplication(NutsApplicationContext applicationContext){
        
    }

    default NutsApplicationContext createApplicationContext(NutsWorkspace ws, String[] args){
        return null;
    }
}

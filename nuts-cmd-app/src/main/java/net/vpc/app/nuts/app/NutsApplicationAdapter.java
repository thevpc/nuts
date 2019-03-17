/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.app;

import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public class NutsApplicationAdapter implements NutsApplicationListener {

    @Override
    public void onRunApplication(NutsApplicationContext applicationContext) {
    }

    @Override
    public void onInstallApplication(NutsApplicationContext applicationContext) {
    }

    @Override
    public void onUpdateApplication(NutsApplicationContext applicationContext) {
    }

    @Override
    public void onUninstallApplication(NutsApplicationContext applicationContext) {
    }

    @Override
    public NutsApplicationContext createApplicationContext(NutsWorkspace ws, String[] args) {
        return null;
    }

}

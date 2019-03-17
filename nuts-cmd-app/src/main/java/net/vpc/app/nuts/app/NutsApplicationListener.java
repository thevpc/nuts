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
public interface NutsApplicationListener {

    void onRunApplication(NutsApplicationContext applicationContext);

    void onInstallApplication(NutsApplicationContext applicationContext);

    void onUpdateApplication(NutsApplicationContext applicationContext);

    void onUninstallApplication(NutsApplicationContext applicationContext);

    NutsApplicationContext createApplicationContext(NutsWorkspace ws, String[] args);
}

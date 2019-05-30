/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsComponent;

/**
 *
 * @author vpc
 */
public interface NAdminSubCommand extends NutsComponent<Object> {

    /**
     * true if processed
     *
     * @param cmdLine
     * @param config
     * @param autoSave
     * @param context
     * @return
     */
    boolean exec(NutsCommand cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context);
}

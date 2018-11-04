/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.NutsComponent;
import net.vpc.common.commandline.CommandLine;

/**
 *
 * @author vpc
 */
public interface ConfigSubCommand extends NutsComponent<Object> {

    /**
     * true if processed
     *
     * @param cmdLine
     * @param autoSave
     * @param context
     * @param config
     * @return
     */
    boolean exec(CommandLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context);
}

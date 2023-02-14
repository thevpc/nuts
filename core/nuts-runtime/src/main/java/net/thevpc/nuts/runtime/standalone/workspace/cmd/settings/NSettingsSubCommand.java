/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NComponent;

/**
 *
 * @author thevpc
 */
public interface NSettingsSubCommand extends NComponent {

    /**
     * execute command and return true.
     * If the command is not supported return false.
     *
     * @param cmdLine  command line
     * @param autoSave auto save
     * @param session application context
     * @return true if the sub command is supported and executed
     */
    boolean exec(NCmdLine cmdLine, Boolean autoSave, NSession session);
}

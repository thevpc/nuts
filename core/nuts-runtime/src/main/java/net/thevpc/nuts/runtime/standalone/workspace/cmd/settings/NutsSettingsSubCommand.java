/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings;

import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsComponent;

/**
 *
 * @author thevpc
 */
public interface NutsSettingsSubCommand extends NutsComponent {

    /**
     * execute command and return true.
     * If the command is not supported return false.
     *
     * @param cmdLine  command line
     * @param autoSave auto save
     * @param session application context
     * @return true if the sub command is supported and executed
     */
    boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session);
}

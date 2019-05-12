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
public interface NutsWorkspaceCommand {

    /**
     * 
     * @param autoCreate
     * @return 
     */
    NutsSession getSession(boolean autoCreate);
    
    NutsSession getSession();

    NutsWorkspaceCommand session(NutsSession session);

    NutsWorkspaceCommand setSession(NutsSession session);

    NutsWorkspaceCommand configure(String... args);
    
    NutsWorkspaceCommand configure(NutsCommandLine commandLine, boolean skipIgnored);
    
    boolean configureFirst(NutsCommandLine commandLine);

    NutsWorkspaceCommand run();


}

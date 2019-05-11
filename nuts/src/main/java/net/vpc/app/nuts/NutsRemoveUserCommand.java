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
public interface NutsRemoveUserCommand extends NutsWorkspaceCommand{

    String getLogin();

    NutsRemoveUserCommand login(String login);

    NutsRemoveUserCommand setLogin(String login);

    @Override
    NutsRemoveUserCommand session(NutsSession session);

    @Override
    NutsRemoveUserCommand setSession(NutsSession session);

    @Override
    NutsRemoveUserCommand parseOptions(String... args);

    @Override
    NutsRemoveUserCommand run();
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.util.Collection;

/**
 *
 * @author vpc
 */
public interface NutsAddUserCommand extends NutsWorkspaceCommand {

    NutsAddUserCommand addGroup(String group);

    NutsAddUserCommand addGroups(String... groups);

    NutsAddUserCommand addGroups(Collection<String> groups);

    NutsAddUserCommand addRight(String right);

    NutsAddUserCommand addRights(String... rights);

    NutsAddUserCommand addRights(Collection<String> rights);

    String[] getGroups();

    String getLogin();

    String getCredentials();

    String getRemoteIdentity();

    String[] getRights();

    NutsAddUserCommand group(String group);

    NutsAddUserCommand groups(String... groups);

    NutsAddUserCommand groups(Collection<String> groups);

    NutsAddUserCommand removeGroups(String... groups);

    NutsAddUserCommand removeGroups(Collection<String> groups);

    NutsAddUserCommand removeRights(String... rights);

    NutsAddUserCommand removeRights(Collection<String> rights);

    NutsAddUserCommand right(String right);

    NutsAddUserCommand rights(String... rights);

    NutsAddUserCommand rights(Collection<String> rights);

    NutsAddUserCommand login(String login);

    NutsAddUserCommand setLogin(String login);

    NutsAddUserCommand credentials(String password);

    NutsAddUserCommand setCredentials(String password);

    NutsAddUserCommand remoteIdentity(String remoteIdentity);

    NutsAddUserCommand setRemoteIdentity(String remoteIdentity);

    //
    // NutsWorkspaceCommand overridden methods
    //    

    @Override
    NutsAddUserCommand session(NutsSession session);

    @Override
    NutsAddUserCommand setSession(NutsSession session);

    @Override
    NutsAddUserCommand parseOptions(String... args);

    @Override
    NutsAddUserCommand run();
}

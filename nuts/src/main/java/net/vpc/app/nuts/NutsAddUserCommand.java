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
public interface NutsAddUserCommand extends NutsWorkspaceCommand{

    NutsAddUserCommand addGroup(String group);

    NutsAddUserCommand addGroups(String... groups);

    NutsAddUserCommand addGroups(Collection<String> groups);

    NutsAddUserCommand addRight(String right);

    NutsAddUserCommand addRights(String... rights);

    NutsAddUserCommand addRights(Collection<String> rights);

    NutsAddUserCommand force();

    NutsAddUserCommand force(boolean force);

    String[] getGroups();

    String getLogin();

    String getCredentials();

    String getRemoteIdentity();

    String[] getRights();

    NutsSession getSession();

    NutsAddUserCommand group(String group);

    NutsAddUserCommand groups(String... groups);

    NutsAddUserCommand groups(Collection<String> groups);

    boolean isForce();

    boolean isTrace();

    NutsAddUserCommand removeGroups(String... groups);

    NutsAddUserCommand removeGroups(Collection<String> groups);

    NutsAddUserCommand removeRights(String... rights);

    NutsAddUserCommand removeRights(Collection<String> rights);

    NutsAddUserCommand right(String right);

    NutsAddUserCommand rights(String... rights);

    NutsAddUserCommand rights(Collection<String> rights);

    NutsAddUserCommand session(NutsSession session);

    NutsAddUserCommand setForce(boolean force);

    NutsAddUserCommand login(String login);

    NutsAddUserCommand setLogin(String login);

    NutsAddUserCommand credentials(String password);

    NutsAddUserCommand setCredentials(String password);

    NutsAddUserCommand remoteIdentity(String remoteIdentity);

    NutsAddUserCommand setRemoteIdentity(String remoteIdentity);

    NutsAddUserCommand setSession(NutsSession session);

    NutsAddUserCommand setTrace(boolean trace);

    NutsAddUserCommand trace();

    NutsAddUserCommand trace(boolean trace);

    NutsAddUserCommand run();
    
    NutsAddUserCommand parseOptions(String... args);

}

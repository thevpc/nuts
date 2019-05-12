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
public interface NutsUpdateUserCommand extends NutsWorkspaceCommand {

    NutsUpdateUserCommand removeGroup(String group);

    NutsUpdateUserCommand addGroup(String group);

    NutsUpdateUserCommand undoAddGroup(String group);

    NutsUpdateUserCommand addGroups(String... groups);

    NutsUpdateUserCommand undoAddGroups(String... groups);

    NutsUpdateUserCommand addGroups(Collection<String> groups);

    NutsUpdateUserCommand undoAddGroups(Collection<String> groups);

    NutsUpdateUserCommand removeRight(String right);

    NutsUpdateUserCommand addRight(String right);

    NutsUpdateUserCommand undoAddRight(String right);

    NutsUpdateUserCommand addRights(String... rights);

    NutsUpdateUserCommand undoAddRights(String... rights);

    NutsUpdateUserCommand addRights(Collection<String> rights);

    NutsUpdateUserCommand undoAddRights(Collection<String> rights);

    String[] getAddGroups();

    String[] getRemoveGroups();

    String getCredentials();

    String getOldCredentials();

    String getRemoteIdentity();

    String[] getAddRights();

    String[] getRemoveRights();

    NutsUpdateUserCommand removeGroups(String... groups);

    NutsUpdateUserCommand undoRemoveGroups(String... groups);

    NutsUpdateUserCommand removeGroups(Collection<String> groups);

    NutsUpdateUserCommand undoRemoveGroups(Collection<String> groups);

    NutsUpdateUserCommand removeRights(String... rights);

    NutsUpdateUserCommand undoRemoveRights(String... rights);

    NutsUpdateUserCommand removeRights(Collection<String> rights);

    NutsUpdateUserCommand undoRemoveRights(Collection<String> rights);

    NutsUpdateUserCommand credentials(String password);

    NutsUpdateUserCommand setCredentials(String password);

    NutsUpdateUserCommand oldCredentials(String password);

    NutsUpdateUserCommand setOldCredentials(String oldCredentials);

    NutsUpdateUserCommand remoteIdentity(String remoteIdentity);

    NutsUpdateUserCommand setRemoteIdentity(String remoteIdentity);

    String getLogin();

    NutsUpdateUserCommand login(String login);

    NutsUpdateUserCommand setLogin(String login);

    boolean isResetRights();

    NutsUpdateUserCommand resetRights();

    NutsUpdateUserCommand resetRights(boolean resetRights);

    NutsUpdateUserCommand setResetRights(boolean resetRights);

    boolean isResetGroups();

    NutsUpdateUserCommand resetGroups();

    NutsUpdateUserCommand resetGroups(boolean resetGroups);

    NutsUpdateUserCommand setResetGroups(boolean resetGroups);

    @Override
    NutsUpdateUserCommand session(NutsSession session);

    @Override
    NutsUpdateUserCommand setSession(NutsSession session);

    @Override
    NutsUpdateUserCommand configure(String... args);

    @Override
    NutsUpdateUserCommand run();
}

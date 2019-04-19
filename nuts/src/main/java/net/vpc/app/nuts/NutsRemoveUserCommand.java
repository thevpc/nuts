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
public interface NutsRemoveUserCommand {

    NutsSession getSession();

    boolean isTrace();

    NutsRemoveUserCommand session(NutsSession session);

    NutsRemoveUserCommand setSession(NutsSession session);

    NutsRemoveUserCommand setTrace(boolean trace);

    NutsRemoveUserCommand trace();

    NutsRemoveUserCommand trace(boolean trace);

    String getLogin();

    NutsRemoveUserCommand login(String login);

    NutsRemoveUserCommand setLogin(String login);

}

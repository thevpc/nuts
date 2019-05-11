/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositorySession;

/**
 *
 * @author vpc
 * @param <T>
 */
public abstract class NutsRepositoryCommandBase<T> {

    protected NutsRepository repo;
    private NutsRepositorySession session;

    public NutsRepositoryCommandBase(NutsRepository repo) {
        this.repo = repo;
    }

    //@Override
    protected T copyFromWorkspaceCommandBase(NutsRepositoryCommandBase other) {
        if (other != null) {
            this.session = other.getSession();
        }
        return (T) this;
    }

    //@Override
    public NutsRepositorySession getSession() {
        return session;
    }
    
    //@Override
    public T session(NutsRepositorySession session) {
        return setSession(session);
    }

    //@Override
    public T setSession(NutsRepositorySession session) {
        this.session = session;
        return (T) this;
    }



    protected void invalidateResult() {

    }

//    public NutsSession getValidSessionCopy() {
//        NutsSession s = getValidSession();
//        if (!sessionCopy) {
//            s = validSession = s.copy();
//            sessionCopy = true;
//        }
//        return s;
//    }
//
//    public NutsSession getValidSession() {
//        if (validSession == null) {
//            validSession = NutsWorkspaceUtils.validateSession(ws, getSession());
//            sessionCopy = true;
//        }
//        return validSession;
//    }

    
    protected NutsRepository getRepo() {
        return repo;
    }

    public abstract T run();
}

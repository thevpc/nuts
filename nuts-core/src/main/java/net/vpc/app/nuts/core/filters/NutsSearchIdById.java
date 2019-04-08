///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.vpc.app.nuts.core.filters;
//
//import net.vpc.app.nuts.NutsDescriptor;
//import net.vpc.app.nuts.NutsId;
//import net.vpc.app.nuts.NutsSearchId;
//import net.vpc.app.nuts.NutsVersion;
//import net.vpc.app.nuts.NutsWorkspace;
//
///**
// *
// * @author vpc
// */
//public class NutsSearchIdById implements NutsSearchId {
//
//    private NutsId id;
//
//    public NutsSearchIdById(NutsId id, NutsWorkspace ws) {
//        this.id = id;
//    }
//
//    @Override
//    public NutsVersion getVersion(NutsWorkspace ws) {
//        return id.getVersion();
//    }
//
//    @Override
//    public NutsId getId(NutsWorkspace ws) {
//        return id;
//    }
//
//    @Override
//    public NutsDescriptor getDescriptor(NutsWorkspace ws) {
//        return ws.fetch().id(id).getResultDescriptor();
//    }
//
//}

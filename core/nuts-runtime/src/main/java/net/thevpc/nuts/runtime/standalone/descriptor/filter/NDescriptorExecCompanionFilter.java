///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.thevpc.nuts.runtime.standalone.descriptor.filter;
//
//import net.thevpc.nuts.*;
//
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Set;
//
//import net.thevpc.nuts.util.NFilterOp;
//
///**
// *
// * @author thevpc
// */
//public class NDescriptorExecCompanionFilter extends AbstractDescriptorFilter {
//    private NId apiId;
//    private Set<String> companions;
//    public NDescriptorExecCompanionFilter(NId apiId, String[] shortIds) {
//        super(NFilterOp.CUSTOM);
//        this.apiId=apiId;
//        this.companions=new HashSet<>(Arrays.asList(shortIds));
//    }
//
//    @Override
//    public boolean acceptDescriptor(NDescriptor other) {
//        if(companions.contains(other.getId().getShortName())){
//            for (NDependency dependency : other.getDependencies()) {
//                if(dependency.toId().getShortName().equals(this.apiId.getShortName())){
//                    if(apiId==null){
//                        return true;
//                    }
//                    if(apiId.getVersion().equals(dependency.toId().getVersion())){
//                        return true;
//                    }
//                    return false;
//                }
//            }
//            return false;
//        }
//        return false;
//    }
//
//    @Override
//    public NDescriptorFilter simplify() {
//        return this;
//    }
//
//    @Override
//    public String toString() {
//        if(apiId==null){
//            return "companion";
//        }
//        return "companion("+ apiId.getVersion()+")";
//    }
//
//}
